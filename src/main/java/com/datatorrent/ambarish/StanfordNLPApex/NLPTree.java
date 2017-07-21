package com.datatorrent.ambarish.StanfordNLPApex;

/**
 * Wrapper class for emiting parse tree of a sentence in the input string.
 */
public class NLPTree
{
  private long id;
  private String parseTree;

  public NLPTree(long id,String parseTree)
  {
    this.id = id;
    this.parseTree = parseTree;
  }

  public NLPTree()
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

  public String getParseTree()
  {
    return parseTree;
  }

  public void setParseTree(String parseTree)
  {
    this.parseTree = parseTree;
  }

  @Override
  public String toString()
  {
    return "NLPTree{" +
      "id=" + id +
      ", parseTree='" + parseTree + '\'' +
      '}';
  }
}
