/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.fra;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFloatingDefinition;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a Forward Rate Agreement (FRA). The pay-off is (Ibor fixing-FRA rate) multiplied by the notional and accrual fraction and discounted with the fixing rate to the payment date.
 * This correspond to "buy" a FRA is the same as buying the Ibor rate and paying the FRA rate for it.
 */
public class ForwardRateAgreementDefinition extends CouponFloatingDefinition {
  /**
   * Preceding business day convention.
   */
  private static final BusinessDayConvention PRECEDING_BDC = BusinessDayConventions.PRECEDING;
  /**
   * Ibor-like index on which the FRA fixes. The index currency should be the same as the instrument currency.
   */
  private final IborIndex _index;
  /**
   * The start date of the fixing period.
   */
  private final ZonedDateTime _fixingPeriodStartDate;
  /**
   * The end date of the fixing period.
   */
  private final ZonedDateTime _fixingPeriodEndDate;
  /**
   * The accrual factor (or year fraction) associated to the fixing period in the Index day count convention.
   */
  private final double _fixingPeriodAccrualFactor;
  /**
   * The FRA rate.
   */
  private final double _rate;
  /**
   * The holiday calendar associated with the ibor fixing.
   */
  private final Calendar _calendar;

  /**
   * Constructor of a FRA from contract details and the Ibor index. The payment currency is the index currency.
   * The fixing period dates are computed from the index conventions.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date (should be a good business day).
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param index The coupon Ibor index. Should of the same currency as the payment.
   * @param calendar The holiday calendar for the ibor leg.
   * @param rate The FRA rate.
   */
  public ForwardRateAgreementDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate,
      final ZonedDateTime accrualEndDate, final double accrualFactor, final double notional, final ZonedDateTime fixingDate, final IborIndex index, final double rate,
      final Calendar calendar) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate);
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "index currency different from payment currency");
    _index = index;
    _fixingPeriodStartDate = ScheduleCalculator.getAdjustedDate(fixingDate, _index.getSpotLag(), calendar);
    _fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(_fixingPeriodStartDate, index.getTenor(), index.getBusinessDayConvention(), calendar,
        index.isEndOfMonth());
    _fixingPeriodAccrualFactor = index.getDayCount().getDayCountFraction(_fixingPeriodStartDate, _fixingPeriodEndDate, calendar);
    _rate = rate;
    _calendar = calendar;
  }

  /**
   * Constructor of a FRA from contract details and the Ibor index. The payment currency is the index currency.
   * There is no check that the fixing period dates are in line with index conventions.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date (should be a good business day).
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param fixingPeriodStartDate The coupon fixing period start date.
   * @param fixingPeriodEndDate The coupon fixing period end date.
   * @param index The coupon Ibor index. Should of the same currency as the payment.
   * @param rate The FRA rate.
   * @param calendar The holiday calendar for the ibor leg.
   */
  public ForwardRateAgreementDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate,
      final ZonedDateTime accrualEndDate, final double accrualFactor, final double notional, final ZonedDateTime fixingDate,
      final ZonedDateTime fixingPeriodStartDate, final ZonedDateTime fixingPeriodEndDate, final IborIndex index, final double rate,
      final Calendar calendar) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate);
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "index currency different from payment currency");
    _index = index;
    _fixingPeriodStartDate = fixingPeriodStartDate;
    _fixingPeriodEndDate = fixingPeriodEndDate;
    _fixingPeriodAccrualFactor = index.getDayCount().getDayCountFraction(_fixingPeriodStartDate, _fixingPeriodEndDate, calendar);
    _rate = rate;
    _calendar = calendar;
  }

  /**
   * Builder of FRA from a coupon, the fixing date and the index. The fixing period dates are deduced from the index and the fixing date.
   * @param coupon Underlying coupon.
   * @param fixingDate The coupon fixing date.
   * @param index The FRA Ibor index.
   * @param rate The FRA rate.
   * @param calendar The holiday calendar for the ibor leg.
   * @return The FRA.
   */
  public static ForwardRateAgreementDefinition from(final CouponDefinition coupon, final ZonedDateTime fixingDate, final IborIndex index, final double rate,
      final Calendar calendar) {
    ArgumentChecker.notNull(coupon, "coupon");
    ArgumentChecker.notNull(fixingDate, "fixing date");
    ArgumentChecker.notNull(index, "index");
    return new ForwardRateAgreementDefinition(index.getCurrency(), coupon.getPaymentDate(), coupon.getAccrualStartDate(), coupon.getAccrualEndDate(),
        coupon.getPaymentYearFraction(), coupon.getNotional(), fixingDate, index, rate, calendar);
  }

  /**
   * Builder of FRA from a coupon, the fixing date and the index. The fixing period dates are deduced from the index and the fixing date.
   * @param tradeDate The FRA trade date.
   * @param startPeriod The period from trade to start date.
   * @param notional The notional
   * @param index The FRA Ibor index.
   * @param rate The FRA rate.
   * @param calendar The holiday calendar for the ibor leg.
   * @return The FRA.
   */
  public static ForwardRateAgreementDefinition fromTrade(final ZonedDateTime tradeDate, final Period startPeriod, final double notional, final IborIndex index,
      final double rate, final Calendar calendar) {
    ArgumentChecker.notNull(tradeDate, "trade date");
    ArgumentChecker.notNull(startPeriod, "start period");
    ArgumentChecker.notNull(index, "index");
    final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(tradeDate, index.getSpotLag(), calendar);
    final ZonedDateTime accrualStartDate = ScheduleCalculator.getAdjustedDate(spotDate, startPeriod, index, calendar);
    final Period endPeriod = startPeriod.plus(index.getTenor());
    final ZonedDateTime accrualEndDate = ScheduleCalculator.getAdjustedDate(spotDate, endPeriod, index, calendar);
    final ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(accrualStartDate, -index.getSpotLag(), calendar);
    final double accrualFactor = index.getDayCount().getDayCountFraction(accrualStartDate, accrualEndDate, calendar);
    return new ForwardRateAgreementDefinition(index.getCurrency(), accrualStartDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate, index, rate,
        calendar);
  }

  /**
   * Builder of FRA from the accrual start date, the accrual end date and the index.
   * The fixing period dates are computed from the index conventions.
   * @param accrualStartDate (Unadjusted) start date of the accrual period
   * @param accrualEndDate (Unadjusted) end date of the accrual period
   * @param notional The notional
   * @param index The FRA Ibor index.
   * @param rate The FRA rate.
   * @param calendar The holiday calendar for the ibor leg & payment date.
   * @return The FRA.
   */
  public static ForwardRateAgreementDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double notional,
      final IborIndex index, final double rate, final Calendar calendar) {
    ArgumentChecker.notNull(accrualStartDate, "accrual start date");
    ArgumentChecker.notNull(accrualEndDate, "accrual end date");
    ArgumentChecker.notNull(index, "index");
    final ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(accrualStartDate, -index.getSpotLag(), calendar);
    final double paymentAccrualFactor = index.getDayCount().getDayCountFraction(accrualStartDate, accrualEndDate, calendar);
    return new ForwardRateAgreementDefinition(index.getCurrency(), accrualStartDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional, fixingDate, index,
        rate, calendar);
  }

  /**
   * Builder of FRA from the accrual start date, the accrual end date and the index.
   * The fixing period dates are computed from the index conventions.
   * @param accrualStartDate (Unadjusted) start date of the accrual period
   * @param accrualEndDate (Unadjusted) end date of the accrual period
   * @param notional The notional
   * @param index The FRA Ibor index.
   * @param rate The FRA rate.
   * @param fixingCalendar The holiday calendar for the ibor leg.
   * @param paymentCalendar the payment calendar.
   * @return The FRA.
   */
  public static ForwardRateAgreementDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double notional,
      final IborIndex index, final double rate, final Calendar fixingCalendar, final Calendar paymentCalendar) {
    ArgumentChecker.notNull(accrualStartDate, "accrual start date");
    ArgumentChecker.notNull(accrualEndDate, "accrual end date");
    ArgumentChecker.notNull(index, "index");
    final ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(accrualStartDate, -index.getSpotLag(), fixingCalendar);
    final ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(accrualStartDate, 0, paymentCalendar);
    final double paymentAccrualFactor = index.getDayCount().getDayCountFraction(accrualStartDate, accrualEndDate, fixingCalendar);
    return new ForwardRateAgreementDefinition(index.getCurrency(), paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional, fixingDate, index,
        rate, fixingCalendar);
  }

  /**
   * Builder of FRA from the accrual start date, the accrual end date and the index.
   * The fixing period dates are computed from the index conventions.
   * @param accrualStartDate (Unadjusted) start date of the accrual period
   * @param accrualEndDate (Unadjusted) end date of the accrual period
   * @param notional The notional
   * @param fixingDate The fixing date
   * @param index The FRA Ibor index.
   * @param rate The FRA rate.
   * @param fixingCalendar The holiday calendar for the ibor leg.
   * @param paymentCalendar the payment calendar.
   * @return The FRA.
   */
  public static ForwardRateAgreementDefinition from(final ZonedDateTime accrualStartDate, 
      final ZonedDateTime accrualEndDate, final double notional, final ZonedDateTime fixingDate, final IborIndex index, 
      final double rate, final Calendar fixingCalendar, final Calendar paymentCalendar) {
    ArgumentChecker.notNull(accrualStartDate, "accrual start date");
    ArgumentChecker.notNull(accrualEndDate, "accrual end date");
    ArgumentChecker.notNull(fixingDate, "fixing date");
    ArgumentChecker.notNull(index, "index");
    final ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(accrualStartDate, 0, paymentCalendar);
    final double paymentAccrualFactor = 
        index.getDayCount().getDayCountFraction(accrualStartDate, accrualEndDate, fixingCalendar);
    return new ForwardRateAgreementDefinition(index.getCurrency(), paymentDate, accrualStartDate, accrualEndDate, 
        paymentAccrualFactor, notional, fixingDate, index, rate, fixingCalendar);
  }
  
  /**
   * Gets the Ibor index of the instrument.
   * @return The index.
   */
  public IborIndex getIndex() {
    return _index;
  }

  /**
   * Gets the fixing period start date.
   * @return The fixing period start date.
   */
  public ZonedDateTime getFixingPeriodStartDate() {
    return _fixingPeriodStartDate;
  }

  /**
   * Gets the fixing period end date field.
   * @return the fixing period end date
   */
  public ZonedDateTime getFixingPeriodEndDate() {
    return _fixingPeriodEndDate;
  }

  /**
   * Gets the fixingPeriodAccrualFactor field.
   * @return the fixing period accrual factor
   */
  public double getFixingPeriodAccrualFactor() {
    return _fixingPeriodAccrualFactor;
  }

  /**
   * Gets the FRA rate.
   * @return The rate.
   */
  public double getRate() {
    return _rate;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitForwardRateAgreementDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitForwardRateAgreementDefinition(this);
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public Payment toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(date.isBefore(getFixingDate()), "Do not have any fixing data but are asking for a derivative after the fixing date " + getFixingDate() + " "
        + date);
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.isTrue(yieldCurveNames.length > 1, "at least two curve names are required");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    final String fundingCurveName = yieldCurveNames[0];
    final String forwardCurveName = yieldCurveNames[1];
    final ZonedDateTime zonedDate = date.toLocalDate().atStartOfDay(ZoneOffset.UTC);
    final double paymentTime = actAct.getDayCountFraction(zonedDate, getPaymentDate(), _calendar);
    // Ibor is not fixed yet, all the details are required.
    final double fixingTime = actAct.getDayCountFraction(zonedDate, getFixingDate(), _calendar);
    final double fixingPeriodStartTime = actAct.getDayCountFraction(zonedDate, getFixingPeriodStartDate(), _calendar);
    final double fixingPeriodEndTime = actAct.getDayCountFraction(zonedDate, getFixingPeriodEndDate(), _calendar);
    return new ForwardRateAgreement(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), _index, fixingTime, fixingPeriodStartTime,
        fixingPeriodEndTime, getFixingPeriodAccrualFactor(), _rate, forwardCurveName);
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public Payment toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries, final String... yieldCurveNames) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.isTrue(yieldCurveNames.length > 1, "at least one curve required");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    final String fundingCurveName = yieldCurveNames[0];
    final String forwardCurveName = yieldCurveNames[1];
    final ZonedDateTime zonedDate = date.toLocalDate().atStartOfDay(ZoneOffset.UTC);
    final double paymentTime = actAct.getDayCountFraction(zonedDate, getPaymentDate(), _calendar);
    if (date.isAfter(getFixingDate()) || (date.equals(getFixingDate()))) {
      Double fixedRate = indexFixingTimeSeries.getValue(getFixingDate());
      //TODO remove me when times are sorted out in the swap definitions or we work out how to deal with this another way
      if (fixedRate == null) {
        final ZonedDateTime fixingDateAtLiborFixingTime = getFixingDate().with(LocalTime.of(11, 0));
        fixedRate = indexFixingTimeSeries.getValue(fixingDateAtLiborFixingTime);
      }
      if (fixedRate == null) {
        final ZonedDateTime previousBusinessDay = PRECEDING_BDC.adjustDate(_calendar, getFixingDate().minusDays(1));
        fixedRate = indexFixingTimeSeries.getValue(previousBusinessDay);
        //TODO remove me when times are sorted out in the swap definitions or we work out how to deal with this another way
        if (fixedRate == null) {
          final ZonedDateTime previousBusinessDayAtLiborFixingTime = previousBusinessDay.with(LocalTime.of(11, 0));
          fixedRate = indexFixingTimeSeries.getValue(previousBusinessDayAtLiborFixingTime);
        }
        if (fixedRate == null) {
          throw new OpenGammaRuntimeException("Could not get fixing value for date " + getFixingDate());
        }
      }
      return new CouponFixed(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), fixedRate - _rate);
    }

    // Ibor is not fixed yet, all the details are required.
    final double fixingTime = actAct.getDayCountFraction(zonedDate, getFixingDate(), _calendar);
    final double fixingPeriodStartTime = actAct.getDayCountFraction(zonedDate, getFixingPeriodStartDate(), _calendar);
    final double fixingPeriodEndTime = actAct.getDayCountFraction(zonedDate, getFixingPeriodEndDate(), _calendar);
    return new ForwardRateAgreement(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), _index, fixingTime, fixingPeriodStartTime,
        fixingPeriodEndTime, getFixingPeriodAccrualFactor(), _rate, forwardCurveName);
  }

  @Override
  public Payment toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(date.isBefore(getFixingDate()), 
        "Do not have any fixing data but are asking for a derivative after the fixing date " + getFixingDate() + " " + date);
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    final ZonedDateTime zonedDate = date.toLocalDate().atStartOfDay(ZoneOffset.UTC);
    final double paymentTime = actAct.getDayCountFraction(zonedDate, getPaymentDate(), _calendar);
    // Ibor is not fixed yet, all the details are required.
    final double fixingTime = actAct.getDayCountFraction(zonedDate, getFixingDate(), _calendar);
    final double fixingPeriodStartTime = actAct.getDayCountFraction(zonedDate, getFixingPeriodStartDate(), _calendar);
    final double fixingPeriodEndTime = actAct.getDayCountFraction(zonedDate, getFixingPeriodEndDate(), _calendar);
    return new ForwardRateAgreement(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), _index, fixingTime, fixingPeriodStartTime,
        fixingPeriodEndTime, getFixingPeriodAccrualFactor(), _rate);
  }

  @Override
  public Payment toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    final ZonedDateTime zonedDate = date.toLocalDate().atStartOfDay(ZoneOffset.UTC);
    final double paymentTime = actAct.getDayCountFraction(zonedDate, getPaymentDate(), _calendar);
    if (date.isAfter(getFixingDate()) || (date.equals(getFixingDate()))) {
      Double fixedRate = indexFixingTimeSeries.getValue(getFixingDate());
      //TODO remove me when times are sorted out in the swap definitions or we work out how to deal with this another way
      if (fixedRate == null) {
        final ZonedDateTime fixingDateAtLiborFixingTime = getFixingDate().with(LocalTime.of(11, 0));
        fixedRate = indexFixingTimeSeries.getValue(fixingDateAtLiborFixingTime);
      }
      if (fixedRate == null) {
        final ZonedDateTime previousBusinessDay = PRECEDING_BDC.adjustDate(_calendar,
            getFixingDate().minusDays(1));
        fixedRate = indexFixingTimeSeries.getValue(previousBusinessDay);
        //TODO remove me when times are sorted out in the swap definitions or we work out how to deal with this another way
        if (fixedRate == null) {
          final ZonedDateTime previousBusinessDayAtLiborFixingTime = previousBusinessDay.with(LocalTime.of(11, 0));
          fixedRate = indexFixingTimeSeries.getValue(previousBusinessDayAtLiborFixingTime);
        }
        if (fixedRate == null) {
          throw new OpenGammaRuntimeException("Could not get fixing value for date " + getFixingDate());
        }
      }
      return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixedRate - _rate);
    }

    // Ibor is not fixed yet, all the details are required.
    final double fixingTime = actAct.getDayCountFraction(zonedDate, getFixingDate(), _calendar);
    final double fixingPeriodStartTime = actAct.getDayCountFraction(zonedDate, getFixingPeriodStartDate(), _calendar);
    final double fixingPeriodEndTime = actAct.getDayCountFraction(zonedDate, getFixingPeriodEndDate(), _calendar);
    return new ForwardRateAgreement(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), _index, fixingTime, fixingPeriodStartTime,
        fixingPeriodEndTime, getFixingPeriodAccrualFactor(), _rate);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_fixingPeriodAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _fixingPeriodEndDate.hashCode();
    result = prime * result + _fixingPeriodStartDate.hashCode();
    result = prime * result + _index.hashCode();
    temp = Double.doubleToLongBits(_rate);
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
    final ForwardRateAgreementDefinition other = (ForwardRateAgreementDefinition) obj;
    if (Double.doubleToLongBits(_fixingPeriodAccrualFactor) != Double.doubleToLongBits(other._fixingPeriodAccrualFactor)) {
      return false;
    }
    if (!ObjectUtils.equals(_fixingPeriodEndDate, other._fixingPeriodEndDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_fixingPeriodStartDate, other._fixingPeriodStartDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    if (Double.doubleToLongBits(_rate) != Double.doubleToLongBits(other._rate)) {
      return false;
    }
    return true;
  }
}
