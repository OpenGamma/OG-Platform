/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.util;

import static org.testng.AssertJUnit.assertTrue;

import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivityUtils;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.MatrixAlgebraFactory;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Class used in test to assert that two sensitivity objects are the same or different.
 */
public class AssertSensitivityObjects {

  /**
   * Compare two sensitivities with a given tolerance. The tolerance is used for both the time and the value. The two sensitivities are suppose to be in the same time order.
   * The comparison is done on the discounting curve and forward curves sensitivities.
   * @param msg The message.
   * @param sensitivity1 The first sensitivity.
   * @param sensitivity2 The second sensitivity.
   * @param tolerance The tolerance.
   * @param opposite The flag indicating if the opposite result should be used.
   * @return True if the difference is below the tolerance and False if not. If the curves are not the same it returns False.
   */
  private static boolean compare(final String msg, final MulticurveSensitivity sensitivity1, final MulticurveSensitivity sensitivity2, final double tolerance, final boolean opposite) {
    boolean cmp = true;
    if (!InterestRateCurveSensitivityUtils.compare(sensitivity1.getYieldDiscountingSensitivities(), sensitivity2.getYieldDiscountingSensitivities(), tolerance)) {
      cmp = false;
    }
    if (!compareFwd(sensitivity1.getForwardSensitivities(), sensitivity2.getForwardSensitivities(), tolerance)) {
      cmp = false;
    }
    if (opposite) {
      cmp = !cmp;
    }
    assertTrue(msg, cmp);
    return cmp;
  }

  /**
   * Compare two sensitivities with a given tolerance. The tolerance is used for both the time and the value. The two sensitivities are suppose to be in the same time order.
   * The comparison is done on the discounting curve and forward curves sensitivities.
   * @param msg The message.
   * @param sensitivity1 The first sensitivity.
   * @param sensitivity2 The second sensitivity.
   * @param tolerance The tolerance.
   * @return True if the difference is below the tolerance and False if not. If the curves are not the same it returns False.
   */
  public static boolean assertEquals(final String msg, final MulticurveSensitivity sensitivity1, final MulticurveSensitivity sensitivity2, final double tolerance) {
    return compare(msg, sensitivity1, sensitivity2, tolerance, false);
  }

  public static boolean assertDoesNotEqual(final String msg, final MulticurveSensitivity sensitivity1, final MulticurveSensitivity sensitivity2, final double tolerance) {
    return compare(msg, sensitivity1, sensitivity2, tolerance, true);
  }

  /**
   * Compare two maps of sensitivities with a given tolerance. The tolerance is used for both the time and the value. The two sensitivities are suppose to be in the same time order.
   * @param sensi1 The first sensitivity (as a map).
   * @param sensi2 The second sensitivity (as a map).
   * @param tolerance The tolerance.
   * @return True if the difference is below the tolerance and False if not. If the curves are not the same it returns False.
   */
  private static boolean compareFwd(final Map<String, List<ForwardSensitivity>> sensi1, final Map<String, List<ForwardSensitivity>> sensi2, final double tolerance) {
    ArgumentChecker.notNull(sensi1, "sensitivity");
    ArgumentChecker.notNull(sensi2, "sensitivity");
    for (final Map.Entry<String, List<ForwardSensitivity>> entry : sensi1.entrySet()) {
      final String name = entry.getKey();
      if (sensi2.containsKey(name)) {
        if (!compareFwd(entry.getValue(), sensi2.get(name), tolerance)) {
          return false;
        }
      } else {
        return false;
      }
    }
    for (final Map.Entry<String, List<ForwardSensitivity>> entry : sensi2.entrySet()) {
      if (!(sensi1.containsKey(entry.getKey()))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Compare two lists of sensitivities with a given tolerance. The tolerance is used for both the time and the value. The two sensitivities are suppose to be in the same time order.
   * @param sensi1 The first sensitivity (as a list).
   * @param sensi2 The second sensitivity (as a list).
   * @param tolerance The tolerance.
   * @return True if the difference is below the tolerance and False if not.
   */
  private static boolean compareFwd(final List<ForwardSensitivity> sensi1, final List<ForwardSensitivity> sensi2, final double tolerance) {
    for (int looptime = 0; looptime < sensi1.size(); looptime++) {
      final double startTime1 = sensi1.get(looptime).getStartTime();
      final double startTime2 = sensi2.get(looptime).getStartTime();
      if ((Math.abs(startTime1 - startTime2) > tolerance) || (Math.abs(sensi1.get(looptime).getValue() - sensi2.get(looptime).getValue()) > tolerance)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Compare two sensitivities with a given tolerance. The tolerance is used for both the time and the value. The two sensitivities are suppose to be in the same time order.
   * The comparison is done on the discounting curve and forward curves sensitivities.
   * @param msg The message.
   * @param sensi1 The first sensitivity.
   * @param sensi2 The second sensitivity.
   * @param tolerance The tolerance.
   * @return True if the difference is below the tolerance and False if not. If the curves are not the same it returns False.
   */
  public static boolean assertEquals(final String msg, final InflationSensitivity sensi1, 
      final InflationSensitivity sensi2, final double tolerance) {
    boolean cmp = true;
    if (!InterestRateCurveSensitivityUtils.compare(sensi1.getYieldDiscountingSensitivities(), 
        sensi2.getYieldDiscountingSensitivities(), tolerance)) {
      cmp = false;
    }
    if (!compareFwd(sensi1.getForwardSensitivities(), sensi2.getForwardSensitivities(), tolerance)) {
      cmp = false;
    }
    if (!InterestRateCurveSensitivityUtils.compare(sensi1.getPriceCurveSensitivities(), 
        sensi2.getPriceCurveSensitivities(), tolerance)) {
      cmp = false;
    }
    assertTrue(msg, cmp);
    return cmp;
  }

  /**
   * Compare two sensitivities with a given tolerance. The tolerance is used for both the time and the value. 
   * For each currency, the two sensitivities are suppose to be in the same time order.
   * @param msg The message.
   * @param sensi1 The first sensitivity.
   * @param sensi2 The second sensitivity.
   * @param tolerance The tolerance.
   * @return True if the difference is below the tolerance and False if not. If the currencies or the curves are not the same it returns False.
   */
  public static boolean assertEquals(final String msg, final MultipleCurrencyMulticurveSensitivity sensi1, final MultipleCurrencyMulticurveSensitivity sensi2, final double tolerance) {
    boolean cmp = true;
    final boolean keycmp = sensi1.getCurrencies().equals(sensi2.getCurrencies());
    if (!keycmp) {
      cmp = false;
    }
    for (final Currency loopccy : sensi1.getCurrencies()) {
      if (!assertEquals(msg, sensi1.getSensitivity(loopccy), sensi2.getSensitivity(loopccy), tolerance)) {
        cmp = false;
      }
    }
    assertTrue(msg, cmp);
    return cmp;
  }

  /**
   * Compare two sensitivities with a given tolerance. The tolerance is used for both the time and the value. 
   * For each currency, the two sensitivities are suppose to be in the same time order.
   * @param msg The message.
   * @param sensi1 The first sensitivity.
   * @param sensi2 The second sensitivity.
   * @param tolerance The tolerance.
   * @return True if the difference is below the tolerance and False if not. If the currencies or the curves are not the same it returns False.
   */
  public static boolean assertEquals(final String msg, final MultipleCurrencyInflationSensitivity sensi1, 
      final MultipleCurrencyInflationSensitivity sensi2, final double tolerance) {
    boolean cmp = true;
    final boolean keycmp = sensi1.getCurrencies().equals(sensi2.getCurrencies());
    if (!keycmp) {
      cmp = false;
    }
    for (final Currency loopccy : sensi1.getCurrencies()) {
      if (!assertEquals(msg, sensi1.getSensitivity(loopccy), sensi2.getSensitivity(loopccy), tolerance)) {
        cmp = false;
      }
    }
    assertTrue(msg, cmp);
    return cmp;
  }

  /**
   * Compare two sensitivities with a given tolerance.
   * @param msg The message.
   * @param sensitivity1 The first sensitivity.
   * @param sensitivity2 The second sensitivity.
   * @param tolerance The tolerance.
   * @param opposite The flag indicating if the opposite result should be used.
   * @return True if the difference is below the tolerance and False if not. If the curves are not the same it returns False.
   */
  private static boolean compare(final String msg, final MultipleCurrencyParameterSensitivity sensitivity1, final MultipleCurrencyParameterSensitivity sensitivity2, final double tolerance,
      final boolean opposite) {
    ArgumentChecker.notNull(sensitivity1, "sensitivity1");
    ArgumentChecker.notNull(sensitivity2, "sensitivity2");
    ArgumentChecker.isTrue(tolerance > 0, "tolerance must be greater than 0; have {}", tolerance);
    boolean cmp = true;
    final MatrixAlgebra algebra = MatrixAlgebraFactory.COMMONS_ALGEBRA;
    final Map<Pair<String, Currency>, DoubleMatrix1D> map1 = sensitivity1.getSensitivities();
    final Map<Pair<String, Currency>, DoubleMatrix1D> map2 = sensitivity2.getSensitivities();
    for (final Map.Entry<Pair<String, Currency>, DoubleMatrix1D> entry : map1.entrySet()) {
      final Pair<String, Currency> nameCcy = entry.getKey();
      if (map2.get(nameCcy) == null) {
        if (algebra.getNormInfinity(entry.getValue()) > tolerance) {
          cmp = false;
        }
      } else {
        if (algebra.getNormInfinity(algebra.add(entry.getValue(), algebra.scale(map2.get(nameCcy), -1.0))) > tolerance) {
          cmp = false;
        }
      }
    }
    for (final Map.Entry<Pair<String, Currency>, DoubleMatrix1D> entry : map2.entrySet()) {
      final Pair<String, Currency> nameCcy = entry.getKey();
      if (map1.get(nameCcy) == null) {
        if (algebra.getNormInfinity(entry.getValue()) > tolerance) {
          cmp = false;
        }
      } else {
        if (algebra.getNormInfinity(algebra.add(entry.getValue(), algebra.scale(map1.get(nameCcy), -1.0))) > tolerance) {
          cmp = false;
        }
      }
    }
    if (opposite) {
      cmp = !cmp;
    }
    assertTrue(msg, cmp);
    return cmp;
  }

  /**
   * Assert that two sensitivities are equal within a given tolerance. The tolerance is applied for each value (not to the total).
   * @param msg The message.
   * @param sensitivity1 The first sensitivity.
   * @param sensitivity2 The second sensitivity.
   * @param tolerance The tolerance.
   * @return True if the difference is below the tolerance and False if not. If the curves are not the same it returns False.
   */
  public static boolean assertEquals(final String msg, final MultipleCurrencyParameterSensitivity sensitivity1, final MultipleCurrencyParameterSensitivity sensitivity2, final double tolerance) {
    return compare(msg, sensitivity1, sensitivity2, tolerance, false);
  }

  /**
   * Compare two sensitivities with a given tolerance.
   * @param msg The message.
   * @param sensitivity1 The first sensitivity.
   * @param sensitivity2 The second sensitivity.
   * @param tolerance The tolerance.
   * @return True if the difference is above the tolerance and False if not.
   */
  public static boolean assertDoesNotEqual(final String msg, final MultipleCurrencyParameterSensitivity sensitivity1, final MultipleCurrencyParameterSensitivity sensitivity2, final double tolerance) {
    return compare(msg, sensitivity1, sensitivity2, tolerance, true);
  }

  /**
   * Compare two sensitivities with a given tolerance.
   * @param msg The message.
   * @param sensitivity1 The first sensitivity.
   * @param sensitivity2 The second sensitivity.
   * @param tolerance The tolerance.
   * @param opposite The flag indicating if the opposite result should be used.
   * @return True if the difference is below the tolerance and False if not. If the curves are not the same it returns False.
   */
  private static boolean compare(final String msg, final SimpleParameterSensitivity sensitivity1, final SimpleParameterSensitivity sensitivity2, final double tolerance, final boolean opposite) {
    ArgumentChecker.notNull(sensitivity1, "sensitivity1");
    ArgumentChecker.notNull(sensitivity2, "sensitivity2");
    ArgumentChecker.isTrue(tolerance > 0, "tolerance must be greater than 0; have {}", tolerance);
    boolean cmp = true;
    final MatrixAlgebra algebra = MatrixAlgebraFactory.COMMONS_ALGEBRA;
    final Map<String, DoubleMatrix1D> map1 = sensitivity1.getSensitivities();
    final Map<String, DoubleMatrix1D> map2 = sensitivity2.getSensitivities();
    for (final Map.Entry<String, DoubleMatrix1D> entry : map1.entrySet()) {
      final String name = entry.getKey();
      if (map2.get(name) == null) {
        if (algebra.getNormInfinity(entry.getValue()) > tolerance) {
          cmp = false;
        }
      } else {
        if (algebra.getNormInfinity(algebra.add(entry.getValue(), algebra.scale(map2.get(name), -1.0))) > tolerance) {
          cmp = false;
        }
      }
    }
    if (opposite) {
      cmp = !cmp;
    }
    assertTrue(msg, cmp);
    return cmp;
  }

  /**
   * Assert that two sensitivities are equal within a given tolerance. The tolerance is applied for each value (not to the total).
   * @param msg The message.
   * @param sensitivity1 The first sensitivity.
   * @param sensitivity2 The second sensitivity.
   * @param tolerance The tolerance.
   * @return True if the difference is below the tolerance and False if not. If the curves are not the same it returns False.
   */
  public static boolean assertEquals(final String msg, final SimpleParameterSensitivity sensitivity1, final SimpleParameterSensitivity sensitivity2, final double tolerance) {
    return compare(msg, sensitivity1, sensitivity2, tolerance, false);
  }

  /**
   * Compare two sensitivities with a given tolerance.
   * @param msg The message.
   * @param sensitivity1 The first sensitivity.
   * @param sensitivity2 The second sensitivity.
   * @param tolerance The tolerance.
   * @return True if the difference is above the tolerance and False if not.
   */
  public static boolean assertDoesNotEqual(final String msg, final SimpleParameterSensitivity sensitivity1, final SimpleParameterSensitivity sensitivity2, final double tolerance) {
    return compare(msg, sensitivity1, sensitivity2, tolerance, true);
  }

  /**
   * Compare two sensitivities with a given tolerance. The tolerance is used for both the time and the value. The two sensitivities are suppose to be in the same time order.
   * @param msg The message.
   * @param sensitivity1 The first sensitivity.
   * @param sensitivity2 The second sensitivity.
   * @param tolerance The tolerance.
   * @param opposite The flag indicating if the opposite result should be used.
   * @return True if the difference is below the tolerance and False if not. If the curves are not the same it returns False.
   */
  private static boolean compare(final String msg, final InterestRateCurveSensitivity sensitivity1, final InterestRateCurveSensitivity sensitivity2, final double tolerance, final boolean opposite) {
    boolean cmp = true;
    cmp = InterestRateCurveSensitivityUtils.compare(sensitivity1.getSensitivities(), sensitivity2.getSensitivities(), tolerance);
    if (opposite) {
      cmp = !cmp;
    }
    assertTrue(msg, cmp);
    return cmp;
  }

  public static boolean assertEquals(final String msg, final InterestRateCurveSensitivity sensitivity1, final InterestRateCurveSensitivity sensitivity2, final double tolerance) {
    return compare(msg, sensitivity1, sensitivity2, tolerance, false);
  }

  public static boolean assertDoesNotEqual(final String msg, final InterestRateCurveSensitivity sensitivity1, final InterestRateCurveSensitivity sensitivity2, final double tolerance) {
    return compare(msg, sensitivity1, sensitivity2, tolerance, true);
  }

  /**
   * Compare two sensitivities with a given tolerance. The tolerance is used for both the time and the value. 
   * @param msg The message.
   * For each currency, the two sensitivities are suppose to be in the same time order.
   * @param sensi1 The first sensitivity.
   * @param sensi2 The second sensitivity.
   * @param tolerance The tolerance.
   * @return True if the difference is below the tolerance and False if not. If the currencies or the curves are not the same it returns False.
   */
  public static boolean assertEquals(final String msg, final MultipleCurrencyInterestRateCurveSensitivity sensi1, final MultipleCurrencyInterestRateCurveSensitivity sensi2, final double tolerance) {
    boolean cmp = true;
    final boolean keycmp = sensi1.getCurrencies().equals(sensi2.getCurrencies());
    if (!keycmp) {
      cmp = false;
    }
    for (final Currency loopccy : sensi1.getCurrencies()) {
      if (!assertEquals(msg, sensi1.getSensitivity(loopccy), sensi2.getSensitivity(loopccy), tolerance)) {
        cmp = false;
      }
    }
    assertTrue(msg, cmp);
    return cmp;
  }

}
