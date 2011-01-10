/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import com.opengamma.financial.interestrate.PresentValueSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.FixedCouponPayment;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class ZSpreadCalculatorTest {
  private static final ZSpreadCalculator CALCULATOR = ZSpreadCalculator.getInstance();
  private static final GenericAnnuity<FixedCouponPayment> PAYMENTS;
  private static final YieldCurveBundle CURVES;
  private static final String CURVE_NAME = "A";
  private static final double YIELD = 0.04;

  static {
    final int n = 5;
    final FixedCouponPayment[] rateAtYield = new FixedCouponPayment[n];
    CURVES = new YieldCurveBundle(new String[] {CURVE_NAME}, new YieldCurve[] {new YieldCurve(ConstantDoublesCurve.from(YIELD))});
    for (int i = 0; i < n; i++) {
      rateAtYield[i] = new FixedCouponPayment(0.5 * (i + 1), 0.5, YIELD, CURVE_NAME);
    }
    PAYMENTS = new GenericAnnuity<FixedCouponPayment>(rateAtYield);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullAnnuity1() {
    CALCULATOR.calculatePriceForZSpread(null, CURVES, 0.04);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullAnnuity2() {
    CALCULATOR.calculatePriceSensitivityToCurve(null, CURVES, 0.04);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullAnnuity3() {
    CALCULATOR.calculatePriceSensitivityToZSpread(null, CURVES, 0.04);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullAnnuity4() {
    CALCULATOR.calculateZSpread(null, CURVES, 0.04);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullAnnuity5() {
    CALCULATOR.calculateZSpreadSensitivityToCurve(null, CURVES, 0.04);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurves1() {
    CALCULATOR.calculatePriceForZSpread(PAYMENTS, null, 0.04);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurves2() {
    CALCULATOR.calculatePriceSensitivityToCurve(PAYMENTS, null, 0.04);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurves3() {
    CALCULATOR.calculatePriceSensitivityToZSpread(PAYMENTS, null, 0.04);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurves4() {
    CALCULATOR.calculateZSpread(PAYMENTS, null, 0.04);
  }

  @Test(expected = IllegalArgumentException.class)
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
    final Map<String, List<DoublesPair>> pvSensitivity = PresentValueSensitivityCalculator.getInstance().visit(PAYMENTS, CURVES);
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
