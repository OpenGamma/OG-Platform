/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.riskfactor.ValueGammaCalculator;
import com.opengamma.analytics.financial.riskfactor.ValueGreekCalculator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
public class ListedEquityOptionRollGeskeWhaleyValueGammaFunction extends ListedEquityOptionRollGeskeWhaleyFunction {

  /** Value gamma calculator */
  private static final ValueGreekCalculator CALCULATOR = ValueGammaCalculator.getInstance();

  /** Default constructor */
  public ListedEquityOptionRollGeskeWhaleyValueGammaFunction() {
    super(ValueRequirementNames.VALUE_GAMMA);
  }
  
  @Override
  protected Set<ComputedValue> computeValues(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties) {
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementNames()[0], targetSpec, resultProperties);
    final Object gammaObject = inputs.getValue(ValueRequirementNames.GAMMA);
    if (gammaObject == null) {
      throw new OpenGammaRuntimeException("Could not get gamma");
    }
    final double gamma = (Double) gammaObject;
    final double valueGamma = CALCULATOR.valueGreek(derivative, market, gamma);
    return Collections.singleton(new ComputedValue(resultSpec, valueGamma));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    if (requirements == null) {
      return null;
    }
    requirements.add(new ValueRequirement(ValueRequirementNames.GAMMA, target.toSpecification(), desiredValue.getConstraints()));
    return requirements;
  }

}
