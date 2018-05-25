import javax.crypto.*;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;


/**
 * Created by 1 on 21.03.2018.
 */
public class Crypto {
    private static final String TYPE_OF_CRYPTO = "RSA";
    private Cipher cipher;
    private KeyPairGenerator keyPairGenerator;
    private Key publicKey;
    private Key privateKey;

    public Key getPublicKey() {
        return publicKey;
    }

    public Key getPrivateKey() {return  privateKey;}




    public Crypto() {
        try {
            cipher = Cipher.getInstance(TYPE_OF_CRYPTO);
            keyPairGenerator = KeyPairGenerator.getInstance(TYPE_OF_CRYPTO);
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            publicKey = keyPair.getPublic();
            privateKey = keyPair.getPrivate();


        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    public Crypto(PairOfKeys pairOfKeys) {
        try {
            cipher = Cipher.getInstance(TYPE_OF_CRYPTO);
            publicKey = pairOfKeys.getPublicKey();
            privateKey = pairOfKeys.getPrivateKey();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    public byte[] encrypt(String message, Key friendKey) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        cipher.init(Cipher.ENCRYPT_MODE, friendKey);
        byte[] bytes = cipher.doFinal(message.getBytes());
        return bytes;
    }

    public byte[] decrypt(byte[] message) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher decriptCipher = Cipher.getInstance(TYPE_OF_CRYPTO);
        decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decBytes = decriptCipher.doFinal(message);
        return decBytes;
    }


}
