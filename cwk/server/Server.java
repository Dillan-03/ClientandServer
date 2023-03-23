import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.plaf.basic.BasicComboBoxUI.ItemHandler;

import java.time.*;
import java.time.format.DateTimeFormatter;

// DONE 
//need to make sure that usage message is displayed for no arguments
//same for bid you need validation for number of args, if > 4 you need message

// TO DO
//then make sure you check in the hashmap that the item you are trying to add 
// exists in the hashmap if so create a message

public class Server {

	//Variables
	private ServerSocket serverSocket = null;
	private int serverPort = 6503;
 	private BufferedWriter logWriter = null;
	HashMap<String, itemInfo> hashMap = new HashMap<String, itemInfo>();
	private BufferedWriter toClient = null;

	

	
	//To check if port can be used and whether the log file can be created 
    public Server(String fileName) throws IOException{
        try {
			//Creates a new port
            serverSocket = new ServerSocket(serverPort);

			//Creates a log file			
			logWriter = new BufferedWriter(new FileWriter(fileName));

		}
        catch (IOException e) {
			//error message unalbe to listen to the port
            System.err.println("Could not listen on port: "+ serverPort);
			System.err.println(e);
			// System.err.println(e.getMessage());

        }

    }

	public void runServer() {

		Socket clientSocket = null;

			try {
				ExecutorService executorService = Executors.newFixedThreadPool(30);
				while(true){

					clientSocket = serverSocket.accept();

					InetAddress inet = clientSocket.getInetAddress();
				
					//Add each new executorservice for the client connection
					// Creates a new thread for the client and adds it to the executor
					//HashMap to hold the item value

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


	public static void main(String[] args) {
		try{
			Server server = new Server("log.txt");
			server.runServer();
		}catch(IOException e){
			System.err.println("Unable to connect to server" + e.getMessage());
		}
	}

}
 
class itemInfo {
    private double value;
    private String host;

    public itemInfo(Double value, String host) {

        this.value = value;
        this.host = host;
        
    }

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
		BufferedWriter toClient = null;
		PrintWriter printClient = null;

		//Dynamic Array
		ArrayList<String> showItems = new ArrayList<String>();



		HashMap<String, itemInfo> hashMap = new HashMap<String, itemInfo>();
		String item;


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
			//Variable
			String clientInput;
			Boolean found = false;
			String readLine = null;

			try{
				fileReader = new BufferedReader(new FileReader("log.txt"));

			}catch (FileNotFoundException e){
				printClient.println(e.getMessage());
			}

			// System.out.println("Show");

			try{

				// toClient = new PrintWriter(clientSocket.getOutputStream(),true);
				clientInput = readClient.readLine();

				// System.out.println("Received from client: " + clientInput);
			
				//Check Validation
				if (clientInput.equalsIgnoreCase("show")){

					// System.out.println("Show");
					if (hashMap.size() == 0){
						printClient.println("There are currently no items in this auction.");
					}
					else{
						//Send the entire hashmap back to the client for it to be displayed
						for (Map.Entry<String, itemInfo> row : hashMap.entrySet()){

							showItems.add(row.getKey() + " : " + row.getValue().getValue()+ " : " + row.getValue().getHost());
						}
						//Convert ArrayList to an array 
						String[] output = showItems.toArray(new String[showItems.size()]);
						String returnItems = Arrays.toString(output);
						printClient.println(returnItems);

					}

				}else{
					String[] clientHolder = clientInput.split(" ");
					item = clientHolder[1];
				
					if (clientHolder[0].equalsIgnoreCase("item")){

						//Variable to check if an item exists
						Boolean exists = false;

						//Loop in hashmap to check item does exist
						for (Map.Entry<String, itemInfo> row : hashMap.entrySet()){
							// printClient.println(row.getKey().toString());
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

					}else if (clientHolder[0].equalsIgnoreCase("bid")){
						
						try{
							//Get item
							if (hashMap.get(clientHolder[1]).getValue() >= Double.parseDouble(clientHolder[2])){
								printClient.println("Rejected.");
							}else{
								
								save(clientSocket, clientInput);
								hashMap.put(clientHolder[1], itemInfo(Double.parseDouble(clientHolder[2]),clientSocket.getInetAddress().getHostAddress().toString()));
								printClient.println("Accepted.");
							}

							// System.out.println("Hash Map Value is " + hashMap.get(clientHolder[1]).getValue() + " Client Value is "+Double.parseDouble(clientHolder[2]));
						}catch(NullPointerException e){
							printClient.println("Rejected.");
						}

					}
				}
			


			
				//Show command to print off all items in the auction

			}catch(IOException e){
				printClient.println("Error reading from the client " + e.getMessage());
			}

			// System.out.println();
		
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

	




