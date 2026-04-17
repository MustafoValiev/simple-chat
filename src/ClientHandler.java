import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class ClientHandler implements Runnable {
    public int id;
    public boolean isLoggedIn;
    private String username;
    private final DataInputStream inputChannel;
    private final DataOutputStream outputChannel;
    private final Socket socket;
    private final Connection connection;

    public enum CreateOrGetUserResultValues {CREATE, GET, ERROR}

    // constructor
    public ClientHandler(Socket socket, Connection connection) {
        try {
            this.inputChannel = new DataInputStream(socket.getInputStream());
            this.outputChannel = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.id = -1;
        this.socket = socket;
        this.connection = connection;
        this.isLoggedIn = false;
    }

    public void UpdateDB(String table, String column, String value) {
        try {
            Statement stmt = this.connection.createStatement();
            String query = String.format("UPDATE %s SET %s='%s' WHERE client_id='%s'", table, column, value, this.id);

            stmt.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void InsertDB(String table, Map<String, String> items) {
        try {
            Statement stmt = this.connection.createStatement();
            String query = String.format("INSERT INTO %s (%s) VALUES (%s)",
                    table,
                    String.join(", ", items.keySet()),
                    String.join(", ", items.values().stream().map(x -> String.format("'%s'", x)).toList()));

            stmt.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void AddMessageToDB(String message) {
        this.InsertDB("messages", Map.of(
                "user_id", String.format("%d", this.id),
                "message", message,
                "type", "text"
        ));
    }

    public int GetUserIdDB(String username) {
        try {
            var stmt = this.connection.createStatement();
            var query = String.format("SELECT * FROM users WHERE name='%s'", username);
            var result = stmt.executeQuery(query);

            if (result.next()) {
                return result.getInt("id");
            } else {
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public CreateOrGetUserResultValues CreateOrGetUserDB(String username) {
        var id = GetUserIdDB(username);

        if (id == -1) {
            this.InsertDB("users", Map.of(
                    "name", username
            ));

            this.id = this.GetUserIdDB(username);
            return CreateOrGetUserResultValues.CREATE;
        } else {
            this.id = id;
            return CreateOrGetUserResultValues.GET;
        }
    }

    public String GetAllMessagesDB() {
        try {
            Statement stmt = this.connection.createStatement();
            String query = "SELECT messages.id, name, message FROM messages LEFT JOIN users ON messages.user_id = users.id ORDER BY messages.id DESC LIMIT 30";

            var result = stmt.executeQuery(query);
            StringBuilder output = new StringBuilder();

            while (result.next()) {
                var name = result.getString("name");
                var message = result.getString("message");
                output.append(String.format("%s@%s#", name, message));
            }
            if (output.length() > 1) {
                return output.substring(0, output.length() - 1);
            }
            else return output.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void SendMessage(String message) {
        try {
            this.outputChannel.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String received;
        while (true) {
            try {
                received = inputChannel.readUTF();

                String[] parts = received.split("/");
                String operation = parts[0].trim();

                switch (operation) {
                    case "enter": {
                        var username = parts[1];

                        var result = this.CreateOrGetUserDB(username);
                        this.isLoggedIn = true;

                        var output = "";
                        var shouldFetchMessages = false;

                        switch (result) {
                            case CREATE: {
                                output = String.format("enter/success/Hello, new user: %s!", username);
                                this.username = username;
                                shouldFetchMessages = true;
                                break;
                            }
                            case GET: {
                                output = String.format("enter/success/Welcome back: %s!", username);
                                this.username = username;
                                shouldFetchMessages = true;
                                break;
                            }
                            case ERROR: {
                                output = String.format("enter/error/Error occurred. Try restart client!");
                                break;
                            }
                        }

                        if (shouldFetchMessages) {
                            output += "/" + this.GetAllMessagesDB();
                        }

                        outputChannel.writeUTF(output);

                        break;
                    }
                    case "chat": {
                        var message = parts[1];

                        this.AddMessageToDB(message);
                        Server.Instance.SendAllExceptOne(message, this.username, this.id);
                        break;
                    }
                }
            } catch (Exception e1) {
                e1.printStackTrace();
                break;
            }
        }
    }
}
