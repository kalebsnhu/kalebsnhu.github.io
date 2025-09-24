// Kaleb Ward
// 10-16-22

public class Dog extends RescueAnimal { // create Dog class that extends from RescueAnimal class

    // Instance variable
    private String breed;

    // Constructor
    public Dog(String name, String breed, String gender, String age,
    String weight, String acquisitionDate, String acquisitionCountry,
	String trainingStatus, boolean reserved, String inServiceCountry) {
        setName(name);
        setBreed(breed);
        setGender(gender);
        setAge(age);
        setWeight(weight);
        setAcquisitionDate(acquisitionDate);
        setAcquisitionLocation(acquisitionCountry);
        setTrainingStatus(trainingStatus);
        setReserved(reserved);
        setInServiceCountry(inServiceCountry);

    }

    // Method: getBreed
    // Returns the dog's breed

    public String getBreed() {
        return breed;
    }

    // Method: getBreed
    // Argument: (dogBreed) : sets the dog's breed

    public void setBreed(String dogBreed) {
        breed = dogBreed;
    }

}
