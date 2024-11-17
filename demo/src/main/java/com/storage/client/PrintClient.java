package com.storage.client;
import com.storage.server.PasswordManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class PrintClient {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
      
        String sessionToken = null;
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1101);
            PasswordManager printServer = (PasswordManager) registry.lookup("PasswordManager");

            while(true){
                System.out.print("Enter username: ");
                String username = scanner.nextLine();
                System.out.print("Enter password: ");
                String password = scanner.nextLine();
                
                System.out.println("Debug: Attempting to log in with username: " + username);
                sessionToken = printServer.login(username, password);

                if (sessionToken == null) {
                    System.out.println("Login failed. Please check your credentials.");
                    return;
                    }
                System.out.println("Login successful. Session token: " + sessionToken);
            
                while (true) {
                    System.out.println("\nChoose an action: print, queue, status, or logout");
                    String action = scanner.nextLine().toLowerCase();

                    try{ 
                        switch (action) {
                            case "print":
                                printServer.print("document.txt", "Printer1", sessionToken);
                                System.out.println("Print method completed.");
                                break;
                            case "queue":
                                printServer.queue("Printer1", sessionToken);
                                System.out.println("Queue method completed.");
                                break;
                            case "status":
                                printServer.status("Printer1", sessionToken);
                                System.out.println("Status method completed.");
                                break;
                            case "logout":
                                printServer.logout(sessionToken);
                                System.out.println("Logged out successfully.");
                                sessionToken = null; // Nullstiller token
                                break;
                            default:
                                System.out.println("Invalid action. Please try again.");
                                break;
                        }
                        if (action.equals("logout")) {
                            break; // Gå ut av indre løkke når brukeren logger ut
                        }
                    }
                    catch (RemoteException e){
                        System.out.println("Session expired or invalid. Please log in again.");
                        break;
                    }
                }
                if (sessionToken == null) {
                    // Hvis brukeren logger ut manuelt, gå ut av ytre løkke også
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}