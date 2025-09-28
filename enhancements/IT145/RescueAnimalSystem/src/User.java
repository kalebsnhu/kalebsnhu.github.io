import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class User {
    private String username;
    private String password;
    private String fullName;
    private UserRole role;
    private LocalDateTime createdDate;
    private LocalDateTime lastLoginDate;
    private boolean active;
    private String email; // Optional field for future use
    private int loginAttempts; // Track failed login attempts
    
    // Default constructor
    public User() {
        this.createdDate = LocalDateTime.now();
        this.active = true;
        this.loginAttempts = 0;
    }
    
    // Primary constructor
    public User(String username, String password, String fullName, UserRole role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.createdDate = LocalDateTime.now();
        this.active = true;
        this.loginAttempts = 0;
    }
    
    // Constructor with all fields
    public User(String username, String password, String fullName, UserRole role, 
                boolean active, String email) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.createdDate = LocalDateTime.now();
        this.active = active;
        this.email = email;
        this.loginAttempts = 0;
    }
    
    // Getters
    public String getUsername() { 
        return username; 
    }
    
    public String getPassword() { 
        return password; 
    }
    
    public String getFullName() { 
        return fullName; 
    }
    
    public UserRole getRole() { 
        return role; 
    }
    
    public LocalDateTime getCreatedDate() { 
        return createdDate; 
    }
    
    public LocalDateTime getLastLoginDate() { 
        return lastLoginDate; 
    }
    
    public boolean isActive() { 
        return active; 
    }
    
    public String getEmail() { 
        return email; 
    }
    
    public int getLoginAttempts() { 
        return loginAttempts; 
    }
    
    // Setters
    public void setUsername(String username) { 
        this.username = username; 
    }
    
    public void setPassword(String password) { 
        this.password = password; 
    }
    
    public void setFullName(String fullName) { 
        this.fullName = fullName; 
    }
    
    public void setRole(UserRole role) { 
        this.role = role; 
    }
    
    public void setCreatedDate(LocalDateTime createdDate) { 
        this.createdDate = createdDate; 
    }
    
    public void setLastLoginDate(LocalDateTime lastLoginDate) { 
        this.lastLoginDate = lastLoginDate; 
    }
    
    public void setActive(boolean active) { 
        this.active = active; 
    }
    
    public void setEmail(String email) { 
        this.email = email; 
    }
    
    public void setLoginAttempts(int loginAttempts) { 
        this.loginAttempts = loginAttempts; 
    }
    
    // Utility methods
    
    /**
     * Update the last login date to current time
     */
    public void updateLastLogin() {
        this.lastLoginDate = LocalDateTime.now();
    }
    
    /**
     * Reset failed login attempts counter
     */
    public void resetLoginAttempts() {
        this.loginAttempts = 0;
    }
    
    /**
     * Increment failed login attempts counter
     */
    public void incrementLoginAttempts() {
        this.loginAttempts++;
    }
    
    /**
     * Check if user has a specific permission level or higher
     */
    public boolean hasPermission(UserRole requiredRole) {
        return this.role.getLevel() >= requiredRole.getLevel();
    }
    
    /**
     * Check if user can manage other users (admin only)
     */
    public boolean canManageUsers() {
        return this.role == UserRole.ADMIN;
    }
    
    /**
     * Check if user can add/edit animals (staff or admin)
     */
    public boolean canEditAnimals() {
        return this.role.getLevel() >= UserRole.STAFF.getLevel();
    }
    
    /**
     * Check if user can view activity logs (monitor or higher)
     */
    public boolean canViewActivities() {
        return this.role.getLevel() >= UserRole.MONITOR.getLevel();
    }
    
    /**
     * Check if user can make reservations (staff or admin)
     */
    public boolean canMakeReservations() {
        return this.role.getLevel() >= UserRole.STAFF.getLevel();
    }
    
    /**
     * Get formatted creation date
     */
    public String getFormattedCreatedDate() {
        if (createdDate == null) return "Unknown";
        return createdDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
    }
    
    /**
     * Get formatted last login date
     */
    public String getFormattedLastLoginDate() {
        if (lastLoginDate == null) return "Never";
        return lastLoginDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
    }
    
    /**
     * Get user status as string
     */
    public String getStatusString() {
        return active ? "Active" : "Inactive";
    }
    
    /**
     * Get role display name
     */
    public String getRoleDisplayName() {
        switch (role) {
            case VIEW:
                return "Viewer";
            case MONITOR:
                return "Monitor";
            case STAFF:
                return "Staff";
            case ADMIN:
                return "Administrator";
            default:
                return role.toString();
        }
    }
    
    /**
     * Get role description
     */
    public String getRoleDescription() {
        switch (role) {
            case VIEW:
                return "Can view animals and basic information";
            case MONITOR:
                return "Can view animals and monitor activities";
            case STAFF:
                return "Can add/edit animals and make reservations";
            case ADMIN:
                return "Full access including user management";
            default:
                return "Unknown role";
        }
    }
    
    /**
     * Check if user account needs attention (inactive, too many failed attempts, etc.)
     */
    public boolean needsAttention() {
        return !active || loginAttempts >= 3;
    }
    
    /**
     * Get days since account creation
     */
    public long getDaysSinceCreation() {
        if (createdDate == null) return 0;
        return java.time.Duration.between(createdDate, LocalDateTime.now()).toDays();
    }
    
    /**
     * Get days since last login
     */
    public long getDaysSinceLastLogin() {
        if (lastLoginDate == null) return -1; // Never logged in
        return java.time.Duration.between(lastLoginDate, LocalDateTime.now()).toDays();
    }
    
    /**
     * Check if user is a system default user
     */
    public boolean isSystemUser() {
        return "admin".equals(username) || "system".equals(username);
    }
    
    /**
     * Validate user data
     */
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
               password != null && !password.trim().isEmpty() &&
               fullName != null && !fullName.trim().isEmpty() &&
               role != null;
    }
    
    /**
     * Get user summary for logging
     */
    public String getSummary() {
        return String.format("User[%s, %s, %s, %s]", 
                           username, fullName, role, active ? "Active" : "Inactive");
    }
    
    @Override
    public String toString() {
        return String.format("User{username='%s', fullName='%s', role=%s, active=%s, created=%s}", 
                           username, fullName, role, active, 
                           createdDate != null ? createdDate.format(DateTimeFormatter.ISO_LOCAL_DATE) : "null");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return username != null ? username.equals(user.username) : user.username == null;
    }
    
    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }
    
    /**
     * Create a copy of this user with a new role (for role updates)
     */
    public User withRole(UserRole newRole) {
        User copy = new User(this.username, this.password, this.fullName, newRole);
        copy.setCreatedDate(this.createdDate);
        copy.setLastLoginDate(this.lastLoginDate);
        copy.setActive(this.active);
        copy.setEmail(this.email);
        copy.setLoginAttempts(this.loginAttempts);
        return copy;
    }
    
    /**
     * Create a sanitized version of this user (without password) for API responses
     */
    public UserInfo toUserInfo() {
        return new UserInfo(username, fullName, role, active, createdDate, lastLoginDate);
    }
    
    /**
     * Inner class for safe user information transfer (without password)
     */
    public static class UserInfo {
        private final String username;
        private final String fullName;
        private final UserRole role;
        private final boolean active;
        private final LocalDateTime createdDate;
        private final LocalDateTime lastLoginDate;
        
        public UserInfo(String username, String fullName, UserRole role, boolean active,
                       LocalDateTime createdDate, LocalDateTime lastLoginDate) {
            this.username = username;
            this.fullName = fullName;
            this.role = role;
            this.active = active;
            this.createdDate = createdDate;
            this.lastLoginDate = lastLoginDate;
        }
        
        // Getters
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public UserRole getRole() { return role; }
        public boolean isActive() { return active; }
        public LocalDateTime getCreatedDate() { return createdDate; }
        public LocalDateTime getLastLoginDate() { return lastLoginDate; }
        
        public String toJson() {
            return String.format(
                "{\"username\":\"%s\",\"fullName\":\"%s\",\"role\":\"%s\",\"active\":%s}",
                username, fullName, role, active
            );
        }
    }
}