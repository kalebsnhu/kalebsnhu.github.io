public class Bird extends RescueAnimal {
    private String species;
    private String wingspan;
    private boolean canFly;
    private String beakType;

    // Default Constructor
    public Bird() {
        super();
        setAnimalType("Bird");
    }

    // Parameterized Constructor
    public Bird(String name, String species, String wingspan, boolean canFly, String beakType,
                String gender, String age, String weight, String acquisitionDate, 
                String acquisitionCountry, String trainingStatus, boolean reserved, 
                String inServiceCountry) {
        super(name, "Bird", gender, age, weight, acquisitionDate, acquisitionCountry,
              trainingStatus, reserved, inServiceCountry);
        this.species = species;
        this.wingspan = wingspan;
        this.canFly = canFly;
        this.beakType = beakType;
    }

    // Getter and Setter methods
    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getWingspan() {
        return wingspan;
    }

    public void setWingspan(String wingspan) {
        this.wingspan = wingspan;
    }

    public boolean isCanFly() {
        return canFly;
    }

    public void setCanFly(boolean canFly) {
        this.canFly = canFly;
    }

    public String getBeakType() {
        return beakType;
    }

    public void setBeakType(String beakType) {
        this.beakType = beakType;
    }

    // Enhanced methods specific to Bird
    public boolean isSuitableForDetection() {
        // Intelligent species are better for detection and alert work
        String[] detectionSpecies = {"African Grey Parrot", "Cockatiel", "Macaw", "Amazon Parrot"};
        for (String detectionType : detectionSpecies) {
            if (species.equalsIgnoreCase(detectionType)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSuitableForCalming() {
        // Gentle, quiet species are better for emotional support
        String[] calmingSpecies = {"Canary", "Finch", "Budgerigar", "Dove"};
        for (String calmingType : calmingSpecies) {
            if (species.equalsIgnoreCase(calmingType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("Bird: %s (Species: %s, %s, %s years, %s, Wingspan: %s, Training: %s, Reserved: %s)", 
                           getName(), species, getGender(), getAge(), getWeight(), wingspan,
                           getTrainingStatus(), getReserved());
    }
}