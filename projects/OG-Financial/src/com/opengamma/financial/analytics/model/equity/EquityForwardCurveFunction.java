/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurveYieldImplied;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.util.money.Currency;

/**
 * Function produces a FORWARD_CURVE given YIELD_CURVE and Equity MARKET_VALUE
 * Simple implementation does not include any Dividend treatment
 */
public class EquityForwardCurveFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(EquityForwardCurveFunction.class);

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD)
        .withAny(ValuePropertyNames.CURVE_CURRENCY)
        .withAny(ValuePropertyNames.CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
        .get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), properties));
  }

  @Override
  /** Expected Target is a BLOOMBERG_TICKER, e.g. DJX Index */
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != getTargetType()) {
      return false;
    }
    if (target.getUniqueId() == null) {
      s_logger.error("Target unique id was null; {}", target);
      return false;
    }
    final String targetScheme = target.getUniqueId().getScheme();
    return (targetScheme.equalsIgnoreCase(ExternalSchemes.BLOOMBERG_TICKER.getName()) ||
        targetScheme.equalsIgnoreCase(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName()));
  }

  /** Spot value of the equity index or name */
  private ValueRequirement getSpotRequirement(final ComputationTarget target) {
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, target.getUniqueId());
  }

  /** Funding curve of the equity's currency */
  private ValueRequirement getFundingCurveRequirement(final Currency ccy, final String curveName) {
    final ValueProperties fundingProperties = ValueProperties.builder()  // Note that createValueProperties is _not_ used - otherwise engine can't find the requirement
        .with(ValuePropertyNames.CURVE, curveName)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD)
        .get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE,
        ccy.getUniqueId(), fundingProperties);
  }

  @Override
  /** If a requirement is not found, return null, and go looking for a default TODO */
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    final ValueProperties constraints = desiredValue.getConstraints();

    // Spot Requirement
    requirements.add(getSpotRequirement(target));

    // Check for forwardCurveName for efficiency, though it isn't used until execute method
    final Set<String> forwardCurveName = constraints.getValues(ValuePropertyNames.CURVE);
    if (forwardCurveName == null || forwardCurveName.size() != 1) {
      return null;
    }

    // Funding Curve Currency
    final Set<String> ccyConstraint = constraints.getValues(ValuePropertyNames.CURVE_CURRENCY);
    if (ccyConstraint == null || ccyConstraint.size() != 1) {
      return null;
    }
    final Currency currency = Currency.of(ccyConstraint.iterator().next());
    // Funding Curve Name
    final Set<String> fundingCurveNameSet = constraints.getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    if (fundingCurveNameSet  == null || fundingCurveNameSet.size() != 1) {
      return null;
    }
    final String fundingCurveName = fundingCurveNameSet.iterator().next();
    // Funding Curve Requirement
    requirements.add(getFundingCurveRequirement(currency, fundingCurveName));

    return requirements;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {

    // desiredValues is defined by getResults. In our case, a singleton
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String forwardCurveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String ccyName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CURRENCY);
    final Currency currency = Currency.of(ccyName);
    final String fundingCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE);

    // Spot
    final Double spot = (Double) inputs.getValue(getSpotRequirement(target));
    if (spot == null) {
      throw new OpenGammaRuntimeException("Failed to get spot value requirement");
    }

    // Funding
    final Object fundingCurveObject = inputs.getValue(YieldCurveFunction.getCurveRequirement(currency, fundingCurveName, null, null));
    if (fundingCurveObject == null) {
      throw new OpenGammaRuntimeException("Failed to get funding curve requirement");
    }
    final YieldCurve fundingCurve = (YieldCurve) fundingCurveObject;
    // Cost of Carry TODO Dividend treatment
    final YieldCurve zeroCostOfCarryCurve = YieldCurve.from(ConstantDoublesCurve.from(0.0, "CostOfCarry"));



    // Compute ForwardCurve
    final ForwardCurveYieldImplied forwardCurve = new ForwardCurveYieldImplied(spot, fundingCurve, zeroCostOfCarryCurve);

    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURVE, forwardCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
        .with(ValuePropertyNames.CURVE_CURRENCY, ccyName)
        .get();

    final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(resultSpec, forwardCurve));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }






}
