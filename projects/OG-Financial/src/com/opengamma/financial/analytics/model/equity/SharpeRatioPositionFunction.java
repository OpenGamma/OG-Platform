/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;

/**
 * 
 */
public class SharpeRatioPositionFunction extends SharpeRatioFunction {

  public SharpeRatioPositionFunction(final String returnCalculatorName, final String expectedReturnCalculatorName, final String standardDeviationCalculatorName,
      final String startDate) {
    super(returnCalculatorName, expectedReturnCalculatorName, standardDeviationCalculatorName, startDate);
  }

  @Override
  public Object getTarget(final ComputationTarget target) {
    return target.getPosition();
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.POSITION;
  }

  @Override
  public String getShortName() {
    return "SharpeRatioPositionFunction";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

}
