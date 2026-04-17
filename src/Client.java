import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            InetAddress ip = InetAddress.getByName("localhost");

            // establish the connection
            Socket socket = new Socket(ip, 8000);
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

            new ClientGUI(inputStream, outputStream);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
