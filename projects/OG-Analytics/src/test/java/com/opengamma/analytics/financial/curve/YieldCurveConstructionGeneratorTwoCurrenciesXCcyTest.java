/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
* TODO: This is old code that has been commented. It should be removed at some point.
 */
@Test(groups = TestGroup.UNIT)
public class YieldCurveConstructionGeneratorTwoCurrenciesXCcyTest {

  // Present Value
  //  private static final PresentValueMCACalculator PV_CALCULATOR = PresentValueMCACalculator.getInstance();
  //  private static final PresentValueCurveSensitivityMCSCalculator PVCS_CALCULATOR = PresentValueCurveSensitivityMCSCalculator.getInstance();
  //  private static final Currency CCY_PV = YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.CCY1;
  //  private static final PresentValueConvertedCalculator PV_CONVERTED_CALCULATOR = new PresentValueConvertedCalculator(CCY_PV, PV_CALCULATOR);
  //  private static final PresentValueCurveSensitivityConvertedCalculator PVCS_CONVERTED_CALCULATOR = new PresentValueCurveSensitivityConvertedCalculator(CCY_PV, PVCS_CALCULATOR);
  //  // Par spread market quote
  //  private static final ParSpreadMarketQuoteCalculator PSMQ_CALCULATOR = ParSpreadMarketQuoteCalculator.getInstance();
  //  private static final ParSpreadMarketQuoteCurveSensitivityCalculator PSMQCS_CALCULATOR = ParSpreadMarketQuoteCurveSensitivityCalculator.getInstance();
  //  // Par Rate
  //  //  private static final ParSpreadRateCalculator PSR_CALCULATOR = ParSpreadRateCalculator.getInstance();
  //  //  private static final ParSpreadRateCurveSensitivityCalculator PSRS_CALCULATOR = ParSpreadRateCurveSensitivityCalculator.getInstance();
  //
  //  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2011, 9, 28);
  //
  //  private static List<InstrumentDefinition<?>> DEFINITIONS_DSC_1;
  //  private static List<InstrumentDefinition<?>> DEFINITIONS_FWD_1;
  //  private static List<InstrumentDefinition<?>> DEFINITIONS_DSC_2;
  //  private static List<InstrumentDefinition<?>> DEFINITIONS_FWD_2;
  //  private static Pair<YieldCurveBundle, DoubleMatrix2D> CURVES_PRESENT_VALUE_WITH_TODAY;
  //  private static Pair<YieldCurveBundle, DoubleMatrix2D> CURVES_PRESENT_VALUE_WITH_TODAY_2BLOCKS;
  //  private static Pair<YieldCurveBundle, DoubleMatrix2D> CURVES_PAR_SPREAD_MQ_WITH_TODAY;
  //  private static Pair<YieldCurveBundle, DoubleMatrix2D> CURVES_PAR_SPREAD_MQ_WITH_TODAY_2BLOCKS;
  //  private static Pair<YieldCurveBundle, DoubleMatrix2D> CURVES_PRESENT_VALUE_WITHOUT_TODAY;
  //  private static Pair<YieldCurveBundle, DoubleMatrix2D> CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY;
  //
  //  private static final double TOLERANCE_PV = 1.0E-10;
  //
  //  @BeforeSuite
  //  static void initClass() {
  //    DEFINITIONS_DSC_1 = YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.getDefinitions(YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.DSC_1_MARKET_QUOTES,
  //        YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.DSC_1_GENERATORS, YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.DSC_1_TENOR);
  //    DEFINITIONS_FWD_1 = YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.getDefinitions(YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.FWD_1_MARKET_QUOTES,
  //        YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.FWD_1_GENERATORS, YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.FWD_1_TENOR);
  //    DEFINITIONS_DSC_2 = YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.getDefinitions(YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.DSC_2_MARKET_QUOTES,
  //        YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.DSC_2_GENERATORS, YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.DSC_2_TENOR);
  //    DEFINITIONS_FWD_2 = YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.getDefinitions(YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.FWD_2_MARKET_QUOTES,
  //        YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.FWD_2_GENERATORS, YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.FWD_2_TENOR);
  //
  //    CURVES_PRESENT_VALUE_WITH_TODAY = YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.makeCurves(DEFINITIONS_DSC_1, DEFINITIONS_FWD_1, DEFINITIONS_DSC_2, DEFINITIONS_FWD_2,
  //        PV_CONVERTED_CALCULATOR, PVCS_CONVERTED_CALCULATOR, true, true);
  //    CURVES_PRESENT_VALUE_WITH_TODAY_2BLOCKS = YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.makeCurves(DEFINITIONS_DSC_1, DEFINITIONS_FWD_1, DEFINITIONS_DSC_2, DEFINITIONS_FWD_2,
  //        PV_CONVERTED_CALCULATOR, PVCS_CONVERTED_CALCULATOR, true, true);
  //    CURVES_PAR_SPREAD_MQ_WITH_TODAY = YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.makeCurves(DEFINITIONS_DSC_1, DEFINITIONS_FWD_1, DEFINITIONS_DSC_2, DEFINITIONS_FWD_2, PSMQ_CALCULATOR,
  //        PSMQCS_CALCULATOR, true, false);
  //    CURVES_PAR_SPREAD_MQ_WITH_TODAY_2BLOCKS = YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.makeCurves2Blocks(DEFINITIONS_DSC_1, DEFINITIONS_FWD_1, DEFINITIONS_DSC_2, DEFINITIONS_FWD_2,
  //        PSMQ_CALCULATOR, PSMQCS_CALCULATOR, true, false);
  //
  //    CURVES_PRESENT_VALUE_WITHOUT_TODAY = YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.makeCurves(DEFINITIONS_DSC_1, DEFINITIONS_FWD_1, DEFINITIONS_DSC_2, DEFINITIONS_FWD_2,
  //        PV_CONVERTED_CALCULATOR, PVCS_CONVERTED_CALCULATOR, false, true);
  //    //	    CURVES_PAR_SPREAD_RATE_WITHOUT_TODAY = YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.makeCurves(DEFINITIONS_DSC, DEFINITIONS_FWD_3M, PSR_CALCULATOR, PSRS_CALCULATOR, false);
  //    CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY = YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.makeCurves(DEFINITIONS_DSC_1, DEFINITIONS_FWD_1, DEFINITIONS_DSC_2, DEFINITIONS_FWD_2, PSMQ_CALCULATOR,
  //        PSMQCS_CALCULATOR, false, false);
  //  }
  //
  //  @Test
  //  public void curveConstructionTwoCurrenciesXCcy() {
  //    // Curve constructed with present value and today fixing
  //    curveConstructionTest(CURVES_PRESENT_VALUE_WITH_TODAY.getFirst(), true);
  //    // Curve constructed in 2 blocks with present value and today fixing
  //    curveConstructionTest(CURVES_PRESENT_VALUE_WITH_TODAY_2BLOCKS.getFirst(), true);
  //    // Curve constructed with par spread (market quote) and  today fixing
  //    curveConstructionTest(CURVES_PAR_SPREAD_MQ_WITH_TODAY.getFirst(), true);
  //    // Curve constructed in 2 blocks with par spread (market quote) and today fixing
  //    curveConstructionTest(CURVES_PAR_SPREAD_MQ_WITH_TODAY_2BLOCKS.getFirst(), true);
  //    final double[][] mqj1 = CURVES_PAR_SPREAD_MQ_WITH_TODAY.getSecond().getData();
  //    final double[][] mqj2 = CURVES_PAR_SPREAD_MQ_WITH_TODAY_2BLOCKS.getSecond().getData();
  //    final double[][] pvj1 = CURVES_PRESENT_VALUE_WITH_TODAY.getSecond().getData();
  //    final double[][] pvj2 = CURVES_PRESENT_VALUE_WITH_TODAY_2BLOCKS.getSecond().getData();
  //    for (int loop1 = 0; loop1 < mqj1.length; loop1++) {
  //      for (int loop2 = 0; loop2 < mqj1[0].length; loop2++) {
  //        assertEquals("Curve construction 2 blocks: Jacobian -" + loop1 + " - " + loop2, mqj1[loop1][loop2], mqj2[loop1][loop2], 1.0E-6);
  //        assertEquals("Curve construction 2 blocks: Jacobian -" + loop1 + " - " + loop2, pvj1[loop1][loop2], pvj2[loop1][loop2], 1.0E-6);
  //      }
  //    }
  //    // Curve constructed with present value and no today fixing
  //    curveConstructionTest(CURVES_PRESENT_VALUE_WITHOUT_TODAY.getFirst(), false);
  //    //	    // Curve constructed with par rate and no today fixing
  //    //	    curveConstructionTest(CURVES_PAR_SPREAD_RATE_WITHOUT_TODAY.getFirst(), false);
  //    // Curve constructed with par spread (market quote) and no today fixing
  //    curveConstructionTest(CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY.getFirst(), false);
  //  }
  //
  //  @SuppressWarnings({"unchecked" })
  //  public void curveConstructionTest(final YieldCurveBundle curves, final boolean withToday) {
  //    final int nbDsc1 = DEFINITIONS_DSC_1.size();
  //    final int nbFwd1 = DEFINITIONS_FWD_1.size();
  //    final int nbDsc2 = DEFINITIONS_DSC_2.size();
  //    final int nbFwd2 = DEFINITIONS_FWD_2.size();
  //    final double[] pv = new double[nbDsc1 + nbFwd1 + nbDsc2 + nbFwd2];
  //    final List<InstrumentDerivative> instruments = new ArrayList<InstrumentDerivative>();
  //    for (final InstrumentDefinition<?> instrument : DEFINITIONS_DSC_1) {
  //      InstrumentDerivative ird;
  //      if (instrument instanceof SwapFixedONDefinition) {
  //        final String[] curveNames = new String[] {YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.CURVE_NAME_DSC_1, YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.CURVE_NAME_DSC_1 };
  //        ird = ((SwapFixedONDefinition) instrument).toDerivative(NOW, withToday ? YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.TS_FIXED_OIS_USD_WITH_TODAY
  //            : YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.TS_FIXED_OIS_USD_WITHOUT_TODAY, curveNames);
  //      } else {
  //        final String[] curveNames = new String[] {YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.CURVE_NAME_DSC_1 };
  //        ird = instrument.toDerivative(NOW, curveNames);
  //      }
  //      instruments.add(ird);
  //    }
  //    for (final InstrumentDefinition<?> instrument : DEFINITIONS_FWD_1) {
  //      InstrumentDerivative ird;
  //      if (instrument instanceof SwapFixedIborDefinition) {
  //        final String[] curveNames = new String[] {YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.CURVE_NAME_DSC_1, YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.CURVE_NAME_FWD_1 };
  //        ird = ((SwapFixedIborDefinition) instrument).toDerivative(NOW, withToday ? YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.TS_FIXED_IBOR_USD3M_WITH_TODAY
  //            : YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.TS_FIXED_IBOR_USD3M_WITHOUT_TODAY, curveNames);
  //      } else {
  //        final String[] curveNames = new String[] {YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.CURVE_NAME_FWD_1 };
  //        ird = instrument.toDerivative(NOW, curveNames);
  //      }
  //      instruments.add(ird);
  //    }
  //    for (final InstrumentDefinition<?> instrument : DEFINITIONS_DSC_2) {
  //      InstrumentDerivative ird;
  //      if (instrument instanceof SwapXCcyIborIborDefinition) {
  //        final String[] curveNames = new String[] {YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.CURVE_NAME_DSC_2, YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.CURVE_NAME_FWD_2,
  //            YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.CURVE_NAME_DSC_1, YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.CURVE_NAME_FWD_1 };
  //        ird = ((SwapXCcyIborIborDefinition) instrument).toDerivative(NOW, withToday ? YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.TS_FIXED_IBOR_EURUSD3M_WITH_TODAY
  //            : YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.TS_FIXED_IBOR_EURUSD3M_WITHOUT_TODAY, curveNames);
  //      } else {
  //        if (instrument instanceof CashDefinition) {
  //          final String[] curveNames = new String[] {YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.CURVE_NAME_DSC_2 };
  //          ird = instrument.toDerivative(NOW, curveNames);
  //        } else {
  //          final String[] curveNames = new String[] {YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.CURVE_NAME_DSC_2, YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.CURVE_NAME_DSC_1 };
  //          ird = instrument.toDerivative(NOW, curveNames);
  //        }
  //      }
  //      instruments.add(ird);
  //    }
  //    for (final InstrumentDefinition<?> instrument : DEFINITIONS_FWD_2) {
  //      InstrumentDerivative ird;
  //      if (instrument instanceof SwapFixedIborDefinition) {
  //        final String[] curveNames = new String[] {YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.CURVE_NAME_DSC_2, YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.CURVE_NAME_FWD_2 };
  //        ird = ((SwapFixedIborDefinition) instrument).toDerivative(NOW, withToday ? YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.TS_FIXED_IBOR_EUR3M_WITH_TODAY
  //            : YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.TS_FIXED_IBOR_EUR3M_WITHOUT_TODAY, curveNames);
  //      } else {
  //        final String[] curveNames = new String[] {YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.CURVE_NAME_FWD_2 };
  //        ird = instrument.toDerivative(NOW, curveNames);
  //      }
  //      instruments.add(ird);
  //    }
  //    final String[] curveBundleName = curves.getAllNames().toArray(new String[0]);
  //    for (int loopdsc1 = 0; loopdsc1 < nbDsc1; loopdsc1++) {
  //      pv[loopdsc1] = curves.getFxRates().convert(instruments.get(loopdsc1).accept(PV_CALCULATOR, curves), curves.getCurveCurrency(curveBundleName[0])).getAmount();
  //      assertEquals("Curve construction: node dsc ccy1 - " + loopdsc1, 0, pv[loopdsc1], TOLERANCE_PV);
  //    }
  //    for (int loopfwd1 = 0; loopfwd1 < nbFwd1; loopfwd1++) {
  //      pv[loopfwd1] = curves.getFxRates().convert(instruments.get(loopfwd1).accept(PV_CALCULATOR, curves), curves.getCurveCurrency(curveBundleName[0])).getAmount();
  //      assertEquals("Curve construction: node fwd ccy1 - " + loopfwd1, 0, pv[loopfwd1], TOLERANCE_PV);
  //    }
  //    for (int loopdsc2 = 0; loopdsc2 < nbDsc2; loopdsc2++) {
  //      pv[nbDsc1 + nbFwd1 + loopdsc2] = curves.getFxRates().convert(instruments.get(nbDsc1 + nbFwd1 + loopdsc2).accept(PV_CALCULATOR, curves), curves.getCurveCurrency(curveBundleName[1])).getAmount();
  //      assertEquals("Curve construction: node dsc ccy2 - " + loopdsc2, 0, pv[nbDsc1 + nbFwd1 + loopdsc2], TOLERANCE_PV);
  //    }
  //    for (int loopfwd2 = 0; loopfwd2 < nbFwd2; loopfwd2++) {
  //      pv[nbDsc1 + nbFwd1 + nbDsc2 + loopfwd2] = curves.getFxRates()
  //          .convert(instruments.get(nbDsc1 + nbFwd1 + nbDsc2 + loopfwd2).accept(PV_CALCULATOR, curves), curves.getCurveCurrency(curveBundleName[1])).getAmount();
  //      assertEquals("Curve construction: node fwd ccy2 - " + loopfwd2, 0, pv[nbDsc1 + nbFwd1 + nbDsc2 + loopfwd2], TOLERANCE_PV);
  //    }
  //  }
  //
  //  @Test(enabled = false)
  //  public void performance() {
  //    long startTime, endTime;
  //    final int nbTest = 100;
  //    @SuppressWarnings("unused")
  //    Pair<YieldCurveBundle, DoubleMatrix2D> curvePresentValue;
  //    @SuppressWarnings("unused")
  //    Pair<YieldCurveBundle, DoubleMatrix2D> curveParSpreadMQ;
  //    @SuppressWarnings("unused")
  //    Pair<YieldCurveBundle, DoubleMatrix2D> curveParSpreadMQ2;
  //
  //    final int nbDsc1 = DEFINITIONS_DSC_1.size();
  //    final int nbFwd1 = DEFINITIONS_FWD_1.size();
  //    final int nbDsc2 = DEFINITIONS_DSC_2.size();
  //    final int nbFwd2 = DEFINITIONS_FWD_2.size();
  //
  //    startTime = System.currentTimeMillis();
  //    for (int looptest = 0; looptest < nbTest; looptest++) {
  //      curvePresentValue = YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.makeCurves(DEFINITIONS_DSC_1, DEFINITIONS_FWD_1, DEFINITIONS_DSC_2, DEFINITIONS_FWD_2, PV_CONVERTED_CALCULATOR,
  //          PVCS_CONVERTED_CALCULATOR, true, true);
  //    }
  //    endTime = System.currentTimeMillis();
  //    System.out.println(nbTest + " curve construction TwoCcyXCcy (with present value and " + (nbDsc1 + nbFwd1 + nbDsc2 + nbFwd2) + " instruments): " + (endTime - startTime) + " ms");
  //    // Performance note: curve construction (with par spread (market quote), 4 curves - 2 ccy and 40 instruments): 16-Jul-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 1150 ms for 100 bundles.
  //
  //    startTime = System.currentTimeMillis();
  //    for (int looptest = 0; looptest < nbTest; looptest++) {
  //      curveParSpreadMQ = YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.makeCurves(DEFINITIONS_DSC_1, DEFINITIONS_FWD_1, DEFINITIONS_DSC_2, DEFINITIONS_FWD_2, PSMQ_CALCULATOR,
  //          PSMQCS_CALCULATOR, true, false);
  //    }
  //    endTime = System.currentTimeMillis();
  //    System.out.println(nbTest + " curve construction TwoCcyXCcy (with par spread (market quote) and " + (nbDsc1 + nbFwd1 + nbDsc2 + nbFwd2) + " instruments): " + (endTime - startTime) + " ms");
  //    // Performance note: curve construction (with par spread (market quote), 4 curves - 2 ccy and 40 instruments): 13-Jul-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 1500 ms for 100 bundles.
  //
  //    startTime = System.currentTimeMillis();
  //    for (int looptest = 0; looptest < nbTest; looptest++) {
  //      curveParSpreadMQ2 = YieldCurveConstructionGeneratorTwoCurrenciesXCcyData.makeCurves2Blocks(DEFINITIONS_DSC_1, DEFINITIONS_FWD_1, DEFINITIONS_DSC_2, DEFINITIONS_FWD_2, PSMQ_CALCULATOR,
  //          PSMQCS_CALCULATOR, true, false);
  //    }
  //    endTime = System.currentTimeMillis();
  //    System.out.println(nbTest + " curve construction TwoCcyXCcy 2 Blocks (with par spread (market quote) and " + (nbDsc1 + nbFwd1 + nbDsc2 + nbFwd2) + " instruments): " + (endTime - startTime)
  //        + " ms");
  //    // Performance note: curve construction (with par spread (market quote) in 2 blocks, 4 curves - 2 ccy and 40 instruments): 13-Jul-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 960 ms for 100 bundles.
  //  }

}
