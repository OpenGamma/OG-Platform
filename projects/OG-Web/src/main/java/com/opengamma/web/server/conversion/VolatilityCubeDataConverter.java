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
  public Object convertForDisplay(ResultConverterCache context, ValueSpecification valueSpec, VolatilityCubeData value, ConversionMode mode) {
    return convertToText(context, valueSpec, value);
  }

  @Override
  public Object convertForHistory(ResultConverterCache context, ValueSpecification valueSpec, VolatilityCubeData value) {
    return null;
  }

  @Override
  public String convertToText(ResultConverterCache context, ValueSpecification valueSpec, VolatilityCubeData value) {
    return "Volatility Cube data (" + value.getDataPoints().size() + " volatility points, " + value.getATMStrikes().size()
        + " strikes, " + value.getOtherData().getDataPoints().size() + " other data points " + ")";
    
  }
  
  @Override
  public String getFormatterName() {
    return "PRIMITIVE";
  }
  
}
