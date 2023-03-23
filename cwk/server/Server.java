import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.plaf.basic.BasicComboBoxUI.ItemHandler;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class Server {

	//Variables
	private ServerSocket serverSocket = null;
	private int serverPort = 6500;
 	private BufferedWriter logWriter = null;
	HashMap<String, itemInfo> hashMap = new HashMap<String, itemInfo>();


	//To check if port can be used and whether the log file can be created 
    public Server(String fileName) throws IOException{
        try {
			//Creates a new port
            serverSocket = new ServerSocket(serverPort);

			//Creates a log file			
			logWriter = new BufferedWriter(new FileWriter(fileName));

        }
        catch (IOException e) {
			//error message unable to listen to the port
            System.err.println("Could not listen on port: "+ serverPort);
			System.err.println(e);
        }
    }


	public void runServer() {

		Socket clientSocket = null;

			try {

			
				ExecutorService executorService = Executors.newFixedThreadPool(30);
				while(true){

					clientSocket = serverSocket.accept();
				
					
					//Add each new executorservice for the client connection
					// Creates a new thread for the client and adds it to the executor

					ClientHandler clientConnection = new ClientHandler(clientSocket,logWriter,hashMap);
					executorService.submit(clientConnection);
				}
	
			} catch (IOException e) {
				System.err.println("Communication failed.");
			}finally{
				// Close the connection
				// Close the server socket after all connections have been processed
					try {
						serverSocket.close();
					} catch (IOException e) {
						System.err.println("Could not close server socket.");
					}
			}
	}


	//Main to call the server with creating a log.txt file 
	public static void main(String[] args) {
		try{
			Server server = new Server("log.txt");
			server.runServer();
		}catch(IOException e){
			System.err.println("Unable to connect to server" + e.getMessage());
		}
	}
}
 
//class to hold instantiate the values variables that will be stored in the dictionary for the user
class itemInfo {

    private double value;
    private String host;

	
	//Constructor
    public itemInfo(Double value, String host) {
        this.value = value;
        this.host = host;

    }

	//Getters
    public double getValue() {
        return value;
    }

    public String getHost() {
        return host;
    }
}



//Class which handles the client input and processes it accordingly 
class ClientHandler implements Runnable {
	
		Socket clientSocket = null;
		BufferedReader readClient = null;
		BufferedWriter logFile = null;
		BufferedReader fileReader = null;

		HashMap<String, itemInfo> hashMap = new HashMap<String, itemInfo>();
		String item;



		//Constructor
		public ClientHandler(Socket clientSocket, BufferedWriter logFile, HashMap<String, itemInfo> hashMap) throws IOException {
			this.clientSocket = clientSocket;
        	this.readClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			this.logFile = logFile;
			this.hashMap = hashMap;
			
		}

		// Method for accessing the itemInfo class 
		// Returning the value stored in the dictionary
		public itemInfo itemInfo(double value, String host){
			return new itemInfo(value, host);
		}

		// Get the connection from the client and handle the input and output
		@Override
		public void run() {
			//Variable
			String clientInput;

			//Try to open the log.txt file
			try{
				fileReader = new BufferedReader(new FileReader("log.txt"));

			}catch (FileNotFoundException e){
				System.err.println(e.getMessage());
			}

			try{

				//Read the user input
				clientInput = readClient.readLine();
							
				//Check Validation
				if (clientInput.equalsIgnoreCase("show")){

					// System.out.println("Show");
					if (hashMap.size() == 0){
						System.out.println("There are currently no items in this auction.");
					}
					else{
						//Send the entire hashmap back to the client for it to be looped and displayed
						for (Map.Entry<String, itemInfo> row : hashMap.entrySet()){

							//where do we get its corresponding host address from 
							
							System.out.println(row.getKey() + " : " + row.getValue().getValue()+ " : " + row.getValue().getHost());
						}

					}

				}else{
					String[] clientHolder = clientInput.split(" ");
				
					if (clientHolder[0].equalsIgnoreCase("item")){

						//Loop in hashmap to check item does exist
						for (Map.Entry<String, itemInfo> row : hashMap.entrySet()){
							if (row.getKey().equals(item)){
								System.err.println("Failure.");
								break;
							}
						}
						// Save item to hashmap
						hashMap.put(clientHolder[1], itemInfo(0.0,"<no bids>"));
						// Save valid request to log file
						save(clientSocket, clientInput);
						System.out.println("Success.");


					}else if (clientHolder[0].equalsIgnoreCase("bid")){
						
						try{
							//Get item
							if (hashMap.get(clientHolder[1]).getValue() >= Double.parseDouble(clientHolder[2])){
							System.err.println("Rejected.");
							}else{
								
								save(clientSocket, clientInput);
								hashMap.put(clientHolder[1], itemInfo(Double.parseDouble(clientHolder[2]),clientSocket.getInetAddress().getHostAddress().toString()));
								System.err.println("Accepted.");
							}

							System.out.println("Hash Map Value is " + hashMap.get(clientHolder[1]).getValue() + " Client Value is "+Double.parseDouble(clientHolder[2]));
						}catch(NullPointerException e){
							System.err.println("Rejected.");
						}
						// System.out.println("Pssing");
						// System.out.println(hashMap.get(clientHolder[1]));
						// System.out.println(Float.parseFloat(clientHolder[1]));
						

						

						
					}
				}
			


			
				//Show command to print off all items in the auction

			}catch(IOException e){
				System.err.println("Error reading from the client " + e.getMessage());
			}

			System.out.println();
		
		}

		//Function to store every valid request to the log file
		public void save(Socket clientSocket, String clientInput) throws IOException{

			//Save request to log file.
			//format : date|time|client IP address|request

			//Date And Time						

			LocalDate date = LocalDate.now(); 

			LocalTime time = LocalTime.now();

			DateTimeFormatter time_format = DateTimeFormatter.ofPattern("HH:mm:ss");

			// System.out.println(clientHolder);
			// System.err.println(date + "|" + time + "|" + "|" + clientSocket.getInetAddress().getHostAddress() + "|" +clientHolder[-1]);
			logFile.write(date.toString() + "|" + time.format(time_format).toString() + "|" + clientSocket.getInetAddress().getHostAddress().toString()
			+ "|" + clientInput);

			logFile.newLine();

			logFile.flush();

				 
					
		}
}
	

		//Store all items in the auction


		// Accepted bids should also store the IP address of the client



		// Create log file to store the following format, data|time| client IP address | request
		// No header row
	



