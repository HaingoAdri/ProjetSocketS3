package client;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Server {

	// The server socket.
	private static ServerSocket serverSocket = null;
	// The client socket.
	private static Socket clientSocket = null;

	public static ArrayList<clientThread> clients = new ArrayList<clientThread>();

	public static void main(String args[]) {

		//  port number.
		int portNumber = 1234;


		if (args.length < 1) 
		{

			System.out.println("Tsisy port foronon'i user.\nDonc port par defaut=" + portNumber);

		} 
		else 
		{
			portNumber = Integer.valueOf(args[0]).intValue();

			System.out.println("Connexion du server par le port=" + portNumber);
		}

		//manokatra socket aminy port 1234
		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			System.out.println("Server Socket tsy afaka creena");
		}

		
		//creation d'un client socket a chaque connexion et le convertir en clientThread
		int clientNum = 1;
		while (true) {
			try {

				clientSocket = serverSocket.accept();
				clientThread curr_client =  new clientThread(clientSocket, clients);
				clients.add(curr_client);
				curr_client.start();
				System.out.println("Client "  + clientNum + "Tafiditra!");
				clientNum++;

			} catch (IOException e) {

				System.out.println("Client tsy mety Tafiditra!!");
			}


		}

	}
}




