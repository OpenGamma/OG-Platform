/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.futureoption;

import java.util.HashSet;
import java.util.Set;

import com.opengamma.analytics.financial.commodity.calculator.ComFutOptBjerksundStenslandGreekCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
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
public class CommodityFutureOptionBjerksundStenslandGreeksFunction extends CommodityFutureOptionBjerksundStenslandFunction {
  /** Value requirement names */
  private static final String[] GREEK_NAMES = new String[] {
    ValueRequirementNames.DELTA,
    ValueRequirementNames.DUAL_DELTA,
    ValueRequirementNames.RHO,
    ValueRequirementNames.CARRY_RHO,
    ValueRequirementNames.VEGA,
    ValueRequirementNames.THETA
  };
  /** Equivalent greeks */
  private static final Greek[] GREEKS = new Greek[] {
    Greek.DELTA,
    Greek.DUAL_DELTA,
    Greek.RHO,
    Greek.CARRY_RHO,
    Greek.VEGA,
    Greek.THETA
  };

  /**
   * Default constructor
   */
  public CommodityFutureOptionBjerksundStenslandGreeksFunction() {
    super(GREEK_NAMES);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return ((CommodityFutureOptionSecurity) target.getSecurity()).getExerciseType() instanceof AmericanExerciseType;
  }

  @Override
  protected boolean getFunctionIncludesCurrencyProperty() {
    return false;
  }
  
  @Override
  protected Set<ComputedValue> computeValues(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties) {
    final GreekResultCollection greeks = derivative.accept(ComFutOptBjerksundStenslandGreekCalculator.getInstance(), market);
    final Set<ComputedValue> result = new HashSet<>();
    for (int i = 0; i < GREEKS.length; i++) {
      final ValueSpecification spec = new ValueSpecification(GREEK_NAMES[i], targetSpec, resultProperties);
      final double greek = greeks.get(GREEKS[i]);
      result.add(new ComputedValue(spec, greek));
    }
    return result;
  }
}
