/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswapoption.definition;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.obligor.CreditRating;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingFitch;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingMoodys;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingStandardAndPoors;
import com.opengamma.analytics.financial.credit.obligor.Region;
import com.opengamma.analytics.financial.credit.obligor.Sector;
import com.opengamma.analytics.financial.credit.obligor.definition.Obligor;
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
 * Tests to verify the correct construction of a CDS swaption contract
 */
public class CreditDefaultSwapOptionDefinitionTest {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Add the date ordering tests

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Flag to control if any test results are output to the console
  private static final boolean outputResults = false;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // The CDS swaption contract parameters

  private static final BuySellProtection cdsSwaptionBuySellProtection = BuySellProtection.BUY;

  private static final String cdsSwaptionProtectionBuyerTicker = "BARC";
  private static final String cdsSwaptionProtectionBuyerShortName = "Barclays";
  private static final String cdsSwaptionProtectionBuyerREDCode = "ABC123";

  private static final String cdsSwaptionProtectionSellerTicker = "RBS";
  private static final String cdsSwaptionProtectionSellerShortName = "Royal Bank of Scotland";
  private static final String cdsSwaptionProtectionSellerREDCode = "XYZ321";

  private static final CreditRating cdsSwaptionProtectionBuyerCompositeRating = CreditRating.AA;
  private static final CreditRating cdsSwaptionProtectionBuyerImpliedRating = CreditRating.A;

  private static final CreditRating cdsSwaptionProtectionSellerCompositeRating = CreditRating.AA;
  private static final CreditRating cdsSwaptionProtectionSellerImpliedRating = CreditRating.A;

  private static final CreditRatingMoodys cdsSwaptionProtectionBuyerCreditRatingMoodys = CreditRatingMoodys.AA;
  private static final CreditRatingStandardAndPoors cdsSwaptionProtectionBuyerCreditRatingStandardAndPoors = CreditRatingStandardAndPoors.A;
  private static final CreditRatingFitch cdsSwaptionProtectionBuyerCreditRatingFitch = CreditRatingFitch.AA;

  private static final boolean cdsSwaptionProtectionBuyerHasDefaulted = false;

  private static final CreditRatingMoodys cdsSwaptionProtectionSellerCreditRatingMoodys = CreditRatingMoodys.AA;
  private static final CreditRatingStandardAndPoors cdsSwaptionProtectionSellerCreditRatingStandardAndPoors = CreditRatingStandardAndPoors.A;
  private static final CreditRatingFitch cdsSwaptionProtectionSellerCreditRatingFitch = CreditRatingFitch.AA;

  private static final boolean cdsSwaptionProtectionSellerHasDefaulted = false;

  private static final Sector cdsSwaptionProtectionBuyerSector = Sector.FINANCIALS;
  private static final Region cdsSwaptionProtectionBuyerRegion = Region.EUROPE;
  private static final String cdsSwaptionProtectionBuyerCountry = "United Kingdom";

  private static final Sector cdsSwaptionProtectionSellerSector = Sector.FINANCIALS;
  private static final Region cdsSwaptionProtectionSellerRegion = Region.EUROPE;
  private static final String cdsSwaptionProtectionSellerCountry = "United Kingdom";

  private static final Currency cdsSwaptionCurrency = Currency.EUR;

  private static final ZonedDateTime cdsSwaptionStartDate = DateUtils.getUTCDate(2008, 3, 20);
  private static final ZonedDateTime cdsSwaptionEffectiveDate = DateUtils.getUTCDate(2008, 3, 21);
  private static final ZonedDateTime cdsSwaptionExerciseDate = DateUtils.getUTCDate(2008, 3, 21);
  private static final ZonedDateTime cdsSwaptionMaturityDate = DateUtils.getUTCDate(2013, 3, 20);

  private static final double cdsSwaptionNotional = 10000000.0;
  private static final double cdsSwaptionStrike = 100.0;

  private static final boolean isKnockout = true;
  private static final boolean isPayer = true;
  private static final CDSOptionExerciseType cdsSwaptionExerciseType = CDSOptionExerciseType.EUROPEAN;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // The underlying CDS contract parameters

  private static final BuySellProtection buySellProtection = cdsSwaptionBuySellProtection;

  private static final String protectionBuyerTicker = "MSFT";
  private static final String protectionBuyerShortName = "Microsoft";
  private static final String protectionBuyerREDCode = "ABC123";

  private static final String protectionSellerTicker = "IBM";
  private static final String protectionSellerShortName = "International Business Machines";
  private static final String protectionSellerREDCode = "XYZ321";

  private static final String referenceEntityTicker = "BT";
  private static final String referenceEntityShortName = "British telecom";
  private static final String referenceEntityREDCode = "123ABC";

  private static final CreditRating protectionBuyerCompositeRating = CreditRating.AA;
  private static final CreditRating protectionBuyerImpliedRating = CreditRating.A;

  private static final CreditRating protectionSellerCompositeRating = CreditRating.AA;
  private static final CreditRating protectionSellerImpliedRating = CreditRating.A;

  private static final CreditRating referenceEntityCompositeRating = CreditRating.AA;
  private static final CreditRating referenceEntityImpliedRating = CreditRating.A;

  private static final CreditRatingMoodys protectionBuyerCreditRatingMoodys = CreditRatingMoodys.AA;
  private static final CreditRatingStandardAndPoors protectionBuyerCreditRatingStandardAndPoors = CreditRatingStandardAndPoors.A;
  private static final CreditRatingFitch protectionBuyerCreditRatingFitch = CreditRatingFitch.AA;

  private static final boolean protectionBuyerHasDefaulted = false;

  private static final CreditRatingMoodys protectionSellerCreditRatingMoodys = CreditRatingMoodys.AA;
  private static final CreditRatingStandardAndPoors protectionSellerCreditRatingStandardAndPoors = CreditRatingStandardAndPoors.A;
  private static final CreditRatingFitch protectionSellerCreditRatingFitch = CreditRatingFitch.AA;

  private static final boolean protectionSellerHasDefaulted = false;

  private static final CreditRatingMoodys referenceEntityCreditRatingMoodys = CreditRatingMoodys.AA;
  private static final CreditRatingStandardAndPoors referenceEntityCreditRatingStandardAndPoors = CreditRatingStandardAndPoors.A;
  private static final CreditRatingFitch referenceEntityCreditRatingFitch = CreditRatingFitch.AA;

  private static final boolean referenceEntityHasDefaulted = false;

  private static final Sector protectionBuyerSector = Sector.INDUSTRIALS;
  private static final Region protectionBuyerRegion = Region.NORTHAMERICA;
  private static final String protectionBuyerCountry = "United States";

  private static final Sector protectionSellerSector = Sector.INDUSTRIALS;
  private static final Region protectionSellerRegion = Region.NORTHAMERICA;
  private static final String protectionSellerCountry = "United States";

  private static final Sector referenceEntitySector = Sector.INDUSTRIALS;
  private static final Region referenceEntityRegion = Region.EUROPE;
  private static final String referenceEntityCountry = "United Kingdom";

  private static final Currency currency = Currency.USD;

  private static final DebtSeniority debtSeniority = DebtSeniority.SENIOR;
  private static final RestructuringClause restructuringClause = RestructuringClause.NORE;

  private static final Calendar calendar = new MondayToFridayCalendar("TestCalendar");

  private static final ZonedDateTime startDate = cdsSwaptionExerciseDate;
  private static final ZonedDateTime effectiveDate = startDate.plusDays(1);
  private static final ZonedDateTime maturityDate = cdsSwaptionMaturityDate;

  private static final StubType stubType = StubType.FRONTSHORT;
  private static final PeriodFrequency couponFrequency = PeriodFrequency.QUARTERLY;
  private static final DayCount daycountFractionConvention = DayCountFactory.INSTANCE.getDayCount("ACT/360");
  private static final BusinessDayConvention businessdayAdjustmentConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");

  private static final boolean immAdjustMaturityDate = false;
  private static final boolean adjustEffectiveDate = false;
  private static final boolean adjustMaturityDate = false;

  private static final double notional = cdsSwaptionNotional;
  private static final double recoveryRate = 0.40;
  private static final boolean includeAccruedPremium = false;
  private static final boolean protectionStart = true;

  private static final double parSpread = 100.0;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Construct the obligors party to the CDS swaption contract

  // The CDS swaption protection buyer
  private static final Obligor cdsSwaptionProtectionBuyer = new Obligor(
      cdsSwaptionProtectionBuyerTicker,
      cdsSwaptionProtectionBuyerShortName,
      cdsSwaptionProtectionBuyerREDCode,
      cdsSwaptionProtectionBuyerCompositeRating,
      cdsSwaptionProtectionBuyerImpliedRating,
      cdsSwaptionProtectionBuyerCreditRatingMoodys,
      cdsSwaptionProtectionBuyerCreditRatingStandardAndPoors,
      cdsSwaptionProtectionBuyerCreditRatingFitch,
      cdsSwaptionProtectionBuyerHasDefaulted,
      cdsSwaptionProtectionBuyerSector,
      cdsSwaptionProtectionBuyerRegion,
      cdsSwaptionProtectionBuyerCountry);

  // The CDS swaption protection seller
  private static final Obligor cdsSwaptionProtectionSeller = new Obligor(
      cdsSwaptionProtectionSellerTicker,
      cdsSwaptionProtectionSellerShortName,
      cdsSwaptionProtectionSellerREDCode,
      cdsSwaptionProtectionSellerCompositeRating,
      cdsSwaptionProtectionSellerImpliedRating,
      cdsSwaptionProtectionSellerCreditRatingMoodys,
      cdsSwaptionProtectionSellerCreditRatingStandardAndPoors,
      cdsSwaptionProtectionSellerCreditRatingFitch,
      cdsSwaptionProtectionSellerHasDefaulted,
      cdsSwaptionProtectionSellerSector,
      cdsSwaptionProtectionSellerRegion,
      cdsSwaptionProtectionSellerCountry);

  // The protection buyer in the underlying CDS which is exercised into
  private static final Obligor cdsProtectionBuyer = new Obligor(
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

  // The protection seller in the underlying CDS which is exercised into
  private static final Obligor cdsProtectionSeller = new Obligor(
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

  // The reference entity in the underlying CDS which is exercised into
  private static final Obligor cdsReferenceEntity = new Obligor(
      referenceEntityTicker,
      referenceEntityShortName,
      referenceEntityREDCode,
      referenceEntityCompositeRating,
      referenceEntityImpliedRating,
      referenceEntityCreditRatingMoodys,
      referenceEntityCreditRatingStandardAndPoors,
      referenceEntityCreditRatingFitch,
      referenceEntityHasDefaulted,
      referenceEntitySector,
      referenceEntityRegion,
      referenceEntityCountry);

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Construct CDS contract that is the underlying for the CDS swaption
  private static final LegacyVanillaCreditDefaultSwapDefinition underlyingCDS = new LegacyVanillaCreditDefaultSwapDefinition(
      buySellProtection,
      cdsProtectionBuyer,
      cdsProtectionSeller,
      cdsReferenceEntity,
      currency,
      debtSeniority,
      restructuringClause,
      calendar,
      startDate,
      effectiveDate,
      maturityDate,
      stubType,
      couponFrequency,
      daycountFractionConvention,
      businessdayAdjustmentConvention,
      immAdjustMaturityDate,
      adjustEffectiveDate,
      adjustMaturityDate,
      notional,
      recoveryRate,
      includeAccruedPremium,
      protectionStart,
      parSpread);

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Construct the CDS swaption object
  private static final CreditDefaultSwapOptionDefinition cdsSwaption = new CreditDefaultSwapOptionDefinition(
      cdsSwaptionBuySellProtection,
      cdsSwaptionProtectionBuyer,
      cdsSwaptionProtectionSeller,
      cdsSwaptionCurrency,
      cdsSwaptionStartDate,
      cdsSwaptionExerciseDate,
      cdsSwaptionNotional,
      cdsSwaptionStrike,
      isKnockout,
      isPayer,
      cdsSwaptionExerciseType,
      underlyingCDS);

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBuySellProtectionField() {

    new CreditDefaultSwapOptionDefinition(null, cdsSwaptionProtectionBuyer, cdsSwaptionProtectionSeller, cdsSwaptionCurrency, cdsSwaptionStartDate,
        cdsSwaptionExerciseDate, cdsSwaptionNotional, cdsSwaptionStrike, isKnockout, isPayer, cdsSwaptionExerciseType, underlyingCDS);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullProtectionBuyerField() {

    new CreditDefaultSwapOptionDefinition(cdsSwaptionBuySellProtection, null, cdsSwaptionProtectionSeller, cdsSwaptionCurrency, cdsSwaptionStartDate,
        cdsSwaptionExerciseDate, cdsSwaptionNotional, cdsSwaptionStrike, isKnockout, isPayer, cdsSwaptionExerciseType, underlyingCDS);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullProtectionSellerField() {

    new CreditDefaultSwapOptionDefinition(cdsSwaptionBuySellProtection, cdsSwaptionProtectionBuyer, null, cdsSwaptionCurrency, cdsSwaptionStartDate,
        cdsSwaptionExerciseDate, cdsSwaptionNotional, cdsSwaptionStrike, isKnockout, isPayer, cdsSwaptionExerciseType, underlyingCDS);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrencyField() {

    new CreditDefaultSwapOptionDefinition(cdsSwaptionBuySellProtection, cdsSwaptionProtectionBuyer, cdsSwaptionProtectionSeller, null, cdsSwaptionStartDate,
        cdsSwaptionExerciseDate, cdsSwaptionNotional, cdsSwaptionStrike, isKnockout, isPayer, cdsSwaptionExerciseType, underlyingCDS);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStartDateField() {

    new CreditDefaultSwapOptionDefinition(cdsSwaptionBuySellProtection, cdsSwaptionProtectionBuyer, cdsSwaptionProtectionSeller, cdsSwaptionCurrency, null,
        cdsSwaptionExerciseDate, cdsSwaptionNotional, cdsSwaptionStrike, isKnockout, isPayer, cdsSwaptionExerciseType, underlyingCDS);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExerciseDateField() {

    new CreditDefaultSwapOptionDefinition(cdsSwaptionBuySellProtection, cdsSwaptionProtectionBuyer, cdsSwaptionProtectionSeller, cdsSwaptionCurrency, cdsSwaptionStartDate,
        null, cdsSwaptionNotional, cdsSwaptionStrike, isKnockout, isPayer, cdsSwaptionExerciseType, underlyingCDS);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeNotionalField() {

    new CreditDefaultSwapOptionDefinition(cdsSwaptionBuySellProtection, cdsSwaptionProtectionBuyer, cdsSwaptionProtectionSeller, cdsSwaptionCurrency, cdsSwaptionStartDate,
        cdsSwaptionExerciseDate, -cdsSwaptionNotional, cdsSwaptionStrike, isKnockout, isPayer, cdsSwaptionExerciseType, underlyingCDS);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeStrikeField() {

    new CreditDefaultSwapOptionDefinition(cdsSwaptionBuySellProtection, cdsSwaptionProtectionBuyer, cdsSwaptionProtectionSeller, cdsSwaptionCurrency, cdsSwaptionStartDate,
        cdsSwaptionExerciseDate, cdsSwaptionNotional, -cdsSwaptionStrike, isKnockout, isPayer, cdsSwaptionExerciseType, underlyingCDS);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExerciseTypeField() {

    new CreditDefaultSwapOptionDefinition(cdsSwaptionBuySellProtection, cdsSwaptionProtectionBuyer, cdsSwaptionProtectionSeller, cdsSwaptionCurrency, cdsSwaptionStartDate,
        cdsSwaptionExerciseDate, cdsSwaptionNotional, cdsSwaptionStrike, isKnockout, isPayer, null, underlyingCDS);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingCDSField() {

    new CreditDefaultSwapOptionDefinition(cdsSwaptionBuySellProtection, cdsSwaptionProtectionBuyer, cdsSwaptionProtectionSeller, cdsSwaptionCurrency, cdsSwaptionStartDate,
        cdsSwaptionExerciseDate, cdsSwaptionNotional, cdsSwaptionStrike, isKnockout, isPayer, cdsSwaptionExerciseType, null);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------
}
