/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * Provides the external id of instruments for which the ticker does not change with time.
 * 
 * This should be pulled from the configuration.
 */
public class StaticCurveInstrumentProvider implements CurveInstrumentProvider {
  /** The market data identifier */
  private final ExternalId _identifier;

  /**
   * @param identifier The market data identifier, not null
   */
  public StaticCurveInstrumentProvider(final ExternalId identifier) {
    ArgumentChecker.notNull(identifier, "identifier");
    _identifier = identifier;
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor) {
    return _identifier;
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final int numQuarterlyFuturesFromTenor) {
    throw new OpenGammaRuntimeException("Futures not supported by this class");
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor startTenor, final Tenor futureTenor, final int numFutureFromTenor) {
    throw new OpenGammaRuntimeException("Futures not supported by this class");
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final int periodsPerYear, final boolean isPeriodicZeroDeposit) {
    if (isPeriodicZeroDeposit) {
      return _identifier;
    }
    throw new OpenGammaRuntimeException("Flag indicating periodic zero deposit was false");
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final Tenor payTenor, final Tenor receiveTenor, final IndexType payIndexType,
      final IndexType receiveIndexType) {
    return _identifier;
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final Tenor resetTenor, final IndexType indexType) {
    return _identifier;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof StaticCurveInstrumentProvider)) {
      return false;
    }
    final StaticCurveInstrumentProvider other = (StaticCurveInstrumentProvider) o;
    return _identifier.equals(other._identifier);
  }

  @Override
  public int hashCode() {
    return _identifier.hashCode();
  }

  @Override
  public String toString() {
    return "StaticCurveInstrumentProvider[" + _identifier.toString() + "]";
  }

}
