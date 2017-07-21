/**
 * Put your copyright and license info here.
 */
package TwitterSentimentAnalysis;

import org.apache.hadoop.conf.Configuration;

import com.datatorrent.ambarish.StanfordNLPApex.AnnotatorOperator;
import com.datatorrent.api.Context;
import com.datatorrent.api.DAG;
import com.datatorrent.api.StreamingApplication;
import com.datatorrent.api.annotation.ApplicationAnnotation;

import twitter4j.Status;

@ApplicationAnnotation(name = "TwitterSentimentAnalysis")
public class Application implements StreamingApplication
{

  @Override
  public void populateDAG(DAG dag, Configuration conf)
  {

    TwitterSampleInput twitter = dag.addOperator("Twitter", new TwitterSampleInput());
    TweetCleaner cleaner = dag.addOperator("Tweet-Cleaner", TweetCleaner.class);
    AnnotatorOperator annotatorOp = dag.addOperator("Annotator", AnnotatorOperator.class);
    TweetAnalyser analyser = dag.addOperator("Analyser", TweetAnalyser.class);

    dag.addStream("Tweets", twitter.status, cleaner.input);
    dag.addStream("Cleaned Tweets", cleaner.output, annotatorOp.input);
    dag.addStream("Sentiment", annotatorOp.sentimentOutput, analyser.input);

    dag.setInputPortAttribute(cleaner.input, Context.PortContext.STREAM_CODEC, new CustomStreamCodec<Status>());
  }
}
