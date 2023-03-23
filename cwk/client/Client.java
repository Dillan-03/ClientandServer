import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Client {
	private Socket s;
	private static PrintWriter clientOut = null;
	private static BufferedReader clientIn = null;
	private int clientPort = 6503;

	public Client(){
		
	}

	public void connect() throws IOException{
       
            // Try to open up a socket with this host ('localhost'), port number.
			try{
				s = new Socket( "localhost", clientPort);

				// System.out.println("Connection established.. processing now ");

				//Create a chain for reading and writing back to the user
				clientOut = new PrintWriter(s.getOutputStream(), true);
				clientIn = new BufferedReader(new InputStreamReader(s.getInputStream()));	

				
			}catch (IOException e){
				System.out.println("Unable to connect to server.");
			}
		
    }

	public static void main( String[] args ){
		
		// Three Commands arguments: show, item, bid
		// Invalid argument -> quit with meaningful error message
		try{
			Client client = new Client();

			// show : displays a table containing all items in the auction with column names (item name, current bid, IP address of client
			// System.out.println(args.length);
			if (args.length == 0){
				System.err.println("Usage: java show");
			} else if (args.length == 1){
				if (args[0].equalsIgnoreCase("show")){
				
					client.connect();
					clientOut.println("show");
					String response = clientIn.readLine();
					
					
					if (response.startsWith("[") && response.endsWith("]")) {
						System.out.println("myString is an array");
					}
					// System.out.println(response.startsWith("["));

					// System.out.println(response.endsWith("]"));


				}else{
					System.err.println("Usage: java show");		

				}
		
			// item : adds a new item to the auction with a bid price of zero (no bids has been made)
			}else if (args.length == 2){

			 	if (args[0].equalsIgnoreCase("item")){		

					client.connect();
					clientOut.println("item " + args[1]);
					System.out.println("Server response: " + clientIn.readLine());

				}else{

					System.err.println("Usage: java item <string>");		
				}
			
			// bid : attempts to to make a bid of value for the item
			}else if (args.length == 3){
				if (args[0].equalsIgnoreCase("bid")){
				
					client.connect();
					clientOut.println("bid " + args[1] + " " + args[2]);
					System.out.println(clientIn.readLine());
					// }

				}else{
					System.err.println("Usage: java bid <item> <value>");
				}
			}else if (args.length == 4){
				System.err.println("Usage: java bid <item> <value>");
			}
			
		}
	
	
		catch(IOException e ){
			//Error Message
            System.out.println( "Unable to connect to the server." );
        }

		
	}


		

		

		
}
