/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderForward;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests related to the Market bundle with forward provided directly my forward curves.
 */
@Test(groups = TestGroup.UNIT)
public class CurveForwardProviderTest {

  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final InflationProviderForward MARKET_1 = new InflationProviderForward();
  private static final YieldAndDiscountCurve CURVE_50 = YieldCurve.from(ConstantDoublesCurve.from(0.0500));
  private static final YieldAndDiscountCurve CURVE_45 = YieldCurve.from(ConstantDoublesCurve.from(0.0450));
  private static final DoublesCurve CURVE_INT_1 = InterpolatedDoublesCurve.from(new double[] {0.0, 0.5, 1.0 }, new double[] {0.01, 0.02, 0.01 }, LINEAR_FLAT);;
  private static final DoublesCurve CURVE_35 = ConstantDoublesCurve.from(0.0350);
  private static final DoublesCurve CURVE_30 = ConstantDoublesCurve.from(0.0300);
  private static final IborIndex EURIBOR_3M = IndexIborMaster.getInstance().getIndex("EURIBOR3M");
  private static final IborIndex EURIBOR_6M = IndexIborMaster.getInstance().getIndex("EURIBOR6M");
  private static final IborIndex USDLIBOR_3M = IndexIborMaster.getInstance().getIndex("USDLIBOR3M");
  private static final String NAME_EUR_PRICE_INDEX = "Euro HICP x";
  private static final IndexPrice PRICE_INDEX_EUR = new IndexPrice(NAME_EUR_PRICE_INDEX, Currency.EUR);
  private static double[] INDEX_VALUE = new double[] {108.23, 108.64, 111.0, 119.0, 129.0, 149.0 };
  private static double[] TIME_VALUE = new double[] {-3.0 / 12.0, -2.0 / 12.0, 9.0 / 12.0, 4.0 + 9.0 / 12.0, 9.0 + 9.0 / 12.0, 19.0 + 9.0 / 12.0 };
  private static final InterpolatedDoublesCurve CURVE_HICP = InterpolatedDoublesCurve.from(TIME_VALUE, INDEX_VALUE, LINEAR_FLAT);
  private static final PriceIndexCurve PRICE_INDEX_HICP_CURVE = new PriceIndexCurve(CURVE_HICP);
  static {
    MARKET_1.setCurve(Currency.EUR, CURVE_50);
    MARKET_1.setCurve(Currency.USD, CURVE_45);
    MARKET_1.setCurve(EURIBOR_3M, CURVE_INT_1);
    MARKET_1.setCurve(EURIBOR_6M, CURVE_35);
    MARKET_1.setCurve(USDLIBOR_3M, CURVE_30);
    MARKET_1.setCurve(PRICE_INDEX_EUR, PRICE_INDEX_HICP_CURVE);
  }
  private static final double TOLERANCE_RATE = 1.0E-8;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void setExisitingDscCurve() {
    MARKET_1.setCurve(Currency.EUR, CURVE_50);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void setExisitingFwdCurve() {
    MARKET_1.setCurve(EURIBOR_3M, CURVE_INT_1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void setExisitingPriceCurve() {
    MARKET_1.setCurve(PRICE_INDEX_EUR, PRICE_INDEX_HICP_CURVE);
  }

  @Test
  public void setterGetter() {
    assertEquals("Market: getter", CURVE_50, MARKET_1.getCurve(Currency.EUR));
    assertEquals("Market: getter", CURVE_45, MARKET_1.getCurve(Currency.USD));
    assertEquals("Market: getter", CURVE_INT_1, MARKET_1.getCurve(EURIBOR_3M));
    assertEquals("Market: getter", CURVE_35, MARKET_1.getCurve(EURIBOR_6M));
    assertEquals("Market: getter", CURVE_30, MARKET_1.getCurve(USDLIBOR_3M));
    assertEquals("Market: getter", PRICE_INDEX_HICP_CURVE, MARKET_1.getCurve(PRICE_INDEX_EUR));
  }

  @Test
  public void forwardRate() {
    final double[] time = new double[] {0.0, 0.75, 2.50 };
    for (final double element : time) {
      assertEquals("MarketForward: forward rate", CURVE_INT_1.getYValue(element), MARKET_1.getForwardRate(EURIBOR_3M, element, element + 0.25, 0.25), TOLERANCE_RATE);
    }
  }

}
