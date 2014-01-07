/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.marketdata.historical.HistoricalShockMarketDataSnapshot.ShockType;
import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.HistoricalShockMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionSequence;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Target for {@link HistoricalViewEvaluationFunction} which ensures that an execution sequence is constructed which matches the function's expectations.
 */
public class HistoricalViewEvaluationTarget extends ViewEvaluationTarget {

  /**
   * Fudge field containing the start date
   */
  protected static final String START_DATE_FIELD = "startDate";
  /**
   * Fudge field containing whether the start date is inclusive.
   */
  protected static final String INCLUDE_START_FIELD = "includeStart";
  /**
   * Fudge field containing the end date.
   */
  protected static final String END_DATE_FIELD = "endDate";
  /**
   * Fudge field containing whether the end date is inclusive.
   */
  protected static final String INCLUDE_END_FIELD = "includeEnd";
  /**
   * Repeated Fudge field, each containing a currency ISO code for the compound holiday calendar to use.
   */
  protected static final String CURRENCY_CALENDAR_FIELD = "currencyCalendar";
  /**
   * Fudge field containing the market data mode.
   */
  protected static final String MARKET_DATA_MODE_FIELD = "marketDataMode";

  /**
   * Creates a new target.
   * 
   * @param user  the user the view is created for, not null.
   * @param startDate  the start date as a {@link DateConstraint} encoded string, not null
   * @param includeStart  whether to include the start date in the evaluation.
   * @param endDate  the end date as a {@link DateConstraint} encoded string, not null
   * @param includeEnd  whether to include the end date in the evaluation.
   * @param currencyCalendars  the currencies for which to obtain a compound holiday calendar in order to obtain a valid sequence of dates, may be null
   * @param marketDataMode  the mode in which market data should be obtained, not null
   */
  public HistoricalViewEvaluationTarget(UserPrincipal user, String startDate, boolean includeStart, String endDate,
      boolean includeEnd, Set<Currency> currencyCalendars, HistoricalViewEvaluationMarketDataMode marketDataMode) {
    super(user, new HistoricalSequence(startDate, includeStart, endDate, includeEnd, currencyCalendars, marketDataMode));
  }

  protected HistoricalViewEvaluationTarget(ViewDefinition viewDefinition, final HistoricalSequence sequence) {
    super(viewDefinition, sequence);
  }

  private HistoricalViewEvaluationTarget(FudgeDeserializer deserializer, FudgeMsg message) {
    this(deserializer, message,
        message.getString(START_DATE_FIELD),
        message.getBoolean(INCLUDE_START_FIELD),
        message.getString(END_DATE_FIELD),
        message.getBoolean(INCLUDE_END_FIELD),
        getCurrencyCalendars(deserializer, message),
        deserializer.fieldValueToObject(HistoricalViewEvaluationMarketDataMode.class, message.getByName(MARKET_DATA_MODE_FIELD)));
  }

  private HistoricalViewEvaluationTarget(FudgeDeserializer deserializer, FudgeMsg message, String startDate,
      boolean includeStart, String endDate, boolean includeEnd, Set<Currency> currencyCalendars, HistoricalViewEvaluationMarketDataMode marketDataMode) {
    super(deserializer, message, new HistoricalSequence(startDate, includeStart, endDate, includeEnd, currencyCalendars, marketDataMode));
  }

  protected HistoricalViewEvaluationTarget(final HistoricalViewEvaluationTarget copyFrom, final UniqueId uid) {
    super(copyFrom, uid);
  }

  @Override
  protected ViewEvaluationTarget createUnion(final ViewDefinition newViewDefinition) {
    return new HistoricalViewEvaluationTarget(newViewDefinition, (HistoricalSequence) getExecutionSequence());
  }
  
  private static Set<Currency> getCurrencyCalendars(FudgeDeserializer deserializer, FudgeMsg message) {
    Set<Currency> currencies = new HashSet<Currency>();
    for (FudgeField ccyField : message.getAllByName(CURRENCY_CALENDAR_FIELD)) {
      Currency ccy = deserializer.fieldValueToObject(Currency.class, ccyField);
      currencies.add(ccy);
    }
    return !currencies.isEmpty() ? currencies : null;
  }

  private static final class HistoricalSequence implements ViewCycleExecutionSequenceDescriptor {

    /**
     * The description of the start date.
     */
    private final String _startDateDescriptor;
    /**
     * Indicates whether to include the start date.
     */
    private final boolean _includeStart;
    /**
     * The description of the end date.
     */
    private final String _endDateDescriptor;
    /**
     * Indicates whether to include the end date.
     */
    private final boolean _includeEnd;
    /**
     * The currencies for which to obtain a compound calendar in order to generate the correct sequence of dates, null to ignore holidays.
     */
    private final Set<Currency> _currencyCalendars;
    /**
     * The historical market data type
     */
    private final HistoricalViewEvaluationMarketDataMode _marketDataMode;

    public HistoricalSequence(String startDateDescriptor, boolean includeStart, String endDateDescriptor,
        boolean includeEnd, Set<Currency> currencyCalendars, HistoricalViewEvaluationMarketDataMode marketDataMode) {
      ArgumentChecker.notNull(startDateDescriptor, "startDate");
      ArgumentChecker.notNull(endDateDescriptor, "endDate");
      ArgumentChecker.notNull(marketDataMode, "marketDataMode");
      _startDateDescriptor = startDateDescriptor;
      _includeStart = includeStart;
      _endDateDescriptor = endDateDescriptor;
      _includeEnd = includeEnd;
      _currencyCalendars = currencyCalendars;
      _marketDataMode = marketDataMode;
    }

    // ViewCycleExecutionSequenceDescriptor

    @Override
    public ViewCycleExecutionSequence createSequence(FunctionExecutionContext executionContext) {
      LocalDate startDate = DateConstraint.evaluate(executionContext, _startDateDescriptor);
      LocalDate endDate = DateConstraint.evaluate(executionContext, _endDateDescriptor);
      if (!_includeStart) {
        startDate = startDate.plusDays(1);
      }
      if (!_includeEnd) {
        endDate = endDate.minusDays(1);
      }
      HolidaySourceCalendarAdapter calendar;
      if (_currencyCalendars != null) {
        Currency[] currencies = _currencyCalendars.toArray(new Currency[_currencyCalendars.size()]);
        HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
        calendar = new HolidaySourceCalendarAdapter(holidaySource, currencies);
      } else {
        calendar = null;
      }
      List<ViewCycleExecutionOptions> executionSequence = new LinkedList<ViewCycleExecutionOptions>();
      LocalDate previousWorkingDate = null;
      LocalDate currentDate = startDate;
      while (!currentDate.isAfter(endDate)) {
        if (calendar == null || calendar.isWorkingDay(currentDate)) {
          MarketDataSpecification marketDataSpec = createMarketDataSpec(previousWorkingDate, currentDate, LocalDate.now(executionContext.getValuationClock()));
          if (marketDataSpec != null) {
            ViewCycleExecutionOptions executionOptions = ViewCycleExecutionOptions.builder()
                .setMarketDataSpecification(marketDataSpec)
                .create();
            executionSequence.add(executionOptions);
          }
          previousWorkingDate = currentDate;
        }
        currentDate = currentDate.plusDays(1);
      }
      return new ArbitraryViewCycleExecutionSequence(executionSequence);
    }

    private MarketDataSpecification createMarketDataSpec(LocalDate previousHistoricalDate, LocalDate historicalDate, LocalDate valuationDate) {
      FixedHistoricalMarketDataSpecification historicalDateSpec = new FixedHistoricalMarketDataSpecification(historicalDate);
      switch (_marketDataMode) {
        case HISTORICAL:
          return historicalDateSpec;
        case RELATIVE_SHOCK:
          if (previousHistoricalDate == null) {
            return null;
          }
          FixedHistoricalMarketDataSpecification valuationDateSpec = new FixedHistoricalMarketDataSpecification(valuationDate);
          FixedHistoricalMarketDataSpecification previousHistoricalDateSpec = new FixedHistoricalMarketDataSpecification(previousHistoricalDate);
          return HistoricalShockMarketDataSpecification.of(ShockType.PROPORTIONAL, previousHistoricalDateSpec, historicalDateSpec, valuationDateSpec);
        default:
          throw new OpenGammaRuntimeException("Unsupported market data mode: " + _marketDataMode);
      }
    }

    // Object

    @Override
    public int hashCode() {
      int hc = 1;
      hc += (hc << 4) + ObjectUtils.hashCode(_startDateDescriptor);
      hc += (hc << 4) + (_includeStart ? 1 : 0);
      hc += (hc << 4) + ObjectUtils.hashCode(_endDateDescriptor);
      hc += (hc << 4) + (_includeEnd ? 1 : 0);
      hc += (hc << 4) + (_currencyCalendars != null ? _currencyCalendars.hashCode() : 0);
      hc += (hc << 4) + _marketDataMode.hashCode();
      return hc;
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof HistoricalSequence)) {
        return false;
      }
      final HistoricalSequence other = (HistoricalSequence) o;
      return ObjectUtils.equals(_startDateDescriptor, other._startDateDescriptor)
          && (_includeStart == other._includeStart)
          && ObjectUtils.equals(_endDateDescriptor, other._endDateDescriptor)
          && (_includeEnd == other._includeEnd)
          && ObjectUtils.equals(_currencyCalendars, other._currencyCalendars)
          && (_marketDataMode == other._marketDataMode);
    }

  }

  public String getStartDate() {
    return ((HistoricalSequence) getExecutionSequence())._startDateDescriptor;
  }

  public boolean isIncludeStart() {
    return ((HistoricalSequence) getExecutionSequence())._includeStart;
  }

  public String getEndDate() {
    return ((HistoricalSequence) getExecutionSequence())._endDateDescriptor;
  }

  public boolean isIncludeEnd() {
    return ((HistoricalSequence) getExecutionSequence())._includeEnd;
  }
  
  public Set<Currency> getCurrencyCalendars() {
    return ((HistoricalSequence) getExecutionSequence())._currencyCalendars;
  }
  
  public HistoricalViewEvaluationMarketDataMode getMarketDataMode() {
    return ((HistoricalSequence) getExecutionSequence())._marketDataMode;
  }

  @Override
  public HistoricalViewEvaluationTarget withUniqueId(final UniqueId uid) {
    return new HistoricalViewEvaluationTarget(this, uid);
  }

  @Override
  protected void toFudgeMsgImpl(FudgeSerializer serializer, MutableFudgeMsg message) {
    super.toFudgeMsgImpl(serializer, message);
  }

  @Override
  protected void serializeExecutionSequence(FudgeSerializer serializer, MutableFudgeMsg message) {
    // More efficient to recreate the execution sequence rather than serializing it
    message.add(START_DATE_FIELD, getStartDate());
    message.add(INCLUDE_START_FIELD, isIncludeStart());
    message.add(END_DATE_FIELD, getEndDate());
    message.add(INCLUDE_END_FIELD, isIncludeEnd());
    if (getCurrencyCalendars() != null) {
      for (Currency ccy : getCurrencyCalendars()) {
        serializer.addToMessage(message, CURRENCY_CALENDAR_FIELD, null, ccy);
      }
    }
    serializer.addToMessage(message, MARKET_DATA_MODE_FIELD, null, getMarketDataMode());
  }

  public static HistoricalViewEvaluationTarget fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg message) {
    return new HistoricalViewEvaluationTarget(deserializer, message);
  }

}
