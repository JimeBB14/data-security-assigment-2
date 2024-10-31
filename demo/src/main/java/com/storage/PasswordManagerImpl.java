package com.storage;

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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PasswordManagerImpl extends UnicastRemoteObject implements PasswordManager {
    private static final String DB_URL = "jdbc:sqlite:user_management.db";
    private static final int SESSION_DURATION_MINUTES = 1; // Duración de la sesión en minutos

    private Map<String, Session> sessions = new HashMap<>();

    protected PasswordManagerImpl() throws RemoteException {
        super();
        initializeDatabase();
    }

    // Método para iniciar sesión
    @Override
    public String startSession(String username, String password) throws RemoteException {
        if (authenticateUser(username, password)) {
            String sessionId = UUID.randomUUID().toString();
            LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(SESSION_DURATION_MINUTES);
            sessions.put(sessionId, new Session(sessionId, username, expiryTime));
            System.out.println("Session started for user: " + username);
            return sessionId;
        }
        return null; // Retorna null si la autenticación falla
    }

    // Método para verificar si la sesión es válida
    @Override
    public boolean isSessionValid(String sessionId) throws RemoteException {
        Session session = sessions.get(sessionId);
        if (session == null || session.isExpired()) {
            sessions.remove(sessionId); // Eliminar sesión si ha expirado
            return false;
        }
        return true;
    }

    // Método para terminar la sesión
    @Override
    public void endSession(String sessionId) throws RemoteException {
        sessions.remove(sessionId);
        System.out.println("Session ended: " + sessionId);
    }

    // Ejemplo de actualización de contraseña usando sesión
    @Override
    public boolean updatePassword(String username, String oldPassword, String newPassword, String sessionId)
            throws RemoteException {
        if (isSessionValid(sessionId)) {
            Session session = sessions.get(sessionId);
            if (session.getUsername().equals(username) && authenticateUser(username, oldPassword)) {
                String query = "UPDATE users SET password = ? WHERE username = ?";
                try (Connection conn = getConnection();
                        PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, newPassword);
                    stmt.setString(2, username);
                    int rowsAffected = stmt.executeUpdate();
                    return rowsAffected > 0; // Retorna true si se actualizó la contraseña
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false; // Retorna false si la sesión no es válida o la autenticación falla
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

    // @Override
    // public boolean updatePassword(String username, String oldPassword, String
    // newPassword) throws RemoteException {
    // if (authenticateUser(username, oldPassword)) { // Verificación con contraseña
    // actual
    // String query = "UPDATE users SET password = ? WHERE username = ?";

    // try (Connection conn = getConnection();
    // PreparedStatement stmt = conn.prepareStatement(query)) {

    // stmt.setString(1, newPassword); // Guarda la nueva contraseña en texto plano
    // stmt.setString(2, username);
    // int rowsAffected = stmt.executeUpdate();

    // return rowsAffected > 0; // True si la actualización fue exitosa
    // } catch (SQLException e) {
    // System.err.println("Error updating password.");
    // e.printStackTrace();
    // }
    // } else {
    // System.err.println("Old password is incorrect.");
    // }
    // return false;
    // }

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
}
