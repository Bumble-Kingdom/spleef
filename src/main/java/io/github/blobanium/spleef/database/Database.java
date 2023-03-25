package io.github.blobanium.spleef.database;

import io.github.blobanium.spleef.config.ConfigReader;
import net.minecraft.server.network.ServerPlayerEntity;

import java.sql.*;

public class Database {
    //DB Vars
    public static boolean isactive = false;

    //Connection vars
    static Connection connection; //This is the variable we will use to connect to database

    public static void onEnable(){
        try { // try catch to get any SQL errors (for example connections errors)
            connection = DriverManager.getConnection(
                    ConfigReader.db_url,
                    ConfigReader.db_username,
                    ConfigReader.db_password);
            // with the method getConnection() from DriverManager, we're trying to set
            // the connection's url, username, password to the variables we made earlier and
            // trying to get a connection at the same time. JDBC allows us to do this.
            isactive = true;
            SqlActions.createTableIfNotExists();
        } catch (SQLException e) {
            onDisable();
            isactive = false;// catching errors
            e.printStackTrace(); // prints out SQLException errors to the console (if any)
        }
    }

    public static void onDisable() {
        // invoke on disable.
        try { // using a try catch to catch connection errors (like wrong sql password...)
            if (connection!=null && !connection.isClosed()){ // checking if connection isn't null to
                // avoid receiving a nullpointer
                connection.close(); // closing the connection field variable.
                isactive = false;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static class SqlActions{
        public static void createTableIfNotExists(){
            String sql = "CREATE TABLE IF NOT EXISTS `" + ConfigReader.db_table_name + "` (`UUID` varchar(36) NOT NULL, `currencyAmmount` int(11) NOT NULL DEFAULT 0, `isOnline` tinyint(1) NOT NULL );";
            try {
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public static void onPlayerJoin(ServerPlayerEntity player){
            String sql = "INSERT IGNORE INTO " + ConfigReader.db_table_name + "(UUID, currencyAmmount, isOnline) VALUES (?, 0, 1) ON DUPLICATE KEY UPDATE isOnline=1";
            try{
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setString(1, player.getUuid().toString());
                stmt.executeUpdate();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }

        public static void parseCurrencyUpdate(ServerPlayerEntity player, int ammount){

        }
    }
}
