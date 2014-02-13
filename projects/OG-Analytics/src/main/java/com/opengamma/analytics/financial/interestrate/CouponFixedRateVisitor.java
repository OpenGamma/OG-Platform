/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedCompounding;

/**
 *
 */
public class CouponFixedRateVisitor extends InstrumentDerivativeVisitorAdapter<Void, Double> {

  @Override
  public Double visitCouponFixed(final CouponFixed payment) {
    return payment.getFixedRate();
  }

  @Override
  public Double visitCouponFixedAccruedCompounding(final CouponFixedAccruedCompounding payment) {
    return payment.getFixedRate();
  }

  @Override
  public Double visitCouponFixedCompounding(final CouponFixedCompounding payment) {
    return payment.getFixedRate();
  }

}
