/**
 * 
 */
package edu.odu.cs.zeil.codegrader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * The automatic grader.
 * 
 * @author zeil
 *
 */
public class Grader {
	
	/**
	 * Configuration info for this assignment.
	 */
	private AssignmentProperties properties;

	/**
	 * Where is the assignment root?
	 */
	private Path assignmentDir = Paths.get(".");
	
	/**
	 * A YAML file describing the assignment configuration.
	 */
	private File settingsFile;
	
	/**
	 * First phase to perform.
	 */
    private int startPhase;
    
    /**
     * Last phase to perform.
     */
    private int stopPhase;
	
    /**
     * List of phases, in the order performed
     * @author zeil
     *
     */
    private enum PhaseNames {Setup, Build, Inspection, Test, Report, Reset};
    
    /**
     * All phases, in the order given by PhaseNames.
     */
    final private Phase[] phases = new Phase[PhaseNames.Reset.ordinal()+1];
    
	/**
	 * Configuration info for the assignment.
	 * @author zeil
	 *
	 */
	public static class AssignmentProperties extends ReflectiveProperties {
		/**
		 * Name for the assignment.
		 */
		public String name = "Assignment";
		
		/**
		 * Directory containing the properties file and, by default,
		 * the other grading info. 
		 */
		public PathProperty assignmentRoot;
		
		/**
		 * Configuration of the Setup phase.
		 */
		public SetupPhase.SetupProperties setup = new SetupPhase.SetupProperties();
		
		/**
		 * Configuration of the Examine phase.
		 */
		public ExaminePhase.ExamineProperties examine = new ExaminePhase.ExamineProperties();

		/**
		 * Configuration of the Build phase.
		 */
		public BuildPhase.BuildProperties build = new BuildPhase.BuildProperties();
		
		/**
		 * Configuration of the Test phase.
		 */
		public TestPhase.TestProperties test = new TestPhase.TestProperties();

		/**
		 * Configuration of the Report phase.
		 */
		public ReportPhase.ReportProperties report = new ReportPhase.ReportProperties();
	}
	


	/**
	 * Construct a grader object from the indicated command line parameters.
	 * @param params command line paramaters
	 * @throws IOException on inability to read settings file
	 */
	public Grader(final String[] params) throws IOException { // NOPMD by zeil on 4/7/20, 11:57 AM
		final String DefaultSettingsFileName = "Assignment.yaml"; // NOPMD by zeil on 4/7/20, 11:58 AM

		final char minusSign = '-';
		for (final String param: params) {
			if (param.charAt(0) != minusSign) {
				// Only positional parameter is the settings file locations
				settingsFile = new File(param);
				if (settingsFile.exists()) {
					if (settingsFile.isDirectory()) {
						settingsFile = new File(settingsFile, DefaultSettingsFileName);
					}
				} else {
					throw new IOException("Cannot find settings file " + param);
				}
			}
		}
		if (settingsFile == null) {
			settingsFile = assignmentDir.resolve(DefaultSettingsFileName).toFile();
			if (!settingsFile.exists()) {
				settingsFile = null; // NOPMD by zeil on 4/7/20, 12:02 PM
			}
		}
		if (settingsFile == null) {
		    properties = new AssignmentProperties();
		} else {
			assignmentDir = settingsFile.getParentFile().toPath();
			loadSettingsFile();
		}
		processPropertyParameters(params);
		
		phases[PhaseNames.Setup.ordinal()] = new SetupPhase(properties);
		phases[PhaseNames.Build.ordinal()] = new BuildPhase();
		phases[PhaseNames.Inspection.ordinal()] = new ExaminePhase();
		phases[PhaseNames.Test.ordinal()] = new TestPhase();
		phases[PhaseNames.Report.ordinal()] = new ReportPhase();
		phases[PhaseNames.Reset.ordinal()] = new ResetPhase();
		properties.setByReflection("assignmentRoot", assignmentDir.toString());
		
		processNamedCommandLineParameters(params);
	}

	private void processPropertyParameters(final String[] params) {
		for (final String param: params) {
			if (param.startsWith("-D")) {
				final String[] parts = param.split("=");
				if (parts.length != 2) { // NOPMD by zeil on 4/7/20, 12:06 PM
					throw new IllegalArgumentException("Cannot parse '" + param + "'");
				}
				final String propertyName = parts[0].substring(2); // Discard the "-D"
				final String propertyValue = parts[1];
				properties.setByReflection(propertyName, propertyValue);
			}
		}
	}

	private void processNamedCommandLineParameters(final String[] params) {
		startPhase = 0;
		stopPhase = phases.length-1;
		final String StartParamName = "--start="; // NOPMD by zeil on 4/7/20, 12:08 PM
		final String StopParamName = "--stop="; // NOPMD by zeil on 4/7/20, 12:08 PM
		for (final String param: params) {
			if (param.startsWith(StartParamName)) {
				final int phaseStart = identifyPhase(substringAfter(param, StartParamName));
				if (phaseStart >= 0) { 
					startPhase = phaseStart;
				} else {
					throw new IllegalArgumentException("'" + param + "' does not name a valid phase.");
				}
			} else if (param.startsWith(StopParamName)) {
				final int phaseStart = identifyPhase(substringAfter(param, StopParamName));
				if (phaseStart >= 0) { 
					stopPhase = phaseStart;
				} else {
					throw new IllegalArgumentException("'" + param + "' does not name a valid phase.");
				}
			}
		}
	}

	private int identifyPhase(final String phaseName) {
		final Locale locale = new Locale("en", "US");
		final String phaseNameLC = phaseName.toLowerCase(locale); // NOPMD by zeil on 4/7/20, 12:19 PM
		for (int i = 0; i < phases.length; ++i) {
			final Phase phase = phases[i];
			String pname = phase.getClass().getSimpleName().toLowerCase(locale);
			if (pname.length() > phaseNameLC.length()) {
				pname = pname.substring(0, phaseNameLC.length());
				if (phaseNameLC.equals(pname)) {
					return i; // NOPMD by zeil on 4/7/20, 12:22 PM
				}
			}
		}
		return -1;
	}

	private String substringAfter(final String str, final String prefix) {
		return str.substring(prefix.length());
	}

	private void loadSettingsFile() {
		final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		mapper.findAndRegisterModules();
		try {
		    properties = mapper.readValue(settingsFile, AssignmentProperties.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
		}
	}

	/**
	 * @param args
	 * @throws IOException if a settings file is named but does not exist or cannot be read.
	 */
	public static void main(final String[] args) throws IOException {
		Logging.setup();
		final boolean status = new Grader(args).runGrader();
		System.exit(status? 0 : 1);
	}

	/**
	 * Run the autograder.
	 * @return  success or failure
	 */
	public boolean runGrader() {
		boolean status = true;
		for (int i = startPhase; i <= stopPhase; ++i) {
			final Phase phase = phases[i];
			final boolean success = phase.runPhase();
			status = status & success;
		}
		return status;
	}

	/**
	 * @return the assignment directory
	 */
	public Path getAssignmentDirectory() {
		return assignmentDir;
	}

	/**
	 * @return the assignment name
	 */
	public String getName() {
		return properties.name;
	}

	/**
	 * @return the Setup phase
	 */
	public SetupPhase getSetupPhase() {
		return (SetupPhase)phases[PhaseNames.Setup.ordinal()];
	}


	/**
	 * @return the properties
	 */
	public AssignmentProperties getProperties() {
		return properties;
	}

	public BuildPhase getBuildPhase() {
		return (BuildPhase)phases[PhaseNames.Build.ordinal()];
	}


}
