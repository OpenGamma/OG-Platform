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
import com.opengamma.util.money.Currency;

/**
 * Generator (or template) for leg paying an Ibor rate (plus a spread).
 */
public class GeneratorLegIbor extends GeneratorLeg {

  /** The ON index on which the fixing is done. */
  private final IborIndex _indexIbor;
  /** The period between two payments. */
  private final Period _paymentPeriod;
  /** The offset in business days between trade and settlement date (usually 2 or 0). */
  private final int _spotOffset;
  /** The offset in days between end of the accrual period and the payment. */
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
   * @param ccy The leg currency.
   * @param indexIbor The overnight index underlying the leg.
   * @param paymentPeriod The period between two payments.
   * @param spotOffset The offset in business days between trade and settlement date (usually 2 or 0).
   * @param paymentOffset The offset in days between the last ON fixing date and the coupon payment.
   * @param businessDayConvention The business day convention for the payments.
   * @param endOfMonth The flag indicating if the end-of-month rule is used.
   * @param stubType The stub type.
   * @param isExchangeNotional Whether the notional exchanged (at start and at end).
   * @param indexCalendar The calendar associated with the overnight index.
   * @param paymentCalendar The calendar used to adjust the payments.
   */
  public GeneratorLegIbor(String name, Currency ccy, IborIndex indexIbor, Period paymentPeriod, int spotOffset, 
      int paymentOffset, BusinessDayConvention businessDayConvention, boolean endOfMonth, StubType stubType, 
      boolean isExchangeNotional, Calendar indexCalendar, Calendar paymentCalendar) {
    super(name, ccy);
    ArgumentChecker.notNull(indexIbor, "Index Ibor");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    _indexIbor = indexIbor;
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
   * Gets the underlying Ibor index.
   * @return The index.
   */
  public IborIndex getIndexIbor() {
    return _indexIbor;
  }

  /**
   * Gets the payment period.
   * @return the payment period.
   */
  public Period getPaymentPeriod() {
    return _paymentPeriod;
  }

  /**
   * Gets the spot offset.
   * @return the spot offset.
   */
  public int getSpotOffset() {
    return _spotOffset;
  }

  /**
   * Gets the payment offset.
   * @return the paymentOffset
   */
  public int getPaymentOffset() {
    return _paymentOffset;
  }

  /**
   * Gets the business day convention.
   * @return the businessDayConvention
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
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
   * Gets the indexCalendar.
   * @return the indexCalendar
   */
  public Calendar getIndexCalendar() {
    return _indexCalendar;
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
    AdjustedDateParameters adjustedDateIndex = new AdjustedDateParameters(_indexCalendar, _businessDayConvention);
    OffsetAdjustedDateParameters offsetFixing = new OffsetAdjustedDateParameters(-_indexIbor.getSpotLag(), 
        OffsetType.BUSINESS, _indexCalendar, BusinessDayConventionFactory.of("Following"));
    AnnuityDefinition<?> leg = new FloatingAnnuityDefinitionBuilder().
        payer(false).notional(notionalProvider).startDate(startDate.toLocalDate()).endDate(endDate.toLocalDate()).
        index(_indexIbor).accrualPeriodFrequency(_paymentPeriod).
        rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).resetDateAdjustmentParameters(adjustedDateIndex).
        accrualPeriodParameters(adjustedDateIndex).dayCount(_indexIbor.getDayCount()).
        fixingDateAdjustmentParameters(offsetFixing).currency(_indexIbor.getCurrency()).spread(marketQuote).
        exchangeInitialNotional(_isExchangeNotional).startDateAdjustmentParameters(adjustedDateIndex).
        exchangeFinalNotional(_isExchangeNotional).endDateAdjustmentParameters(adjustedDateIndex).
        build();
    return leg;
  }

}
