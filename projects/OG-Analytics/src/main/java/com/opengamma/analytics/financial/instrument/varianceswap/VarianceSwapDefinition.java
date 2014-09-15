/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.varianceswap;

import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.getDaysBetween;
import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.getWorkingDaysInclusive;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.varianceswap.VarianceSwap;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * A variance swap is a forward contract on the realized variance of an underlying security.
 * The floating leg of a variance swap is the realized variance.
 */
public class VarianceSwapDefinition implements InstrumentDefinitionWithData<VarianceSwap, DoubleTimeSeries<LocalDate>> {
  /** The currency */
  private final Currency _currency;
  /** The volatility strike. _varStrike := _volStrike^2 until we need something more elaborate */
  private final double _volStrike;
  /** The volatility notional. _varNotional := 0.5 * _volNotional / _volStrike. Provides a rough estimate of the payoff if volatility realizes 1 point above strike */
  private final double _volNotional;
  /** The variance strike. Computed internally */
  private final double _varStrike;
  /** The variance notional. Computed internally */
  private final double _varNotional;
  /** The variance observation period start date */
  private final ZonedDateTime _obsStartDate;
  /** The variance observation period end date */
  private final ZonedDateTime _obsEndDate;
  /** The settlement date */
  private final ZonedDateTime _settlementDate;
  /** The number of observations expected given the observation dates and the holiday calendar */
  private final int _nObsExpected;
  /** The variance annualization factor */
  private final double _annualizationFactor;
  /** The holiday calendar */
  private final Calendar _calendar;

  /**
   * Constructor based upon vega (volatility) parameterisation - strike and notional.
   * 
   * @param obsStartDate Date of first observation, not null
   * @param obsEndDate Date of final observation, not null
   * @param settlementDate Date of cash settlement, not null
   * @param obsFreq The frequency of observations, not null
   * @param currency Currency of cash settlement, not null
   * @param calendar Specification of good business days (and holidays), not null
   * @param annualizationFactor Number of business days per year, not null
   * @param volStrike Fair value of volatility, the square root of variance, struck at trade date
   * @param volNotional Trade pays the difference between realized and strike variance multiplied by 0.5 * volNotional / volStrike
   */
  public VarianceSwapDefinition(final ZonedDateTime obsStartDate, final ZonedDateTime obsEndDate, final ZonedDateTime settlementDate, final Currency currency, final Calendar calendar,
      final double annualizationFactor, final double volStrike, final double volNotional) {
    ArgumentChecker.notNull(obsStartDate, "obsStartDate");
    ArgumentChecker.notNull(obsEndDate, "obsEndDate");
    ArgumentChecker.notNull(settlementDate, "settlementDate");
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNegativeOrZero(annualizationFactor, "annualizationFactor");
    ArgumentChecker.notNull(calendar, "calendar");

    _obsStartDate = obsStartDate;
    _obsEndDate = obsEndDate;
    _settlementDate = settlementDate;
    _currency = currency;
    _calendar = calendar;
    _nObsExpected = getWorkingDaysInclusive(obsStartDate, obsEndDate, calendar);
    _annualizationFactor = annualizationFactor;
    _volStrike = volStrike;
    _volNotional = volNotional;
    _varStrike = volStrike * volStrike;
    _varNotional = 0.5 * volNotional / volStrike;
  }

  /**
   * Static constructor of a variance swap using a vega parameterisation of the contract.
   * @param obsStartDate Date of the first observation, not null
   * @param obsEndDate Date of the last observation, not null
   * @param settlementDate The settlement date, not null
   * @param obsFreq The observation frequency, not null
   * @param currency The currency, not null
   * @param calendar The calendar used for calculating good business days, not null
   * @param annualizationFactor The annualisation factor
   * @param volStrike The volatility strike
   * @param volNotional The volatility notional
   * @return The contract definition
   */
  public static VarianceSwapDefinition fromVegaParams(final ZonedDateTime obsStartDate, final ZonedDateTime obsEndDate, final ZonedDateTime settlementDate, final Currency currency,
      final Calendar calendar, final double annualizationFactor, final double volStrike, final double volNotional) {
    return new VarianceSwapDefinition(obsStartDate, obsEndDate, settlementDate, currency, calendar, annualizationFactor, volStrike, volNotional);
  }

  /**
   * Static constructor of a variance swap using a variance parameterisation of the contract.
   * @param obsStartDate Date of the first observation, not null
   * @param obsEndDate Date of the last observation, not null
   * @param settlementDate The settlement date, not null
   * @param obsFreq The observation frequency, not null
   * @param currency The currency, not null
   * @param calendar The calendar used for calculating good business days, not null
   * @param annualizationFactor The annualisation factor
   * @param varStrike The variance strike, not negative
   * @param varNotional The variance notional
   * @return The contract definition
   */
  public static VarianceSwapDefinition fromVarianceParams(final ZonedDateTime obsStartDate, final ZonedDateTime obsEndDate, final ZonedDateTime settlementDate, final Currency currency,
      final Calendar calendar, final double annualizationFactor, final double varStrike, final double varNotional) {
    ArgumentChecker.notNegative(varStrike, "variance strike");
    final double volStrike = Math.sqrt(varStrike);
    final double volNotional = 2 * varNotional * volStrike;
    return new VarianceSwapDefinition(obsStartDate, obsEndDate, settlementDate, currency, calendar, annualizationFactor, volStrike, volNotional);
  }

  /**
   * {@inheritDoc} The definition is responsible for constructing a view of the variance swap as of a particular date.
   * An empty time series is used for the variance observations, and so this method should only be used
   * in the case where variance observation has not begun.
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public VarianceSwap toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    return toDerivative(date, ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES, yieldCurveNames);
  }

  /**
   * {@inheritDoc} The definition is responsible for constructing a view of the variance swap as of a particular date.
   * An empty time series is used for the variance observations, and so this method should only be used
   * in the case where variance observation has not begun.
   */
  @Override
  public VarianceSwap toDerivative(final ZonedDateTime date) {
    return toDerivative(date, ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES);
  }

  /**
   * {@inheritDoc} The definition is responsible for constructing a view of the variance swap as of a particular date.
   * In particular, it resolves calendars. The VarianceSwap needs an array of observations, as well as its *expected* length.
   * The actual number of observations may be less than that expected at trade inception because of a market disruption event.
   * ( For an example of a market disruption event, see http://cfe.cboe.com/Products/Spec_VT.aspx )
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public VarianceSwap toDerivative(final ZonedDateTime valueDate, final DoubleTimeSeries<LocalDate> underlyingTimeSeries, final String... yieldCurveNames) {
    return toDerivative(valueDate, underlyingTimeSeries);
  }

  /**
   * {@inheritDoc} The definition is responsible for constructing a view of the variance swap as of a particular date.
   * In particular, it resolves calendars. The variance swap needs an array of observations, as well as its *expected* length.
   * The actual number of observations may be less than that expected at trade inception because of a market disruption event.
   * ( For an example of a market disruption event, see http://cfe.cboe.com/Products/Spec_VT.aspx )
   */
  @Override
  public VarianceSwap toDerivative(final ZonedDateTime date, final DoubleTimeSeries<LocalDate> underlyingTimeSeries) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(underlyingTimeSeries, "underlyingTimeSeries");
    final double timeToObsStart = TimeCalculator.getTimeBetween(date, _obsStartDate);
    final double timeToObsEnd = TimeCalculator.getTimeBetween(date, _obsEndDate);
    final double timeToSettlement = TimeCalculator.getTimeBetween(date, _settlementDate);
    DoubleTimeSeries<LocalDate> realizedTS;
    if (timeToObsStart > 0) {
      realizedTS = ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES;
    } else {
      realizedTS = underlyingTimeSeries.subSeries(_obsStartDate.toLocalDate(), true, date.toLocalDate(), false);
    }
    final double[] observations = realizedTS.valuesArrayFast();
    final double[] observationWeights = {}; // TODO Case 2011-06-29 Calendar Add functionality for non-trivial weighting of observations
    //if we view this option on some date between the observation start and end dates, then the observation on that particular
    //date will not have been made (observations are closing levels) 
    final int nObservations = date.isAfter(_obsEndDate) ? _nObsExpected : (date.isBefore(_obsStartDate) ? 0 : getDaysBetween(_obsStartDate, date, _calendar));
    final int nObsDisrupted = nObservations - observations.length;
    ArgumentChecker.isTrue(nObsDisrupted >= 0, "Have more observations {} than good business days {}", observations.length, nObservations);
    return new VarianceSwap(timeToObsStart, timeToObsEnd, timeToSettlement, _varStrike, _varNotional, _currency, _annualizationFactor, _nObsExpected, nObsDisrupted, observations, observationWeights);
  }

  /**
   * Gets the first observation date.
   * @return the first observation date
   */
  public ZonedDateTime getObsStartDate() {
    return _obsStartDate;
  }

  /**
   * Gets the last observation date.
   * @return the last observation date
   */
  public ZonedDateTime getObsEndDate() {
    return _obsEndDate;
  }

  /**
   * Gets the settlement date.
   * @return the settlement date
   */
  public ZonedDateTime getSettlementDate() {
    return _settlementDate;
  }

  /**
   * Gets the number of observations expected. This is the number of good business days as expected at trade inception.
   * The actual number of observations may be less if a market disruption event occurs.
   * @return the nObsExpected
   */
  public int getObsExpected() {
    return _nObsExpected;
  }

  /**
   * Gets the currency.
   * @return the currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Gets the volatility strike.
   * @return the volatility strike
   */
  public double getVolStrike() {
    return _volStrike;
  }

  /**
   * Gets the volatility notional.
   * @return the volatility notional
   */
  public double getVolNotional() {
    return _volNotional;
  }

  /**
   * Gets the variance strike.
   * @return the variance strike
   */
  public double getVarStrike() {
    return _varStrike;
  }

  /**
   * Gets the variance notional.
   * @return the variance notional
   */
  public double getVarNotional() {
    return _varNotional;
  }

  /**
   * Gets the calendar.
   * @return the calendar
   */
  public Calendar getCalendar() {
    return _calendar;
  }

  /**
   * Gets the annualization factor.
   * @return The annualization factor
   */
  public double getAnnualizationFactor() {
    return _annualizationFactor;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currency.hashCode();
    result = prime * result + _obsEndDate.hashCode();
    result = prime * result + _obsStartDate.hashCode();
    result = prime * result + _settlementDate.hashCode();
    result = prime * result + _calendar.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_volNotional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_volStrike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_annualizationFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof VarianceSwapDefinition)) {
      return false;
    }
    final VarianceSwapDefinition other = (VarianceSwapDefinition) obj;
    if (Double.compare(_volStrike, other._volStrike) != 0) {
      return false;
    }
    if (Double.compare(_volNotional, other._volNotional) != 0) {
      return false;
    }
    if (Double.compare(_annualizationFactor, other._annualizationFactor) != 0) {
      return false;
    }
    if (!(ObjectUtils.equals(_obsStartDate, other._obsStartDate))) {
      return false;
    }
    if (!(ObjectUtils.equals(_obsEndDate, other._obsEndDate))) {
      return false;
    }
    if (!(ObjectUtils.equals(_settlementDate, other._settlementDate))) {
      return false;
    }
    if (!(ObjectUtils.equals(_currency, other._currency))) {
      return false;
    }
    if (!(ObjectUtils.equals(_calendar, other._calendar))) {
      return false;
    }
    return true;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitVarianceSwapDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitVarianceSwapDefinition(this);
  }

}
