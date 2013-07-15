/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.indexcreditdefaultswap.pricing;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.indexcreditdefaultswap.definition.CDSIndex;
import com.opengamma.analytics.financial.credit.indexcreditdefaultswap.definition.IndexCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.obligor.CreditRating;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingFitch;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingMoodys;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingStandardAndPoors;
import com.opengamma.analytics.financial.credit.obligor.Region;
import com.opengamma.analytics.financial.credit.obligor.Sector;
import com.opengamma.analytics.financial.credit.obligor.definition.Obligor;
import com.opengamma.analytics.financial.credit.underlyingpool.UnderlyingPoolDummyPool;
import com.opengamma.analytics.financial.credit.underlyingpool.definition.UnderlyingPool;
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

  // TODO : Add the credit spread term structures

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Flag to control if any test results are output to the console
  private static final boolean outputResults = false;

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // The index CDS contract parameters

  private static final String indexName = "Bespoke_1";

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

  // Create a pool construction object
  private static final UnderlyingPoolDummyPool pool = new UnderlyingPoolDummyPool();

  // Build the underlying pool
  private static final UnderlyingPool dummyPool = pool.constructPool();

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Construct the index (from the pool and the other contract parameters)
  private static final IndexCreditDefaultSwapDefinition dummyIndex = new IndexCreditDefaultSwapDefinition(
      indexName,
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
      indexCoupon);

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test
  public void testCDSIndexPresentValue() {

    // Construct an index present value calculator object
    final PresentValueIndexCreditDefaultSwap indexPresentValue = new PresentValueIndexCreditDefaultSwap();

    // Calculate the value of the index
    final double presentValue = 0.0; //indexPresentValue.getPresentValueIndexCreditDefaultSwap(dummyIndex);

    if (outputResults) {
      System.out.println("Index Present Value = " + presentValue);
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------
}
