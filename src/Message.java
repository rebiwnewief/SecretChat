import java.io.Serializable;
import java.security.Key;

/**
 * Created by 1 on 18.01.2018.
 */
public class Message implements Serializable {
    private byte[] data;
    private MessageType type;
    private Key key;
    private String info;

    public Message(MessageType type) {
        this.type = type;
        data = null;
    }

    public Message(MessageType type, byte[] data) {
        this.type = type;
        this.data = data;
    }

    public Message(MessageType type, Key key) {
        this.type = type;
        this.key = key;
    }

    public Message(MessageType type, String info) {
        this.type = type;
        this.info = info;
    }

    public byte[] getData() {
        return data;
    }

    public MessageType getType() {
        return type;
    }

    public Key getKey() {
        return key;
    }

    public String getInfo() {
        return info;
    }
}
