/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isda.cdsoption;

import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues;

/**
 * 
 */
public abstract class ISDACreditDefaultSwapOptionIR01Function extends ISDACreditDefaultSwapOptionFunction {

  public ISDACreditDefaultSwapOptionIR01Function(final String... valueRequirements) {
    super(valueRequirements);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    if (requirements == null) {
      return null;
    }
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> yieldCurveBumps = constraints.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_INTEREST_RATE_CURVE_BUMP);
    if (yieldCurveBumps == null || yieldCurveBumps.size() != 1) {
      return null;
    }
    final Set<String> yieldCurveBumpTypes = constraints.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_INTEREST_RATE_BUMP_TYPE);
    if (yieldCurveBumpTypes == null || yieldCurveBumpTypes.size() != 1) {
      return null;
    }
    return requirements;
  }

  @Override
  protected ValueProperties.Builder getCommonResultProperties() {
    return createValueProperties()
        .withAny(CreditInstrumentPropertyNamesAndValues.PROPERTY_INTEREST_RATE_CURVE_BUMP)
        .withAny(CreditInstrumentPropertyNamesAndValues.PROPERTY_INTEREST_RATE_BUMP_TYPE);
  }

  @Override
  protected boolean labelResultWithCurrency() {
    return true;
  }

}
