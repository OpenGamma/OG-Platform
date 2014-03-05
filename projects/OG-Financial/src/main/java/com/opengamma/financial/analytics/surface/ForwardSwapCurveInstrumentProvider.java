/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.surface;

import org.joda.beans.Bean;
import org.joda.beans.PropertyDefinition;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.analytics.volatility.surface.SurfaceInstrumentProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.time.Tenor;

/**
 * Generates Bloomberg tickers for a forward swap surface, with the x axis the time to
 * start and the y axis the swap length.
 */
public class ForwardSwapCurveInstrumentProvider implements Bean, SurfaceInstrumentProvider<Tenor, Tenor>, UniqueIdentifiable {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * The unique id.
   */
  @PropertyDefinition
  private UniqueId _uniqueId;

  @Override
  public ExternalId getInstrument(final Tenor xAxis, final Tenor yAxis) {
    return null;
  }

  @Override
  public ExternalId getInstrument(final Tenor xAxis, final Tenor yAxis, final LocalDate surfaceDate) {
    return null;
  }

  @Override
  public String getDataFieldName() {
    return null;
  }

}
