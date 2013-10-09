/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.daycount.AccruedInterestCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * Describes a transaction on a fixed coupon bond issue.
 */
public class BondFixedTransactionDefinition extends BondTransactionDefinition<PaymentFixedDefinition, CouponFixedDefinition> {

  /**
   * Accrued interest at settlement date.
   */
  private double _accruedInterestAtSettlement;

  /**
   * Constructor of a fixed coupon bond transaction from all the transaction details.
   * @param underlyingBond The fixed coupon bond underlying the transaction.
   * @param quantity The number of bonds purchased (can be negative or positive).
   * @param settlementDate Transaction settlement date.
   * @param dirtyPrice The (dirty) price of the transaction in relative term (i.e. 0.90 if the dirty price is 90% of nominal).
   */
  public BondFixedTransactionDefinition(final BondFixedSecurityDefinition underlyingBond, final double quantity, final ZonedDateTime settlementDate, final double dirtyPrice) {
    super(underlyingBond, quantity, settlementDate, dirtyPrice);
    _accruedInterestAtSettlement = 0;
    final int nbCoupon = underlyingBond.getCoupons().getNumberOfPayments();
    final double accruedInterest = AccruedInterestCalculator.getAccruedInterest(getUnderlyingBond().getDayCount(), getCouponIndex(), nbCoupon, getPreviousAccrualDate(),
        settlementDate, getNextAccrualDate(), underlyingBond.getCoupons().getNthPayment(getCouponIndex()).getRate(), underlyingBond.getCouponPerYear(),
        underlyingBond.isEOM());
    if (underlyingBond.getExCouponDays() != 0 && getNextAccrualDate().minusDays(underlyingBond.getExCouponDays()).isBefore(settlementDate)) {
      _accruedInterestAtSettlement = accruedInterest - underlyingBond.getCoupons().getNthPayment(getCouponIndex()).getRate();
    } else {
      _accruedInterestAtSettlement = accruedInterest;
    }
  }

  /**
   * Gets the accrued interest at transaction settlement.
   * @return The accrued interest at settlement.
   */
  public double getAccruedInterestAtSettlement() {
    return _accruedInterestAtSettlement;
  }

  /**
   * Gets the bond underlying the transaction.
   * @return The underlying bond.
   */
  @Override
  public BondFixedSecurityDefinition getUnderlyingBond() {
    return (BondFixedSecurityDefinition) super.getUnderlyingBond();
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public BondFixedTransaction toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    // Implementation note: First yield curve used for coupon and notional (credit), the second for risk free settlement.
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getUnderlyingBond().getSettlementDays(), getUnderlyingBond().getCalendar());
    final BondFixedSecurity bondPurchase = getUnderlyingBond().toDerivative(date, getSettlementDate(), yieldCurveNames);
    final BondFixedSecurity bondStandard = getUnderlyingBond().toDerivative(date, yieldCurveNames);
    final int nbCoupon = getUnderlyingBond().getCoupons().getNumberOfPayments();
    int couponIndex = 0; // The index of the coupon of the spot date.
    for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
      if (getUnderlyingBond().getCoupons().getNthPayment(loopcpn).getAccrualEndDate().isAfter(spot)) {
        couponIndex = loopcpn;
        break;
      }
    }
    final double notionalStandard = getUnderlyingBond().getCoupons().getNthPayment(couponIndex).getNotional();
    double price;
    if (getSettlementDate().isBefore(date)) { // If settlement already took place, the price is set to 0.
      price = 0.0;
    } else {
      price = getPrice();
    }
    final BondFixedTransaction result = new BondFixedTransaction(bondPurchase, getQuantity(), price, bondStandard, notionalStandard);
    return result;
  }

  @Override
  public BondFixedTransaction toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getUnderlyingBond().getSettlementDays(), getUnderlyingBond().getCalendar());
    final BondFixedSecurity bondPurchase = getUnderlyingBond().toDerivative(date, getSettlementDate());
    final BondFixedSecurity bondStandard = getUnderlyingBond().toDerivative(date);
    final int nbCoupon = getUnderlyingBond().getCoupons().getNumberOfPayments();
    int couponIndex = 0; // The index of the coupon of the spot date.
    for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
      if (getUnderlyingBond().getCoupons().getNthPayment(loopcpn).getAccrualEndDate().isAfter(spot)) {
        couponIndex = loopcpn;
        break;
      }
    }
    final double notionalStandard = getUnderlyingBond().getCoupons().getNthPayment(couponIndex).getNotional();
    double price;
    if (getSettlementDate().isBefore(date)) { // If settlement already took place, the price is set to 0.
      price = 0.0;
    } else {
      price = getPrice();
    }
    final BondFixedTransaction result = new BondFixedTransaction(bondPurchase, getQuantity(), price, bondStandard, notionalStandard);
    return result;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondFixedTransactionDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondFixedTransactionDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_accruedInterestAtSettlement);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final BondFixedTransactionDefinition other = (BondFixedTransactionDefinition) obj;
    if (Double.doubleToLongBits(_accruedInterestAtSettlement) != Double.doubleToLongBits(other._accruedInterestAtSettlement)) {
      return false;
    }
    return true;
  }

}
