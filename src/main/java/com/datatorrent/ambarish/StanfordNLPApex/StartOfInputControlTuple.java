package com.datatorrent.ambarish.StanfordNLPApex;

import org.apache.apex.api.operator.ControlTuple;

/**
 * Control tuple to indicate start of an input string in Annotator Operator.
 */
public class StartOfInputControlTuple implements ControlTuple
{

  public long id;
  public String inputString;
  public boolean immediate;

  public StartOfInputControlTuple()
  {
    id = 0;
  }

  public StartOfInputControlTuple(long id, String inputString, boolean immediate)
  {
    this.id = id;
    this.inputString = inputString;
    this.immediate = immediate;
  }

  @Override
  public DeliveryType getDeliveryType()
  {
    if (immediate) {
      return DeliveryType.IMMEDIATE;
    } else {
      return DeliveryType.END_WINDOW;
    }
  }

  @Override
  public String toString()
  {
    return "StartOfInputControlTuple{" +
      "id=" + id +
      ", inputString='" + inputString + '\'' +
      ", immediate=" + immediate +
      '}';
  }
}
