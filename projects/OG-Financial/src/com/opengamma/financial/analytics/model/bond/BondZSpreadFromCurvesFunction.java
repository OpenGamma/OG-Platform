/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.interestrate.bond.method.BondSecurityDiscountingMethod;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class BondZSpreadFromCurvesFunction extends BondFromCurvesFunction {
  private static final BondSecurityDiscountingMethod CALCULATOR = BondSecurityDiscountingMethod.getInstance();
  
  public BondZSpreadFromCurvesFunction(final String currency, final String creditCurveName, final String riskFreeCurveName) {
    super(currency, creditCurveName, riskFreeCurveName);
  }
  
  public BondZSpreadFromCurvesFunction(final Currency currency, final String creditCurveName, final String riskFreeCurveName) {
    super(currency, creditCurveName, riskFreeCurveName);
  }
  
  @Override
  protected Set<ComputedValue> calculate(final BondFixedSecurity bond, final YieldCurveBundle data, final ComputationTarget target, final FunctionInputs inputs) {
    final Object cleanPriceObject = inputs.getValue(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, target.getSecurity().getUniqueId()));
    if (cleanPriceObject == null) {
      throw new OpenGammaRuntimeException("Clean price was null");      
    }
    double cleanPrice = (Double) cleanPriceObject;
    double zSpread = CALCULATOR.zSpreadFromCurvesAndClean(bond, data, cleanPrice);
    return Sets.newHashSet(new ComputedValue(getResultSpec(target), zSpread));
  }

  @Override
  protected ValueSpecification getResultSpec(final ComputationTarget target) {
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.CALCULATION_METHOD, FROM_CURVES_METHOD).get();
    return new ValueSpecification(ValueRequirementNames.Z_SPREAD, target.toSpecification(), properties);
  }
  
  @Override 
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    Set<ValueRequirement> result = super.getRequirements(context, target, desiredValue);
    result.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, target.getSecurity().getUniqueId()));
    return result;
  }
}
