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
	 * Create a new stage.
	 * 
	 * @param asst            the assignment that this test suite is intended to
	 *                        assess.
	 * @param submission      the submission being graded, or null if this is to be
	 *                        the gold version stage.
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
	 * Delete the entire staging area after use.
	 * 
	 */
	public void clear() {
		try {
			FileUtils.deleteDirectory(stageDir);
		} catch (IOException ex) {
			logger.warn("Problem clearing the staging directory "
					+ stageDir.toString(), ex);
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
	 * Copy the required files into the gold stage
	 */
	private void setupGoldVersion() {
		Path goldDir = assignment.getGoldDirectory();
		try {
			FileUtils.copyDirectory(goldDir, stageDir, null, null);
		} catch (IOException e) {
			throw new TestConfigurationError(
					"Cannot create staging directory for gold version\n"
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
		buildCommand = assignment.parameterSubstitution(buildCommand, "");

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
				if (stageDir.resolve("gradlew").toFile().exists()) {
					command = "." + File.separator + "gradlew build";
				} else {
					command = "gradle build";
				}
			} else if (stageDir.resolve("pom.xml").toFile().exists()) {
				command = "mvn compile";
			} else if (stageDir.resolve("build.xml").toFile().exists()) {
				command = "ant";
			} else {
				List<File> javaDirs = FileUtils.findDirectoriesContaining(
						stageDir, ".java");
				if (javaDirs.size() > 0) {
					StringBuilder commandStr = new StringBuilder();
					commandStr.append("javac ");
					if (stageDir.resolve("lib").toFile().isDirectory()) {
						List<File> jars = FileUtils.findAllFiles(
								stageDir.resolve("lib"), ".jar");
						if (jars.size() > 0) {
							commandStr.append("-cp ." + File.pathSeparator
									+ "'lib/*.jar' ");
						}
					}
					for (File srcDir : javaDirs) {
						Path relativeDir = stageDir.relativize(srcDir.toPath());
						commandStr.append(relativeDir.toString()
								+ File.separator + "*.java ");
					}
					command = commandStr.toString();
				} else {
					StringBuilder commandStr = new StringBuilder();
					commandStr.append("g++ -g -std=c++17 ");
					List<File> cppDirs = FileUtils.findDirectoriesContaining(
							stageDir, ".cpp");
					for (File srcDir : cppDirs) {
						commandStr.append(srcDir.toString()
								+ File.separator + "*.cpp ");
					}
					command = commandStr.toString();
				}
			}
		}
		if (command == null || command.equals("")) {
			throw new TestConfigurationError(
					"Could not infer a build command for "
							+ stageDir.toString());
		}
		return command;
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
		List<String> requiredStudentFiles = listRequiredStudentFiles();
		Path submittedSourceCode = beingGraded.getSubmissionDirectory();
		try {
			FileUtils.copyDirectory(
					assignment.getInstructorCodeDirectory(),
					stageDir,
					null,
					requiredStudentFiles);
		} catch (IOException e) {
			throw new TestConfigurationError(
					"Could not copy instructor files from "
							+ assignment.getInstructorCodeDirectory().toString()
							+ " into " + stageDir.toString() + "\n"
							+ e.getMessage());
		}
		try {
			FileUtils.copyDirectory(
				submittedSourceCode,
					stageDir,
					properties.build.studentFiles,
					null,
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
				Files.move(javaFile.toPath(), desiredFile);
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

	private List<String> listRequiredStudentFiles() {
		List<String> results = new ArrayList<>();
		for (String pattern : properties.build.studentFiles) {
			if (!pattern.contains("*")) {
				results.add(pattern);
			}
		}
		return results;
	}

	private void runGoldVersion(TestCaseProperties tcProperties) {
	}

	/**
	 * @return the stage directory
	 */
	public Path getStageDir() {
		return stageDir;
	}

	public String getLaunchCommand(String commandFromProperties) {
		String command = commandFromProperties;
		if (command == null || command.equals("")) {
			// Infer command
		}
		return command;
	}

}
