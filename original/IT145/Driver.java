// Kaleb Ward
// 10-16-22

import java.util.ArrayList; // import ArrayList library so we can create ArrayLists containing dogs/monkeys/validmonkeyspecies
import java.util.Arrays; // import Arrays library so I can create a arraylist containing the valid monkey species
import java.util.Scanner; // import the Scanner Library so we can receive the user's input.

public class Driver {
    private static ArrayList<Dog> dogList = new ArrayList<Dog>();
    private static ArrayList<Monkey> monkeyList = new ArrayList<Monkey>();
    private static ArrayList<String> validMonkeySpecies = new ArrayList<>(Arrays.asList("capuchin", "guenon", "macaque", "marmoset", "squirrel monkey", "tamarin"));
    
    public static void main(String[] args) {


        initializeDogList(); // Initilize DogList
        initializeMonkeyList(); // Initilize MonkeyList

        Scanner input = new Scanner(System.in);
     
        while( true ) { // Create a for loop for the main menu.
        	displayMenu(); // Display default options with the method provided.
        	String userChoice = input.next();
        	input.nextLine();
        	
        	if( userChoice.equals("1") ) // Create if statements so we can get the user's result and compare it to the menu.
        		intakeNewDog(input);
        	else if( userChoice.equals("2") )
        		intakeNewMonkey(input);
        	else if( userChoice.equals("3") )
        		reserveAnimal(input);
        	else if( userChoice.equals("4") )
        		printAnimals("dog");
        	else if( userChoice.equals("5") )
        		printAnimals("monkey");
        	else if( userChoice.equals("6") )
        		printAnimals("available");
        	else if( userChoice.equalsIgnoreCase("q") )
        		System.exit(0);
        	else
        		System.out.println("Wrong Choice");
        }
    }

    // This method prints the menu options
    public static void displayMenu() {
        System.out.println("\n\n");
        System.out.println("\t\t\t\tRescue Animal System Menu");
        System.out.println("[1] Intake a new dog");
        System.out.println("[2] Intake a new monkey");
        System.out.println("[3] Reserve an animal");
        System.out.println("[4] Print a list of all dogs");
        System.out.println("[5] Print a list of all monkeys");
        System.out.println("[6] Print a list of all animals that are not reserved");
        System.out.println("[q] Quit application");
        System.out.println();
        System.out.println("Enter a menu selection");
    }


    // Adds dogs to a list for testing
    public static void initializeDogList() { // Create a DogList so we can verify if the application is working correctly. (3 dogs)
        Dog dog1 = new Dog("Charlie", "German Shepherd", "male", "1", "110", "09-10-2017", "United States", "intake", false, "United States");
        Dog dog2 = new Dog("Popcorn", "Lab", "male", "3", "55", "10-01-2018", "United States", "Phase I", true, "United States");
        Dog dog3 = new Dog("Pumpkin", "Great Dane", "male", "1", "130", "03-20-2021", "United States", "intake", false, "United States");
        
        dogList.add(dog1);
        dogList.add(dog2);
        dogList.add(dog3);
    }


    // Adds monkeys to a list for testing
    public static void initializeMonkeyList() { // Create a MonkeyList so we can verify if the application is working correctly. (2 monkeys)
        Monkey monkey1 = new Monkey("Monkerz","male","5", "110", "Mandril", "1.5", "7", "6", "12-02-2022", "United States", "in service", true, "United states");
        Monkey monkey2 = new Monkey("George","male","5", "75", "Marmoset", "3", "5", "5.2", "11-02-2019", "United States", "in service", false, "United states");
        monkeyList.add(monkey1);
        monkeyList.add(monkey2);
    }

    // Method: intakeNewDog
    // Arguments: (scanner) : allows us to receive the user's input when we first intake the new dog.

    public static void intakeNewDog(Scanner scanner) { // Create intake
        System.out.println("What is the dog's name?");
        String name = scanner.nextLine();
        
        for( Dog dog : dogList ) { // create a for loop looping through dogList so we can verify the animal's name is not already in the system.
            if( dog.getName().equalsIgnoreCase(name) ) {
                System.out.println("\n\nThis dog is already in our system\n\n");
                return; //returns to menu
            }
        }

        System.out.print("Breed: "); // Receive the animal's breed
        String breed = scanner.nextLine();
        
        System.out.print("Gender (male/female): "); // Receive the animal's gender
        String gender = scanner.nextLine();
        
        System.out.print("Age (in years): "); // Receive the animal's age
        String age = scanner.nextLine();
        
        System.out.print("Weight: ");  // Receive the animal's weight
        String weight = scanner.nextLine();
        
        System.out.print("Acquisition date: ");  // Receive the animal's acquisition date
        String acqDate = scanner.nextLine();
        
        System.out.print("Acquisition country: ");  // Receive the animal's acquisition country
        String country1 = scanner.nextLine();
        
        System.out.print("Training status: ");  // Receive the animal's training status
        String status = scanner.nextLine();
        
        System.out.print("Reserved (1 for true, 0 for false): "); // Receive the animal's reserve status
        String choice = scanner.nextLine();

        boolean reserveBool; // Create reserveBool so we can change the user's choice into a boolean
        if( choice.equals("1") )
        	reserveBool = true;
        else
        	reserveBool = false;
        
        System.out.println("In service country: ");
        String inServiceCountry = scanner.nextLine();
        
        Dog newDog = new Dog(name, breed, gender, age, weight, acqDate, country1, status, reserveBool, inServiceCountry); // Create new dog with the variables created above.
        dogList.add(newDog);
    }


    // Method: intakeNewMonkey
    // Arguments: (scanner) : allows us to receive the user's input when we first intake the new monkey.

        public static void intakeNewMonkey(Scanner scanner) {
            System.out.print("Name: ");
            String name= scanner.nextLine();
            
            for( Monkey monkey : monkeyList ) 
            {
           
                if( monkey.getName().equalsIgnoreCase(name) ) // create a for loop looping through dogList so we can verify the animal's name is not already in the system.
                {
                    System.out.println("\n\nThis Monkey is already in our system\n\n");
                    return; //returns to menu
                }
            }
            
            System.out.print("Gender: "); // Receive the animal's gender
            String gender = scanner.nextLine();
            
            System.out.print("Age: "); // Receive the animal's age
            String age = scanner.nextLine();
            
            System.out.print("Weight: "); // Receive the animal's weight
            String weight = scanner.nextLine();
            
            System.out.print("Species: "); // Receive the animal's species
            String species = scanner.nextLine();
            
            boolean speciesValid = validMonkeySpecies.contains(species.toLowerCase()); // create a boolean verifying if the species is in the validMonkeySpecies array created above
            if( !speciesValid ) {
                System.out.println("\n\nThat species is not valid\n\n");
                return;
            }
            
            System.out.print("Tail length: "); // Receive the animal's tail length
            String tailLength = scanner.nextLine();
            
            System.out.print("Height: "); // Receive the animal's height
            String height = scanner.nextLine();
            
            System.out.print("Body Length: "); // Receive the animal's body length
            String bodyLength = scanner.nextLine();
            
            System.out.print("Acquisition date: "); // Receive the animal's acquistion date
            String acqDate = scanner.nextLine();
            
            System.out.print("Acquisition country: "); // Receive the animal's acquisition country
            String country1 = scanner.nextLine();
            
            System.out.print("Training status: "); // Receive the animal's training status
            String status = scanner.nextLine();
            
            System.out.print("Reserved (1 for true, 0 for false): "); // Receive the animal's reserve status
            String choice = scanner.nextLine();

            boolean reserveBool; // Create reserveBool so we can change the user's choice into a boolean
            if( choice.equals("1") )
            	reserveBool = true;
            else
            	reserveBool = false;
            
            System.out.println("In service country: "); // Receive the animal's in service country
            String inServiceCountry = scanner.nextLine();
            
            Monkey newMonkey = new Monkey(name, gender, age, weight, species, tailLength, height, bodyLength, acqDate, country1, status, reserveBool, inServiceCountry); // Create new monkey with the variables created above.
            monkeyList.add(newMonkey);
        }

        // Method: reserveAnimal
        // Arguments: (scanner) : allows us to receive the user's input when we first intake the new reserveAnimal.

        public static void reserveAnimal(Scanner scanner) {
            System.out.println("Dog or Monkey: "); // Receive if the animal is a dog or a monkey
            String choiceAnimal = scanner.nextLine();
            
            System.out.println("Service country: ");
            String serviceCountry = scanner.nextLine();
            
            if( choiceAnimal.equalsIgnoreCase("dog") || choiceAnimal.equalsIgnoreCase("monkey") ){  // Create an if statement verifying if the choice is a monkey or a dog
                int hasReserve = 0; // Create an integer so we can use it later to verify if there is a reserve animal
 
                if( choiceAnimal.equalsIgnoreCase("dog") ) { // if the choice is a dog then we can loop through the dogList array and receive dogs
                    for( Dog dog : dogList )
                    {
                        if( dog.getInServiceLocation().equalsIgnoreCase(serviceCountry) )
                        {
                            if( !dog.getReserved() ) // if the dog is not a reserve then we can set it to reserve
                            {
                                dog.setReserved(true);
                                System.out.println("Dog " + dog.getName() + " is now reserved");
                                hasReserve = 1;
                            }
                        }
                    }
                }
                else {
                    for( Monkey monkey : monkeyList ) // the choice is not a dog so it has to be a monkey, we can loop through monkeyList and receive monkeys.
                    {
                        if( monkey.getInServiceLocation().equalsIgnoreCase(serviceCountry) )
                        {
                            if( !monkey.getReserved() ) // if the monkey is not a reserve then we can set it to reserve.
                            {
                                monkey.setReserved(true);
                                System.out.println("Monkey " + monkey.getName() + " is now reserved");
                                hasReserve = 1;
                            }
                        }
                    }
                }
                
                if( hasReserve == 0 ) // if not reserve animals were found in the loops above then we do not have a reserve animal
                    System.out.println("No " + choiceAnimal + " found to reserve");
            }
     
            else{ // if all else fails, the animal type was invalid.
                System.out.println("Wrong animal type");
            }
        }

        // Method: printAnimals
        // Arguments: (listType) : allows us to printanimals with the provided listtype (dog/monkey/available)
    
        public static void printAnimals(String listType) 
        {
            if( listType.equalsIgnoreCase("dog") ) // if the listType equals dog then we can print all of the valid dogs
            {
                for( Dog dog : dogList ) // loop through the dogList and print all available attributes
                {
                    System.out.println("Dog " + dog.getName() + "\nGender: " + dog.getGender() 
                            + "\nAge: " + dog.getAge() + "\nWeight: " + dog.getWeight()
                            + "\nAcquisition Date: " + dog.getAcquisitionDate() 
                            + "\nAcquisition Country: " + dog.getAcquisitionLocation() 
                            + "\nReserved: " + dog.getReserved() + "\nTraining Status: " 
                            + dog.getTrainingStatus() + "\nService country: " + dog.getInServiceLocation() + "\n");
                }
            }
            else if( listType.equalsIgnoreCase("monkey") )
            {
                for( Monkey monkey : monkeyList ) // loop through the monkeyList and print all available attributes
                {
                    System.out.println("Monkey " + monkey.getName() + "\nGender: " + monkey.getGender() 
                            + "\nAge: " + monkey.getAge() + "\nWeight: " + monkey.getWeight()
                            + "\nSpecies: " + monkey.getSpecies() + "\nTail Length: " + monkey.getTailLength()
                            + "\nHeight: "+ monkey.getHeight() + "\nBody Length: " + monkey.getBodyLength()
                            + "\nAcquisition Date: " + monkey.getAcquisitionDate() 
                            + "\nAcquisition Country: " + monkey.getAcquisitionLocation() 
                            + "\nReserved: " + monkey.getReserved() + "\nTraining Status: " 
                            + monkey.getTrainingStatus() + "\nService country: " + monkey.getInServiceLocation() + "\n");
                }
            }
            else if( listType.equalsIgnoreCase("available") ) // because it wants to print available we will print all of the available animals.
            {
                for( Dog dog : dogList ) // loop through the dogList and print all available attributes
                {
                    if( dog.getTrainingStatus().equalsIgnoreCase("in service") && (!dog.getReserved()) )
                    {
                        System.out.println("Dog: " + dog.getName() + "\nGender: " + dog.getGender() 
                            + "\nAge: " + dog.getAge() + "\nWeight: " + dog.getWeight()
                            + "\nAcquisition Date: " + dog.getAcquisitionDate() 
                            + "\nAcquisition Country: " + dog.getAcquisitionLocation() 
                            + "\nReserved: " + dog.getReserved() + "\nTraining Status: " 
                            + dog.getTrainingStatus() + "\nService country: " + dog.getInServiceLocation() + "\n");
                    }
                }
                
                for( Monkey monkey : monkeyList ) // loop through the monkeyList and print all available attributes
                {
                    if( monkey.getTrainingStatus().equalsIgnoreCase("in service") && ( !monkey.getReserved() ) )
                    {
                        System.out.println("Monkey: " + monkey.getName() + "\nGender: " + monkey.getGender() 
                            + "\nAge: " + monkey.getAge() + "\nWeight: " + monkey.getWeight()
                            + "\nSpecies: " + monkey.getSpecies() + "\nTail Length: " + monkey.getTailLength()
                            + "\nHeight: "+ monkey.getHeight() + "\nBody Length: " + monkey.getBodyLength()
                            + "\nAcquisition Date: " + monkey.getAcquisitionDate() 
                            + "\nAcquisition Country: " + monkey.getAcquisitionLocation() 
                            + "\nReserved: " + monkey.getReserved() + "\nTraining Status: " 
                            + monkey.getTrainingStatus() + "\nService country: " + monkey.getInServiceLocation() + "\n");
                    }
                }
            }
            else
            {
                System.out.println("Wrong list type"); // not a valid list type 
            }
        }
}

