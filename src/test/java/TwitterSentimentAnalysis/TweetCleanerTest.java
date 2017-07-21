package TwitterSentimentAnalysis;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by ambarish on 18/7/17.
 */
public class TweetCleanerTest
{

  @Test
  public void testStripTweet(){
    String text = "@hello ekasnd #asdas \u1F63 blah blah";
    TweetCleaner clean = new TweetCleaner();
    System.out.println(clean.stripTweet(text));

  }
}