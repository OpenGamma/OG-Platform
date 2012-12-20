/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import javax.time.calendar.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalId;
import com.opengamma.util.time.Tenor;

/**
 * This should be pulled from the configuration.
 */
public class StaticCurveInstrumentProvider implements CurveInstrumentProvider {
  private final ExternalId _identifier;

  public StaticCurveInstrumentProvider(final ExternalId identifier) {
    _identifier = identifier;
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor) {
    return _identifier;
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final int numQuarterlyFuturesFromTenor) {
    throw new OpenGammaRuntimeException("futures not supported by this class");
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
