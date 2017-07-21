package TwitterSentimentAnalysis;//


import com.datatorrent.api.DefaultOutputPort;
import com.datatorrent.api.InputOperator;
import com.datatorrent.api.Context.OperatorContext;
import com.datatorrent.api.Operator.ActivationListener;
import java.util.concurrent.ArrayBlockingQueue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.apache.hadoop.classification.InterfaceStability.Evolving;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.HashtagEntity;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.URLEntity;
import twitter4j.conf.ConfigurationBuilder;

@Evolving
public class TwitterSampleInput implements InputOperator, ActivationListener<OperatorContext>, StatusListener {
  public final transient DefaultOutputPort<Status> status = new DefaultOutputPort();
  public final transient DefaultOutputPort<String> text = new DefaultOutputPort();
  public final transient DefaultOutputPort<String> url = new DefaultOutputPort();
  public final transient DefaultOutputPort<String> hashtag = new DefaultOutputPort();
  public final transient DefaultOutputPort<?> userMention = null;
  public final transient DefaultOutputPort<?> media = null;
  private boolean debug;
  private transient Thread operatorThread;
  private transient TwitterStream ts;
  private transient ArrayBlockingQueue<Status> statuses = new ArrayBlockingQueue(1048576);
  transient int count;
  private int feedMultiplier = 1;
  @Min(0L)
  private int feedMultiplierVariance = 0;
  @NotNull
  private String consumerKey;
  @NotNull
  private String consumerSecret;
  @NotNull
  private String accessToken;
  @NotNull
  private String accessTokenSecret;
  private boolean reConnect;
  private static final Logger logger = LoggerFactory.getLogger(TwitterSampleInput.class);
  private int pcount;
  public int getTuplesPerWindow()
  {
    return tuplesPerWindow;
  }

  public void setTuplesPerWindow(int tuplesPerWindow)
  {
    this.tuplesPerWindow = tuplesPerWindow;
  }

  private int tuplesPerWindow;
  public TwitterSampleInput() {
  }

  public void setup(OperatorContext context) {
    this.operatorThread = Thread.currentThread();
    if(this.feedMultiplier != 1) {
      logger.info("Load set to be {}% of the entire twitter feed", Integer.valueOf(this.feedMultiplier));
    }

    ConfigurationBuilder cb = new ConfigurationBuilder();
    cb.setDebugEnabled(this.debug).setOAuthConsumerKey(this.consumerKey).setOAuthConsumerSecret(this.consumerSecret).setOAuthAccessToken(this.accessToken).setOAuthAccessTokenSecret(this.accessTokenSecret);
    this.ts = (new TwitterStreamFactory(cb.build())).getInstance();
  }

  public void teardown() {
    this.ts = null;
  }

  public void onStatus(Status status) {
    int randomMultiplier = this.feedMultiplier;
    int min;
    if(this.feedMultiplierVariance > 0) {
      min = this.feedMultiplier - this.feedMultiplierVariance;
      if(min < 0) {
        min = 0;
      }

      int max = this.feedMultiplier + this.feedMultiplierVariance;
      randomMultiplier = min + (int)(Math.random() * (double)(max - min + 1));
    }

    try {
      for(min = randomMultiplier; min-- > 0; ++this.count) {
        this.statuses.put(status);
      }
    } catch (InterruptedException var5) {
      logger.debug("Streaming interrupted; Passing the inerruption to the operator", var5);
      this.operatorThread.interrupt();
    }

  }

  public void endWindow() {
    if(this.count % 16 == 0) {
      logger.debug("processed {} statuses", Integer.valueOf(this.count));
    }

  }

  public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
  }

  public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
  }

  public void onScrubGeo(long userId, long upToStatusId) {
  }

  public void onStallWarning(StallWarning stallWarning) {
  }

  public void onException(Exception ex) {
    logger.error("Sampling Error", ex);
    logger.debug("reconnect: {}", Boolean.valueOf(this.reConnect));
    this.ts.shutdown();
    if(this.reConnect) {
      try {
        Thread.sleep(1000L);
      } catch (Exception var3) {
        ;
      }

      this.setUpTwitterConnection();
    } else {
      this.operatorThread.interrupt();
    }

  }

  private void setUpTwitterConnection() {
    ConfigurationBuilder cb = new ConfigurationBuilder();
    cb.setDebugEnabled(this.debug).setOAuthConsumerKey(this.consumerKey).setOAuthConsumerSecret(this.consumerSecret).setOAuthAccessToken(this.accessToken).setOAuthAccessTokenSecret(this.accessTokenSecret);
    this.ts = (new TwitterStreamFactory(cb.build())).getInstance();
    this.ts.addListener(this);
    this.ts.sample();
  }

  public void beginWindow(long windowId) {
    pcount = 0;
  }

  public void activate(OperatorContext context) {
    this.ts.addListener(this);
    this.ts.sample();
  }

  public void deactivate() {
    this.ts.shutdown();
  }

  public void setFeedMultiplier(int multiplier) {
    this.feedMultiplier = multiplier;
  }

  public int getFeedMultiplier() {
    return this.feedMultiplier;
  }

  public void setFeedMultiplierVariance(int multiplierVariance) {
    this.feedMultiplierVariance = multiplierVariance;
  }

  public int getFeedMultiplierVariance() {
    return this.feedMultiplierVariance;
  }

  public void emitTuples() {
    if(pcount < tuplesPerWindow){
      int var1 = this.statuses.size();
      if(var1-- <= 0) {
            return;
          }
        Status s = (Status)this.statuses.poll();

        if(this.status.isConnected()) {
        this.status.emit(s);
      }

      if(this.text.isConnected()) {
        this.text.emit(s.getText());
      }
      pcount++;
    }

  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  public String getConsumerKey() {
    return this.consumerKey;
  }

  public void setConsumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
  }

  public String getConsumerSecret() {
    return this.consumerSecret;
  }

  public void setConsumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
  }

  public String getAccessToken() {
    return this.accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getAccessTokenSecret() {
    return this.accessTokenSecret;
  }

  public void setAccessTokenSecret(String accessTokenSecret) {
    this.accessTokenSecret = accessTokenSecret;
  }

  public boolean isReConnect() {
    return this.reConnect;
  }

  public void setReConnect(boolean reConnect) {
    this.reConnect = reConnect;
  }

  public int hashCode() {
    int hash = 7;
    hash = 11 * hash + (this.debug?1:0);
    hash = 11 * hash + this.feedMultiplier;
    hash = 11 * hash + this.feedMultiplierVariance;
    hash = 11 * hash + (this.consumerKey != null?this.consumerKey.hashCode():0);
    hash = 11 * hash + (this.consumerSecret != null?this.consumerSecret.hashCode():0);
    hash = 11 * hash + (this.accessToken != null?this.accessToken.hashCode():0);
    hash = 11 * hash + (this.accessTokenSecret != null?this.accessTokenSecret.hashCode():0);
    return hash;
  }

  public boolean equals(Object obj) {
    if(obj == null) {
      return false;
    } else if(this.getClass() != obj.getClass()) {
      return false;
    } else {
      TwitterSampleInput other = (TwitterSampleInput)obj;
      if(this.debug != other.debug) {
        return false;
      } else if(this.feedMultiplier != other.feedMultiplier) {
        return false;
      } else if(this.feedMultiplierVariance != other.feedMultiplierVariance) {
        return false;
      } else {
  label64: {
          if(this.consumerKey == null) {
            if(other.consumerKey == null) {
              break label64;
            }
          } else if(this.consumerKey.equals(other.consumerKey)) {
            break label64;
          }

          return false;
        }

  label57: {
          if(this.consumerSecret == null) {
            if(other.consumerSecret == null) {
              break label57;
            }
          } else if(this.consumerSecret.equals(other.consumerSecret)) {
            break label57;
          }

          return false;
        }

        if(this.accessToken == null) {
          if(other.accessToken != null) {
            return false;
          }
        } else if(!this.accessToken.equals(other.accessToken)) {
          return false;
        }

        if(this.accessTokenSecret == null) {
          if(other.accessTokenSecret != null) {
            return false;
          }
        } else if(!this.accessTokenSecret.equals(other.accessTokenSecret)) {
          return false;
        }

        return true;
      }
    }
  }

  public String toString() {
    return "TwitterSampleInput{debug=" + this.debug + ", feedMultiplier=" + this.feedMultiplier + ", feedMultiplierVariance=" + this.feedMultiplierVariance + ", consumerKey=" + this.consumerKey + ", consumerSecret=" + this.consumerSecret + ", accessToken=" + this.accessToken + ", accessTokenSecret=" + this.accessTokenSecret + '}';
  }
}
