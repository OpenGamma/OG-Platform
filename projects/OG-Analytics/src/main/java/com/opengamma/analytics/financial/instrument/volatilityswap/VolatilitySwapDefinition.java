/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.volatilityswap;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionUtils;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.volatilityswap.VolatilitySwap;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * A volatility swap is a forward contract on the realized volatility of an underlying security.
 */
public class VolatilitySwapDefinition implements InstrumentDefinitionWithData<VolatilitySwap, DoubleTimeSeries<LocalDate>> {
  /** The currency */
  private final Currency _currency;
  /** The volatility strike */
  private final double _volStrike;
  /** The volatility notional */
  private final double _volNotional;
  /** The volatility observation period start date */
  private final ZonedDateTime _observationStartDate;
  /** The volatility observation period end date */
  private final ZonedDateTime _observationEndDate;
  /** The effective date */
  private final ZonedDateTime _effectiveDate;
  /** The settlement date */
  private final ZonedDateTime _settlementDate;
  /** The observation frequency */
  private final PeriodFrequency _observationFrequency;
  /** The number of observations expected given the observatin dates andthe holiday calendar */
  private final int _nObservations;
  /** The annualization factor */
  private final double _annualizationFactor;
  /** The holiday calendar */
  private final Calendar _calendar;

  /**
   * @param currency The currency, not null
   * @param volStrike The volatility strike, not negative
   * @param volNotional The volatility notional
   * @param observationStartDate The observation start date, not null
   * @param observationEndDate The observation end date, not null
   * @param effectiveDate The effective date, not null
   * @param settlementDate The settlement date, not null
   * @param observationFrequency The observation frequency, not null
   * @param annualizationFactor The annualization factor, greater than zero
   * @param calendar The holiday calendar, not null
   */
  public VolatilitySwapDefinition(final Currency currency, final double volStrike, final double volNotional, final ZonedDateTime observationStartDate,
      final ZonedDateTime observationEndDate, final ZonedDateTime effectiveDate, final ZonedDateTime settlementDate,
      final PeriodFrequency observationFrequency, final double annualizationFactor, final Calendar calendar) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNegative(volStrike, "volStrike");
    ArgumentChecker.notNull(observationStartDate, "observationStartDate");
    ArgumentChecker.notNull(observationEndDate, "observationEndDate");
    ArgumentChecker.notNull(effectiveDate, "settlementDate");
    ArgumentChecker.notNull(settlementDate, "settlementDate");
    ArgumentChecker.notNull(observationFrequency, "observationFrequency");
    ArgumentChecker.notNegativeOrZero(annualizationFactor, "annualizationFactor");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.isTrue(observationFrequency.equals(PeriodFrequency.DAILY), "Only DAILY observation frequencies are currently supported. obsFreq {} ",
        observationFrequency.toString());
    _currency = currency;
    _volStrike = volStrike;
    _volNotional = volNotional;
    _effectiveDate = effectiveDate;
    _settlementDate = settlementDate;
    _observationStartDate = observationStartDate;
    _observationEndDate = observationEndDate;
    _observationFrequency = observationFrequency;
    _annualizationFactor = annualizationFactor;
    _calendar = calendar;
    _nObservations = InstrumentDefinitionUtils.countExpectedGoodDays(observationStartDate.toLocalDate(), observationEndDate.toLocalDate(), calendar, observationFrequency);
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
   * @return The volatility strike
   */
  public double getVolatilityStrike() {
    return _volStrike;
  }

  /**
   * Gets the volatility notional.
   * @return The volatility notional
   */
  public double getVolatilityNotional() {
    return _volNotional;
  }

  /**
   * Gets the observation start date.
   * @return The observation start date
   */
  public ZonedDateTime getObservationStartDate() {
    return _observationStartDate;
  }

  /**
   * Gets the observation end date.
   * @return the observation end date
   */
  public ZonedDateTime getObservationEndDate() {
    return _observationEndDate;
  }

  /**
   * Gets the effective date.
   * @return the effective date
   */
  public ZonedDateTime getEffectiveDate() {
    return _effectiveDate;
  }

  /**
   * Gets the settlement date.
   * @return the settlement date
   */
  public ZonedDateTime getSettlementDate() {
    return _settlementDate;
  }

  /**
   * Gets the observation frequency.
   * @return the observation frequency
   */
  public PeriodFrequency getObservationFrequency() {
    return _observationFrequency;
  }

  /**
   * Gets the number of expected observations of the underlying.
   * @return The number of expected observations of the underlying
   */
  public int getNumberOfObservationsExpected() {
    return _nObservations;
  }

  /**
   * Gets the annualization factor.
   * @return the annualization factor
   */
  public double getAnnualizationFactor() {
    return _annualizationFactor;
  }

  /**
   * Gets the calendar.
   * @return the calendar
   */
  public Calendar getCalendar() {
    return _calendar;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitVolatilitySwapDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitVolatilitySwapDefinition(this);
  }

  @Override
  public VolatilitySwap toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    return toDerivative(date, ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES);
  }

  @Override
  public VolatilitySwap toDerivative(final ZonedDateTime date) {
    return toDerivative(date, ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES);
  }

  @Override
  public VolatilitySwap toDerivative(final ZonedDateTime date, final DoubleTimeSeries<LocalDate> data, final String... yieldCurveNames) {
    return toDerivative(date, data);
  }

  @Override
  public VolatilitySwap toDerivative(final ZonedDateTime date, final DoubleTimeSeries<LocalDate> data) {
    return null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_annualizationFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _calendar.hashCode();
    result = prime * result + _currency.hashCode();
    result = prime * result + _nObservations;
    result = prime * result + _observationEndDate.hashCode();
    result = prime * result + _observationFrequency.hashCode();
    result = prime * result + _observationStartDate.hashCode();
    result = prime * result + _effectiveDate.hashCode();
    result = prime * result + _settlementDate.hashCode();
    temp = Double.doubleToLongBits(_volNotional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_volStrike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof VolatilitySwapDefinition)) {
      return false;
    }
    final VolatilitySwapDefinition other = (VolatilitySwapDefinition) obj;
    if (Double.compare(_volStrike, other._volStrike) != 0) {
      return false;
    }
    if (Double.compare(_volNotional, other._volNotional) != 0) {
      return false;
    }
    if (Double.doubleToLongBits(_annualizationFactor) != Double.doubleToLongBits(other._annualizationFactor)) {
      return false;
    }
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (_nObservations != other._nObservations) {
      return false;
    }
    if (!ObjectUtils.equals(_effectiveDate, other._effectiveDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_settlementDate, other._settlementDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_observationStartDate, other._observationStartDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_observationEndDate, other._observationEndDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_observationFrequency, other._observationFrequency)) {
      return false;
    }
    if (!ObjectUtils.equals(_calendar, other._calendar)) {
      return false;
    }
    return true;
  }

}
