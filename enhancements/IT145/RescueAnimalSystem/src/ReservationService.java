public class ReservationService {
    private AnimalService animalService;
    private MonitoringSystem monitoringSystem;
    
    public ReservationService(AnimalService animalService, MonitoringSystem monitoringSystem) {
        this.animalService = animalService;
        this.monitoringSystem = monitoringSystem;
    }
    
    /**
     * Reserve an animal for service
     * Only animals that are "in service" and not already reserved can be reserved
     */
    public boolean reserveAnimal(String animalType, String serviceCountry, String reservedBy) {
        if (animalType == null || serviceCountry == null || reservedBy == null) {
            System.out.println("Invalid reservation parameters");
            return false;
        }
        
        switch (animalType.toLowerCase()) {
            case "dog":
                return reserveDog(serviceCountry, reservedBy);
            case "monkey":
                return reserveMonkey(serviceCountry, reservedBy);
            case "cat":
                return reserveCat(serviceCountry, reservedBy);
            case "bird":
                return reserveBird(serviceCountry, reservedBy);
            case "rabbit":
                return reserveRabbit(serviceCountry, reservedBy);
            default:
                System.out.println("Unknown animal type: " + animalType);
                return false;
        }
    }
    
    /**
     * Reserve a specific animal by name
     * More precise than reserving by type and country
     */
    public boolean reserveSpecificAnimal(String animalName, String reservedBy) {
        if (animalName == null || reservedBy == null) {
            System.out.println("Invalid reservation parameters");
            return false;
        }
        
        // Try to find the animal in each type list
        Dog dog = animalService.findDogByName(animalName);
        if (dog != null) {
            return reserveSpecificDog(dog, reservedBy);
        }
        
        Monkey monkey = animalService.findMonkeyByName(animalName);
        if (monkey != null) {
            return reserveSpecificMonkey(monkey, reservedBy);
        }
        
        Cat cat = animalService.findCatByName(animalName);
        if (cat != null) {
            return reserveSpecificCat(cat, reservedBy);
        }
        
        Bird bird = animalService.findBirdByName(animalName);
        if (bird != null) {
            return reserveSpecificBird(bird, reservedBy);
        }
        
        Rabbit rabbit = animalService.findRabbitByName(animalName);
        if (rabbit != null) {
            return reserveSpecificRabbit(rabbit, reservedBy);
        }
        
        System.out.println("Animal not found: " + animalName);
        return false;
    }
    
    /**
     * Cancel a reservation for a specific animal
     */
    public boolean cancelReservation(String animalName, String cancelledBy) {
        if (animalName == null || cancelledBy == null) {
            System.out.println("Invalid cancellation parameters");
            return false;
        }
        
        // Try to find the animal in each type list
        Dog dog = animalService.findDogByName(animalName);
        if (dog != null && dog.getReserved()) {
            dog.setReserved(false);
            monitoringSystem.logActivity(dog.getName(), "Dog", "RESERVATION_CANCELLED", 
                                       "Reservation cancelled", 
                                       monitoringSystem.getAnimalLocation(dog.getName()), cancelledBy);
            System.out.println("Reservation cancelled for dog: " + animalName);
            return true;
        }
        
        Monkey monkey = animalService.findMonkeyByName(animalName);
        if (monkey != null && monkey.getReserved()) {
            monkey.setReserved(false);
            monitoringSystem.logActivity(monkey.getName(), "Monkey", "RESERVATION_CANCELLED", 
                                       "Reservation cancelled", 
                                       monitoringSystem.getAnimalLocation(monkey.getName()), cancelledBy);
            System.out.println("Reservation cancelled for monkey: " + animalName);
            return true;
        }
        
        Cat cat = animalService.findCatByName(animalName);
        if (cat != null && cat.getReserved()) {
            cat.setReserved(false);
            monitoringSystem.logActivity(cat.getName(), "Cat", "RESERVATION_CANCELLED", 
                                       "Reservation cancelled", 
                                       monitoringSystem.getAnimalLocation(cat.getName()), cancelledBy);
            System.out.println("Reservation cancelled for cat: " + animalName);
            return true;
        }
        
        Bird bird = animalService.findBirdByName(animalName);
        if (bird != null && bird.getReserved()) {
            bird.setReserved(false);
            monitoringSystem.logActivity(bird.getName(), "Bird", "RESERVATION_CANCELLED", 
                                       "Reservation cancelled", 
                                       monitoringSystem.getAnimalLocation(bird.getName()), cancelledBy);
            System.out.println("Reservation cancelled for bird: " + animalName);
            return true;
        }
        
        Rabbit rabbit = animalService.findRabbitByName(animalName);
        if (rabbit != null && rabbit.getReserved()) {
            rabbit.setReserved(false);
            monitoringSystem.logActivity(rabbit.getName(), "Rabbit", "RESERVATION_CANCELLED", 
                                       "Reservation cancelled", 
                                       monitoringSystem.getAnimalLocation(rabbit.getName()), cancelledBy);
            System.out.println("Reservation cancelled for rabbit: " + animalName);
            return true;
        }
        
        System.out.println("Animal not found or not reserved: " + animalName);
        return false;
    }
    
    // Private helper methods for each animal type
    
    private boolean reserveDog(String serviceCountry, String reservedBy) {
        for (Dog dog : animalService.getDogList()) {
            if (dog.getInServiceLocation().equalsIgnoreCase(serviceCountry) && 
                !dog.getReserved() && 
                dog.getTrainingStatus().equalsIgnoreCase("in service")) {
                
                dog.setReserved(true);
                monitoringSystem.logActivity(dog.getName(), "Dog", "RESERVATION", 
                                           "Reserved for service in " + serviceCountry, 
                                           serviceCountry, reservedBy);
                System.out.println("Dog reserved: " + dog.getName() + " for " + serviceCountry);
                return true;
            }
        }
        return false;
    }
    
    private boolean reserveMonkey(String serviceCountry, String reservedBy) {
        for (Monkey monkey : animalService.getMonkeyList()) {
            if (monkey.getInServiceLocation().equalsIgnoreCase(serviceCountry) && 
                !monkey.getReserved() && 
                monkey.getTrainingStatus().equalsIgnoreCase("in service")) {
                
                monkey.setReserved(true);
                monitoringSystem.logActivity(monkey.getName(), "Monkey", "RESERVATION", 
                                           "Reserved for service in " + serviceCountry, 
                                           serviceCountry, reservedBy);
                System.out.println("Monkey reserved: " + monkey.getName() + " for " + serviceCountry);
                return true;
            }
        }
        return false;
    }
    
    private boolean reserveCat(String serviceCountry, String reservedBy) {
        for (Cat cat : animalService.getCatList()) {
            if (cat.getInServiceLocation().equalsIgnoreCase(serviceCountry) && 
                !cat.getReserved() && 
                cat.getTrainingStatus().equalsIgnoreCase("in service")) {
                
                cat.setReserved(true);
                monitoringSystem.logActivity(cat.getName(), "Cat", "RESERVATION", 
                                           "Reserved for service in " + serviceCountry, 
                                           serviceCountry, reservedBy);
                System.out.println("Cat reserved: " + cat.getName() + " for " + serviceCountry);
                return true;
            }
        }
        return false;
    }
    
    private boolean reserveBird(String serviceCountry, String reservedBy) {
        for (Bird bird : animalService.getBirdList()) {
            if (bird.getInServiceLocation().equalsIgnoreCase(serviceCountry) && 
                !bird.getReserved() && 
                bird.getTrainingStatus().equalsIgnoreCase("in service")) {
                
                bird.setReserved(true);
                monitoringSystem.logActivity(bird.getName(), "Bird", "RESERVATION", 
                                           "Reserved for service in " + serviceCountry, 
                                           serviceCountry, reservedBy);
                System.out.println("Bird reserved: " + bird.getName() + " for " + serviceCountry);
                return true;
            }
        }
        return false;
    }
    
    private boolean reserveRabbit(String serviceCountry, String reservedBy) {
        for (Rabbit rabbit : animalService.getRabbitList()) {
            if (rabbit.getInServiceLocation().equalsIgnoreCase(serviceCountry) && 
                !rabbit.getReserved() && 
                rabbit.getTrainingStatus().equalsIgnoreCase("in service")) {
                
                rabbit.setReserved(true);
                monitoringSystem.logActivity(rabbit.getName(), "Rabbit", "RESERVATION", 
                                           "Reserved for service in " + serviceCountry, 
                                           serviceCountry, reservedBy);
                System.out.println("Rabbit reserved: " + rabbit.getName() + " for " + serviceCountry);
                return true;
            }
        }
        return false;
    }
    
    // Specific animal reservation methods
    
    private boolean reserveSpecificDog(Dog dog, String reservedBy) {
        if (dog.getReserved()) {
            System.out.println("Dog is already reserved: " + dog.getName());
            return false;
        }
        
        if (!dog.getTrainingStatus().equalsIgnoreCase("in service")) {
            System.out.println("Dog is not ready for service: " + dog.getName() + " (Status: " + dog.getTrainingStatus() + ")");
            return false;
        }
        
        dog.setReserved(true);
        monitoringSystem.logActivity(dog.getName(), "Dog", "RESERVATION", 
                                   "Specifically reserved by " + reservedBy, 
                                   monitoringSystem.getAnimalLocation(dog.getName()), reservedBy);
        System.out.println("Dog specifically reserved: " + dog.getName());
        return true;
    }
    
    private boolean reserveSpecificMonkey(Monkey monkey, String reservedBy) {
        if (monkey.getReserved()) {
            System.out.println("Monkey is already reserved: " + monkey.getName());
            return false;
        }
        
        if (!monkey.getTrainingStatus().equalsIgnoreCase("in service")) {
            System.out.println("Monkey is not ready for service: " + monkey.getName() + " (Status: " + monkey.getTrainingStatus() + ")");
            return false;
        }
        
        monkey.setReserved(true);
        monitoringSystem.logActivity(monkey.getName(), "Monkey", "RESERVATION", 
                                   "Specifically reserved by " + reservedBy, 
                                   monitoringSystem.getAnimalLocation(monkey.getName()), reservedBy);
        System.out.println("Monkey specifically reserved: " + monkey.getName());
        return true;
    }
    
    private boolean reserveSpecificCat(Cat cat, String reservedBy) {
        if (cat.getReserved()) {
            System.out.println("Cat is already reserved: " + cat.getName());
            return false;
        }
        
        if (!cat.getTrainingStatus().equalsIgnoreCase("in service")) {
            System.out.println("Cat is not ready for service: " + cat.getName() + " (Status: " + cat.getTrainingStatus() + ")");
            return false;
        }
        
        cat.setReserved(true);
        monitoringSystem.logActivity(cat.getName(), "Cat", "RESERVATION", 
                                   "Specifically reserved by " + reservedBy, 
                                   monitoringSystem.getAnimalLocation(cat.getName()), reservedBy);
        System.out.println("Cat specifically reserved: " + cat.getName());
        return true;
    }
    
    private boolean reserveSpecificBird(Bird bird, String reservedBy) {
        if (bird.getReserved()) {
            System.out.println("Bird is already reserved: " + bird.getName());
            return false;
        }
        
        if (!bird.getTrainingStatus().equalsIgnoreCase("in service")) {
            System.out.println("Bird is not ready for service: " + bird.getName() + " (Status: " + bird.getTrainingStatus() + ")");
            return false;
        }
        
        bird.setReserved(true);
        monitoringSystem.logActivity(bird.getName(), "Bird", "RESERVATION", 
                                   "Specifically reserved by " + reservedBy, 
                                   monitoringSystem.getAnimalLocation(bird.getName()), reservedBy);
        System.out.println("Bird specifically reserved: " + bird.getName());
        return true;
    }
    
    private boolean reserveSpecificRabbit(Rabbit rabbit, String reservedBy) {
        if (rabbit.getReserved()) {
            System.out.println("Rabbit is already reserved: " + rabbit.getName());
            return false;
        }
        
        if (!rabbit.getTrainingStatus().equalsIgnoreCase("in service")) {
            System.out.println("Rabbit is not ready for service: " + rabbit.getName() + " (Status: " + rabbit.getTrainingStatus() + ")");
            return false;
        }
        
        rabbit.setReserved(true);
        monitoringSystem.logActivity(rabbit.getName(), "Rabbit", "RESERVATION", 
                                   "Specifically reserved by " + reservedBy, 
                                   monitoringSystem.getAnimalLocation(rabbit.getName()), reservedBy);
        System.out.println("Rabbit specifically reserved: " + rabbit.getName());
        return true;
    }
}