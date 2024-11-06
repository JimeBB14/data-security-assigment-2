package com.storage.client;
import com.storage.server.PasswordManager;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class PrintClient {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String host = "localhost"; // or server IP
        try {
            Registry registry = LocateRegistry.getRegistry(host);
            PasswordManager printServer = (PasswordManager) registry.lookup("PasswordManager");

            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            // Call the print method
            printServer.print("document.txt", "Printer1", username, password);

            // Call the queue method
            printServer.queue("Printer1", username, password);

            // Call the status method
            printServer.status("Printer1");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}