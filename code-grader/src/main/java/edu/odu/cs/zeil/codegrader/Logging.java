package edu.odu.cs.zeil.codegrader;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.spi.LoggerContext;

public final class Logging {

    /**
     * Set up logging for this application.
     * 
     * @throws IOException
     */
    public static void setup(Path logDirectory) throws IOException {
        // ADAPTED from https://logging.apache.org/log4j/2.x/manual/customconfig.html
        ConfigurationBuilder< BuiltConfiguration > builder = ConfigurationBuilderFactory.newConfigurationBuilder();

        builder.setStatusLevel( Level.ERROR);
        builder.setConfigurationName("RollingBuilder");
        // create a console appender
        AppenderComponentBuilder appenderBuilder = builder.newAppender("Stdout", "CONSOLE").addAttribute("target",
        ConsoleAppender.Target.SYSTEM_OUT);
        appenderBuilder.add(builder.newLayout("PatternLayout")
            .addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable"));
        builder.add( appenderBuilder );
        // create a rolling file appender
        LayoutComponentBuilder layoutBuilder = builder.newLayout("PatternLayout")
            .addAttribute("pattern", "%d [%t] %-5level: %msg%n");
        ComponentBuilder triggeringPolicy = builder.newComponent("Policies")
            .addComponent(builder.newComponent("CronTriggeringPolicy").addAttribute("schedule", "0 0 * * * ?"));
        String fileName = logDirectory.resolve("codegrader.log").toString();
        String rolledFilePattern = fileName.replace("codegrader.log", "codegrader-%d{MM-dd-yy}.log.gz");
        
        appenderBuilder = builder.newAppender("rolling", "RollingFile")
            .addAttribute("fileName", fileName)
            .addAttribute("filePattern", rolledFilePattern)
            .add(layoutBuilder)
            .addComponent(triggeringPolicy);
        builder.add(appenderBuilder);

        // create the new logger
        builder.add( builder.newLogger( "TestLogger", Level.INFO )
            .add( builder.newAppenderRef( "rolling" ) )
            .addAttribute( "additivity", false ) );

        builder.add( builder.newRootLogger( Level.DEBUG )
            .add( builder.newAppenderRef( "rolling" ) ) );
        LoggerContext ctx = Configurator.initialize(builder.build());
    }

    private Logging() { }
}
