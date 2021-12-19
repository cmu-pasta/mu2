package cmu.pasta.mu2.diff;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.List;

public class Serializer {
    public static byte[] serialize(Object[] items) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try(ObjectOutputStream oos = new ObjectOutputStream(out)){
            for (Object item : items) {
                if(item != null) oos.writeObject(item);
            }
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static List<Object> deserialize(byte[] bytes, ClassLoader cl, Object[] original) throws ClassNotFoundException, IOException {
        List<Object> itemList = new ArrayList<>();
        try(ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes)) {
            @Override
            public Class<?> resolveClass(ObjectStreamClass osc) throws IOException, ClassNotFoundException {
                try {
                    return Class.forName(osc.getName(), true, cl);
                } catch (Exception e) {
                    e.printStackTrace();
                    return super.resolveClass(osc);
                }
            }
        }) {
            for(Object item : original) {
                if(item != null) itemList.add(ois.readObject());
                else itemList.add(null);
            }
            return itemList;
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
