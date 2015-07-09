/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.YearMonth;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

@Test(groups = TestGroup.UNIT)
public class MulticurveBundleTest {

  /**
   * Tests that curve node ID objects can be used to look up the index of the curve node
   */
  @Test
  public void curveNodeIds() {
    String usdDiscounting = "usdDiscounting";
    YieldCurve usdDiscountingYieldCurve = new YieldCurve(usdDiscounting, ConstantDoublesCurve.from(1d));

    String eurDiscounting = "eurDiscounting";
    YieldCurve eurDiscountingYieldCurve = new YieldCurve(eurDiscounting, ConstantDoublesCurve.from(1d));

    String gbpDiscounting = "gbpDiscounting";
    YieldCurve gbpDiscountingYieldCurve = new YieldCurve(gbpDiscounting, ConstantDoublesCurve.from(1d));

    MulticurveProviderDiscount multicurve = new MulticurveProviderDiscount();
    multicurve.setCurve(Currency.USD, usdDiscountingYieldCurve);
    multicurve.setCurve(Currency.EUR, eurDiscountingYieldCurve);
    multicurve.setCurve(Currency.GBP, gbpDiscountingYieldCurve);
    List<TenorCurveNodeId> usdTenors =
        TenorCurveNodeId.listOf(Tenor.ONE_MONTH, Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.FIVE_YEARS);
    List<FuturesExpiryCurveNodeId> eurExpiries =
        FuturesExpiryCurveNodeId.listOf(YearMonth.of(2015, 3), YearMonth.of(2015, 6), YearMonth.of(2015, 9));
    List<CurveNodeId> gbpExpiries =
        ImmutableList.<CurveNodeId>builder()
            .add(TenorCurveNodeId.of(Tenor.ONE_MONTH))
            .add(FuturesExpiryCurveNodeId.of(2015, 6))
            .add(TenorCurveNodeId.of(Tenor.SIX_MONTHS))
            .build();
    Map<String, List<? extends CurveNodeId>> curveNodeIds =
        ImmutableMap.of(
            usdDiscounting, usdTenors,
            eurDiscounting, eurExpiries,
            gbpDiscounting, gbpExpiries);
    MulticurveBundle bundle = new MulticurveBundle(multicurve, curveNodeIds, new CurveBuildingBlockBundle());

    assertEquals(Integer.valueOf(0), bundle.curveNodeIndex(gbpDiscounting, TenorCurveNodeId.of(Tenor.ONE_MONTH)));
    assertEquals(Integer.valueOf(1), bundle.curveNodeIndex(gbpDiscounting, FuturesExpiryCurveNodeId.of(2015, 6)));
    assertEquals(Integer.valueOf(2), bundle.curveNodeIndex(gbpDiscounting, TenorCurveNodeId.of(Tenor.SIX_MONTHS)));

    assertEquals(Integer.valueOf(0), bundle.curveNodeIndex(eurDiscounting, FuturesExpiryCurveNodeId.of(2015, 3)));
    assertEquals(Integer.valueOf(1), bundle.curveNodeIndex(eurDiscounting, FuturesExpiryCurveNodeId.of(2015, 6)));
    assertEquals(Integer.valueOf(2), bundle.curveNodeIndex(eurDiscounting, FuturesExpiryCurveNodeId.of(2015, 9)));

    assertEquals(Integer.valueOf(0), bundle.curveNodeIndex(usdDiscounting, TenorCurveNodeId.of(Tenor.ONE_MONTH)));
    assertEquals(Integer.valueOf(1), bundle.curveNodeIndex(usdDiscounting, TenorCurveNodeId.of(Tenor.ONE_YEAR)));
    assertEquals(Integer.valueOf(2), bundle.curveNodeIndex(usdDiscounting, TenorCurveNodeId.of(Tenor.TWO_YEARS)));
    assertEquals(Integer.valueOf(3), bundle.curveNodeIndex(usdDiscounting, TenorCurveNodeId.of(Tenor.FIVE_YEARS)));
  }

  /**
   * Tests that an exception is thrown if the list of curve node IDs contains duplicates
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void duplicateCurveNodeIds() {
    String usdDiscounting = "usdDiscounting";
    YieldCurve usdDiscountingYieldCurve = new YieldCurve(usdDiscounting, ConstantDoublesCurve.from(1d));
    MulticurveProviderDiscount multicurve = new MulticurveProviderDiscount();
    multicurve.setCurve(Currency.USD, usdDiscountingYieldCurve);
    List<? extends CurveNodeId> usdTenors =
        TenorCurveNodeId.listOf(Tenor.ONE_MONTH, Tenor.ONE_YEAR, Tenor.ONE_YEAR, Tenor.FIVE_YEARS);
    Map<String, List<? extends CurveNodeId>> curveNodeIds =
        ImmutableMap.<String, List<? extends CurveNodeId>>of(usdDiscounting, usdTenors);
    new MulticurveBundle(multicurve, curveNodeIds, new CurveBuildingBlockBundle());
  }
}
