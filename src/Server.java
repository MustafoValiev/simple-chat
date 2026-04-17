import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.sql.*;

public class Server {
    public static Server Instance = null;

    static Vector<ClientHandler> clientHandlers = new Vector<>();

    public boolean isRunning;

    private final String port;
    private final String database;
    private final String user;
    private final String password;

    public Server(String port, String database, String user, String password) {
        if (Instance == null) Instance = this;

        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
    }

    public void SendAllExceptOne(String message, String sender, int id) {
        for (ClientHandler client : clientHandlers) {
            if (client.id != id && client.isLoggedIn) {
                client.SendMessage(String.format("chat/%s/%s", sender, message));
            }
        }
    }

    public void run() {
        Connection connection = null;

        try {
            var server = new ServerSocket(8000);
            Socket socket;

            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                    String.format("jdbc:mysql://localhost:%s/%s", this.port, this.database),
                    this.user,
                    this.password);

            while (true) {
                isRunning = true;
                socket = server.accept();

                ClientHandler clientHandler = new ClientHandler(socket, connection);

                var t = new Thread(clientHandler);
                clientHandlers.add(clientHandler);

                t.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isRunning = false;
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}