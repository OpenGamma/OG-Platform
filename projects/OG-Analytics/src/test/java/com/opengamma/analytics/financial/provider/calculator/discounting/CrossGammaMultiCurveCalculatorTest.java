/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import static org.testng.AssertJUnit.assertTrue;

import java.util.HashMap;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.tutorial.datasets.AnalysisMarketDataJPYSets;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Tests the zero-coupon rate cross-gamma calculator for multi-curve.
 */
public class CrossGammaMultiCurveCalculatorTest {

  private static final Calendar TYO = new MondayToFridayCalendar("TYO");
  private static final ZonedDateTime CALIBRATION_DATE = DateUtils.getUTCDate(2014, 8, 2);
  
  private static final GeneratorSwapFixedIborMaster GENERATOR_IRS_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapIborIborMaster GENERATOR_BS_MASTER = GeneratorSwapIborIborMaster.getInstance();
  /** Instrument description */
  private static final GeneratorSwapFixedIbor JPY6MLIBOR6M = GENERATOR_IRS_MASTER.getGenerator("JPY6MLIBOR6M", TYO);
  private static final GeneratorSwapIborIbor JPYLIBOR3MLIBOR6M = GENERATOR_BS_MASTER.getGenerator("JPYLIBOR3MLIBOR6M", TYO);
  private static final IborIndex JPYLIBOR6M = JPY6MLIBOR6M.getIborIndex();
  private static final Period SWAP_TENOR = Period.ofYears(10);
  private static final ZonedDateTime SETTLEMENT_DATE = 
      ScheduleCalculator.getAdjustedDate(CALIBRATION_DATE, JPYLIBOR6M.getSpotLag(), TYO);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE_FIXED = 0.025;
  private static final double SPREAD_BS = 0.0005;
  private static final SwapFixedIborDefinition SWAP_FIXED_IBOR_DEFINITION = 
      SwapFixedIborDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, JPY6MLIBOR6M, NOTIONAL, RATE_FIXED, true);
  private static final SwapIborIborDefinition SWAP_IBOR_IBOR_DEFINITION =
      JPYLIBOR3MLIBOR6M.generateInstrument(CALIBRATION_DATE, SPREAD_BS, NOTIONAL, new GeneratorAttributeIR(SWAP_TENOR));
  /** Calculators */
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final CrossGammaMultiCurveCalculator CGC = new CrossGammaMultiCurveCalculator(PVCSDC);
  /** Constants */
  private static final double SHIFT = 1.0E-4;
  private static final double TOLERANCE_PV_GAMMA = 2.0E+0;
  private static final double TOLERANCE_PV_GAMMA_RELATIF = 6.0E-4;

  @Test
  public void crossGammaIntraCurveIrs() {
    final SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(CALIBRATION_DATE);
    crossGammaIntraCurve(swap);
  }
  
  @Test
  public void crossGammaIntraCurveBs() {
    final Swap<Coupon, Coupon> swap = SWAP_IBOR_IBOR_DEFINITION.toDerivative(CALIBRATION_DATE);
    crossGammaIntraCurve(swap);
  }
 
  private void crossGammaIntraCurve(Swap<?,?> swap) {
    Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> multicurvePair = AnalysisMarketDataJPYSets.getMulticurveJPY();
    MulticurveProviderDiscount multicurve = multicurvePair.getFirst();
    HashMap<String, DoubleMatrix2D> gammaMap = CGC.calculateCrossGammaIntraCurve(swap, multicurve);
    Set<String> names = multicurve.getAllNames();
    for(String name: names) { // Start curves
      Set<Currency> ccys = multicurve.getCurrencies();
      ArgumentChecker.isTrue(ccys.size() == 1, "only one currency allowed for multi-curve gamma");
      Currency ccy = ccys.iterator().next();
      YieldAndDiscountCurve curve = multicurve.getCurve(name);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "curve should be YieldCurve");
      YieldCurve yieldCurve = (YieldCurve) curve;
      ArgumentChecker.isTrue(yieldCurve.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) yieldCurve.getCurve();
      double[] y = interpolatedCurve.getYDataAsPrimitive();
      double[] x = interpolatedCurve.getXDataAsPrimitive();
      int nbNode = y.length;
      double[][] gammaComputed = gammaMap.get(name).getData();
      double[][] gammaExpected = new double[nbNode][nbNode];
      for (int i = 0; i < nbNode; i++) { // Start node
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
              for (Currency loopccy : multicurve.getCurrencies()) {
                if (loopccy.equals(multicurve.getCurrencyForName(name))) {
                  providerBumped.setCurve(loopccy, curveBumped);
                } else {
                  providerBumped.setCurve(loopccy, multicurve.getCurve(loopccy));
                }
              }
              for (IborIndex loopibor : multicurve.getIndexesIbor()) {
                if (loopibor.equals(multicurve.getIborIndexForName(name))) {
                  providerBumped.setCurve(loopibor, curveBumped);
                } else {
                  providerBumped.setCurve(loopibor, multicurve.getCurve(loopibor));
                }
              }
              pv[pmi][pmj] = swap.accept(PVDC, providerBumped).getAmount(ccy);
            }
          }
          gammaExpected[i][j] = (pv[0][0] - pv[1][0] - pv[0][1] + pv[1][1]) / (2 * SHIFT * 2 * SHIFT);
        }
      } // End node
      for (int i = 0; i < nbNode; i++) { // Start assert
        for (int j = 0; j < nbNode; j++) {
          if (Math.abs(gammaExpected[i][j]) > 1 || Math.abs(gammaComputed[i][j]) > 1) { // Check only the meaningful numbers
            assertTrue("CrossGammaSingleCurveCalculator - " + i + " - " + j + " / " + gammaExpected[i][j] + " - " + gammaComputed[i][j],
                (Math.abs(gammaExpected[i][j] / gammaComputed[i][j] - 1) < TOLERANCE_PV_GAMMA_RELATIF) || // If relative difference is small enough
                    (Math.abs(gammaExpected[i][j] - gammaComputed[i][j]) < TOLERANCE_PV_GAMMA)); // If absolute difference is small enough
          }
        }
      } // End assert
    } // End curves
  }
  
}
