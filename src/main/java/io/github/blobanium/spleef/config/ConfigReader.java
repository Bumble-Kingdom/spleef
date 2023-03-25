package io.github.blobanium.spleef.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


    public class ConfigReader {
        //variables;
        public static boolean refreshingConfig = false;

        //configs
        public static boolean database = false;
        public static String db_url = "";
        public static String db_username = "";
        public static String db_password = "";
        public static String db_table_name = "bumblespleefData";

        public static final Logger LOGGER = LogManager.getLogger("Bumble Spleef");

        public static void configRegister(boolean initialize){
            LOGGER.info("Registering config..");
            SimpleConfig CONFIG = SimpleConfig.of("BumbleSpleef").provider(ConfigReader::ltProvider).request();

            if(initialize) {
                database = CONFIG.getOrDefault("database", database);
                db_url = CONFIG.getOrDefault("database_url", db_url);
                db_username = CONFIG.getOrDefault("database_username", db_username);
                db_password = CONFIG.getOrDefault("database_password", db_password);
                db_table_name = CONFIG.getOrDefault("database_table_name", db_table_name);
            }

            LOGGER.info("Regestering done!");
        }

        private static String ltProvider(String filename) {
            return "#Bumble Spleef Config File."
                    + "\ndatabase=" + database
                    + "\ndatabase_url=" + db_url
                    + "\ndatabase_username=" + db_username
                    + "\ndatabase_password=" + db_password
                    + "\ndatabase_table_name=" + db_table_name;

        }
}
