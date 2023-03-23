// This file is used to handle input from the clients and be processed to be sent to the server
public class Protocol 
{
    
    // Variables to differentiate the user value
    // Default case is 0

    // State variable to hold which command is entered
    private static int state = 0;
    private static int show = 0;
    private static int bid= 0 ;
    private static int quit = 0;
    private static int item = 0;

    
    //Assigning base values to the variables
    public Protocol()
    {
        show = 0;
        bid = 1;
        quit = 2;
        item = 3;
    }

    //Checking the user input
    public String processuserInput(String userInput){

    
    // String to send back to the server 
        String output = null;

        // if (state == state) {
           
        // } else if (state == SENTKNOCKKNOCK) {
            
        // } else if (state == SENTCLUE) {
            
        // } else if (state == ANOTHER) {
           
        // }
        
        return "";
    
    }
    public static void main(String[] args) 
	{
		
	} 
}
