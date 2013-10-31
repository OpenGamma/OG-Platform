/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import org.apache.commons.lang.NotImplementedException;
import org.threeten.bp.LocalDate;

/**
 * The 'Flat' day count.
 */
public class FlatDayCount extends StatelessDayCount {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  @Override
  public double getDayCountFraction(final LocalDate firstDate, final LocalDate secondDate) {
    throw new NotImplementedException("Cannot get day count fraction for a flat day count");
  }

  @Override
  public double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon, final double paymentsPerYear) {
    return 0;
  }

  @Override
  public String getName() {
    return "Flat";
  }

}
