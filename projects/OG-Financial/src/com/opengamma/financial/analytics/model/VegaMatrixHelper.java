/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.analytics.DoubleLabelledMatrix3D;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;
import com.opengamma.financial.forex.method.PresentValueVolatilityQuoteSensitivityDataBundle;
import com.opengamma.id.ExternalId;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class VegaMatrixHelper {
  private static final DecimalFormat FX_OPTION_FORMATTER = new DecimalFormat("##");
  private static final DecimalFormat IR_FUTURE_OPTION_FORMATTER = new DecimalFormat("##.###");

  public static DoubleLabelledMatrix2D getVegaFXQuoteMatrixInStandardForm(final PresentValueVolatilityQuoteSensitivityDataBundle data) {
    final double[] expiries = data.getExpiries();
    final double[] delta = data.getDelta();
    final double[][] vega = data.getVega();
    final int nDelta = delta.length;
    final int nExpiries = expiries.length;
    final Double[] rowValues = new Double[nExpiries];
    final String[] rowLabels = new String[nExpiries];
    final Double[] columnValues = new Double[nDelta];
    final String[] columnLabels = new String[nDelta];
    final double[][] values = new double[nDelta][nExpiries];
    columnLabels[0] = "ATM " + " " + data.getCurrencyPair().getFirst() + "/" + data.getCurrencyPair().getSecond();
    columnValues[0] = 0.;
    int n = (nDelta - 1) / 2;
    for (int i = 0; i < n; i++) {
      columnLabels[1 + i] = "RR " + FX_OPTION_FORMATTER.format(delta[i] * 100) + " " + data.getCurrencyPair().getFirst() + "/" + data.getCurrencyPair().getSecond();
      columnValues[1 + i] = 1. + i;
      columnLabels[n + 1 + i] = "B " + FX_OPTION_FORMATTER.format(delta[i] * 100) + " " + data.getCurrencyPair().getFirst() + "/" + data.getCurrencyPair().getSecond();
      columnValues[n + 1 + i] = n + 1. + i;
    }
    for (int j = 0; j < nExpiries; j++) {
      rowValues[j] = expiries[j];
      rowLabels[j] = getFXVolatilityFormattedExpiry(expiries[j]);
    }
    for (int i = 0; i < nDelta; i++) {
      for (int j = 0; j < nExpiries; j++) {
        values[i][j] = vega[j][i];
      }
    }
    return new DoubleLabelledMatrix2D(rowValues, rowLabels, columnValues, columnLabels, values);
  }

  public static DoubleLabelledMatrix2D getVegaIRFutureOptionQuoteMatrixInStandardForm(final VolatilitySurfaceDefinition<?, ?> definition, final DoubleMatrix2D matrix, final double[] expiryValues) {
    final int columns = matrix.getNumberOfRows();
    if (columns != expiryValues.length) {
      throw new OpenGammaRuntimeException("Should never happen");
    }
    final int rows = matrix.getNumberOfColumns();
    final Double[] rowValues = new Double[rows];
    final Double[] columnValues = new Double[columns];
    final Object[] rowLabels = new Object[rows];
    final Object[] columnLabels = new Object[columns];
    final double[][] values = new double[rows][columns];
    final Object[] strikes = definition.getYs();
    final Object[] nFutureOption = definition.getXs();
    for (int i = 0; i < rows; i++) {
      double strike =  ((Number) strikes[i]).doubleValue();
      rowValues[i] = strike;
      rowLabels[i] = IR_FUTURE_OPTION_FORMATTER.format(strike);
      for (int j = 0; j < columns; j++) {
        if (i == 0) {
          int n = ((Number) nFutureOption[j]).intValue();
          columnValues[j] = Double.valueOf(n);
          columnLabels[j] = n;
        }
        values[i][j] = matrix.getEntry(j, i);
      }
    }
    return new DoubleLabelledMatrix2D(columnValues, columnLabels, rowValues, rowLabels, values);
  }
  
  public static DoubleLabelledMatrix3D getVegaSwaptionCubeQuoteMatrixInStandardForm(final SortedMap<DoublesPair, ExternalId[]> smileIds, final Map<Double, DoubleMatrix2D> matrices) {
    final List<Double> xKeysList = new ArrayList<Double>();
    final List<Object> xLabelsList = new ArrayList<Object>();
    final List<Double> yKeysList = new ArrayList<Double>();
    final List<Object> yLabelsList = new ArrayList<Object>();
    final List<Double> zKeysList = new ArrayList<Double>();
    final List<Object> zLabelsList = new ArrayList<Object>();    
    for (final Entry<DoublesPair, ExternalId[]> entry : smileIds.entrySet()) {
      if (!zKeysList.contains(entry.getKey().first)) {
        zKeysList.add(entry.getKey().first);
        zLabelsList.add(entry.getKey().first);
      }
      if (!yKeysList.contains(entry.getKey().second)) {
        yKeysList.add(entry.getKey().second);
        yLabelsList.add(entry.getKey().second);
      }
      if (xKeysList.size() == 0) {
        Object idsObject = entry.getValue();
        final String[] ids;
        if (idsObject instanceof ArrayList) {
          ArrayList<String> temp1 = (ArrayList<String>) idsObject;
          ids = temp1.toArray(new String[temp1.size()]);
        } else {
          ids = (String[]) idsObject;
        }
        for (int i = 0; i < ids.length; i++) {
          xKeysList.add(Double.valueOf(i));
          xLabelsList.add(i);
        }
      }      
    }
    final Double[] xKeys = xKeysList.toArray(ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY);
    final Object[] xLabels = xLabelsList.toArray();
    final Double[] yKeys = yKeysList.toArray(ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY);
    final Object[] yLabels = yLabelsList.toArray();
    final Double[] zKeys = zKeysList.toArray(ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY);
    final Object[] zLabels = zLabelsList.toArray();
    final double[][][] values = new double[zKeys.length][xKeys.length][yKeys.length];
    for (int i = 0; i < zKeys.length; i++) {
      values[i] = matrices.get(zKeys[i]).toArray();
    }
    return new DoubleLabelledMatrix3D(xKeys, xLabels, yKeys, yLabels, zKeys, zLabels, values);
  }
  
  private static String getFXVolatilityFormattedExpiry(final double expiry) {
    if (expiry < 1. / 54) {
      final int days = (int) Math.ceil((365 * expiry));
      return days + "D";
    }
    if (expiry < 1. / 13) {
      final int weeks = (int) Math.ceil((52 * expiry));
      return weeks + "W";
    }
    if (expiry < 0.95) {
      final int months = (int) Math.ceil((12 * expiry));
      return months + "M";
    }
    return ((int) Math.ceil(expiry)) + "Y";
  }
}
