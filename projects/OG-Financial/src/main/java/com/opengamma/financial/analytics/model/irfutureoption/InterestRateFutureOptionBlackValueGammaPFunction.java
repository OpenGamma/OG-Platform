/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginSecurityBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginTransactionBlackSurfaceMethod;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.black.BlackDiscountingValueGammaIRFutureOptionFunction;

/**
 * Calculates the "ValueGammaP" ({@link ValueRequirementNames#VALUE_GAMMA_P}) of an interest rate future option.
 * The underlying Futures price is computed from the futures curve.
 * @deprecated Use {@link BlackDiscountingValueGammaIRFutureOptionFunction}
 */
@Deprecated
public class InterestRateFutureOptionBlackValueGammaPFunction extends InterestRateFutureOptionBlackFunction {
  /** Calculates gamma  */
  private static final InterestRateFutureOptionMarginTransactionBlackSurfaceMethod TRANSACTION_METHOD = InterestRateFutureOptionMarginTransactionBlackSurfaceMethod.getInstance();
  /** Calculates the underlying forward rate */
  private static final InterestRateFutureOptionMarginSecurityBlackSurfaceMethod SECURITY_METHOD = InterestRateFutureOptionMarginSecurityBlackSurfaceMethod.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#VALUE_GAMMA_P}
   */
  public InterestRateFutureOptionBlackValueGammaPFunction() {
    super(ValueRequirementNames.VALUE_GAMMA_P);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative derivative, final YieldCurveWithBlackCubeBundle data, final ValueSpecification spec, final Set<ValueRequirement> desiredValues) {
    // Get scaling and adjust properties to reflect
    final InterestRateFutureOptionMarginTransaction  transaction = (InterestRateFutureOptionMarginTransaction) derivative;
    final double gamma = TRANSACTION_METHOD.presentValueGamma(transaction, data);
    final double spot = SECURITY_METHOD.underlyingFuturePrice(transaction.getUnderlyingOption(), data);
    final Double valueGamma = gamma * spot;
    return Collections.singleton(new ComputedValue(spec, valueGamma / 2500000.0));
  }

}
