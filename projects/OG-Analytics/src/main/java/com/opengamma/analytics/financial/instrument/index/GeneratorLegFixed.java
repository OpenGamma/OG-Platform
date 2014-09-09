/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.NotionalProvider;
import com.opengamma.analytics.financial.instrument.annuity.AdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.FixedAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Generator (or template) for leg paying a fixed rate.
 */
public class GeneratorLegFixed extends GeneratorLeg {
  
  /** The offset in business days between trade and settlement date (usually 2 or 0). */
  private final int _spotOffset;
  /** The period between two payments. */
  private final Period _paymentPeriod;
  /** Day count convention */
  private final DayCount _dayCount;
  /** The business day convention for the payments. */
  private final BusinessDayConvention _businessDayConvention;
  /** The offset in days between end of the accrual period and the payment. */
  private final int _paymentOffset;
  /** The flag indicating if the end-of-month rule is used. */
  private final boolean _endOfMonth;
  /** The stub type. */
  private final StubType _stubType;
  /** Whether the notional exchanged (at start and at end). */
  private final boolean _isExchangeNotional;
  /** The calendar used for the payments. */
  private final Calendar _paymentCalendar;
  
  /**
   * Constructor.
   * @param name The generator name.
   * @param ccy The leg currency.
   * @param spotOffset The offset in business days between trade and settlement date (usually 2 or 0).
   * @param paymentPeriod The period between two payments.
   * @param dayCount The payment day count.
   * @param businessDayConvention The business day convention for the payments.
   * @param paymentOffset The offset in days between the end accrual date and the coupon payment.
   * @param endOfMonth The flag indicating if the end-of-month rule is used.
   * @param stubType The stub type.
   * @param isExchangeNotional Whether the notional exchanged (at start and at end).
   * @param paymentCalendar The calendar used to adjust the payments.
   */
  public GeneratorLegFixed(String name, Currency ccy, int spotOffset, Period paymentPeriod, DayCount dayCount, 
      BusinessDayConvention businessDayConvention, int paymentOffset, boolean endOfMonth, StubType stubType, 
      boolean isExchangeNotional, Calendar paymentCalendar) {
    super(name, ccy);
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(dayCount, "day count");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.notNull(stubType, "stub type");
    ArgumentChecker.notNull(paymentCalendar, "payment calendar");
    _spotOffset = spotOffset;
    _paymentPeriod = paymentPeriod;
    _dayCount = dayCount;
    _businessDayConvention = businessDayConvention;
    _paymentOffset = paymentOffset;
    _endOfMonth = endOfMonth;
    _stubType = stubType;
    _isExchangeNotional = isExchangeNotional;
    _paymentCalendar = paymentCalendar;
  }

  /**
   * Gets the spot offset.
   * @return the spot offset.
   */
  public int getSpotOffset() {
    return _spotOffset;
  }

  /**
   * Gets the payment period.
   * @return the payment period.
   */
  public Period getPaymentPeriod() {
    return _paymentPeriod;
  }

  /**
   * Returns the day count convention.
   * @return The day count.
   */
  public DayCount getDayCount() {
    return _dayCount;
  }

  /**
   * Returns the business day convention.
   * @return The convention.
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
  }

  /**
   * Gets the payment offset.
   * @return the paymentOffset
   */
  public int getPaymentOffset() {
    return _paymentOffset;
  }

  /**
   * Returns the end-of-month flag.
   * @return The flag.
   */
  public boolean isEndOfMonth() {
    return _endOfMonth;
  }

  /**
   * Returns the stub type.
   * @return The stub type.
   */
  public StubType getStubType() {
    return _stubType;
  }

  /**
   * Gets the notional exchange flag.
   * @return The flag.
   */
  public boolean isExchangeNotional() {
    return _isExchangeNotional;
  }

  /**
   * Returns the payment calendar.
   * @return The calendar.
   */
  public Calendar getPaymentCalendar() {
    return _paymentCalendar;
  }

  @Override
  public AnnuityDefinition<?> generateInstrument(final ZonedDateTime date, final double marketQuote, 
      final double notional, final GeneratorAttributeIR attribute) {
    ArgumentChecker.notNull(date, "Reference date");
    ArgumentChecker.notNull(attribute, "Attributes");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, _spotOffset, _paymentCalendar);
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spot, attribute.getStartPeriod(), 
        _businessDayConvention, _paymentCalendar, _endOfMonth);
    final ZonedDateTime endDate = startDate.plus(attribute.getEndPeriod());
    NotionalProvider notionalProvider = new NotionalProvider() {
      @Override
      public double getAmount(final LocalDate date) {
        return notional;
      }
    };
    AdjustedDateParameters adjustedDate = new AdjustedDateParameters(_paymentCalendar, _businessDayConvention);
    AnnuityDefinition<?> leg = new FixedAnnuityDefinitionBuilder().payer(false).currency(getCurrency()).
        notional(notionalProvider).startDate(startDate.toLocalDate()).endDate(endDate.toLocalDate()).
        dayCount(_dayCount).accrualPeriodFrequency(_paymentPeriod).rate(marketQuote).
        accrualPeriodParameters(adjustedDate).
        exchangeInitialNotional(_isExchangeNotional).startDateAdjustmentParameters(adjustedDate).
        exchangeFinalNotional(_isExchangeNotional).endDateAdjustmentParameters(adjustedDate).
        build();
    return leg;
  }
  
  

}
