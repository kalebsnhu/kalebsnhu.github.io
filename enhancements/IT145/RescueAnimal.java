// Kaleb Ward
// 10-16-22

import java.lang.String;

public class RescueAnimal { // create class RescueAnimal

    // Instance variables
    private String name;
    private String animalType;
    private String gender;
    private String age;
    private String weight;
    private String acquisitionDate;
    private String acquisitionCountry;
	private String trainingStatus;
    private boolean reserved;
	private String inServiceCountry;


    // Constructor
    public RescueAnimal() {
    }


	// Method: getName
    // Returns the animal's name

	public String getName() {
		return name;
	}

	// Method: setName
    // Argument: (name) : sets the animal's name

	public void setName(String name) {
		this.name = name;
	}

	// Method: getAnimalType
    // Returns the animal's type

	public String getAnimalType() {
		return animalType;
	}

	// Method: setAnimalType
    // Argument: (animalType) : sets the animal's type

	public void setAnimalType(String animalType) {
		this.animalType = animalType;
	}

	// Method: getGender
    // Returns the animal's gender

	public String getGender() {
		return gender;
	}

	// Method: setGender
    // Argument: (gender) : sets the animal's gender

	public void setGender(String gender) {
		this.gender = gender;
	}

	// Method: getAge
    // Returns the animal's age

	public String getAge() {
		return age;
	}

	// Method: setAge
    // Argument: (age) : sets the animal's age

	public void setAge(String age) {
		this.age = age;
	}

	// Method: getWeight
    // Returns the animal's weight

	public String getWeight() {
		return weight;
	}

	// Method: setWeight
    // Argument: (weight) : sets the animal's weight

	public void setWeight(String weight) {
		this.weight = weight;
	}

	// Method: getAcquisitionDate
    // Returns the animal's acquisition date

	public String getAcquisitionDate() {
		return acquisitionDate;
	}

	// Method: setWeight
    // Argument: (acquisitionDate) : sets the animal's acquisition date

	public void setAcquisitionDate(String acquisitionDate) {
		this.acquisitionDate = acquisitionDate;
	}

	// Method: getAcquisitionLocation
    // Returns the animal's acquisition location

	public String getAcquisitionLocation() {
		return acquisitionCountry;
	}

	// Method: setAcquisitionLocation
    // Argument: (acquisitionCountry) : sets the animal's acquisition location

	public void setAcquisitionLocation(String acquisitionCountry) {
		this.acquisitionCountry = acquisitionCountry;
	}

	// Method: getReserved
    // Returns the animal's reserve status

	public boolean getReserved() {
		return reserved;
	}

	// Method: setReserved
    // Argument: (reserved) : sets the animal's reserve status

	public void setReserved(boolean reserved) {
		this.reserved = reserved;
	}

	// Method: getInServiceLocation
    // Returns the animal's in-service location

	public String getInServiceLocation() {
		return inServiceCountry;
	}

	// Method: setInServiceCountry
    // Argument: (inServiceCountry) : sets the animal's in-service country

	public void setInServiceCountry(String inServiceCountry) {
		this.inServiceCountry = inServiceCountry;
	}

	// Method: getTrainingStatus
    // Returns the animal's training status

	public String getTrainingStatus() {
		return trainingStatus;
	}

	// Method: setTrainingStatus
    // Argument: (trainingStatus) : sets the animal's training status

	public void setTrainingStatus(String trainingStatus) {
		this.trainingStatus = trainingStatus;
	}
}
