/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMSSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a caplet/floorlet on CMS spread. The notional is positive for long the option and negative for short the option.
 * The pay-off of the instrument is a cap/floor on the difference between the first CMS rate and the second CMS rate.
 * Both swaps underlying the CMS need to have the same settlement date.
 */
public class CapFloorCMSSpreadDefinition extends CouponFloatingDefinition implements CapFloor {

  /**
   * The swap underlying the first CMS.
   */
  private final SwapFixedIborDefinition _underlyingSwap1;
  /**
   * The index associated to the first CMS.
   */
  private final IndexSwap _cmsIndex1;
  /**
   * The swap underlying the second CMS.
   */
  private final SwapFixedIborDefinition _underlyingSwap2;
  /**
   * The index associated to the second CMS.
   */
  private final IndexSwap _cmsIndex2;
  /**
   * The cap/floor strike.
   */
  private final double _strike;
  /**
   * The cap (true) / floor (false) flag.
   */
  private final boolean _isCap;
  /**
   * The calendar associated with the first leg.
   */
  private final Calendar _calendar1;
  /**
   * The calendar associated with the second leg.
   */
  @SuppressWarnings("unused")
  private final Calendar _calendar2;

  /**
   * Cap/floor CMS spread constructor from all the details.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param underlyingSwap1 The swap underlying the first CMS.
   * @param cmsIndex1 The index associated to the first CMS.
   * @param underlyingSwap2 The swap underlying the second CMS.
   * @param cmsIndex2 The index associated to the second CMS.
   * @param strike The strike
   * @param isCap The cap (true) /floor (false) flag.
   * @param calendar1 The holiday calendar for the first leg.
   * @param calendar2 The holiday calendar for the second leg.
   */
  public CapFloorCMSSpreadDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double accrualFactor,
      final double notional, final ZonedDateTime fixingDate, final SwapFixedIborDefinition underlyingSwap1, final IndexSwap cmsIndex1, final SwapFixedIborDefinition underlyingSwap2,
      final IndexSwap cmsIndex2, final double strike, final boolean isCap, final Calendar calendar1, final Calendar calendar2) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate);
    ArgumentChecker.notNull(underlyingSwap1, "underlying swap");
    ArgumentChecker.notNull(cmsIndex1, "CMS index");
    ArgumentChecker.notNull(underlyingSwap2, "underlying swap");
    ArgumentChecker.notNull(cmsIndex2, "CMS index");
    ArgumentChecker.isTrue(underlyingSwap1.getFixedLeg().getNthPayment(0).getAccrualStartDate() == underlyingSwap2.getFixedLeg().getNthPayment(0).getAccrualStartDate(), "Identic settlement date");
    _underlyingSwap1 = underlyingSwap1;
    _cmsIndex1 = cmsIndex1;
    _underlyingSwap2 = underlyingSwap2;
    _cmsIndex2 = cmsIndex2;
    _strike = strike;
    _isCap = isCap;
    _calendar1 = calendar1;
    _calendar2 = calendar2;
  }

  /**
   * Builder of a CMS spread cap/floor. The fixing date is computed from the start accrual date with the Ibor index spot lag (fixing in advance).
   * The underlying swaps are computed from that date and the CMS indexes.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param cmsIndex1 The index associated to the first CMS.
   * @param cmsIndex2 The index associated to the second CMS.
   * @param strike The strike
   * @param isCap The cap (true) /floor (false) flag.
   * @param calendar1 The holiday calendar of the first leg.
   * @param calendar2 The holiday calendar of the second leg.
   * @return The CMS spread cap/floor.
   */
  public static CapFloorCMSSpreadDefinition from(final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double accrualFactor,
      final double notional, final IndexSwap cmsIndex1, final IndexSwap cmsIndex2, final double strike, final boolean isCap, final Calendar calendar1, final Calendar calendar2) {
    ArgumentChecker.notNull(accrualStartDate, "Accrual start date.");
    ArgumentChecker.notNull(cmsIndex1, "CMS index");
    ArgumentChecker.notNull(cmsIndex2, "CMS index");
    final ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(accrualStartDate, -cmsIndex1.getIborIndex().getSpotLag(), calendar1);
    // Implementation comment: the underlying swap is used for forward. The notional, rate and payer flag are irrelevant.
    final SwapFixedIborDefinition underlyingSwap1 = SwapFixedIborDefinition.from(accrualStartDate, cmsIndex1, 1.0, 1.0, true, calendar1);
    final SwapFixedIborDefinition underlyingSwap2 = SwapFixedIborDefinition.from(accrualStartDate, cmsIndex2, 1.0, 1.0, true, calendar2);
    return new CapFloorCMSSpreadDefinition(cmsIndex1.getIborIndex().getCurrency(), paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate, underlyingSwap1, cmsIndex1,
        underlyingSwap2, cmsIndex2, strike, isCap, calendar1, calendar2);
  }

  /**
   * Gets the underlying swap associated to the first CMS.
   * @return The underlying swap
   */
  public SwapFixedIborDefinition getUnderlyingSwap1() {
    return _underlyingSwap1;
  }

  /**
   * Gets the index associated to the first CMS.
   * @return The CMS index.
   */
  public IndexSwap getCmsIndex1() {
    return _cmsIndex1;
  }

  /**
   * Gets the underlying swap associated to the second CMS.
   * @return The underlying swap
   */
  public SwapFixedIborDefinition getUnderlyingSwap2() {
    return _underlyingSwap2;
  }

  /**
   * Gets the index associated to the second CMS.
   * @return The CMS index.
   */
  public IndexSwap getCmsIndex2() {
    return _cmsIndex2;
  }

  @Override
  public double getStrike() {
    return _strike;
  }

  @Override
  public boolean isCap() {
    return _isCap;
  }

  @Override
  /**
   * The "fixing" is the difference between the first and the second CMS rates fixings.
   */
  public double payOff(final double fixing) {
    final double omega = (_isCap) ? 1.0 : -1.0;
    return Math.max(omega * (fixing - _strike), 0);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCapFloorCMSSpreadDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCapFloorCMSSpreadDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _cmsIndex1.hashCode();
    result = prime * result + _cmsIndex2.hashCode();
    result = prime * result + (_isCap ? 1231 : 1237);
    long temp;
    temp = Double.doubleToLongBits(_strike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _underlyingSwap1.hashCode();
    result = prime * result + _underlyingSwap2.hashCode();
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
    final CapFloorCMSSpreadDefinition other = (CapFloorCMSSpreadDefinition) obj;
    if (!ObjectUtils.equals(_cmsIndex1, other._cmsIndex1)) {
      return false;
    }
    if (!ObjectUtils.equals(_cmsIndex2, other._cmsIndex2)) {
      return false;
    }
    if (_isCap != other._isCap) {
      return false;
    }
    if (Double.doubleToLongBits(_strike) != Double.doubleToLongBits(other._strike)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingSwap1, other._underlyingSwap1)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingSwap2, other._underlyingSwap2)) {
      return false;
    }
    return true;
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(date.isBefore(getFixingDate()), "Do not have any fixing data but are asking for a derivative after the fixing date");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    // First curve used for discounting. If two curves, the same forward is used for both swaps;
    // if more than two curves, the second is used for the forward of the first swap and the third for the forward of the second swap.
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    // CMS spread is not fixed yet, all the details are required.
    final double fixingTime = TimeCalculator.getTimeBetween(date, getFixingDate());
    final double settlementTime = TimeCalculator.getTimeBetween(date, _underlyingSwap1.getFixedLeg().getNthPayment(0).getAccrualStartDate());
    final SwapFixedCoupon<Coupon> swap1 = _underlyingSwap1.toDerivative(date);
    final SwapFixedCoupon<Coupon> swap2 = _underlyingSwap2.toDerivative(date);
    return new CapFloorCMSSpread(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixingTime, swap1, _cmsIndex1, swap2, _cmsIndex2, settlementTime, _strike, _isCap);
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> data) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    // First curve used for discounting. If two curves, the same forward is used for both swaps;
    // if more than two curves, the second is used for the forward of the first swap and the third for the forward of the second swap.
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    if (date.isAfter(getFixingDate()) || date.equals(getFixingDate())) { // The CMS coupon has already fixed, it is now a fixed coupon.
      Double fixedRate = data.getValue(getFixingDate());
      //TODO remove me when times are sorted out in the swap definitions or we work out how to deal with this another way
      if (fixedRate == null) {
        final ZonedDateTime fixingDateAtLiborFixingTime = getFixingDate().with(LocalTime.of(11, 0));
        fixedRate = data.getValue(fixingDateAtLiborFixingTime);
      }
      if (fixedRate == null) {
        final ZonedDateTime previousBusinessDay = BusinessDayConventions.PRECEDING.adjustDate(_calendar1,
            getFixingDate().minusDays(1));
        fixedRate = data.getValue(previousBusinessDay);
        //TODO remove me when times are sorted out in the swap definitions or we work out how to deal with this another way
        if (fixedRate == null) {
          final ZonedDateTime previousBusinessDayAtLiborFixingTime = previousBusinessDay.with(LocalTime.of(11, 0));
          fixedRate = data.getValue(previousBusinessDayAtLiborFixingTime);
        }
        if (fixedRate == null) {
          fixedRate = data.getLatestValue(); //TODO remove me as soon as possible
          //throw new OpenGammaRuntimeException("Could not get fixing value for date " + getFixingDate());
        }
      }

      return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), payOff(fixedRate));
    }
    // CMS spread is not fixed yet, all the details are required.
    final double fixingTime = TimeCalculator.getTimeBetween(date, getFixingDate());
    final double settlementTime = TimeCalculator.getTimeBetween(date, _underlyingSwap1.getFixedLeg().getNthPayment(0).getAccrualStartDate());
    final SwapFixedCoupon<Coupon> swap1 = _underlyingSwap1.toDerivative(date);
    final SwapFixedCoupon<Coupon> swap2 = _underlyingSwap2.toDerivative(date);
    return new CapFloorCMSSpread(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixingTime, swap1, _cmsIndex1, swap2, _cmsIndex2, settlementTime, _strike, _isCap);
  }
}
