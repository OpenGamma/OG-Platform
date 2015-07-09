/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.function.scenarios.curvedata;

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
 *
 * @deprecated use the new scenario framework
 */
@Deprecated
public class CurveDataShiftDecorator
    implements CurveSpecificationMarketDataFn, ScenarioFunction<CurveDataShift, CurveDataShiftDecorator> {

  /** The underlying function that this function decorates. */
  private final CurveSpecificationMarketDataFn _delegate;

  /**
   * @param delegate the function to decorate
   */
  public CurveDataShiftDecorator(CurveSpecificationMarketDataFn delegate) {
    _delegate = ArgumentChecker.notNull(delegate, "delegate");
  }

  @Override
  public Result<Map<ExternalIdBundle, Double>> requestData(Environment env, CurveSpecification curveSpecification) {
    Result<Map<ExternalIdBundle, Double>> result = _delegate.requestData(env, curveSpecification);

    if (!result.isSuccess()) {
      return result;
    }
    Map<ExternalIdBundle, Double> results = result.getValue();
    List<CurveDataShift> shifts = env.getScenarioArguments(this);

    for (CurveDataShift shift : shifts) {
      results = shift.apply(curveSpecification, results);
    }
    return Result.success(results);
  }

  @Override
  public Class<CurveDataShift> getArgumentType() {
    return CurveDataShift.class;
  }
}
