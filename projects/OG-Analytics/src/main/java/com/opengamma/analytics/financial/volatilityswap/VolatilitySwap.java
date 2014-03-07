/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.volatilityswap;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * A volatility swap is a forward contract on the realised volatility of a generic underlying. This could be a single equity price, the value of an equity index,
 * an FX rate or <b>any</b> other financial metric on which a volatility swap contract is based.<p>
 */
public class VolatilitySwap implements InstrumentDerivative {
  /** Time to the start of volatility observations */
  private final double _timeToObservationStart;
  /** Time to the end of volatility observations */
  private final double _timeToObservationEnd;
  /** The observation frequency */
  private final PeriodFrequency _observationFrequency;
  /** Time to maturity */
  private final double _timeToMaturity;
  /** The volatility strike */
  private final double _volStrike;
  /** The volatility notional */
  private final double _volNotional;
  /** The currency */
  private final Currency _currency;
  /** The annualization factor */
  private final double _annualizationFactor;

  /**
   * @param timeToObservationStart Time to first observation. Negative if observations have begun.
   * @param timeToObservationEnd Time to final observation. Negative if observations have finished.
   * @param observationFrequency The observation frequency, not null
   * @param timeToMaturity Time of cash maturity. If negative, the swap has expired.
   * @param volStrike Fair value of Variance struck at trade date
   * @param volNotional Trade pays the difference between realized and strike variance multiplied by this
   * @param currency Currency of cash maturity
   * @param annualizationFactor Number of business days per year
   */
  public VolatilitySwap(final double timeToObservationStart, final double timeToObservationEnd, final PeriodFrequency observationFrequency,
      final double timeToMaturity, final double volStrike, final double volNotional, final Currency currency, final double annualizationFactor) {
    ArgumentChecker.notNull(observationFrequency, "observationFrequency");
    ArgumentChecker.notNull(currency, "currency");
    _timeToObservationStart = timeToObservationStart;
    _timeToObservationEnd = timeToObservationEnd;
    _observationFrequency = observationFrequency;
    _timeToMaturity = timeToMaturity;
    _volStrike = volStrike;
    _volNotional = volNotional;
    _currency = currency;
    _annualizationFactor = annualizationFactor;
  }

  /**
   * Copy constructor
   * @param other VarianceSwap to copy from
   */
  public VolatilitySwap(final VolatilitySwap other) {
    ArgumentChecker.notNull(other, "other");
    _timeToObservationStart = other._timeToObservationStart;
    _timeToObservationEnd = other._timeToObservationEnd;
    _observationFrequency = other._observationFrequency;
    _timeToMaturity = other._timeToMaturity;
    _volStrike = other._volStrike;
    _volNotional = other._volNotional;
    _currency = other._currency;
    _annualizationFactor = other._annualizationFactor;
  }

  /**
   * Gets the time to the start of volatility observation.
   * @return The time to the start of volatility observation
   */
  public double getTimeToObservationStart() {
    return _timeToObservationStart;
  }

  /**
   * Gets the time to the end of volatility observation.
   * @return The time to the end of volatility observation
   */
  public double getTimeToObservationEnd() {
    return _timeToObservationEnd;
  }

  /**
   * Gets the observation frequency.
   * @return The observation frequency
   */
  public PeriodFrequency getObservationFrequency() {
    return _observationFrequency;
  }

  /**
   * Gets the time to maturity.
   * @return The time to maturity
   */
  public double getTimeToMaturity() {
    return _timeToMaturity;
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
   * Gets the annualization factor.
   * @return The annualization factor
   */
  public double getAnnualizationFactor() {
    return _annualizationFactor;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitVolatilitySwap(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitVolatilitySwap(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_annualizationFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _currency.hashCode();
    temp = Double.doubleToLongBits(_timeToObservationEnd);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_timeToObservationStart);
    result = prime * result + _observationFrequency.hashCode();
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_timeToMaturity);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    if (!(obj instanceof VolatilitySwap)) {
      return false;
    }
    final VolatilitySwap other = (VolatilitySwap) obj;
    if (Double.doubleToLongBits(_annualizationFactor) != Double.doubleToLongBits(other._annualizationFactor)) {
      return false;
    }
    if (_currency == null) {
      if (other._currency != null) {
        return false;
      }
    } else if (!_currency.equals(other._currency)) {
      return false;
    }
    if (Double.doubleToLongBits(_timeToObservationEnd) != Double.doubleToLongBits(other._timeToObservationEnd)) {
      return false;
    }
    if (Double.doubleToLongBits(_timeToObservationStart) != Double.doubleToLongBits(other._timeToObservationStart)) {
      return false;
    }
    if (Double.doubleToLongBits(_timeToMaturity) != Double.doubleToLongBits(other._timeToMaturity)) {
      return false;
    }
    if (Double.doubleToLongBits(_volNotional) != Double.doubleToLongBits(other._volNotional)) {
      return false;
    }
    if (Double.doubleToLongBits(_volStrike) != Double.doubleToLongBits(other._volStrike)) {
      return false;
    }
    if (!ObjectUtils.equals(_observationFrequency, other._observationFrequency)) {
      return false;
    }
    return true;
  }

}
