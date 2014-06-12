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
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginTransactionBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionPremiumTransactionBlackSurfaceMethod;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Calculates the value theta ({@link ValueRequirementNames#VALUE_THETA}) of an interest rate future option.
 * The underlying Futures price is computed from the futures curve.
 * @deprecated The parent is deprecated
 */
@Deprecated
public class InterestRateFutureOptionBlackValueThetaFunction extends InterestRateFutureOptionBlackFunction {
  /** Calculates theta  */
  private static final InterestRateFutureOptionMarginTransactionBlackSurfaceMethod MARGINED_TRANSACTION_METHOD = InterestRateFutureOptionMarginTransactionBlackSurfaceMethod.getInstance();
  /** Calculates theta  */
  private static final InterestRateFutureOptionPremiumTransactionBlackSurfaceMethod PREMIUM_TRANSACTION_METHOD = InterestRateFutureOptionPremiumTransactionBlackSurfaceMethod.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#VALUE_THETA}
   */
  public InterestRateFutureOptionBlackValueThetaFunction() {
    super(ValueRequirementNames.VALUE_THETA, true);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative derivative, final YieldCurveWithBlackCubeBundle data, final ValueSpecification spec, final Set<ValueRequirement> desiredValues) {
    // Get scaling and adjust properties to reflect
    final double theta;
    if (derivative instanceof InterestRateFutureOptionMarginTransaction) {
      final InterestRateFutureOptionMarginTransaction transaction = (InterestRateFutureOptionMarginTransaction) derivative;
      theta = MARGINED_TRANSACTION_METHOD.theta(transaction, data);
    } else if (derivative instanceof InterestRateFutureOptionPremiumTransaction) {
      final InterestRateFutureOptionPremiumTransaction transaction = (InterestRateFutureOptionPremiumTransaction) derivative;
      theta = PREMIUM_TRANSACTION_METHOD.theta(transaction, data);
    } else {
      throw new OpenGammaRuntimeException("Could not handle derivatives of type " + derivative.getClass());
    }
    final Double valueTheta = theta / 365.25;
    return Collections.singleton(new ComputedValue(spec, valueTheta));
  }

}
