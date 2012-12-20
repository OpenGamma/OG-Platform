/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceMoneyness;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
public class LocalVolatilitySurfaceMoneynessConverter implements ResultConverter<LocalVolatilitySurfaceMoneyness> {
  private static final DecimalFormat LABEL_FORMAT = new DecimalFormat("##.##");
  
  @Override
  public Object convertForDisplay(ResultConverterCache context, ValueSpecification valueSpec, LocalVolatilitySurfaceMoneyness value, ConversionMode mode) {
    Map<String, Object> result = new HashMap<String, Object>();
    if (value.getSurface() instanceof InterpolatedDoublesSurface) {
      InterpolatedDoublesSurface interpolated = (InterpolatedDoublesSurface) value.getSurface();
      result.put("xCount", interpolated.getXData().length);
      result.put("yCount", interpolated.getYData().length);
      if (mode == ConversionMode.FULL) {
        Double[] xs = interpolated.getXData();
        Double[] ys = interpolated.getYData();
        List<Double> uniqueX = new ArrayList<Double>();
        List<Double> uniqueY = new ArrayList<Double>();
        for (Double x : xs) {
          if (!uniqueX.contains(x)) {
            uniqueX.add(x);
          }
        }
        for (Double y : ys) {
          if (!uniqueY.contains(y)) {
            uniqueY.add(y);
          }
        }
        Collections.sort(uniqueX);
        Collections.sort(uniqueY);      
        Object[] xLabels = new Object[uniqueX.size()];
        Object[] yLabels = new Object[uniqueY.size()];
        double[][] surface = new double[xs.length][ys.length];
        boolean[][] missingValues = new boolean[xs.length][ys.length];
        for (int i = 0; i < uniqueX.size(); i++) {
          xLabels[i] = uniqueX.get(i).toString();
          for (int j = 0; j < uniqueY.size(); j++) {
            if (i == 0) {
              yLabels[j] = uniqueY.get(j).toString();
            }
            try {
              surface[i][j] = interpolated.getZValue(uniqueX.get(i), uniqueY.get(i));
            } catch (MathException e) {
              surface[i][j] = Double.MAX_VALUE;
              missingValues[i][j] = true;
            }
          }
        }
        result.put("xs", uniqueX.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        result.put("ys", uniqueY.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        result.put("surface", surface);
        result.put("missingValues", missingValues);
      }
    } else if (value.getSurface() instanceof FunctionalDoublesSurface) {
      FunctionalDoublesSurface functional = (FunctionalDoublesSurface) value.getSurface();

      final double[] expiries = {0.1, 0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 2.0, 2.5, 3., 4., 5.};
      final int nX = expiries.length;
      
      result.put("xCount", nX);
      result.put("yCount", 21);
      if (mode == ConversionMode.FULL) {
        String[] xLabels = new String[nX];
        String[] yLabels = new String[21];
        double[][] surface = new double[21][nX];
        boolean[][] missingValues = new boolean[21][nX];
        for (int i = 0; i < nX; i++) {
          double x = expiries[i];
          xLabels[i] = LABEL_FORMAT.format(x);
          double y = .45; // Moneyness from 0.5 to 2.0
          for (int j = 0; j < 21; j++) {
            y += 0.05;
            if (i == 0) {
              yLabels[j] = LABEL_FORMAT.format(y);
            }
            surface[j][i] = 100 * functional.getZValue(x, y);
          }
        }
        result.put("xs", xLabels);
        result.put("ys", yLabels);
        result.put("surface", surface);
        result.put("missingValues", missingValues);
      }
    }
    return result;
  }

  @Override
  public Object convertForHistory(ResultConverterCache context, ValueSpecification valueSpec, LocalVolatilitySurfaceMoneyness value) {
    return null;
  }

  @Override
  public String convertToText(ResultConverterCache context, ValueSpecification valueSpec, LocalVolatilitySurfaceMoneyness value) {
    return "Local Volatility Surface Moneyness";
  }

  @Override
  public String getFormatterName() {
    return "SURFACE_DATA";
  }

}
