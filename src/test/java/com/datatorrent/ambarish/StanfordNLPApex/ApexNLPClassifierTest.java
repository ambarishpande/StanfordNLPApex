package com.datatorrent.ambarish.StanfordNLPApex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;

import com.datatorrent.lib.testbench.CountAndLastTupleTestSink;

/**
 * Created by ambarish on 25/7/17.
 */
public class ApexNLPClassifierTest
{

  @Test
  public void classifierTest() throws IOException
  {
    ApexNLPClassifier op = new ApexNLPClassifier();
    CountAndLastTupleTestSink sink = new CountAndLastTupleTestSink();
    op.setModelFilePath( "/home/ambarish/models/email/spam/email.model");
    op.setClassifierProperties("useClassFeature = true\n" +
      "1.splitWordsRegexp = \\\\s\n" +
      "1.useSplitWords = true\n" +
      "useNB = true\n" +
      "goldAnswerColumn = 0");
    op.output.setSink(sink);
    op.setup(null);
    op.beginWindow(0);
    // Testing Test Input
    FileInputStream fis = new FileInputStream(new File("/code/Development/StanfordNLPApex/src/main/resources/data/email.test"));
    BufferedReader br = new BufferedReader(new InputStreamReader(fis));
    while (br.ready()){
      String x = br.readLine();
      System.out.println("Length : " + x.length());
      op.testInput.process(x);
      System.out.println(sink.tuple);
    }
    System.out.println("Accuracy : " + op.getAccuracy());
    br.close();
    fis.close();

    //Testing input
//    op.input.process("llt v . 2 , n . 1  happy announce vol . 2 , . 1 language learn  technology available http polyglot . cal .\n msu . edu  llt . special issue \" design evaluation multimedium software , \" contents list below . please visit llt web site sure enter free subscription already . , welcome submission article , review , commentary vol . 2 , . 2 future issue . check our guideline submission http polyglot . cal . msu . edu  llt  contrib . html . lucinda hart  gonzalez  mark warschauer , editor llteditor  hawaius . edu feature article 1 . carol . chapelle , \" multimedium call  lesson learn research instruct sla \" 2 . jan l . plass , \" design evaluation user interface foreign language multimedium software  cognitive approach \" 3 . farzad ehsanus  eva knodt , \" speech technology computer  aide language learn  strength limitation call paradigm \" 4 . dorothy m . chun , \" signal analysis software teach discourse intonation \" column  editor lucinda hart  gonzalez , co  editor  guest editor irene thompson net using www multimedium foreign language classroom  is ? jean w . leloup robert ponterio emerge technology development digital video bob godwin  jone announcement sponsor organization review computer assist language learn  context conceptualization review christine leahy tripleplay plus  english review alison mackey jung  yoon chous call paper theme  role computer technology second language acquisition research");
//    System.out.println(sink.tuple);

    op.endWindow();
    op.teardown();
  }

}