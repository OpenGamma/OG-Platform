/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.forwardcurve;

import javax.time.calendar.LocalDate;

import com.opengamma.id.ExternalId;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public abstract class ForwardSwapCurveInstrumentProvider implements ForwardCurveInstrumentProvider {

  public abstract ExternalId getInstrument(LocalDate curveDate, Tenor tenor, Tenor forwardTenor);

  public abstract ExternalId getSpotInstrument(Tenor forwardTenor);

  @Override
  public ExternalId getSpotInstrument() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final int numQuarterlyFuturesFromTenor) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final int periodsPerYear, final boolean isPeriodicZeroDeposit) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor) {
    throw new UnsupportedOperationException();
  }

}
