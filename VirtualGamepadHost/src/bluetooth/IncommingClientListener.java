package bluetooth;

import java.io.DataInputStream;
import java.io.IOException;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

public class IncommingClientListener extends Thread {

	private StreamConnectionNotifier server;

	public IncommingClientListener(StreamConnectionNotifier server) {
		this.server = server;
	}

	@Override
	public void run() {
		super.run();

		while (!interrupted()) {
			try {
				StreamConnection conn = server.acceptAndOpen();
				System.out.println("Client connected!");

				DataInputStream dis = new DataInputStream(conn.openInputStream());

				BluetoothClient client = new BluetoothClient(dis);

				BluetoothServer.addClient(client);
				
				System.out.println("Added client with ID: " + client.getClientId());
				
				client.start();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
