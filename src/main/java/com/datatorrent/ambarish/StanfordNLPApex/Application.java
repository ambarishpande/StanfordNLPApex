/**
 * Put your copyright and license info here.
 */
package com.datatorrent.ambarish.StanfordNLPApex;

import org.apache.hadoop.conf.Configuration;

import com.datatorrent.api.annotation.ApplicationAnnotation;
import com.datatorrent.api.StreamingApplication;
import com.datatorrent.api.DAG;
import com.datatorrent.api.DAG.Locality;
import com.datatorrent.lib.io.ConsoleOutputOperator;

import java.io.File;

@ApplicationAnnotation(name = "StanfordNLPApex")
public class Application implements StreamingApplication
{

  @Override
  public void populateDAG(DAG dag, Configuration conf)
  {

    FileInputOp fileInputOp = dag.addOperator("FileInputOp", FileInputOp.class);
    AnnotatorOperator annotatorOp = dag.addOperator("Annotator", AnnotatorOperator.class);
    ControlAwareConsoleOutputOperator console = dag.addOperator("TOKEN", new ControlAwareConsoleOutputOperator());
//    ControlAwareConsoleOutputOperator console2 = dag.addOperator("SENTENCE", new ControlAwareConsoleOutputOperator());
//    ControlAwareConsoleOutputOperator console3 = dag.addOperator("POS", new ControlAwareConsoleOutputOperator());
//    ControlAwareConsoleOutputOperator console4 = dag.addOperator("LEMMA", new ControlAwareConsoleOutputOperator());
//    ControlAwareConsoleOutputOperator console5 = dag.addOperator("NER", new ControlAwareConsoleOutputOperator());
//    ControlAwareConsoleOutputOperator console6 = dag.addOperator("PARSE", new ControlAwareConsoleOutputOperator());
//    ControlAwareConsoleOutputOperator console7 = dag.addOperator("Sentiment", new ControlAwareConsoleOutputOperator());
//    ControlAwareConsoleOutputOperator console8 = dag.addOperator("Coreference", new ControlAwareConsoleOutputOperator());

    dag.addStream("Strings", fileInputOp.output, annotatorOp.input);
    dag.addStream("Tokens", annotatorOp.tokenizeOutput, console.input);
//    dag.addStream("Sentences", annotatorOp.ssplitOutput, console2.input);
//    dag.addStream("POS", annotatorOp.posOutput, console3.input);
//    dag.addStream("LEMMA", annotatorOp.lemmaOutput, console4.input);
//    dag.addStream("NER", annotatorOp.nerOutput, console5.input);
//    dag.addStream("PARSE", annotatorOp.parseOutput, console6.input);
//    dag.addStream("Sentiment", annotatorOp.sentimentOutput, console7.input);
//    dag.addStream("Coreference", annotatorOp.corefOutput, console8.input);

  }
}
