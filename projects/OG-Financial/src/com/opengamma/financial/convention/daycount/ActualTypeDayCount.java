/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.ZonedDateTime;

/**
 * Base class for 'actual' style day counts.
 */
public abstract class ActualTypeDayCount extends StatelessDayCount {

  @Override
  public abstract double getAccruedInterest(
      final ZonedDateTime previousCouponDate, final ZonedDateTime date, final ZonedDateTime nextCouponDate,
      final double coupon, final int paymentsPerYear);

}
