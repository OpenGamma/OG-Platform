/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.util.LinkedHashMap;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondDataSetsUsd;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedInflationMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedInflationZeroCoupon;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationGearingDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationZeroCouponDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorDelegate;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsGovtUsInflationUSD;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsInflationUSD;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveUSD;
import com.opengamma.analytics.financial.interestrate.datasets.StandardTimeSeriesInflationDataSets;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PV01CurveParametersCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.MarketQuoteInflationSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.ParSpreadInflationMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueCurveSensitivityDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflationissuer.PresentValueCurveSensitivityInflationIssuerCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflationissuer.PresentValueInflationIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueCurveSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationConventionDataSets;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationTestsUtils;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.ParameterSensitivityInflationParameterCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ParameterSensitivityMulticurveMatrixCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.analytics.util.export.ExportUtils;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * End-to-end tests for (inflation) capital indexed bonds.
 */
@Test(groups = TestGroup.UNIT)
public class BondCapitalIndexedDiscountingE2ETest {

  private static final ZonedDateTime CALIBRATION_DATE = DateUtils.getUTCDate(2014, 12, 15);
  private static final Currency USD = Currency.USD;
  
  private static final GeneratorSwapFixedInflationZeroCoupon GENERATOR_ZCINFLATION_US = 
      GeneratorSwapFixedInflationMaster.getInstance().getGenerator("USCPI");
  private static final IndexPrice US_CPI = GENERATOR_ZCINFLATION_US.getIndexPrice();
  private static final ZonedDateTimeDoubleTimeSeries HTS_CPI = 
      StandardTimeSeriesInflationDataSets.timeSeriesUsCpi(CALIBRATION_DATE);
  private static final ZonedDateTimeDoubleTimeSeries  HTS_EMPTY = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(ZoneOffset.UTC);
  
  /** Calculators **/
  private static final BondTransactionDiscountingMethod METHOD_BOND_TRA = 
	      BondTransactionDiscountingMethod.getInstance();
	  private static final BondSecurityDiscountingMethod METHOD_BOND_SEC = 
	      BondSecurityDiscountingMethod.getInstance();

  private static final BondCapitalIndexedSecurityDiscountingMethod METHOD_CAPIND_BOND_SEC =
      BondCapitalIndexedSecurityDiscountingMethod.getInstance();  
  private static final PresentValueIssuerCalculator PVIssuerC = 
      PresentValueIssuerCalculator.getInstance();
  private static final PresentValueDiscountingInflationCalculator PVInflC = 
      PresentValueDiscountingInflationCalculator.getInstance();


  private static final ParSpreadInflationMarketQuoteDiscountingCalculator PSIMQDC = ParSpreadInflationMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator PSIMQCSDC = ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();


  private static final PresentValueCurveSensitivityDiscountingInflationCalculator sensi =
      PresentValueCurveSensitivityDiscountingInflationCalculator.getInstance();
  private static final PresentValueInflationIssuerDiscountingCalculator PVInflIssuerC =
      PresentValueInflationIssuerDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityInflationIssuerCalculator PVCSInflIssuerC =
      PresentValueCurveSensitivityInflationIssuerCalculator.getInstance();

  private static final ParameterSensitivityInflationParameterCalculator<ParameterInflationIssuerProviderInterface> PSIC = 
      new ParameterSensitivityInflationParameterCalculator<>(PVCSInflIssuerC);
  private static final MarketQuoteInflationSensitivityBlockCalculator<ParameterInflationIssuerProviderInterface> MQISBC =
      new MarketQuoteInflationSensitivityBlockCalculator<>(PSIC);


  private static final PresentValueCurveSensitivityIssuerCalculator PVCSIC = PresentValueCurveSensitivityIssuerCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<ParameterIssuerProviderInterface> PSC =
		  new ParameterSensitivityParameterCalculator<>(PVCSIC);
  private static final MarketQuoteSensitivityBlockCalculator<ParameterIssuerProviderInterface> MQSBC =
		  new MarketQuoteSensitivityBlockCalculator<>(PSC);  
  
  
  /** Bond fixed coupon 2024 */
  private static final BondFixedSecurityDefinition UST_SEC_DEFINITION = BondDataSetsUsd.bondUST_20241115(1.0);
  private static final double QUANTITY = 10000000; // 10m
  private static final ZonedDateTime SETTLE_DATE_FIXED = DateUtils.getUTCDate(2014, 12, 16);
  private static final double TRADE_PRICE_FIXED = 1.01+11.0/32.0/100.0;
  private static final BondFixedTransactionDefinition UST_TRA_DEFINITION = 
      new BondFixedTransactionDefinition(UST_SEC_DEFINITION, QUANTITY, SETTLE_DATE_FIXED, TRADE_PRICE_FIXED);
  private static final BondFixedTransaction UST_TRA = 
      UST_TRA_DEFINITION.toDerivative(CALIBRATION_DATE);


  private static final BondFixedSecurityDefinition UST_SEC_DEFINITION1 = BondDataSetsUsd.bondUST_20240815(1.0);
  private static final double QUANTITY1 = 10000000; // 10m
  private static final ZonedDateTime SETTLE_DATE_FIXED1 = DateUtils.getUTCDate(2014, 12, 16);
  private static final double TRADE_PRICE_FIXED1 = 1.01;
  private static final BondFixedTransactionDefinition UST_TRA_DEFINITION1 =
      new BondFixedTransactionDefinition(UST_SEC_DEFINITION1, QUANTITY1, SETTLE_DATE_FIXED1, TRADE_PRICE_FIXED1);
  private static final BondFixedTransaction UST_TRA1 =
      UST_TRA_DEFINITION1.toDerivative(CALIBRATION_DATE);
  
  /** Zero-coupon Inflation US (linear interpolation of Price Index). 5Y aged. */
  private static final double NOTIONAL = 10_000_000;
  private static final ZonedDateTime ACCRUAL_START_DATE_2 = DateUtils.getUTCDate(2014, 1, 8);
  private static final GeneratorAttributeIR ZCI_2_ATTR = new GeneratorAttributeIR(Period.ofYears(5));
  private static final double RATE_FIXED_2 = 0.0100;
  private static final SwapFixedInflationZeroCouponDefinition ZCI_2_DEFINITION = 
      GENERATOR_ZCINFLATION_US.generateInstrument(ACCRUAL_START_DATE_2, RATE_FIXED_2, NOTIONAL, ZCI_2_ATTR);
  private static final InstrumentDerivative ZCI_2 = ZCI_2_DEFINITION.toDerivative(ACCRUAL_START_DATE_2, 
      new ZonedDateTimeDoubleTimeSeries[] {HTS_EMPTY, HTS_CPI});
  
  /** Bond Inflation (TIPS) 2024 */
  private static final BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponInterpolationGearingDefinition> 
    TIPS_24_SEC_DEFINITION = BondDataSetsUsd.bondTIPS_20240715(1.0);
  private static final double QUANTITY_TIPS_1 = 10000000; // 10m
  private static final ZonedDateTime SETTLE_DATE_TIPS_1 = DateUtils.getUTCDate(2014, 12, 16);
  private static final double TRADE_PRICE_TIPS_1 = 0.98 + 23.5d / 100.0d / 32.0d;
  private static final BondCapitalIndexedTransactionDefinition<CouponInflationZeroCouponInterpolationGearingDefinition> 
    TIPS_24_TRA_DEFINITION = new BondCapitalIndexedTransactionDefinition<>(TIPS_24_SEC_DEFINITION, QUANTITY_TIPS_1,
        SETTLE_DATE_TIPS_1, TRADE_PRICE_TIPS_1);
  private static final BondCapitalIndexedTransaction<?> TIPS_24_1_TRA =
      TIPS_24_TRA_DEFINITION.toDerivative(CALIBRATION_DATE, HTS_CPI);

  private static final BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponInterpolationGearingDefinition>
      TIPS_43_SEC_DEFINITION = BondDataSetsUsd.bondTIPS_20430215(1.0);

  private static final double TRADE_PRICE_TIPS_43 = 1.1211;
  private static final BondCapitalIndexedTransactionDefinition<CouponInflationZeroCouponInterpolationGearingDefinition>
      TIPS_43_TRA_DEFINITION = new BondCapitalIndexedTransactionDefinition<>(TIPS_43_SEC_DEFINITION, QUANTITY_TIPS_1,
                                                                             SETTLE_DATE_TIPS_1, TRADE_PRICE_TIPS_43);
  private static final BondCapitalIndexedTransaction<?> TIPS_43_1_TRA =
      TIPS_43_TRA_DEFINITION.toDerivative(CALIBRATION_DATE, HTS_CPI);
  
  /** Curves **/
  
  // Curves for Nominal Treasury bonds - OIS + USGOVT
  private static final Pair<IssuerProviderDiscount, CurveBuildingBlockBundle> ISSUER_GOVT_PAIR = 
      StandardDataSetsGovtUsInflationUSD.getCurvesUsdOisUsGovt(CALIBRATION_DATE);
  private static final IssuerProviderDiscount ISSUER_GOVT = ISSUER_GOVT_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK = ISSUER_GOVT_PAIR.getSecond();
    
  // Curves for Inflation-linked Treasury bonds - OIS + USGOVT + USCPI
  private static final Pair<InflationIssuerProviderDiscount, CurveBuildingBlockBundle> INFL_ISSUER_GOVT_PAIR =
      StandardDataSetsGovtUsInflationUSD.getCurvesUsdOisUsGovtUsCpi(CALIBRATION_DATE);
  private static final InflationIssuerProviderDiscount INFL_ISSUER_GOVT_1 = INFL_ISSUER_GOVT_PAIR.getFirst();
  private static final CurveBuildingBlockBundle INFL_ISSUER_GOVT_1_BLOCK = INFL_ISSUER_GOVT_PAIR.getSecond();
  
  // Curves for Inflation-linked Treasury bonds - OIS + USGOVT + USCPI + SEASONALITY
  private static final Pair<InflationIssuerProviderDiscount, CurveBuildingBlockBundle> INFL_ISSUER_GOVT_2_PAIR =
      StandardDataSetsGovtUsInflationUSD.getCurvesUsdOisUsGovtUsCpiCurrentSeasonality(CALIBRATION_DATE);
  private static final InflationIssuerProviderDiscount INFL_ISSUER_GOVT_2 = INFL_ISSUER_GOVT_2_PAIR.getFirst();
  private static final CurveBuildingBlockBundle INFL_ISSUER_GOVT_2_BLOCK = INFL_ISSUER_GOVT_2_PAIR.getSecond();
  
  // Curves for Inflation-linked Treasury bonds - OIS + USGOVT + TIPS
  private static final Pair<InflationIssuerProviderDiscount, CurveBuildingBlockBundle> INFL_ISSUER_GOVT_3_PAIR =
      StandardDataSetsGovtUsInflationUSD.getCurvesUsdOisUsGovtUsTips(CALIBRATION_DATE);
  private static final InflationIssuerProviderDiscount INFL_ISSUER_GOVT_3 = INFL_ISSUER_GOVT_3_PAIR.getFirst();
  private static final CurveBuildingBlockBundle INFL_ISSUER_GOVT_3_BLOCK = INFL_ISSUER_GOVT_3_PAIR.getSecond();

  // Curves for Inflation-linked Treasury bonds - OIS + USGOVT + TIPS + Hedged
  private static final Pair<InflationIssuerProviderDiscount, CurveBuildingBlockBundle> INFL_ISSUER_GOVT_4_PAIR =
      StandardDataSetsGovtUsInflationUSD.getHedgeCurvesUsdOisUsGovtUsCpi(CALIBRATION_DATE);
  private static final InflationIssuerProviderDiscount INFL_ISSUER_GOVT_4 = INFL_ISSUER_GOVT_4_PAIR.getFirst();
  private static final CurveBuildingBlockBundle INFL_ISSUER_GOVT_4_BLOCK = INFL_ISSUER_GOVT_4_PAIR.getSecond();
  
  private static final double BP1 = 1.0E-4;
  private static final double TOLERANCE_PRICE = 1.0E-8;
  
  
  @Test
  public void benchmarkNominalTreasuryBondEvaluation() {
	  
	// On-the-run Nominal Treasury bonds valued using USGOVT curve
	System.out.println("");
	System.out.println("Nominal Treasury bonds : ON THE RUN - UST_20241115");
	System.out.println("");

	
	// Present value
	MultipleCurrencyAmount pvComputedIs = UST_TRA.accept(PVIssuerC, ISSUER_GOVT);
    System.out.println("On-the-run PV Computed from Curves: " + pvComputedIs.getAmount(USD));

	// Sensitivities to curve points
	MultipleCurrencyParameterSensitivity pvpsComputed =
			MQSBC.fromInstrument(UST_TRA, ISSUER_GOVT, BLOCK).multipliedBy(BP1);
	System.out.println("Sensitivities (OIS/USGOVT/TIPS): " + pvpsComputed);
    System.out.print(pvpsComputed.getAllNamesCurrency());
	ExportUtils.consolePrint(pvpsComputed,ISSUER_GOVT.getMulticurveProvider());
	
	// Compute accrued
	double accruedInterest = UST_TRA.getBondStandard().getAccruedInterest()*10000000;
	System.out.println("Accrued interest to standard settlement date: " + accruedInterest);
		
	// Compute yield from curves
	double yield = METHOD_BOND_SEC.yieldFromCurves(UST_TRA.getBondStandard(), ISSUER_GOVT);
	System.out.println("Yield from curves [OIS + USGOVT]: " + yield);
		
	// Compute clean price from yield
	double price = METHOD_BOND_SEC.cleanPriceFromYield(UST_TRA.getBondStandard(), yield);
	System.out.println("Price from yield: " + price);
	
	
  }
  
  @Test
  public void offTheRunNominalTreasuryBondEvaluation() {
	  
	// Off-the-run Nominal Treasury bonds valued using USGOVT curve
	System.out.println("");
	System.out.println("Nominal Treasury bonds : OFF THE RUN - UST_20240815");
	System.out.println("");
    
    // Present value
    MultipleCurrencyAmount pvComputedIs1 = UST_TRA1.accept(PVIssuerC, ISSUER_GOVT);
    System.out.println("Off-the-run PV Computed from Curves: " + pvComputedIs1.getAmount(USD));

	// Sensitivities to curve points
	MultipleCurrencyParameterSensitivity pvpsComputed1 =
			MQSBC.fromInstrument(UST_TRA1, ISSUER_GOVT, BLOCK).multipliedBy(BP1);
	System.out.println("Sensitivities (OIS/USGOVT/TIPS): " + pvpsComputed1);
	ExportUtils.consolePrint(pvpsComputed1,ISSUER_GOVT.getMulticurveProvider());
  
	
	// Compute accrued
	double accruedInterest = UST_TRA1.getBondStandard().getAccruedInterest()*10000000;
	System.out.println("Accrued interest to standard settlement date: " + accruedInterest);
		
	// Compute yield from curves
	double yield = METHOD_BOND_SEC.yieldFromCurves(UST_TRA1.getBondStandard(), ISSUER_GOVT);
	System.out.println("Yield from curves [OIS + USGOVT]: " + yield);
		
	// Compute clean price from yield
	double price = METHOD_BOND_SEC.cleanPriceFromYield(UST_TRA1.getBondStandard(), yield);
	System.out.println("Price from yield: " + price);

  }
  

  @Test  
  public void benchmarkTIPSEvaluation() {
	  
	  // Inflation-linked bonds PV and PV01 off several curve structures
	  System.out.println("");
	  System.out.println("Inflation-linked bonds : ON THE RUN (2024-07-15)");
	  System.out.println("");

	  // Using curves: OIS + USGOVT + TIPS
	  // - PV
	  MultipleCurrencyAmount pv1 = TIPS_24_1_TRA.accept(PVInflIssuerC, INFL_ISSUER_GOVT_3);
	  System.out.println("On-the-run TIPS from [OIS + USGOVT + TIPS] PV: " + pv1.getAmount(USD));
	  // - Sensitivity
	  MultipleCurrencyParameterSensitivity pvpsComputed1 =
			  MQISBC.fromInstrument(TIPS_24_1_TRA, INFL_ISSUER_GOVT_3, INFL_ISSUER_GOVT_3_BLOCK).multipliedBy(BP1);
	  System.out.println("Sensitivities [OIS + USGOVT + TIPS]: " + pvpsComputed1);
	  ExportUtils.consolePrint(pvpsComputed1,INFL_ISSUER_GOVT_3);

    // Using curves: OIS + USGOVT + TIPS + Hedged
    // - PV
    MultipleCurrencyAmount pv4 = TIPS_24_1_TRA.accept(PVInflIssuerC, INFL_ISSUER_GOVT_4);
    System.out.println("On-the-run TIPS from [OIS + USGOVT + TIPS + Hedged] PV: " + pv4.getAmount(USD));
    // - Sensitivity
    MultipleCurrencyParameterSensitivity pvpsComputed4 =
        MQISBC.fromInstrument(TIPS_24_1_TRA, INFL_ISSUER_GOVT_4, INFL_ISSUER_GOVT_4_BLOCK).multipliedBy(BP1);
    System.out.println("Sensitivities [OIS + USGOVT + TIPS + Hedged]: " + pvpsComputed1);
    ExportUtils.consolePrint(pvpsComputed1,INFL_ISSUER_GOVT_4);

	  // Using curves: OIS + USGOVT + USCPI
	  // - PV
	  MultipleCurrencyAmount pv2 = TIPS_24_1_TRA.accept(PVInflIssuerC, INFL_ISSUER_GOVT_1);
	  System.out.println("On-the-run TIPS from [OIS + USGOVT + USCPI] PV: " + pv2.getAmount(USD));
	  // - Sensitivity
	  MultipleCurrencyParameterSensitivity pvpsComputed2 =
			  MQISBC.fromInstrument(TIPS_24_1_TRA, INFL_ISSUER_GOVT_1, INFL_ISSUER_GOVT_1_BLOCK).multipliedBy(BP1);
	  System.out.println("Sensitivities [OIS + USGOVT + USCPI]: " + pvpsComputed2);
	  ExportUtils.consolePrint(pvpsComputed2,INFL_ISSUER_GOVT_1);

	  
	  // Using curves: OIS + USGOVT + USCPI + SEASONALITY
	  // - PV
	  MultipleCurrencyAmount pv3 = TIPS_24_1_TRA.accept(PVInflIssuerC, INFL_ISSUER_GOVT_2);
	  System.out.println("On-the-run TIPS from [OIS + USGOVT + USCPI + SEASONALITY] PV: " + pv3.getAmount(USD));
	  // - Sensitivity
	  MultipleCurrencyParameterSensitivity pvpsComputed3 =
			  MQISBC.fromInstrument(TIPS_24_1_TRA, INFL_ISSUER_GOVT_2, INFL_ISSUER_GOVT_2_BLOCK).multipliedBy(BP1);
	  System.out.println("Sensitivities [OIS + USGOVT + USCPI + SEASONALITY]: " + pvpsComputed3);
	  ExportUtils.consolePrint(pvpsComputed3,INFL_ISSUER_GOVT_1);

		
      // Index ratio to T+1
	  double ratio = ((CouponFixed) TIPS_24_1_TRA.getBondTransaction().getSettlement()).getFixedRate();
	  System.out.println("Index ratio to standard settlement date: " + ratio);
	  
	  // Compute accrued
	  double accruedInterest = TIPS_24_1_TRA.getBondStandard().getAccruedInterest() * 10000000;
	  System.out.println("Accrued interest to standard settlement date: " + accruedInterest);
		
	  // Compute real yield
	  double yieldReal = METHOD_CAPIND_BOND_SEC.yieldRealFromCurves(TIPS_24_1_TRA.getBondStandard(), INFL_ISSUER_GOVT_3);
	  System.out.println("Real Yield from PV [OIS + USGOVT + TIPS]: " + yieldReal);
		
	  // Compute real price
	  double cleanRealPrice = METHOD_CAPIND_BOND_SEC.cleanRealPriceFromCurves(TIPS_24_1_TRA.getBondStandard(), INFL_ISSUER_GOVT_3);
	  System.out.println("Clean real price from PV [OIS + USGOVT + TIPS]: " + cleanRealPrice);
		
  }
  
  @Test  
  public void offTheRunTIPSEvaluation() {
	  
	  // Inflation-linked bonds PV and PV01 off several curve structures
	  System.out.println("");
	  System.out.println("Inflation-linked bonds : OFF THE RUN (2043-02-15)");
	  System.out.println("");
	  

	  // Using curves: OIS + USGOVT + TIPS
	  // - PV
	  MultipleCurrencyAmount pv1 = TIPS_43_1_TRA.accept(PVInflIssuerC, INFL_ISSUER_GOVT_3);
	  System.out.println("Off-the-run TIPS from [OIS + USGOVT + TIPS] PV: " + pv1.getAmount(USD));
	  // - Sensitivity
	  MultipleCurrencyParameterSensitivity pvpsComputed1 =
			  MQISBC.fromInstrument(TIPS_43_1_TRA, INFL_ISSUER_GOVT_3, INFL_ISSUER_GOVT_3_BLOCK).multipliedBy(BP1);
	  System.out.println("Sensitivities [OIS + USGOVT + TIPS]: " + pvpsComputed1);
	  ExportUtils.consolePrint(pvpsComputed1,INFL_ISSUER_GOVT_3);

    // Using curves: OIS + USGOVT + TIPS + Hedged
    // - PV
    MultipleCurrencyAmount pv4 = TIPS_43_1_TRA.accept(PVInflIssuerC, INFL_ISSUER_GOVT_4);
    System.out.println("Off-the-run TIPS from [OIS + USGOVT + TIPS + Hedged] PV: " + pv4.getAmount(USD));
    // - Sensitivity
    MultipleCurrencyParameterSensitivity pvpsComputed4 =
        MQISBC.fromInstrument(TIPS_43_1_TRA, INFL_ISSUER_GOVT_4, INFL_ISSUER_GOVT_4_BLOCK).multipliedBy(BP1);
    System.out.println("Sensitivities [OIS + USGOVT + TIPS + Hedged]: " + pvpsComputed4);
    ExportUtils.consolePrint(pvpsComputed1,INFL_ISSUER_GOVT_4);


	  // Using curves: OIS + USGOVT + USCPI
	  // - PV
	  MultipleCurrencyAmount pv2 = TIPS_43_1_TRA.accept(PVInflIssuerC, INFL_ISSUER_GOVT_1);
	  System.out.println("Off-the-run TIPS from [OIS + USGOVT + USCPI] PV: " + pv2.getAmount(USD));
	  // - Sensitivity
	  MultipleCurrencyParameterSensitivity pvpsComputed2 =
			  MQISBC.fromInstrument(TIPS_43_1_TRA, INFL_ISSUER_GOVT_1, INFL_ISSUER_GOVT_1_BLOCK).multipliedBy(BP1);
	  System.out.println("Sensitivities [OIS + USGOVT + USCPI]: " + pvpsComputed2);
	  ExportUtils.consolePrint(pvpsComputed2,INFL_ISSUER_GOVT_1);

	  
	  // Using curves: OIS + USGOVT + USCPI + SEASONALITY
	  // - PV
	  MultipleCurrencyAmount pv3 = TIPS_43_1_TRA.accept(PVInflIssuerC, INFL_ISSUER_GOVT_2);
	  System.out.println("Off-the-run TIPS from [OIS + USGOVT + USCPI + SEASONALITY] PV: " + pv3.getAmount(USD));
	  // - Sensitivity
	  MultipleCurrencyParameterSensitivity pvpsComputed3 =
			  MQISBC.fromInstrument(TIPS_43_1_TRA, INFL_ISSUER_GOVT_2, INFL_ISSUER_GOVT_2_BLOCK).multipliedBy(BP1);
	  System.out.println("Sensitivities [OIS + USGOVT + USCPI + SEASONALITY]: " + pvpsComputed3);
	  ExportUtils.consolePrint(pvpsComputed3,INFL_ISSUER_GOVT_2);
	  
		
      // Index ratio to T+1
	  double ratio = ((CouponFixed) TIPS_43_1_TRA.getBondTransaction().getSettlement()).getFixedRate();
	  System.out.println("Index ratio to standard settlement date: " + ratio);
	  
	  // Compute accrued
	  double accruedInterest = TIPS_43_1_TRA.getBondStandard().getAccruedInterest()*10000000;
	  System.out.println("Accrued interest to standard settlement date: " + accruedInterest);
		
	  // Compute real yield
	  double yieldReal = METHOD_CAPIND_BOND_SEC.yieldRealFromCurves(TIPS_43_1_TRA.getBondStandard(), INFL_ISSUER_GOVT_3);
	  System.out.println("Real Yield from PV [OIS + USGOVT + TIPS]: " + yieldReal);
		
	  // Compute real price
	  double cleanRealPrice = METHOD_CAPIND_BOND_SEC.cleanRealPriceFromCurves(TIPS_43_1_TRA.getBondStandard(), INFL_ISSUER_GOVT_3);
	  System.out.println("Clean real price from PV [OIS + USGOVT + TIPS]: " + cleanRealPrice);
	   
  }





  public static SwapFixedInflationZeroCouponDefinition[] getHedgeCurveBundle(
      ZonedDateTime calibrationDate, ParameterInflationProviderInterface standardCurveBundle) {

    /** Tenors for the HICP USD curve */
    final Period[] HICP_USD_TENOR = new Period[] {
        Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
        Period.ofYears(6), Period.ofYears(7), Period.ofYears(8), Period.ofYears(9), Period.ofYears(10),
        Period.ofYears(12), Period.ofYears(15), Period.ofYears(20), Period.ofYears(25), Period.ofYears(30) };
    final GeneratorAttributeIR[] HICP_USD_ATTR = new GeneratorAttributeIR[HICP_USD_TENOR.length];

      for (int loopins = 0; loopins < HICP_USD_TENOR.length; loopins++) {
        HICP_USD_ATTR[loopins] = new GeneratorAttributeIR(HICP_USD_TENOR[loopins]);
      }


    InstrumentDerivativeVisitorAdapter<ParameterInflationProviderInterface, Double> target= PSIMQDC;;
    InstrumentDerivativeVisitorAdapter<ParameterInflationProviderInterface, InflationSensitivity> targetSensitivity= PSIMQCSDC;

    double[] initRates = new double[HICP_USD_TENOR.length];
    double[] calcRates = new double[HICP_USD_TENOR.length];
    double notional = 1000000;

    for(int i=0;i<HICP_USD_TENOR.length;i++) {
      SwapFixedInflationZeroCouponDefinition definition=GENERATOR_ZCINFLATION_US.generateInstrument(calibrationDate, initRates[i], notional, HICP_USD_ATTR[i]);
      InstrumentDerivative derivative=definition.toDerivative(calibrationDate,
                                                   new ZonedDateTimeDoubleTimeSeries[] {HTS_EMPTY, HTS_CPI});

      calcRates[i]=derivative.accept(target, standardCurveBundle);
    }

    SwapFixedInflationZeroCouponDefinition[] definitions= new SwapFixedInflationZeroCouponDefinition[HICP_USD_TENOR.length];

    for(int i=0;i<HICP_USD_TENOR.length;i++) {
      definitions[i]=GENERATOR_ZCINFLATION_US.generateInstrument(calibrationDate, calcRates[i], notional, HICP_USD_ATTR[i]);
    }

    return definitions;
  }
}
