/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.futureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.commodity.calculator.ComFutOptBjerksundStenslandPresentValueCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;

/**
 *
 */
public class CommodityFutureOptionBjerksundStenslandPVFunction extends CommodityFutureOptionBjerksundStenslandFunction {

  /**
   * Default constructor
   */
  public CommodityFutureOptionBjerksundStenslandPVFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return ((CommodityFutureOptionSecurity) target.getSecurity()).getExerciseType() instanceof AmericanExerciseType;
  }

  @Override
  protected Set<ComputedValue> computeValues(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties) {
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementNames()[0], targetSpec, resultProperties);
    final Double pv = derivative.accept(ComFutOptBjerksundStenslandPresentValueCalculator.getInstance(), market);
    return Collections.singleton(new ComputedValue(resultSpec, pv));
  }
}
