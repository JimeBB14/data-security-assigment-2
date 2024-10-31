package com.storage;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface PasswordManager extends Remote {
    String startSession(String username, String password) throws RemoteException; // Iniciar sesión
    boolean isSessionValid(String sessionId) throws RemoteException; // Verificar sesión
    void endSession(String sessionId) throws RemoteException; // Terminar sesión

    boolean addUser(String username, String password) throws RemoteException;
    boolean deleteUser(String username) throws RemoteException;
    boolean authenticateUser(String username, String password) throws RemoteException;
    boolean updatePassword(String username, String oldPassword, String newPassword, String sessionId) throws RemoteException;
    boolean isDatabaseEmpty() throws RemoteException;
    void clearUsers() throws RemoteException;
    List<String> getAllUsers() throws RemoteException;
}
