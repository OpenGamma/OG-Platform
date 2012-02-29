/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import com.opengamma.id.ExternalId;

/**
 * @param <X> The type of the x-axis values
 * @param <Y> The type of the y-axis values
 * @param <Z> The type of the z-axis values
 */
public interface CubeInstrumentProvider<X, Y, Z> {

  ExternalId getInstrument(X xAxis, Y yAxis, Z zaxis);

  String getDataFieldName();

}
