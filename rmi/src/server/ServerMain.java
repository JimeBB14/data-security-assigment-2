package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
    public static void main(String[] args) {
        try {
            PrintServerImpl server = new PrintServerImpl();
            // Try to create a new registry on port 1099; if it exists, get the existing one
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(1099);
                System.out.println("Created new RMI registry on port 1099.");
            } catch (Exception e) {
                registry = LocateRegistry.getRegistry(1099);
                System.out.println("Connected to existing RMI registry on port 1099.");
            }

            // Bind the server instance to the registry
            registry.rebind("PrintServer", server);
            System.out.println("Print Server is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
