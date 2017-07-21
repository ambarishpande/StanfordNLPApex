package com.datatorrent.ambarish.StanfordNLPApex;

import org.apache.apex.api.ControlAwareDefaultInputPort;
import org.apache.apex.api.operator.ControlTuple;

import com.datatorrent.api.Context;
import com.datatorrent.api.Operator;
import com.datatorrent.common.util.BaseOperator;
import com.datatorrent.lib.io.ConsoleOutputOperator;

/**
 * Created by ambarish on 13/7/17.
 */
public class ControlAwareConsoleOutputOperator extends BaseOperator
{

  public final transient ControlAwareDefaultInputPort input = new ControlAwareDefaultInputPort()
  {
    @Override
    public boolean processControl(ControlTuple controlTuple)
    {
      System.out.println("End of Input Control Tuple Received for : " + controlTuple.toString());
      return false;
    }

    @Override
    public void process(Object o)
    {

      System.out.println(o.toString() + "\n");

    }
  };

  @Override
  public void setup(Context.OperatorContext context)
  {

  }

  @Override
  public void teardown()
  {

  }

  @Override
  public void beginWindow(long l)
  {

  }

  @Override
  public void endWindow()
  {

  }
}

