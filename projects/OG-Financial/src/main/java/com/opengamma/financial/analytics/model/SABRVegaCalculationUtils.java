/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.math.interpolation.Interpolator2D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.MatrixAlgebraFactory;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public class SABRVegaCalculationUtils {
  private static final MatrixAlgebra ALGEBRA = MatrixAlgebraFactory.OG_ALGEBRA;
  private static final DoublesPairComparator COMPARATOR = new DoublesPairComparator();

  public static DoubleMatrix2D getVegaSurface(final double alpha, final double rho, final double nu, final Map<Double, Interpolator1DDataBundle> alphaDataBundle,
      final Map<Double, Interpolator1DDataBundle> rhoDataBundle, final Map<Double, Interpolator1DDataBundle> nuDataBundle,
      final Map<DoublesPair, DoubleMatrix2D> inverseJacobians, final DoublesPair expiryMaturity, final Interpolator2D nodeSensitivityCalculator,
      final Map<Double, List<Double>> fittedDataPoints, final VolatilitySurfaceDefinition<Object, Object> definition) {
    final Map<Double, List<Pair<Double, Double>>> alphaGridNodeSensitivities =
      SABRVegaCalculationUtils.getMaturityExpiryValueMap(nodeSensitivityCalculator.getNodeSensitivitiesForValue(alphaDataBundle, expiryMaturity));
    final Map<Double, List<Pair<Double, Double>>> nuGridNodeSensitivities =
      SABRVegaCalculationUtils.getMaturityExpiryValueMap(nodeSensitivityCalculator.getNodeSensitivitiesForValue(nuDataBundle, expiryMaturity));
    final Map<Double, List<Pair<Double, Double>>> rhoGridNodeSensitivities =
      SABRVegaCalculationUtils.getMaturityExpiryValueMap(nodeSensitivityCalculator.getNodeSensitivitiesForValue(rhoDataBundle, expiryMaturity));
    final Map<Double, List<Pair<Double, DoubleMatrix2D>>> orderedInverseJacobian = getMaturityExpiryValueMap(inverseJacobians);
    //TODO having to know the order of the parameters is not good - change the result that is returned from the fit
    final DoubleMatrix2D alphaResult = getVegaSurfaceForParameter(alpha, alphaGridNodeSensitivities, orderedInverseJacobian, 0, fittedDataPoints, definition);
    final DoubleMatrix2D rhoResult = getVegaSurfaceForParameter(rho, rhoGridNodeSensitivities, orderedInverseJacobian, 1, fittedDataPoints, definition);
    final DoubleMatrix2D nuResult = getVegaSurfaceForParameter(nu, nuGridNodeSensitivities, orderedInverseJacobian, 2, fittedDataPoints, definition);
    return (DoubleMatrix2D) ALGEBRA.add(alphaResult, ALGEBRA.add(nuResult, rhoResult));
  }

  public static Map<Double, DoubleMatrix2D> getVegaCube(final double alpha, final double rho, final double nu, final Map<Double, Interpolator1DDataBundle> alphaDataBundle,
      final Map<Double, Interpolator1DDataBundle> rhoDataBundle, final Map<Double, Interpolator1DDataBundle> nuDataBundle,
      final Map<DoublesPair, DoubleMatrix2D> inverseJacobians, final DoublesPair expiryMaturity, final Interpolator2D nodeSensitivityCalculator) {
    final Map<Double, List<Pair<Double, Double>>> alphaGridNodeSensitivities =
      SABRVegaCalculationUtils.getMaturityExpiryValueMap(nodeSensitivityCalculator.getNodeSensitivitiesForValue(alphaDataBundle, expiryMaturity));
    final Map<Double, List<Pair<Double, Double>>> nuGridNodeSensitivities =
      SABRVegaCalculationUtils.getMaturityExpiryValueMap(nodeSensitivityCalculator.getNodeSensitivitiesForValue(nuDataBundle, expiryMaturity));
    final Map<Double, List<Pair<Double, Double>>> rhoGridNodeSensitivities =
      SABRVegaCalculationUtils.getMaturityExpiryValueMap(nodeSensitivityCalculator.getNodeSensitivitiesForValue(rhoDataBundle, expiryMaturity));
    final Map<Double, List<Pair<Double, DoubleMatrix2D>>> orderedInverseJacobian = getMaturityExpiryValueMap(inverseJacobians);
    //TODO having to know the order of the parameters is not good - change the result that is returned from the fit
    final Map<Double, DoubleMatrix2D> alphaResult = getVegaCubeForParameter(alpha, alphaGridNodeSensitivities, orderedInverseJacobian, 0);
    final Map<Double, DoubleMatrix2D> rhoResult = getVegaCubeForParameter(rho, rhoGridNodeSensitivities, orderedInverseJacobian, 1);
    final Map<Double, DoubleMatrix2D> nuResult = getVegaCubeForParameter(nu, nuGridNodeSensitivities, orderedInverseJacobian, 2);
    if (!alphaResult.keySet().equals(nuResult.keySet())) {
      throw new OpenGammaRuntimeException("Did not have the same number of maturities in the nu results as in the alpha results");
    }
    if (!alphaResult.keySet().equals(rhoResult.keySet())) {
      throw new OpenGammaRuntimeException("Did not have the same number of maturities in the rho results as in the alpha results");
    }
    final Map<Double, DoubleMatrix2D> result = new HashMap<>();
    for (final Map.Entry<Double, DoubleMatrix2D> alphaEntry : alphaResult.entrySet()) {
      result.put(alphaEntry.getKey(), (DoubleMatrix2D) ALGEBRA.add(alphaEntry.getValue(), ALGEBRA.add(nuResult.get(alphaEntry.getKey()), rhoResult.get(alphaEntry.getKey()))));
    }
    return result;
  }

  private static <T> Map<Double, List<Pair<Double, T>>> getMaturityExpiryValueMap(final Map<DoublesPair, T> data) {
    final TreeMap<DoublesPair, T> sorted = new TreeMap<>(COMPARATOR);
    sorted.putAll(data);
    final Map<Double, List<Pair<Double, T>>> result = new TreeMap<>();
    for (final Map.Entry<DoublesPair, T> entry : sorted.entrySet()) {
      final double maturity = entry.getKey().second;
      if (!result.containsKey(maturity)) {
        final List<Pair<Double, T>> expiryValue = new ArrayList<>();
        expiryValue.add(Pairs.of(entry.getKey().first, entry.getValue()));
        result.put(maturity, expiryValue);
      } else {
        final List<Pair<Double, T>> expiryValue = result.get(maturity);
        expiryValue.add(Pairs.of(entry.getKey().first, entry.getValue()));
      }
    }
    return result;
  }

  private static DoubleMatrix2D getVegaSurfaceForParameter(final double parameter, final Map<Double, List<Pair<Double, Double>>> gridNodeSensitivities,
      final Map<Double, List<Pair<Double, DoubleMatrix2D>>> inverseJacobians, final int parameterNumber, final Map<Double, List<Double>> fittedDataPoints,
      final VolatilitySurfaceDefinition<Object, Object> definition) {
    if (inverseJacobians.size() != 1) {
      throw new OpenGammaRuntimeException("Cannot handle volatility cubes");
    }
    final List<Pair<Double, Double>> gns = gridNodeSensitivities.values().iterator().next();
    final List<Pair<Double, DoubleMatrix2D>> invJac = inverseJacobians.values().iterator().next();
    final int rows = gns.size();
    final double[][] result = new double[rows][];
    for (int i = 0; i < rows; i++) {
      final Pair<Double, Double> expirySensitivity = gns.get(i);
      final double expiry = expirySensitivity.getFirst();
      final List<Double> fittedDataPointsForExpiry = fittedDataPoints.get(expiry);
      final List<Object> allDataPointsForExpiry = Arrays.asList(definition.getYs());
      final int totalStrikes = allDataPointsForExpiry.size();
      final double sensitivity = expirySensitivity.getSecond();
      final Pair<Double, DoubleMatrix2D> expiryMatrix = invJac.get(i);
      if (Double.doubleToLongBits(expiry) != Double.doubleToLongBits(expiryMatrix.getFirst())) {
        throw new OpenGammaRuntimeException("Should never happen");
      }
      final DoubleMatrix2D m = expiryMatrix.getSecond();
      result[i] = new double[totalStrikes];
      for (int j = 0; j < m.getNumberOfColumns(); j++) {
        final double temp = m.getEntry(parameterNumber, j) * sensitivity * parameter;
        final int k = totalStrikes - allDataPointsForExpiry.indexOf(fittedDataPointsForExpiry.get(j)) - 1;
        result[i][k] = temp;
      }
    }
    return new DoubleMatrix2D(result);
  }

  private static Map<Double, DoubleMatrix2D> getVegaCubeForParameter(final double parameter, final Map<Double, List<Pair<Double, Double>>> gridNodeSensitivities,
      final Map<Double, List<Pair<Double, DoubleMatrix2D>>> inverseJacobians, final int parameterNumber) {
    final Map<Double, DoubleMatrix2D> vega = new TreeMap<>();
    for (final Map.Entry<Double, List<Pair<Double, Double>>> entry : gridNodeSensitivities.entrySet()) {
      final List<Pair<Double, Double>> gns = entry.getValue();
      final List<Pair<Double, DoubleMatrix2D>> invJac = inverseJacobians.get(entry.getKey());
      final int rows = gns.size();
      final double[][] result = new double[rows][];
      for (int i = 0; i < rows; i++) {
        final Pair<Double, Double> expirySensitivity = gns.get(i);
        final double expiry = expirySensitivity.getFirst();
        final double sensitivity = expirySensitivity.getSecond();
        final Pair<Double, DoubleMatrix2D> expiryMatrix = invJac.get(i);
        if (Double.doubleToLongBits(expiry) != Double.doubleToLongBits(expiryMatrix.getFirst())) {
          throw new OpenGammaRuntimeException("Should never happen");
        }
        final DoubleMatrix2D m = expiryMatrix.getSecond();
        result[i] = new double[m.getNumberOfColumns()];
        for (int j = 0; j < m.getNumberOfColumns(); j++) {
          result[i][j] = m.getEntry(parameterNumber, j) * sensitivity * parameter;
        }
      }
      vega.put(entry.getKey(), new DoubleMatrix2D(result));
    }
    return vega;
  }

  private static final class DoublesPairComparator implements Comparator<DoublesPair> {

    public DoublesPairComparator() {
    }

    @Override
    public int compare(final DoublesPair p1, final DoublesPair p2) {
      if (Double.compare(p1.second, p2.second) == 0) {
        return Double.compare(p1.first, p2.first);
      }
      return Double.compare(p1.second, p2.second);
    }
  }
}
