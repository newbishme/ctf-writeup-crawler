import java.util.Scanner;

/*
 * Database Frontend for the ctf crawler database.
 */
public class DatabaseFrontend {
    private DatabaseHandler dbHandler;
    private Scanner sc;
    
    public static void main(String[] args) {
        DatabaseFrontend frontend = new DatabaseFrontend();
        frontend.runUntilExitOrCrash();
    }
    
    public DatabaseFrontend() {
        dbHandler = new DatabaseHandler();
        sc = new Scanner(System.in);
        System.out.println("Welcome to CTF Writeup Database Frontend.");
    }
    
    private void runUntilExitOrCrash() {
        boolean willRun = true;
        
        while (willRun) {
            showAllOptions();
            String input = readUserInput();
            processUserInput(input);
        }
    }
    
    private void showAllOptions() {
        System.out.println("====================================");
        System.out.println("Select an option:");
        System.out.println("1. Show all entries in the database.");
        System.out.println("2. Show all categories in the database.");
        System.out.println("3. Show all entries filtered by domain name.");
        System.out.println("4. Show all entries filtered by a category.");
        System.out.println("5. Exit");
        System.out.println("====================================");
    }
    
    private String readUserInput() {
        String userInput = "";
        boolean willRun = true;
        
        while (willRun) {
            userInput = sc.nextLine();
            
            switch (userInput) {
            case "1":
            case "2":
            case "3":
            case "4":
            case "5":
                willRun = false;
                break;
            default:
                System.out.println("Please enter a valid option.");
                break;
            }
        }
        
        return userInput;
    }
    
    private void processUserInput(String input) {
        String newInput = "";
        switch (input) {
        case "1":
            System.out.println("Displaying all entries in database.");
            dbHandler.showAllEntries();
            break;
        case "2":
            System.out.println("Displaying all categories in database.");
            dbHandler.showAllCategories();
            break;
        case "3":
            while ("".equals(newInput) || newInput == null) {
                System.out.println("Enter the domain name to search for.");
                newInput = sc.nextLine();
            }
            
            dbHandler.showEntriesWithDomainName(newInput);
            break;
        case "4":
            while ("".equals(newInput) || newInput == null) {
                System.out.println("Enter a category to search for.");
                newInput = sc.nextLine();
            }
            
            dbHandler.showEntriesWithCategory(newInput);
            break;
        case "5":
            sc.close();
            System.out.println("Program ending.");
            System.exit(0);
            break;
        default:
            sc.close();
            System.out.println("System error.");
            System.exit(1);
        }
        
        System.out.println("FINISH PROCESSING USER INPUT.");
        System.out.println("");
    }
}
