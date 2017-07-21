package com.datatorrent.ambarish.StanfordNLPApex;

/**
 * Wrapper class for emiting tokens of the input string from Annotators in Annotator Operator.
 */
public class NLPToken
{
  private long id;
  private String token;

  public NLPToken()
  {

  }

  public NLPToken(long id, String token)
  {
    this.id = id;
    this.token = token;
  }

  public long getId()
  {
    return id;
  }

  public void setId(long id)
  {
    this.id = id;
  }

  public String getToken()
  {
    return token;
  }

  public void setToken(String token)
  {
    this.token = token;
  }

  @Override
  public String toString()
  {
    return "NLPToken{" +
      "id=" + id +
      ", token='" + token + '\'' +
      '}';
  }
}
