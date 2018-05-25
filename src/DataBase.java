import com.mysql.fabric.jdbc.FabricMySQLDriver;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;

/**
 * Created by 1 on 22.01.2018.
 */


public class DataBase {
    private static final String URL = "jdbc:mysql://localhost:3306/mydbest";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "toor";

    private static Connection connect;

    public static void runDataBase() {

        try {
            Driver driver = new FabricMySQLDriver();
            DriverManager.registerDriver(driver);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try
        {
            connect = DriverManager.getConnection(URL,USERNAME,PASSWORD);
            System.out.println("БД запущена");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkContainsUsers(String name) {


        try(PreparedStatement statement = connect.prepareStatement("select name from users where name = ?")) {

            statement.setString(1,name);
            System.out.println(statement.toString());
            ResultSet resultSet = statement.executeQuery();


            while (resultSet.next()) {
                if (resultSet.getString("name").equals(name)) {
                    return true;
                }
            }

        } catch (SQLException e) {
            System.out.println("Произошла ошибка при поиске " + e.getClass().getSimpleName() + " сообщение ошибки " + e.getMessage());
        }
        catch (NullPointerException e) {
            System.out.println("Произошла ошибка при поиске " + e.getClass().getSimpleName() + " сообщение ошибки " + e.getMessage());
        }

        return false;
    }

    public static boolean registration(String name, String password) {
        if (checkContainsUsers(name)) {
            System.out.println("такой пользователь уже есть");
            return false;
        }
        else {
            String query = "INSERT INTO users VALUES(?,?,?)";
            try {
                PreparedStatement statement = connect.prepareStatement(query);
                statement.setString(1, name);
                statement.setString(2, password);
                statement.setInt(3,1);

                statement.execute();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public static boolean logIn(String login, String password) {
            String query = "SELECT * FROM users WHERE name = ?";
            try {
                PreparedStatement statement = connect.prepareStatement(query,ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                statement.setString(1, login);
                ResultSet resultSet = statement.executeQuery();

                resultSet.next();
                if (resultSet.getString("password").equals(password)) {
                    resultSet.updateInt("active",1);
                    return true;
                }

            } catch (SQLException e) {
                return false;
            }
        return false;
    }


    public byte[] read(String recipient) {
        try {
            String query = "SELECT * FROM chat WHERE recipient = ? AND passed='0'";
            PreparedStatement statement = connect.prepareStatement(query,ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            statement.setString(1, recipient);

            ResultSet resultSet = statement.executeQuery();

           /* while (resultSet.wasNull()) {
                Thread.sleep(1000);
                resultSet = statement.executeQuery();
            }*/

            if (resultSet.next()) {
                int id = resultSet.getInt("number");
                byte[] message = resultSet.getBytes("message");
                String updateQuery = "UPDATE chat SET passed = '1' WHERE number = ?";
                PreparedStatement updateStatement = connect.prepareStatement(updateQuery);
                updateStatement.setInt(1,id);

                updateStatement.executeUpdate();
                resultSet.refreshRow();

                System.out.println(resultSet.getInt("passed"));
                return message;

            }
            else {
                Thread.sleep(1000);
                return read(recipient);
            }


        } catch (SQLException e) {
            return e.getMessage().getBytes();
        }
        catch (NullPointerException e) {
            return e.getMessage().getBytes();
        } catch (InterruptedException e) {
            return e.getMessage().getBytes();
        }

    }

    public static synchronized void send(String sender, String recipient, byte[] message) {
        String query = "INSERT INTO chat VALUES(?,?,?,?,?)";
        try {
            PreparedStatement statement = connect.prepareStatement(query);

            statement.setString(1,sender);
            statement.setString(2,recipient);
            statement.setBytes(3,message);
            statement.setInt(4,0);
            statement.setInt(5,0);

            statement.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void delete(String recipient, String sender) {
        String query = "DELETE FROM chat WHERE recipient = ?";
        try {
            PreparedStatement statement = connect.prepareStatement(query);

            statement.setString(1,recipient);
            statement.executeUpdate();

            query = "DELETE FROM chat WHRERE sender = ?";

            statement = connect.prepareStatement(query);
            statement.setString(1,sender);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void setKey(String sender, Key publicKey, Key privateKey) {
        String query = "INSERT INTO tabe VALUES(?,?)";
        try {
            PreparedStatement statement = connect.prepareStatement(query);

            String key = com.sun.org.apache.xml.internal.security.utils.Base64.encode(publicKey.getEncoded());
            statement.setString(1,sender);
            statement.setString(2,key);

            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Key getKey(String sender) {
        String query = "SELECT * FROM tabe WHERE sen = ?";
        try {
            PreparedStatement statement = connect.prepareStatement(query);

            statement.setString(1, sender);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String publicKey = resultSet.getString("pen");
                X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(Base64.decode(publicKey));
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                Key key = keyFactory.generatePublic(pubKeySpec);
                return key;
            }

            else {
                return null;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }


}
