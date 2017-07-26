package com.datatorrent.ambarish.StanfordNLPApex;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.serializers.JavaSerializer;

import com.datatorrent.api.AutoMetric;
import com.datatorrent.api.Context;
import com.datatorrent.api.DefaultInputPort;
import com.datatorrent.api.DefaultOutputPort;
import com.datatorrent.common.util.BaseOperator;
import com.datatorrent.common.util.Pair;

import edu.stanford.nlp.classify.Classifier;
import edu.stanford.nlp.classify.ColumnDataClassifier;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.util.ErasureUtils;
import edu.stanford.nlp.util.MutableDouble;
import edu.stanford.nlp.util.PropertiesUtils;

/**
 * This operator is used to classify text documents based on the model provided by the user.
 * It uses Stanford NLP library. 
 * 
 * USAGE:
 * 1) Upload pretrained model on hdfs.
 * 2) Set modelFilePath in properties.xml to the path where the model is saved on HDFS.
 * 3) Set classifierProperties in properties.xml to properties of classifier. Each property seperated by new line.
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

  @FieldSerializer.Bind(JavaSerializer.class)
  @AutoMetric
  private Collection<Collection<Pair<String, Object>>> precisions = new ArrayList<>();

  private int [][] confusionMatrix;
  private ArrayList<String> labels;
  public transient DefaultOutputPort<String> output = new DefaultOutputPort<>();
  private double[] precision;
  private double[] recall;
  /**
   * Used for classifying unlabelled documents.
   */
//  public transient DefaultInputPort<String> input = new DefaultInputPort<String>()
//  {
//    @Override
//    public void process(String s)
//    {
//      String s1 = " \t"+s;
//      Datum<String, String> d = cdc.makeDatumFromLine(s1);
//      output.emit(s + " : " + cl.classOf(d));
//    }
//  };

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
      String predictedLabel = cl.classOf(d);
      confusionMatrix[labels.indexOf(expectedLabel)][labels.indexOf(predictedLabel)]++;
      printConfusionMatrix();
      calculateAccuracy();
      calculatePrecision();
      calculateRecall();
    }
  };

  @Override
  public void setup(Context.OperatorContext context)
  {

    accuracy = 0.0;
    cl = null;

    readModelFromHdfs(modelFilePath);
    labels = (ArrayList<String>) cl.labels();
    confusionMatrix = new int[cl.labels().size()][cl.labels().size()];
    precision = new double[cl.labels().size()];
    recall = new double[cl.labels().size()];
    Properties props = PropertiesUtils.fromString(classifierProperties);
    cdc = new ColumnDataClassifier(props);
    super.setup(context);

  }

  @Override
  public void beginWindow(long windowId)
  {
    super.beginWindow(windowId);

    precisions.clear();
    for(int i = 0; i < confusionMatrix.length;i++){
      Collection<Pair<String, Object>> row = new ArrayList<>();
      row.add(new Pair<String, Object>("Label",labels.get(i)));
      row.add(new Pair<String, Object>("Precision",new Double(precision[i])));
      row.add(new Pair<String, Object>("Recall",new Double(recall[i])));
      double f1 = 2*(precision[i]*recall[i])/(precision[i]+recall[i]);
      row.add(new Pair<String, Object>("F1 Score",new Double(f1)));
      row.add(new Pair<String, Object>("TP",new Double(confusionMatrix[i][i])));
      int sum=0;
      for (int j = 0; j<confusionMatrix.length;j++){
        sum+=confusionMatrix[j][i];
      }
      row.add(new Pair<String, Object>("Predicted",new Double(sum)));

      int sum1=0;
      for (int j = 0; j<confusionMatrix.length;j++){
        sum1+=confusionMatrix[i][j];
      }

      row.add(new Pair<String, Object>("Total Count",new Double(sum1)));

      precisions.add(row);
    }

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

  public void printConfusionMatrix(){
    for (int i=0;i<confusionMatrix.length;i++){
      for (int j =0;j<confusionMatrix.length;j++){
        System.out.print(confusionMatrix[i][j]+" ");
      }
      System.out.println("\n");
    }
  }

  public void calculateAccuracy(){

    int num=0;
    int denum=0;
    for (int i=0;i<confusionMatrix.length;i++){
      for (int j =0;j<confusionMatrix.length;j++){
        if(i==j){
          num+=confusionMatrix[i][j];
        }
        denum+=confusionMatrix[i][j];
      }
    }
    this.accuracy = (double)num/denum;
    System.out.println("Accuracy " + accuracy);
  }

  public void calculatePrecision(){

//    precision = new double[confusionMatrix.length];
    for (int i=0 ; i < confusionMatrix.length; i++){
      precision[i] = confusionMatrix[i][i];
      int sum = 0;
      for (int j=0 ; j < confusionMatrix.length; j++){
        System.out.print("+ "+ confusionMatrix[j][i]);
        sum+=confusionMatrix[j][i];
      }
      System.out.println("\n");
      precision[i]/=sum;
    }

//    for (Object o: precisions){
//      for (Object a : (ArrayList) o){
//        System.out.println(a.toString() + " ");
//      }
//      System.out.println("\n");
//    }

  }

  public void calculateRecall(){
//    recall = new double[confusionMatrix.length];
    for (int i=0 ; i < confusionMatrix.length; i++){
      recall[i] = confusionMatrix[i][i];
      int sum = 0;
      for (int j=0 ; j < confusionMatrix.length; j++){
        System.out.print("+ "+ confusionMatrix[j][i]);
        sum+=confusionMatrix[i][j];
      }
      System.out.println("\n");
      recall[i]/=sum;
    }
  }
}
