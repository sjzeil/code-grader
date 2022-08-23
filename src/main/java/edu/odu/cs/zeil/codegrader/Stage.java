package edu.odu.cs.zeil.codegrader;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A stage is an area (directory) in which a version of the assignment,
 * either submitted or gold, can be built and run.
 */
public class Stage {

	private TestSuitePropertiesBase properties;
	private Assignment assignment;
	private Path stageDir;
	private Submission beingGraded;

	/**
	 * Error logging.
	 */
	private static Logger logger = LoggerFactory.getLogger(
			MethodHandles.lookup().lookupClass());

	/**
	 * Create a new stage for submitted code.
	 * 
	 * @param asst            the assignment that this test suite is intended to
	 *                        assess.
	 * @param submission      the submission being graded.
	 * @param suiteProperties info about the suite this is a stage for.
	 */
	public Stage(Assignment asst, Submission submission,
			TestSuitePropertiesBase suiteProperties) {
		beingGraded = submission;
		stageDir = (submission == null)
				? asst.getGoldStage()
				: asst.getSubmitterStage();
		assignment = asst;
		properties = suiteProperties;
	}

	/**
	 * Create a new gold version stage.
	 * 
	 * @param asst            the assignment that this test suite is intended to
	 *                        assess.
	 * @param suiteProperties info about the suite this is a stage for.
	 */
	public Stage(Assignment asst,
			TestSuitePropertiesBase suiteProperties) {
		beingGraded = null;
		stageDir = asst.getGoldStage();
		assignment = asst;
		properties = suiteProperties;
	}

	/**
	 * Delete the entire staging area after use.
	 * 
	 */
	public void clear() {
		if (stageDir.toFile().isDirectory()) {
			try {
				FileUtils.deleteDirectory(stageDir);
			} catch (IOException ex) {
				logger.warn("Problem clearing the staging directory "
						+ stageDir.toString(), ex);
			}
		}
	}

	public static class BuildResult {
		/**
		 * Status code of the build process (0 if OK).
		 */
		private int statusCode;

		/**
		 * Message/info. Should be non-empty if the build failed, but
		 * can be non-empty for successful builds.
		 */
		private String message;

		/**
		 * Create a build result.
		 * 
		 * @param code status code from the build process
		 * @param msg  output printed by the build process
		 */
		public BuildResult(int code, String msg) {
			statusCode = code;
			message = msg;
		}

		/**
		 * @return the statusCode
		 */
		public int getStatusCode() {
			return statusCode;
		}

		/**
		 * @return the message
		 */
		public String getMessage() {
			return message;
		}

	}

	/**
	 * If the instructor has provided a gold version of the program,
	 * build the code.
	 */
	public void buildGoldVersionIfAvailable() {
		Path goldDir = assignment.getGoldDirectory();
		if (goldDir != null) {
			if (goldDir.toFile().exists() && goldDir.toFile().isDirectory()) {
				Path goldStage = assignment.getGoldStage();
				if (!goldStage.toFile().exists()) {
					try {
						FileUtils.copyDirectory(goldDir, goldStage, null, null);
					} catch (IOException e) {
						throw new TestConfigurationError(
							"Cannot create staging directory for gold version\n"
							+ e.getMessage());
					}
					BuildResult result = buildCode(/* goldStage */);
					if (result.statusCode != 0) {
						throw new TestConfigurationError(
								"Gold code does not build\n"
										+ result.message);
					}
				}
			}
		}
	}

	/**
	 * Copy the required files into the gold stage.
	 */
	private void setupGoldVersion() {
		Path goldDir = assignment.getGoldDirectory();
		try {
			FileUtils.copyDirectory(goldDir, stageDir, 
				null, null, 
				StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new TestConfigurationError(
					"Cannot create staging directory for gold version \n"
							+ e.getMessage());
		}
	}

	/**
	 * Build the submitted code (in the staging area).
	 * 
	 * @return status code and messages from the build process
	 */
	public BuildResult buildCode() {
		String buildCommand = getBuildCommand();
		if (buildCommand == null || buildCommand.equals("")) {
			throw new TestConfigurationError(
					"Could not deduce build command in "
							+ stageDir.toString());
		}
		buildCommand = parameterSubstitution(buildCommand);

		ExternalProcess process = new ExternalProcess(
				stageDir,
				buildCommand,
				properties.build.timeLimit,
				null,
				"build process (" + buildCommand + ")");
		process.execute();
		String buildInfo = process.getOutput() + "\n\n" + process.getErr();
		if (process.getOnTime()) {
			if (process.getStatusCode() == 0) {
				return new BuildResult(0, buildInfo);
			} else {
				return new BuildResult(process.getStatusCode(),
						"Build failed with status code "
								+ process.getStatusCode()
								+ ".\n" + buildInfo);
			}
		} else {
			return new BuildResult(-1,
					"Build exceeded " + properties.build.timeLimit
							+ " seconds.\n" + buildInfo);
		}

	}

	/**
	 * Determine the command used to build the code.
	 * Can be set as a suite property or will attempt to infer the command
	 * from the stage directory contents.
	 * 
	 * @return a build command
	 * @throws TestConfigurationError if no build command can be determined.
	 */
	public String getBuildCommand() {
		String command = properties.build.command;
		if (command == null || command.equals("")) {
			// Try to infer the command from the contents of the
			// build directory.
			if (stageDir.resolve("makefile").toFile().exists()) {
				command = "make";
			} else if (stageDir.resolve("Makefile").toFile().exists()) {
				command = "make";
			} else if (stageDir.resolve("build.gradle").toFile().exists()) {
				String gradleCommandBase;
				if (stageDir.resolve("gradlew").toFile().exists()) {
					gradleCommandBase = "." + File.separator + "gradlew ";
				} else {
					gradleCommandBase = "gradle ";
				}
				command = gradleCommandBase + "jar";
			} else if (stageDir.resolve("pom.xml").toFile().exists()) {
				command = "mvn compile";
			} else if (stageDir.resolve("build.xml").toFile().exists()) {
				command = "ant";
			} else {
				List<File> javaDirs = FileUtils.findDirectoriesContaining(
						stageDir, ".java");
				if (javaDirs.size() > 0) {
					Pair<String, String> javaDetails = examineJavaSetup();
					command = "javac -g  " + javaDetails.getFirst()
							+ " " + javaDetails.getSecond();
				} else {
					List<File> cppDirs = FileUtils.findDirectoriesContaining(
							stageDir, ".cpp");
					if (cppDirs.size() > 0) {
						StringBuilder commandStr = new StringBuilder();
						commandStr.append("g++ -g -std=c++17 ");
						for (File srcDir : cppDirs) {
							commandStr.append(srcDir.toString()
									+ File.separator + "*.cpp ");
						}
						command = commandStr.toString();
					} else {
						List<File> pyFiles = FileUtils.findAllFiles(
								stageDir, ".py");
						if (pyFiles.size() > 0) {
							command = "echo Detected Python source files.";
						} else {
							command = ""; // Cannot infer build command
						}
					}
				}
			}
		}

		if (command == null || command.equals("")) {
			throw new TestConfigurationError(
					"Could not infer a build command for "
							+ stageDir.toString());
		} else {
			logger.info("Inferred build command: " + command
					+ "\n  in " + stageDir);
		}
		return command;
	}

	/**
	 * Examines the Java files within the stage and determines the ClassPath
	 * and source directories required for compilation and execution.
	 * 
	 * @return a pair of strings holding the classpath and source files paths.
	 */
	private Pair<String, String> examineJavaSetup() {
		List<File> javaFiles = FileUtils.findAllDeepFiles(
				stageDir, ".java");
		if (javaFiles.size() > 0) {
			StringBuilder classPath = new StringBuilder();
			boolean firstEntry = true;
			classPath.append("-cp ");
			StringBuilder sourcePaths = new StringBuilder();
			if (stageDir.resolve("lib").toFile().isDirectory()) {
				List<File> jars = FileUtils.findAllFiles(
						stageDir.resolve("lib"), ".jar");
				if (jars.size() > 0) {
					classPath.append(File.pathSeparatorChar);
					classPath.append("lib/*.jar");
					firstEntry = false;
				}
			}
			List<String> srcRoots = properties.build.javaSrcDir;
			if (srcRoots == null) {
				srcRoots = new ArrayList<>();
			}
			if (srcRoots.isEmpty()) {
				srcRoots.add(".");
			}
			for (String root : srcRoots) {
				if (!firstEntry) {
					classPath.append(File.pathSeparatorChar);
				}
				firstEntry = false;
				classPath.append(root);
			}
			if (javaFiles.isEmpty()) {
				javaFiles.add(new File("."));
			}
			for (File srcFile : javaFiles) {
				Path relativeDir = stageDir.relativize(srcFile.toPath());
				sourcePaths.append(' ');
				sourcePaths.append(relativeDir.toString());
			}
			// classPath.append("'");
			return new Pair<String, String>(
					classPath.toString(), sourcePaths.toString());
		} else {
			return new Pair<String, String>("", "");
		}
	}

	/**
	 * Set up the contents of a stage, in preparation for building.
	 * 
	 */
	public void setupStage() {
		stageDir.toFile().mkdirs();
		if (beingGraded == null) {
			// This is a gold stage. Copy everything from the gold directory.
			setupGoldVersion();
		} else {
			setupSubmitterStage();
		}
		arrangeJavaFiles();
	}

	private void setupSubmitterStage() {
		Path submittedSourceCode = beingGraded.getSubmissionDirectory();
		if (assignment.getInstructorCodeDirectory() != null) {
			try {
				FileUtils.copyDirectory(
						assignment.getInstructorCodeDirectory(),
						stageDir,
						properties.build.instructorFiles.include,
						properties.build.instructorFiles.exclude
						);
			} catch (IOException e) {
				throw new TestConfigurationError(
						"Could not copy instructor files from "
							+ assignment.getInstructorCodeDirectory().toString()
							+ " into " + stageDir.toString() + "\n"
							+ e.getMessage());
			}
		}
		try {
			FileUtils.copyDirectory(
					submittedSourceCode,
					stageDir,
					properties.build.studentFiles.include,
					properties.build.studentFiles.exclude,
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new TestConfigurationError(
					"Could not copy student files from "
							+ submittedSourceCode.toString()
							+ " into " + stageDir.toString() + "\n"
							+ e.getMessage());
		}
		arrangeJavaFiles();
	}

	/**
	 * Look for Java files that are out of position according to their
	 * package name and move them to the proper location.
	 * 
	 */
	private void arrangeJavaFiles() {
		List<File> javaFiles = FileUtils.findAllDeepFiles(stageDir, ".java");
		for (File javaFile : javaFiles) {
			String packageName = getJavaPackage(javaFile);
			if (notPlacedInPackage(javaFile, packageName)) {
				moveIntoPackage(javaFile, packageName);
			}
		}
	}

	private void moveIntoPackage(File javaFile, String packageName) {
		Path packagePath = Paths.get(packageName.replaceAll("\\.", "/"));
		Path stageSrcDir;
		if (properties.build.javaSrcDir.size() > 0) {
			String srcDir = properties.build.javaSrcDir.get(0);
			Path srcDirPath = Paths.get(srcDir);
			stageSrcDir = stageDir.resolve(srcDirPath);
		} else {
			stageSrcDir = stageDir;
		}
		Path desiredPackage = stageSrcDir.resolve(packagePath);
		Path desiredFile = desiredPackage.resolve(javaFile.getName());
		if (!desiredFile.equals(javaFile.toPath())) {
			if (!desiredPackage.toFile().exists()) {
				desiredPackage.toFile().mkdirs();
			}
			try {
				Files.move(javaFile.toPath(), desiredFile,
						StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				throw new TestConfigurationError("Unable to move "
						+ javaFile.toString() + " to "
						+ desiredFile.toString() + "\n"
						+ e.getMessage());
			}
		}
	}

	/**
	 * Tests to see if a Java file is not properly placed in a source
	 * directory, taking into consideration the package declaration in the
	 * Java file.
	 * 
	 * @param javaFile    a Java source file
	 * @param packageName The package it belongs to.
	 * @return true if this file needs to be moved
	 */
	private boolean notPlacedInPackage(File javaFile, String packageName) {
		Path stage = assignment.getSubmitterStage();
		Path packagePath = Paths.get(packageName.replaceAll("\\.", "/"));
		if (properties.build.javaSrcDir.size() > 0) {
			for (String srcDir : properties.build.javaSrcDir) {
				Path srcDirPath = Paths.get(srcDir);
				Path stageSrcDir = stage.resolve(srcDirPath);
				Path possiblePackage = stageSrcDir.resolve(packagePath);
				Path possibleFile = possiblePackage.resolve(javaFile.getName());
				if (possibleFile.equals(javaFile.toPath())) {
					return false;
				}
			}
		} else {
			Path stageSrcDir = stage;
			Path possiblePackage = stageSrcDir.resolve(packagePath);
			Path possibleFile = possiblePackage.resolve(javaFile.getName());
			if (possibleFile.equals(javaFile.toPath())) {
				return false;
			}
		}
		return true;
	}

	private String getJavaPackage(File javaFile) {
		String javaSourceCode = FileUtils.readTextFile(javaFile);
		javaSourceCode = javaSourceCode.replaceAll("//.*\n", "\n");
		javaSourceCode = javaSourceCode.replaceAll("(?s)/[*].*?[*]/", "");
		java.util.Scanner scanner = new java.util.Scanner(javaSourceCode);
		if (scanner.hasNext()) {
			if (scanner.next().equals("package")) {
				if (scanner.hasNext()) {
					String packageName = scanner.next();
					if (packageName.endsWith(";")) {
						packageName = packageName.substring(
								0, packageName.length() - 1);
					}
					scanner.close();
					return packageName;
				}
			}
		}
		scanner.close();
		return "";
	}


	/**
	 * @return the stage directory
	 */
	public Path getStageDir() {
		return stageDir;
	}

	/**
	 * Get the launch command for this stage. If not specified as a property,
	 * will attempt to indef a suitable launch command from the stage contents.
	 * 
	 * @param commandFromProperties
	 * @return the launch command, or "" if none could be inferred.
	 */
	public String getLaunchCommand(String commandFromProperties) {
		String command = commandFromProperties;
		if (command == null) {
			command = "";
		}
		if (command.equals("")) {
			// Try to infer the command from the contents of the
			// build directory.
			if (stageDir.resolve("build.gradle").toFile().exists()) {
				String gradleCommandBase;
				if (stageDir.resolve("gradlew").toFile().exists()) {
					gradleCommandBase = "." + File.separator + "gradlew ";
				} else {
					gradleCommandBase = "gradle ";
				}
				command = gradleCommandBase + "run --args='@P'";
			}
			if (command.equals("")) {
				List<File> javaDirs = FileUtils.findDirectoriesContaining(
						stageDir, ".java");
				if (javaDirs.size() > 0) {
					Pair<String, String> javaDetails = examineJavaSetup();
					String mainClassName = findJavaMainClass();
					command = "java  " + javaDetails.getFirst()
							+ " " + mainClassName;
				}
			}
			if (command.equals("")) {
				List<File> pyFiles = FileUtils.findAllFiles(
						stageDir, ".py");
				if (pyFiles.size() == 1) {
					command = "python3 " + pyFiles.get(0);
				} else {
					command = ""; // Cannot infer build command
				}
			}
			if (command.equals("")) {
				command = findExecutableFile(stageDir);
			}
			if (command.equals("")) {

				if (stageDir.resolve("makefile").toFile().exists()) {
					String makeContents = FileUtils.readTextFile(stageDir
						.resolve("makefile").toFile());
					if (makeContents.contains("run:")) {
						command = "make run args='@P'";
					}
				} else if (stageDir.resolve("Makefile").toFile().exists()) {
					String makeContents = FileUtils.readTextFile(stageDir
						.resolve("Makefile").toFile());
					if (makeContents.contains("run:")) {
						command = "make run args='@P'";
					}
				}
			}
			if (command == null || command.equals("")) {
				throw new TestConfigurationError(
						"Could not infer a launch command for "
								+ stageDir.toString());
			} else {
				logger.info("Inferred launch command: " + command
						+ "\n  in " + stageDir);
			}
		}
		return command;
	}

	private String findJavaMainClass() {
		String mainClassName = null;
		int mainCount = 0;
		File mainClass = null;
		Pattern huntForMain = Pattern.compile("void +main *[(]");
		for (File javaFile : FileUtils.findAllDeepFiles(
				stageDir, ".java")) {
			String sourceCode = FileUtils.readTextFile(javaFile);
			Matcher matcher = huntForMain.matcher(sourceCode);
			if (matcher.find()) {
				++mainCount;
				mainClass = javaFile;
			}
		}
		if (mainCount == 1 && mainClass != null) {
			mainClassName = classNameIn(mainClass);
		} else {
			mainClassName = "";
		}
		return mainClassName;
	}

	private String classNameIn(File mainClass) {
		String javaSourceCode = FileUtils.readTextFile(mainClass);
		javaSourceCode = javaSourceCode.replaceAll("//.*\n", "\n");
		javaSourceCode = javaSourceCode.replaceAll("(?s)/[*].*?[*]/", "");
		java.util.Scanner scanner = new java.util.Scanner(javaSourceCode);
		String packageName = "";
		if (scanner.hasNext()) {
			if (scanner.next().equals("package")) {
				if (scanner.hasNext()) {
					packageName = scanner.next();
					if (packageName.endsWith(";")) {
						packageName = packageName.substring(
								0, packageName.length() - 1);
					}
					scanner.close();
				}
			}
		}
		scanner.close();
		String className = mainClass.getName();
		className = className.replace(".java", "");
		if (packageName.equals("")) {
			return className;
		} else {
			return packageName + '.' + className;
		}
	}

	private String findExecutableFile(Path stage) {
		File[] files = stage.toFile().listFiles();
		if (files != null) {
			File executable = null;
			for (File file : files) {
				if (file.canExecute()) {
					if (executable == null) {
						executable = file;
					} else {
						return "";
					}
				}
			}
			if (executable != null) {
				return executable.getAbsolutePath();
			} else {
				return "";
			}
		} else {
			return "";
		}
	}

	/**
	 * Scans a string for shortcuts, replacing by the appropriate string.
	 * Shortcuts are
	 * <ul>
	 * <li>@P the test command line parameters</li>
	 * <li>@S the staging directory</li>
	 * <li>@T the test suite directory</li>
	 * <li>@t the test case name</li>
	 * <li>@R the reporting directory</li>
	 * </ul>
	 * A shortcut must be followed by a non-alphabetic character.
	 * 
	 * @param launchCommandStr a string describing a command to be run
	 * @return the launchCommandStr with shortcuts replaced by the appropriate
	 *         path/value
	 */
	public String parameterSubstitution(String launchCommandStr) {
		StringBuilder result = new StringBuilder();
		int i = 0;
		while (i < launchCommandStr.length()) {
			char c = launchCommandStr.charAt(i);
			if (c == '@') {
				if (i + 1 < launchCommandStr.length()) {
					char c2 = launchCommandStr.charAt(i + 1);
					if (c2 == 'S' || c2 == 'R') {
						boolean ok = (i + 2 >= launchCommandStr.length())
								|| !Character.isAlphabetic(
										launchCommandStr.charAt(i + 2));
						if (ok) {
							i += 2;
							try {
								if (c2 == 'S') {
									result.append(
											getStageDir()
													.toRealPath().toString());
								} else if (c2 == 'R') {
									result.append(
											beingGraded.getRecordingDir()
													.toRealPath().toString());
								}
							} catch (IOException ex) {
								// Path has not been set
								i -= 1;
								result.append(c);
							}
						} else {
							i += 1;
							result.append(c);
						}
					} else {
						i += 1;
						result.append(c);
					}
				} else {
					result.append(c);
					++i;
				}
			} else {
				result.append(c);
				++i;
			}
		}
		return result.toString();
	}

}
