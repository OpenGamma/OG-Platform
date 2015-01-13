/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.sesame.CurveNodeId;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.TenorCurveNodeId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

@Test(groups = TestGroup.UNIT)
public class MulticurvePointShiftTest {

  public void applyAbsoluteShifts() {
    double[] xData = {0.1, 1, 2, 5};
    double[] yData = {1, 2, 3, 4};
    InterpolatedDoublesCurve doublesCurve = InterpolatedDoublesCurve.from(xData, yData, new LinearInterpolator1D());
    String curveName = "curveName";
    YieldCurve yieldCurve = new YieldCurve(curveName, doublesCurve);
    MulticurveProviderDiscount multicurve = new MulticurveProviderDiscount();
    multicurve.setCurve(Currency.USD, yieldCurve);
    List<TenorCurveNodeId> tenors =
        TenorCurveNodeId.listOf(Tenor.ONE_MONTH, Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.FIVE_YEARS);
    Map<String, List<? extends CurveNodeId>> curveNodeIds =
        ImmutableMap.<String, List<? extends CurveNodeId>>of(curveName, tenors);
    MulticurveBundle bundle = new MulticurveBundle(multicurve, curveNodeIds, new CurveBuildingBlockBundle());
    MulticurvePointShift shift =
        MulticurvePointShiftBuilder.absolute()
            .shift(TenorCurveNodeId.of(Tenor.ONE_YEAR), 0.01)
            .shift(TenorCurveNodeId.of(Tenor.FIVE_YEARS), 0.02)
            .build();

    MulticurveBundle shiftedBundle = (MulticurveBundle) shift.apply(bundle, StandardMatchDetails.multicurve(curveName));
    YieldAndDiscountCurve shiftedCurve = shiftedBundle.getMulticurveProvider().getDiscountingCurves().get(Currency.USD);
    assertEquals(curveName, shiftedCurve.getName());
    assertEquals(1d, shiftedCurve.getInterestRate(0.1));
    assertEquals(2.01, shiftedCurve.getInterestRate(1d));
    assertEquals(3d, shiftedCurve.getInterestRate(2d));
    assertEquals(4.02, shiftedCurve.getInterestRate(5d));
  }

  public void applyRelativeShifts() {
    double[] xData = {0.1, 1, 2, 5};
    double[] yData = {1, 2, 3, 4};
    InterpolatedDoublesCurve doublesCurve = InterpolatedDoublesCurve.from(xData, yData, new LinearInterpolator1D());
    String curveName = "curveName";
    YieldCurve yieldCurve = new YieldCurve(curveName, doublesCurve);
    MulticurveProviderDiscount multicurve = new MulticurveProviderDiscount();
    multicurve.setCurve(Currency.USD, yieldCurve);
    List<TenorCurveNodeId> tenors =
        TenorCurveNodeId.listOf(Tenor.ONE_MONTH, Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.FIVE_YEARS);
    Map<String, List<? extends CurveNodeId>> curveNodeIds =
        ImmutableMap.<String, List<? extends CurveNodeId>>of(curveName, tenors);
    MulticurveBundle bundle = new MulticurveBundle(multicurve, curveNodeIds, new CurveBuildingBlockBundle());
    MulticurvePointShift shift =
        MulticurvePointShiftBuilder.relative()
            .shift(TenorCurveNodeId.of(Tenor.ONE_YEAR), 0.1)
            .shift(TenorCurveNodeId.of(Tenor.FIVE_YEARS), 0.2)
            .build();

    MulticurveBundle shiftedBundle = (MulticurveBundle) shift.apply(bundle, StandardMatchDetails.multicurve(curveName));
    YieldAndDiscountCurve shiftedCurve = shiftedBundle.getMulticurveProvider().getDiscountingCurves().get(Currency.USD);
    assertEquals(curveName, shiftedCurve.getName());
    assertEquals(1d, shiftedCurve.getInterestRate(0.1));
    assertEquals(2.2, shiftedCurve.getInterestRate(1d));
    assertEquals(3d, shiftedCurve.getInterestRate(2d));
    assertEquals(4.8, shiftedCurve.getInterestRate(5d));
  }
}
