/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.swap;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;

/**
 * 
 */
public class SwapPaymentCalculator {

  public static double[] getFixedPayments(final FixedInterestRateLeg leg, final double[] paymentTimes, final boolean payFixed) {
    Validate.notNull(leg);
    Validate.notNull(paymentTimes);
    if (paymentTimes.length == 0) {
      throw new IllegalArgumentException("Must have at least one payment");
    }
    final int n = paymentTimes.length;
    final double[] result = new double[n];
    final double amount = (payFixed ? -1 : 1) * leg.getRate() * ((InterestRateNotional) leg.getNotional()).getAmount();
    result[0] = paymentTimes[0] * amount;
    for (int i = 1; i < paymentTimes.length; i++) {
      result[i] = amount * (paymentTimes[i] - paymentTimes[i - 1]); //TODO this might not work for some of the crazier daycount conventions
    }
    return result;
  }

  public static double getFirstFloatPayment(final FloatingInterestRateLeg leg, final double[] paymentTimes, final boolean payFixed) {
    Validate.notNull(leg);
    Validate.notNull(paymentTimes);
    if (paymentTimes.length == 0) {
      throw new IllegalArgumentException("Must have at least one payment");
    }
    final double amount = (payFixed ? 1 : -1) * leg.getInitialFloatingRate() * ((InterestRateNotional) leg.getNotional()).getAmount();
    return paymentTimes[0] * amount;
  }
}
