/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedSecurityDefinitionBrazilTest;
import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyGearingDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.datasets.StandardTimeSeriesInflationDataSets;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurveSimple;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

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
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  
  private static final double TOLERANCE_PV = 1.0E-4;
  
  /** Curves */
  private static final LegalEntityFilter<LegalEntity> META = new LegalEntityShortName();
  private static final String BR_GOVT_NAME = NTNB_SECURITY_DEFINITION.getIssuer();
  private static final Pair<Object, LegalEntityFilter<LegalEntity>> ISSUER_BR_GOVT = Pairs.of((Object) BR_GOVT_NAME, META);
  private static final String BRL_DSC_NAME = "BRL-DSC";
  private static final YieldAndDiscountCurve CURVE_DSC = new YieldCurve(BRL_DSC_NAME, ConstantDoublesCurve.from(0.10, BRL_DSC_NAME));
  private static final String BRL_GOVT_NAME = "BRL-GOVT";
  private static final YieldAndDiscountCurve CURVE_GOVT = new YieldCurve(BRL_GOVT_NAME, ConstantDoublesCurve.from(0.11, BRL_GOVT_NAME));
  private static final String BRL_IPCA_NAME = "BRL-ZCINFL-IPCA";
  private static final double PRICE_INDEX_SEPTEMBER = 3991.24;
  private static final double[] INDEX_VALUE_BR = new double[] {PRICE_INDEX_SEPTEMBER, PRICE_INDEX_SEPTEMBER * (1+0.07), 
    PRICE_INDEX_SEPTEMBER * Math.pow(1.07, 40) };
  private static final double[] TIME_VALUE_BR = new double[] {-33/360, 1-33/360, 40-33/360 };
  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final InterpolatedDoublesCurve PRICE_CURVE_BR = 
      InterpolatedDoublesCurve.from(TIME_VALUE_BR, INDEX_VALUE_BR, LINEAR_FLAT, BRL_IPCA_NAME);
  private static final PriceIndexCurveSimple PRICE_INDEX_CURVE_BR = new PriceIndexCurveSimple(PRICE_CURVE_BR);
  private static final InflationIssuerProviderDiscount MARKET = new InflationIssuerProviderDiscount();
  static {
    MARKET.setCurve(Currency.BRL, CURVE_DSC);
    MARKET.setCurve(ISSUER_BR_GOVT, CURVE_GOVT);
    MARKET.setCurve(PRICE_INDEX_IPCA, PRICE_INDEX_CURVE_BR);
  }
  
  @Test
  public void presentValue() {
    MultipleCurrencyAmount pvStdComputed = METHOD_TRA.presentValue(NTNB_TRANSACTION_STD, MARKET);
    MultipleCurrencyAmount pvPasComputed = METHOD_TRA.presentValue(NTNB_TRANSACTION_PAS, MARKET);
    MultipleCurrencyAmount pvFwdComputed = METHOD_TRA.presentValue(NTNB_TRANSACTION_FWD, MARKET);
    Coupon settleStd = NTNB_TRANSACTION_STD.getBondTransaction().getSettlement();
    Coupon settleFwd = NTNB_TRANSACTION_FWD.getBondTransaction().getSettlement();
    MultipleCurrencyAmount settleStdPv = settleStd.accept(PVDC, MARKET).multipliedBy(TRADE_PRICE * QUANTITY);
    MultipleCurrencyAmount settleFwdPv = settleFwd.accept(PVDC, MARKET).multipliedBy(TRADE_PRICE * QUANTITY);
    assertEquals("BondCapitalIndexedTransactionDiscountingMethodBrazil: present Value", 
        pvStdComputed.getAmount(BRL) - settleStdPv.getAmount(BRL), 
        pvFwdComputed.getAmount(BRL) - settleFwdPv.getAmount(BRL), TOLERANCE_PV);
    assertEquals("BondCapitalIndexedTransactionDiscountingMethodBrazil: present Value", 
        pvStdComputed.getAmount(BRL) - settleStdPv.getAmount(BRL), 
        pvPasComputed.getAmount(BRL), TOLERANCE_PV);
    
  }
  
}
