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
 * Generator (or template) for leg paying arithmetic average of overnight rate (plus a spread).
 * The generated coupons have all the intermediary date.
 */
public class GeneratorLegONArithmeticAverage extends GeneratorLegONAbstract {

  /**
   * Constructor from all the details.
   * @param name The generator name.
   * @param ccy The leg currency.
   * @param indexON The overnight index underlying the leg.
   * @param paymentPeriod The period between two payments.
   * @param spotOffset The offset in business days between trade and settlement date (usually 2 or 0).
   * @param paymentOffset The offset in days between the last ON fixing date and the coupon payment.
   * @param businessDayConvention The business day convention for the payments.
   * @param endOfMonth The flag indicating if the end-of-month rule is used.
   * @param stubType The stub type.
   * @param isExchangeNotional Whether the notional exchanged (at start and at end).
   * @param indexCalendar The calendar associated with the overnight index.
   * @param paymentCalendar The calendar used for the payments.
   */
  public GeneratorLegONArithmeticAverage(String name, Currency ccy, IndexON indexON, Period paymentPeriod, 
      int spotOffset, int paymentOffset, BusinessDayConvention businessDayConvention, boolean endOfMonth, 
      StubType stubType, boolean isExchangeNotional, Calendar indexCalendar, Calendar paymentCalendar) {
    super(name, ccy, indexON, paymentPeriod, spotOffset, paymentOffset, businessDayConvention, endOfMonth, stubType, 
        isExchangeNotional, indexCalendar, paymentCalendar);
  }

  @Override
  public AnnuityDefinition<?> generateInstrument(final ZonedDateTime date, final double marketQuote, 
      final double notional, final GeneratorAttributeIR attribute) {
    ArgumentChecker.notNull(date, "Reference date");
    ArgumentChecker.notNull(attribute, "Attributes");
    ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getSpotOffset(), getPaymentCalendar());
    ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spot, attribute.getStartPeriod(), 
        getBusinessDayConvention(), getPaymentCalendar(), isEndOfMonth());
    ZonedDateTime endDate = startDate.plus(attribute.getEndPeriod());
    NotionalProvider notionalProvider = new NotionalProvider() {
      @Override
      public double getAmount(final LocalDate date) {
        return notional;
      }
    };
    AdjustedDateParameters adjustedDateIndex = new AdjustedDateParameters(getIndexCalendar(), getBusinessDayConvention());
    OffsetAdjustedDateParameters offsetFixing = new OffsetAdjustedDateParameters(0, OffsetType.BUSINESS, 
        getIndexCalendar(), BusinessDayConventionFactory.of("Following"));
    AnnuityDefinition<?> leg = new FloatingAnnuityDefinitionBuilder().
        payer(false).notional(notionalProvider).startDate(startDate.toLocalDate()).endDate(endDate.toLocalDate()).
        index(getIndexON()).accrualPeriodFrequency(getPaymentPeriod()).
        rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).resetDateAdjustmentParameters(adjustedDateIndex).
        accrualPeriodParameters(adjustedDateIndex).dayCount(getIndexON().getDayCount()).
        fixingDateAdjustmentParameters(offsetFixing).currency(getIndexON().getCurrency()).spread(marketQuote).build();
    return leg;
  }

}
