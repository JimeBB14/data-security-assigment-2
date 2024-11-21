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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.storage.util.HashingUtil;

public class PasswordManagerImpl extends UnicastRemoteObject implements PasswordManager {
    private static final String DB_URL = "jdbc:sqlite:user_management.db";
    @SuppressWarnings("unused")
    private boolean serverRunning = false;
    private final long SESSION_TIMEOUT = 5 * 60 * 1000;
    private Map<String, Long> activeSessions = new HashMap<>();
    private Map<String, String> sessionUserMap = new HashMap<>(); 

    protected PasswordManagerImpl() throws RemoteException {
        super();
        initializeDatabase();
    }

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

    // Database connection
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private String validateSession(String sessionToken) throws RemoteException {
        System.out.println("Debug: Validating session token: " + sessionToken);
        Long expirationTime = activeSessions.get(sessionToken);
    
        if (expirationTime == null) {
            System.out.println("Session token not found: " + sessionToken);
            throw new RemoteException("Session token not found. Please log in again.");
        }
    
        if (System.currentTimeMillis() > expirationTime) {
            System.out.println("Debug: Session token expired: " + sessionToken);
            activeSessions.remove(sessionToken);
            sessionUserMap.remove(sessionToken); // Ensure the user mapping is also cleaned up
            throw new RemoteException("Session expired. Please log in again.");
        }
    
        // Renew session expiration time
        activeSessions.put(sessionToken, System.currentTimeMillis() + SESSION_TIMEOUT);
        System.out.println("Session renewed for token: " + sessionToken);
    
        // Return the username associated with this session token
        return sessionUserMap.get(sessionToken); // Ensure sessionUserMap is properly maintained
    }
    
    private void checkPermission(String sessionToken, String action) throws RemoteException {
        String user = validateSession(sessionToken); 
        boolean hasPermission = PasswordManagerServer.hasPermission(user, action);
    
        if (!hasPermission) {
            System.out.println("Unauthorized access: User " + user + " does not have permission for action: " + action);
            throw new RemoteException("Access denied for user: " + user + " for action: " + action);
        }
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
                    return HashingUtil.validatePassword(plainPassword, storedPassword);
                } else {
                    System.out.println("Debug: User was not found in the database.");
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
        if (authenticateUser(username, oldPassword)) {
            String query = "UPDATE users SET password = ? WHERE username = ?";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                byte[] salt = HashingUtil.generateSalt();
                String hashedPassword = HashingUtil.hashPassword(newPassword, salt);
               
                stmt.setString(1, hashedPassword);
                stmt.setString(2, username);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
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
        
        byte[] salt = HashingUtil.generateSalt();
        String hashedPassword = HashingUtil.hashPassword(password, salt);
        stmt.setString(1, username);
        stmt.setString(2, hashedPassword);
        int rowsAffected = stmt.executeUpdate();
        return rowsAffected > 0;
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

            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user.");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String login(String username, String password) throws RemoteException {
        if (authenticateUser(username, password)) {
            activeSessions.entrySet().removeIf(entry -> sessionUserMap.get(entry.getKey()).equals(username));
            sessionUserMap.values().removeIf(value -> value.equals(username));
    
        
            String sessionToken = UUID.randomUUID().toString();
            activeSessions.put(sessionToken, System.currentTimeMillis() + SESSION_TIMEOUT);
            sessionUserMap.put(sessionToken, username);
    
            System.out.println("Login successful. Session token: " + sessionToken);
            System.out.println("Debug: Attempting to log in with username: " + username);
            System.out.println("Debug: Received session token: " + sessionToken);
            return sessionToken;
        }
        return null;
    }

    @Override
    public void logout(String sessionToken) throws RemoteException {
        if (activeSessions.remove(sessionToken) != null) {
            sessionUserMap.remove(sessionToken);
            System.out.println("Session " + sessionToken + " has been logged out.");
        } else {
            System.out.println("Invalid session token. Logout failed.");
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

    @Override
public void print(String filename, String printer, String sessionToken) throws RemoteException {
    System.out.println("entro al print");
    checkPermission(sessionToken, "print");
    System.out.println("Printing " + filename + " on " + printer);
}

@Override
public void queue(String printer, String sessionToken) throws RemoteException {
    checkPermission(sessionToken, "queue");
    System.out.println("Listing queue for printer: " + printer);
}

@Override
public void topQueue(String printer, int job, String sessionToken) throws RemoteException {
    checkPermission(sessionToken, "topQueue");
    System.out.println("Moving job " + job + " to the top of the queue for printer: " + printer);
}

@Override
public void start(String sessionToken) throws RemoteException {
    checkPermission(sessionToken, "start");
    serverRunning = true;
    System.out.println("Print server started.");
}

@Override
public void stop(String sessionToken) throws RemoteException {
    checkPermission(sessionToken, "stop");
    serverRunning = false;
    System.out.println("Print server stopped.");
}

@Override
public void restart(String sessionToken) throws RemoteException {
    checkPermission(sessionToken, "restart");
    stop(sessionToken);
    start(sessionToken);
}


@Override
public void status(String printer, String sessionToken) throws RemoteException {
    checkPermission(sessionToken, "status");
    System.out.println("Status of printer " + printer + ": Ready");
}

@Override
public void readConfig(String parameter, String sessionToken) throws RemoteException {
    checkPermission(sessionToken, "readConfig");
    System.out.println("Config parameter " + parameter + ": value");
}

@Override
public void setConfig(String parameter, String value, String sessionToken) throws RemoteException {
    checkPermission(sessionToken, "setConfig");
    System.out.println("Setting config " + parameter + " to " + value);
}

}
