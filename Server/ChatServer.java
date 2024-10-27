import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static Set<PrintWriter> clientWriters = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Chat server started...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ChatServerThread(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ChatServerThread extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String clientName;

        public ChatServerThread(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                // Get client name
                out.println("Enter your name: ");
                clientName = in.readLine();
                broadcast(clientName + " has joined the chat.");

                String message;
                while ((message = in.readLine()) != null) {
                    broadcast(clientName + ": " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
                broadcast(clientName + " has left the chat.");
            }
        }

        private void broadcast(String message) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                }
            }
        }
    }
}
