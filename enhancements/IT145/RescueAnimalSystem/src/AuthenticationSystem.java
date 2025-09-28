import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class AuthenticationSystem {
    private SimpleDataManager dataManager;
    private Map<String, SessionData> activeSessions;
    private User currentUser; // For console-based interface
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes in milliseconds
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private Map<String, LoginAttemptTracker> loginAttempts;
    
    public AuthenticationSystem(SimpleDataManager dataManager) {
        this.dataManager = dataManager;
        this.activeSessions = new ConcurrentHashMap<>();
        this.loginAttempts = new ConcurrentHashMap<>();
        
        // Clean up expired sessions periodically
        startSessionCleanupTask();
    }
    
    /**
     * Web-based login - returns a session for API use
     */
    public SessionData login(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty()) {
            logFailedLoginAttempt(username, "Invalid credentials");
            return null;
        }
        
        // Check for too many failed attempts
        if (isAccountLocked(username)) {
            System.out.println("Account locked due to too many failed attempts: " + username);
            return null;
        }
        
        User user = getUser(username);
        
        if (user != null && user.isActive() && PasswordUtil.verify(password, user.getPassword())) {
            // Clear failed attempts on successful login
            loginAttempts.remove(username);
            
            // Create new session
            String sessionId = generateSessionId();
            SessionData session = new SessionData(sessionId, user, System.currentTimeMillis());
            activeSessions.put(sessionId, session);
            
            System.out.println(String.format("User logged in successfully: %s (%s) - Session: %s", 
                              username, user.getRole(), sessionId.substring(0, 8) + "..."));
            return session;
        }
        
        logFailedLoginAttempt(username, "Invalid credentials");
        System.out.println("Failed login attempt for: " + username);
        return null;
    }
    
    /**
     * Console-based login - for backward compatibility
     */
    public boolean login(Scanner scanner) {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        
        SessionData session = login(username, password);
        if (session != null) {
            currentUser = session.getUser();
            return true;
        }
        return false;
    }
    
    /**
     * Create a new user account
     * Regular users are created with VIEW role by default
     * Only admins can create other admin accounts
     */
    public boolean createUser(String username, String password, String fullName, UserRole role) {
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            System.out.println("Username cannot be empty");
            return false;
        }
        if (password == null || password.length() < 6) {
            System.out.println("Password must be at least 6 characters long");
            return false;
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            System.out.println("Full name cannot be empty");
            return false;
        }
        
        // Check if username already exists
        if (getUser(username) != null) {
            System.out.println("Username already exists: " + username);
            return false;
        }
        
        // For security, regular registration creates VIEW role users only
        // Admin users must be created by existing admins
        UserRole userRole = (role != null) ? role : UserRole.VIEW;
        
        String encryptedPassword = PasswordUtil.encrypt(password);
        User newUser = new User(username, encryptedPassword, fullName, userRole);
        
        boolean success = addUser(newUser);
        if (success) {
            System.out.println(String.format("User created successfully: %s (%s) - Role: %s", 
                              username, fullName, userRole));
        } else {
            System.out.println("Failed to create user: " + username);
        }
        
        return success;
    }
    
    /**
     * Get all users (admin only)
     */
    public List<User> getAllUsers(User requestingUser) {
        if (!hasPermission(requestingUser, UserRole.ADMIN)) {
            System.out.println("Only admins can view all users");
            return new ArrayList<>();
        }
        
        if (dataManager != null) {
            Map<String, User> users = dataManager.getUsers();
            return new ArrayList<>(users.values());
        }
        return new ArrayList<>();
    }
    
    /**
     * Update user role (admin only)
     */
    public boolean updateUserRole(String username, UserRole newRole, User requestingUser) {
        if (!hasPermission(requestingUser, UserRole.ADMIN)) {
            System.out.println("Only admins can update user roles");
            return false;
        }
        
        User targetUser = getUser(username);
        if (targetUser == null) {
            System.out.println("User not found: " + username);
            return false;
        }
        
        // Prevent removing admin role from the default admin
        if (targetUser.getUsername().equals("admin") && newRole != UserRole.ADMIN) {
            System.out.println("Cannot remove admin role from the default admin account");
            return false;
        }
        
        // Prevent users from modifying their own role
        if (requestingUser.getUsername().equals(username)) {
            System.out.println("Cannot modify your own role");
            return false;
        }
        
        UserRole oldRole = targetUser.getRole();
        targetUser.setRole(newRole);
        
        // Save changes to database
        if (dataManager != null) {
            dataManager.saveUsers();
        }
        
        // Invalidate all sessions for this user if role was downgraded
        if (newRole.getLevel() < oldRole.getLevel()) {
            invalidateUserSessions(username);
        }
        
        System.out.println(String.format("User role updated: %s - %s -> %s", 
                          username, oldRole, newRole));
        return true;
    }
    
    /**
     * Delete user account (admin only)
     */
    public boolean deleteUser(String username, User requestingUser) {
        if (!hasPermission(requestingUser, UserRole.ADMIN)) {
            System.out.println("Only admins can delete user accounts");
            return false;
        }
        
        User targetUser = getUser(username);
        if (targetUser == null) {
            System.out.println("User not found: " + username);
            return false;
        }
        
        // Prevent deletion of the default admin
        if (targetUser.getUsername().equals("admin")) {
            System.out.println("Cannot delete the default admin account");
            return false;
        }
        
        // Prevent users from deleting themselves
        if (requestingUser.getUsername().equals(username)) {
            System.out.println("Cannot delete your own account");
            return false;
        }
        
        // Remove user from database
        boolean success = false;
        if (dataManager != null) {
            success = dataManager.removeUser(username);
        }
        
        if (success) {
            // Invalidate all sessions for this user
            invalidateUserSessions(username);
            System.out.println("User account deleted: " + username);
        }
        
        return success;
    }
    
    /**
     * Invalidate all sessions for a specific user
     */
    private void invalidateUserSessions(String username) {
        activeSessions.entrySet().removeIf(entry -> 
            entry.getValue().getUser().getUsername().equals(username));
        System.out.println("All sessions invalidated for user: " + username);
    }
    
    /**
     * Create a new user by admin (with specified role)
     */
    public boolean createUserByAdmin(String username, String password, String fullName, 
                                   UserRole role, User requestingUser) {
        if (!hasPermission(requestingUser, UserRole.ADMIN)) {
            System.out.println("Only admins can create users with specific roles");
            return false;
        }
        
        return createUser(username, password, fullName, role);
    }
    
    /**
     * Reset user password (admin only)
     */
    public boolean resetUserPassword(String username, String newPassword, User requestingUser) {
        if (!hasPermission(requestingUser, UserRole.ADMIN)) {
            System.out.println("Only admins can reset user passwords");
            return false;
        }
        
        User targetUser = getUser(username);
        if (targetUser == null) {
            System.out.println("User not found: " + username);
            return false;
        }
        
        if (newPassword == null || newPassword.length() < 6) {
            System.out.println("New password must be at least 6 characters long");
            return false;
        }
        
        // Update password
        String encryptedNewPassword = PasswordUtil.encrypt(newPassword);
        targetUser.setPassword(encryptedNewPassword);
        
        // Save changes to database
        if (dataManager != null) {
            dataManager.saveUsers();
        }
        
        // Invalidate all sessions for this user to force re-login
        invalidateUserSessions(username);
        
        System.out.println("Password reset successfully for user: " + username);
        return true;
    }
    
    /**
     * Toggle user active status (admin only)
     */
    public boolean toggleUserStatus(String username, User requestingUser) {
        if (!hasPermission(requestingUser, UserRole.ADMIN)) {
            System.out.println("Only admins can toggle user status");
            return false;
        }
        
        User targetUser = getUser(username);
        if (targetUser == null) {
            System.out.println("User not found: " + username);
            return false;
        }
        
        // Prevent deactivating the default admin
        if (targetUser.getUsername().equals("admin")) {
            System.out.println("Cannot deactivate the default admin account");
            return false;
        }
        
        // Prevent users from deactivating themselves
        if (requestingUser.getUsername().equals(username)) {
            System.out.println("Cannot deactivate your own account");
            return false;
        }
        
        boolean newStatus = !targetUser.isActive();
        targetUser.setActive(newStatus);
        
        // Save changes to database
        if (dataManager != null) {
            dataManager.saveUsers();
        }
        
        // If deactivating, invalidate all sessions for this user
        if (!newStatus) {
            invalidateUserSessions(username);
        }
        
        System.out.println(String.format("User %s: %s", 
                          username, newStatus ? "activated" : "deactivated"));
        return true;
    }
    
    // Helper methods to work with data manager
    private User getUser(String username) {
        if (dataManager != null) {
            return dataManager.getUser(username);
        }
        return null;
    }
    
    private boolean addUser(User user) {
        if (dataManager != null) {
            return dataManager.addUser(user);
        }
        return false;
    }
    
    /**
     * Validate a session by session ID
     * Used by web API to check if user is authenticated
     */
    public SessionData validateSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return null;
        }
        
        SessionData session = activeSessions.get(sessionId);
        if (session == null) {
            return null;
        }
        
        // Check if session has expired
        if (System.currentTimeMillis() - session.getLastAccess() > SESSION_TIMEOUT) {
            activeSessions.remove(sessionId);
            System.out.println("Session expired and removed: " + sessionId.substring(0, 8) + "...");
            return null;
        }
        
        // Check if user is still active
        User user = getUser(session.getUser().getUsername());
        if (user == null || !user.isActive()) {
            activeSessions.remove(sessionId);
            System.out.println("Session invalidated - user inactive: " + sessionId.substring(0, 8) + "...");
            return null;
        }
        
        // Update last access time
        session.updateLastAccess();
        return session;
    }
    
    /**
     * Logout by session ID (web-based)
     */
    public boolean logout(String sessionId) {
        SessionData session = activeSessions.remove(sessionId);
        if (session != null) {
            System.out.println("User logged out: " + session.getUser().getUsername() + 
                             " - Session: " + sessionId.substring(0, 8) + "...");
            return true;
        }
        return false;
    }
    
    /**
     * Logout current user (console-based)
     */
    public void logout() {
        if (currentUser != null) {
            System.out.println("User logged out: " + currentUser.getUsername());
            currentUser = null;
        }
    }
    
    /**
     * Check if user is logged in (console-based)
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Get current user (console-based)
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Check if current user has required permission level
     */
    public boolean hasPermission(UserRole requiredRole) {
        return hasPermission(currentUser, requiredRole);
    }
    
    /**
     * Check if specified user has required permission level
     */
    public boolean hasPermission(User user, UserRole requiredRole) {
        if (user == null || requiredRole == null) {
            return false;
        }
        return user.getRole().getLevel() >= requiredRole.getLevel();
    }
    
    /**
     * Change user password
     * Users can change their own password, admins can change any password
     */
    public boolean changePassword(String username, String oldPassword, String newPassword, User requestingUser) {
        if (newPassword == null || newPassword.length() < 6) {
            System.out.println("New password must be at least 6 characters long");
            return false;
        }
        
        User targetUser = getUser(username);
        if (targetUser == null) {
            System.out.println("User not found: " + username);
            return false;
        }
        
        // Check permissions
        boolean isAdmin = hasPermission(requestingUser, UserRole.ADMIN);
        boolean isOwnPassword = requestingUser != null && requestingUser.getUsername().equals(username);
        
        if (!isAdmin && !isOwnPassword) {
            System.out.println("Insufficient permissions to change password for: " + username);
            return false;
        }
        
        // Verify old password (unless admin changing someone else's password)
        if (!isAdmin || isOwnPassword) {
            if (oldPassword == null || !PasswordUtil.verify(oldPassword, targetUser.getPassword())) {
                System.out.println("Invalid old password for user: " + username);
                return false;
            }
        }
        
        // Update password
        String encryptedNewPassword = PasswordUtil.encrypt(newPassword);
        targetUser.setPassword(encryptedNewPassword);
        
        // Save changes to database
        if (dataManager != null) {
            dataManager.saveUsers();
        }
        
        System.out.println("Password changed successfully for user: " + username);
        return true;
    }
    
    /**
     * Deactivate a user account (admin only) - kept for backward compatibility
     */
    public boolean deactivateUser(String username, User requestingUser) {
        return toggleUserStatus(username, requestingUser);
    }
    
    /**
     * Get all active sessions (admin only)
     */
    public List<SessionInfo> getActiveSessions(User requestingUser) {
        if (!hasPermission(requestingUser, UserRole.ADMIN)) {
            return new ArrayList<>();
        }
        
        List<SessionInfo> sessions = new ArrayList<>();
        for (SessionData session : activeSessions.values()) {
            sessions.add(new SessionInfo(
                session.getSessionId().substring(0, 8) + "...",
                session.getUser().getUsername(),
                session.getUser().getFullName(),
                session.getCreatedTime(),
                session.getLastAccess()
            ));
        }
        
        return sessions;
    }
    
    /**
     * Force logout a user session (admin only)
     */
    public boolean forceLogout(String sessionId, User requestingUser) {
        if (!hasPermission(requestingUser, UserRole.ADMIN)) {
            System.out.println("Only admins can force logout sessions");
            return false;
        }
        
        return logout(sessionId);
    }
    
    // Private helper methods
    
    private String generateSessionId() {
        return UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }
    
    private void logFailedLoginAttempt(String username, String reason) {
        LoginAttemptTracker tracker = loginAttempts.computeIfAbsent(username, 
            k -> new LoginAttemptTracker());
        tracker.addFailedAttempt();
        
        System.out.println(String.format("Failed login attempt for '%s': %s (Attempt %d/%d)", 
                          username, reason, tracker.getAttemptCount(), MAX_LOGIN_ATTEMPTS));
    }
    
    private boolean isAccountLocked(String username) {
        LoginAttemptTracker tracker = loginAttempts.get(username);
        return tracker != null && tracker.isLocked();
    }
    
    private void startSessionCleanupTask() {
        Timer timer = new Timer(true); // Daemon thread
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                cleanupExpiredSessions();
                cleanupOldLoginAttempts();
            }
        }, SESSION_TIMEOUT, SESSION_TIMEOUT); // Run every session timeout period
    }
    
    private void cleanupExpiredSessions() {
        long currentTime = System.currentTimeMillis();
        int removedCount = 0;
        
        Iterator<Map.Entry<String, SessionData>> iterator = activeSessions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, SessionData> entry = iterator.next();
            if (currentTime - entry.getValue().getLastAccess() > SESSION_TIMEOUT) {
                iterator.remove();
                removedCount++;
            }
        }
        
        if (removedCount > 0) {
            System.out.println("Cleaned up " + removedCount + " expired sessions");
        }
    }
    
    private void cleanupOldLoginAttempts() {
        int removedCount = 0;
        Iterator<Map.Entry<String, LoginAttemptTracker>> iterator = loginAttempts.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, LoginAttemptTracker> entry = iterator.next();
            if (entry.getValue().shouldReset()) {
                iterator.remove();
                removedCount++;
            }
        }
        
        if (removedCount > 0) {
            System.out.println("Cleaned up " + removedCount + " old login attempt trackers");
        }
    }
    
    // Inner classes
    
    private static class LoginAttemptTracker {
        private int attemptCount = 0;
        private LocalDateTime lastAttempt = LocalDateTime.now();
        private static final int LOCKOUT_MINUTES = 15;
        
        public void addFailedAttempt() {
            attemptCount++;
            lastAttempt = LocalDateTime.now();
        }
        
        public boolean isLocked() {
            if (attemptCount < MAX_LOGIN_ATTEMPTS) {
                return false;
            }
            
            // Check if lockout period has expired
            LocalDateTime unlockTime = lastAttempt.plusMinutes(LOCKOUT_MINUTES);
            if (LocalDateTime.now().isAfter(unlockTime)) {
                attemptCount = 0; // Reset attempts after lockout period
                return false;
            }
            
            return true;
        }
        
        public int getAttemptCount() {
            return attemptCount;
        }
        
        public boolean shouldReset() {
            // Reset tracker if no attempts for 24 hours
            return ChronoUnit.HOURS.between(lastAttempt, LocalDateTime.now()) > 24;
        }
    }
    
    public static class SessionInfo {
        private String sessionId;
        private String username;
        private String fullName;
        private long createdTime;
        private long lastAccess;
        
        public SessionInfo(String sessionId, String username, String fullName, 
                          long createdTime, long lastAccess) {
            this.sessionId = sessionId;
            this.username = username;
            this.fullName = fullName;
            this.createdTime = createdTime;
            this.lastAccess = lastAccess;
        }
        
        // Getters
        public String getSessionId() { return sessionId; }
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public long getCreatedTime() { return createdTime; }
        public long getLastAccess() { return lastAccess; }
    }
}

/**
 * Session data holder for web-based authentication
 */
class SessionData {
    private String sessionId;
    private User user;
    private long createdTime;
    private long lastAccess;
    
    public SessionData(String sessionId, User user, long createdTime) {
        this.sessionId = sessionId;
        this.user = user;
        this.createdTime = createdTime;
        this.lastAccess = createdTime;
    }
    
    public String getSessionId() { return sessionId; }
    public User getUser() { return user; }
    public long getCreatedTime() { return createdTime; }
    public long getLastAccess() { return lastAccess; }
    
    public void updateLastAccess() {
        this.lastAccess = System.currentTimeMillis();
    }
}