/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.method.BondSecurityDiscountingMethod;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.BondSecurityConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.bond.BondSecurity;

/**
 *
 */
public class BondZSpreadFromMarketCleanPriceFunction extends BondFromPriceFunction {
  private static final BondSecurityDiscountingMethod CALCULATOR = BondSecurityDiscountingMethod.getInstance();

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties.Builder properties = getResultProperties();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.Z_SPREAD, target.toSpecification(), properties.get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> riskFreeCurves = desiredValue.getConstraints().getValues(BondFunction.PROPERTY_RISK_FREE_CURVE);
    if (riskFreeCurves == null || riskFreeCurves.size() != 1) {
      return null;
    }
    final Set<String> curves = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE);
    if (curves == null || curves.size() != 1) {
      return null;
    }
    final Set<String> creditCurves = desiredValue.getConstraints().getValues(BondFunction.PROPERTY_CREDIT_CURVE);
    if (creditCurves == null || creditCurves.size() != 1) {
      return null;
    }
    final String riskFreeCurveName = riskFreeCurves.iterator().next();
    final String curveName = curves.iterator().next();
    return Sets.newHashSet(getCurveRequirement(target, riskFreeCurveName), getCurveRequirement(target, curveName), getCleanPriceRequirement(target, desiredValue));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String curveName = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> input : inputs.entrySet()) {
      if (ValueRequirementNames.YIELD_CURVE.equals(input.getKey().getValueName())) {
        curveName = input.getKey().getProperty(ValuePropertyNames.CURVE);
      }
    }
    assert curveName != null;
    final String riskFreeCurveName = curveName;
    final String creditCurveName = riskFreeCurveName;
    final ValueProperties.Builder properties = getResultProperties(riskFreeCurveName, creditCurveName, curveName);
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.Z_SPREAD, target.toSpecification(), properties.get()));
  }

  @Override
  protected ValueRequirement getCleanPriceRequirement(final ComputationTarget target, final ValueRequirement desiredValue) {
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, target.toSpecification());
  }

  @Override
  protected String getCalculationMethodName() {
    return BondFunction.FROM_CLEAN_PRICE_METHOD;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.BOND_SECURITY;
  }

  @Override
  protected ValueProperties.Builder getResultProperties() {
    return createValueProperties()
        .withAny(BondFunction.PROPERTY_RISK_FREE_CURVE)
        .withAny(BondFunction.PROPERTY_CREDIT_CURVE)
        .withAny(ValuePropertyNames.CURVE)
        .with(ValuePropertyNames.CALCULATION_METHOD, getCalculationMethodName());
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final String riskFreeCurveName, final String creditCurveName, final String curveName) {
    return createValueProperties()
        .with(BondFunction.PROPERTY_RISK_FREE_CURVE, riskFreeCurveName)
        .with(BondFunction.PROPERTY_CREDIT_CURVE, creditCurveName)
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CALCULATION_METHOD, getCalculationMethodName());
  }

  @Override
  protected double getValue(final FunctionExecutionContext context, final ZonedDateTime date, final String riskFreeCurveName,
      final String creditCurveName, final ComputationTarget target, final YieldCurveBundle data, final double price) {
    final BondSecurity bond = (BondSecurity) target.getSecurity();
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(context);
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(context);
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(context);
    final BondSecurityConverter visitor = new BondSecurityConverter(holidaySource, conventionSource, regionSource);
    final BondFixedSecurityDefinition definition = (BondFixedSecurityDefinition) bond.accept(visitor);
    final BondFixedSecurity derivative = definition.toDerivative(date, riskFreeCurveName, creditCurveName);
    return 10000 * CALCULATOR.zSpreadFromCurvesAndClean(derivative, data, price);
  }

}
