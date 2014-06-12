/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.generic;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.testng.annotations.Test;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ZSpreadCalculatorTest {
  private static final String CURVE_NAME = "Discounting";
  private static final PresentValueDiscountingCalculator PV_CALCULATOR = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVS_CALCULATOR = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final double[] T;
  private static final double[] R1;
  private static final double[] R2;
  private static final double[] R3;
  private static final double[] R4;
  private static final MulticurveProviderDiscount CONSTANT_CURVES;
  private static final MulticurveProviderDiscount MULTI_CURVES;
  private static final YieldCurve EUR_DISCOUNTING;
  private static final Annuity<CouponFixed> PAYMENTS;
  private static final double YIELD = 0.04;
  private static final Currency CUR = Currency.EUR;
  private static final ZSpreadCalculator<MulticurveProviderInterface> CALCULATOR = new ZSpreadCalculator<>(
      PV_CALCULATOR, PVS_CALCULATOR);
  private static final Interpolator1D INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);

  static {
    int n = 5;
    final CouponFixed[] rateAtYield = new CouponFixed[n];
    EUR_DISCOUNTING = YieldCurve.from(ConstantDoublesCurve.from(YIELD, CURVE_NAME));
    final Map<Currency, YieldAndDiscountCurve> constantDiscounting = Collections.<Currency, YieldAndDiscountCurve>singletonMap(Currency.EUR, EUR_DISCOUNTING);
    final Map<IborIndex, YieldAndDiscountCurve> emptyForwardIbor = Collections.emptyMap();
    final Map<IndexON, YieldAndDiscountCurve> emptyForwardON = Collections.emptyMap();
    final FXMatrix fxMatrix = new FXMatrix();
    fxMatrix.addCurrency(Currency.EUR, Currency.USD, 1.2);
    CONSTANT_CURVES = new MulticurveProviderDiscount(constantDiscounting, emptyForwardIbor, emptyForwardON, fxMatrix);
    for (int i = 0; i < n; i++) {
      rateAtYield[i] = new CouponFixed(CUR, 0.5 * (i + 1), 0.5, YIELD);
    }
    n = 10;
    T = new double[n];
    R1 = new double[n];
    R2 = new double[n];
    R3 = new double[n];
    R4 = new double[n];
    final Random random = new Random(12983);
    for (int i = 0; i < n; i++) {
      final double time = 0.5 * (i + 1);
      T[i] = time + Math.max(0, (0.5 - random.nextDouble()) / 10);
      R1[i] = 0.02 + time * random.nextGaussian() / 10;
      R2[i] = 0.03 + time * random.nextGaussian() / 10;
      R3[i] = 0.04 + time * random.nextGaussian() / 10;
      R4[i] = 0.04 + time * random.nextGaussian() / 10;
    }
    final Map<Currency, YieldAndDiscountCurve> discounting = new HashMap<>();
    discounting.put(Currency.EUR, YieldCurve.from(InterpolatedDoublesCurve.from(T, R1, INTERPOLATOR)));
    discounting.put(Currency.USD, YieldCurve.from(InterpolatedDoublesCurve.from(T, R2, INTERPOLATOR)));
    final Map<IborIndex, YieldAndDiscountCurve> forwardIbor = new HashMap<>();
    forwardIbor.put(new IborIndex(Currency.EUR, Period.ofMonths(6), n, DayCounts.ACT_360,
        BusinessDayConventions.NONE, false, "Ibor"), YieldCurve.from(InterpolatedDoublesCurve.from(T, R3, INTERPOLATOR)));
    final Map<IndexON, YieldAndDiscountCurve> forwardON = new HashMap<>();
    forwardON.put(new IndexON("ON", Currency.EUR, DayCounts.ACT_360, 0), YieldCurve.from(InterpolatedDoublesCurve.from(T, R4, INTERPOLATOR)));
    MULTI_CURVES = new MulticurveProviderDiscount(discounting, forwardIbor, forwardON, fxMatrix);
    PAYMENTS = new Annuity<>(rateAtYield);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPVCalculator() {
    new ZSpreadCalculator<>(null, PVS_CALCULATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPVSCalculator() {
    new ZSpreadCalculator<>(PV_CALCULATOR, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAnnuity1() {
    CALCULATOR.calculatePriceForZSpread(null, MULTI_CURVES, 0.04);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAnnuity2() {
    CALCULATOR.calculatePriceSensitivityToCurve(null, MULTI_CURVES, 0.04);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAnnuity3() {
    CALCULATOR.calculatePriceSensitivityToZSpread(null, MULTI_CURVES, 0.04);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAnnuity4() {
    CALCULATOR.calculateZSpread(null, MULTI_CURVES, 0.04);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAnnuity5() {
    CALCULATOR.calculateZSpreadSensitivityToCurve(null, MULTI_CURVES, 0.04);
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
    double price = 0;
    for (int i = 0; i < 5; i++) {
      price += EUR_DISCOUNTING.getDiscountFactor(0.5 * (i + 1));
    }
    price *= YIELD / 2;
    assertEquals(0, CALCULATOR.calculateZSpread(PAYMENTS, CONSTANT_CURVES, price), 1e-12);
    assertEquals(CALCULATOR.calculatePriceForZSpread(PAYMENTS, CONSTANT_CURVES, 0), price, 1e-12);
  }

  @Test
  public void testZSpread() {
    final double price = PAYMENTS.accept(PV_CALCULATOR, CONSTANT_CURVES).getAmount(Currency.EUR);
    final double zSpread = CALCULATOR.calculateZSpread(PAYMENTS, MULTI_CURVES, price);
    final double[] rSpread = new double[R1.length];
    for (int i = 0; i < rSpread.length; i++) {
      rSpread[i] = R1[i] + zSpread;
    }
    final MulticurveProviderDiscount multicurves = new MulticurveProviderDiscount(MULTI_CURVES.copy());
    multicurves.replaceCurve(Currency.EUR, YieldCurve.from(InterpolatedDoublesCurve.from(T, rSpread, INTERPOLATOR)));
    assertEquals(price, PAYMENTS.accept(PV_CALCULATOR, multicurves).getAmount(Currency.EUR), 1e-12);
    assertEquals(price, CALCULATOR.calculatePriceForZSpread(PAYMENTS, MULTI_CURVES, zSpread), 1e-12);
  }

  @Test
  public void testPriceSensitivityToZSpread() {
    final double price = PAYMENTS.accept(PV_CALCULATOR, CONSTANT_CURVES).getAmount(Currency.EUR);
    final double zSpread = CALCULATOR.calculateZSpread(PAYMENTS, MULTI_CURVES, price);
    final double eps = 1e-3;
    final double[] rSpread = new double[R1.length];
    for (int i = 0; i < rSpread.length; i++) {
      rSpread[i] = R1[i] + zSpread + eps;
    }
    final MulticurveProviderDiscount multicurves = new MulticurveProviderDiscount(MULTI_CURVES.copy());
    multicurves.replaceCurve(Currency.EUR, YieldCurve.from(InterpolatedDoublesCurve.from(T, rSpread, INTERPOLATOR)));
    final double newPrice = PAYMENTS.accept(PV_CALCULATOR, multicurves).getAmount(Currency.EUR);
    assertEquals((newPrice - price) / eps, CALCULATOR.calculatePriceSensitivityToZSpread(PAYMENTS, MULTI_CURVES, zSpread), eps);
  }

  @Test
  public void testSensitivities() {
    double zSpread = 0.06;
    final double dPdZ = CALCULATOR.calculatePriceSensitivityToZSpread(PAYMENTS, CONSTANT_CURVES, zSpread);
    final Map<String, List<DoublesPair>> dZdC = CALCULATOR.calculateZSpreadSensitivityToCurve(PAYMENTS, CONSTANT_CURVES, zSpread);
    Map<String, List<DoublesPair>> dPdC = CALCULATOR.calculatePriceSensitivityToCurve(PAYMENTS, CONSTANT_CURVES, zSpread);
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
        assertEquals(pairs1.get(i).first, pairs2.get(i).first, 1e-15);
        assertEquals(-pairs2.get(i).second / pairs1.get(i).second, dPdZ, 1e-15);
      }
    }
    zSpread = 0.0;
    dPdC = CALCULATOR.calculatePriceSensitivityToCurve(PAYMENTS, CONSTANT_CURVES, zSpread);
    final MultipleCurrencyMulticurveSensitivity mcms = PAYMENTS.accept(PVS_CALCULATOR, CONSTANT_CURVES);
    assertEquals(mcms.getCurrencies().size(), 1);
    final Map<String, List<DoublesPair>> pvSensitivity = mcms.getSensitivity(CUR).getYieldDiscountingSensitivities();
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
        assertEquals(pairs1.get(i).first, pairs2.get(i).first, 1e-15);
        assertEquals(pairs2.get(i).second, pairs1.get(i).second, 1e-15);
      }
    }
  }

  @Test
  public void testZSpreadSensitivityToCurve() {

  }

  @Test
  public void testPriceSensitivityToCurve() {

  }
}
