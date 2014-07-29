/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative;

import com.opengamma.analytics.financial.commodity.multicurvecommodity.underlying.CommodityUnderlying;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * Class describing a cash settle commodity coupon.
 */
public class CouponCommodityCashSettle extends CouponCommodity {

  /**
   * Constructor with all details.
   * @param paymentYearFraction The payment year fraction, positive
   * @param underlying The commodity underlying, not null
   * @param unitName name of the unit of the commodity delivered, not null
   * @param notional notional The number of unit
   * @param settlementTime The settlement time, , positive
   * @param calendar The holiday calendar, not null
   */
  public CouponCommodityCashSettle(final double paymentYearFraction, final CommodityUnderlying underlying, final String unitName, final double notional, final double settlementTime,
      final Calendar calendar) {
    super(paymentYearFraction, underlying, unitName, notional, settlementTime, calendar);

  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponCommodityCashSettle(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponCommodityCashSettle(this);
  }

  @Override
  public double getReferenceAmount() {
    return getNotional();
  }

}
