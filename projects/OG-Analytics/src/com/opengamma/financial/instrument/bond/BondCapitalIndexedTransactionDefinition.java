/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.bond;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.financial.instrument.inflation.CouponInflationDefinition;
import com.opengamma.financial.instrument.inflation.CouponInflationZeroCouponInterpolationGearingDefinition;
import com.opengamma.financial.instrument.inflation.CouponInflationZeroCouponMonthlyGearingDefinition;
import com.opengamma.financial.instrument.payment.CouponDefinition;
import com.opengamma.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.financial.interestrate.bond.definition.BondTransaction;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.schedule.ScheduleCalculator;
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
  public BondCapitalIndexedTransactionDefinition(BondSecurityDefinition<C, C> underlyingBond, double quantity, ZonedDateTime settlementDate, double price) {
    super(underlyingBond, quantity, settlementDate, price);
    Validate.isTrue(underlyingBond instanceof BondCapitalIndexedSecurityDefinition, "Capital Indexed bond");
  }

  //  /**
  //   * Build a bond from a quoted real price and interpolated reference index. This is the quotation convention for US TIPS, UK Gilts (post-2005) and France OATi.
  //   * @param underlyingBond The underlying bond.
  //   * @param quantity The number of bonds purchased (can be negative or positive).
  //   * @param settlementDate Transaction settlement date.
  //   * @param priceClean The real clean price. The real accrued interest will be added the sum multiplied by the ratio of reference index to obtain the nominal dirty price.
  //   * @param priceIndexTimeSeries The price index time series. Should contains at least the indexes related to the settlement reference dates.
  //   * @return The bond.
  //   */
  //  public static BondCapitalIndexedTransactionDefinition<CouponInflationZeroCouponInterpolationGearingDefinition> fromRealCleanPriceInterpolation(
  //      BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponInterpolationGearingDefinition> underlyingBond, double quantity, ZonedDateTime settlementDate, double priceClean,
  //      DoubleTimeSeries<ZonedDateTime> priceIndexTimeSeries) {
  //    Validate.isTrue(underlyingBond instanceof BondCapitalIndexedSecurityDefinition<?>, "Bond Capital Indexed");
  //    ZonedDateTime refInterpolatedDate = settlementDate.minusMonths(underlyingBond.getMonthLag());
  //    ZonedDateTime[] referenceSettleDate = new ZonedDateTime[2];
  //    referenceSettleDate[0] = refInterpolatedDate.withDayOfMonth(1);
  //    referenceSettleDate[1] = referenceSettleDate[0].plusMonths(1);
  //    double weight = 1.0 - (settlementDate.getDayOfMonth() - 1.0) / settlementDate.getMonthOfYear().getLastDayOfMonth(settlementDate.isLeapYear());
  //    Double[] knownIndex = new Double[] {priceIndexTimeSeries.getValue(referenceSettleDate[0]), priceIndexTimeSeries.getValue(referenceSettleDate[1])};
  //    Validate.isTrue((knownIndex[0] != null) && (knownIndex[1] != null), "Time series does not contains the required price index");
  //    double indexEnd = weight * knownIndex[0] + (1 - weight) * knownIndex[1]; // Interpolated index
  //    double referenceIndex = indexEnd / underlyingBond.getIndexStartValue();
  //    final int nbCoupon = underlyingBond.getCoupon().getNumberOfPayments();
  //    int couponIndex = 0;
  //    for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
  //      if (underlyingBond.getCoupon().getNthPayment(loopcpn).getAccrualEndDate().isAfter(settlementDate)) {
  //        couponIndex = loopcpn;
  //        break;
  //      }
  //    }
  //    double accruedInterest = 0.0;
  //    accruedInterest = AccruedInterestCalculator.getAccruedInterest(underlyingBond.getDayCount(), couponIndex, nbCoupon, underlyingBond.getCoupon().getNthPayment(couponIndex).getAccrualStartDate(),
  //        settlementDate, underlyingBond.getCoupon().getNthPayment(couponIndex).getAccrualEndDate(), underlyingBond.getCoupon().getNthPayment(couponIndex).getFactor(),
  //        underlyingBond.getCouponPerYear(), underlyingBond.isEOM());
  //    if (underlyingBond.getExCouponDays() != 0 && underlyingBond.getCoupon().getNthPayment(couponIndex).getAccrualEndDate().minusDays(underlyingBond.getExCouponDays()).isBefore(settlementDate)) {
  //      accruedInterest = accruedInterest - underlyingBond.getCoupon().getNthPayment(couponIndex).getFactor();
  //    }
  //    double adjustedDirtyPrice = referenceIndex * (priceClean + accruedInterest);
  //    return new BondCapitalIndexedTransactionDefinition<CouponInflationZeroCouponInterpolationGearingDefinition>(underlyingBond, quantity, settlementDate, adjustedDirtyPrice);
  //  }

  //TODO: from clean price adjusted monthly (for UK linked-gilts pre-2005).

  @Override
  public BondCapitalIndexedTransaction<Coupon> toDerivative(ZonedDateTime date, String... yieldCurveNames) {
    DoubleTimeSeries<ZonedDateTime> series = new ArrayZonedDateTimeDoubleTimeSeries();
    return toDerivative(date, series, yieldCurveNames);
  }

  @Override
  public BondCapitalIndexedTransaction<Coupon> toDerivative(ZonedDateTime date, DoubleTimeSeries<ZonedDateTime> data, String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(data, "Price index fixing time series");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    @SuppressWarnings("unchecked")
    final BondCapitalIndexedSecurity<Coupon> bondPurchase = ((BondCapitalIndexedSecurityDefinition<CouponInflationDefinition>) getUnderlyingBond()).toDerivative(date, getSettlementDate(), data);
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getUnderlyingBond().getCalendar(), getUnderlyingBond().getSettlementDays());
    @SuppressWarnings("unchecked")
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
  public <U, V> V accept(InstrumentDefinitionVisitor<U, V> visitor, U data) {
    return visitor.visitBondCapitalIndexedTransaction(this, data);
  }

  @Override
  public <V> V accept(InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitBondCapitalIndexedTransaction(this);
  }

}
