package com.datatorrent.ambarish.StanfordNLPApex;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.datatorrent.api.AutoMetric;
import com.datatorrent.api.Context;
import com.datatorrent.api.DefaultInputPort;
import com.datatorrent.api.DefaultOutputPort;
import com.datatorrent.common.util.BaseOperator;

import edu.stanford.nlp.classify.Classifier;
import edu.stanford.nlp.classify.ColumnDataClassifier;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.util.ErasureUtils;
import edu.stanford.nlp.util.PropertiesUtils;

/**
 * This operator is used to classify text documents based on the model provided by the user.
 * It uses Stanford NLP library. 
 * 
 * USAGE:
 * 1) Upload pretrained model on hdfs.
 * 1) Set modelFilePath in properties.xml to the path where the model is saved on HDFS.
 * 2) Set classifierProperties in properties.xml to properties of classifier. Each property seperated by new line.
 * e.g.
 * <value>
 * useClassFeature = true
 * 1.splitWordsRegexp = \\s
 * 1.useSplitWords = true
 * useNB = true
 * goldAnswerColumn = 0
 * </value>
 * Created by ambarish on 25/7/17.
 */
public class ApexNLPClassifier extends BaseOperator
{
  private static final Logger LOG = LoggerFactory.getLogger(ApexNLPClassifier.class);
  private String modelFilePath;
  private transient ColumnDataClassifier cdc;
  private transient Classifier<String, String> cl;
  private String classifierProperties;
  @AutoMetric
  private double accuracy;
  @AutoMetric
  private int correct;
  @AutoMetric
  private int incorrect;
  public transient DefaultOutputPort<String> output = new DefaultOutputPort<>();

  /**
   * Used for classifying unlabelled documents.
   */
  public transient DefaultInputPort<String> input = new DefaultInputPort<String>()
  {
    @Override
    public void process(String s)
    {
      String s1 = " \t"+s;
      Datum<String, String> d = cdc.makeDatumFromLine(s1);
      output.emit(s + " : " + cl.classOf(d));
    }
  };

  /**
   * Used to test model accuracy. Provide data in 'label <tab> data' format.
   */
  public transient DefaultInputPort<String> testInput = new DefaultInputPort<String>()
  {
    @Override
    public void process(String s)
    {
      Datum<String, String> d = cdc.makeDatumFromLine(s);
      output.emit(s + " : " + cl.classOf(d));
      String expectedLabel = s.split("\t")[0];
      if (expectedLabel.equals(cl.classOf(d))) {
        correct++;
      } else {
        incorrect++;
      }
      accuracy = ((double)correct / (correct + incorrect)) * 100;
    }
  };

  @Override
  public void setup(Context.OperatorContext context)
  {

    correct = 0;
    incorrect = 0;
    accuracy = 0.0;
    Classifier<String, String> cl = null;
    readModelFromHdfs(modelFilePath);
    Properties props = PropertiesUtils.fromString(classifierProperties);
    cdc = new ColumnDataClassifier(props);
    super.setup(context);

  }

  /**
   * Method to read the model file stored at path on hdfs.
   * @param path
   */
  public void readModelFromHdfs(String path)
  {

    Path location = new Path(path);
    Configuration configuration = new Configuration();

    try {
      FileSystem hdfs = FileSystem.newInstance(new URI(configuration.get("fs.defaultFS")), configuration);
      if (hdfs.exists(location)) {
        FSDataInputStream hdfsInputStream = hdfs.open(location);
        ObjectInputStream ois = new ObjectInputStream(hdfsInputStream);
        cl = (Classifier)ErasureUtils.uncheckedCast(ois.readObject());
        LOG.info("Model Loaded Successfully");
        ois.close();
        hdfsInputStream.close();

      }

    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

  }

  public String getModelFilePath()
  {
    return modelFilePath;
  }

  public void setModelFilePath(String modelFilePath)
  {
    this.modelFilePath = modelFilePath;
  }

  public String getClassifierProperties()
  {
    return classifierProperties;
  }

  public void setClassifierProperties(String classifierProperties)
  {
    this.classifierProperties = classifierProperties;
  }

  public double getAccuracy()
  {
    return accuracy;
  }

  public void setAccuracy(double accuracy)
  {
    this.accuracy = accuracy;
  }

  public int getCorrect()
  {
    return correct;
  }

  public void setCorrect(int correct)
  {
    this.correct = correct;
  }

  public int getIncorrect()
  {
    return incorrect;
  }

  public void setIncorrect(int incorrect)
  {
    this.incorrect = incorrect;
  }
}
