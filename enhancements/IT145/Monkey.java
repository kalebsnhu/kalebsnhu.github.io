// Kaleb Ward
// 10-16-22

public class Monkey extends RescueAnimal { // create Monkey class that extends from RescueAnimal class
	private String tailLength, height, bodyLength, species;
	
	public Monkey(String name, String gender, String age, // Create a default constructor with the provided attributes
		    String weight, String species, String tailLength, String height, 
		    String bodyLength, String acquisitionDate, String acquisitionCountry,
			String trainingStatus, boolean reserved, String inServiceCountry) {
		// We can set all of the attributes with the methods we have made previously.
        setName(name);
        setAnimalType("Monkey");
        setGender(gender);
        setAge(age);
        setWeight(weight);
        setAcquisitionDate(acquisitionDate);
        setAcquisitionLocation(acquisitionCountry);
        setTrainingStatus(trainingStatus);
        setReserved(reserved);
        setInServiceCountry(inServiceCountry);
        this.tailLength = tailLength;
        this.height = height;
        this.bodyLength = bodyLength;
        this.species = species;
	}

    // Method: getSpecies
    // Returns the monkey's species

	public String getSpecies() { 
		return this.species;
	}

	// Method: setSpecies
    // Arguments: (species) : Sets the monkey's species

	public void setSpecies(String species) {
		this.species = species;
	}
	
    // Method: getTailLength
    // Returns the monkey's tail length

	public String getTailLength() {
		return this.tailLength;
	}
	
	// Method: setTailLength
    // Arguments: (tailLength) : Sets the monkey's tail length

	public void setTailLength(String tailLength) {
		this.tailLength = tailLength;
	}
	
    // Method: getHeight
    // Returns the monkey's height

	public String getHeight() {
		return this.height;
	}
	
	// Method: setTailLength
    // Arguments: (height) : Sets the monkey's height

	public void setHeight(String height) {
		this.height = height;
	}
	
    // Method: getBodyLength
    // Returns the monkey's body length

	public String getBodyLength() {
		return this.bodyLength;
	}

	// Method: setBodyLength
    // Arguments: (bodyLength) : Sets the monkey's body length

	public void setBodyLength(String bodyLength) {
		this.bodyLength = bodyLength;
	}
}
