/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.surface.DoublesSurface;
import com.opengamma.math.surface.Surface;

/**
 * Converter for {@link VolatilitySurface} objects.
 * TODO Describe how this differs from VolatilitySurfaceData
 */
public class VolatilitySurfaceConverter implements ResultConverter<VolatilitySurface> {

  @Override
  public Object convertForDisplay(ResultConverterCache context, ValueSpecification valueSpec, VolatilitySurface value, ConversionMode mode) {
    Map<String, Object> result = new HashMap<String, Object>();

    Surface<Double, Double, Double> inputSurface = value.getSurface();
    if (inputSurface instanceof DoublesSurface) {
      Double[] xData = inputSurface.getXData();
      Double[] yData = inputSurface.getYData();
      final int xCount = xData.length;
      final int yCount = yData.length;
      result.put("xCount", xCount);
      result.put("yCount", yCount);
      
      if (mode == ConversionMode.FULL) {
        String[] xStrings = new String[xCount];
        for (int i = 0; i < xCount; i++) {
          xStrings[i] = xData[i].toString();
        }
        result.put("xData", xStrings);
        
        String[] ysStrings = new String[yCount];
        for (int i = 0; i < yCount; i++) {
          ysStrings[i] = yData[i].toString();
        }
        result.put("yData", ysStrings);
        
        
        double[][] outputSurface = new double[yCount][xCount];
        boolean[][] missingValues = new boolean[yCount][xCount];
        // Summary view includes only the actual points of the surface
        for (int y = 0; y < yCount; y++) {
          for (int x = 0; x < xCount; x++) {
            Double volatility = inputSurface.getZValue(xData[x], yData[y]);
            if (volatility == null) {
              missingValues[y][x] = true;
              //Some 'obviously wrong' value in case client displays it.  Can't use NaN
              outputSurface[y][x] = Double.MAX_VALUE;
            } else {
              outputSurface[y][x] = volatility;
            }
          }
        }
        result.put("surface", outputSurface);
        result.put("missingValues", missingValues);
      }
      
    } else {
      result.put("summary", "Converter only displays VolatilitySurface(DoublesSurface) as we require axes data");
    }
    return result;
  }

  @Override
  public Object convertForHistory(ResultConverterCache context,
      ValueSpecification valueSpec, VolatilitySurface value) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String convertToText(ResultConverterCache context, ValueSpecification valueSpec, VolatilitySurface value) {
    // Could actually serialise the surface to a string if this is an issue
    return "Volatility Surface (" + value.getSurface().getXData().length + " x " + value.getSurface().getYData().length + ")";
  }

  @Override
  public String getFormatterName() {
    return "SURFACE_DATA";
  }


}
