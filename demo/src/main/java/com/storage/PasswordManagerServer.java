package com.storage;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class PasswordManagerServer {
    public static void main(String[] args) {
        try {
            PasswordManager manager = new PasswordManagerImpl();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("PasswordManager", manager);
            System.out.println("Password Manager Server is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
