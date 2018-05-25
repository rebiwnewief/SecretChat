import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by 1 on 25.04.2018.
 */
public class Chat extends JFrame {
    static Chat chat;
    JTextArea area;
    JPanel jPanel;
    private Client client;
    private String yourName, friendName;
    private Crypto crypto;

    public static void main(String[] args) {

        chat = new Chat();
        chat.firstAnswer();


    }
    public Chat() {
        client = new Client();
        client.run();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setSize(373,345);
        jPanel = new JPanel();
        area = new JTextArea(20,50);
        JScrollPane jScrollPane = new JScrollPane(area);
        jScrollPane.setBounds(5,5,350,250);
        area.setLineWrap(true);
        jPanel.setLayout(null);

        final JTextField textField = new JTextField(10);
        textField.setBounds(5,255,220,50);
        textField.setText("For output");

        JButton send = new JButton("send");
        send.setBounds(226,255,128,50);


        jPanel.add(jScrollPane);
        jPanel.add(textField);
        jPanel.add(send);
        add(jPanel);


        jPanel.revalidate();

        send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.send(textField.getText());
                area.append("\n" + yourName + ": " + textField.getText());
                textField.setText("");
            }
        });


    }

    public void firstAnswer() {
        String[] options = {"login", "registration"};
        int answer = JOptionPane.showOptionDialog(chat, "Вы зарегистрированы?", "Начало",JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, "login");

        switch (answer) {
            case 0:
                loginAnswer(); break;
            case 1:
                registrationAnswer(); break;
        }
    }

    public void loginAnswer() {
        final JTextField name = new JTextField(10);
        final JPasswordField password = new JPasswordField(10);
        Object[] object = { new JLabel("login"),name,
                new JLabel("password"), password,
               "OK", "Cancel"};

        int answer = JOptionPane.showOptionDialog(chat, "Введите имя и пароль", "Авторизация", JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, object, "OK");

        System.out.println(name.getText());
        System.out.println(new String(password.getPassword()));
        System.out.println(answer);

       switch (answer) {
           case 4:
               logVerification(name.getText(), new String(password.getPassword())); break;

           case 5:
               firstAnswer(); break;
       }
    }

    public void logVerification(String name, String password) {
        if (client.authorization(name, password, chat)) {
            yourName = name;
            getKeys();
            friendName = chooseFried();
            client.addFriend(friendName, chat);
            messagesListener();
        }
    }

    public void registrationAnswer() {
        final JTextField name = new JTextField(10);
        final JPasswordField password = new JPasswordField(10);
        Object[] object = { new JLabel("login"),name,
                new JLabel("password"), password,
                "OK", "Cancel"};

        int answer = JOptionPane.showOptionDialog(chat, "Введите имя и пароль", "Регистрация", JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, object, "OK");

        switch (answer) {
            case 4:
                regVerification(name.getText(), new String(password.getPassword())); break;
            case 5:
                firstAnswer();
        }
    }

    public void regVerification(String name, String password) {
        crypto = new Crypto();
        client.setCrypto(crypto);
        if (client.registration(name, password, chat)) {
            yourName = name;
            System.out.println(crypto.getPublicKey());
            setKeys(new PairOfKeys(crypto.getPublicKey(), crypto.getPrivateKey()));
            friendName = chooseFried();
            client.addFriend(friendName, chat);
            messagesListener();
        }
    }



    public String chooseFried() {
        return JOptionPane.showInputDialog(chat, "addFriend", JOptionPane.QUESTION_MESSAGE);
    }


    public void getKeys() {
        String path = JOptionPane.showInputDialog(chat, "Укажите путь к файлу", JOptionPane.QUESTION_MESSAGE);
        crypto = new Crypto(client.getKeys(path));
        client.setCrypto(crypto);
    }

    public void setKeys(PairOfKeys pairOfKeys) {
        String path = JOptionPane.showInputDialog(chat, "Укажите путь к файлу", JOptionPane.QUESTION_MESSAGE);
        client.save(path, pairOfKeys);
    }

    public void messagesListener() {
        MessageListener messageListener = new MessageListener();
        messageListener.setDaemon(true);
        messageListener.run();
    }

    public synchronized void refreshMessages(String message) {
        area.append("\n" +friendName + ": " + message);
    }


    private class MessageListener extends Thread {
        @Override
        public void run() {
            while (true) {
                refreshMessages(client.getMessages());
            }
        }
    }


}
