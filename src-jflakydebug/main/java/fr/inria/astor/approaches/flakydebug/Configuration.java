package fr.inria.astor.approaches.flakydebug;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @brief Reads configurations from `config.properties` in runtime. Allowing to modify setups more easily.
 * @author Pedro Itiro Nagao
 */
public class Configuration {
    /**
     * Permutation range for shuffle operators
     */
    private float shuffleRange = 0.2f;
    
    /**
     * JUnit executions
     */
    private int jUnitExecutions = 100;

    private static final String filePath = "src-jflakydebug/main/java/fr/inria/astor/approaches/flakydebug/config.properties";
    private Configuration() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(filePath)) {
            properties.load(input);

            this.shuffleRange = Math.max(Math.min(Float.parseFloat(properties.getProperty("shuffle_range", "0.2f")), 1.0f), 0.2f);
            this.jUnitExecutions = Math.max(Integer.parseInt(properties.getProperty("junit_executions", "100")), 1);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static class Helper {
        private static final Configuration INSTANCE = new Configuration();
    }

    public static Configuration getInstance() {
        return Helper.INSTANCE;
    }

    /**
     * JUnit executions
     */
    public int getExecutions() {
        return this.jUnitExecutions;
    }

    /**
     * Permutation range for shuffle operators
     */
    public float getShuffleRange() {
        return this.shuffleRange;
    }

}
