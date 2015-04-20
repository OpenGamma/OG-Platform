package com.opengamma.sesame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;

/**
 * Implementation of a curve labelling function that uses
 * a curve definition function to provide the labels.
 */
public class CurveDefinitionCurveLabellingFn implements CurveLabellingFn {

  /**
   * Curve definition function used to derive the labels for a curve.
   */
  private final CurveDefinitionFn _curveDefinitionFn;

  /**
   * Create the function using the supplied {@code CurveDefinitionFn}.
   *
   * @param curveDefinitionFn  curve definition function used to
   *   derive the labels for a curve
   */
  public CurveDefinitionCurveLabellingFn(CurveDefinitionFn curveDefinitionFn) {
    _curveDefinitionFn = ArgumentChecker.notNull(curveDefinitionFn, "curveDefinitionFn");
  }

  @Override
  public Result<Map<String, CurveMatrixLabeller>> getCurveLabellers(Set<String> curveNames) {

    Result<Map<String, CurveDefinition>> result = _curveDefinitionFn.getCurveDefinitions(curveNames);
    if (result.isSuccess()) {
      return Result.success(createLabellers(result.getValue()));
    } else {
      return Result.failure(result);
    }
  }

  private Map<String, CurveMatrixLabeller> createLabellers(Map<String, CurveDefinition> curveDefinitions) {
    Map<String, CurveMatrixLabeller> labellers = new HashMap<>();
    for (Map.Entry<String, CurveDefinition> entry : curveDefinitions.entrySet()) {
      labellers.put(entry.getKey(), createLabeller(entry.getValue()));
    }
    return ImmutableMap.copyOf(labellers);
  }

  private CurveMatrixLabeller createLabeller(CurveDefinition definition) {

    List<String> labels = new ArrayList<>();
    for (CurveNode node : definition.getNodes()) {
      String name = node.getName();
      if (name == null) {
        name = node.getClass().getSimpleName() + " " + node.getResolvedMaturity().toFormattedString();
      }
      labels.add(name);
    }

    return new CurveMatrixLabeller(labels);
  }
}
