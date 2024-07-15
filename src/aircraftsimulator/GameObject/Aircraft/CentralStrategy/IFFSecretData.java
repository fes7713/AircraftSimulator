package aircraftsimulator.GameObject.Aircraft.CentralStrategy;

import aircraftsimulator.GameObject.Aircraft.Communication.Data.Data;

import javax.vecmath.Vector3f;
import java.util.Random;
import java.util.UUID;

public class IFFSecretData implements Data {
    private static final Random rand = new Random();

    private final String secret;
    private final String pw;
    private final Vector3f source;

    public IFFSecretData(String secret, String pw, Vector3f source)
    {
        this.secret = secret;
        this.pw = pw;
        this.source = source;
    }

    public String getSecret(String pw){
        if(pw.equals(this.pw))
            return secret;
        else
            return UUID.randomUUID().toString();
    }

    public Vector3f getSource(String pw)
    {
        if(pw.equals(this.pw))
            return source;
        else
            return new Vector3f(rand.nextFloat() * 1000, rand.nextFloat() * 1000, rand.nextFloat() * 1000);
    }
}
