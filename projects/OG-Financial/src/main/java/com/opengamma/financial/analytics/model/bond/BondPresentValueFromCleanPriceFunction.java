/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.method.BondTransactionDiscountingMethod;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.BondSecurityConverter;
import com.opengamma.financial.analytics.conversion.BondTradeConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.util.money.Currency;

/**
 * Bond present value from a quoted clean price.
 */
public class BondPresentValueFromCleanPriceFunction extends BondFromPriceFunction {

  /**
   * The method used to compute the present value result.
   */
  private static final BondTransactionDiscountingMethod CALCULATOR = BondTransactionDiscountingMethod.getInstance();

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ZonedDateTime date = ZonedDateTime.now(executionContext.getValuationClock());
    if (desiredValues.size() != 1) {
      throw new OpenGammaRuntimeException("This function " + getShortName() + " only provides a single output");
    }
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String riskFreeCurveName = desiredValue.getConstraint(BondFunction.PROPERTY_RISK_FREE_CURVE);
    final Object riskFreeCurveObject = inputs.getValue(getCurveRequirement(target, riskFreeCurveName));
    if (riskFreeCurveObject == null) {
      throw new OpenGammaRuntimeException("Risk free curve was null");
    }
    final Object cleanPriceObject = inputs.getValue(getCleanPriceRequirement(target, desiredValue));
    if (cleanPriceObject == null) {
      throw new OpenGammaRuntimeException("Could not get clean price requirement");
    }
    final Double cleanPrice = (Double) cleanPriceObject;
    final String creditCurveName = riskFreeCurveName;
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    final ValueProperties.Builder properties = getResultProperties(riskFreeCurveName, creditCurveName, currency);
    final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), properties.get());
    final YieldAndDiscountCurve riskFreeCurve = (YieldAndDiscountCurve) riskFreeCurveObject;
    final YieldCurveBundle data = new YieldCurveBundle(new String[] {riskFreeCurveName, riskFreeCurveName }, new YieldAndDiscountCurve[] {riskFreeCurve, riskFreeCurve });
    return Sets.newHashSet(new ComputedValue(resultSpec, getValue(executionContext, date, riskFreeCurveName, creditCurveName, target, data, cleanPrice)));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties.Builder properties = getResultProperties(FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()));
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), properties.get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> riskFreeCurves = desiredValue.getConstraints().getValues(BondFunction.PROPERTY_RISK_FREE_CURVE);
    if (riskFreeCurves == null || riskFreeCurves.size() != 1) {
      return null;
    }
    final Set<String> creditCurves = desiredValue.getConstraints().getValues(BondFunction.PROPERTY_CREDIT_CURVE);
    if (creditCurves == null || creditCurves.size() != 1) {
      return null;
    }
    final String riskFreeCurveName = riskFreeCurves.iterator().next();
    return Sets.newHashSet(getCurveRequirement(target, riskFreeCurveName), getCleanPriceRequirement(target, desiredValue));
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
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    final ValueProperties.Builder properties = getResultProperties(riskFreeCurveName, creditCurveName, currency);
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), properties.get()));
  }

  @Override
  protected ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    final ValueProperties.Builder properties = ValueProperties.with(ValuePropertyNames.CURVE, curveName);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(currency), properties.get());
  }

  @Override
  protected ValueRequirement getCleanPriceRequirement(final ComputationTarget target, final ValueRequirement desiredValue) {
    final Trade trade = target.getTrade();
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, trade.getSecurity().getUniqueId(), ValueProperties.builder().get());
  }

  @Override
  protected String getCalculationMethodName() {
    return BondFunction.FROM_CLEAN_PRICE_METHOD;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Trade trade = target.getTrade();
    return trade.getSecurity() instanceof BondSecurity;
  }

  @Override
  protected ValueProperties.Builder getResultProperties() {
    throw new UnsupportedOperationException();
  }

  protected ValueProperties.Builder getResultProperties(final Currency currency) {
    return createValueProperties()
        .withAny(BondFunction.PROPERTY_RISK_FREE_CURVE)
        .withAny(BondFunction.PROPERTY_CREDIT_CURVE)
        .with(ValuePropertyNames.CURRENCY, currency.getCode())
        .with(ValuePropertyNames.CALCULATION_METHOD, getCalculationMethodName());
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final String riskFreeCurveName, final String creditCurveName, final String curveName) {
    throw new UnsupportedOperationException();
  }

  protected ValueProperties.Builder getResultProperties(final String riskFreeCurveName, final String creditCurveName, final Currency currency) {
    return createValueProperties()
        .with(BondFunction.PROPERTY_RISK_FREE_CURVE, riskFreeCurveName)
        .with(BondFunction.PROPERTY_CREDIT_CURVE, creditCurveName)
        .with(ValuePropertyNames.CURRENCY, currency.getCode())
        .with(ValuePropertyNames.CALCULATION_METHOD, getCalculationMethodName());
  }

  @Override
  protected double getValue(final FunctionExecutionContext context, final ZonedDateTime date, final String riskFreeCurveName, final String creditCurveName, final ComputationTarget target,
      final YieldCurveBundle data, final double price) {
    final Trade trade = target.getTrade();
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(context);
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(context);
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(context);
    final BondTradeConverter visitor = new BondTradeConverter(new BondSecurityConverter(holidaySource, conventionSource, regionSource));
    final BondFixedTransactionDefinition definition = visitor.convert(trade);
    final BondFixedTransaction derivative = definition.toDerivative(date, riskFreeCurveName, creditCurveName);
    return CALCULATOR.presentValueFromCleanPrice(derivative, data, price);
  }

}
