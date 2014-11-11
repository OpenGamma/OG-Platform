/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedSecurityDefinitionBrazilTest;
import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyGearingDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsGovtBrInflationBRL;
import com.opengamma.analytics.financial.interestrate.datasets.StandardTimeSeriesInflationDataSets;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurveSimple;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.MarketQuoteInflationSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflationissuer.PresentValueCurveSensitivityDiscountingInflationIssuerCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflationissuer.PresentValueDiscountingInflationIssuerCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.ParameterSensitivityInflationParameterCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Tests the pricing and risk of Brazilian inflation bonds (capital indexed).
 */
@Test(groups = TestGroup.UNIT)
public class BondCapitalIndexedTransactionDiscountingMethodBrazilTest {

  private static final ZonedDateTime VALUATION_DATE = BondCapitalIndexedSecurityDefinitionBrazilTest.VALUATION_DATE;
  private static final ZonedDateTime SETTLE_DATE_STANDARD = VALUATION_DATE.plusDays(1);
  private static final ZonedDateTime SETTLE_DATE_PAST = VALUATION_DATE.minusDays(4);
  private static final ZonedDateTime SETTLE_DATE_FOWARD = VALUATION_DATE.plusDays(7);
  private static final double QUANTITY = 1000;
  private static final double TRADE_PRICE = 2500; // Dirty nominal not price;
  private static final BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> 
      NTNB_SECURITY_DEFINITION = BondCapitalIndexedSecurityDefinitionBrazilTest.NTNB_SECURITY_DEFINITION;
  private static final Currency BRL = NTNB_SECURITY_DEFINITION.getCurrency();
  private static final IndexPrice PRICE_INDEX_IPCA = NTNB_SECURITY_DEFINITION.getPriceIndex();
  private static final BondCapitalIndexedTransactionDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> 
      NTNB_TRANSACTION_STD_DEFINITION = new BondCapitalIndexedTransactionDefinition<>(
          NTNB_SECURITY_DEFINITION, QUANTITY, SETTLE_DATE_STANDARD, TRADE_PRICE);
  private static final BondCapitalIndexedTransactionDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> 
      NTNB_TRANSACTION_PAS_DEFINITION = new BondCapitalIndexedTransactionDefinition<>(
          NTNB_SECURITY_DEFINITION, QUANTITY, SETTLE_DATE_PAST, TRADE_PRICE);
  private static final BondCapitalIndexedTransactionDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> 
      NTNB_TRANSACTION_FWD_DEFINITION = new BondCapitalIndexedTransactionDefinition<>(
          NTNB_SECURITY_DEFINITION, QUANTITY, SETTLE_DATE_FOWARD, TRADE_PRICE);
  private static final DoubleTimeSeries<ZonedDateTime> BR_IPCA = StandardTimeSeriesInflationDataSets.timeSeriesBrCpi(VALUATION_DATE);
  private static final BondCapitalIndexedTransaction<Coupon> NTNB_TRANSACTION_STD = 
      NTNB_TRANSACTION_STD_DEFINITION.toDerivative(VALUATION_DATE, BR_IPCA);  
  private static final BondCapitalIndexedTransaction<Coupon> NTNB_TRANSACTION_PAS = 
      NTNB_TRANSACTION_PAS_DEFINITION.toDerivative(VALUATION_DATE, BR_IPCA);
  private static final BondCapitalIndexedTransaction<Coupon> NTNB_TRANSACTION_FWD = 
      NTNB_TRANSACTION_FWD_DEFINITION.toDerivative(VALUATION_DATE, BR_IPCA);
  
  private static final BondCapitalIndexedTransactionDiscountingMethod METHOD_TRA =
      BondCapitalIndexedTransactionDiscountingMethod.getInstance();
  private static final BondCapitalIndexedSecurityDiscountingMethod METHOD_SEC =
      BondCapitalIndexedSecurityDiscountingMethod.getInstance();
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = 
      PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final PresentValueDiscountingInflationIssuerCalculator PVDIIC = 
      PresentValueDiscountingInflationIssuerCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingInflationIssuerCalculator PVCSDIIC = 
      PresentValueCurveSensitivityDiscountingInflationIssuerCalculator.getInstance();
  private static final ParameterSensitivityInflationParameterCalculator<ParameterInflationIssuerProviderInterface> PSIC = 
      new ParameterSensitivityInflationParameterCalculator<>(PVCSDIIC);
      private static final MarketQuoteInflationSensitivityBlockCalculator<ParameterInflationIssuerProviderInterface> MQISBC =
          new MarketQuoteInflationSensitivityBlockCalculator<>(PSIC);
  
  private static final double TOLERANCE_PV = 1.0E-4;
  private static final double TOLERANCE_PV_DELTA = 1.0E-2;
  private static final double BP1 = 1.0E-4;
  
  /** Curves */
  private static final LegalEntityFilter<LegalEntity> META = new LegalEntityShortName();
  private static final String BR_GOVT_NAME = NTNB_SECURITY_DEFINITION.getIssuer();
  private static final Pair<Object, LegalEntityFilter<LegalEntity>> ISSUER_BR_GOVT = Pairs.of((Object) BR_GOVT_NAME, META);
  private static final String BRL_DSC_NAME = "BRL-DSC";
  private static final YieldAndDiscountCurve CURVE_DSC = 
      new YieldCurve(BRL_DSC_NAME, ConstantDoublesCurve.from(0.10, BRL_DSC_NAME));
  private static final String BRL_GOVT_NAME = "BRL-GOVT";
  private static final YieldAndDiscountCurve CURVE_GOVT = 
      new YieldCurve(BRL_GOVT_NAME, ConstantDoublesCurve.from(0.11, BRL_GOVT_NAME));
  private static final String BRL_IPCA_NAME = "BRL-ZCINFL-IPCA";
  private static final double PRICE_INDEX_SEPTEMBER = 3991.24;
  private static final double[] INDEX_VALUE_BR = new double[] {PRICE_INDEX_SEPTEMBER, PRICE_INDEX_SEPTEMBER * (1+0.07), 
    PRICE_INDEX_SEPTEMBER * Math.pow(1.07, 10), PRICE_INDEX_SEPTEMBER * Math.pow(1.07, 40) };
  private static final double[] TIME_VALUE_BR = new double[] {-33.0d/360.0d, 1.0d-33.0d/360.0d, 10.0d-33.0d/360.0d, 40.0d-33.0d/360.0d };
  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  // TODO: replace by exponential interpolator.
  private static final InterpolatedDoublesCurve PRICE_CURVE_BR = 
      InterpolatedDoublesCurve.from(TIME_VALUE_BR, INDEX_VALUE_BR, LINEAR_FLAT, BRL_IPCA_NAME);
  private static final PriceIndexCurveSimple PRICE_INDEX_CURVE_BR = new PriceIndexCurveSimple(PRICE_CURVE_BR);
  private static final InflationIssuerProviderDiscount MARKET = new InflationIssuerProviderDiscount();
  static {
    MARKET.setCurve(Currency.BRL, CURVE_DSC);
    MARKET.setCurve(ISSUER_BR_GOVT, CURVE_GOVT);
    MARKET.setCurve(PRICE_INDEX_IPCA, PRICE_INDEX_CURVE_BR);
  }
  
  /** Present value of bonds with different settlement dates differs only by the settlement amount. */
  @Test
  public void presentValueSettlement() {
    MultipleCurrencyAmount pvPasComputed = METHOD_TRA.presentValue(NTNB_TRANSACTION_PAS, MARKET);
    MultipleCurrencyAmount pvStdComputed = METHOD_TRA.presentValue(NTNB_TRANSACTION_STD, MARKET);
    MultipleCurrencyAmount pvFwdComputed = METHOD_TRA.presentValue(NTNB_TRANSACTION_FWD, MARKET);
    Coupon settleStd = NTNB_TRANSACTION_STD.getBondTransaction().getSettlement();
    Coupon settleFwd = NTNB_TRANSACTION_FWD.getBondTransaction().getSettlement();
    MultipleCurrencyAmount settleStdPv = settleStd.accept(PVDC, MARKET).multipliedBy(TRADE_PRICE * QUANTITY);
    MultipleCurrencyAmount settleFwdPv = settleFwd.accept(PVDC, MARKET).multipliedBy(TRADE_PRICE * QUANTITY);
    assertEquals("BondCapitalIndexedTransactionDiscountingMethodBrazil: present Value", 
        pvStdComputed.getAmount(BRL) + settleStdPv.getAmount(BRL), 
        pvFwdComputed.getAmount(BRL) + settleFwdPv.getAmount(BRL), TOLERANCE_PV);
    assertEquals("BondCapitalIndexedTransactionDiscountingMethodBrazil: present Value", 
        pvStdComputed.getAmount(BRL) + settleStdPv.getAmount(BRL), 
        pvPasComputed.getAmount(BRL), TOLERANCE_PV);
  }
  
  //TODO: more precise valuation test
  
  @Test
  public void presentValueMethodVsCalculator() {
    MultipleCurrencyAmount pvPasMethod = METHOD_TRA.presentValue(NTNB_TRANSACTION_PAS, MARKET);
    MultipleCurrencyAmount pvPasCalculator = NTNB_TRANSACTION_PAS.accept(PVDIIC, MARKET);
    assertEquals("BondCapitalIndexedTransactionDiscountingMethodBrazil: present Value", 
        pvPasMethod.getAmount(BRL), pvPasCalculator.getAmount(BRL), TOLERANCE_PV);
    MultipleCurrencyAmount pvStdMethod = METHOD_TRA.presentValue(NTNB_TRANSACTION_STD, MARKET);
    MultipleCurrencyAmount pvStdCalculator = NTNB_TRANSACTION_STD.accept(PVDIIC, MARKET);
    assertEquals("BondCapitalIndexedTransactionDiscountingMethodBrazil: present Value", 
        pvStdMethod.getAmount(BRL), pvStdCalculator.getAmount(BRL), TOLERANCE_PV);
    MultipleCurrencyAmount pvFwdMethod = METHOD_TRA.presentValue(NTNB_TRANSACTION_FWD, MARKET);
    MultipleCurrencyAmount pvFwdCalculator = NTNB_TRANSACTION_FWD.accept(PVDIIC, MARKET);
    assertEquals("BondCapitalIndexedTransactionDiscountingMethodBrazil: present Value", 
        pvFwdMethod.getAmount(BRL), pvFwdCalculator.getAmount(BRL), TOLERANCE_PV);
  }

  /** Present value curve sensitivity of bonds with different settlement dates differs only by the settlement amount. */
  @Test
  public void presentValueCurveSensitivity() {
    MultipleCurrencyInflationSensitivity pvcsPasComputed = 
        METHOD_TRA.presentValueCurveSensitivity(NTNB_TRANSACTION_PAS, MARKET).cleaned();
    MultipleCurrencyInflationSensitivity pvcsStdComputed = 
        METHOD_TRA.presentValueCurveSensitivity(NTNB_TRANSACTION_STD, MARKET).cleaned();
    MultipleCurrencyInflationSensitivity pvcsFwdComputed = 
        METHOD_TRA.presentValueCurveSensitivity(NTNB_TRANSACTION_FWD, MARKET).cleaned();
    Coupon settleStd = NTNB_TRANSACTION_STD.getBondTransaction().getSettlement();
    Coupon settleFwd = NTNB_TRANSACTION_FWD.getBondTransaction().getSettlement();
    MultipleCurrencyMulticurveSensitivity settleStdPvcs = settleStd.accept(PVCSDC, MARKET).multipliedBy(TRADE_PRICE * QUANTITY);
    MultipleCurrencyMulticurveSensitivity settleFwdPvcs = settleFwd.accept(PVCSDC, MARKET).multipliedBy(TRADE_PRICE * QUANTITY);
    MultipleCurrencyInflationSensitivity pvcsStdComputedNoSettle = 
        pvcsStdComputed.plus(MultipleCurrencyInflationSensitivity.of(settleStdPvcs)).cleaned();
    MultipleCurrencyInflationSensitivity pvcsFwdComputedNoSettle = 
        pvcsFwdComputed.plus(MultipleCurrencyInflationSensitivity.of(settleFwdPvcs)).cleaned();
    AssertSensitivityObjects.assertEquals("BondCapitalIndexedTransactionDiscountingMethodBrazil: present Value curve sensitivity", 
        pvcsPasComputed, pvcsStdComputedNoSettle, TOLERANCE_PV_DELTA);
    AssertSensitivityObjects.assertEquals("BondCapitalIndexedTransactionDiscountingMethodBrazil: present Value curve sensitivity", 
        pvcsPasComputed, pvcsFwdComputedNoSettle, TOLERANCE_PV_DELTA);
  }
  
  @Test
  public void presentValueCurveSensitivityAllCurves() {
    MultipleCurrencyInflationSensitivity pvcsStd = 
        METHOD_TRA.presentValueCurveSensitivity(NTNB_TRANSACTION_STD, MARKET).cleaned();
    InflationSensitivity pvcsSingleCurrency = pvcsStd.getSensitivity(BRL);
    Map<String, List<DoublesPair>> pvcsPriceIndex = pvcsSingleCurrency.getPriceCurveSensitivities();
    assertEquals("BondCapitalIndexedTransactionDiscountingMethodBrazil: present Value curve sensitivity", 
        1, pvcsPriceIndex.size()); // 1 inflation curve
    Map<String, List<DoublesPair>> pvcsDiscounting = pvcsSingleCurrency.getYieldDiscountingSensitivities();
    assertEquals("BondCapitalIndexedTransactionDiscountingMethodBrazil: present Value curve sensitivity", 
        2, pvcsDiscounting.size()); // 1 discounting and 1 issuer
  }
  
  @Test
  public void presentValueCurveSensitivityOrderMagnitude() {
    MultipleCurrencyParameterSensitivity pvcsStd = PSIC.calculateSensitivity(NTNB_TRANSACTION_STD, MARKET);
    double pvcsIssuerTotal = pvcsStd.getSensitivity(BRL_GOVT_NAME, BRL).getEntry(0);
    assertTrue("BondCapitalIndexedTransactionDiscountingMethodBrazil: present Value curve sensitivity", 
        pvcsIssuerTotal < 0); // Long bond: negative sensitivity to rate
    assertTrue("BondCapitalIndexedTransactionDiscountingMethodBrazil: present Value curve sensitivity", 
        pvcsIssuerTotal < -1_000_000 * 10 * 2.5 * 0.5); 
    assertTrue("BondCapitalIndexedTransactionDiscountingMethodBrazil: present Value curve sensitivity", 
        pvcsIssuerTotal > -1_000_000 * 10 * 2.5 * 2); 
    // Long bond: 1,000,000 notional x ~10Y x index ratio ~ 2.5 + inflation - discounting
    DoubleMatrix1D pvcsInf = pvcsStd.getSensitivity(BRL_IPCA_NAME, BRL);
    double pvcsInfTotal = 0.0d;
    for(int loopinfl = 0; loopinfl < pvcsInf.getNumberOfElements() ; loopinfl++) {
      pvcsInfTotal += pvcsInf.getEntry(loopinfl);
    }
    assertTrue("BondCapitalIndexedTransactionDiscountingMethodBrazil: present Value curve sensitivity", 
        pvcsInfTotal > 0); // Long bond: positive sensitivity to inflation
    //TODO: Full curve calibration + sensi to market quotes.
  }
  
  /** Brazil inflation bond are quoted as dirty nominal note or certificate price, i.e. the total price paid
   *  for a given certificate, usually with a notional of BRL 1,000. */
  @Test
  public void notePrice() {
    double noteDirtyPrice = METHOD_SEC.dirtyNominalNotePriceFromCurves(NTNB_TRANSACTION_STD.getBondStandard(), MARKET);
    double dirtyPrice = METHOD_SEC.dirtyNominalPriceFromCurves(NTNB_TRANSACTION_STD.getBondStandard(), MARKET);
    assertEquals("BondCapitalIndexedTransactionDiscountingMethodBrazil: note price", 
        dirtyPrice * NTNB_TRANSACTION_STD.getNotionalStandard(), noteDirtyPrice);
  }
  
  /** Calibrated curves */
  private static final Pair<InflationIssuerProviderDiscount, CurveBuildingBlockBundle> MARKET_CALIBRATED_PAIR = 
      StandardDataSetsGovtBrInflationBRL.getCurvesBrlOisBrCpiCurrent(VALUATION_DATE);
  private static final InflationIssuerProviderDiscount MARKET_CALIBRATED = MARKET_CALIBRATED_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_CALIBRATED = MARKET_CALIBRATED_PAIR.getSecond();
    
  @Test
  public void bucketedPV01Calibrated() {
    MultipleCurrencyParameterSensitivity pvmqsComputed = 
        MQISBC.fromInstrument(NTNB_TRANSACTION_STD, MARKET_CALIBRATED, BLOCK_CALIBRATED).multipliedBy(BP1);
    String cpiCurveName = MARKET_CALIBRATED.getName(PRICE_INDEX_IPCA);
    DoubleMatrix1D mqsInfl = pvmqsComputed.getSensitivity(cpiCurveName, BRL);
    double pvcsInfTotal = 0.0d;
    for(int loopinfl = 0; loopinfl < mqsInfl.getNumberOfElements() ; loopinfl++) {
      pvcsInfTotal += mqsInfl.getEntry(loopinfl);
    }
    double quickEstimate = 10 * Math.pow(1.06, 9) / Math.pow(1.12, 10) * 1_000_000 / 10_000;
    // nbYears * inflation cmp * df * notional / bp
    for(int loopcpn = 0; loopcpn < 10 ; loopcpn++) { // 10Y bond
      quickEstimate += 0.06 * (loopcpn+1) * Math.pow(1.06, loopcpn) / Math.pow(1.12, loopcpn+1) * 1_000_000 / 10_000;
    }
    assertTrue("BondCapitalIndexedTransactionDiscountingMethodBrazil: market quote sensitivity", 
        pvcsInfTotal > quickEstimate * 0.5); 
    assertTrue("BondCapitalIndexedTransactionDiscountingMethodBrazil: market quote sensitivity", 
        pvcsInfTotal > quickEstimate * 3.0);  // TODO: change to 2 when exponential
    @SuppressWarnings("unused")
    int t=0;
  }
  
}
