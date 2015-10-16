import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * DatabaseHandler is the controller for managing the backend Postgres database.
 * Please ensure the following is configured on the machine before running this.
 * - Ensure that Postgres server is installed and running on machine
 * - Ensure that there is a database named: ctfcrawler
 * - Ensure that there is a postgres user/password: ctfcrawler/ctfcrawler 
 */
public class DatabaseHandler {

    private static final String dbUrl = "jdbc:postgresql://localhost/ctfcrawler";
    private static final String dbUser = "ctfcrawler";
    private static final String dbPassword = "ctfcrawler";
    
    private Connection dbCon = null;
    
    public DatabaseHandler() {
        connectDatabase();
        //createDatabaseStructureIfRequired();
        //verifyDatabaseStructure();
    }
    
    public static boolean isDatabaseAlive() {
        boolean isAlive = false;
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        
        try {
            con = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            st = con.createStatement();
            rs = st.executeQuery("SELECT VERSION()");

            if (rs.next()) {
                System.out.println("Database is up and alive.");
            }
            
            isAlive = true;
        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(DatabaseHandler.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                Logger lgr = Logger.getLogger(DatabaseHandler.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        
        return isAlive;
    }
    
    private void connectDatabase() {
        Statement st = null;
        ResultSet rs = null;
        
        try {
            dbCon = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            st = dbCon.createStatement();
            rs = st.executeQuery("SELECT VERSION()");

            if (rs.next()) {
                System.out.println(rs.getString(1));
            }

        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(DatabaseHandler.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
            } catch (SQLException ex) {
                Logger lgr = Logger.getLogger(DatabaseHandler.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
    
    //  Method to test database handler methods
    public static void main(String[] args) {
        DatabaseHandler.isDatabaseAlive();
        DatabaseHandler dbHandler = new DatabaseHandler();
    }
}