/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.ArrayUtils;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilityNodeSensitivityDataBundle;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilityQuoteSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueSwaptionSurfaceSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.analytics.DoubleLabelledMatrix3D;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Contains utility methods that vega output from the analytics libraries into objects that
 * can be transported and displayed by the engine.
 */
public class VegaMatrixUtils {
  private static final DecimalFormat FX_OPTION_FORMATTER = new DecimalFormat("##");
  private static final DecimalFormat IR_FUTURE_OPTION_FORMATTER = new DecimalFormat("##.###");
  private static final DecimalFormat DELTA_FORMATTER = new DecimalFormat("##");

  /**
   * Returns a bucketed FX option vega matrix with delta / expiry axes.
   * @param vegas The vegas, not null
   * @return A labelled vega matrix.
   */
  public static DoubleLabelledMatrix2D getVegaFXMatrix(final PresentValueForexBlackVolatilityNodeSensitivityDataBundle vegas) {
    ArgumentChecker.notNull(vegas, "vegas");
    final double[] expiries = vegas.getExpiries().getData();
    final double[] delta = vegas.getDelta().getData();
    final double[][] vega = vegas.getVega().getData();
    final int nDelta = delta.length;
    final int nExpiries = expiries.length;
    final Double[] rowValues = new Double[nExpiries];
    final String[] rowLabels = new String[nExpiries];
    final Double[] columnValues = new Double[nDelta];
    final String[] columnLabels = new String[nDelta];
    final double[][] values = new double[nDelta][nExpiries];
    for (int i = 0; i < nDelta; i++) {
      columnValues[i] = delta[i];
      columnLabels[i] = "P" + DELTA_FORMATTER.format(delta[i] * 100) + " " + vegas.getCurrencyPair().getFirst() + "/" + vegas.getCurrencyPair().getSecond();
      for (int j = 0; j < nExpiries; j++) {
        if (i == 0) {
          rowValues[j] = expiries[j];
          rowLabels[j] = VegaMatrixUtils.getFXVolatilityFormattedExpiry(expiries[j]);
        }
        values[i][j] = vega[j][i];
      }
    }
    return new DoubleLabelledMatrix2D(rowValues, rowLabels, columnValues, columnLabels, values);
  }

  /**
   * Returns a bucketed FX option vega matrix with the same axes as the volatility quotes (i.e. ATM, risk-reversal and butterfly quotes)
   * @param vegas The vegas, not null
   * @return A labelled vega matrix
   */
  public static DoubleLabelledMatrix2D getVegaFXQuoteMatrix(final PresentValueForexBlackVolatilityQuoteSensitivityDataBundle vegas) {
    ArgumentChecker.notNull(vegas, "vegas");
    final double[] expiries = vegas.getExpiries();
    final double[] delta = vegas.getDelta();
    final double[][] vega = vegas.getVega();
    final int nDelta = delta.length;
    final int nExpiries = expiries.length;
    final Double[] rowValues = new Double[nExpiries];
    final String[] rowLabels = new String[nExpiries];
    final Double[] columnValues = new Double[nDelta];
    final String[] columnLabels = new String[nDelta];
    final double[][] values = new double[nDelta][nExpiries];
    columnLabels[0] = "ATM " + " " + vegas.getCurrencyPair().getFirst() + "/" + vegas.getCurrencyPair().getSecond();
    columnValues[0] = 0.;
    final int n = (nDelta - 1) / 2;
    for (int i = 0; i < n; i++) {
      columnLabels[1 + i] = "RR " + FX_OPTION_FORMATTER.format(delta[i] * 100) + " " + vegas.getCurrencyPair().getFirst() + "/" + vegas.getCurrencyPair().getSecond();
      columnValues[1 + i] = 1. + i;
      columnLabels[n + 1 + i] = "B " + FX_OPTION_FORMATTER.format(delta[i] * 100) + " " + vegas.getCurrencyPair().getFirst() + "/" + vegas.getCurrencyPair().getSecond();
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

  /**
   * Returns a bucketed interest rate future option vega matrix with strike / expiry axes.
   * @param definition The volatility surface, not null
   * @param matrix The vega matrix, not null
   * @param expiryValues The expiries, not null
   * @return A labelled vega matrix.
   */
  public static DoubleLabelledMatrix2D getVegaIRFutureOptionQuoteMatrix(final VolatilitySurfaceDefinition<?, ?> definition, final DoubleMatrix2D matrix,
      final double[] expiryValues) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(matrix, "matrix");
    ArgumentChecker.notNull(expiryValues, "expiry values");
    final int columns = matrix.getNumberOfRows();
    ArgumentChecker.isTrue(columns == expiryValues.length, "Did not have same number of columns as expiries");
    final int rows = matrix.getNumberOfColumns();
    final Double[] rowValues = new Double[rows];
    final Double[] columnValues = new Double[columns];
    final Object[] rowLabels = new Object[rows];
    final Object[] columnLabels = new Object[columns];
    final double[][] values = new double[rows][columns];
    final Object[] strikes = definition.getYs();
    final Object[] nFutureOption = definition.getXs();
    for (int i = 0; i < rows; i++) {
      final double strike = ((Number) strikes[i]).doubleValue();
      rowValues[i] = strike;
      rowLabels[i] = IR_FUTURE_OPTION_FORMATTER.format(strike);
      for (int j = 0; j < columns; j++) {
        if (i == 0) {
          final int n = ((Number) nFutureOption[j]).intValue();
          columnValues[j] = Double.valueOf(n);
          columnLabels[j] = n;
        }
        values[i][j] = matrix.getEntry(j, i);
      }
    }
    return new DoubleLabelledMatrix2D(columnValues, columnLabels, rowValues, rowLabels, values);
  }

  /**
   * Returns a bucketed swaption vega cube with swaption expiry / swap maturity / distance from ATM axes.
   * @param fittedPoints The points in the swaption volatility cube, not null
   * @param matrices a map from swaption expiry to vega matrix, not null
   * @return A labelled vega cube
   */
  public static DoubleLabelledMatrix3D getVegaSwaptionCubeQuoteMatrix(final Map<Pair<Tenor, Tenor>, Double[]> fittedPoints, final Map<Double, DoubleMatrix2D> matrices) {
    ArgumentChecker.notNull(fittedPoints, "fitted points");
    ArgumentChecker.notNull(matrices, "matrices");
    final List<Double> xKeysList = new ArrayList<>();
    final List<Double> xLabelsList = new ArrayList<>();
    final List<Double> yKeysList = new ArrayList<>();
    final List<Tenor> yLabelsList = new ArrayList<>();
    final List<Double> zKeysList = new ArrayList<>();
    final List<Tenor> zLabelsList = new ArrayList<>();
    for (final Entry<Pair<Tenor, Tenor>, Double[]> entry : fittedPoints.entrySet()) {
      final double swapMaturity = getTime(entry.getKey().getFirst());
      if (!zKeysList.contains(swapMaturity)) {
        zKeysList.add(swapMaturity);
        zLabelsList.add(entry.getKey().getFirst());
      }
      final double swaptionExpiry = getTime(entry.getKey().getSecond());
      if (!yKeysList.contains(swaptionExpiry)) {
        yKeysList.add(swaptionExpiry);
        yLabelsList.add(entry.getKey().getSecond());
      }
      if (xKeysList.size() == 0) {
        final Double[] relativeStrikesArray = entry.getValue();
        for (final Double relativeStrike : relativeStrikesArray) {
          xKeysList.add(relativeStrike);
          xLabelsList.add(relativeStrike);
        }
      }
    }
    final Double[] xKeys = xKeysList.toArray(ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY);
    final Double[] xLabels = xLabelsList.toArray(ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY);
    final Double[] yKeys = yKeysList.toArray(ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY);
    final Tenor[] yLabels = yLabelsList.toArray(new Tenor[yLabelsList.size()]);
    final Double[] zKeys = zKeysList.toArray(ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY);
    final Tenor[] zLabels = zLabelsList.toArray(new Tenor[zLabelsList.size()]);
    final double[][][] values = new double[zKeys.length][xKeys.length][yKeys.length];
    for (int i = 0; i < zKeys.length; i++) {
      values[i] = matrices.get(zKeys[i]).toArray();
    }
    return new DoubleLabelledMatrix3D(xKeys, xLabels, yKeys, yLabels, zKeys, zLabels, values);
  }

  /**
   * Returns a bucketed swaption atm vega matrix with swaption expiry / swap maturity axes.
   * @param vegas a map from swaption expiry, maturity to vega, not null
   * @return A labelled vega matrix
   */
  public static DoubleLabelledMatrix2D getVegaSwaptionMatrix(final PresentValueSwaptionSurfaceSensitivity vegas) {
    ArgumentChecker.notNull(vegas, "vegas");
    final HashMap<DoublesPair, Double> vegaMap = vegas.getSensitivity().getMap();
    final List<Double> xKeysList = new ArrayList<>();
    final List<Double> xLabelsList = new ArrayList<>();
    final List<Double> yKeysList = new ArrayList<>();
    final List<Double> yLabelsList = new ArrayList<>();
    for (final Entry<DoublesPair, Double> entry : vegaMap.entrySet()) {
      final double swapExpiry = entry.getKey().getFirst();
      if (!xKeysList.contains(swapExpiry)) {
        xKeysList.add(swapExpiry);
        xLabelsList.add(swapExpiry);
      }
      final double swaptionMaturity = entry.getKey().getSecond();
      if (!yKeysList.contains(swaptionMaturity)) {
        yKeysList.add(swaptionMaturity);
        yLabelsList.add(swaptionMaturity);
      }
    }
    final Double[] xKeys = xKeysList.toArray(ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY);
    final Double[] xLabels = xLabelsList.toArray(ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY);
    final int nExpiries = xLabels.length;
    final Double[] yKeys = yKeysList.toArray(ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY);
    final Double[] yLabels = yLabelsList.toArray(ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY);
    final int nMaturities = yLabels.length;
    final double[][] values = new double[nMaturities][nExpiries];
    
    for (int i = 0; i < nExpiries; i++) {
      for (int j = 0; j < nMaturities; j++) {
        DoublesPair key = DoublesPair.of(xKeys[i].doubleValue(), yKeys[j].doubleValue());
        Double value = vegaMap.get(key);
        values[j][i] = value == null ? 0.0 : value;
      }
    }
    
    return new DoubleLabelledMatrix2D(xKeys, xLabels, yKeys, yLabels, values);
  }
  
  public static String getFXVolatilityFormattedExpiry(final double expiry) {
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

  private static double getTime(final Tenor tenor) { //TODO this should be moved into a utils class
    final Period period = tenor.getPeriod();
    final double months = period.toTotalMonths();
    return months / 12.;
  }
  

  
}
