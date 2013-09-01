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

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginSecurityBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginTransactionBlackSurfaceMethod;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.engine.value.ComputedValue;
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
  protected Set<ComputedValue> getResult(final InstrumentDerivative derivative, final YieldCurveWithBlackCubeBundle data, final ValueSpecification spec) {
    Double valueGamma = null;
    if (derivative instanceof InterestRateFutureOptionMarginTransaction) {
      final InterestRateFutureOptionMarginTransaction  transaction = (InterestRateFutureOptionMarginTransaction) derivative;
      final double gamma = TRANSANCTION_METHOD.presentValueGamma(transaction, data);
      final double spot = SECURITY_METHOD.underlyingFuturePrice(transaction.getUnderlyingOption(), data);
      valueGamma = 0.5 * spot * spot * gamma;
    } else {
      s_logger.error("Unexpected security type! {}", derivative.getClass());
    }
    return Collections.singleton(new ComputedValue(spec, valueGamma));
  }

  private static final Logger s_logger = LoggerFactory.getLogger(InterestRateFutureOptionBlackValueGammaFunction.class);
}
