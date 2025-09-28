import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MonitoringSystem {
    private SimpleDataManager dataManager;  // Changed from Object to SimpleDataManager
    
    // Constructor with SimpleDataManager
    public MonitoringSystem(SimpleDataManager dataManager) {
        this.dataManager = dataManager;
    }
    
    // Default constructor for backward compatibility
    public MonitoringSystem() {
        this.dataManager = null;
    }
    
    /**
     * Log an activity to the system
     * Activities are automatically saved to the database
     */
    public void logActivity(String animalName, String animalType, String activityType, 
                           String description, String location, String performedBy) {
        Activity activity = new Activity(animalName, animalType, activityType, 
                                       description, location, performedBy);
        
        if (dataManager != null) {
            dataManager.addActivity(activity);
            
            // Update location if it's a location-based activity
            if (activityType.equals("LOCATION_UPDATE") || activityType.equals("TRANSFER")) {
                dataManager.updateAnimalLocation(animalName, location);
            }
            
            System.out.println("Activity logged: " + activity.toString());
        }
    }
    
    /**
     * Display all activities to console
     * Primarily used for debugging or console-based interface
     */
    public void displayActivityLog() {
        System.out.println("\n=== ANIMAL ACTIVITY LOG ===");
        
        List<Activity> activities = new ArrayList<>();
        if (dataManager != null) {
            activities = dataManager.getActivities();
        }
            
        if (activities.isEmpty()) {
            System.out.println("No activities recorded.");
            return;
        }
        
        // Sort by timestamp (newest first)
        activities.sort((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()));
        
        for (Activity activity : activities) {
            System.out.println(activity);
        }
    }
    
    /**
     * Display current animal locations to console
     * Primarily used for debugging or console-based interface
     */
    public void displayAnimalLocations() {
        System.out.println("\n=== CURRENT ANIMAL LOCATIONS ===");
        
        Map<String, String> animalLocations = new HashMap<>();
        if (dataManager != null) {
            animalLocations = dataManager.getAllLocations();
        }
            
        if (animalLocations.isEmpty()) {
            System.out.println("No location data available.");
            return;
        }
        
        for (Map.Entry<String, String> entry : animalLocations.entrySet()) {
            System.out.printf("%-15s: %s%n", entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Track activities for a specific animal
     * Returns activities for the given animal name
     */
    public List<Activity> getAnimalActivities(String animalName) {
        List<Activity> animalActivities = new ArrayList<>();
        
        List<Activity> allActivities = new ArrayList<>();
        if (dataManager != null) {
            allActivities = dataManager.getActivities();
        }
        
        for (Activity activity : allActivities) {
            if (activity.getAnimalName().equalsIgnoreCase(animalName)) {
                animalActivities.add(activity);
            }
        }
        
        // Sort by timestamp (newest first)
        animalActivities.sort((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()));
        
        return animalActivities;
    }
    
    /**
     * Display activities for a specific animal to console
     * Primarily used for debugging or console-based interface
     */
    public void trackAnimalActivities(String animalName) {
        System.out.println("\n=== ACTIVITIES FOR " + animalName.toUpperCase() + " ===");
        
        List<Activity> activities = getAnimalActivities(animalName);
        
        if (activities.isEmpty()) {
            System.out.println("No activities found for " + animalName);
        } else {
            for (Activity activity : activities) {
                System.out.println(activity);
            }
        }
    }
    
    /**
     * Get current location of an animal
     */
    public String getAnimalLocation(String animalName) {
        if (dataManager != null) {
            return dataManager.getAnimalLocation(animalName);
        }
        return "Location unknown";
    }
    
    /**
     * Update an animal's location
     * This will log an activity and update the location in the database
     */
    public void updateAnimalLocation(String animalName, String newLocation, String updatedBy) {
        if (dataManager != null) {
            String oldLocation = getAnimalLocation(animalName);
            
            dataManager.updateAnimalLocation(animalName, newLocation);
            
            // Log the location update activity
            logActivity(animalName, "Unknown", "LOCATION_UPDATE", 
                       String.format("Location updated from '%s' to '%s'", oldLocation, newLocation), 
                       newLocation, updatedBy);
                       
            System.out.println(String.format("Location updated for %s: %s -> %s", 
                              animalName, oldLocation, newLocation));
        }
    }
    
    /**
     * Get all activities from the database
     * Used by web interface and reporting
     */
    public List<Activity> getAllActivities() {
        if (dataManager != null) {
            return dataManager.getActivities();
        }
        return new ArrayList<>();
    }
    
    /**
     * Get all animal locations from the database
     * Used by web interface and reporting
     */
    public Map<String, String> getAllAnimalLocations() {
        if (dataManager != null) {
            return dataManager.getAllLocations();
        }
        return new HashMap<>();
    }
    
    /**
     * Get activities by type
     * Useful for filtering activities (e.g., only INTAKE, only LOCATION_UPDATE, etc.)
     */
    public List<Activity> getActivitiesByType(String activityType) {
        List<Activity> filteredActivities = new ArrayList<>();
        
        List<Activity> allActivities = getAllActivities();
        for (Activity activity : allActivities) {
            if (activity.getActivityType().equalsIgnoreCase(activityType)) {
                filteredActivities.add(activity);
            }
        }
        
        // Sort by timestamp (newest first)
        filteredActivities.sort((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()));
        
        return filteredActivities;
    }
    
    /**
     * Get activities by performer
     * Useful for tracking which user performed which actions
     */
    public List<Activity> getActivitiesByPerformer(String performedBy) {
        List<Activity> userActivities = new ArrayList<>();
        
        List<Activity> allActivities = getAllActivities();
        for (Activity activity : allActivities) {
            if (activity.getPerformedBy().equalsIgnoreCase(performedBy)) {
                userActivities.add(activity);
            }
        }
        
        // Sort by timestamp (newest first)
        userActivities.sort((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()));
        
        return userActivities;
    }
    
    /**
     * Get recent activities (within specified number of days)
     * Useful for dashboard widgets showing recent system activity
     */
    public List<Activity> getRecentActivities(int days) {
        List<Activity> recentActivities = new ArrayList<>();
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        List<Activity> allActivities = getAllActivities();
        
        for (Activity activity : allActivities) {
            if (activity.getTimestamp().isAfter(cutoffDate)) {
                recentActivities.add(activity);
            }
        }
        
        // Sort by timestamp (newest first)
        recentActivities.sort((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()));
        
        return recentActivities;
    }
    
    /**
     * Get activity statistics
     * Returns a map with counts of different activity types
     */
    public Map<String, Integer> getActivityStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        
        List<Activity> allActivities = getAllActivities();
        
        for (Activity activity : allActivities) {
            String type = activity.getActivityType();
            stats.put(type, stats.getOrDefault(type, 0) + 1);
        }
        
        return stats;
    }
    
    /**
     * Generate a summary report of system activity
     * Useful for administrative reporting
     */
    public String generateActivitySummary(int days) {
        List<Activity> recentActivities = getRecentActivities(days);
        Map<String, Integer> activityStats = new HashMap<>();
        Map<String, Integer> userStats = new HashMap<>();
        
        for (Activity activity : recentActivities) {
            // Count by activity type
            String type = activity.getActivityType();
            activityStats.put(type, activityStats.getOrDefault(type, 0) + 1);
            
            // Count by user
            String user = activity.getPerformedBy();
            userStats.put(user, userStats.getOrDefault(user, 0) + 1);
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Activity Summary (Last %d days)\n", days));
        summary.append("=".repeat(40)).append("\n");
        summary.append(String.format("Total Activities: %d\n\n", recentActivities.size()));
        
        summary.append("Activities by Type:\n");
        for (Map.Entry<String, Integer> entry : activityStats.entrySet()) {
            summary.append(String.format("  %-15s: %d\n", entry.getKey(), entry.getValue()));
        }
        
        summary.append("\nActivities by User:\n");
        for (Map.Entry<String, Integer> entry : userStats.entrySet()) {
            summary.append(String.format("  %-15s: %d\n", entry.getKey(), entry.getValue()));
        }
        
        return summary.toString();
    }
}