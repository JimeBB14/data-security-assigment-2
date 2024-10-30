package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
    public static void main(String[] args) {
        try {
            PrintServerImpl server = new PrintServerImpl();
            Registry registry = LocateRegistry.createRegistry(1099); // Default RMI port
            registry.rebind("PrintServer", server);
            System.out.println("Print Server is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
