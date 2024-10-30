package server;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;


public class PrintServerImpl extends UnicastRemoteObject implements PrintServer {
    private Map<String, String> userCredentials = new HashMap<>();
    private boolean serverRunning = false;

    public PrintServerImpl() throws RemoteException {
        userCredentials.put("user1", "password1");
        userCredentials.put("user2", "password2");
    }

    private boolean authenticate(String username, String password) {
        return password.equals(userCredentials.get(username));
    }

    @Override
    public void print(String filename, String printer, String username, String password) throws RemoteException {
        if (!authenticate(username, password)) {
            System.out.println("Authentication failed for user: " + username);
            return;
        }
        System.out.println("Printing " + filename + " on " + printer);
    }
    @Override
    public void queue(String printer, String username, String password) throws RemoteException {
        if (!authenticate(username, password)) {
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
        if (!authenticate(username, password)) {
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


    // Implement remote methods with authentication checks...
}
