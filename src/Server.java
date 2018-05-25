import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by 1 on 18.01.2018.
 */
public class Server {
    private static Map<String, Connect> connectionMap = new ConcurrentHashMap<>();
    private static Map<String, String> network = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        DataBase.runDataBase();

        int serverPort = 5555;

        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {

            System.out.println("Сервер запущен");

            while (true) {
                Socket socket = serverSocket.accept();
                Handler handler = new Handler(socket);
                handler.start();
            }
        } catch (Exception e) {
            System.out.println(" Ошибка ");
        }
    }

    public static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override

        public void run() {
            System.out.println("Установлено соединение с удаленным клиентом с адресом: " +
                    socket.getRemoteSocketAddress());
            Connect connect = null;
            String name = null;
            try {
                connect = new Connect(socket);
                loginOrRegistration(connect);
                name = serverHandshake(connect);
                serverMainLoop(connect, name);
            } catch (IOException e) {
                handleHandlerException(e, connect);
            } catch (ClassNotFoundException e) {
                handleHandlerException(e, connect);
            }

            System.out.println(String.format("Соединение с удаленным адресом (%s) закрыто.", socket.getRemoteSocketAddress()));
        }

        private void loginOrRegistration(Connect connect) {
            try {
                Message message = connect.receive();
                if (message.getType() == MessageType.AUTHORIZATION) {
                    login(connect);
                }
                else if (message.getType() == MessageType.REGISTRATION) {
                    registration(connect);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private void login(Connect connect) {
            Message messageLogin = null;
            Message messagePassword = null;
            try {
                messageLogin = connect.receive();
                messagePassword = connect.receive();
                Boolean result = DataBase.logIn(new String(messageLogin.getData()), new String(messagePassword.getData()));
                if (result) {
                    connect.send(new Message(MessageType.OKLOG));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }

        private void registration(Connect connect) {
            Message messageLogin = null;
            Message messagePassword = null;
            try {
                messageLogin = connect.receive();
                messagePassword = connect.receive();
                Boolean result = DataBase.registration(new String(messageLogin.getData()), new String(messagePassword.getData()));
                if (result) {
                    connect.send(new Message(MessageType.OKREG));
                    Message keyMessage = connect.receive();
                    Message privateKey = connect.receive();
                    if (keyMessage.getType() == MessageType.KEY) {
                        DataBase.setKey(new String(messageLogin.getData()), keyMessage.getKey(), privateKey.getKey());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }


        private String serverHandshake(Connect connection) throws IOException, ClassNotFoundException {
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message message = connection.receive();
                System.out.println(message);
                if (message.getType() == MessageType.USER_NAME) {
                    if (message.getData().length == 0) {
                        continue;
                    }
                    if (connectionMap.containsKey(message.getData())) {
                        continue;
                    }

                    System.out.println("Вы добавлены");
                    connectionMap.put(new String(message.getData()), connection);
                    connection.send(new Message(MessageType.NAME_ACCEPTED));

                    return new String(message.getData());
                }
            }
        }


        private void handleHandlerException(Exception e, Connect connect) {
            System.out.println("Произошла ошибка при обмене данными с удаленным адресом: " +
                    socket.getRemoteSocketAddress() + "%n" +
                    "Тип ошибки: " + e.getClass().getSimpleName() + "%n" +
                    "Текст ошибки: " + e.getMessage());
            try {
                if (connect != null)
                    connect.close();
                socket.close();
            } catch (IOException e_) { /* NOP */ }
        }

        private void keyHandshakeBetweenUsers(String sender, String recipient) {
            try {
                Connect senderConnection = connectionMap.get(sender);
                Key recipientKey = DataBase.getKey(recipient);
                if (recipientKey == null) {
                    senderConnection.send(new Message(MessageType.NOKEY));
                }
                senderConnection.send(new Message(MessageType.KEY, recipientKey));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }




        private void serverMainLoop(Connect connect, String name) throws IOException, ClassNotFoundException {
            String nameOfFriend = null;
            Message message = null;



            message = connect.receive();
            System.out.println(message.getType());
            if (message.getType() == MessageType.FRIEND_NAME) {
                if (DataBase.checkContainsUsers(message.getInfo())) {
                    network.put(name, message.getInfo());
                    nameOfFriend = network.get(name);

                    connect.send(new Message(MessageType.AGREEMENT));
                    keyHandshakeBetweenUsers(name, nameOfFriend);
                }
                else {
                    connect.send(new Message(MessageType.DISAGREEMENT));
                }
            }


            Send send = new Send(connect, name);
            send.setDaemon(true);
            send.start();
            DataBase dataBase = new DataBase();
            while (true) {
                byte[] text = dataBase.read(name);
                connect.send(new Message(MessageType.TEXT,text));
            }
        }

        public void closeSocket() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public class Send extends Thread {
            private final Connect connect;
            private final String name;

            public Send(Connect connect, String name) {
                this.connect = connect;
                this.name = name;
            }

            @Override
            public void run() {
                while (true) {
                    try {

                        Message message = connect.receive();
                        if (message.getType() == MessageType.TEXT) {
                            DataBase.send(name, network.get(name), message.getData());
                        }
                        else if (message.getType() == MessageType.QUIT) {
                            closeSocket();
                            connect.close();
                            DataBase.delete(name, network.get(name));
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        this.interrupt();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        this.interrupt();
                    }

                }
            }
        }
    }
}


