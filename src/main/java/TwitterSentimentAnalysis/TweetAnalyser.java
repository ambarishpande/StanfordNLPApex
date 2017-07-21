package TwitterSentimentAnalysis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.apex.api.ControlAwareDefaultInputPort;
import org.apache.apex.api.operator.ControlTuple;

import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.serializers.JavaSerializer;

import com.datatorrent.ambarish.StanfordNLPApex.NLPToken;
import com.datatorrent.ambarish.StanfordNLPApex.StartOfInputControlTuple;
import com.datatorrent.api.AutoMetric;
import com.datatorrent.api.Context;
import com.datatorrent.common.util.BaseOperator;
import com.datatorrent.common.util.Pair;

/**
 * Created by ambarish on 13/7/17.
 */
public class TweetAnalyser extends BaseOperator
{

  @AutoMetric
  private int positiveTweets = 0;
  @AutoMetric
  private int negativeTweets = 0;
  @AutoMetric
  private int neutralTweets = 0;
  @AutoMetric
  private int veryPositiveTweets = 0;
  @AutoMetric
  private int veryNegativeTweets = 0;
  @FieldSerializer.Bind(JavaSerializer.class)
  @AutoMetric
  private Collection<Collection<Pair<String, Object>>> pieChart = new ArrayList<>();
  final LineMetrics lineMetrics = new LineMetrics();
  private long currentTweetId = 0;
  private String currentTweetText;
  private int currentTweetSentimentScore;
  private int curCount = 0;
  @AutoMetric
  private int sentiment = 0;
  public transient ControlAwareDefaultInputPort<NLPToken> input = new ControlAwareDefaultInputPort<NLPToken>()
  {
    public boolean processControl(ControlTuple controlTuple)
    {
      pieChart.clear();
      if (currentTweetId != 0) {
        try {
          float s = (float)(currentTweetSentimentScore / curCount);
          s += 0.5;
          int score = (int)Math.floor(s);
          sentiment = score;

        } catch (ArithmeticException e) {
          System.out.print("Tweet ID" + currentTweetId);
        }
        String sentimentString = "";
        if (sentiment == 1) {
          lineMetrics.sentiment = 1;
          sentimentString = "Negative";
          negativeTweets++;
        } else if (sentiment == 2) {
          lineMetrics.sentiment = 2;
          sentimentString = "Neutral";
          neutralTweets++;
        } else if (sentiment == 3) {
          lineMetrics.sentiment = 3;
          sentimentString = "Positive";
          positiveTweets++;
        } else if (sentiment == 4) {
          lineMetrics.sentiment = 4;
          sentimentString = "Very Positive";
          veryPositiveTweets++;
        } else if (sentiment == 0) {
          lineMetrics.sentiment = 0;
          sentimentString = "Very Negative";
          veryNegativeTweets++;
        }
        Collection<Pair<String, Object>> row = new ArrayList<>();
        row.add(new Pair<String, Object>("Name", "Positive"));
        row.add(new Pair<String, Object>("Value", positiveTweets));

        Collection<Pair<String, Object>> row1 = new ArrayList<>();
        row.add(new Pair<String, Object>("Name", "Very Positive"));
        row.add(new Pair<String, Object>("Value", veryPositiveTweets));

        Collection<Pair<String, Object>> row2 = new ArrayList<>();
        row.add(new Pair<String, Object>("Name", "Negative"));
        row.add(new Pair<String, Object>("Value", negativeTweets));

        Collection<Pair<String, Object>> row3 = new ArrayList<>();
        row.add(new Pair<String, Object>("Name", "Neutral"));
        row.add(new Pair<String, Object>("Value", neutralTweets));

        Collection<Pair<String, Object>> row4 = new ArrayList<>();
        row.add(new Pair<String, Object>("Name", "Very Negative"));
        row.add(new Pair<String, Object>("Value", veryNegativeTweets));

        pieChart.add(row);
        pieChart.add(row1);
        pieChart.add(row2);
        pieChart.add(row3);
        pieChart.add(row4);

        System.out.println(currentTweetId + " - " + currentTweetText + " - " + sentimentString);

      }
      lineMetrics.tweetId = ((StartOfInputControlTuple)controlTuple).id;
      currentTweetId = ((StartOfInputControlTuple)controlTuple).id;
      currentTweetText = ((StartOfInputControlTuple)controlTuple).inputString;
      currentTweetSentimentScore = 0;
      curCount = 0;
      return false;
    }

    @Override
    public void process(NLPToken nlpToken)
    {

      if (nlpToken.getId() == currentTweetId) {
        curCount++;
        if (nlpToken.getToken().equals("Positive")) {
          currentTweetSentimentScore += 3;
        } else if (nlpToken.getToken().equals("Negative")) {
          currentTweetSentimentScore += 1;
        } else if (nlpToken.getToken().equals("Neutral")) {
          currentTweetSentimentScore += 2;
        } else if (nlpToken.getToken().equals("Very Positive")) {
          currentTweetSentimentScore += 4;
        } else if (nlpToken.getToken().equals("Very Negative")) {
          currentTweetSentimentScore += 0;
        }

      }

    }
  };

  @Override
  public void setup(Context.OperatorContext context)
  {
  }

  public int getPositiveTweets()
  {
    return positiveTweets;
  }

  public void setPositiveTweets(int positiveTweets)
  {
    this.positiveTweets = positiveTweets;
  }

  public int getNegativeTweets()
  {
    return negativeTweets;
  }

  public void setNegativeTweets(int negativeTweets)
  {
    this.negativeTweets = negativeTweets;
  }

  public int getNeutralTweets()
  {
    return neutralTweets;
  }

  public void setNeutralTweets(int neutralTweets)
  {
    this.neutralTweets = neutralTweets;
  }

  public static class LineMetrics implements Serializable
  {
    long tweetId;
    long sentiment;

    private static final long serialVersionUID = 201511041908L;
  }
}
