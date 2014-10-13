/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurveSimple;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class InflationProviderDiscountTest {

  private static final InflationProviderDiscount MARKET_1 = new InflationProviderDiscount();
  private static final YieldAndDiscountCurve CURVE_50 = YieldCurve.from(ConstantDoublesCurve.from(0.0500));
  private static final YieldAndDiscountCurve CURVE_45 = YieldCurve.from(ConstantDoublesCurve.from(0.0450));
  private static final YieldAndDiscountCurve CURVE_40 = YieldCurve.from(ConstantDoublesCurve.from(0.0400));
  private static final YieldAndDiscountCurve CURVE_35 = YieldCurve.from(ConstantDoublesCurve.from(0.0350));
  private static final YieldAndDiscountCurve CURVE_30 = YieldCurve.from(ConstantDoublesCurve.from(0.0300));
  private static final IborIndex EURIBOR_3M = IndexIborMaster.getInstance().getIndex("EURIBOR3M");
  private static final IborIndex EURIBOR_6M = IndexIborMaster.getInstance().getIndex("EURIBOR6M");
  private static final IborIndex USDLIBOR_3M = IndexIborMaster.getInstance().getIndex("USDLIBOR3M");
  private static final String NAME_EUR_PRICE_INDEX = "Euro HICP x";
  private static final IndexPrice PRICE_INDEX_EUR = new IndexPrice(NAME_EUR_PRICE_INDEX, Currency.EUR);
  private static double[] INDEX_VALUE = new double[] {108.23, 108.64, 111.0, 119.0, 129.0, 149.0 };
  private static double[] TIME_VALUE = new double[] {-3.0 / 12.0, -2.0 / 12.0, 9.0 / 12.0, 4.0 + 9.0 / 12.0, 9.0 + 9.0 / 12.0, 19.0 + 9.0 / 12.0 };
  private static final InterpolatedDoublesCurve CURVE = InterpolatedDoublesCurve.from(TIME_VALUE, INDEX_VALUE, new LinearInterpolator1D());
  private static final PriceIndexCurveSimple PRICE_INDEX_CURVE = new PriceIndexCurveSimple(CURVE);

  @Test
  public void setterGetter() {
    MARKET_1.setCurve(Currency.EUR, CURVE_40);
    MARKET_1.setCurve(Currency.USD, CURVE_30);
    assertEquals("Market: getter", CURVE_40, MARKET_1.getCurve(Currency.EUR));
    assertEquals("Market: getter", CURVE_30, MARKET_1.getCurve(Currency.USD));
    MARKET_1.setCurve(EURIBOR_3M, CURVE_45);
    MARKET_1.setCurve(EURIBOR_6M, CURVE_50);
    MARKET_1.setCurve(USDLIBOR_3M, CURVE_35);
    assertEquals("Market: getter", CURVE_45, MARKET_1.getCurve(EURIBOR_3M));
    assertEquals("Market: getter", CURVE_50, MARKET_1.getCurve(EURIBOR_6M));
    assertEquals("Market: getter", CURVE_35, MARKET_1.getCurve(USDLIBOR_3M));
    MARKET_1.setCurve(PRICE_INDEX_EUR, PRICE_INDEX_CURVE);
    assertEquals("Market: getter", PRICE_INDEX_CURVE, MARKET_1.getCurve(PRICE_INDEX_EUR));
  }

}
