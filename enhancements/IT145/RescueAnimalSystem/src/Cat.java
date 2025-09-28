public class Cat extends RescueAnimal {
    private String breed;
    private String coatColor;
    private boolean declawed;

    // Default Constructor
    public Cat() {
        super();
        setAnimalType("Cat");
    }

    // Parameterized Constructor
    public Cat(String name, String breed, String coatColor, boolean declawed, String gender, String age,
               String weight, String acquisitionDate, String acquisitionCountry,
               String trainingStatus, boolean reserved, String inServiceCountry) {
        super(name, "Cat", gender, age, weight, acquisitionDate, acquisitionCountry,
              trainingStatus, reserved, inServiceCountry);
        this.breed = breed;
        this.coatColor = coatColor;
        this.declawed = declawed;
    }

    // Getter and Setter methods
    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getCoatColor() {
        return coatColor;
    }

    public void setCoatColor(String coatColor) {
        this.coatColor = coatColor;
    }

    public boolean isDeclawed() {
        return declawed;
    }

    public void setDeclawed(boolean declawed) {
        this.declawed = declawed;
    }

    // Enhanced methods specific to Cat
    public boolean isSuitableForTherapy() {
        // Calm, gentle breeds are better for therapy work
        String[] therapyBreeds = {"Persian", "Ragdoll", "Maine Coon", "British Shorthair", "Siamese", "Other"};
        for (String therapyBreed : therapyBreeds) {
            if (breed.equalsIgnoreCase(therapyBreed)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSuitableForDetection() {
        // Active, intelligent breeds are better for detection work
        String[] detectionBreeds = {"Siamese", "Bengal", "Abyssinian", "Oriental Shorthair", "Other"};
        for (String detectionBreed : detectionBreeds) {
            if (breed.equalsIgnoreCase(detectionBreed)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("Cat: %s (Breed: %s, Color: %s, %s, %s years, %s, Training: %s, Reserved: %s)", 
                           getName(), breed, coatColor, getGender(), getAge(), getWeight(), 
                           getTrainingStatus(), getReserved());
    }
}