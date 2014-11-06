/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.annuity;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.commodity.multicurvecommodity.definition.CouponCommodityPhysicalSettleDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 *A wrapper class for an annuity containing AnnuityCouponCommodityPhysicalSettleDefinition. 
 */
public class AnnuityCouponCommodityPhysicalSettleDefinition extends AnnuityCouponCommodityDefinition<CouponCommodityPhysicalSettleDefinition> {

  /**
   * Constructor from a list of overnight arithmetic average coupons with spread.
   * @param payments The coupons.
   * @param calendar The holiday calendar
   */
  public AnnuityCouponCommodityPhysicalSettleDefinition(final CouponCommodityPhysicalSettleDefinition[] payments, final Calendar calendar) {
    super(payments, calendar);
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public Annuity<? extends Payment> toDerivative(final ZonedDateTime valZdt, final String... yieldCurveNames) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Annuity<? extends Payment> toDerivative(final ZonedDateTime valZdt) {
    ArgumentChecker.notNull(valZdt, "date");

    final List<Payment> resultList = new ArrayList<>();
    final CouponCommodityPhysicalSettleDefinition[] payments = getPayments();
    final ZonedDateTime valZdtInPaymentZone = valZdt.withZoneSameInstant(payments[0].getPaymentDate().getZone());
    final LocalDate valDate = valZdtInPaymentZone.toLocalDate();

    for (int loopcoupon = 0; loopcoupon < payments.length; loopcoupon++) {
      if (!valDate.isAfter(payments[loopcoupon].getPaymentDate().toLocalDate())) {
        resultList.add(payments[loopcoupon].toDerivative(valZdt));
      }
    }
    return new Annuity<>(resultList.toArray(new Coupon[resultList.size()]));
  }

}
