package com.datatorrent.ambarish.StanfordNLPApex;

import com.datatorrent.api.Context;
import com.datatorrent.api.DefaultInputPort;
import com.datatorrent.common.util.BaseOperator;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import org.jooq.tools.json.JSONObject;
import org.jooq.tools.json.JSONParser;
import org.jooq.tools.json.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import org.apache.apex.api.ControlAwareDefaultOutputPort;

/**
 * Operator to perform Following NLP tasks
 * No.    Task                  Annotator Name
 * 1) Tokenization               tokenize
 * 2) Sentence Spliting          ssplit
 * 3) Parts of Speech Tagging    pos
 * 4) Named Entity Recognition   ner
 * 5) Parsing                    parse
 * 6) Sentiment Analysis         sentiment
 *
 * Usage Steps :
 * 1) Set the prop.annotator through properties.xml as a comma separated list of required annotators.
 * e.g. pos, parse
 * 2) Set prop.pipelineProperties through properties.xml for providing additional properties for annotators.
 *
 * Uses Stanford CoreNLP library v3.4.1
 */

public class AnnotatorOperator extends BaseOperator
{

  private static final Logger LOG = LoggerFactory.getLogger(AnnotatorOperator.class);
  private Map<java.lang.String, LinkedHashSet<Annotator>> annotatorDependency;
  private Properties props;
  private transient Annotation document;
  private transient StanfordCoreNLP pipeline;

  public enum Annotator
  {
    tokenize, ssplit, pos, ner, lemma, parse, sentiment
  }

  /**
   * Set through properties.xml
   * Takes Comma seperated list of annotators to be applied.
   * e.g. : pos, parse
   */
  @NotNull
  private String annotator;

  /**
   * Set through properties.xml
   * Takes JSON String with properties and values.
   * e.g. : {"ner.model" : "/path/to/model.ser.gz"}
   */
  private String pipelineProperties;

  public transient DefaultInputPort<String> input = new DefaultInputPort<String>()
  {
    @Override
    public void process(String s)
    {
      document = new Annotation(s);
      pipeline.annotate(document);
      emit();
    }
  };

  public transient ControlAwareDefaultOutputPort<Annotation> output = new ControlAwareDefaultOutputPort<>();
  public transient ControlAwareDefaultOutputPort<NLPToken> tokenizeOutput = new ControlAwareDefaultOutputPort<>();
  public transient ControlAwareDefaultOutputPort<NLPToken> ssplitOutput = new ControlAwareDefaultOutputPort<>();
  public transient ControlAwareDefaultOutputPort<NLPTag> posOutput = new ControlAwareDefaultOutputPort<>();
  public transient ControlAwareDefaultOutputPort<NLPTag> nerOutput = new ControlAwareDefaultOutputPort<>();
  public transient ControlAwareDefaultOutputPort<NLPTag> lemmaOutput = new ControlAwareDefaultOutputPort<>();
  public transient ControlAwareDefaultOutputPort<NLPTree> parseOutput = new ControlAwareDefaultOutputPort<>();
  public transient ControlAwareDefaultOutputPort<NLPToken> sentimentOutput = new ControlAwareDefaultOutputPort<>();

  @Override
  public void setup(Context.OperatorContext context)
  {

    String annotators = resolveDependency();

    props = new Properties();
    props.setProperty("annotators", annotators);
    parseAndSetPipelineProperties();
    pipeline = new StanfordCoreNLP(props);

  }

  public void emit()
  {
    long id = 0;
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      id = new BigInteger(1, md.digest(document.toString().getBytes())).longValue();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    if (output.isConnected()) {
      output.emit(document);
    }
    if (tokenizeOutput.isConnected()) {
      emitTokens(id);
    }

    if (ssplitOutput.isConnected()) {
      emitSentences(id);
    }

    if (posOutput.isConnected()) {
      emitPos(id);
    }

    if (nerOutput.isConnected()) {
      emitNer(id);
    }

    if (lemmaOutput.isConnected()) {
      emitLemmas(id);
    }

    if (parseOutput.isConnected()) {
      emitParseTree(id);
    }

    if (sentimentOutput.isConnected()) {
      emitSentiment(id);
    }

  }

  /**
   * Emits Tokens of the Input String.
   *
   * @param id
   */
  public void emitTokens(long id)
  {
    List<CoreLabel> tokens = document.get(CoreAnnotations.TokensAnnotation.class);
    tokenizeOutput.emitControl(new StartOfInputControlTuple(id,document.toString(), true));
    for (CoreLabel token : tokens) {
      tokenizeOutput.emit(new NLPToken(id, token.get(CoreAnnotations.TextAnnotation.class)));
    }
  }

  /**
   * Emits Sentences of the Input String.
   *
   * @param id
   */
  public void emitSentences(long id)
  {

    List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
    ssplitOutput.emitControl(new StartOfInputControlTuple(id,document.toString(), true));
    for (CoreMap sentence : sentences) {
      ssplitOutput.emit(new NLPToken(id, sentence.toString()));
    }
  }

  /**
   * Emits POS tag for each token in the Input String.
   *
   * @param id
   */
  public void emitPos(long id)
  {
    List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
    posOutput.emitControl(new StartOfInputControlTuple(id,document.toString(), true));

    for (CoreMap sentence : sentences) {
      for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
        String word = token.get(CoreAnnotations.TextAnnotation.class);
        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
        posOutput.emit(new NLPTag(id, word, pos));
      }
    }

  }

  /**
   * Emits NER tag for each token in the Input String.
   *
   * @param id
   */
  public void emitNer(long id)
  {
    List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
    nerOutput.emitControl(new StartOfInputControlTuple(id,document.toString(), true));

    for (CoreMap sentence : sentences) {
      for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
        String word = token.get(CoreAnnotations.TextAnnotation.class);
        String ner = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
        nerOutput.emit(new NLPTag(id, word, ner));
      }

    }
  }

  /**
   * Emits lemma for each token in the Input String.
   *
   * @param id
   */
  public void emitLemmas(long id)
  {

    List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
    lemmaOutput.emitControl(new StartOfInputControlTuple(id,document.toString(), true));
    for (CoreMap sentence : sentences) {
      for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
        String word = token.get(CoreAnnotations.TextAnnotation.class);
        String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
        lemmaOutput.emit(new NLPTag(id, word, lemma));
      }

    }
  }

  /**
   * Emits Parse Tree for each sentence in the Input String.
   *
   * @param id
   */
  public void emitParseTree(long id)
  {
    List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
    parseOutput.emitControl(new StartOfInputControlTuple(id, document.toString(), true));
    for (CoreMap sentence : sentences) {
      Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
      parseOutput.emit(new NLPTree(id, tree.toString()));
    }
  }

  /**
   * Emits Sentiment class for each sentence in the Input String.
   *
   * @param id
   */
  public void emitSentiment(long id)
  {
    List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
    sentimentOutput.emitControl(new StartOfInputControlTuple(id,document.toString(), true));
    for (CoreMap sentence : sentences) {
      Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
      int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
      String sSentiment;
      switch (sentiment) {
        case 0:
          sSentiment = "Very Negative";
          break;
        case 1:
          sSentiment = "Negative";
          break;
        case 2:
          sSentiment = "Neutral";
          break;
        case 3:
          sSentiment = "Positive";
          break;
        case 4:
          sSentiment = "Very Positive";
          break;
        default:
          sSentiment = "";
      }
      sentimentOutput.emit(new NLPToken(id, sSentiment));
    }
  }

  /**
   * Utility function to parse the pipelineProperties json string.
   */
  private void parseAndSetPipelineProperties()
  {
    if (pipelineProperties != null) {
      JSONParser jsonParser = new JSONParser();
      try {
        JSONObject jsonProps = (JSONObject)jsonParser.parse(pipelineProperties);
        for (Object o : jsonProps.keySet()) {
          props.setProperty(o.toString(), jsonProps.get(o).toString());
        }

      } catch (ParseException e) {
        e.printStackTrace();

      }
    }

  }

  /**
   * Utility function to resolve dependencies of the requested annotators.
   * Returns the final pipeline string.
   *
   * @return
   */
  public String resolveDependency()
  {

    annotatorDependency = new HashMap<>();

    LinkedHashSet<Annotator> tokenizeDep = new LinkedHashSet<>();
    tokenizeDep.add(Annotator.tokenize);
    annotatorDependency.put(Annotator.tokenize.toString(), tokenizeDep);

    LinkedHashSet<Annotator> ssplitDep = new LinkedHashSet<>();
    ssplitDep.add(Annotator.tokenize);
    ssplitDep.add(Annotator.ssplit);
    annotatorDependency.put(Annotator.ssplit.toString(), ssplitDep);

    LinkedHashSet<Annotator> posDep = new LinkedHashSet<>();
    posDep.add(Annotator.tokenize);
    posDep.add(Annotator.ssplit);
    posDep.add(Annotator.pos);
    annotatorDependency.put(Annotator.pos.toString(), posDep);

    LinkedHashSet<Annotator> lemmaDep = new LinkedHashSet<>();
    lemmaDep.add(Annotator.tokenize);
    lemmaDep.add(Annotator.ssplit);
    lemmaDep.add(Annotator.pos);
    lemmaDep.add(Annotator.lemma);
    annotatorDependency.put(Annotator.lemma.toString(), lemmaDep);

    LinkedHashSet<Annotator> nerDep = new LinkedHashSet<>();
    nerDep.add(Annotator.tokenize);
    nerDep.add(Annotator.ssplit);
    nerDep.add(Annotator.pos);
    nerDep.add(Annotator.lemma);
    nerDep.add(Annotator.ner);
    annotatorDependency.put(Annotator.ner.toString(), nerDep);

    LinkedHashSet<Annotator> parseDep = new LinkedHashSet<>();
    parseDep.add(Annotator.tokenize);
    parseDep.add(Annotator.ssplit);
    parseDep.add(Annotator.parse);
    annotatorDependency.put(Annotator.parse.toString(), parseDep);

    LinkedHashSet<Annotator> sentimentDep = new LinkedHashSet<>();
    sentimentDep.add(Annotator.tokenize);
    sentimentDep.add(Annotator.ssplit);
    sentimentDep.add(Annotator.pos);
    sentimentDep.add(Annotator.parse);
    sentimentDep.add(Annotator.sentiment);
    annotatorDependency.put(Annotator.sentiment.toString(), sentimentDep);

    String[] allAnnotators = annotator.split(",");

    LinkedHashSet<Annotator> main = new LinkedHashSet<>();
    for (String annotator : allAnnotators) {
      main.addAll(annotatorDependency.get(annotator));
    }

    String finalPipeline = new String();
    for (Annotator s : main
      ) {
      finalPipeline += s + ", ";

    }
    finalPipeline = finalPipeline.replaceAll(", $", "");

    return finalPipeline;
  }

  public String getAnnotator()
  {
    return annotator;
  }

  public void setAnnotator(String annotator)
  {
    this.annotator = annotator;
  }

  public String getPipelineProperties()
  {
    return pipelineProperties;
  }

  public void setPipelineProperties(String pipelineProperties)
  {
    this.pipelineProperties = pipelineProperties;
  }
}
