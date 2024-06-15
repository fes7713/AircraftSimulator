package aircraftsimulator.GameObject.Aircraft.Communication;

import java.io.*;

public class ByteConvertor {
    /**
     * シリアライズする
     */
    public static byte[] serialize(Serializable serializable) {

        // Byte配列への出力を行うストリーム
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // オブジェクトをストリーム（バイト配列）に変換する為のクラス
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(baos);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // オブジェクトをストリームに変換（シリアライズ）
        try {
            oos.writeObject(serializable);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    public static byte[][] serialize(Serializable serializable, int frameSize) {

        // Byte配列への出力を行うストリーム
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // オブジェクトをストリーム（バイト配列）に変換する為のクラス
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(baos);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // オブジェクトをストリームに変換（シリアライズ）
        try {
            oos.writeObject(serializable);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int nFrames = baos.size() / frameSize + (baos.size() % frameSize == 0 ? 0 : 1);
        byte[][] arrays = new byte[nFrames][];

        ByteArrayInputStream baio = new ByteArrayInputStream(baos.toByteArray());

        for(int i = 0; i < arrays.length - 1; i++)
        {
            arrays[i] = new byte[frameSize];
            baio.read(arrays[i], 0, frameSize);
        }

        if(baos.size() / frameSize != 0)
        {
            arrays[arrays.length - 1] = new byte[baos.size() - frameSize * (arrays.length - 1)];
            baio.read(arrays[arrays.length - 1], 0, baos.size() - frameSize * (arrays.length - 1));
        }
        return arrays;
    }



    /**
     * デシリアライズする
     */
    public static <E> E deSerialize(byte[] stream)
            throws ClassNotFoundException, IOException {

        // ストリームを入力する
        ByteArrayInputStream bais = new ByteArrayInputStream(stream);

        // デシリアライズする為のクラスとストリーム入力を連結する
        ObjectInputStream os = new ObjectInputStream(bais);

        // デシリアライズ

        return (E)os.readObject();
    }

    public static <E> E deSerialize(byte[][] streams)
            throws ClassNotFoundException, IOException {

        ByteArrayOutputStream oStream = new ByteArrayOutputStream();
        for (byte[] stream : streams) {
            oStream.write(stream);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(oStream.toByteArray());
        // デシリアライズする為のクラスとストリーム入力を連結する
        ObjectInputStream os = new ObjectInputStream(bais);

        // デシリアライズ

        return (E)os.readObject();
    }

    public static String convert(byte[] stream) {

        StringBuilder sb = new StringBuilder();

        for(byte b:stream) {
            sb.append(b);
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String convert(byte[][] streams) {

        StringBuilder sb = new StringBuilder();

        for(byte[] bs:streams) {
            for(byte b: bs)
            {
                sb.append(b);
                sb.append(" ");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append("|");
        }
        return sb.toString();
    }
}
