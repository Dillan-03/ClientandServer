import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.*;
import java.time.format.DateTimeFormatter;


public class Server {

	//Variables
	private ServerSocket serverSocket = null;
	private int serverPort = 6503;
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

				//Creates a thread pool size of 30
				ExecutorService executorService = Executors.newFixedThreadPool(30);

				while(true){

					clientSocket = serverSocket.accept();
				
					//Add each new executor service for the client connection
					// Creates a new thread for the client and adds it to the executor
					ClientHandler clientConnection = new ClientHandler(clientSocket,logWriter,hashMap);
					executorService.submit(clientConnection);

				}

			} catch (IOException e) {
				System.err.println("Communication failed.");
				System.err.println(e);
		
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


	//Calls the server to try to make a connection to the client
	public static void main(String[] args) {

	
		try{
			Server server = new Server("log.txt");
			server.runServer();
		}catch(IOException e){
			System.err.println("Unable to connect to server" + e.getMessage());
		}

	}

}


//class to hold the information (value, hostAddress) in a contained class
class itemInfo {
    private double value;
    private String host;

	//Constructors 
    public itemInfo(Double value, String host) {

        this.value = value;
        this.host = host;
        
    }

	//Getters returning the current bid
    public double getValue() {
        return value;
    }

	//Getters returning the host 

    public String getHost() {
        return host;
    }
}



//Class which handles the client input and processes it accordingly 
class ClientHandler implements Runnable {

		//Variables which will be later used
		Socket clientSocket = null;
		BufferedReader readClient = null;
		BufferedWriter logFile = null;
		BufferedReader fileReader = null;
		BufferedWriter toClient = null;
		PrintWriter printClient = null;		
		String item;

		//Dynamic Array
		ArrayList<String> showItems = new ArrayList<String>();

		//HashMap to store the valid user request
		HashMap<String, itemInfo> hashMap = new HashMap<String, itemInfo>();

		//Constructor
		//parameters are sent from the client class (line 56)
		public ClientHandler(Socket clientSocket, BufferedWriter logFile, HashMap<String, itemInfo> hashMap) throws IOException {
			this.clientSocket = clientSocket;
        	this.readClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			this.logFile = logFile;
			this.hashMap = hashMap;
			this.printClient = new PrintWriter(clientSocket.getOutputStream(),true);
		}

		// Method for accessing the itemInfo class
		public itemInfo itemInfo(double value, String host){
			return new itemInfo(value, host);
		}

		// Get the connection from the client and handle the input and output
		@Override
		public void run() {

			//Variables
			String clientInput;

			//Tries to read the text file
			try{
				fileReader = new BufferedReader(new FileReader("log.txt"));
			}catch (FileNotFoundException e){
				printClient.println(e.getMessage());
			}

			try{
				//Receives client input  
				clientInput = readClient.readLine();

				//Check Validation
				//Show Validation
				if (clientInput.equalsIgnoreCase("show")){

					//No items in the hashmap
					if (hashMap.size() == 0){
						printClient.println("There are currently no items in this auction.");
					}else{

						//Send the entire hashmap back to the client for it to be displayed
						for (Map.Entry<String, itemInfo> row : hashMap.entrySet()){

							showItems.add(row.getKey() + " : " + row.getValue().getValue()+ " : " + row.getValue().getHost());
						}

						//Convert ArrayList to an array 
						String[] output = showItems.toArray(new String[showItems.size()]);
						String returnItems = Arrays.toString(output);

						//Returns the arrays back to the client to be further processed
						printClient.println(returnItems);

					}
				
				//item Validation
				}else{

					String[] clientHolder = clientInput.split(" ");
					item = clientHolder[1];
				
					if (clientHolder[0].equalsIgnoreCase("item")){

						//Variable to check if an item exists
						Boolean exists = false;

						//Loop in hashmap to check item does exist
						for (Map.Entry<String, itemInfo> row : hashMap.entrySet()){

							if (row.getKey().equals(item)){
								exists = true;
							}

						}

						if (exists == true){
							printClient.println("Failure.");
						}else{
							// Save item to hashmap
							hashMap.put(clientHolder[1], itemInfo(0.0,"<no bids>"));
							// Save valid request to log file
							save(clientSocket, clientInput);
							printClient.println("Success.");
						}

					//Bid Validation
					}else if (clientHolder[0].equalsIgnoreCase("bid")){
						
						try{
							//Get item
							//Compare bid values 
							if (hashMap.get(clientHolder[1]).getValue() >= Double.parseDouble(clientHolder[2])){
								printClient.println("Rejected.");
							}else{
								//Client bid is larger 
								save(clientSocket, clientInput);
								hashMap.put(clientHolder[1], itemInfo(Double.parseDouble(clientHolder[2]),clientSocket.getInetAddress().getHostAddress().toString()));
								printClient.println("Accepted.");
							}

						}catch(NullPointerException e){
							printClient.println("Rejected.");
						}

					}
				}
			
			}catch(IOException e){
				printClient.println("Error reading from the client " + e.getMessage());
			}

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
			// printClient.println(date + "|" + time + "|" + "|" + clientSocket.getInetAddress().getHostAddress() + "|" +clientHolder[-1]);
			logFile.write(date.toString() + "|" + time.format(time_format).toString() + "|" + clientSocket.getInetAddress().getHostAddress().toString()
			+ "|" + clientInput);

			logFile.newLine();

			logFile.flush();

		}
}

	




