import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Created by 1 on 22.04.2018.
 */
public class PairOfKeys {

    private Key publicKey;
    private Key privateKey;

    public PairOfKeys(Key publicKey, Key privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }


    public Key getPublicKey() {
        return publicKey;
    }

    public Key getPrivateKey() {
        return privateKey;
    }


}
