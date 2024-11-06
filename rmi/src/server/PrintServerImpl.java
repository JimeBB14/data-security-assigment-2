package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class PrintServerImpl extends UnicastRemoteObject implements PrintServer {
    private Map<String, String> userCredentials = new HashMap<>();
    private Map<String, Long> activeSessions = new HashMap<>(); //SM
    private boolean serverRunning = false;
    private final long SESSION_TIMEOUT = 1 * 60 * 1000; //SM


    public PrintServerImpl() throws RemoteException {
        userCredentials.put("user1", "password1");
        userCredentials.put("user2", "password2");
    }

    private boolean authenticate(String username, String password) {
        return password.equals(userCredentials.get(username));
    }

    private boolean validateSession(String sessionToken) throws RemoteException{
        Long expirationTime = activeSessions.get(sessionToken);

        if (expirationTime == null) {
            System.out.println("Session token not found: " + sessionToken);
            throw new RemoteException("Session token not found. Please log in again."); //return false;
        }
    
        if (System.currentTimeMillis() > expirationTime) {
            // Session expired, remove it from active sessions
            activeSessions.remove(sessionToken);
            System.out.println("Session token expired: " + sessionToken);
            throw new RemoteException("Session expired. Please log in again."); //return false;
        }

    
        // Refresh expiration time for active session
        activeSessions.put(sessionToken, System.currentTimeMillis() + SESSION_TIMEOUT);
        System.out.println("Session " + sessionToken + " is valid. Expiration refreshed to: " + (System.currentTimeMillis() + SESSION_TIMEOUT));
        return true;
    }

    @Override
    public void print(String filename, String printer, String sessionToken) throws RemoteException {
        if (!validateSession(sessionToken)) {
            System.out.println("Session is invalid or expired. Please log in again.");
            return;
        }
        System.out.println("Printing " + filename + " on " + printer);
    }
    @Override
    public void queue(String printer, String sessionToken) throws RemoteException {
        if (!validateSession(sessionToken)) {
            System.out.println("Session is invalid or expired. Please log in again");
            return;
        }
        // Simulate queuing behavior
        System.out.println("Listing queue for printer: " + printer);
        // For demo, just a static message; in a real scenario, return the actual queue
        System.out.println("Current print jobs for " + printer + ": None"); // Placeholder
    }

    @Override
    public void topQueue(String printer, int job, String sessionToken) throws RemoteException {
        if (!validateSession(sessionToken)) {
            System.out.println("Session is invalid or expired. Please log in again.");
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
    public void status(String printer, String sessionToken) throws RemoteException {
        // Simulate printing status
        if (!validateSession(sessionToken)) {  // Check if the session is valid
            System.out.println("Session is invalid or expired. Please log in again.");
            return;
        }
        System.out.println("Status of printer " + printer + ": Ready");
    }

    @Override
    public void readConfig(String parameter, String sessionToken) throws RemoteException {
        // Simulate reading a configuration parameter
        if (!validateSession(sessionToken)) {  // Check if the session is valid
            System.out.println("Session is invalid or expired. Please log in again.");
            return;
        }
        System.out.println("Config parameter " + parameter + ": value");
    }

    @Override
    public void setConfig(String parameter, String value, String sessionToken) throws RemoteException {
        // Simulate setting a configuration parameter
        if (!validateSession(sessionToken)) {  // Check if the session is valid
            System.out.println("Session is invalid or expired. Please log in again.");
            return;
        }
        System.out.println("Setting config " + parameter + " to " + value);
    }

    @Override //SM
    public String login(String username, String password) throws RemoteException{
        // Authenticate user by checking credentials 
        if (!authenticate(username, password)){
            System.out.println("Login failed. Please check your credentials.");
            return null;
        }

        String sessionToken = UUID.randomUUID().toString();

        activeSessions.put(sessionToken, System.currentTimeMillis() + SESSION_TIMEOUT);
        System.out.println("Login successful. Session token: " + sessionToken);

        return sessionToken;
    }

    @Override
    public void logout(String sessionToken) throws RemoteException {
        if (activeSessions.remove(sessionToken) != null) {
            System.out.println("Session " + sessionToken + " has been logged out.");
        } else {
            System.out.println("Invalid session token. Logout failed.");
        }
    }
    // Implement remote methods with authentication checks...
}

//TO RUN 
// COMPILE: javac server/*.java client/*.java
// TERMINAL 1 (SERVER): java -Djava.rmi.server.hostname=localhost server.ServerMain
// TERMINAL 2 (CLIENT): java client.PrintClient   