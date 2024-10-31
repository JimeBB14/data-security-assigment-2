package com.storage;
public class main {
    public static void main(String[] args) {
        try {
            PasswordManagerImpl manager = new PasswordManagerImpl();
            
            // Prueba de creación de usuario
            boolean isAdded = manager.addUser("manualTestUser", "password123");
            System.out.println("Usuario manualTestUser creado: " + isAdded);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
}
