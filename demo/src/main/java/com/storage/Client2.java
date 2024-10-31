package com.storage;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class Client2 {

    public static void main(String[] args) {
        try {
            // Conectar al registro RMI y buscar el servicio PasswordManager
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            PasswordManager manager = (PasswordManager) registry.lookup("PasswordManager");

            // Obtener todos los usuarios de la base de datos
            List<String> users = manager.getAllUsers();

            // Mostrar los usuarios en la consola
            System.out.println("=== Usuarios en la Base de Datos ===");
            if (users.isEmpty()) {
                System.out.println("La base de datos está vacía.");
            } else {
                for (String user : users) {
                    System.out.println("Usuario: " + user);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
