package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PrintServer extends Remote {
    String login(String username, String password) throws RemoteException; //SM
    void logout(String sessionToken) throws RemoteException; 
    
    void print(String filename, String printer, String sessionToken) throws RemoteException;
    void queue(String printer, String sessionToken) throws RemoteException;
    void topQueue(String printer, int job, String sessionToken) throws RemoteException;
    void status(String printer, String sessionToken) throws RemoteException;
    void readConfig(String parameter, String sessionToken) throws RemoteException;
    void setConfig(String parameter, String value, String sessionToken) throws RemoteException;
    
    void start() throws RemoteException;
    void stop() throws RemoteException;
    void restart() throws RemoteException;
}