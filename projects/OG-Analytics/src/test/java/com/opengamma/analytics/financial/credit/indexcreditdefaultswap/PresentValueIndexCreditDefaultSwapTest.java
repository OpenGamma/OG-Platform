/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.indexcreditdefaultswap;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.CreditSpreadTenors;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.indexcreditdefaultswap.definition.CDSIndex;
import com.opengamma.analytics.financial.credit.indexcreditdefaultswap.definition.IndexCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.indexcreditdefaultswap.pricing.PresentValueIndexCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.obligormodel.CreditRating;
import com.opengamma.analytics.financial.credit.obligormodel.CreditRatingFitch;
import com.opengamma.analytics.financial.credit.obligormodel.CreditRatingMoodys;
import com.opengamma.analytics.financial.credit.obligormodel.CreditRatingStandardAndPoors;
import com.opengamma.analytics.financial.credit.obligormodel.Region;
import com.opengamma.analytics.financial.credit.obligormodel.Sector;
import com.opengamma.analytics.financial.credit.obligormodel.definition.Obligor;
import com.opengamma.analytics.financial.credit.underlyingpool.definition.UnderlyingPool;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Class containing methods for the valuation of a vanilla index CDS
 */
public class PresentValueIndexCreditDefaultSwapTest {

  //--------------------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Make sure that all the input arguments have been error checked
  // TODO : Add the credit spread term structures

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Flag to control if any test results are output to the console
  private static final boolean outputResults = false;

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Define the composition of the underlying pool

  private static final int numberOfObligors = 3;
  private static final int numberOfTenors = 4;

  private static final Obligor[] obligors = new Obligor[numberOfObligors];

  private static final double[] notionals = new double[numberOfObligors];
  private static final double[] coupons = new double[numberOfObligors];
  private static final double[] recoveryRates = new double[numberOfObligors];
  private static final double[] obligorWeights = new double[numberOfObligors];

  private static final Currency[] currency = new Currency[numberOfObligors];
  private static final DebtSeniority[] debtSeniority = new DebtSeniority[numberOfObligors];
  private static final RestructuringClause[] restructuringClause = new RestructuringClause[numberOfObligors];

  private static final CreditSpreadTenors[] creditSpreadTenors = new CreditSpreadTenors[numberOfTenors];
  private static final double[][] spreadTermStructures = new double[numberOfObligors][numberOfTenors];

  private static final YieldCurve[] yieldCurve = new YieldCurve[numberOfObligors];

  private static final double[] obligorNotionals = {10000000.0, 10000000.0, 10000000.0 };
  private static final double[] obligorCoupons = {100.0, 100.0, 100.0 };
  private static final double[] obligorRecoveryRates = {0.40, 0.40, 0.40 };
  private static final double[] obligorIndexWeights = {1.0 / numberOfObligors, 1.0 / numberOfObligors, 1.0 / numberOfObligors };

  private static final Currency[] obligorCurrencies = {Currency.USD, Currency.USD, Currency.EUR };
  private static final DebtSeniority[] obligorDebtSeniorities = {DebtSeniority.SENIOR, DebtSeniority.SENIOR, DebtSeniority.SENIOR };
  private static final RestructuringClause[] obligorRestructuringClauses = {RestructuringClause.NORE, RestructuringClause.NORE, RestructuringClause.MODRE };

  private static final CreditSpreadTenors[] obligorCreditSpreadTenors = {CreditSpreadTenors._3Y, CreditSpreadTenors._5Y, CreditSpreadTenors._7Y, CreditSpreadTenors._10Y };

  private static final YieldCurve[] obligorYieldCurves = {null, null, null };

  private static final String[] obligorTickers = {"MSFT", "IBM", "BT" };
  private static final String[] obligorShortName = {"Microsoft", "International Business Machine", "British Telecom" };
  private static final String[] obligorREDCode = {"ABC123", "XYZ321", "123ABC" };

  private static final CreditRating[] obligorCompositeRating = {CreditRating.AA, CreditRating.AA, CreditRating.AA };
  private static final CreditRating[] obligorImpliedRating = {CreditRating.AA, CreditRating.AA, CreditRating.AA };
  private static final CreditRatingMoodys[] obligorCreditRatingMoodys = {CreditRatingMoodys.AA, CreditRatingMoodys.AA, CreditRatingMoodys.AA };
  private static final CreditRatingStandardAndPoors[] obligorCreditRatingStandardAndPoors = {CreditRatingStandardAndPoors.AA, CreditRatingStandardAndPoors.AA, CreditRatingStandardAndPoors.AA };
  private static final CreditRatingFitch[] obligorCreditRatingFitch = {CreditRatingFitch.AA, CreditRatingFitch.AA, CreditRatingFitch.AA };

  private static final boolean[] obligorHasDefaulted = {false, false, false };

  private static final Sector[] obligorSector = {Sector.INDUSTRIALS, Sector.INDUSTRIALS, Sector.INDUSTRIALS };
  private static final Region[] obligorRegion = {Region.NORTHAMERICA, Region.NORTHAMERICA, Region.EUROPE };
  private static final String[] obligorCountry = {"United States", "United States", "United Kingdom" };

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // The index CDS contract parameters

  private static final BuySellProtection buySellProtection = BuySellProtection.BUY;

  private static final String protectionBuyerTicker = "BARC";
  private static final String protectionBuyerShortName = "Barclays";
  private static final String protectionBuyerREDCode = "ABC123";

  private static final String protectionSellerTicker = "GS";
  private static final String protectionSellerShortName = "Goldman Sachs";
  private static final String protectionSellerREDCode = "XYZ321";

  private static final CreditRating protectionBuyerCompositeRating = CreditRating.AA;
  private static final CreditRating protectionBuyerImpliedRating = CreditRating.A;

  private static final CreditRating protectionSellerCompositeRating = CreditRating.AA;
  private static final CreditRating protectionSellerImpliedRating = CreditRating.A;

  private static final CreditRatingMoodys protectionBuyerCreditRatingMoodys = CreditRatingMoodys.AA;
  private static final CreditRatingStandardAndPoors protectionBuyerCreditRatingStandardAndPoors = CreditRatingStandardAndPoors.A;
  private static final CreditRatingFitch protectionBuyerCreditRatingFitch = CreditRatingFitch.AA;

  private static final boolean protectionBuyerHasDefaulted = false;

  private static final CreditRatingMoodys protectionSellerCreditRatingMoodys = CreditRatingMoodys.AA;
  private static final CreditRatingStandardAndPoors protectionSellerCreditRatingStandardAndPoors = CreditRatingStandardAndPoors.A;
  private static final CreditRatingFitch protectionSellerCreditRatingFitch = CreditRatingFitch.AA;

  private static final boolean protectionSellerHasDefaulted = false;

  private static final Sector protectionBuyerSector = Sector.FINANCIALS;
  private static final Region protectionBuyerRegion = Region.EUROPE;
  private static final String protectionBuyerCountry = "United Kingdom";

  private static final Sector protectionSellerSector = Sector.FINANCIALS;
  private static final Region protectionSellerRegion = Region.NORTHAMERICA;
  private static final String protectionSellerCountry = "United States";

  private static final CDSIndex cdsIndex = CDSIndex.BESPOKE;
  private static final int cdsIndexSeries = 18;
  private static final String cdsIndexVersion = "V1";

  private static final Currency indexCurrency = Currency.USD;
  private static final Calendar calendar = new MondayToFridayCalendar("TestCalendar");

  private static final ZonedDateTime startDate = DateUtils.getUTCDate(2008, 3, 20);
  private static final ZonedDateTime effectiveDate = startDate;
  private static final ZonedDateTime settlementDate = startDate.plusDays(3);
  private static final ZonedDateTime maturityDate = DateUtils.getUTCDate(2013, 3, 20);

  private static final StubType stubType = StubType.FRONTSHORT;
  private static final PeriodFrequency couponFrequency = PeriodFrequency.QUARTERLY;
  private static final DayCount daycountFractionConvention = DayCountFactory.INSTANCE.getDayCount("ACT/360");
  private static final BusinessDayConvention businessdayAdjustmentConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");

  private static final boolean immAdjustMaturityDate = false;
  private static final boolean adjustEffectiveDate = false;
  private static final boolean adjustSettlementDate = false;
  private static final boolean adjustMaturityDate = false;

  private static final boolean includeAccruedPremium = false;
  private static final boolean protectionStart = true;

  private static final double notional = 10000000.0;
  private static final double upfrontPayment = 0.1;
  private static final double indexCoupon = 500.0;
  private static final double indexSpread = 500.0;

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Construct the obligors party to the index CDS contract

  private static final Obligor indexProtectionBuyer = new Obligor(
      protectionBuyerTicker,
      protectionBuyerShortName,
      protectionBuyerREDCode,
      protectionBuyerCompositeRating,
      protectionBuyerImpliedRating,
      protectionBuyerCreditRatingMoodys,
      protectionBuyerCreditRatingStandardAndPoors,
      protectionBuyerCreditRatingFitch,
      protectionBuyerHasDefaulted,
      protectionBuyerSector,
      protectionBuyerRegion,
      protectionBuyerCountry);

  private static final Obligor indexProtectionSeller = new Obligor(
      protectionSellerTicker,
      protectionSellerShortName,
      protectionSellerREDCode,
      protectionSellerCompositeRating,
      protectionSellerImpliedRating,
      protectionSellerCreditRatingMoodys,
      protectionSellerCreditRatingStandardAndPoors,
      protectionSellerCreditRatingFitch,
      protectionSellerHasDefaulted,
      protectionSellerSector,
      protectionSellerRegion,
      protectionSellerCountry);

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Build the underlying pool

  private static final UnderlyingPool dummyPool = constructPool();

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  //Construct the index

  private static final IndexCreditDefaultSwapDefinition dummyIndex = new IndexCreditDefaultSwapDefinition(
      buySellProtection,
      indexProtectionBuyer,
      indexProtectionSeller,
      dummyPool,
      cdsIndex,
      cdsIndexSeries,
      cdsIndexVersion,
      indexCurrency,
      calendar,
      startDate,
      effectiveDate,
      settlementDate,
      maturityDate,
      stubType,
      couponFrequency,
      daycountFractionConvention,
      businessdayAdjustmentConvention,
      immAdjustMaturityDate,
      adjustEffectiveDate,
      adjustSettlementDate,
      adjustMaturityDate,
      includeAccruedPremium,
      protectionStart,
      notional,
      upfrontPayment,
      indexCoupon,
      indexSpread);

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test
  public void testCDSIndexPresentValue() {

    // Construct an index present value calculator object
    final PresentValueIndexCreditDefaultSwap indexPresentValue = new PresentValueIndexCreditDefaultSwap();

    // Calculate the value of the index
    final double presentValue = indexPresentValue.getPresentValueIndexCreditDefaultSwap(dummyIndex);

    if (outputResults) {
      System.out.println("Index Present Value = " + presentValue);
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Initialise the obligors in the pool
  private static void initialiseObligorsInPool() {

    // Loop over each of the obligors in the pool
    for (int i = 0; i < numberOfObligors; i++) {

      // Build obligor i
      final Obligor obligor = new Obligor(
          obligorTickers[i],
          obligorShortName[i],
          obligorREDCode[i],
          obligorCompositeRating[i],
          obligorImpliedRating[i],
          obligorCreditRatingMoodys[i],
          obligorCreditRatingStandardAndPoors[i],
          obligorCreditRatingFitch[i],
          obligorHasDefaulted[i],
          obligorSector[i],
          obligorRegion[i],
          obligorCountry[i]);

      // Assign obligor i
      obligors[i] = obligor;

      // Assign the currency of obligor i
      currency[i] = obligorCurrencies[i];

      // Assign the debt seniority of obligor i
      debtSeniority[i] = obligorDebtSeniorities[i];

      // Assign the restructuring clause of obligor i
      restructuringClause[i] = obligorRestructuringClauses[i];

      // Assign the term structure of credit spreads for obligor i
      for (int j = 0; j < numberOfTenors; j++) {
        spreadTermStructures[i][j] = i * j;
      }

      // Assign the notional amount for obligor i
      notionals[i] = obligorNotionals[i];

      // Assign the coupon for obligor i
      coupons[i] = obligorCoupons[i];

      // Assign the recovery rate for obligor i
      recoveryRates[i] = obligorRecoveryRates[i];

      // Assign the weight of obligor i in the index
      obligorWeights[i] = obligorIndexWeights[i];

      yieldCurve[i] = obligorYieldCurves[i];
    }

    // Assign the credit spread tenors
    for (int j = 0; j < numberOfTenors; j++) {
      creditSpreadTenors[j] = obligorCreditSpreadTenors[j];
    }
  }

  //--------------------------------------------------------------------------------------------------------------------------------------------------

  // Build the underlying pool
  private static final UnderlyingPool constructPool() {

    // Initialise the obligors in the pool
    initialiseObligorsInPool();

    // Call the pool constructor
    final UnderlyingPool underlyingPool = new UnderlyingPool(
        obligors,
        currency,
        debtSeniority,
        restructuringClause,
        creditSpreadTenors,
        spreadTermStructures,
        notionals,
        coupons,
        recoveryRates,
        obligorWeights,
        yieldCurve);

    return underlyingPool;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

}
