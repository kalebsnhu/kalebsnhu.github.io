public enum UserRole {
    VIEW(0),      // Can only view data, cannot modify
    MONITOR(1),   // Can view and track activities
    STAFF(2),     // Can add/modify animals and reservations
    ADMIN(3);     // Full access including user management
    
    private final int level;
    
    UserRole(int level) {
        this.level = level;
    }
    
    public int getLevel() {
        return level;
    }
}