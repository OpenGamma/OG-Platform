/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

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
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * Describes a capital inflation indexed bond transaction. Both the coupon and the nominal are indexed on a price index.
 * @param <C> Type of inflation coupon. Can be {@link CouponInflationZeroCouponMonthlyGearingDefinition} or {@link CouponInflationZeroCouponInterpolationGearingDefinition}.
 */
public class BondCapitalIndexedTransactionDefinition<C extends CouponDefinition> extends BondTransactionDefinition<C, C>
  implements InstrumentDefinitionWithData<BondTransaction<? extends BondSecurity<? extends Payment, ? extends Coupon>>, DoubleTimeSeries<ZonedDateTime>> {

  /**
   * Constructor of a Capital indexed bond transaction from all the transaction details.
   * @param underlyingBond The capital indexed bond underlying the transaction.
   * @param quantity The number of bonds purchased (can be negative or positive).
   * @param settlementDate Transaction settlement date.
   * @param price The (clean quoted) price of the transaction in relative term (i.e. 0.90 if the dirty price is 90% of nominal).
   */
  public BondCapitalIndexedTransactionDefinition(final BondSecurityDefinition<C, C> underlyingBond, final double quantity, final ZonedDateTime settlementDate,
      final double price) {
    super(underlyingBond, quantity, settlementDate, price);
    ArgumentChecker.isTrue(underlyingBond instanceof BondCapitalIndexedSecurityDefinition, "Capital Indexed bond");
  }

  //TODO: from clean price adjusted monthly (for UK linked-gilts pre-2005).

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public BondCapitalIndexedTransaction<Coupon> toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> data, final String... yieldCurveNames) {
    return toDerivative(date, data);
  }

  @Override
  public BondCapitalIndexedTransaction<Coupon> toDerivative(final ZonedDateTime date) {
    final ImmutableZonedDateTimeDoubleTimeSeries series = ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(ZoneOffset.UTC);
    return toDerivative(date, series);
  }

  @Override
  public BondCapitalIndexedTransaction<Coupon> toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> data) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(data, "Price index fixing time series");
    final BondCapitalIndexedSecurity<Coupon> bondPurchase = ((BondCapitalIndexedSecurityDefinition<CouponInflationDefinition>) getUnderlyingBond()).toDerivative(date, getSettlementDate(), data);
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getUnderlyingBond().getSettlementDays(), getUnderlyingBond().getCalendar());
    final BondCapitalIndexedSecurity<Coupon> bondStandard = ((BondCapitalIndexedSecurityDefinition<CouponInflationDefinition>) getUnderlyingBond()).toDerivative(date, spot, data);
    final int nbCoupon = getUnderlyingBond().getCoupons().getNumberOfPayments();
    int couponIndex = 0; // The index of the coupon of the spot date.
    for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
      if (getUnderlyingBond().getCoupons().getNthPayment(loopcpn).getAccrualEndDate().isAfter(spot)) {
        couponIndex = loopcpn;
        break;
      }
    }
    final double notionalStandard = getUnderlyingBond().getCoupons().getNthPayment(couponIndex).getNotional();
    final BondCapitalIndexedTransaction<Coupon> result = new BondCapitalIndexedTransaction<>(bondPurchase, getQuantity(), getPrice(), bondStandard, notionalStandard);
    return result;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondCapitalIndexedTransaction(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondCapitalIndexedTransaction(this);
  }

}
