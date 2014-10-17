/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;

/**
 * Function implementation that returns a curve definition from the configured source.
 * <p>
 * If not available, the return Result will indicate the reason why.
 */
public class DefaultCurveDefinitionFn implements CurveDefinitionFn {

  //-------------------------------------------------------------------------
  @Override
  public Result<CurveDefinition> getCurveDefinition(String curveName) {
    try {
      return Result.success(ConfigLink.resolvable(curveName, CurveDefinition.class).resolve());
    } catch (DataNotFoundException ex) {
      return Result.failure(FailureStatus.MISSING_DATA, ex, "Could not get curve definition called {}", curveName);
    }
  }

  @Override
  public Result<Map<String, CurveDefinition>> getCurveDefinitions(Set<String> curveNames) {
    Map<String, CurveDefinition> curveDefinitions = new HashMap<>();
    Result<?> curveDefinitionResult = Result.success(true);
    for (String curveName : curveNames) {
      Result<CurveDefinition> curveDefinition = getCurveDefinition(curveName);
      if (curveDefinition.isSuccess()) {
        curveDefinitions.put(curveName, curveDefinition.getValue());
      } else {
        curveDefinitionResult = Result.failure(curveDefinitionResult, Result.failure(curveDefinition));
      }
    }
    if (!curveDefinitionResult.isSuccess()) {
      return Result.failure(curveDefinitionResult);
    }
    return Result.success(curveDefinitions);
  }

}

