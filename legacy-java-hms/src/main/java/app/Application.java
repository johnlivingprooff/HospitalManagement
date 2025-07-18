package app;

import app.core.ApplicationRunner;
import app.util.SparkSessionUtils;
import lib.gintec_rdl.jini.Jini;
import org.apache.log4j.*;

import java.io.File;

public final class Application {

    private static final Configuration CONFIGURATION;

    // Load configurations
    static {
        CONFIGURATION = Jini.load(".ini", Configuration.class);
    }

    public static void main(String[] args) throws Exception {
        Logger root = Logger.getRootLogger();

        SparkSessionUtils.setSCookieName("mihr_hmis");

        if (CONFIGURATION.DebugMode) {
            root.setLevel(Level.DEBUG);
            root.addAppender(new ConsoleAppender(new TTCCLayout()));
        } else {
            root.setLevel(Level.WARN);
        }

        if (CONFIGURATION.EnableLogging) {
            root.addAppender(new RollingFileAppender(new TTCCLayout(),
                    new File(CONFIGURATION.LogDirectory,
                            HMSApplication.class.getCanonicalName() + ".log").getAbsolutePath()));
        }

        new ApplicationRunner<>(HMSApplication.class)
                .context(CONFIGURATION)
                .run();
    }
}
