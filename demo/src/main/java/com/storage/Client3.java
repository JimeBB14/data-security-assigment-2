package com.storage;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client3 {
    public static void main(String[] args) {
        try {
            // Conectar al registro RMI y buscar el servicio PasswordManager
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            PasswordManager manager = (PasswordManager) registry.lookup("PasswordManager");

            // Iniciar sesión
            String username = "testUser1";
            String password = "password123";
            String sessionId = manager.startSession(username, password);

            if (sessionId != null) {
                System.out.println("Sesión iniciada con éxito. Session ID: " + sessionId);

                // Ejemplo de actualización de contraseña
                String newPassword = "newPassword123";
                boolean isUpdated = manager.updatePassword(username, password, newPassword, sessionId);
                System.out.println("Contraseña actualizada: " + isUpdated);

                // Terminar la sesión
                manager.endSession(sessionId);
                System.out.println("Sesión finalizada.");
            } else {
                System.out.println("Fallo en la autenticación.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
