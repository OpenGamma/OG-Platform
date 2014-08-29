/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the zero-coupon rate cross-gamma calculator for single curve.
 */
@Test(groups = TestGroup.UNIT)
public class CrossGammaSingleCurveCalculatorTest {

  private static final Calendar NYC = MulticurveProviderDiscountDataSets.getUSDCalendar();
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  /** Instrument description */
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  private static final Period SWAP_TENOR = Period.ofYears(10);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2012, 5, 17);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE_FIXED = 0.025;
  private static final SwapFixedIborDefinition SWAP_FIXED_IBOR_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, USD6MLIBOR3M, NOTIONAL, RATE_FIXED, true);
  /** Calculators */
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final CrossGammaSingleCurveCalculator CGC = new CrossGammaSingleCurveCalculator(PVCSDC);
  /** Constants */
  private static final double SHIFT = 1.0E-4;
  private static final double TOLERANCE_PV_GAMMA = 2.0E+0;
  private static final double TOLERANCE_PV_GAMMA_RELATIF = 5.0E-4;

  @Test
  public void crossGamma() {
    MulticurveProviderDiscount singleCurve = MulticurveProviderDiscountDataSets.createSingleCurveUsd();
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    final SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate);
    String name = singleCurve.getAllNames().iterator().next();
    Currency ccy = singleCurve.getCurrencyForName(name);
    YieldAndDiscountCurve curve = singleCurve.getCurve(name);
    ArgumentChecker.isTrue(curve instanceof YieldCurve, "curve should be YieldCurve");
    YieldCurve yieldCurve = (YieldCurve) curve;
    ArgumentChecker.isTrue(yieldCurve.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
    InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) yieldCurve.getCurve();
    double[] y = interpolatedCurve.getYDataAsPrimitive();
    double[] x = interpolatedCurve.getXDataAsPrimitive();
    int nbNode = y.length;
    double[][] gammaComputed = CGC.calculateCrossGamma(swap, singleCurve).getData();
    double[][] gammaExpected = new double[nbNode][nbNode];
    for (int i = 0; i < nbNode; i++) {
      for (int j = 0; j < nbNode; j++) {
        double[][] pv = new double[2][2];
        for (int pmi = 0; pmi < 2; pmi++) {
          for (int pmj = 0; pmj < 2; pmj++) {
            final double[] yieldBumpedPP = y.clone();
            yieldBumpedPP[i] += ((pmi == 0) ? SHIFT : -SHIFT);
            yieldBumpedPP[j] += ((pmj == 0) ? SHIFT : -SHIFT);
            final YieldAndDiscountCurve curveBumped = new YieldCurve(name,
                new InterpolatedDoublesCurve(x, yieldBumpedPP, interpolatedCurve.getInterpolator(), true));
            MulticurveProviderDiscount providerBumped = new MulticurveProviderDiscount();
            for (Currency loopccy : singleCurve.getCurrencies()) {
              providerBumped.setCurve(loopccy, curveBumped);
            }
            for (IborIndex loopibor : singleCurve.getIndexesIbor()) {
              providerBumped.setCurve(loopibor, curveBumped);
            }
            pv[pmi][pmj] = swap.accept(PVDC, providerBumped).getAmount(ccy);
          }
        }
        gammaExpected[i][j] = (pv[0][0] - pv[1][0] - pv[0][1] + pv[1][1]) / (2 * SHIFT * 2 * SHIFT);
      }
    }
    for (int i = 0; i < nbNode; i++) {
      for (int j = 0; j < nbNode; j++) {
        if (Math.abs(gammaExpected[i][j]) > 1 || Math.abs(gammaComputed[i][j]) > 1) { // Check only the meaningful numbers
          assertTrue("CrossGammaSingleCurveCalculator - " + i + " - " + j + " / " + gammaExpected[i][j] + " - " + gammaComputed[i][j],
              (Math.abs(gammaExpected[i][j] / gammaComputed[i][j] - 1) < TOLERANCE_PV_GAMMA_RELATIF) || // If relative difference is small enough
                  (Math.abs(gammaExpected[i][j] - gammaComputed[i][j]) < TOLERANCE_PV_GAMMA)); // If absolute difference is small enough
        }
      }
    }
  }

}
