/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.payment;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.index.IndexSwap;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.interestrate.payments.CapFloorCMS;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.CouponCMS;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.TimeCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Class describing a caplet/floorlet on CMS rate. The notional is positive for long the option and negative for short the option.
 */
public class CapFloorCMSDefinition extends CouponCMSDefinition implements CapFloor {

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
    super(currency, paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate, underlyingSwap, cmsIndex);
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
    Validate.notNull(underlyingSwap, "underlying swap");
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
   * @return The CMS cap/floor.
   */
  public static CapFloorCMSDefinition from(final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double accrualFactor,
      final double notional, final IndexSwap cmsIndex, final double strike, final boolean isCap) {
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(accrualStartDate, cmsIndex.getIborIndex().getBusinessDayConvention(), cmsIndex.getIborIndex().getCalendar(), -cmsIndex.getIborIndex()
        .getSpotLag());
    // Implementation comment: the underlying swap is used for forward. The notional, rate and payer flag are irrelevant.
    final SwapFixedIborDefinition underlyingSwap = SwapFixedIborDefinition.from(accrualStartDate, cmsIndex, 1.0, 1.0, true);
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
    Validate.notNull(coupon);
    return new CapFloorCMSDefinition(coupon.getCurrency(), coupon.getPaymentDate(), coupon.getAccrualStartDate(), coupon.getAccrualEndDate(), coupon.getPaymentYearFraction(), coupon.getNotional(),
        coupon.getFixingDate(), coupon.getUnderlyingSwap(), coupon.getCMSIndex(), strike, isCap);
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
  public Coupon toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.isTrue(date.isBefore(getFixingDate()), "Do not have any fixing data but are asking for a derivative after the fixing date " + getFixingDate() + " " + date);
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 1, "at least two curves required");
    Validate.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    // CMS is not fixed yet, all the details are required.
    final CouponCMS cmsCoupon = (CouponCMS) super.toDerivative(date, yieldCurveNames);
    return CapFloorCMS.from(cmsCoupon, _strike, _isCap);
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTS, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(indexFixingTS, "index fixing time series");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 1, "at least two curves required");
    Validate.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final String fundingCurveName = yieldCurveNames[0];
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    if (date.isAfter(getFixingDate()) || date.equals(getFixingDate())) { // The CMS coupon has already fixed, it is now a fixed coupon.
      Double fixedRate = indexFixingTS.getValue(getFixingDate());
      //TODO remove me when times are sorted out in the swap definitions or we work out how to deal with this another way
      if (fixedRate == null) {
        final ZonedDateTime fixingDateAtLiborFixingTime = getFixingDate().withTime(11, 0);
        fixedRate = indexFixingTS.getValue(fixingDateAtLiborFixingTime);
      }
      if (fixedRate == null) {
        final ZonedDateTime previousBusinessDay = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Preceding").adjustDate(getCMSIndex().getIborIndex().getCalendar(),
            getFixingDate().minusDays(1));
        fixedRate = indexFixingTS.getValue(previousBusinessDay);
        //TODO remove me when times are sorted out in the swap definitions or we work out how to deal with this another way
        if (fixedRate == null) {
          final ZonedDateTime previousBusinessDayAtLiborFixingTime = previousBusinessDay.withTime(11, 0);
          fixedRate = indexFixingTS.getValue(previousBusinessDayAtLiborFixingTime);
        }
        if (fixedRate == null) {
          fixedRate = indexFixingTS.getLatestValue(); //TODO remove me as soon as possible
          //throw new OpenGammaRuntimeException("Could not get fixing value for date " + getFixingDate());
        }
      }
      return new CouponFixed(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), payOff(fixedRate));
    }
    // CMS is not fixed yet, all the details are required.
    final CouponCMS cmsCoupon = (CouponCMS) super.toDerivative(date, indexFixingTS, yieldCurveNames);
    return CapFloorCMS.from(cmsCoupon, _strike, _isCap);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitCapFloorCMS(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitCapFloorCMS(this);
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
