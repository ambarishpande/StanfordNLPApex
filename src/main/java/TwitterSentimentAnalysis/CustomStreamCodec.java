package TwitterSentimentAnalysis;

import java.io.IOException;
import java.io.ObjectInputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.JavaSerializer;

import twitter4j.Status;

/**
 * Created by ambarish on 20/7/17.
 */
public class CustomStreamCodec<T> extends com.datatorrent.lib.codec.KryoSerializableStreamCodec<T>
{
  private int n = 0;
  private int nPartitions;
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    in.defaultReadObject();
    this.kryo = new Kryo();
    this.kryo.setClassLoader(Thread.currentThread().getContextClassLoader());
    this.kryo.register(Status.class, new JavaSerializer()); // Register the types along with custom serializers
  }

  @Override
  public int getPartition(T t)
  {

    return n++ % nPartitions;
  }

  private static final long serialVersionUID = 201411031405L;

  public void setnPartitions(int nPartitions)
  {
    this.nPartitions = nPartitions;
  }
}
