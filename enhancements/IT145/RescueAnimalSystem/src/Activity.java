import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Activity {
    private String animalName;
    private String animalType;
    private String activityType;
    private String description;
    private String location;
    private LocalDateTime timestamp;
    private String performedBy;
    
    public Activity(String animalName, String animalType, String activityType, 
                   String description, String location, String performedBy) {
        this.animalName = animalName;
        this.animalType = animalType;
        this.activityType = activityType;
        this.description = description;
        this.location = location;
        this.performedBy = performedBy;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters
    public String getAnimalName() { return animalName; }
    public String getAnimalType() { return animalType; }
    public String getActivityType() { return activityType; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getPerformedBy() { return performedBy; }
    
    @Override
    public String toString() {
        return String.format("[%s] %s %s - %s at %s (by %s)", 
            timestamp.format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")),
            animalType, animalName, description, location, performedBy);
    }
}