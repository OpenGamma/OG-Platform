/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondDataSets;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsBondCurveGBP;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveGBP;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Tests on (fixed coupon) bonds with static data. Data available also in interface unit test and in snapshots.
 */
public class BondFixedTransactionDiscountingMethodE2ETest {

  private static final Currency GBP = Currency.GBP;
  // Curve calibrated on swaps (OIS)
  private static final ZonedDateTime REFERENCE_DATE_SWAPCURVE = DateUtils.getUTCDate(2014, 1, 22);
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_SWAP_PAIR = StandardDataSetsMulticurveGBP.getCurvesGBPSonia();
  private static final MulticurveProviderDiscount MULTICURVE_SWAP = MULTICURVE_SWAP_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_SWAP = MULTICURVE_SWAP_PAIR.getSecond();
  private static final Calendar LON = StandardDataSetsMulticurveGBP.calendarArray()[0];
  // Curve calibrated with bills and bonds.
  private static final ZonedDateTime REFERENCE_DATE_GOVTCURVE = DateUtils.getUTCDate(2014, 7, 11);
  private static final Pair<IssuerProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_GOVT_PAIR = StandardDataSetsBondCurveGBP.getCurvesGBPSoniaGovt();
  private static final IssuerProviderDiscount MULTICURVE_GOVT = MULTICURVE_GOVT_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_GOVT = MULTICURVE_SWAP_PAIR.getSecond();

  // Bond description
  private static final BondFixedSecurityDefinition UKT_800_20210607_SEC_DEF = BondDataSets.bondUKT800_20210607(1.0);
  private static final String NAME_ISSUER = UKT_800_20210607_SEC_DEF.getIssuer();
  private static final IssuerProviderDiscount ISSUER_SWAP = new IssuerProviderDiscount(MULTICURVE_SWAP);
  static {
    ISSUER_SWAP.setCurve(Pairs.of((Object) NAME_ISSUER, (LegalEntityFilter<LegalEntity>) new LegalEntityShortName()), MULTICURVE_SWAP.getCurve(GBP));
  }
  private static final double QUANTITY_UKT_800_20210607 = 10000;
  private static final ZonedDateTime TRADE_DATE_UKT_800_20210607 = DateUtils.getUTCDate(2014, 2, 7);
  private static final double TRADE_PRICE_800_20210607 = 0.99;
  private static final BondFixedTransactionDefinition UKT_800_20210607_TRA_DEF = new BondFixedTransactionDefinition(UKT_800_20210607_SEC_DEF,
      QUANTITY_UKT_800_20210607, TRADE_DATE_UKT_800_20210607, TRADE_PRICE_800_20210607);
  private static final BondFixedTransaction UKT_800_20210607_TRA_1 = UKT_800_20210607_TRA_DEF.toDerivative(REFERENCE_DATE_SWAPCURVE);
  private static final BondFixedTransaction UKT_800_20210607_TRA_2 = UKT_800_20210607_TRA_DEF.toDerivative(REFERENCE_DATE_GOVTCURVE);

  // Calculator and methods
  private static final PresentValueIssuerCalculator PVIC = PresentValueIssuerCalculator.getInstance();
  private static final BondTransactionDiscountingMethod METHOD_BOND_TRA = BondTransactionDiscountingMethod.getInstance();
  private static final BondSecurityDiscountingMethod METHOD_BOND_SEC = BondSecurityDiscountingMethod.getInstance();

  private static final double BOND_QUOTED_CLEAN_PRICE = 1.40;
  private static final double BOND_QUOTED_YIELD = 0.018;

  private static final double TOLERANCE_PV = 1.0E-4;
  private static final double TOLERANCE_YIELD = 1.0E-6;
  private static final double TOLERANCE_PRICE = 1.0E-6;

  // Curve calibrated on swaps (OIS)
  @Test
  public void presentValueCurveOIS() {
    double pvExpected = 4257.244165;
    MultipleCurrencyAmount pvComputed = UKT_800_20210607_TRA_1.accept(PVIC, ISSUER_SWAP);
    assertEquals("BondFixedTransactionDiscountingMethodE2ETest", pvComputed.getAmount(GBP), pvExpected, TOLERANCE_PV);
  }

  @Test
  public void yieldCurveOIS() {
    double yieldExpected = 0.01886776;
    double yieldComputed = METHOD_BOND_SEC.yieldFromCurves(UKT_800_20210607_TRA_1.getBondStandard(), ISSUER_SWAP);
    assertEquals("BondFixedTransactionDiscountingMethodE2ETest", yieldExpected, yieldComputed, TOLERANCE_YIELD);
  }

  @Test
  public void priceCurveOIS() {
    double priceExpected = 1.41864835; // Clean price
    double priceComputed = METHOD_BOND_SEC.cleanPriceFromCurves(UKT_800_20210607_TRA_1.getBondStandard(), ISSUER_SWAP);
    assertEquals("BondFixedTransactionDiscountingMethodE2ETest", priceExpected, priceComputed, TOLERANCE_PRICE);
  }

  @Test
  public void presentValuePrice() {
    double pvExpected = 4070.764983;
    MultipleCurrencyAmount pvComputed = METHOD_BOND_TRA.presentValueFromCleanPrice(UKT_800_20210607_TRA_1, ISSUER_SWAP, BOND_QUOTED_CLEAN_PRICE);
    assertEquals("BondFixedTransactionDiscountingMethodE2ETest", pvComputed.getAmount(GBP), pvExpected, TOLERANCE_PV);
  }

  @Test
  public void yieldPrice() {
    double yieldExpected = 0.02109311;
    double yieldComputed = METHOD_BOND_SEC.yieldFromCleanPrice(UKT_800_20210607_TRA_1.getBondStandard(), BOND_QUOTED_CLEAN_PRICE);
    assertEquals("BondFixedTransactionDiscountingMethodE2ETest", yieldExpected, yieldComputed, TOLERANCE_YIELD);
  }

  @Test
  public void presentValueYield() {
    double pvExpected = 4330.776443;
    MultipleCurrencyAmount pvComputed = METHOD_BOND_TRA.presentValueFromYield(UKT_800_20210607_TRA_1, ISSUER_SWAP, BOND_QUOTED_YIELD);
    assertEquals("BondFixedTransactionDiscountingMethodE2ETest", pvComputed.getAmount(GBP), pvExpected, TOLERANCE_PV);
  }

  @Test
  public void priceYield() {
    double priceExpected = 1.42600175; // Clean price
    double priceComputed = METHOD_BOND_SEC.cleanPriceFromYield(UKT_800_20210607_TRA_1.getBondStandard(), BOND_QUOTED_YIELD);
    assertEquals("BondFixedTransactionDiscountingMethodE2ETest", priceExpected, priceComputed, TOLERANCE_PRICE);
  }

  // Curve calibrated with bills and bonds.
  @Test
  public void presentValueCurveGovt() {
    double pvExpected = 4257.244165;
    MultipleCurrencyAmount pvComputed = UKT_800_20210607_TRA_2.accept(PVIC, MULTICURVE_GOVT);
    assertEquals("BondFixedTransactionDiscountingMethodE2ETest", pvComputed.getAmount(GBP), pvExpected, TOLERANCE_PV);
  }

}
