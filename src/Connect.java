import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by 1 on 18.01.2018.
 */
public class Connect implements Closeable, Serializable {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    public Connect(Socket socket) throws IOException {
        this.socket = socket;
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public void send(Message message) throws IOException {
        synchronized (out) {
            out.writeObject(message);
        }
    }

    public Message receive() throws IOException, ClassNotFoundException {
        Message input;
        synchronized (in) {
            input = (Message) in.readObject();
        }
        return input;
    }


    @Override
    public void close() throws IOException {
        out.close();
        in.close();
        socket.close();
    }
}
