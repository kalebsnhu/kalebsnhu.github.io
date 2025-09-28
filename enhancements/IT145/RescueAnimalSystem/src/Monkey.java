public class Monkey extends RescueAnimal {
    private String species;
    private String tailLength;
    private String height;
    private String bodyLength;

    // Default Constructor
    public Monkey() {
        super();
        setAnimalType("Monkey");
    }

    // Parameterized Constructor
    public Monkey(String name, String gender, String age, String weight, String species,
                  String tailLength, String height, String bodyLength, String acquisitionDate,
                  String acquisitionCountry, String trainingStatus, boolean reserved,
                  String inServiceCountry) {
        super(name, "Monkey", gender, age, weight, acquisitionDate, acquisitionCountry,
              trainingStatus, reserved, inServiceCountry);
        this.species = species;
        this.tailLength = tailLength;
        this.height = height;
        this.bodyLength = bodyLength;
    }

    // Getter and Setter methods
    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getTailLength() {
        return tailLength;
    }

    public void setTailLength(String tailLength) {
        this.tailLength = tailLength;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getBodyLength() {
        return bodyLength;
    }

    public void setBodyLength(String bodyLength) {
        this.bodyLength = bodyLength;
    }

    // Enhanced methods specific to Monkey
    public boolean isSuitableForFineMotorTasks() {
        // Smaller monkeys with dexterous hands are better for fine motor tasks
        String[] fineMotorSpecies = {"capuchin", "marmoset", "tamarin"};
        for (String fineSpecies : fineMotorSpecies) {
            if (species.equalsIgnoreCase(fineSpecies)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSuitableForMobilityAssistance() {
        // Larger, stronger monkeys are better for mobility assistance
        String[] mobilitySpecies = {"macaque", "guenon"};
        for (String mobilityType : mobilitySpecies) {
            if (species.equalsIgnoreCase(mobilityType)) {
                return true;
            }
        }
        return false;
    }

    public double calculateBMI() {
        try {
            double weightKg = Double.parseDouble(getWeight());
            double heightM = Double.parseDouble(height) / 100; // Convert cm to meters
            return weightKg / (heightM * heightM);
        } catch (NumberFormatException e) {
            return 0.0; // Return 0 if weight or height is not numeric
        }
    }

    @Override
    public String toString() {
        return String.format("Monkey: %s (Species: %s, %s, %s years, %s, Training: %s, Reserved: %s)", 
                           getName(), species, getGender(), getAge(), getWeight(), 
                           getTrainingStatus(), getReserved());
    }
}