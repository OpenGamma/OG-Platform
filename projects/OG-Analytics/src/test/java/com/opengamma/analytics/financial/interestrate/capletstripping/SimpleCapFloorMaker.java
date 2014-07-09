/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import org.threeten.bp.Period;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class SimpleCapFloorMaker {

  /**
   * A simplistic (i.e. for testing) method for making a cap/floor. The simplified assumptions are:<ul>
   * <li>the day-count is ACT/365.25</li>
   * <li>no calendar conventions are used (each caplet covers exactly the same amount of time)</li>
   * <li>the fixing period end of one caplet equals the the fixing period start of the next (i.e. spanning forwards)</li>
   * <li> the payment time equals the fixing period end</li>
   * <li> the payment and fixing year fraction are equal (and the same for each caplet)</li>
   * </ul>
   * @param ccy The payment currency.
   * @param index The Ibor-like index on which the coupon fixes.
   * @param start Integer for the start of the cap. A spot starting cap with have start = 0, while one starting in one year,
   * based on 3M-Libor will have start = 4.
   * @param end Integer for the end of the cap. A 10 year (spot staring) cap based on 6M Libor will have end = 20.
   * @param discountCurve Name of the discount/funding curve.
   * @param indexCurve Name of the index (aka (forward, estimation or projection) curve.
   * @param strike The strike.
   * @param capFloor true for a cap, false for a floor
   * @return A cap or floor
   */
  public static CapFloor makeCap(final Currency ccy, final IborIndex index, final int start, final int end, final double strike, final boolean capFloor) {
    return new CapFloor(makeCapletStrip(ccy, index, start, end, strike, capFloor));
  }

  private static CapFloorIbor[] makeCapletStrip(final Currency ccy, final IborIndex index, final int start, final int end, final double strike,
      final boolean capFloor) {
    ArgumentChecker.notNull(ccy, "null ccy");
    ArgumentChecker.notNull(index, "null index");
    ArgumentChecker.isTrue(strike >= 0.0, "negative strike");
    ArgumentChecker.isTrue(end > start, "end index must be greater that start index");
    final int n = end - start;
    final Period tenor = index.getTenor();
    final double tau = tenor.getDays() / 365.25 + tenor.getMonths() / 12.0 + tenor.getYears();

    final CapFloorIbor[] caplets = new CapFloorIbor[n];
    for (int i = 0; i < n; i++) {
      final double fixingStart = (i + start) * tau;
      final double fixingEnd = (i + 1 + start) * tau;

      caplets[i] = new CapFloorIbor(ccy, fixingEnd, tau, 1.0, fixingStart, index, fixingStart, fixingEnd, tau, strike, capFloor);
    }
    return caplets;
  }

}
