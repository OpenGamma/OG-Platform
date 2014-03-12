/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Converter for {@link VolatilityCubeData} objects.
 */
public class VolatilityCubeDataConverter implements ResultConverter<VolatilityCubeData> {

  @Override
  public Object convertForDisplay(final ResultConverterCache context, final ValueSpecification valueSpec, final VolatilityCubeData value, final ConversionMode mode) {
    return convertToText(context, valueSpec, value);
  }

  @Override
  public Object convertForHistory(final ResultConverterCache context, final ValueSpecification valueSpec, final VolatilityCubeData value) {
    return null;
  }

  @Override
  public String convertToText(final ResultConverterCache context, final ValueSpecification valueSpec, final VolatilityCubeData value) {
    return "Volatility Cube data (" + value.size() + " volatility points " + ")";
  }

  @Override
  public String getFormatterName() {
    return "PRIMITIVE";
  }

}
