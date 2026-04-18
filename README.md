# Java Chat Project

## Overview

This is a simple Java chat application with a GUI client and a server backend.
The server stores users and messages in a MySQL database and accepts TCP connections from clients.

## Project structure

- `src/` - Java source files
- `lib/` - optional library dependencies
- `libs/mysql-connector-j-8.2.0/` - MySQL JDBC driver
- `.gitignore` - ignores build artifacts and IDE metadata

## Requirements

- Java JDK 17 or newer
- MySQL server running on `localhost:3306`
- A MySQL database named `java_chat`
- The `mysql-connector-j-8.2.0.jar` file is included in `libs/mysql-connector-j-8.2.0/`

## Database setup

Create the database and required tables before running the server.
Below is a sample SQL structure:

```sql
CREATE DATABASE java_chat;
USE java_chat;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## Build and run

From the project root, compile the source files and include the MySQL JDBC driver on the classpath.

### Compile

```bash
javac -cp "libs/mysql-connector-j-8.2.0/mysql-connector-j-8.2.0.jar" src/*.java
```

### Run the server

```bash
java -cp "libs/mysql-connector-j-8.2.0/mysql-connector-j-8.2.0.jar;src" Application
```

### Run the client

```bash
java -cp "src" Client
```

> Note: On macOS/Linux, replace `;` with `:` in the classpath.

## Notes

- The server listens on port `8000`.
- The client connects to `localhost` port `8000`.
- If you use an IDE like IntelliJ IDEA, keep `.idea/` and `*.iml` out of the repository.

