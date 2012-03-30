/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationGearingDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondTransaction;
import com.opengamma.analytics.financial.interestrate.payments.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

/**
 * Describes a capital inflation indexed bond transaction. Both the coupon and the nominal are indexed on a price index.
 * @param <C> Type of inflation coupon. Can be {@link CouponInflationZeroCouponMonthlyGearingDefinition} or {@link CouponInflationZeroCouponInterpolationGearingDefinition}.
 */
public class BondCapitalIndexedTransactionDefinition<C extends CouponDefinition> extends BondTransactionDefinition<C, C> implements
    InstrumentDefinitionWithData<BondTransaction<? extends BondSecurity<? extends Payment, ? extends Coupon>>, DoubleTimeSeries<ZonedDateTime>> {

  /**
   * Constructor of a Capital indexed bond transaction from all the transaction details.
   * @param underlyingBond The capital indexed bond underlying the transaction.
   * @param quantity The number of bonds purchased (can be negative or positive).
   * @param settlementDate Transaction settlement date.
   * @param price The (clean quoted) price of the transaction in relative term (i.e. 0.90 if the dirty price is 90% of nominal).
   */
  public BondCapitalIndexedTransactionDefinition(final BondSecurityDefinition<C, C> underlyingBond, final double quantity, final ZonedDateTime settlementDate, final double price) {
    super(underlyingBond, quantity, settlementDate, price);
    Validate.isTrue(underlyingBond instanceof BondCapitalIndexedSecurityDefinition, "Capital Indexed bond");
  }

  //TODO: from clean price adjusted monthly (for UK linked-gilts pre-2005).

  @Override
  public BondCapitalIndexedTransaction<Coupon> toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    final DoubleTimeSeries<ZonedDateTime> series = new ArrayZonedDateTimeDoubleTimeSeries();
    return toDerivative(date, series, yieldCurveNames);
  }

  @Override
  public BondCapitalIndexedTransaction<Coupon> toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> data, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(data, "Price index fixing time series");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    final BondCapitalIndexedSecurity<Coupon> bondPurchase = ((BondCapitalIndexedSecurityDefinition<CouponInflationDefinition>) getUnderlyingBond()).toDerivative(date, getSettlementDate(), data);
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getUnderlyingBond().getSettlementDays(), getUnderlyingBond().getCalendar());
    final BondCapitalIndexedSecurity<Coupon> bondStandard = ((BondCapitalIndexedSecurityDefinition<CouponInflationDefinition>) getUnderlyingBond()).toDerivative(date, spot, data);
    final int nbCoupon = getUnderlyingBond().getCoupon().getNumberOfPayments();
    int couponIndex = 0; // The index of the coupon of the spot date.
    for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
      if (getUnderlyingBond().getCoupon().getNthPayment(loopcpn).getAccrualEndDate().isAfter(spot)) {
        couponIndex = loopcpn;
        break;
      }
    }
    final double notionalStandard = getUnderlyingBond().getCoupon().getNthPayment(couponIndex).getNotional();
    final BondCapitalIndexedTransaction<Coupon> result = new BondCapitalIndexedTransaction<Coupon>(bondPurchase, getQuantity(), getPrice(), bondStandard, notionalStandard);
    return result;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitBondCapitalIndexedTransaction(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitBondCapitalIndexedTransaction(this);
  }

}
