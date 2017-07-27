package EmailSpam;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

import com.datatorrent.api.AutoMetric;
import com.datatorrent.common.util.Pair;

/**
 * Created by ambarish on 26/7/17.
 */
public class ClassifierMetricsAggregator implements AutoMetric.Aggregator, Serializable
{
  Map<String, Object> result = Maps.newHashMap();

  @Override
  public Map<String, Object> aggregate(long l, Collection<AutoMetric.PhysicalMetricsContext> collection)
  {


    Collection<Collection<Pair<String, Object>>> ret = new ArrayList<>();
    Collection<Collection<Pair<String, Object>>> ret1 = new ArrayList<>();


    double accuracy = 0.0;

    for (AutoMetric.PhysicalMetricsContext pmc : collection) {
      for (Map.Entry<String, Object> metrics : pmc.getMetrics().entrySet()) {
        String key = metrics.getKey();
        Object value = metrics.getValue();
        switch (key) {
          case "modelPerformance":
            Collection<Collection<Pair<String, Object>>> temp  = (Collection<Collection<Pair<String, Object>>>)value;
            ret.addAll(temp);
            break;
          case "confusionMatrixVisual":
            Collection<Collection<Pair<String, Object>>> temp1  = (Collection<Collection<Pair<String, Object>>>)value;
            ret1.addAll(temp1);
            break;
          case "accuracy":
            accuracy = (double)value;
            break;
          }
      }
    }

    if (ret.size() > 0) {
      result.put("modelPerformance", ret);
    }
    if (ret1.size() > 0) {
      result.put("confusionMatrixVisual", ret1);
    }

    result.put("accuracy",accuracy);

    return result;
  }
}
