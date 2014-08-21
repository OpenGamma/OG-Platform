/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.tutorial.analysis.swap;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.datasets.CalendarTarget;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.provider.calculator.discounting.CrossGammaMultiCurveCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.CrossGammaSingleCurveCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.analytics.tutorial.datasets.AnalysisMarketDataEURJun13Sets;
import com.opengamma.financial.convention.calendar.ExceptionCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Analysis of cross-gamma to zero-coupon and market rates.
 */
public class SwapGammaMultiCurveProfitEURAnalysis {

  private static final ExceptionCalendar TARGET = new CalendarTarget("TARGET");

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2013, 6, 13);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, 2, TARGET);

  private static final GeneratorSwapFixedIborMaster MASTER_SWAP_FIXED_IBOR = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR3M = MASTER_SWAP_FIXED_IBOR.getGenerator("EUR1YEURIBOR3M", TARGET);
  private static final IndexIborMaster MASTER_IBOR_INDEX = IndexIborMaster.getInstance();
  private static final IborIndex EURIBOR3M = MASTER_IBOR_INDEX.getIndex("EURIBOR3M");
  private static final Currency EUR = EURIBOR3M.getCurrency();

  private static final Period TENOR_START = Period.ofMonths(150);
  private static final Period TENOR_SWAP = Period.ofYears(5);
  private static final boolean IS_PAYER = true;

  private static final double NOTIONAL = 1.0E6; // 1m
  private static final ZonedDateTime START_DATE = ScheduleCalculator.getAdjustedDate(SPOT_DATE, TENOR_START, EURIBOR3M, TARGET);
  private static final SwapFixedIborDefinition SWAP_EUR_DEFINITION = SwapFixedIborDefinition.from(START_DATE, TENOR_SWAP, EUR1YEURIBOR3M, NOTIONAL, 0.02, IS_PAYER);
  private static final SwapFixedCoupon<Coupon> SWAP_EUR = SWAP_EUR_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final CrossGammaMultiCurveCalculator CGMCC = new CrossGammaMultiCurveCalculator(PVCSDC);
  private static final CrossGammaSingleCurveCalculator CGSCC = new CrossGammaSingleCurveCalculator(PVCSDC);

  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_PAIR_0 = AnalysisMarketDataEURJun13Sets.getMulticurveEUR();
  private static final MulticurveProviderDiscount MULTICURVE = MULTICURVE_PAIR_0.getFirst();
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> SINGLECURVE_PAIR_0 = AnalysisMarketDataEURJun13Sets.getSingleCurveEUR();
  private static final MulticurveProviderDiscount SINGLECURVE = SINGLECURVE_PAIR_0.getFirst();

  private static final String[] CURVE_NAME = new String[2];
  static {
    CURVE_NAME[0] = MULTICURVE_PAIR_0.getFirst().getName(EUR);
    CURVE_NAME[1] = MULTICURVE_PAIR_0.getFirst().getName(EURIBOR3M);
  }
  private static final double BP1 = 1.0E-4;
  private static final double SHIFT = 1 * BP1;

  private static final OGMatrixAlgebra ALGEBRA = new OGMatrixAlgebra();

  private static final CrossGammaSingleCurveCalculator GC = new CrossGammaSingleCurveCalculator(BP1, PVCSDC);

  private static final int NB_NODE_EUR = AnalysisMarketDataEURJun13Sets.getCurveEURNumberNodeForward();
  private static final DoubleMatrix2D GAMMA_EUR = GC.calculateCrossGamma(SWAP_EUR, SINGLECURVE_PAIR_0.getFirst());
  private static final double[] GAMMA_SUM_EUR = new double[GAMMA_EUR.getNumberOfColumns()];
  static {
    for (int loopcol = 0; loopcol < GAMMA_EUR.getNumberOfColumns(); loopcol++) {
      for (int looprow = 0; looprow < GAMMA_EUR.getNumberOfRows(); looprow++) {
        GAMMA_SUM_EUR[loopcol] += GAMMA_EUR.getEntry(looprow, loopcol);
      }
    }
  }

  @Test(enabled = false)
  public void crossGammaZeroSingleExport() {
    try {
      final FileWriter writer = new FileWriter("swap-x-gamma-single.csv");
      for (int loopnodei = 0; loopnodei < NB_NODE_EUR; loopnodei++) {
        String line = "";
        for (int loopnode2 = 0; loopnode2 < NB_NODE_EUR; loopnode2++) {
          line = line + "," + GAMMA_EUR.getEntry(loopnodei, loopnode2);
        }
        writer.append(line + "0 \n");
      }
      writer.flush();
      writer.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unused")
  @Test(enabled = false)
  public void crossGammaDiagonalComp() {
    double[] marketMvtArray = new double[NB_NODE_EUR];
    Arrays.fill(marketMvtArray, 0.0010);
    marketMvtArray[5] = -0.0020;
    marketMvtArray[7] = -0.0020;
    marketMvtArray[9] = -0.0020;
    marketMvtArray[11] = -0.0020;
    DoubleMatrix2D marketMvt = new DoubleMatrix2D(new double[][] {marketMvtArray });

    double plTotal = (Double) ALGEBRA.multiply(ALGEBRA.multiply(marketMvt, GAMMA_EUR), ALGEBRA.getTranspose(marketMvt)).getEntry(0, 0);
    double plDiag = 0;
    for (int loopdiag = 0; loopdiag < NB_NODE_EUR; loopdiag++) {
      plDiag += GAMMA_EUR.getEntry(loopdiag, loopdiag) * marketMvtArray[loopdiag] * marketMvtArray[loopdiag];
    }
    double plCol = 0;
    for (int loopcol = 0; loopcol < NB_NODE_EUR; loopcol++) {
      plCol += GAMMA_SUM_EUR[loopcol] * marketMvtArray[loopcol] * marketMvtArray[loopcol];
    }
  }

  @SuppressWarnings("unused")
  @Test(enabled = false)
  public void crossGammaMulticurveIntraCurve() {
    HashMap<String, DoubleMatrix2D> crossGammaIntra = CGMCC.calculateCrossGammaIntraCurve(SWAP_EUR, MULTICURVE);
    HashMap<String, DoubleMatrix2D> crossGammaSingle1 = CGMCC.calculateCrossGammaIntraCurve(SWAP_EUR, SINGLECURVE);
    DoubleMatrix2D crossGammaSingle2 = CGSCC.calculateCrossGamma(SWAP_EUR, SINGLECURVE);
    for (String name : crossGammaIntra.keySet()) {
      exportMatrix(crossGammaIntra.get(name).getData(), "cross-gamma-eur-" + name + ".csv");
    }
    int t = 0;
  }

  @Test(enabled = false)
  public void crossGammaZeroMulti() {
    MulticurveProviderDiscount multicurve = MULTICURVE_PAIR_0.getFirst();
    MultipleCurrencyParameterSensitivity ps0 = PSC.calculateSensitivity(SWAP_EUR, multicurve);
    DoubleMatrix1D[] ps0Mat = new DoubleMatrix1D[2];
    for (int i = 0; i < 2; i++) {
      ps0Mat[i] = ps0.getSensitivity(CURVE_NAME[i], EUR);
    }
    DoubleMatrix1D[][][] psShift = new DoubleMatrix1D[2][2][];
    DoubleMatrix1D[][][] gamma = new DoubleMatrix1D[2][2][]; // Curve shifted, curve impacted
    int[] nbNode = new int[2];
    nbNode[0] = AnalysisMarketDataEURJun13Sets.getCurveEURNumberNodeDiscounting();
    nbNode[1] = AnalysisMarketDataEURJun13Sets.getCurveEURNumberNodeForward();
    MultipleCurrencyParameterSensitivity[] psShiftDsc = new MultipleCurrencyParameterSensitivity[nbNode[0]];
    for (int i = 0; i < 2; i++) {
      psShift[0][i] = new DoubleMatrix1D[nbNode[0]];
      gamma[0][i] = new DoubleMatrix1D[nbNode[0]];
    }
    for (int loopdsc = 0; loopdsc < nbNode[0]; loopdsc++) {
      MulticurveProviderDiscount multicurveShift = AnalysisMarketDataEURJun13Sets.getMulticurvesEURShiftParameterPoint(SHIFT, loopdsc, true);
      psShiftDsc[loopdsc] = PSC.calculateSensitivity(SWAP_EUR, multicurveShift);
      for (int i = 0; i < 2; i++) {
        psShift[0][i][loopdsc] = psShiftDsc[loopdsc].getSensitivity(CURVE_NAME[i], EUR);
        gamma[0][i][loopdsc] = (DoubleMatrix1D) ALGEBRA.add(psShift[0][i][loopdsc], ALGEBRA.scale(ps0Mat[i], -1.0));
      }
    }
    for (int i = 0; i < 2; i++) {
      psShift[1][i] = new DoubleMatrix1D[nbNode[1]];
      gamma[1][i] = new DoubleMatrix1D[nbNode[1]];
    }
    MultipleCurrencyParameterSensitivity[] psShiftFwd = new MultipleCurrencyParameterSensitivity[nbNode[1]];
    for (int loopfwd = 0; loopfwd < nbNode[1]; loopfwd++) {
      MulticurveProviderDiscount multicurveShift = AnalysisMarketDataEURJun13Sets.getMulticurvesEURShiftParameterPoint(SHIFT, loopfwd, false);
      psShiftFwd[loopfwd] = PSC.calculateSensitivity(SWAP_EUR, multicurveShift);
      for (int i = 0; i < 2; i++) {
        psShift[1][i][loopfwd] = psShiftFwd[loopfwd].getSensitivity(CURVE_NAME[i], EUR);
        gamma[1][i][loopfwd] = (DoubleMatrix1D) ALGEBRA.add(psShift[1][i][loopfwd], ALGEBRA.scale(ps0Mat[i], -1.0));
      }
    }

    try {
      final FileWriter writer = new FileWriter("swap-x-gamma-multicurve.csv");
      for (int i = 0; i < 2; i++) {
        for (int loopnodei = 0; loopnodei < nbNode[i]; loopnodei++) {
          String line = "";
          for (int j = 0; j < 2; j++) {
            for (int loopnode2 = 0; loopnode2 < nbNode[j]; loopnode2++) {
              line = line + "," + gamma[i][j][loopnodei].getEntry(loopnode2);
            }
          }
          writer.append(line + "0 \n");
        }
      }
      writer.flush();
      writer.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private void exportMatrix(double[][] matrix, String fileName) {
    try {
      final FileWriter writer = new FileWriter("swap-x-gamma-multicurve.csv");
      for (int i = 0; i < 2; i++) {
        for (int loop1 = 0; loop1 < matrix.length; loop1++) {
          String line = "";
          for (int j = 0; j < 2; j++) {
            for (int loop2 = 0; loop2 < matrix[loop1].length; loop2++) {
              line = line + "," + matrix[loop1][loop2];
            }
          }
          writer.append(line + "0 \n");
        }
      }
      writer.flush();
      writer.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unused")
  @Test(enabled = false)
  public void performance() {
    long startTime, endTime;
    final int nbTest = 1000;

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      MultipleCurrencyAmount pv = SWAP_EUR.accept(PVDC, MULTICURVE);
    }
    endTime = System.currentTimeMillis();
    System.out.println("CrossGammaMultiCurveCalculator - " + nbTest + " pv - 2 curves: " + (endTime - startTime) + " ms");
    // Performance note: PVD: 04-Aug-2014: On Mac Book Pro 2.6 GHz Intel Core i7: 20 ms for 1000 sets.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      MultipleCurrencyMulticurveSensitivity pvcs = SWAP_EUR.accept(PVCSDC, MULTICURVE);
    }
    endTime = System.currentTimeMillis();
    System.out.println("CrossGammaMultiCurveCalculator - " + nbTest + " pvcs - 2 curves: " + (endTime - startTime) + " ms");
    // Performance note: PVCSD: 04-Aug-2014: On Mac Book Pro 2.6 GHz Intel Core i7: 25 ms for 1000 sets.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      HashMap<String, DoubleMatrix2D> crossGammaIntra = CGMCC.calculateCrossGammaIntraCurve(SWAP_EUR, MULTICURVE);
    }
    endTime = System.currentTimeMillis();
    System.out.println("CrossGammaMultiCurveCalculator - " + nbTest + " intro-curve x-gamma - 2 curves: " + (endTime - startTime) + " ms");
    // Performance note: Cross-gamma intra-curve 2 curves: 07-Nov-12: On Mac Book Pro 2.6 GHz Intel Core i7: 1450 ms for 1000 sets.

  }

}
