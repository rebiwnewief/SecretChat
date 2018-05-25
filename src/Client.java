import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


/**
 * Created by 1 on 18.01.2018.
 */
public class Client {
    private Connect connect = null;
    private String name;
    private String friendName;
    private Crypto crypto;
    private static Key friendKey;


    public void setCrypto(Crypto crypto) {this.crypto = crypto;}

    public Key getFriendKey() {
        return  friendKey;
    }


    public void run() {

        System.out.println("Был создан поток ");
        Socket socket = null;
        try {
            socket = new Socket("localhost", 5555);
            connect = new Connect(socket);




        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean authorization(String login, String password, Component component) {

        try {
            connect.send(new Message(MessageType.AUTHORIZATION));
            connect.send(new Message(MessageType.LOGIN, login.getBytes()));
            connect.send(new Message(MessageType.LOGIN, password.getBytes()));
            Message message = connect.receive();
            if (message.getType() == MessageType.OKLOG) {
                JOptionPane.showMessageDialog(component, "Вы зашли");
                name = login;
                clientHandshake(connect);
                return true;
            }
            else {
                return false;
            }


        } catch (IOException e) {
            return false;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public boolean registration(String login, String password, Component component) {
        try {
            connect.send(new Message(MessageType.REGISTRATION));
            connect.send(new Message(MessageType.LOGIN, login.getBytes()));
            connect.send(new Message(MessageType.LOGIN, password.getBytes()));
            Message message = connect.receive();
            if (message.getType() == MessageType.OKREG) {
                JOptionPane.showMessageDialog(component, "Вы зарегистрировались");
                name = login;
                keyHandshake();
                clientHandshake(connect);
                return true;
            }
            else {
                return false;
            }


        } catch (IOException e) {
            return false;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void clientHandshake(Connect connect) {
        try {

            Message message = null;
            message = connect.receive();
            if (message.getType() == MessageType.NAME_REQUEST) {
                connect.send(new Message(MessageType.USER_NAME, name.getBytes()));
                message = connect.receive();
                if (message.getType() == MessageType.NAME_ACCEPTED) {
                    System.out.println("Вы подключились к серверу под именем: " + name);
                }

            } else {
                clientHandshake(connect);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void keyHandshake() {
        try {
            connect.send(new Message(MessageType.KEY, crypto.getPublicKey()));
            connect.send(new Message(MessageType.KEY, crypto.getPrivateKey()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String message) {
        try {
            byte[] sendMessage = crypto.encrypt(message, getFriendKey());
            connect.send(new Message(MessageType.TEXT, sendMessage));
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void addFriend(String name, Component component) {

        Message message = null;
        try {
                friendName = name;

                message = new Message(MessageType.FRIEND_NAME, friendName);
                connect.send(message);

                Message messageAgree = connect.receive();

                if (messageAgree.getType() == MessageType.AGREEMENT) {
                    Message messageKey = connect.receive();
                    if (messageKey.getType() == MessageType.KEY) {
                        friendKey = messageKey.getKey();
                        JOptionPane.showMessageDialog(component,"Вы добавили");

                    }
                    else if (messageKey.getType() == MessageType.NOKEY) {
                        JOptionPane.showMessageDialog(component, "Нет ключа");
                    }
                }

                else {
                    JOptionPane.showMessageDialog(component, "Такого пользователя не существует" +
                    " или он сейчас не в сети");
                }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void save(String path,PairOfKeys pairOfKeys) {
        try (ObjectOutputStream pubWriter = new ObjectOutputStream(new FileOutputStream(path+"pub"));
        ObjectOutputStream privWriter = new ObjectOutputStream(new FileOutputStream(path + "priv"))) {

            pubWriter.writeObject(pairOfKeys.getPublicKey());
            pubWriter.flush();

            privWriter.writeObject(pairOfKeys.getPrivateKey());
            privWriter.flush();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public PairOfKeys getKeys(String path) {

       // KeyPair pair = new KeyPair();
        Key pubkey = null;
        Key privkey = null;

        File pubfile = new File(path+"pub");

        try(ObjectInputStream pubReader = new ObjectInputStream(new FileInputStream(path + "pub"));
        ObjectInputStream privReader = new ObjectInputStream(new FileInputStream(path + "priv"))){
            pubkey = (Key) pubReader.readObject();
            privkey = (Key) privReader.readObject();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        PairOfKeys resPair = new PairOfKeys(pubkey,privkey);
        return resPair;

    }

    public String getMessages() {
            Message message = null;
            try {
                message = connect.receive();
                if (message.getType() == MessageType.TEXT) {
                    byte[] bytes = crypto.decrypt(message.getData());
                    return new String(bytes);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            }
            return "что-то пошло не так";
    }


}