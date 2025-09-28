public class Dog extends RescueAnimal {
    private String breed;

    // Default Constructor
    public Dog() {
        super();
        setAnimalType("Dog");
    }

    // Parameterized Constructor
    public Dog(String name, String breed, String gender, String age,
               String weight, String acquisitionDate, String acquisitionCountry,
               String trainingStatus, boolean reserved, String inServiceCountry) {
        super(name, "Dog", gender, age, weight, acquisitionDate, acquisitionCountry,
              trainingStatus, reserved, inServiceCountry);
        this.breed = breed;
    }

    // Getter and Setter for breed
    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    // Enhanced methods specific to Dog
    public boolean isSuitableForGuardDuty() {
        // Logic for determining if dog is suitable for guard duty based on breed
        String[] guardBreeds = {"German Shepherd", "Rottweiler", "Doberman", "Belgian Malinois"};
        for (String guardBreed : guardBreeds) {
            if (breed.equalsIgnoreCase(guardBreed)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSuitableForTherapy() {
        // Logic for determining if dog is suitable for therapy work
        String[] therapyBreeds = {"Golden Retriever", "Labrador Retriever", "Lab", "Poodle", "Cavalier King Charles Spaniel"};
        for (String therapyBreed : therapyBreeds) {
            if (breed.equalsIgnoreCase(therapyBreed)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("Dog: %s (Breed: %s, %s, %s years, %s, Training: %s, Reserved: %s)", 
                           getName(), breed, getGender(), getAge(), getWeight(), 
                           getTrainingStatus(), getReserved());
    }
}