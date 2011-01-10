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
public class CAPMBetaModelPositionFunction extends CAPMBetaModelFunction {

  public CAPMBetaModelPositionFunction(final String returnCalculatorName, final String startDate) {
    super(returnCalculatorName, startDate);
  }

  @Override
  public Object getTarget(final ComputationTarget target) {
    return target.getPosition();
  }

  @Override
  public String getShortName() {
    return "CAPM_BetaPositionFunction";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.POSITION;
  }

}
