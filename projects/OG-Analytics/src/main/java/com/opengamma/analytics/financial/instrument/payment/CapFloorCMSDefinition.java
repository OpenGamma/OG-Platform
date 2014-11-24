/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a caplet/floorlet on CMS rate. The notional is positive for long the option and negative for short the option.
 */
public class CapFloorCMSDefinition extends CouponFloatingDefinition implements CapFloor {

  /**
   * The swap underlying the CMS coupon.
   */
  private final SwapFixedIborDefinition _underlyingSwap;
  /**
   * The CMS index associated to the coupon.
   */
  private final IndexSwap _cmsIndex;
  /**
   * The cap/floor strike.
   */
  private final double _strike;
  /**
   * The cap (true) / floor (false) flag.
   */
  private final boolean _isCap;

  /**
   * Cap/floor CMS constructor from all the cap/floor details.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param underlyingSwap The underlying swap.
   * @param cmsIndex The CMS index associated to the cap/floor.
   * @param strike The strike
   * @param isCap The cap (true) /floor (false) flag.
   */
  public CapFloorCMSDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double accrualFactor,
      final double notional, final ZonedDateTime fixingDate, final SwapFixedIborDefinition underlyingSwap, final IndexSwap cmsIndex, final double strike, final boolean isCap) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate);
    ArgumentChecker.notNull(underlyingSwap, "underlying swap");
    ArgumentChecker.notNull(cmsIndex, "CMS index");
    _underlyingSwap = underlyingSwap;
    _cmsIndex = cmsIndex;
    _strike = strike;
    _isCap = isCap;
  }

  /**
   * Cap/floor CMS builder from all the cap/floor details.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param underlyingSwap The underlying swap.
   * @param cmsIndex The CMS index associated to the cap/floor.
   * @param strike The strike
   * @param isCap The cap (true) /floor (false) flag.
   * @return The CMS cap/floor.
   */
  public static CapFloorCMSDefinition from(final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double accrualFactor,
      final double notional, final ZonedDateTime fixingDate, final SwapFixedIborDefinition underlyingSwap, final IndexSwap cmsIndex, final double strike, final boolean isCap) {
    ArgumentChecker.notNull(underlyingSwap, "underlying swap");
    return new CapFloorCMSDefinition(underlyingSwap.getCurrency(), paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate, underlyingSwap, cmsIndex, strike, isCap);
  }

  /**
   * Cap/floor CMS builder. The fixing date is computed from the start accrual date with the Ibor index spot lag. The underlying swap is computed from that date and the CMS index.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param cmsIndex The CMS index associated to the cap/floor.
   * @param strike The strike
   * @param isCap The cap (true) /floor (false) flag.
   * @param calendar The holiday calendar of the ibor index.
   * @return The CMS cap/floor.
   */
  public static CapFloorCMSDefinition from(final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double accrualFactor,
      final double notional, final IndexSwap cmsIndex, final double strike, final boolean isCap, final Calendar calendar) {
    final ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(accrualStartDate, -cmsIndex.getIborIndex().getSpotLag(), calendar);
    // Implementation comment: the underlying swap is used for forward. The notional, rate and payer flag are irrelevant.
    final SwapFixedIborDefinition underlyingSwap = SwapFixedIborDefinition.from(accrualStartDate, cmsIndex, 1.0, 1.0, true, calendar);
    return from(paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate, underlyingSwap, cmsIndex, strike, isCap);
  }

  /**
   * Cap/floor CMS builder from a CMS coupon and the option details.
   * @param coupon The CMS coupon.
   * @param strike The strike.
   * @param isCap The cap (true) /floor (false) flag.
   * @return The CMS cap/floor.
   */
  public static CapFloorCMSDefinition from(final CouponCMSDefinition coupon, final double strike, final boolean isCap) {
    ArgumentChecker.notNull(coupon, "coupon");
    return new CapFloorCMSDefinition(coupon.getCurrency(), coupon.getPaymentDate(), coupon.getAccrualStartDate(), coupon.getAccrualEndDate(), coupon.getPaymentYearFraction(), coupon.getNotional(),
        coupon.getFixingDate(), coupon.getUnderlyingSwap(), coupon.getCMSIndex(), strike, isCap);
  }

  /**
   * Gets the underlying swap.
   * @return The underlying swap
   */
  public SwapFixedIborDefinition getUnderlyingSwap() {
    return _underlyingSwap;
  }

  /**
   * Gets the CMS index associated to the coupon.
   * @return The CMS index.
   */
  public IndexSwap getCMSIndex() {
    return _cmsIndex;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getStrike() {
    return _strike;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isCap() {
    return _isCap;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double payOff(final double fixing) {
    final double omega = (_isCap) ? 1.0 : -1.0;
    return Math.max(omega * (fixing - _strike), 0);
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(date.isBefore(getFixingDate()), "Do not have any fixing data but are asking for a derivative after the fixing date " + getFixingDate() + " " + date);
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    // CMS is not fixed yet, all the details are required.
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final double fixingTime = TimeCalculator.getTimeBetween(date, getFixingDate());
    final double settlementTime = TimeCalculator.getTimeBetween(date, _underlyingSwap.getFixedLeg().getNthPayment(0).getAccrualStartDate());
    final SwapFixedCoupon<Coupon> swap = _underlyingSwap.toDerivative(date);
    return new CapFloorCMS(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixingTime, swap, settlementTime, _strike, _isCap);
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime dateTime, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries) {
    ArgumentChecker.notNull(dateTime, "date");
    final LocalDate dayConversion = dateTime.toLocalDate();
    ArgumentChecker.notNull(indexFixingTimeSeries, "Index fixing time series");
    ArgumentChecker.isTrue(!dayConversion.isAfter(getPaymentDate().toLocalDate()), "date is after payment date");
    final double paymentTime = TimeCalculator.getTimeBetween(dateTime, getPaymentDate());
    final LocalDate dayFixing = getFixingDate().toLocalDate();
    if (dayConversion.equals(dayFixing)) { // The fixing is on the reference date; if known the fixing is used and if not, the floating coupon is created.
      final Double fixedRate = indexFixingTimeSeries.getValue(getFixingDate());
      if (fixedRate != null) {
        return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), payOff(fixedRate));
      }
    }
    if (dayConversion.isAfter(dayFixing)) { // The fixing is required
      final Double fixedRate = indexFixingTimeSeries.getValue(getFixingDate().withHour(0)); // TODO: remove time from fixing date.
      if (fixedRate == null) {
        throw new OpenGammaRuntimeException("Could not get fixing value for date " + dayFixing);
      }
      return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), payOff(fixedRate));
    }
    // CMS is not fixed yet, all the details are required.
    final double fixingTime = TimeCalculator.getTimeBetween(dateTime, getFixingDate());
    final double settlementTime = TimeCalculator.getTimeBetween(dateTime, _underlyingSwap.getFixedLeg().getNthPayment(0).getAccrualStartDate());
    final SwapFixedCoupon<Coupon> swap = _underlyingSwap.toDerivative(dateTime);
    return new CapFloorCMS(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixingTime, swap, settlementTime, _strike, _isCap);
  }


  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCapFloorCMSDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCapFloorCMSDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (_isCap ? 1231 : 1237);
    long temp;
    temp = Double.doubleToLongBits(_strike);
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
    final CapFloorCMSDefinition other = (CapFloorCMSDefinition) obj;
    if (_isCap != other._isCap) {
      return false;
    }
    if (Double.doubleToLongBits(_strike) != Double.doubleToLongBits(other._strike)) {
      return false;
    }
    return true;
  }

}
