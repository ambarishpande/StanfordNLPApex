package EmailSpam;

import org.apache.hadoop.conf.Configuration;

import com.datatorrent.ambarish.StanfordNLPApex.ApexNLPClassifier;
import com.datatorrent.ambarish.StanfordNLPApex.FileInputOp;
import com.datatorrent.api.Context;
import com.datatorrent.api.DAG;
import com.datatorrent.api.StreamingApplication;
import com.datatorrent.api.annotation.ApplicationAnnotation;
import com.datatorrent.lib.io.ConsoleOutputOperator;

/**
 * Created by ambarish on 25/7/17.
 */
@ApplicationAnnotation(name = "EmailSpam")
public class Application implements StreamingApplication
{
  @Override
  public void populateDAG(DAG dag, Configuration configuration)
  {
    FileInputOp fileInputOp = dag.addOperator("Emails",FileInputOp.class);
    ApexNLPClassifier classifier = dag.addOperator("Classifier",ApexNLPClassifier.class);
    ConsoleOutputOperator console = dag.addOperator("Console",new ConsoleOutputOperator());

//    dag.addStream("emails",fileInputOp.output,classifier.input);
    dag.addStream("emailsTest",fileInputOp.output,classifier.testInput);
    dag.addStream("class",classifier.output,console.input);
    dag.setAttribute(Context.DAGContext.METRICS_TRANSPORT, null);
    dag.setAttribute(classifier, Context.OperatorContext.METRICS_AGGREGATOR, new ClassifierMetricsAggregator());

  }
}
