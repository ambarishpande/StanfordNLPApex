package com.datatorrent.ambarish.StanfordNLPApex;

import java.util.List;

/**
 * Created by ambarish on 11/7/17.
 */
public class NLPTags
{
  private List<String> tokens;
  private List<String> tag;

  public NLPTags()
  {
    tokens = null;
    tag = null;

  }

  public NLPTags(List<String> tokens, List<String> tag)
  {
    this.tokens = tokens;
    this.tag = tag;
  }

  public List<String> getTokens()
  {
    return tokens;
  }

  public void setTokens(List<String> tokens)
  {
    this.tokens = tokens;
  }

  public List<String> getTag()
  {
    return tag;
  }

  public void setTag(List<String> tag)
  {
    this.tag = tag;
  }

  public String toString()
  {
    String s = "";
    s += "{ ";
    for (int i = 0; i < tokens.size(); i++) {
      s += " ( " + tokens.get(i) + " : " + tag.get(i) + " ),";
    }
    s += "} ";
    return s;
  }
}
