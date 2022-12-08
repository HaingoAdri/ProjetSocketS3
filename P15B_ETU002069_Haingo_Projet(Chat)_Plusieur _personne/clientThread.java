package client;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class clientThread extends Thread {

	private String clientName = null;
	private ObjectInputStream is = null;
	private ObjectOutputStream os = null;
	private Socket clientSocket = null;
	private final ArrayList<clientThread> clients;
	public clientThread(Socket clientSocket, ArrayList<clientThread> clients) {

		this.clientSocket = clientSocket;
		this.clients = clients;

	}


	public void run() {

		ArrayList<clientThread> clients = this.clients;

		try {
			//manokatra input output ho an'ilay client
			is = new ObjectInputStream(clientSocket.getInputStream());
			os = new ObjectOutputStream(clientSocket.getOutputStream());

			String name;
			while (true) {

				synchronized(this)
				{
					this.os.writeObject("Mampiditra Anarana :");
					this.os.flush();
					name = ((String) this.is.readObject()).trim();

					if ((name.indexOf('@') == -1) || (name.indexOf('!') == -1)) {
						break;
					} else {
						this.os.writeObject("tsy azo asiana '@' na '!' ny anarana.");
						this.os.flush();
					}
				}
			}

				//bienvenue

				System.out.println("Anaranao " + name); 

				this.os.writeObject("*** Tongasoa " + name + " ato aminy chat socket mananiany ***\nEnter /quit raha te hiala ato");
				this.os.flush();

				this.os.writeObject("Creation de fichier");
				this.os.flush();
				synchronized(this)
				{

				for (clientThread curr_client : clients)  
				{
					if (curr_client != null && curr_client == this) {
						clientName = "@" + name;
						break;
					}
				}

				for (clientThread curr_client : clients) {
					if (curr_client != null && curr_client != this) {
						curr_client.os.writeObject(name + " has joined");
						curr_client.os.flush();

					}

				}
			}

			//manomboka ny resaka

			while (true) {

				this.os.writeObject("Soraty ary ehh:");
				this.os.flush();

				String line = (String) is.readObject();


				if (line.startsWith("/quit")) {

					break;
				}

				//message privee

				if (line.startsWith("@")) {

					privee(line,name);        	

				}

				//bloquer message ho an'olona tiany tsy ahita azy

				else if(line.startsWith("!"))
				{
					aParts(line,name);
				}

				else 
				{

					toutLeMonde(line,name);

				}

			}

			//miala rehef te hiala

			this.os.writeObject("*** Veloma " + name + " ***");
			this.os.flush();
			System.out.println(name + " miala.");
			clients.remove(this);


			synchronized(this) {

				if (!clients.isEmpty()) {

					for (clientThread curr_client : clients) {


						if (curr_client != null && curr_client != this && curr_client.clientName != null) {
							curr_client.os.writeObject("*** Niala i " + name + " !!! ***");
							curr_client.os.flush();
						}




					}
				}
			}


			this.is.close();
			this.os.close();
			clientSocket.close();

		} catch (IOException e) {

			System.out.println("Tapitra ny session");

		} catch (ClassNotFoundException e) {

			System.out.println("Class Not Found");
		}
	}



	//manao transfert de fichier sy mamoaka message hafa tsy ny olona izay tsy tianao ahita an'ilay message

	public void aParts(String line, String name) throws IOException, ClassNotFoundException {

		String[] words = line.split(":", 2);

		//transfert sauf misy exception

		if (words[1].split(" ")[0].toLowerCase().equals("sendfile"))
		{
			byte[] file_data = (byte[]) is.readObject();

			synchronized(this) {
				for (clientThread curr_client : clients) {
					if (curr_client != null && curr_client != this && curr_client.clientName != null
							&& !curr_client.clientName.equals("@"+words[0].substring(1)))
					{
						curr_client.os.writeObject("Nandefa fichier:"+words[1].split(" ",2)[1].substring(words[1].split("\\s",2)[1].lastIndexOf(File.separator)+1));
						curr_client.os.writeObject(file_data);
						curr_client.os.flush();


					}
				}

				//mampiseho an'ilay fichier nalefa 

				this.os.writeObject(">>Nadefa message amin'ny olona rehetra hafa tsy "+words[0].substring(1));
				this.os.flush();
				System.out.println("Fichier nalefan'i "+ this.clientName.substring(1) + " amin'ny olon'drehetra hafa tsy " + words[0].substring(1));
			}
		}

		//mandefa message

		else 
		{
			if (words.length > 1 && words[1] != null) {
				words[1] = words[1].trim();
				if (!words[1].isEmpty()) {
					synchronized (this){
						for (clientThread curr_client : clients) {
							if (curr_client != null && curr_client != this && curr_client.clientName != null
									&& !curr_client.clientName.equals("@"+words[0].substring(1))) {
								curr_client.os.writeObject("<" + name + "> " + words[1]);
								curr_client.os.flush();


							}
						}
						//mampiseho message nalefa

						this.os.writeObject(">>Nadefa message amin'ny olona rehetra hafa tsy "+words[0].substring(1));
						this.os.flush();
						System.out.println("Message nalefan'i "+ this.clientName.substring(1) + " amin'ny olon'drehetra hafa tsy " + words[0].substring(1));
					}
				}
			}
		}
	}

	//mandefa message amin'ny olon drehetra
	public void toutLeMonde(String line, String name) throws IOException, ClassNotFoundException {

		//mandefa fichier aminy client rehetra

		if (line.split("\\s")[0].toLowerCase().equals("sendfile"))
		{

			byte[] file_data = (byte[]) is.readObject();
			synchronized(this){
				for (clientThread curr_client : clients) {
					if (curr_client != null && curr_client.clientName != null && curr_client.clientName!=this.clientName) 
					{
						curr_client.os.writeObject("Nandefa fichier:"+line.split("\\s",2)[1].substring(line.split("\\s",2)[1].lastIndexOf(File.separator)+1));
						curr_client.os.writeObject(file_data);
						curr_client.os.flush();

					}
				}

				this.os.writeObject("Ficher lasa tsara.");
				this.os.flush();
				System.out.println("Nalefan'i " + this.clientName.substring(1));
			}
		}

		else
		{
			//mandefa message
			synchronized(this){

				for (clientThread curr_client : clients) {

					if (curr_client != null && curr_client.clientName != null && curr_client.clientName!=this.clientName) 
					{

						curr_client.os.writeObject("<" + name + "> " + line);
						curr_client.os.flush();

					}
				}

				this.os.writeObject("Message lasa tsara!");
				this.os.flush();
				System.out.println("Nalefan'i " + this.clientName.substring(1));
			}

		}

	}

	//message privee amizay
	public void privee(String line, String name) throws IOException, ClassNotFoundException {

		String[] words = line.split(":", 2); 

		//transfert de fichier
		if (words[1].split(" ")[0].toLowerCase().equals("sendfile"))
		{
			byte[] file_data = (byte[]) is.readObject();

			for (clientThread curr_client : clients) {
				if (curr_client != null && curr_client != this && curr_client.clientName != null
						&& curr_client.clientName.equals(words[0]))
				{
					curr_client.os.writeObject("Fichier lasa:"+words[1].split(" ",2)[1].substring(words[1].split("\\s",2)[1].lastIndexOf(File.separator)+1));
					curr_client.os.writeObject(file_data);
					curr_client.os.flush();
					System.out.println(this.clientName.substring(1) + " nandefa fichier privee tany a "+ curr_client.clientName.substring(1));

					//fichier privee envoyer
					this.os.writeObject("Fichier privee nalefa tany amin'ny " + curr_client.clientName.substring(1));
					this.os.flush();
					break;

				}
			}
		}

		//mandefa message
		else
		{

			if (words.length > 1 && words[1] != null) {

				words[1] = words[1].trim();


				if (!words[1].isEmpty()) {

					for (clientThread curr_client : clients) {
						if (curr_client != null && curr_client != this && curr_client.clientName != null
								&& curr_client.clientName.equals(words[0])) {
							curr_client.os.writeObject("<" + name + "> " + words[1]);
							curr_client.os.flush();

							System.out.println(this.clientName.substring(1) + " nandefa message privee tany amin'i "+ curr_client.clientName.substring(1));

							//mandefa message privee tany amin'ny
							this.os.writeObject("Message privee nalefa tany amin'i " + curr_client.clientName.substring(1));
							this.os.flush();
							break;
						}
					}
				}
			}
		}
	}


}
