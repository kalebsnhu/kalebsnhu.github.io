public class Rabbit extends RescueAnimal {
    private String breed;
    private String furColor;
    private String earType;
    private boolean litterTrained;

    // Default Constructor
    public Rabbit() {
        super();
        setAnimalType("Rabbit");
    }

    // Parameterized Constructor
    public Rabbit(String name, String breed, String furColor, String earType, boolean litterTrained,
                  String gender, String age, String weight, String acquisitionDate, 
                  String acquisitionCountry, String trainingStatus, boolean reserved, 
                  String inServiceCountry) {
        super(name, "Rabbit", gender, age, weight, acquisitionDate, acquisitionCountry,
              trainingStatus, reserved, inServiceCountry);
        this.breed = breed;
        this.furColor = furColor;
        this.earType = earType;
        this.litterTrained = litterTrained;
    }

    // Getter and Setter methods
    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getFurColor() {
        return furColor;
    }

    public void setFurColor(String furColor) {
        this.furColor = furColor;
    }

    public String getEarType() {
        return earType;
    }

    public void setEarType(String earType) {
        this.earType = earType;
    }

    public boolean isLitterTrained() {
        return litterTrained;
    }

    public void setLitterTrained(boolean litterTrained) {
        this.litterTrained = litterTrained;
    }

    // Enhanced methods specific to Rabbit
    public boolean isSuitableForTherapy() {
        // Calm, gentle breeds are better for therapy work
        String[] therapyBreeds = {"Holland Lop", "Mini Rex", "Lionhead", "Dutch", "English Angora"};
        for (String therapyBreed : therapyBreeds) {
            if (breed.equalsIgnoreCase(therapyBreed)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSuitableForEducational() {
        // Friendly, easy-to-handle breeds are better for educational programs
        String[] educationalBreeds = {"New Zealand", "Californian", "Flemish Giant", "Mini Lop"};
        for (String educationalBreed : educationalBreeds) {
            if (breed.equalsIgnoreCase(educationalBreed)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("Rabbit: %s (Breed: %s, Color: %s, Ears: %s, %s, %s years, %s, Training: %s, Reserved: %s)", 
                           getName(), breed, furColor, earType, getGender(), getAge(), getWeight(), 
                           getTrainingStatus(), getReserved());
    }
}