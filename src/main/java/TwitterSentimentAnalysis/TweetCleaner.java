package TwitterSentimentAnalysis;


import com.vdurmont.emoji.EmojiParser;

import com.datatorrent.api.DefaultInputPort;
import com.datatorrent.api.DefaultOutputPort;
import com.datatorrent.common.util.BaseOperator;

import twitter4j.Status;

/**
 * Created by ambarish on 13/7/17.
 */
public class TweetCleaner extends BaseOperator
{

  public transient DefaultInputPort<Status> input = new DefaultInputPort<Status>()
  {
    @Override
    public void process(Status s)
    {

      if (s.getLang().equals("en")) {
        try {
          String text = s.getText();
          output.emit(stripTweet(text));

        } catch (Exception e) {
          e.printStackTrace();
        }

      }
    }
  };

  public transient DefaultOutputPort<String> output = new DefaultOutputPort<>();

  public String stripTweet(String text){
    String hashtagRegex = "^#\\w+|\\s#\\w+";
    String urlRegex = "http+://[\\S]+|https+://[\\S]+";
    String mentionRegex = "^@\\w+|\\s@\\w+";
    String retweet = "RT";


    text = text.replaceAll(hashtagRegex,"");
    text = text.replaceAll(urlRegex,"");
    text = text.replaceAll(mentionRegex,"");
    text = text.replaceAll(retweet,"");
    text = EmojiParser.removeAllEmojis(text);
    return text;

  }
}
