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
import com.opengamma.analytics.financial.instrument.annuity.CompoundingMethod;
import com.opengamma.analytics.financial.instrument.annuity.FloatingAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.OffsetAdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.OffsetType;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.util.ArgumentChecker;

/**
 * Generator (or template) for leg paying composition of overnight rate (plus a spread).
 */
public class GeneratorLegONCompounded extends GeneratorInstrument<GeneratorAttributeIR> {

  /** The ON index on which the fixing is done. */
  private final IndexON _indexON;
  /** The period between two payments. */
  private final Period _paymentPeriod;
  /** The offset in business days between trade and settlement date (usually 2 or 0). */
  private final int _spotOffset;
  /** The offset in business days between the end of the accrual period and the coupon payment. */
  private final int _paymentOffset;
  /** The business day convention for the payments. */
  private final BusinessDayConvention _businessDayConvention;
  /** The flag indicating if the end-of-month rule is used. */
  private final boolean _endOfMonth;
  /** The stub type. */
  private final StubType _stubType;
  /** Whether the notional exchanged (at start and at end). */
  private final boolean _isExchangeNotional;
  /** The calendar associated with the overnight index. */
  private final Calendar _indexCalendar;
  /** The calendar used for the payments. */
  private final Calendar _paymentCalendar;

  /**
   * Constructor from all the details.
   * @param name The generator name.
   * @param indexON The overnight index underlying the leg.
   * @param paymentPeriod The period between two payments.
   * @param spotOffset The offset in business days between trade and settlement date (usually 2 or 0).
   * @param paymentOffset The offset in business days between the end of the accrual period and the coupon payment.
   * @param businessDayConvention The business day convention for the payments.
   * @param endOfMonth The flag indicating if the end-of-month rule is used.
   * @param stubType The stub type.
   * @param isExchangeNotional Whether the notional exchanged (at start and at end).
   * @param indexCalendar The calendar associated with the overnight index.
   * @param paymentCalendar The calendar used for the payments.
   */
  public GeneratorLegONCompounded(String name, IndexON indexON, Period paymentPeriod, int spotOffset, int paymentOffset,
      BusinessDayConvention businessDayConvention, boolean endOfMonth, StubType stubType, boolean isExchangeNotional,
      Calendar indexCalendar, Calendar paymentCalendar) {
    super(name);
    ArgumentChecker.notNull(name, "Name");
    ArgumentChecker.notNull(indexON, "Index ON");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.notNull(stubType, "Stub type");
    ArgumentChecker.notNull(indexCalendar, "Index calendar");
    ArgumentChecker.notNull(paymentCalendar, "payment calendar");
    _indexON = indexON;
    _paymentPeriod = paymentPeriod;
    _spotOffset = spotOffset;
    _paymentOffset = paymentOffset;
    _businessDayConvention = businessDayConvention;
    _endOfMonth = endOfMonth;
    _stubType = stubType;
    _isExchangeNotional = isExchangeNotional;
    _indexCalendar = indexCalendar;
    _paymentCalendar = paymentCalendar;
  }

  /**
   * Gets the indexON.
   * @return the indexON
   */
  public IndexON getIndexON() {
    return _indexON;
  }

  /**
   * Gets the paymentPeriod.
   * @return the paymentPeriod
   */
  public Period getPaymentPeriod() {
    return _paymentPeriod;
  }

  /**
   * Returns the offset in business days between trade and settlement date.
   * @return The spot offset.
   */
  public int getSpotOffset() {
    return _spotOffset;
  }

  /**
   * Returns the offset in business days between the end of the accrual period and the coupon payment.
   * @return The payment offset.
   */
  public int getPaymentOffset() {
    return _paymentOffset;
  }

  /**
   * Gets the businessDayConvention.
   * @return the businessDayConvention
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
  }

  /**
   * Gets the endOfMonth.
   * @return the endOfMonth
   */
  public boolean isEndOfMonth() {
    return _endOfMonth;
  }

  /**
   * Gets the stubType.
   * @return the stubType
   */
  public StubType getStubType() {
    return _stubType;
  }

  /**
   * Gets the isExchangeNotional.
   * @return the isExchangeNotional
   */
  public boolean isExchangeNotional() {
    return _isExchangeNotional;
  }

  /**
   * Gets the indexCalendar.
   * @return the indexCalendar
   */
  public Calendar getIndexCalendar() {
    return _indexCalendar;
  }

  /**
   * Gets the paymentCalendar.
   * @return the paymentCalendar
   */
  public Calendar getPaymentCalendar() {
    return _paymentCalendar;
  }

  @Override
  public AnnuityDefinition<?> generateInstrument(final ZonedDateTime date, final double marketQuote, final double notional, final GeneratorAttributeIR attribute) {
    ArgumentChecker.notNull(date, "Reference date");
    ArgumentChecker.notNull(attribute, "Attributes");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, _spotOffset, _paymentCalendar);
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spot, attribute.getStartPeriod(), _businessDayConvention, _paymentCalendar, _endOfMonth);
    final ZonedDateTime endDate = startDate.plus(attribute.getEndPeriod());
    NotionalProvider notionalProvider = new NotionalProvider() {
      @Override
      public double getAmount(final LocalDate date) {
        return notional;
      }
    };
    AdjustedDateParameters adjustedDateIndex = new AdjustedDateParameters(_indexCalendar, _businessDayConvention);
    OffsetAdjustedDateParameters offsetFixing = new OffsetAdjustedDateParameters(0, OffsetType.BUSINESS, _indexCalendar, 
        BusinessDayConventionFactory.of("Following"));
    OffsetAdjustedDateParameters offsetPayment = new OffsetAdjustedDateParameters(2, OffsetType.BUSINESS, _paymentCalendar, 
        BusinessDayConventionFactory.of("Following"));
    AnnuityDefinition<?> leg = new FloatingAnnuityDefinitionBuilder().
        payer(false).notional(notionalProvider).startDate(startDate.toLocalDate()).endDate(endDate.toLocalDate()).index(_indexON).
        accrualPeriodFrequency(_paymentPeriod).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
        resetDateAdjustmentParameters(adjustedDateIndex).accrualPeriodParameters(adjustedDateIndex).
        dayCount(_indexON.getDayCount()).fixingDateAdjustmentParameters(offsetFixing).currency(_indexON.getCurrency()).spread(marketQuote).
        compoundingMethod(CompoundingMethod.SPREAD_EXCLUSIVE).paymentDateAdjustmentParameters(offsetPayment).build();
    return leg;
  }

}
