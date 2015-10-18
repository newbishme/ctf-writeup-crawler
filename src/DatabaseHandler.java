import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
        createDatabaseStructureIfRequired();
        verifyDatabaseStructure();
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
            System.out.println("Connecting to Database...");
            dbCon = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            st = dbCon.createStatement();
            rs = st.executeQuery("SELECT VERSION()");

            if (rs.next()) {
                System.out.println("Database connected, version: " + rs.getString(1));
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
    
    private void createDatabaseStructureIfRequired() {
        try {
            if (dbCon == null || dbCon.isClosed()) {
                System.out.println("Database connection not found, or is closed");
                connectDatabase();
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        
        try {
            Statement stmt = dbCon.createStatement();
            String sql =    "CREATE TABLE IF NOT EXISTS CTFWRITEUPS " +
                            "(ID            BIGSERIAL  PRIMARY KEY  NOT NULL," +
                            "URL            TEXT                    NOT NULL," + 
                            "RESPONSETIME   TEXT                    NOT NULL," +
                            "CATEGORIES     TEXT[]                          )";
            int rowsUpdated = stmt.executeUpdate(sql);
            stmt.close();
            
            if (rowsUpdated > 0) {
                System.out.println("Successfully created table CTFWRITEUPS.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private boolean verifyDatabaseStructure() {
        CtfCrawlEntry testEntry = generateTestCrawlEntry();
        boolean isSuccess = insertToCTFCrawler(testEntry);
        isSuccess = isSuccess && deleteFromCTFCrawler(testEntry);
        
        if (isSuccess) { 
            System.out.println("Database Structure is verified.");
        }
        
        return isSuccess;
    }
    
    private CtfCrawlEntry generateTestCrawlEntry() {
        String url = "TEST://ctftime.org";
        String response = "999999999ms";
        String[] tags = {"TEST","ENTRY","ONLY"};
        CtfCrawlEntry testEntry = new CtfCrawlEntry(url, response, tags);
        return testEntry;
    }
    
    public boolean insertToCTFCrawler(CtfCrawlEntry entry) {
        boolean isInserted = false;
        isInserted = insertToCTFCrawler(entry.getUrl(), entry.getResponse(), entry.getTags());
        return isInserted;
    }
    
    public boolean insertToCTFCrawler(String url, String response, String[] tags) {
        boolean isInserted = false;
        
        try {
            if (dbCon == null || dbCon.isClosed()) {
                System.out.println("Database connection not found, or is closed");
                connectDatabase();
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        
        try {
            String sql =    "INSERT INTO CTFWRITEUPS (URL,RESPONSETIME,CATEGORIES) " + 
                            "VALUES (?,?,?)";
            PreparedStatement statement = dbCon.prepareStatement(sql);
            int index = 1;
            statement.setString(index++, url);
            statement.setString(index++, response);
            statement.setArray(index++, dbCon.createArrayOf("text", tags));
            int rowsUpdated = statement.executeUpdate();

            System.out.println("Rows updated from INSERT statement: " + rowsUpdated);             
            isInserted = true;
        } catch (SQLException e) {
            isInserted = false;
            e.printStackTrace();
        }
        
        return isInserted;
    }
    
    public boolean deleteFromCTFCrawler(CtfCrawlEntry entry) {
        boolean isDeleted = false;
        isDeleted = deleteFromCTFCrawler(entry.getUrl(), entry.getResponse(), entry.getTags());
        return isDeleted;
    }
    
    public boolean deleteFromCTFCrawler(String url, String response, String[] tags) {
        boolean isDeleted = false;

        try {
            if (dbCon == null || dbCon.isClosed()) {
                System.out.println("Database connection not found, or is closed");
                connectDatabase();
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        
        try {
            String sql =    "DELETE from CTFWRITEUPS " + 
                            "WHERE  URL=? AND RESPONSETIME=? AND CATEGORIES=?";
            PreparedStatement statement = dbCon.prepareStatement(sql);
            int index = 1;
            statement.setString(index++, url);
            statement.setString(index++, response);
            statement.setArray(index++, dbCon.createArrayOf("text", tags));
            int rowsUpdated = statement.executeUpdate();

            System.out.println("Rows updated from DELETE statement: " + rowsUpdated);             
            isDeleted = true;
        } catch (SQLException e) {
            isDeleted = false;
            e.printStackTrace();
        }
        
        return isDeleted;
    }
    
    
    //  Method to test database handler methods
    public static void main(String[] args) {
        DatabaseHandler.isDatabaseAlive();
        DatabaseHandler dbHandler = new DatabaseHandler();
    }
}