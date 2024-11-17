package com.storage.server;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PasswordManagerServer {
    private static Map<String, Set<String>> rolePermissions = new HashMap<>();
    private static Map<String, String> userRoles = new HashMap<>();
    private static Map<String, Set<String>> aclPermissions = new HashMap<>();
    private static boolean useACL = false;

    public static void loadRoles(String filePath) {
        try (InputStream input = PasswordManagerServer.class.getClassLoader().getResourceAsStream(filePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(input))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) continue;

                String[] parts = line.split(":");
                String role = parts[0].trim();
                String[] permissions = parts[1].trim().split(",");
                Set<String> permissionsSet = new HashSet<>();
                for (String permission : permissions) {
                    permissionsSet.add(permission.trim());
                }
                rolePermissions.put(role, permissionsSet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadUserRoles(String filePath) {
        try (InputStream input = PasswordManagerServer.class.getClassLoader().getResourceAsStream(filePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(input))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) continue;

                String[] parts = line.split(":");
                String user = parts[0].trim();
                String role = parts[1].trim();
                userRoles.put(user, role);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadACL(String filePath) {
        try (InputStream input = PasswordManagerServer.class.getClassLoader().getResourceAsStream(filePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(input))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) continue;

                String[] parts = line.split(":");
                String user = parts[0].trim();
                String[] permissions = parts[1].trim().split(",");
                Set<String> permissionsSet = new HashSet<>();
                for (String permission : permissions) {
                    permissionsSet.add(permission.trim());
                }
                aclPermissions.put(user, permissionsSet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean hasPermission(String user, String action) {
        if (useACL) {
            Set<String> permissions = aclPermissions.get(user);
            return permissions != null && permissions.contains(action);
        } else {
            String role = userRoles.get(user);
            Set<String> permissions = rolePermissions.get(role);
            return permissions != null && (permissions.contains(action) || permissions.contains("ALL"));
        }
    }

    public static void main(String[] args) {
        try {
            PasswordManagerImpl manager = new PasswordManagerImpl();

            // User choice for ACL or RBAC
            useACL = args.length > 0 && args[0].equalsIgnoreCase("acl");
            if (useACL) {
                loadACL("com/storage/server/acl_policy.txt");
                System.out.println("Using Access Control List (ACL)");
            } else {
                loadRoles("com/storage/server/rbac_policy.txt");
                loadUserRoles("com/storage/server/user_roles.txt");
                System.out.println("Using Role-Based Access Control (RBAC)");
            }

            Registry registry = LocateRegistry.createRegistry(1101);
            registry.rebind("PasswordManager", manager);
            System.out.println("Password Manager Server is running...");
            while (true) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
