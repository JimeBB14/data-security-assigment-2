// package com.storage;

// import java.rmi.registry.LocateRegistry;
// import java.rmi.registry.Registry;

// public class PasswordManagerClient {
//     public static void main(String[] args) {
//         try {
//             // Conectar al registro RMI y buscar el servicio PasswordManager
//             Registry registry = LocateRegistry.getRegistry("localhost", 1099);
//             PasswordManager manager = (PasswordManager) registry.lookup("PasswordManager");

//             // Verificar si la base de datos está vacía y vaciarla si no lo está
//             if (!manager.isDatabaseEmpty()) {
//                 System.out.println("La base de datos no está vacía. Procediendo a vaciarla...");
//                 manager.clearUsers();
//             }

//             // Lista de usuarios para pruebas
//             String[] usernames = {"testUser1", "testUser2", "testUser3"};
//             String initialPassword = "password123";
//             String newPassword = "newPassword456";

//             // 1. Crear varios usuarios
//             System.out.println("=== Creando Usuarios ===");
//             for (String username : usernames) {
//                 boolean isAdded = manager.addUser(username, initialPassword);
//                 System.out.println("Usuario " + username + " creado: " + isAdded);
//             }

//             // 2. Autenticar a los usuarios y crear sesiones
//             System.out.println("\n=== Iniciando Sesiones ===");
//             String[] sessionIds = new String[usernames.length];
//             for (int i = 0; i < usernames.length; i++) {
//                 sessionIds[i] = manager.startSession(usernames[i], initialPassword);
//                 if (sessionIds[i] != null) {
//                     System.out.println("Sesión iniciada para " + usernames[i] + ". Session ID: " + sessionIds[i]);
//                 } else {
//                     System.out.println("Error al iniciar sesión para " + usernames[i]);
//                 }
//             }

//             // 3. Cambiar las contraseñas de los usuarios usando el sessionId
//             System.out.println("\n=== Actualizando Contraseñas ===");
//             for (int i = 0; i < usernames.length; i++) {
//                 String username = usernames[i];
//                 String sessionId = sessionIds[i];
//                 if (sessionId != null && manager.isSessionValid(sessionId)) {
//                     boolean isUpdated = manager.updatePassword(username, initialPassword, newPassword, sessionId);
//                     System.out.println("Contraseña actualizada para " + username + ": " + isUpdated);
//                 } else {
//                     System.out.println("La sesión para " + username + " no es válida o ha expirado.");
//                 }
//             }

//             // 4. Re-autenticar con la nueva contraseña y verificar la sesión
//             System.out.println("\n=== Re-autenticando con Nueva Contraseña ===");
//             for (int i = 0; i < usernames.length; i++) {
//                 String username = usernames[i];
//                 boolean isReAuthenticated = manager.authenticateUser(username, newPassword);
//                 System.out.println("Re-autenticación para " + username + " con nueva contraseña: " + isReAuthenticated);
//             }

//             // 5. Cerrar sesiones
//             System.out.println("\n=== Cerrando Sesiones ===");
//             for (String sessionId : sessionIds) {
//                 if (sessionId != null) {
//                     manager.endSession(sessionId);
//                     System.out.println("Sesión finalizada: " + sessionId);
//                 }
//             }

//             // 6. Verificación final: confirmar que los usuarios existen con la nueva contraseña
//             System.out.println("\n=== Verificación Final: Usuarios Presentes en la Base de Datos ===");
//             for (String username : usernames) {
//                 boolean isAuthenticated = manager.authenticateUser(username, newPassword);
//                 System.out.println("Usuario " + username + " autenticado con la nueva contraseña: " + isAuthenticated);
//             }

//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }
// }
package com.storage;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class PasswordManagerClient {
    public static void main(String[] args) {
        try {
            // Conectar al registro RMI y buscar el servicio PasswordManager
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            PasswordManager manager = (PasswordManager) registry.lookup("PasswordManager");

            // Verificar si la base de datos está vacía y vaciarla si no lo está
            if (!manager.isDatabaseEmpty()) {
                System.out.println("La base de datos no está vacía. Procediendo a vaciarla...");
                manager.clearUsers();
            }

            // Lista de usuarios para pruebas
            String[] usernames = {"testUser1", "testUser2", "testUser3"};
            String initialPassword = "password123";
            String newPassword = "newPassword456";

            // 1. Crear varios usuarios
            System.out.println("=== Creando Usuarios ===");
            for (String username : usernames) {
                boolean isAdded = manager.addUser(username, initialPassword);
                System.out.println("Usuario " + username + " creado: " + isAdded);
            }

            // 2. Autenticar a los usuarios y crear sesiones
            System.out.println("\n=== Iniciando Sesiones ===");
            String[] sessionIds = new String[usernames.length];
            for (int i = 0; i < usernames.length; i++) {
                sessionIds[i] = manager.startSession(usernames[i], initialPassword);
                if (sessionIds[i] != null) {
                    System.out.println("Sesión iniciada para " + usernames[i] + ". Session ID: " + sessionIds[i]);
                } else {
                    System.out.println("Error al iniciar sesión para " + usernames[i]);
                }
            }

            // Espera de 1 segundo para forzar la expiración de la sesión
            System.out.println("\n=== Esperando para que expire la sesión ===");
            Thread.sleep(1000);

            // 3. Intentar cambiar la contraseña de los usuarios con la sesión expirada
            System.out.println("\n=== Intentando Actualizar Contraseñas con Sesión Expirada ===");
            for (int i = 0; i < usernames.length; i++) {
                String username = usernames[i];
                String sessionId = sessionIds[i];
                boolean isUpdated = false;
                if (sessionId != null && manager.isSessionValid(sessionId)) {
                    isUpdated = manager.updatePassword(username, initialPassword, newPassword, sessionId);
                } else {
                    System.out.println("La sesión para " + username + " ha expirado. Necesita re-autenticación.");
                }
                
                // Re-autenticar si la sesión ha expirado
                if (!isUpdated) {
                    System.out.println("Re-autenticando al usuario " + username);
                    String newSessionId = manager.startSession(username, initialPassword);
                    if (newSessionId != null) {
                        boolean isReUpdated = manager.updatePassword(username, initialPassword, newPassword, newSessionId);
                        System.out.println("Contraseña actualizada para " + username + " después de re-autenticación: " + isReUpdated);
                    } else {
                        System.out.println("Error al re-autenticar al usuario " + username);
                    }
                }
            }

            // 4. Verificación final: confirmar que los usuarios existen con la nueva contraseña
            System.out.println("\n=== Verificación Final: Usuarios Presentes en la Base de Datos ===");
            for (String username : usernames) {
                boolean isAuthenticated = manager.authenticateUser(username, newPassword);
                System.out.println("Usuario " + username + " autenticado con la nueva contraseña: " + isAuthenticated);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
