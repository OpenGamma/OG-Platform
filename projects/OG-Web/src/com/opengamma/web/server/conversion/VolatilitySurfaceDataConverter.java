/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceData;
import com.opengamma.util.time.Tenor;

/**
 * Converter for {@link VolatilitySurfaceData} objects.
 */
@SuppressWarnings("rawtypes")
public class VolatilitySurfaceDataConverter implements ResultConverter<VolatilitySurfaceData> {

  @Override
  public Object convertForDisplay(ResultConverterCache context, ValueSpecification valueSpec, VolatilitySurfaceData rawValue, ConversionMode mode) {
    VolatilitySurfaceData<Object, Object> value = (VolatilitySurfaceData<Object, Object>)rawValue;
    Map<String, Object> result = new HashMap<String, Object>();
    
    result.put("xCount", value.getXs().length);
    result.put("yCount", value.getYs().length);
    
    if (mode == ConversionMode.FULL) {
      //TODO assuming that all curves are interpolated - bad
      Object[] xs = value.getXs();
      String[] xsStrings = new String[xs.length];
      for (int i = 0; i < xs.length; i++) {
        xsStrings[i] = xs[i].toString();
        //xsStrings[i] = xs[i].getPeriod().toString().replaceFirst("P", "");
      }
      result.put("xs", xsStrings);
      Object[] ys = value.getYs();
      String[] ysStrings = new String[ys.length];
      for (int i = 0; i < ys.length; i++) {
        ysStrings[i] = ys[i].toString();
        //ysStrings[i] = ys[i].getPeriod().toString().replaceFirst("P", "");
      }
      result.put("ys", ysStrings);
      
      double[][] surface = new double[ys.length][xs.length];
      // Summary view includes only the actual points of the curve
      for (int y = 0; y < ys.length; y++) {
        for (int x = 0; x < xs.length; x++) {
          Object xt = xs[x];
          Object yt = ys[y];
          surface[y][x] = value.getVolatility(xt, yt);
        }
      }
      result.put("surface", surface);
    }
    return result;
  }

  @Override
  public Object convertForHistory(ResultConverterCache context, ValueSpecification valueSpec, VolatilitySurfaceData value) {
    return null;
  }

  @Override
  public String convertToText(ResultConverterCache context, ValueSpecification valueSpec, VolatilitySurfaceData value) {
    // Could actually serialise the surface to a string if this is an issue
    return "Volatility Surface (" + value.getXs().length + " x " + value.getYs().length + ")";
  }
  
  @Override
  public String getFormatterName() {
    return "VOLATILITY_SURFACE_DATA";
  }
  
}
