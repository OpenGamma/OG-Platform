/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceData;
import com.opengamma.util.time.Tenor;

/**
 * Converter for {@link VolatilitySurfaceData} objects.
 */
@SuppressWarnings("rawtypes")
public class VolatilitySurfaceDataConverter implements ResultConverter<VolatilitySurfaceData> {

  @Override
  public Object convert(ResultConverterCache context, String valueName, VolatilitySurfaceData rawValue, ConversionMode mode) {
    @SuppressWarnings("unchecked")
    VolatilitySurfaceData<Tenor, Tenor> value = rawValue;
    Map<String, Object> result = new HashMap<String, Object>();
    
    //TODO assuming that all curves are interpolated - bad
    
    Tenor[] xs = value.getXs();
    String[] xsStrings = new String[xs.length];
    for (int i = 0; i < xs.length; i++) {
      xsStrings[i] = xs[i].getPeriod().toString().replaceFirst("P", "");
    }
    result.put("xs", xsStrings);
    Tenor[] ys = value.getYs();
    String[] ysStrings = new String[ys.length];
    for (int i = 0; i < ys.length; i++) {
      ysStrings[i] = ys[i].getPeriod().toString().replaceFirst("P", "");
    }
    result.put("ys", ysStrings);
    
    double[][] surface = new double[ys.length][xs.length];
    // Summary view includes only the actual points of the curve
    for (int y = 0; y < ys.length; y++) {
      for (int x = 0; x < xs.length; x++) {
        Tenor xt = xs[x];
        Tenor yt = ys[y];
        surface[y][x] = value.getVolatility(xt, yt);
      }
    }
    result.put("surface", surface);
    return result;
  }
  
  @Override
  public String getResultTypeName() {
    return "VOLATILITY_SURFACE_DATA";
  }
  
}
