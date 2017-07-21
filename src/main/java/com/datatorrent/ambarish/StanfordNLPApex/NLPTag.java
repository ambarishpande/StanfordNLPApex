package com.datatorrent.ambarish.StanfordNLPApex;

/**
 * Wrapper class for emiting Tagged words from Annotators in Annotator Operator.
 */
public class NLPTag
{
  private long id;
  private String word;
  private String tag;

  public NLPTag(long id, String word, String tag)
  {
    this.id = id;
    this.word = word;
    this.tag = tag;
  }

  public NLPTag()
  {

  }

  public long getId()
  {
    return id;
  }

  public void setId(long id)
  {
    this.id = id;
  }

  public String getWord()
  {
    return word;
  }

  public void setWord(String word)
  {
    this.word = word;
  }

  public String getTag()
  {
    return tag;
  }

  public void setTag(String tag)
  {
    this.tag = tag;
  }

  @Override
  public String toString()
  {
    return "NLPTag{" +
      "id=" + id +
      ", word='" + word + '\'' +
      ", tag='" + tag + '\'' +
      '}';
  }
}
