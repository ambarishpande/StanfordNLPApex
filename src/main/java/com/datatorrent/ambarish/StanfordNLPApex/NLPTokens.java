package com.datatorrent.ambarish.StanfordNLPApex;

import java.util.List;

/**
 * Created by ambarish on 11/7/17.
 */
public class NLPTokens
{
  private String inputString;
  private List<String> tokens;

  public NLPTokens(String inputString, List<String> tokens)
  {
    this.inputString = inputString;
    this.tokens = tokens;
  }

  public NLPTokens()
  {
    this.inputString = null;
    this.tokens = null;
  }

  public String getInputString()
  {
    return inputString;
  }

  public void setInputString(String inputString)
  {
    this.inputString = inputString;
  }

  public List<String> getTokens()
  {
    return tokens;
  }

  public void setTokens(List<String> tokens)
  {
    this.tokens = tokens;
  }

  public String toString()
  {
    String s = inputString;
    s += " = ( ";
    for (int i = 0; i < tokens.size(); i++) {
      s += tokens.get(i) + ", ";
    }
    s += " ) ";
    return s;
  }
}
