/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.function.scenarios.curvedata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.sesame.CurveSpecificationMarketDataFn;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.function.scenarios.ScenarioFunction;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;

/**
 * Function that decorates {@link CurveSpecificationMarketDataFn} and applies shifts to the underlying data.
 * The points to shift are specified by tenor.
 */
public class CurveDataPointShiftsDecorator
    implements CurveSpecificationMarketDataFn, ScenarioFunction<CurveDataPointShifts> {

  /** The underlying function that this function decorates. */
  private final CurveSpecificationMarketDataFn _delegate;

  /**
   * @param delegate the function to decorate
   */
  public CurveDataPointShiftsDecorator(CurveSpecificationMarketDataFn delegate) {
    _delegate = ArgumentChecker.notNull(delegate, "delegate");
  }

  @Override
  public Result<Map<ExternalIdBundle, Double>> requestData(Environment env, CurveSpecification curveSpecification) {
    Result<Map<ExternalIdBundle, Double>> underlyingData = _delegate.requestData(env, curveSpecification);

    if (!underlyingData.isSuccess()) {
      return underlyingData;
    }
    List<CurveDataPointShifts> shifts = env.getScenarioArguments(this);
    Result<Map<ExternalIdBundle, Double>> results = underlyingData;
    List<Result<?>> failures = new ArrayList<>();

    for (CurveDataPointShifts shift : shifts) {
      results = shift.apply(curveSpecification, results.getValue());

      if (!results.isSuccess()) {
        failures.add(results);
      }
    }
    if (Result.anyFailures(failures)) {
      return Result.failure(failures);
    } else {
      return results;
    }
  }

  @Override
  public Class<CurveDataPointShifts> getArgumentType() {
    return CurveDataPointShifts.class;
  }
}
