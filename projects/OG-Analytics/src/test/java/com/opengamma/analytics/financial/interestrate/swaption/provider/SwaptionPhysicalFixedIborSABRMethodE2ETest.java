/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.SABRSensitivityNodeCalculator;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveUSD;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsSABRSwaptionUSD;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.provider.calculator.discounting.PV01CurveParametersCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueCurveSensitivitySABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSensitivitySABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Test related to swaption end-to-end using standardized market data.
 */
@Test(groups = TestGroup.UNIT)
public class SwaptionPhysicalFixedIborSABRMethodE2ETest {

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2014, 1, 22);
  private static final IborIndex[] INDEX_IBOR_LIST = StandardDataSetsMulticurveUSD.indexIborArrayUSDOisL1L3L6();
  private static final IborIndex USDLIBOR3M = INDEX_IBOR_LIST[1];
  private static final Calendar NYC = StandardDataSetsMulticurveUSD.calendarArray()[0];
  private static final Currency USD = USDLIBOR3M.getCurrency();
  // Standard conventions
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_FIXED_IBOR_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_FIXED_IBOR_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  // Curve Data
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_PAIR = StandardDataSetsMulticurveUSD.getCurvesUSDOisL1L3L6();
  private static final MulticurveProviderDiscount MULTICURVE = MULTICURVE_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK = MULTICURVE_PAIR.getSecond();
  // SABR data
  private static final SABRInterestRateParameters SABR_PARAMETER = StandardDataSetsSABRSwaptionUSD.createSABR1();
  private static final SABRSwaptionProviderDiscount MULTICURVE_SABR = new SABRSwaptionProviderDiscount(MULTICURVE, SABR_PARAMETER, USD6MLIBOR3M);

  // Standard swaption
  private static final double NOTIONAL = 100000000; //100m
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2016, 1, 22); // 2Y
  private static final Period TENOR_SWAP_3M = Period.ofYears(7);
  private static final double FIXED_RATE_3M = 0.0350;
  private static final GeneratorAttributeIR ATTRIBUTE_3M = new GeneratorAttributeIR(TENOR_SWAP_3M);
  private static final SwapFixedIborDefinition SWAP_PAYER_DEFINITION = USD6MLIBOR3M.generateInstrument(EXPIRY_DATE, FIXED_RATE_3M, NOTIONAL, ATTRIBUTE_3M);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_P_2Yx7Y_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, true);
  private static final SwaptionPhysicalFixedIbor SWAPTION_P_2Yx7Y = SWAPTION_P_2Yx7Y_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final SwaptionPhysicalFixedIborSABRMethod METHOD_SWPT_SABR = SwaptionPhysicalFixedIborSABRMethod.getInstance();
  private static final PresentValueSABRSwaptionCalculator PVSSC = PresentValueSABRSwaptionCalculator.getInstance();
  private static final PresentValueCurveSensitivitySABRSwaptionCalculator PVCSSSC = PresentValueCurveSensitivitySABRSwaptionCalculator.getInstance();
  private static final PV01CurveParametersCalculator<SABRSwaptionProviderInterface> PV01C = new PV01CurveParametersCalculator<>(PVCSSSC);
  private static final PresentValueSABRSensitivitySABRSwaptionCalculator PVSSSSC = PresentValueSABRSensitivitySABRSwaptionCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<SABRSwaptionProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSSSC);
  private static final MarketQuoteSensitivityBlockCalculator<SABRSwaptionProviderInterface> MQSBC = new MarketQuoteSensitivityBlockCalculator<>(PSC);
  //  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-3;
  private static final double TOLERANCE_PV_DELTA = 1.0E-4;
  private static final double TOLERANCE_RATE = 1.0E-8;
  private static final double BP1 = 1.0E-4;

  public void presentValue() {
    final MultipleCurrencyAmount pvComputed = SWAPTION_P_2Yx7Y.accept(PVSSC, MULTICURVE_SABR);
    final MultipleCurrencyAmount pvExpected = MultipleCurrencyAmount.of(USD, 3156216.4895777884);
    //    final double pr = SWAPTION_P_2Yx7Y.getUnderlyingSwap().accept(PRDC, MULTICURVE);
    assertEquals("SwaptionPhysicalFixedIborSABRMethod: present value from standard curves", pvExpected.getAmount(USD), pvComputed.getAmount(USD), TOLERANCE_PV);
  }

  public void impliedVolatility() {
    final double volExpected = 0.29809226250599946;
    final double volComputed = METHOD_SWPT_SABR.impliedVolatility(SWAPTION_P_2Yx7Y, MULTICURVE_SABR);
    assertEquals("SwaptionPhysicalFixedIborSABRMethod: present value from standard curves", volExpected, volComputed, TOLERANCE_RATE);
  }

  /**
   * Test Bucketed PV01 with a standard set of data against hard-coded standard values for a swap fixed vs LIBOR3M. Can be used for platform testing or regression testing.
   */
  public void pv01() {
    final double pv01dsc = -2253.115361063714;
    final double pv01fwd = 32885.97222733803;
    final ReferenceAmount<Pair<String, Currency>> pv01Computed = SWAPTION_P_2Yx7Y.accept(PV01C, MULTICURVE_SABR);
    assertEquals("SwaptionPhysicalFixedIborSABRMethod: pv01 from standard curves", pv01dsc, pv01Computed.getMap().get(Pairs.of(MULTICURVE.getName(USD), USD)), TOLERANCE_RATE);
    assertEquals("SwaptionPhysicalFixedIborSABRMethod: pv01 from standard curves", pv01fwd, pv01Computed.getMap().get(Pairs.of(MULTICURVE.getName(USDLIBOR3M), USD)), TOLERANCE_RATE);
  }

  /**
   * Test Bucketed PV01 with a standard set of data against hard-coded standard values for a swaption physical fixed vs LIBOR3M. Can be used for platform testing or regression testing.
   */
  public void BucketedPV01() {
    final double[] deltaDsc = {-0.8970521909327039, -0.8970528138871251, 2.0679726864123788E-5, -2.800077859468568E-4, 0.020545355340195248, -28.660344224880443, 1.0311659235333974,
      -101.02574104758263, -162.90022502561072, -34.11856047817592, -41.87866271284144, -47.20852985708558, -52.64477419064427,
      -193.55488041593657, -379.8195117988651, 26.793804732157106, 259.3051035445537 };
    final double[] deltaFwd3 = {0.6768377111533482, -0.013861472263779616, -0.00815248053117034, 28.045784074714817, -10296.86232676286, -9.445439010985615, -12.048126446934697,
      -60.09929275115254, 14090.425121330405, 28748.01823487962, 0.00, 0.00, 0.00, 0.00, 0.00 };
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(ObjectsPair.of(MULTICURVE.getName(USD), USD), new DoubleMatrix1D(deltaDsc));
    sensitivity.put(ObjectsPair.of(MULTICURVE.getName(USDLIBOR3M), USD), new DoubleMatrix1D(deltaFwd3));
    final MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
    final MultipleCurrencyParameterSensitivity pvpsComputed = MQSBC.fromInstrument(SWAPTION_P_2Yx7Y, MULTICURVE_SABR, BLOCK).multipliedBy(BP1);
    AssertSensivityObjects.assertEquals("SwaptionPhysicalFixedIborSABRMethod: bucketed deltas from standard curves", pvpsExpected, pvpsComputed, TOLERANCE_PV_DELTA);
  }

  public void BucketedSABRRisk() {
    final PresentValueSABRSensitivityDataBundle pvssComputed = SWAPTION_P_2Yx7Y.accept(PVSSSSC, MULTICURVE_SABR);
    final PresentValueSABRSensitivityDataBundle pvssNodeComputed = SABRSensitivityNodeCalculator.calculateNodeSensitivities(pvssComputed, SABR_PARAMETER);
    Map<DoublesPair, Double> alphaRiskExpected = new HashMap<>();
    alphaRiskExpected.put(DoublesPair.of(1.0, 5.0), 6204.475194599176);
    alphaRiskExpected.put(DoublesPair.of(2.0, 5.0), 3.946312129841228E7);
    alphaRiskExpected.put(DoublesPair.of(1.0, 10.0), 4136.961894403856);
    alphaRiskExpected.put(DoublesPair.of(2.0, 10.0), 2.6312850632053435E7);
    Map<DoublesPair, Double> alphaRiskComputed = pvssNodeComputed.getAlpha().getMap();
    for (Entry<DoublesPair, Double> entry : alphaRiskExpected.entrySet()) {
      assertEquals("SwaptionPhysicalFixedIborSABRMethod: BucketedSABRRisk from standard curves " + entry.getKey(),
          alphaRiskComputed.get(entry.getKey()), entry.getValue(), TOLERANCE_PV_DELTA);
    }
    Map<DoublesPair, Double> betaRiskExpected = new HashMap<>();
    betaRiskExpected.put(DoublesPair.of(1.0, 5.0), -1135.9264046809967);
    betaRiskExpected.put(DoublesPair.of(2.0, 5.0), -7224978.7593665235);
    betaRiskExpected.put(DoublesPair.of(1.0, 10.0), -757.402375482628);
    betaRiskExpected.put(DoublesPair.of(2.0, 10.0), -4817403.709083163);
    Map<DoublesPair, Double> betaRiskComputed = pvssNodeComputed.getBeta().getMap();
    for (Entry<DoublesPair, Double> entry : betaRiskExpected.entrySet()) {
      assertEquals("SwaptionPhysicalFixedIborSABRMethod: BucketedSABRRisk from standard curves " + entry.getKey(),
          betaRiskComputed.get(entry.getKey()), entry.getValue(), TOLERANCE_PV_DELTA);
    }
    Map<DoublesPair, Double> rhoRiskExpected = new HashMap<>();
    rhoRiskExpected.put(DoublesPair.of(1.0, 5.0), 25.108219123928023);
    rhoRiskExpected.put(DoublesPair.of(2.0, 5.0), 159699.0342933747);
    rhoRiskExpected.put(DoublesPair.of(1.0, 10.0), 16.74142332657722);
    rhoRiskExpected.put(DoublesPair.of(2.0, 10.0), 106482.62725264493);
    Map<DoublesPair, Double> rhoRiskComputed = pvssNodeComputed.getRho().getMap();
    for (Entry<DoublesPair, Double> entry : rhoRiskExpected.entrySet()) {
      assertEquals("SwaptionPhysicalFixedIborSABRMethod: BucketedSABRRisk from standard curves " + entry.getKey(),
          rhoRiskComputed.get(entry.getKey()), entry.getValue(), TOLERANCE_PV_DELTA);
    }
    Map<DoublesPair, Double> nuRiskExpected = new HashMap<>();
    nuRiskExpected.put(DoublesPair.of(1.0, 5.0), 37.75195237231597);
    nuRiskExpected.put(DoublesPair.of(2.0, 5.0), 240118.59649586905);
    nuRiskExpected.put(DoublesPair.of(1.0, 10.0), 25.17189343259352);
    nuRiskExpected.put(DoublesPair.of(2.0, 10.0), 160104.0301854763);
    Map<DoublesPair, Double> nuRiskComputed = pvssNodeComputed.getNu().getMap();
    for (Entry<DoublesPair, Double> entry : nuRiskExpected.entrySet()) {
      assertEquals("SwaptionPhysicalFixedIborSABRMethod: BucketedSABRRisk from standard curves " + entry.getKey(),
          nuRiskComputed.get(entry.getKey()), entry.getValue(), TOLERANCE_PV_DELTA);
    }
  }

}
