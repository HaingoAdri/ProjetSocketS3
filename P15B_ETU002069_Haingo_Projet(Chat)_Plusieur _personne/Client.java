package client;

import java.io.File;
import java.io.*;
import java.util.*;
import java.net.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Client implements Runnable {

	private static Socket clientSocket = null;
	private static ObjectOutputStream os = null;
	private static ObjectInputStream is = null;
	private static BufferedReader inputLine = null;
	private static BufferedInputStream bis = null;
	private static boolean closed = false;

	public static void main(String[] args) {

		//port par defaut
		int portNumber = 1234;
		//host
		// String host = "172.16.1.65";
		String host = "localhost";

		if (args.length < 2) {
			System.out.println("Server: " + host + ", Port par defaut: " + portNumber);
		} else {
			host = args[0];
			portNumber = Integer.valueOf(args[1]).intValue();
			System.out.println("Server: " + host + ", Port: " + portNumber);
		}

		//fanokafana socket
		try {
			clientSocket = new Socket(host, portNumber);
			inputLine = new BufferedReader(new InputStreamReader(System.in));
			os = new ObjectOutputStream(clientSocket.getOutputStream());
			is = new ObjectInputStream(clientSocket.getInputStream());
		} catch (UnknownHostException e) {
			System.err.println("Tsy fantatra " + host);
		} catch (IOException e) {
			System.err.println("Aucun server trouver");
		}

		//manombokka ny connexion
		if (clientSocket != null && os != null && is != null) {
			try {

				//mamorona thread izay vakiana avy ao aminy server
				new Thread(new Client()).start();
				while (!closed) {

					//mamaky ny input client

					String msg = (String) inputLine.readLine().trim();

					//mamadika le inpput ho lasa privee

					if ((msg.split(":").length > 1))
					{
						if (msg.split(":")[1].toLowerCase().startsWith("sendfile"))
						{
							File sfile = new File((msg.split(":")[1]).split(" ",2)[1]);
							
							if (!sfile.exists())
							{
								System.out.println("Tsy misy io fichier io!!");
								continue;
							}
							
							byte [] mybytearray  = new byte [(int)sfile.length()];
							FileInputStream fis = new FileInputStream(sfile);
							bis = new BufferedInputStream(fis);
							while (bis.read(mybytearray,0,mybytearray.length)>=0)
							{
								bis.read(mybytearray,0,mybytearray.length);
							}
							os.writeObject(msg);
							os.writeObject(mybytearray);
							os.flush();

						}
						else
						{
							os.writeObject(msg);
							os.flush();
						}

					}

					//mamadika public

					else if (msg.toLowerCase().startsWith("sendfile"))
					{

						File sfile = new File(msg.split(" ",2)[1]);
						
						if (!sfile.exists())
						{
							System.out.println("Tsy misy io fichier io!!");
							continue;
						}
						
						byte [] mybytearray  = new byte [(int)sfile.length()];
						FileInputStream fis = new FileInputStream(sfile);
						bis = new BufferedInputStream(fis);
						while (bis.read(mybytearray,0,mybytearray.length)>=0)
						{
							bis.read(mybytearray,0,mybytearray.length);
						}
						os.writeObject(msg);
						os.writeObject(mybytearray);
						os.flush();

					}

					//mamadika public
					else 
					{
						os.writeObject(msg);
						os.flush();
					}


				}

				//manidy socket
				os.close();
				is.close();
				clientSocket.close();
			} catch (IOException e) 
			{
				System.err.println("IOException:  " + e);
			}
		
			
		}
	}

	//mamorona thread izay vakiana ao aminy serveur

	public void run() {
		
		String responseLine;
		String filename = null;
		byte[] ipfile = null;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		File directory_name = null;
		String full_path;
		String dir_name = "Hazo ilay fichier";

		try {


			while ((responseLine = (String) is.readObject()) != null)  {

				//condition amoronana directory

				if (responseLine.equals("Directory Created"))
				{
					//creation ou recue du fichier

					directory_name = new File((String) dir_name);

					if (!directory_name.exists())
					{
						directory_name.mkdir();
						directory_name.createNewFile();
						System.out.println("Creation fe fichier pour un client Cree");

					}

					else
					{
						System.out.println("Reception du fichier si client est deja membres");
					}
				}

				//mandefa fichier a ttle monde

				else if (responseLine.startsWith("Sending_File"))
				{

					try
					{
						filename = responseLine.split(":")[1];
						full_path = directory_name.getAbsolutePath()+"/"+filename; 
						ipfile = (byte[]) is.readObject();
						fos = new FileOutputStream(full_path);
						bos = new BufferedOutputStream(fos);
						bos.write(ipfile);
						bos.flush();
						System.out.println("File Received.");
					}
					finally
					{
						if (fos != null) fos.close();
						if (bos != null) bos.close();
					}

				}

				/* Condition for Checking for incoming messages */

				else
				{
					System.out.println(responseLine);
				}


				/* Condition for quitting application */

				if (responseLine.indexOf("*** Veloma") != -1)
				
					break;
			}

			closed = true;
			System.exit(0);

		} catch (IOException | ClassNotFoundException e) {

			System.err.println("Midy ny server");

		}
	}
}