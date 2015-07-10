/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginSecurityBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginTransactionBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionPremiumSecurityBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionPremiumTransactionBlackSurfaceMethod;
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
  /** Calculates gamma for margined future options */
  private static final InterestRateFutureOptionMarginTransactionBlackSurfaceMethod MARGINED_TRANSACTION_METHOD = InterestRateFutureOptionMarginTransactionBlackSurfaceMethod.getInstance();
  /** Calculates the underlying forward rate for margined future options */
  private static final InterestRateFutureOptionMarginSecurityBlackSurfaceMethod MARGINED_SECURITY_METHOD = InterestRateFutureOptionMarginSecurityBlackSurfaceMethod.getInstance();
  /** Calculates gamma for future options with a premium */
  private static final InterestRateFutureOptionPremiumTransactionBlackSurfaceMethod PREMIUM_TRANSACTION_METHOD = InterestRateFutureOptionPremiumTransactionBlackSurfaceMethod.getInstance();
  /** Calculates the underlying forward rate for future options with a premium */
  private static final InterestRateFutureOptionPremiumSecurityBlackSurfaceMethod PREMIUM_SECURITY_METHOD = InterestRateFutureOptionPremiumSecurityBlackSurfaceMethod.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#VALUE_GAMMA_P}
   */
  public InterestRateFutureOptionBlackValueGammaPFunction() {
    super(ValueRequirementNames.VALUE_GAMMA_P, true);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative derivative, final YieldCurveWithBlackCubeBundle data, final ValueSpecification spec, final Set<ValueRequirement> desiredValues) {
    final double gamma, spot;
    if (derivative instanceof InterestRateFutureOptionMarginTransaction) {
      final InterestRateFutureOptionMarginTransaction transaction = (InterestRateFutureOptionMarginTransaction) derivative;
      gamma = MARGINED_TRANSACTION_METHOD.presentValueGamma(transaction, data);
      spot = MARGINED_SECURITY_METHOD.underlyingFuturePrice(transaction.getUnderlyingSecurity(), data);
    } else if (derivative instanceof InterestRateFutureOptionPremiumTransaction) {
      final InterestRateFutureOptionPremiumTransaction transaction = (InterestRateFutureOptionPremiumTransaction) derivative;
      gamma = PREMIUM_TRANSACTION_METHOD.presentValueGamma(transaction, data);
      spot = PREMIUM_SECURITY_METHOD.underlyingFuturePrice(transaction.getUnderlyingSecurity(), data);
    } else {
      throw new OpenGammaRuntimeException("Could not handle derivatives of type " + derivative.getClass());
    }
    final Double valueGamma = gamma * spot;
    return Collections.singleton(new ComputedValue(spec, valueGamma / 2500000.0));
  }

}
