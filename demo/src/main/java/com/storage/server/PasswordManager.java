package com.storage.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface PasswordManager extends Remote {
    boolean addUser(String username, String password) throws RemoteException;
    boolean deleteUser(String username) throws RemoteException;
    boolean authenticateUser(String username, String password) throws RemoteException;
    boolean updatePassword(String username, String oldPassword, String newPassword) throws RemoteException;
    boolean isDatabaseEmpty() throws RemoteException;
    void clearUsers() throws RemoteException;
    List<String> getAllUsers() throws RemoteException;

    // PRINT SERVER
    void print(String filename, String printer, String username, String password) throws RemoteException;
    void queue(String printer, String username, String password) throws RemoteException;
    void topQueue(String printer, int job, String username, String password) throws RemoteException;
    void start() throws RemoteException;
    void stop() throws RemoteException;
    void restart() throws RemoteException;
    void status(String printer) throws RemoteException;
    void readConfig(String parameter) throws RemoteException;
    void setConfig(String parameter, String value) throws RemoteException;
}
