/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.variance;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
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
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.time.TimeCalculator;

/**
 * 
 */
public class EquityForwardFromSpotAndYieldCurveFunction extends AbstractFunction.NonCompiledInvoker {
  /** String describing the method used to calculate the forward value of an equity spot rate */
  public static final String FORWARD_CALCULATION_METHOD = "ForwardCalculationMethod";
  /** String describing the calculation method used in this function */
  public static final String FORWARD_FROM_SPOT_AND_YIELD_CURVE = "ForwardFromSpotAndYieldCurve";
  private final String _curveDefinitionName;

  public EquityForwardFromSpotAndYieldCurveFunction(String curveDefinitionName) {
    Validate.notNull(curveDefinitionName);
    _curveDefinitionName = curveDefinitionName;
  }

  private static final String VALUE_REQUIREMENT = ValueRequirementNames.FORWARD;

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {

    // 1. Get the expiry _time_ from the trade
    EquityVarianceSwapSecurity security = (EquityVarianceSwapSecurity) target.getSecurity();
    double expiry = TimeCalculator.getTimeBetween(executionContext.getValuationClock().zonedDateTime(), security.getLastObservationDate());

    // ExternalId id = security.getSpotUnderlyingIdentifier();

    // 2. Get the discount curve and spot value
    Object discountObject = inputs.getValue(getDiscountRequirement(security));
    if (discountObject == null) {
      throw new OpenGammaRuntimeException("Could not get Discount Curve");
    }
    YieldAndDiscountCurve discountCurve = (YieldAndDiscountCurve) discountObject;

    Object spotObject = inputs.getValue(getSpotRequirement(security));
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Could not get Underlying's Spot value");
    }
    double spot = (Double) spotObject;

    // 3. Compute the forward
    final double discountFactor = discountCurve.getDiscountFactor(expiry);
    Validate.isTrue(discountFactor != 0, "The discount curve has returned a zero value for a discount bond. Check rates.");
    final double forward = spot / discountFactor;

    ValueSpecification valueSpec = getValueSpecification(security);
    return Collections.singleton(new ComputedValue(valueSpec, forward));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof EquityVarianceSwapSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    EquityVarianceSwapSecurity security = (EquityVarianceSwapSecurity) target.getSecurity();
    return Sets.newHashSet(getSpotRequirement(security), getDiscountRequirement(security));
  }

  // Note that createValueProperties is _not_ used - use will mean the engine can't find the requirement
  private ValueRequirement getDiscountRequirement(EquityVarianceSwapSecurity security) {
    ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, _curveDefinitionName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, security.getCurrency().getUniqueId(), properties);
  }

  private ValueRequirement getSpotRequirement(EquityVarianceSwapSecurity security) {
    ExternalId id = security.getSpotUnderlyingId();
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, id); 
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.singleton(getValueSpecification((EquityVarianceSwapSecurity) target.getSecurity()));
  }

  // Note that the properties are created using createValueProperties() - this sets the name of the function in the properties.
  // Not using this means that this function will not work
  private ValueSpecification getValueSpecification(EquityVarianceSwapSecurity security) {
    ValueProperties properties = createValueProperties().with(ValuePropertyNames.CURRENCY, security.getCurrency().getCode())
                                                        .with(FORWARD_CALCULATION_METHOD, FORWARD_FROM_SPOT_AND_YIELD_CURVE)
                                                        .get();
    ExternalId id = security.getSpotUnderlyingId();
    ValueRequirement requirement = new ValueRequirement(VALUE_REQUIREMENT, ComputationTargetType.PRIMITIVE, UniqueId.of(id.getScheme().getName(), id.getValue()));
    return new ValueSpecification(requirement, properties);
  }
}
