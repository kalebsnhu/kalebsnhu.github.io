import java.util.*;

public class AnimalService {
    private List<Dog> dogList;
    private List<Monkey> monkeyList;
    private List<Cat> catList;
    private List<Bird> birdList;
    private List<Rabbit> rabbitList;
    private List<String> validMonkeySpecies;
    private List<String> validCatBreeds;
    private List<String> validBirdSpecies;
    private List<String> validRabbitBreeds;
    private MonitoringSystem monitoringSystem;
    private SimpleDataManager dataManager;  // Changed from JsonDataManager
    
    // Constructor with both MonitoringSystem and SimpleDataManager
    public AnimalService(MonitoringSystem monitoringSystem, SimpleDataManager dataManager) {  // Changed from JsonDataManager
        this.monitoringSystem = monitoringSystem;
        this.dataManager = dataManager;
        initializeValidSpecies();
        refreshFromDatabase();
    }
    
    // Constructor with just MonitoringSystem for backward compatibility
    public AnimalService(MonitoringSystem monitoringSystem) {
        this.monitoringSystem = monitoringSystem;
        this.dataManager = null;
        dogList = new ArrayList<>();
        monkeyList = new ArrayList<>();
        catList = new ArrayList<>();
        birdList = new ArrayList<>();
        rabbitList = new ArrayList<>();
        
        initializeValidSpecies();
    }
    
    /**
     * Refresh in-memory animal lists from the database
     * This allows real-time updates without restarting the application
     */
    public synchronized void refreshFromDatabase() {
        dogList = new ArrayList<>();
        monkeyList = new ArrayList<>();
        catList = new ArrayList<>();
        birdList = new ArrayList<>();
        rabbitList = new ArrayList<>();
        
        if (dataManager != null) {
            List<RescueAnimal> animals = dataManager.getAnimals();
            for (RescueAnimal animal : animals) {
                switch (animal.getAnimalType().toLowerCase()) {
                    case "dog":
                        if (animal instanceof Dog) {
                            dogList.add((Dog) animal);
                        }
                        break;
                    case "monkey":
                        if (animal instanceof Monkey) {
                            monkeyList.add((Monkey) animal);
                        }
                        break;
                    case "cat":
                        if (animal instanceof Cat) {
                            catList.add((Cat) animal);
                        }
                        break;
                    case "bird":
                        if (animal instanceof Bird) {
                            birdList.add((Bird) animal);
                        }
                        break;
                    case "rabbit":
                        if (animal instanceof Rabbit) {
                            rabbitList.add((Rabbit) animal);
                        }
                        break;
                }
            }
        }
        
        System.out.println("Animal lists refreshed from database:");
        System.out.println("  Dogs: " + dogList.size());
        System.out.println("  Monkeys: " + monkeyList.size());
        System.out.println("  Cats: " + catList.size());
        System.out.println("  Birds: " + birdList.size());
        System.out.println("  Rabbits: " + rabbitList.size());
    }
    
    private void initializeValidSpecies() {
        validMonkeySpecies = Arrays.asList("capuchin", "guenon", "macaque", 
                                         "marmoset", "squirrel monkey", "tamarin");
        
        validCatBreeds = Arrays.asList("persian", "ragdoll", "maine coon", "british shorthair", 
                                     "siamese", "bengal", "abyssinian", "oriental shorthair");
        
        validBirdSpecies = Arrays.asList("african grey parrot", "cockatiel", "macaw", "amazon parrot",
                                       "canary", "finch", "budgerigar", "dove");
        
        validRabbitBreeds = Arrays.asList("holland lop", "mini rex", "lionhead", "dutch", 
                                        "english angora", "new zealand", "californian", "flemish giant", "mini lop");
    }
    
    // Dog methods
    public synchronized boolean addDog(Dog dog, String addedBy) {
        if (findDogByName(dog.getName()) != null) {
            return false;
        }
        
        boolean success = false;
        if (dataManager != null) {
            success = dataManager.addAnimal(dog);
        }
        
        if (success) {
            dogList.add(dog);
            monitoringSystem.logActivity(dog.getName(), "Dog", "INTAKE", 
                                       "New dog added to system", "Intake Facility", addedBy);
        }
        
        return success;
    }
    
    public Dog findDogByName(String name) {
        return dogList.stream()
                     .filter(dog -> dog.getName().equalsIgnoreCase(name))
                     .findFirst()
                     .orElse(null);
    }
    
    // Monkey methods
    public synchronized boolean addMonkey(Monkey monkey, String addedBy) {
        if (findMonkeyByName(monkey.getName()) != null) {
            return false;
        }
        
        boolean success = false;
        if (dataManager != null) {
            success = dataManager.addAnimal(monkey);
        }
        
        if (success) {
            monkeyList.add(monkey);
            monitoringSystem.logActivity(monkey.getName(), "Monkey", "INTAKE", 
                                       "New monkey added to system", "Intake Facility", addedBy);
        }
        
        return success;
    }
    
    public Monkey findMonkeyByName(String name) {
        return monkeyList.stream()
                        .filter(monkey -> monkey.getName().equalsIgnoreCase(name))
                        .findFirst()
                        .orElse(null);
    }
    
    // Cat methods
    public synchronized boolean addCat(Cat cat, String addedBy) {
        if (findCatByName(cat.getName()) != null) {
            return false;
        }
        
        boolean success = false;
        if (dataManager != null) {
            success = dataManager.addAnimal(cat);
        }
        
        if (success) {
            catList.add(cat);
            monitoringSystem.logActivity(cat.getName(), "Cat", "INTAKE", 
                                       "New cat added to system", "Intake Facility", addedBy);
        }
        
        return success;
    }
    
    public Cat findCatByName(String name) {
        return catList.stream()
                     .filter(cat -> cat.getName().equalsIgnoreCase(name))
                     .findFirst()
                     .orElse(null);
    }
    
    // Bird methods
    public synchronized boolean addBird(Bird bird, String addedBy) {
        if (findBirdByName(bird.getName()) != null) {
            return false;
        }
        
        boolean success = false;
        if (dataManager != null) {
            success = dataManager.addAnimal(bird);
        }
        
        if (success) {
            birdList.add(bird);
            monitoringSystem.logActivity(bird.getName(), "Bird", "INTAKE", 
                                       "New bird added to system", "Intake Facility", addedBy);
        }
        
        return success;
    }
    
    public Bird findBirdByName(String name) {
        return birdList.stream()
                      .filter(bird -> bird.getName().equalsIgnoreCase(name))
                      .findFirst()
                      .orElse(null);
    }
    
    // Rabbit methods
    public synchronized boolean addRabbit(Rabbit rabbit, String addedBy) {
        if (findRabbitByName(rabbit.getName()) != null) {
            return false;
        }
        
        boolean success = false;
        if (dataManager != null) {
            success = dataManager.addAnimal(rabbit);
        }
        
        if (success) {
            rabbitList.add(rabbit);
            monitoringSystem.logActivity(rabbit.getName(), "Rabbit", "INTAKE", 
                                       "New rabbit added to system", "Intake Facility", addedBy);
        }
        
        return success;
    }
    
    public Rabbit findRabbitByName(String name) {
        return rabbitList.stream()
                        .filter(rabbit -> rabbit.getName().equalsIgnoreCase(name))
                        .findFirst()
                        .orElse(null);
    }
    
    /**
     * Delete an animal from the system
     * This will remove from both database and in-memory lists
     */
    public synchronized boolean deleteAnimal(String name, String deletedBy) {
        // Find the animal first
        RescueAnimal animal = null;
        String animalType = "";
        
        animal = findDogByName(name);
        if (animal != null) {
            animalType = "Dog";
        } else {
            animal = findMonkeyByName(name);
            if (animal != null) {
                animalType = "Monkey";
            } else {
                animal = findCatByName(name);
                if (animal != null) {
                    animalType = "Cat";
                } else {
                    animal = findBirdByName(name);
                    if (animal != null) {
                        animalType = "Bird";
                    } else {
                        animal = findRabbitByName(name);
                        if (animal != null) {
                            animalType = "Rabbit";
                        }
                    }
                }
            }
        }
        
        if (animal == null) {
            return false;
        }
        
        // Remove from database
        boolean success = false;
        if (dataManager != null) {
            success = dataManager.removeAnimal(name);
        }
        
        if (success) {
            // Remove from in-memory lists
            dogList.removeIf(dog -> dog.getName().equalsIgnoreCase(name));
            monkeyList.removeIf(monkey -> monkey.getName().equalsIgnoreCase(name));
            catList.removeIf(cat -> cat.getName().equalsIgnoreCase(name));
            birdList.removeIf(bird -> bird.getName().equalsIgnoreCase(name));
            rabbitList.removeIf(rabbit -> rabbit.getName().equalsIgnoreCase(name));
            
            // Log the deletion
            monitoringSystem.logActivity(name, animalType, "DELETION", 
                                       "Animal removed from system", "System", deletedBy);
        }
        
        return success;
    }
    
    // Validation methods
    public boolean isValidMonkeySpecies(String species) {
        return validMonkeySpecies.contains(species.toLowerCase());
    }
    
    public boolean isValidCatBreed(String breed) {
        return validCatBreeds.contains(breed.toLowerCase());
    }
    
    public boolean isValidBirdSpecies(String species) {
        return validBirdSpecies.contains(species.toLowerCase());
    }
    
    public boolean isValidRabbitBreed(String breed) {
        return validRabbitBreeds.contains(breed.toLowerCase());
    }
    
    // Getter methods - return copies to prevent external modification
    public List<Dog> getDogList() { return new ArrayList<>(dogList); }
    public List<Monkey> getMonkeyList() { return new ArrayList<>(monkeyList); }
    public List<Cat> getCatList() { return new ArrayList<>(catList); }
    public List<Bird> getBirdList() { return new ArrayList<>(birdList); }
    public List<Rabbit> getRabbitList() { return new ArrayList<>(rabbitList); }
    
    public List<String> getValidMonkeySpecies() { return new ArrayList<>(validMonkeySpecies); }
    public List<String> getValidCatBreeds() { return new ArrayList<>(validCatBreeds); }
    public List<String> getValidBirdSpecies() { return new ArrayList<>(validBirdSpecies); }
    public List<String> getValidRabbitBreeds() { return new ArrayList<>(validRabbitBreeds); }
    
    // Utility methods
    public int getTotalAnimals() {
        return dogList.size() + monkeyList.size() + catList.size() + birdList.size() + rabbitList.size();
    }
    
    public int getAvailableAnimals() {
        int available = 0;
        
        available += dogList.stream().mapToInt(dog -> 
            (dog.getTrainingStatus().equalsIgnoreCase("in service") && !dog.getReserved()) ? 1 : 0).sum();
        available += monkeyList.stream().mapToInt(monkey -> 
            (monkey.getTrainingStatus().equalsIgnoreCase("in service") && !monkey.getReserved()) ? 1 : 0).sum();
        available += catList.stream().mapToInt(cat -> 
            (cat.getTrainingStatus().equalsIgnoreCase("in service") && !cat.getReserved()) ? 1 : 0).sum();
        available += birdList.stream().mapToInt(bird -> 
            (bird.getTrainingStatus().equalsIgnoreCase("in service") && !bird.getReserved()) ? 1 : 0).sum();
        available += rabbitList.stream().mapToInt(rabbit -> 
            (rabbit.getTrainingStatus().equalsIgnoreCase("in service") && !rabbit.getReserved()) ? 1 : 0).sum();
            
        return available;
    }
    
    public int getReservedAnimals() {
        int reserved = 0;
        
        reserved += dogList.stream().mapToInt(dog -> dog.getReserved() ? 1 : 0).sum();
        reserved += monkeyList.stream().mapToInt(monkey -> monkey.getReserved() ? 1 : 0).sum();
        reserved += catList.stream().mapToInt(cat -> cat.getReserved() ? 1 : 0).sum();
        reserved += birdList.stream().mapToInt(bird -> bird.getReserved() ? 1 : 0).sum();
        reserved += rabbitList.stream().mapToInt(rabbit -> rabbit.getReserved() ? 1 : 0).sum();
        
        return reserved;
    }
    
    public int getTrainingAnimals() {
        int training = 0;
        
        training += dogList.stream().mapToInt(dog -> 
            (!dog.getReserved() && !"in service".equalsIgnoreCase(dog.getTrainingStatus())) ? 1 : 0).sum();
        training += monkeyList.stream().mapToInt(monkey -> 
            (!monkey.getReserved() && !"in service".equalsIgnoreCase(monkey.getTrainingStatus())) ? 1 : 0).sum();
        training += catList.stream().mapToInt(cat -> 
            (!cat.getReserved() && !"in service".equalsIgnoreCase(cat.getTrainingStatus())) ? 1 : 0).sum();
        training += birdList.stream().mapToInt(bird -> 
            (!bird.getReserved() && !"in service".equalsIgnoreCase(bird.getTrainingStatus())) ? 1 : 0).sum();
        training += rabbitList.stream().mapToInt(rabbit -> 
            (!rabbit.getReserved() && !"in service".equalsIgnoreCase(rabbit.getTrainingStatus())) ? 1 : 0).sum();
        
        return training;
    }
    
    // Image path helper method
    public String getAnimalImagePath(RescueAnimal animal) {
        String animalType = animal.getAnimalType().toLowerCase();
        String imageName = animal.getName() + "_" + animal.getAge() + ".jpg";
        return "images/" + animalType + "s/" + imageName;
    }
    
    /**
     * Get all available animals (in service and not reserved)
     */
    public List<RescueAnimal> getAvailableAnimalsList() {
        List<RescueAnimal> available = new ArrayList<>();
        
        dogList.stream()
            .filter(dog -> dog.getTrainingStatus().equalsIgnoreCase("in service") && !dog.getReserved())
            .forEach(available::add);
            
        monkeyList.stream()
            .filter(monkey -> monkey.getTrainingStatus().equalsIgnoreCase("in service") && !monkey.getReserved())
            .forEach(available::add);
            
        catList.stream()
            .filter(cat -> cat.getTrainingStatus().equalsIgnoreCase("in service") && !cat.getReserved())
            .forEach(available::add);
            
        birdList.stream()
            .filter(bird -> bird.getTrainingStatus().equalsIgnoreCase("in service") && !bird.getReserved())
            .forEach(available::add);
            
        rabbitList.stream()
            .filter(rabbit -> rabbit.getTrainingStatus().equalsIgnoreCase("in service") && !rabbit.getReserved())
            .forEach(available::add);
        
        return available;
    }
}