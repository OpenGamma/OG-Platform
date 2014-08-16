/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.util.ArgumentChecker;

/**
 * Representation of an interest rate cap (floor) as a set of caplets (floorlets) - calls (puts) on an Ibor rate
 */
public class CapFloor extends Annuity<CapFloorIbor> {

  /**
   * @param payments The series of caplets or floorlets. <b>Note:</b> must be all caplets or all floorlets with same strike
   */
  public CapFloor(final CapFloorIbor[] payments) {
    super(payments);
    ArgumentChecker.notEmpty(payments, "no payments given");
    final double k = payments[0].getStrike();
    final boolean isCap = payments[0].isCap();
    final int n = payments.length;
    for (int i = 1; i < n; i++) {
      ArgumentChecker.isTrue(payments[i].getStrike() == k, "Different strikes");
      ArgumentChecker.isTrue(payments[i].isCap() == isCap, "Mixed caplets and floorlets");
    }
  }

  /**
   * get the cap strike
   * @return The cap strike
   */
  public double getStrike() {
    return getNthPayment(0).getStrike();
  }

  /**
   * is a cap
   * @return true if cap and false if floor 
   */
  public boolean isCap() {
    return getNthPayment(0).isCap();
  }

  /**
   * The start time is taken as the fixing period start of the first caplet/floorlet
   * @return The start time
   */
  public double getStartTime() {
    return getNthPayment(0).getFixingPeriodStartTime();
  }

  /**
   * The end time is taken as the fixing period end of the last caplet/floorlet
   * @return The end time
   */
  public double getEndTime() {
    return getNthPayment(getNumberOfPayments() - 1).getFixingPeriodEndTime();
  }

  /**
   * produce a new cap or floor identical to the original except with a new strike
   * @param strike the new strike
   * @return a new cap or floor
   */
  public CapFloor withStrike(final double strike) {
    ArgumentChecker.isTrue(strike >= 0, "negative strike");
    final CapFloorIbor[] payments = getPayments();
    final int n = getNumberOfPayments();
    final CapFloorIbor[] temp = new CapFloorIbor[n];
    for (int i = 0; i < n; i++) {
      temp[i] = payments[i].withStrike(strike);
    }
    return new CapFloor(temp);

  }

}
