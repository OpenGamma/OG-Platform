/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginSecurityBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginTransactionBlackSurfaceMethod;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.black.BlackDiscountingValueGammaIRFutureOptionFunction;

/**
 * Calculates the "ValueGamma" ({@link ValueRequirementNames#VALUE_GAMMA}) of an interest rate future option taking
 * the Black "Gamma" ({@link ValueRequirementNames#GAMMA}) as required input.
 * The underlying Futures price is computed from the futures curve.
 * @deprecated Use {@link BlackDiscountingValueGammaIRFutureOptionFunction}
 */
@Deprecated
public class InterestRateFutureOptionBlackValueGammaFunction extends InterestRateFutureOptionBlackFunction {
  /** The methods  */
  private static final InterestRateFutureOptionMarginTransactionBlackSurfaceMethod TRANSANCTION_METHOD = InterestRateFutureOptionMarginTransactionBlackSurfaceMethod.getInstance();
  private static final InterestRateFutureOptionMarginSecurityBlackSurfaceMethod SECURITY_METHOD = InterestRateFutureOptionMarginSecurityBlackSurfaceMethod.getInstance();

  public InterestRateFutureOptionBlackValueGammaFunction() {
    super(ValueRequirementNames.VALUE_GAMMA);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    // First, confirm Scale is set, by user or by default
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> scale = constraints.getValues(ValuePropertyNames.SCALE);
    if (scale == null || scale.size() != 1) {
      s_logger.info("Could not find {} requirement. Looking for a default..", ValuePropertyNames.SCALE);
      return null;
    }
    // Then get typical requirements
    return super.getRequirements(context, target, desiredValue);
  }
  
  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative derivative, final YieldCurveWithBlackCubeBundle data, final ValueSpecification spec, Set<ValueRequirement> desiredValues) {
    // Get scaling and adjust properties to reflect
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Set<String> scaleValue = desiredValue.getConstraints().getValues(ValuePropertyNames.SCALE);
    final Double scaleFactor;
    final String scaleProperty;
    if (scaleValue == null || scaleValue.isEmpty()) {
      scaleProperty = Double.toString(1);
      scaleFactor = 1.0;
    } else {
      scaleProperty = Iterables.getOnlyElement(scaleValue);
      scaleFactor = Double.parseDouble(scaleProperty);
    }
    ValueProperties properties = spec.getProperties().copy().withoutAny(ValuePropertyNames.SCALE).with(ValuePropertyNames.SCALE, scaleProperty).get();
    ValueSpecification specWithScale = new ValueSpecification(spec.getValueName(), spec.getTargetSpecification(), properties);  
    
    Double valueGamma = null;
    if (derivative instanceof InterestRateFutureOptionMarginTransaction) {
      final InterestRateFutureOptionMarginTransaction  transaction = (InterestRateFutureOptionMarginTransaction) derivative;
      final double gamma = TRANSANCTION_METHOD.presentValueGamma(transaction, data);
      final double spot = SECURITY_METHOD.underlyingFuturePrice(transaction.getUnderlyingOption(), data);
      valueGamma = 0.5 * spot * spot * gamma * scaleFactor * scaleFactor;
    } else { 
      s_logger.error("Unexpected security type! {}. Examine converter", derivative.getClass());
    }
    return Collections.singleton(new ComputedValue(specWithScale, valueGamma));
  }
  
  @Override
  protected ValueProperties.Builder getResultProperties(final String currency) {
    return super.getResultProperties(currency)
        .withAny(ValuePropertyNames.SCALE);
  }

  private static final Logger s_logger = LoggerFactory.getLogger(InterestRateFutureOptionBlackValueGammaFunction.class);
}
