/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.annuity;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class ZSpreadCalculatorTest {
  private static final ZSpreadCalculator CALCULATOR = ZSpreadCalculator.getInstance();
  private static final Annuity<CouponFixed> PAYMENTS;
  private static final YieldCurveBundle CURVES;
  private static final String CURVE_NAME = "A";
  private static final double YIELD = 0.04;
  private static final Currency CUR = Currency.EUR;

  static {
    final int n = 5;
    final CouponFixed[] rateAtYield = new CouponFixed[n];
    CURVES = new YieldCurveBundle(new String[] {CURVE_NAME }, new YieldCurve[] {YieldCurve.from(ConstantDoublesCurve.from(YIELD)) });
    for (int i = 0; i < n; i++) {
      rateAtYield[i] = new CouponFixed(CUR, 0.5 * (i + 1), CURVE_NAME, 0.5, YIELD);
    }
    PAYMENTS = new Annuity<>(rateAtYield);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAnnuity1() {
    CALCULATOR.calculatePriceForZSpread(null, CURVES, 0.04);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAnnuity2() {
    CALCULATOR.calculatePriceSensitivityToCurve(null, CURVES, 0.04);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAnnuity3() {
    CALCULATOR.calculatePriceSensitivityToZSpread(null, CURVES, 0.04);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAnnuity4() {
    CALCULATOR.calculateZSpread(null, CURVES, 0.04);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAnnuity5() {
    CALCULATOR.calculateZSpreadSensitivityToCurve(null, CURVES, 0.04);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurves1() {
    CALCULATOR.calculatePriceForZSpread(PAYMENTS, null, 0.04);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurves2() {
    CALCULATOR.calculatePriceSensitivityToCurve(PAYMENTS, null, 0.04);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurves3() {
    CALCULATOR.calculatePriceSensitivityToZSpread(PAYMENTS, null, 0.04);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurves4() {
    CALCULATOR.calculateZSpread(PAYMENTS, null, 0.04);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurves5() {
    CALCULATOR.calculateZSpreadSensitivityToCurve(PAYMENTS, null, 0.04);
  }

  @Test
  public void testZeroSpread() {
    final YieldAndDiscountCurve curve = CURVES.getCurve(CURVE_NAME);
    double price = 0;
    for (int i = 0; i < 5; i++) {
      price += curve.getDiscountFactor(0.5 * (i + 1));
    }
    price *= YIELD / 2;
    assertEquals(0, CALCULATOR.calculateZSpread(PAYMENTS, CURVES, price), 1e-12);
    assertEquals(CALCULATOR.calculatePriceForZSpread(PAYMENTS, CURVES, 0), price, 1e-12);
  }

  @Test
  public void testSensitivities() {
    double zSpread = 0.06;
    final double dPdZ = CALCULATOR.calculatePriceSensitivityToZSpread(PAYMENTS, CURVES, zSpread);
    final Map<String, List<DoublesPair>> dZdC = CALCULATOR.calculateZSpreadSensitivityToCurve(PAYMENTS, CURVES, zSpread);
    Map<String, List<DoublesPair>> dPdC = CALCULATOR.calculatePriceSensitivityToCurve(PAYMENTS, CURVES, zSpread);
    assertEquals(dZdC.size(), dPdC.size());
    Iterator<Entry<String, List<DoublesPair>>> iter1 = dZdC.entrySet().iterator();
    Iterator<Entry<String, List<DoublesPair>>> iter2 = dPdC.entrySet().iterator();
    while (iter1.hasNext()) {
      final Entry<String, List<DoublesPair>> e1 = iter1.next();
      final Entry<String, List<DoublesPair>> e2 = iter2.next();
      assertEquals(e1.getKey(), CURVE_NAME);
      assertEquals(e2.getKey(), CURVE_NAME);
      final List<DoublesPair> pairs1 = e1.getValue();
      final List<DoublesPair> pairs2 = e2.getValue();
      assertEquals(pairs1.size(), 5);
      assertEquals(pairs2.size(), 5);
      for (int i = 0; i < 5; i++) {
        assertEquals(pairs1.get(i).first, 0.5 * (i + 1), 1e-15);
        assertEquals(pairs2.get(i).first, 0.5 * (i + 1), 1e-15);
        assertEquals(-pairs2.get(i).second / pairs1.get(i).second, dPdZ, 1e-15);
      }
    }
    zSpread = 0.0;
    dPdC = CALCULATOR.calculatePriceSensitivityToCurve(PAYMENTS, CURVES, zSpread);
    final Map<String, List<DoublesPair>> pvSensitivity = PAYMENTS.accept(PresentValueCurveSensitivityCalculator.getInstance(), CURVES);
    assertEquals(pvSensitivity.size(), dPdC.size());
    iter1 = dPdC.entrySet().iterator();
    iter2 = pvSensitivity.entrySet().iterator();
    while (iter1.hasNext()) {
      final Entry<String, List<DoublesPair>> e1 = iter1.next();
      final Entry<String, List<DoublesPair>> e2 = iter2.next();
      assertEquals(e1.getKey(), CURVE_NAME);
      assertEquals(e2.getKey(), CURVE_NAME);
      final List<DoublesPair> pairs1 = e1.getValue();
      final List<DoublesPair> pairs2 = e2.getValue();
      assertEquals(pairs1.size(), 5);
      assertEquals(pairs2.size(), 5);
      for (int i = 0; i < 5; i++) {
        assertEquals(pairs1.get(i).first, 0.5 * (i + 1), 1e-15);
        assertEquals(pairs2.get(i).first, 0.5 * (i + 1), 1e-15);
        assertEquals(pairs2.get(i).second, pairs1.get(i).second, 1e-15);
      }
    }
  }
}
