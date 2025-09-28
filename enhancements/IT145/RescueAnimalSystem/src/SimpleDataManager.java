import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.time.LocalDateTime;

public class SimpleDataManager {
    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + "/users.txt";
    private static final String ANIMALS_FILE = DATA_DIR + "/animals.txt";
    private static final String ACTIVITIES_FILE = DATA_DIR + "/activities.txt";
    private static final String LOCATIONS_FILE = DATA_DIR + "/locations.txt";
    
    // In-memory data structures
    private Map<String, User> users;
    private List<RescueAnimal> animals;
    private List<Activity> activities;
    private Map<String, String> animalLocations;
    
    public SimpleDataManager() {
        initializeDataStructures();
        loadAllData();
    }
    
    private void initializeDataStructures() {
        users = new HashMap<>();
        animals = new ArrayList<>();
        activities = new ArrayList<>();
        animalLocations = new HashMap<>();
        
        // Create data directory
        new File(DATA_DIR).mkdirs();
    }
    
    public void loadAllData() {
        loadUsers();
        loadAnimals();
        loadActivities();
        loadLocations();
        
        System.out.println("Database loaded successfully");
        System.out.println("Users: " + users.size());
        System.out.println("Animals: " + animals.size());
        System.out.println("Activities: " + activities.size());
        System.out.println("Locations: " + animalLocations.size());
    }
    
    public void saveAll() {
        saveUsers();
        saveAnimals();
        saveActivities();
        saveLocations();
        System.out.println("All data saved to database");
    }
    
    // USER MANAGEMENT
    private void loadUsers() {
        try {
            if (Files.exists(Paths.get(USERS_FILE))) {
                List<String> lines = Files.readAllLines(Paths.get(USERS_FILE));
                for (String line : lines) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 5) {
                        User user = new User(parts[0], parts[1], parts[2], UserRole.valueOf(parts[3]));
                        user.setActive(Boolean.parseBoolean(parts[4]));
                        users.put(user.getUsername(), user);
                    } else if (parts.length >= 4) {
                        // Legacy format without active status
                        User user = new User(parts[0], parts[1], parts[2], UserRole.valueOf(parts[3]));
                        users.put(user.getUsername(), user);
                    }
                }
            }
            
            if (users.isEmpty()) {
                createDefaultAdmin();
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
            createDefaultAdmin();
        }
    }
    
    public void saveUsers() {
        try {
            StringBuilder sb = new StringBuilder();
            for (User user : users.values()) {
                sb.append(user.getUsername()).append("|")
                  .append(user.getPassword()).append("|")
                  .append(user.getFullName()).append("|")
                  .append(user.getRole().name()).append("|")
                  .append(user.isActive()).append("\n");
            }
            Files.write(Paths.get(USERS_FILE), sb.toString().getBytes());
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }
    
    private void createDefaultAdmin() {
        String encryptedPassword = PasswordUtil.encrypt("admin123");
        User admin = new User("admin", encryptedPassword, "System Administrator", UserRole.ADMIN);
        users.put("admin", admin);
        saveUsers();
        System.out.println("Default admin user created (username: admin, password: admin123)");
    }
    
    public User getUser(String username) {
        return users.get(username);
    }
    
    public Map<String, User> getUsers() {
        return new HashMap<>(users);
    }
    
    public boolean addUser(User user) {
        if (users.containsKey(user.getUsername())) {
            return false;
        }
        users.put(user.getUsername(), user);
        saveUsers();
        return true;
    }
    
    public boolean removeUser(String username) {
        if (users.containsKey(username)) {
            users.remove(username);
            saveUsers();
            return true;
        }
        return false;
    }
    
    public boolean updateUser(User user) {
        if (users.containsKey(user.getUsername())) {
            users.put(user.getUsername(), user);
            saveUsers();
            return true;
        }
        return false;
    }
    
    // ANIMAL MANAGEMENT
    private void loadAnimals() {
        try {
            if (Files.exists(Paths.get(ANIMALS_FILE))) {
                List<String> lines = Files.readAllLines(Paths.get(ANIMALS_FILE));
                for (String line : lines) {
                    RescueAnimal animal = parseAnimalFromLine(line);
                    if (animal != null) {
                        animals.add(animal);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading animals: " + e.getMessage());
        }
    }
    
    private void saveAnimals() {
        try {
            StringBuilder sb = new StringBuilder();
            for (RescueAnimal animal : animals) {
                sb.append(animalToLine(animal)).append("\n");
            }
            Files.write(Paths.get(ANIMALS_FILE), sb.toString().getBytes());
        } catch (IOException e) {
            System.err.println("Error saving animals: " + e.getMessage());
        }
    }
    
    private RescueAnimal parseAnimalFromLine(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 10) return null;
        
        String type = parts[1];
        switch (type.toLowerCase()) {
            case "dog":
                if (parts.length >= 11) {
                    return new Dog(parts[0], parts[10], parts[2], parts[3], parts[4],
                                  parts[5], parts[6], parts[7], Boolean.parseBoolean(parts[8]), parts[9]);
                }
                break;
            case "monkey":
                if (parts.length >= 14) {
                    return new Monkey(parts[0], parts[2], parts[3], parts[4], parts[10],
                                     parts[11], parts[12], parts[13], parts[5], parts[6],
                                     parts[7], Boolean.parseBoolean(parts[8]), parts[9]);
                }
                break;
            case "cat":
                if (parts.length >= 13) {
                    return new Cat(parts[0], parts[10], parts[11], Boolean.parseBoolean(parts[12]),
                                  parts[2], parts[3], parts[4], parts[5], parts[6],
                                  parts[7], Boolean.parseBoolean(parts[8]), parts[9]);
                }
                break;
            case "bird":
                if (parts.length >= 14) {
                    return new Bird(parts[0], parts[10], parts[11], Boolean.parseBoolean(parts[12]),
                                   parts[13], parts[2], parts[3], parts[4], parts[5], parts[6],
                                   parts[7], Boolean.parseBoolean(parts[8]), parts[9]);
                }
                break;
            case "rabbit":
                if (parts.length >= 14) {
                    return new Rabbit(parts[0], parts[10], parts[11], parts[12],
                                     Boolean.parseBoolean(parts[13]), parts[2], parts[3], parts[4],
                                     parts[5], parts[6], parts[7], Boolean.parseBoolean(parts[8]), parts[9]);
                }
                break;
        }
        return null;
    }
    
    private String animalToLine(RescueAnimal animal) {
        StringBuilder sb = new StringBuilder();
        sb.append(animal.getName()).append("|")
          .append(animal.getAnimalType()).append("|")
          .append(animal.getGender()).append("|")
          .append(animal.getAge()).append("|")
          .append(animal.getWeight()).append("|")
          .append(animal.getAcquisitionDate()).append("|")
          .append(animal.getAcquisitionLocation()).append("|")
          .append(animal.getTrainingStatus()).append("|")
          .append(animal.getReserved()).append("|")
          .append(animal.getInServiceLocation());
        
        if (animal instanceof Dog) {
            Dog dog = (Dog) animal;
            sb.append("|").append(dog.getBreed());
        } else if (animal instanceof Monkey) {
            Monkey monkey = (Monkey) animal;
            sb.append("|").append(monkey.getSpecies())
              .append("|").append(monkey.getTailLength())
              .append("|").append(monkey.getHeight())
              .append("|").append(monkey.getBodyLength());
        } else if (animal instanceof Cat) {
            Cat cat = (Cat) animal;
            sb.append("|").append(cat.getBreed())
              .append("|").append(cat.getCoatColor())
              .append("|").append(cat.isDeclawed());
        } else if (animal instanceof Bird) {
            Bird bird = (Bird) animal;
            sb.append("|").append(bird.getSpecies())
              .append("|").append(bird.getWingspan())
              .append("|").append(bird.isCanFly())
              .append("|").append(bird.getBeakType());
        } else if (animal instanceof Rabbit) {
            Rabbit rabbit = (Rabbit) animal;
            sb.append("|").append(rabbit.getBreed())
              .append("|").append(rabbit.getFurColor())
              .append("|").append(rabbit.getEarType())
              .append("|").append(rabbit.isLitterTrained());
        }
        
        return sb.toString();
    }
    
    public List<RescueAnimal> getAnimals() {
        return new ArrayList<>(animals);
    }
    
    public boolean addAnimal(RescueAnimal animal) {
        boolean exists = animals.stream()
            .anyMatch(a -> a.getName().equalsIgnoreCase(animal.getName()));
        
        if (exists) {
            return false;
        }
        
        animals.add(animal);
        saveAnimals();
        return true;
    }
    
    public RescueAnimal findAnimalByName(String name) {
        return animals.stream()
            .filter(animal -> animal.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }
    
    public boolean removeAnimal(String name) {
        boolean removed = animals.removeIf(animal -> 
            animal.getName().equalsIgnoreCase(name));
        
        if (removed) {
            animalLocations.remove(name);
            saveAnimals();
            saveLocations();
        }
        
        return removed;
    }
    
    // ACTIVITY MANAGEMENT
    private void loadActivities() {
        try {
            if (Files.exists(Paths.get(ACTIVITIES_FILE))) {
                List<String> lines = Files.readAllLines(Paths.get(ACTIVITIES_FILE));
                for (String line : lines) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 6) {
                        Activity activity = new Activity(parts[0], parts[1], parts[2], 
                                                       parts[3], parts[4], parts[5]);
                        activities.add(activity);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading activities: " + e.getMessage());
        }
    }
    
    private void saveActivities() {
        try {
            StringBuilder sb = new StringBuilder();
            for (Activity activity : activities) {
                sb.append(activity.getAnimalName()).append("|")
                  .append(activity.getAnimalType()).append("|")
                  .append(activity.getActivityType()).append("|")
                  .append(activity.getDescription()).append("|")
                  .append(activity.getLocation()).append("|")
                  .append(activity.getPerformedBy()).append("\n");
            }
            Files.write(Paths.get(ACTIVITIES_FILE), sb.toString().getBytes());
        } catch (IOException e) {
            System.err.println("Error saving activities: " + e.getMessage());
        }
    }
    
    public void addActivity(Activity activity) {
        activities.add(activity);
        saveActivities();
    }
    
    public List<Activity> getActivities() {
        return new ArrayList<>(activities);
    }
    
    // LOCATION MANAGEMENT
    private void loadLocations() {
        try {
            if (Files.exists(Paths.get(LOCATIONS_FILE))) {
                List<String> lines = Files.readAllLines(Paths.get(LOCATIONS_FILE));
                for (String line : lines) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 2) {
                        animalLocations.put(parts[0], parts[1]);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading locations: " + e.getMessage());
        }
    }
    
    private void saveLocations() {
        try {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : animalLocations.entrySet()) {
                sb.append(entry.getKey()).append("|").append(entry.getValue()).append("\n");
            }
            Files.write(Paths.get(LOCATIONS_FILE), sb.toString().getBytes());
        } catch (IOException e) {
            System.err.println("Error saving locations: " + e.getMessage());
        }
    }
    
    public String getAnimalLocation(String animalName) {
        return animalLocations.getOrDefault(animalName, "Location not set");
    }
    
    public void updateAnimalLocation(String animalName, String location) {
        animalLocations.put(animalName, location);
        saveLocations();
    }
    
    public Map<String, String> getAllLocations() {
        return new HashMap<>(animalLocations);
    }
}