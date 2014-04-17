/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.variance.pricing.AffineDividends;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurveAffineDividends;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurveYieldImplied;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
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
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.money.Currency;

/**
 * Function produces a FORWARD_CURVE given YIELD_CURVE and Equity MARKET_VALUE Simple implementation does not include any Dividend treatment
 */
public class EquityForwardCurveFunction extends AbstractFunction.NonCompiledInvoker {

  private static final Set<ExternalScheme> s_validSchemes = ImmutableSet.of(ExternalSchemes.BLOOMBERG_TICKER, ExternalSchemes.BLOOMBERG_TICKER_WEAK, ExternalSchemes.ACTIVFEED_TICKER);

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD)
        .withAny(ValuePropertyNames.CURVE_CURRENCY)
        .withAny(ValuePropertyNames.CURVE)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(ValuePropertyNames.DIVIDEND_TYPE)
        .get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), properties));
  }

  // REVIEW Andrew 2012-01-17 -- Can we make the target type of this SECURITY, or even EQUITY_SECURITY ?

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getValue() instanceof ExternalIdentifiable) {
      final ExternalId identifier = ((ExternalIdentifiable) target.getValue()).getExternalId();
      return s_validSchemes.contains(identifier.getScheme());
    }
    return false;
  }

  @Override
  public boolean canHandleMissingInputs() {
    // dividend yield may not be available
    return true;
  }

  /* Spot value of the equity index or name */
  private ValueRequirement getSpotRequirement(final ComputationTarget target) {
    return (new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, target.toSpecification()));
  }

  /* Funding curve of the equity's currency */
  private ValueRequirement getFundingCurveRequirement(final Currency ccy, final String curveName, final String curveCalculationConfig) {
    final ValueProperties fundingProperties = ValueProperties.builder() // Note that createValueProperties is _not_ used - otherwise engine can't find the requirement
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
        .get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(ccy), fundingProperties);
  }

  @Override
  /* If a requirement is not found, return null, and go looking for a default */
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = new HashSet<>();
    final ValueProperties constraints = desiredValue.getConstraints();

    // Spot Requirement
    requirements.add(getSpotRequirement(target));

    // Funding Curve Currency
    final String ccyConstraint = constraints.getStrictValue(ValuePropertyNames.CURVE_CURRENCY);
    if (ccyConstraint == null) {
      return null;
    }
    final Currency currency = Currency.of(ccyConstraint);
    // Funding Curve Name
    final String fundingCurveName = constraints.getStrictValue(ValuePropertyNames.CURVE);
    if (fundingCurveName == null) {
      return null;
    }
    final String curveCalculationConfig = constraints.getStrictValue(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfig == null) {
      return null;
    }
    // Funding Curve Requirement
    requirements.add(getFundingCurveRequirement(currency, fundingCurveName, curveCalculationConfig));

    // Dividend Requirements depend on type
    final String dividendType = constraints.getStrictValue(ValuePropertyNames.DIVIDEND_TYPE);
    if (dividendType == null) {
      return null;
    }
    if (ValuePropertyNames.DIVIDEND_TYPE_DISCRETE.equalsIgnoreCase(dividendType)) {
      requirements.add(new ValueRequirement(ValueRequirementNames.AFFINE_DIVIDENDS, ComputationTargetType.PRIMITIVE, target.getUniqueId()));
    } else if (ValuePropertyNames.DIVIDEND_TYPE_CONTINUOUS.equalsIgnoreCase(dividendType)) {
      requirements.add(new ValueRequirement(MarketDataRequirementNames.DIVIDEND_YIELD, ComputationTargetType.PRIMITIVE, target.getUniqueId()));
    }
    return requirements;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {

    // desiredValues is defined by getResults. In our case, a singleton
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);

    // Spot
    final Double spot = (Double) inputs.getValue(MarketDataRequirementNames.MARKET_VALUE);
    if (spot == null) {
      throw new OpenGammaRuntimeException("Failed to get spot value requirement: " + target.getName());
    }
    final String curveCalculationConfig = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    // Curve Currency
    final String ccyName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CURRENCY);
    if (ccyName == null) {
      throw new OpenGammaRuntimeException("Failed to find " + ValuePropertyNames.CURVE_CURRENCY);
    }
    // Funding
    final String fundingCurveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final Object fundingCurveObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE);
    if (fundingCurveObject == null) {
      throw new OpenGammaRuntimeException("Failed to get funding curve requirement");
    }
    final YieldCurve fundingCurve = (YieldCurve) fundingCurveObject;
    // Dividend type: Discrete or Continuous 
    final String dividendType = desiredValue.getConstraint(ValuePropertyNames.DIVIDEND_TYPE);
    boolean isContinuousDividends = ValuePropertyNames.DIVIDEND_TYPE_CONTINUOUS.equalsIgnoreCase(dividendType);
    // Compute ForwardCurve
    final ForwardCurve forwardCurve;
    if (isContinuousDividends) {
      // Cost of Carry - if no dividend yield available set 0 cost of carry
      final Double dividendYieldObject = (Double) inputs.getValue(MarketDataRequirementNames.DIVIDEND_YIELD);
      final double dividendYield = dividendYieldObject == null ? 0.0 : dividendYieldObject.doubleValue();
      final YieldCurve costOfCarryCurve = YieldCurve.from(ConstantDoublesCurve.from(dividendYield, "CostOfCarry"));
      forwardCurve = new ForwardCurveYieldImplied(spot, fundingCurve, costOfCarryCurve);
    } else {
      Object discreteDividendsInput = inputs.getValue(ValueRequirementNames.AFFINE_DIVIDENDS);
      if ((discreteDividendsInput != null) && (discreteDividendsInput instanceof AffineDividends)) {
        final AffineDividends discreteDividends = (AffineDividends) discreteDividendsInput;
        forwardCurve = new ForwardCurveAffineDividends(spot, fundingCurve, discreteDividends);
      } else {
        forwardCurve = new ForwardCurveYieldImplied(spot, fundingCurve, YieldCurve.from(ConstantDoublesCurve.from(0.0, "CostOfCarry")));
      }
    }

    final ValueProperties properties = createValueProperties()
        .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD)
        .with(ValuePropertyNames.CURVE, fundingCurveName)
        .with(ValuePropertyNames.CURVE_CURRENCY, ccyName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
        .with(ValuePropertyNames.DIVIDEND_TYPE, dividendType)
        .get();

    final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(resultSpec, forwardCurve));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }
}
