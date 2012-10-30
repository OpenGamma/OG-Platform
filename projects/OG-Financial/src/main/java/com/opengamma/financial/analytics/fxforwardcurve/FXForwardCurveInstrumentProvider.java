/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fxforwardcurve;

import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.id.ExternalId;

/**
 * 
 */
public interface FXForwardCurveInstrumentProvider extends CurveInstrumentProvider {

  String getDataFieldName();

  ExternalId getSpotInstrument();
}
