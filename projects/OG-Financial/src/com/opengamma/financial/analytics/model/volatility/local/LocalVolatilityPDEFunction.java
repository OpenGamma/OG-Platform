/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local;

import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.BACKWARDS;
import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.FORWARDS;
import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.PROPERTY_NUMBER_SPACE_STEPS;
import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.PROPERTY_NUMBER_TIME_STEPS;
import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.PROPERTY_PDE_DIRECTION;
import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.PROPERTY_SPACE_STEPS_BUNCHING;
import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.PROPERTY_THETA;
import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.PROPERTY_TIME_STEP_BUNCHING;

import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;

/**
 *
 */
public abstract class LocalVolatilityPDEFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final double theta = Double.parseDouble(desiredValue.getConstraint(PROPERTY_THETA));
    final int nTimeSteps = Integer.parseInt(desiredValue.getConstraint(PROPERTY_NUMBER_TIME_STEPS));
    final int nSpaceSteps = Integer.parseInt(desiredValue.getConstraint(PROPERTY_NUMBER_SPACE_STEPS));
    final double timeStepBunching = Double.parseDouble(desiredValue.getConstraint(PROPERTY_TIME_STEP_BUNCHING));
    final double spaceStepBunching = Double.parseDouble(desiredValue.getConstraint(PROPERTY_SPACE_STEPS_BUNCHING));
    final String direction = desiredValue.getConstraint(PROPERTY_PDE_DIRECTION);
    if (direction.equals(FORWARDS)) {

    } else if (direction.equals(BACKWARDS)) {

    } else {
      throw new OpenGammaRuntimeException("Can only run PDE solver forwards or backwards; asked for " + direction);
    }
    return null;
  }


}
