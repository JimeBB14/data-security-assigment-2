package com.storage.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PasswordManagerImpl extends UnicastRemoteObject implements PasswordManager {
    private static final String DB_URL = "jdbc:sqlite:user_management.db";
    @SuppressWarnings("unused")
    private boolean serverRunning = false;
    
    protected PasswordManagerImpl() throws RemoteException {
        super();
        initializeDatabase();
    }

    // Método para inicializar la base de datos y crear la tabla si no existe
    private void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                    "username TEXT PRIMARY KEY, " +
                    "password TEXT NOT NULL)";
            stmt.execute(createTableSQL);
            System.out.println("Table 'users' checked/created successfully.");
        } catch (SQLException e) {
            System.err.println("Error initializing the database.");
            e.printStackTrace();
        }
    }

    // Método de conexión a la base de datos
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    @Override
    public boolean authenticateUser(String username, String plainPassword) throws RemoteException {
        String query = "SELECT password FROM users WHERE username = ?";

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    return plainPassword.equals(storedPassword); // Comparación directa
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during authentication.");
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updatePassword(String username, String oldPassword, String newPassword) throws RemoteException {
        if (authenticateUser(username, oldPassword)) { // Verificación con contraseña
            String query = "UPDATE users SET password = ? WHERE username = ?";

            try (Connection conn = getConnection();
                    PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, newPassword); // Guarda la nueva contraseña en texto plano
                stmt.setString(2, username);
                int rowsAffected = stmt.executeUpdate();

                return rowsAffected > 0; // True si la actualización fue exitosa
            } catch (SQLException e) {
                System.err.println("Error updating password.");
                e.printStackTrace();
            }
        } else {
            System.err.println("Old password is incorrect.");
        }
        return false;
    }

    @Override
    public boolean addUser(String username, String password) throws RemoteException {
        String query = "INSERT INTO users (username, password) VALUES (?, ?)";

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            int rowsAffected = stmt.executeUpdate();

            return rowsAffected > 0; // Devuelve true si se insertó el usuario
        } catch (SQLException e) {
            System.err.println("Error adding user.");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteUser(String username) throws RemoteException {
        String query = "DELETE FROM users WHERE username = ?";

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            int rowsAffected = stmt.executeUpdate();

            return rowsAffected > 0; // Devuelve true si se eliminó el usuario
        } catch (SQLException e) {
            System.err.println("Error deleting user.");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isDatabaseEmpty() throws RemoteException {
        String query = "SELECT COUNT(*) AS total FROM users";

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int total = rs.getInt("total");
                return total == 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking if database is empty.");
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void clearUsers() throws RemoteException {
        String query = "DELETE FROM users";

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.executeUpdate();
            System.out.println("All users cleared from the database.");
        } catch (SQLException e) {
            System.err.println("Error clearing users.");
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getAllUsers() throws RemoteException {
        String query = "SELECT username FROM users";
        List<String> users = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving users.");
            e.printStackTrace();
        }
        return users;
    }
    // PRINT SERVER
    @Override
    public void print(String filename, String printer, String username, String password) throws RemoteException {
        if (!authenticateUser(username, password)) {
            System.out.println("Authentication failed for user: " + username);
            return;
        }
        System.out.println("Printing " + filename + " on " + printer);
    }
    @Override
    public void queue(String printer, String username, String password) throws RemoteException {
        if (!authenticateUser(username, password)) {
            System.out.println("Authentication failed for user: " + username);
            return;
        }
        // Simulate queuing behavior
        System.out.println("Listing queue for printer: " + printer);
        // For demo, just a static message; in a real scenario, return the actual queue
        System.out.println("Current print jobs for " + printer + ": None"); // Placeholder
    }

    @Override
    public void topQueue(String printer, int job, String username, String password) throws RemoteException {
        if (!authenticateUser(username, password)) {
            System.out.println("Authentication failed for user: " + username);
            return;
        }
        // Simulate moving job to the top of the queue
        System.out.println("Moving job " + job + " to the top of the queue for printer: " + printer);
    }

    @Override
    public void start() throws RemoteException {
        serverRunning = true;
        System.out.println("Print server started.");
    }

    @Override
    public void stop() throws RemoteException {
        serverRunning = false;
        System.out.println("Print server stopped.");
    }

    @Override
    public void restart() throws RemoteException {
        stop();
        start();
    }

    @Override
    public void status(String printer) throws RemoteException {
        // Simulate printing status
        System.out.println("Status of printer " + printer + ": Ready");
    }

    @Override
    public void readConfig(String parameter) throws RemoteException {
        // Simulate reading a configuration parameter
        System.out.println("Config parameter " + parameter + ": value");
    }

    @Override
    public void setConfig(String parameter, String value) throws RemoteException {
        // Simulate setting a configuration parameter
        System.out.println("Setting config " + parameter + " to " + value);
    }


}
