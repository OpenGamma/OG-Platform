/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.bond;

import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.daycount.AccruedInterestCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.bond.definition.BondFixedDescription;
import com.opengamma.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.schedule.ScheduleCalculator;

/**
 * Describes a transaction on a fixed coupon bond issue.
 */
public class BondFixedTransactionDefinition extends BondTransactionDefinition<CouponFixedDefinition> {

  /**
   * Accrued interest at settlement date.
   */
  private double _accruedInterestAtSettlement;

  /**
   * Constructor of a fixed coupon bond transaction from all the transaction details.
   * @param underlyingBond The fixed coupon bond underlying the transaction.
   * @param quantity The number of bonds purchased (can be negative or positive).
   * @param settlementDate Transaction settlement date.
   * @param price The (dirty) price of the transaction in relative term (i.e. 0.90 if the dirty price is 90% of nominal).
   */
  public BondFixedTransactionDefinition(final BondFixedDescriptionDefinition underlyingBond, final double quantity, final ZonedDateTime settlementDate, final double price) {
    super(underlyingBond, quantity, settlementDate, price);
    _accruedInterestAtSettlement = 0;
    final int nbCoupon = underlyingBond.getCoupon().getNumberOfPayments();
    final double accruedInterest = AccruedInterestCalculator.getAccruedInterest(getUnderlyingBond().getDayCount(), getCouponIndex(), nbCoupon, getPreviousAccrualDate(), settlementDate,
        getNextAccrualDate(), underlyingBond.getCoupon().getNthPayment(getCouponIndex()).getRate(), underlyingBond.getCouponPerYear(), underlyingBond.isEOM());
    if (underlyingBond.getExCouponDays() != 0 && getNextAccrualDate().minusDays(underlyingBond.getExCouponDays()).isBefore(settlementDate)) {
      _accruedInterestAtSettlement = accruedInterest - underlyingBond.getCoupon().getNthPayment(getCouponIndex()).getRate();
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
  public BondFixedDescriptionDefinition getUnderlyingBond() {
    return (BondFixedDescriptionDefinition) super.getUnderlyingBond();
  }

  @Override
  public BondFixedTransaction toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    // Implementation note: First yield curve used for coupon and notional (credit), the second for risk free settlement.
    // TODO: Take the ex-coupon days into account.
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    final String creditCurveName = yieldCurveNames[0];
    final String riskFreeCurveName = yieldCurveNames[1];
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(date), TimeZone.UTC);
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(zonedDate, getUnderlyingBond().getCalendar(), getUnderlyingBond().getSettlementDays());
    final double spotTime = actAct.getDayCountFraction(zonedDate, spot);
    final double settlementTime;
    final double settlementAmount;
    if (getSettlementDate().isBefore(zonedDate)) {
      settlementTime = 0;
      settlementAmount = 0;
    } else {
      settlementTime = actAct.getDayCountFraction(zonedDate, getSettlementDate());
      settlementAmount = getPaymentAmount();
    }
    final PaymentFixed settlement = new PaymentFixed(getUnderlyingBond().getCurrency(), settlementTime, settlementAmount, riskFreeCurveName);
    final AnnuityPaymentFixed nominal = getUnderlyingBond().getNominal().toDerivative(date, creditCurveName);
    final AnnuityCouponFixed coupon = getUnderlyingBond().getCoupon().toDerivative(date, creditCurveName);
    final AnnuityPaymentFixed nominalPurchase = nominal.trimBefore(settlementTime);
    final AnnuityCouponFixed couponPurchase = coupon.trimBefore(settlementTime);
    final AnnuityPaymentFixed nominalStandard = nominal.trimBefore(spotTime);
    final AnnuityCouponFixed couponStandard = coupon.trimBefore(spotTime);
    final BondFixedDescription bondPurchase = new BondFixedDescription(nominalPurchase, couponPurchase, getUnderlyingBond().getYieldConvention());
    final BondFixedDescription bondStandard = new BondFixedDescription(nominalStandard, couponStandard, getUnderlyingBond().getYieldConvention());
    final int nbCoupon = getUnderlyingBond().getCoupon().getNumberOfPayments();
    int couponIndex = 0; // The index of the coupon of the spot date.
    for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
      if (getUnderlyingBond().getCoupon().getNthPayment(loopcpn).getAccrualEndDate().isAfter(spot)) {
        couponIndex = loopcpn;
        break;
      }
    }
    final double accruedInterestAtSpot = (getUnderlyingBond()).accruedInterest(spot);
    final double notionalStandard = getUnderlyingBond().getCoupon().getNthPayment(couponIndex).getNotional();
    final BondFixedTransaction result = new BondFixedTransaction(bondPurchase, getQuantity(), settlement, bondStandard, spotTime, accruedInterestAtSpot, notionalStandard);
    return result;
  }

  @Override
  public <U, V> V accept(final FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return null;
  }

  @Override
  public <V> V accept(final FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return null;
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
