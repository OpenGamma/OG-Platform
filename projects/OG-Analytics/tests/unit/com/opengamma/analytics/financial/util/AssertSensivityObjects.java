/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.util;

import static org.testng.AssertJUnit.assertTrue;

import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivityUtils;
import com.opengamma.analytics.financial.interestrate.market.description.CurveSensitivityMarket;
import com.opengamma.analytics.financial.interestrate.market.description.MarketForwardSensitivity;
import com.opengamma.analytics.financial.interestrate.market.description.MultipleCurrencyCurveSensitivityMarket;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class AssertSensivityObjects {

  /**
   * Compare two sensitivities with a given tolerance. The tolerance is used for both the time and the value. The two sensitivities are suppose to be in the same time order.
   * The comparison is done on the discounting curve and forward curves sensitivities.
   * @param msg The message.
   * @param sensi1 The first sensitivity.
   * @param sensi2 The second sensitivity.
   * @param tolerance The tolerance.
   * @return True if the difference is below the tolerance and False if not. If the curves are not the same it returns False.
   */
  public static boolean assertEquals(final String msg, final CurveSensitivityMarket sensi1, final CurveSensitivityMarket sensi2, final double tolerance) {
    boolean cmp = true;
    if (!InterestRateCurveSensitivityUtils.compare(sensi1.getYieldDiscountingSensitivities(), sensi2.getYieldDiscountingSensitivities(), tolerance)) {
      cmp = false;
    }
    if (!compareFwd(sensi1.getForwardSensitivities(), sensi2.getForwardSensitivities(), tolerance)) {
      cmp = false;
    }
    if (!InterestRateCurveSensitivityUtils.compare(sensi1.getPriceCurveSensitivities(), sensi2.getPriceCurveSensitivities(), tolerance)) {
      cmp = false;
    }
    assertTrue(msg, cmp);
    return cmp;
  }

  /**
   * Compare two maps of sensitivities with a given tolerance. The tolerance is used for both the time and the value. The two sensitivities are suppose to be in the same time order.
   * @param sensi1 The first sensitivity (as a map).
   * @param sensi2 The second sensitivity (as a map).
   * @param tolerance The tolerance.
   * @return True if the difference is below the tolerance and False if not. If the curves are not the same it returns False.
   */
  private static boolean compareFwd(final Map<String, List<MarketForwardSensitivity>> sensi1, final Map<String, List<MarketForwardSensitivity>> sensi2, final double tolerance) {
    ArgumentChecker.notNull(sensi1, "sensitivity");
    ArgumentChecker.notNull(sensi2, "sensitivity");
    for (final Map.Entry<String, List<MarketForwardSensitivity>> entry : sensi1.entrySet()) {
      final String name = entry.getKey();
      if (sensi2.containsKey(name)) {
        if (!compareFwd(entry.getValue(), sensi2.get(name), tolerance)) {
          return false;
        }
      } else {
        return false;
      }
    }
    for (final Map.Entry<String, List<MarketForwardSensitivity>> entry : sensi2.entrySet()) {
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
  private static boolean compareFwd(final List<MarketForwardSensitivity> sensi1, final List<MarketForwardSensitivity> sensi2, final double tolerance) {
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
   * Compare two sensitivities with a given tolerance. The tolerance is used for both the time and the value. 
   * For each currency, the two sensitivities are suppose to be in the same time order.
   * @param msg The message.
   * @param sensi1 The first sensitivity.
   * @param sensi2 The second sensitivity.
   * @param tolerance The tolerance.
   * @return True if the difference is below the tolerance and False if not. If the currencies or the curves are not the same it returns False.
   */
  public static boolean assertEquals(final String msg, final MultipleCurrencyCurveSensitivityMarket sensi1, final MultipleCurrencyCurveSensitivityMarket sensi2, final double tolerance) {
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
