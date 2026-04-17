public class Application {
    public static void main(String[] args) {
        final String port = "3306";
        final String database = "java_chat";
        final String user = "root";
        final String password = "";

        var server = new Server(
                port,
                database,
                user,
                password
        );
        server.run();
    }
}
