/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.forex.datasets.StandardDataSetsEURUSDForex;
import com.opengamma.analytics.financial.forex.method.FXMatrixUtils;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveUSD;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Tests the merge tools between different providers.
 */
public class ProviderUtilsTest {

  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_PAIR_USD136 = StandardDataSetsMulticurveUSD.getCurvesUSDOisL1L3L6();
  private static final MulticurveProviderDiscount MULTICURVE_USD136 = MULTICURVE_PAIR_USD136.getFirst();
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_PAIR_USDEUR_FX = StandardDataSetsEURUSDForex.getCurvesUSDOisEURFx();
  private static final MulticurveProviderDiscount MULTICURVE_USDEUR_FX = MULTICURVE_PAIR_USDEUR_FX.getFirst();
  private static final Currency USD = Currency.USD;
  private static final Currency EUR = Currency.EUR;
  private static final IborIndex[] INDEX_USD136 = StandardDataSetsMulticurveUSD.indexIborArrayUSDOisL3();

  private static final double TOLERANCE_FX_RATE = 1.0E-10;

  @Test
  /**
   * Test merge of a single provider.
   */
  public void mergeUSD136() {
    final ArrayList<MulticurveProviderDiscount> multicurveList = new ArrayList<>();
    multicurveList.add(MULTICURVE_USD136);
    final MulticurveProviderDiscount merged1 = ProviderUtils.mergeDiscountingProviders(multicurveList);
    assertEquals("ProviderUtils - Merge multi-curve provider", MULTICURVE_USD136.getCurve(USD), merged1.getCurve(USD));
    for (int loopi = 0; loopi < INDEX_USD136.length; loopi++) {
      assertEquals("ProviderUtils - Merge multi-curve provider", MULTICURVE_USD136.getCurve(INDEX_USD136[loopi]), merged1.getCurve(INDEX_USD136[loopi]));
    }
    assertTrue("ProviderUtils - Merge multi-curve provider", FXMatrixUtils.compare(MULTICURVE_USD136.getFxRates(), merged1.getFxRates(), TOLERANCE_FX_RATE));
  }

  @Test
  /**
   * Test merge of a single provider.
   */
  public void mergeUSDEURForex() {
    final ArrayList<MulticurveProviderDiscount> multicurveList = new ArrayList<>();
    multicurveList.add(MULTICURVE_USDEUR_FX);
    final MulticurveProviderDiscount merged1 = ProviderUtils.mergeDiscountingProviders(multicurveList);
    assertEquals("ProviderUtils - Merge multi-curve provider", MULTICURVE_USDEUR_FX.getCurve(USD), merged1.getCurve(USD));
    assertEquals("ProviderUtils - Merge multi-curve provider", MULTICURVE_USDEUR_FX.getCurve(EUR), merged1.getCurve(EUR));
    assertTrue("ProviderUtils - Merge multi-curve provider", FXMatrixUtils.compare(MULTICURVE_USDEUR_FX.getFxRates(), merged1.getFxRates(), TOLERANCE_FX_RATE));
  }

}
