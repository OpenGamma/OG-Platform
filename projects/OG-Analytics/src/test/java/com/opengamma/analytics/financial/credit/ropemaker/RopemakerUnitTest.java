/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.ropemaker;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.bumpers.SpreadBumpType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.greeks.vanilla.CS01CreditDefaultSwap;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.obligor.CreditRating;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingFitch;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingMoodys;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingStandardAndPoors;
import com.opengamma.analytics.financial.credit.obligor.Region;
import com.opengamma.analytics.financial.credit.obligor.Sector;
import com.opengamma.analytics.financial.credit.obligor.definition.Obligor;
import com.opengamma.analytics.financial.credit.schedulegeneration.GenerateCreditDefaultSwapPremiumLegSchedule;
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
 * Unit test to compute the bucketed CS01 grid
 */
public class RopemakerUnitTest {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : How to convert the ZonedDateTime representing the yield curve spot date into a string
  // TODO : Probably need some error checking around the grid resolution parameter (easy to envisage the wrong number being input and the calc failing)

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // NOTE : We are assuming that CDS maturities in the CS01 grid use the 'Following' convention to adjust for non-business days

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Various daycount conventions used
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("ACT/360");

  // The business day convention for modifying CDS maturities in the CS01 grid
  private static final BusinessDayConvention cdsMaturityBusinessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("F");

  // Create a CDS schedule object
  private static final GenerateCreditDefaultSwapPremiumLegSchedule schedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

  // Create a CS01 calculator object
  private static final CS01CreditDefaultSwap cs01 = new CS01CreditDefaultSwap();

  // Flag to control if any test results are output to the console
  private static final boolean outputResults = false;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Define the CDS contract parameters

  // Date provided by Ropemaker is : Notional, par spread, recovery rate, curve level, valuation date

  private static final BuySellProtection buySellProtection = BuySellProtection.BUY;

  private static final String protectionBuyerTicker = "ABC";
  private static final String protectionBuyerShortName = "ABC Ltd";
  private static final String protectionBuyerREDCode = "ABC123";

  private static final String protectionSellerTicker = "XYZ";
  private static final String protectionSellerShortName = "XYZ Ltd";
  private static final String protectionSellerREDCode = "XYZ321";

  private static final String referenceEntityTicker = "HNG";
  private static final String referenceEntityShortName = "Hungary";
  private static final String referenceEntityREDCode = "XA78EOAB6";

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

  private static final Sector protectionBuyerSector = Sector.FINANCIALS;
  private static final Region protectionBuyerRegion = Region.EUROPE;
  private static final String protectionBuyerCountry = "United Kingdom";

  private static final Sector protectionSellerSector = Sector.FINANCIALS;
  private static final Region protectionSellerRegion = Region.EUROPE;
  private static final String protectionSellerCountry = "United Kingdom";

  private static final Sector referenceEntitySector = Sector.SOVEREIGN;
  private static final Region referenceEntityRegion = Region.EASTERNEUROPE;
  private static final String referenceEntityCountry = "Hungary";

  private static final Currency currency = Currency.USD;

  private static final DebtSeniority debtSeniority = DebtSeniority.SNRFOR;
  private static final RestructuringClause restructuringClause = RestructuringClause.CR;

  private static final Calendar calendar = new MondayToFridayCalendar("TestCalendar");

  private static final ZonedDateTime startDate = DateUtils.getUTCDate(2003, 7, 2);
  private static final ZonedDateTime valuationDate = DateUtils.getUTCDate(2013, 1, 30);
  private static final ZonedDateTime effectiveDate = valuationDate.plusDays(1);
  private static final ZonedDateTime maturityDate = DateUtils.getUTCDate(2014, 9, 22);

  private static final StubType stubType = StubType.FRONTSHORT;
  private static final PeriodFrequency couponFrequency = PeriodFrequency.QUARTERLY;
  private static final DayCount daycountFractionConvention = DayCountFactory.INSTANCE.getDayCount("ACT/360");
  private static final BusinessDayConvention businessdayAdjustmentConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");

  private static final boolean immAdjustMaturityDate = false;
  private static final boolean adjustEffectiveDate = false;
  private static final boolean adjustMaturityDate = false;

  private static final double notional = 1000000.0;
  private static final double recoveryRate = 0.25;
  private static final boolean includeAccruedPremium = true;
  private static final PriceType priceType = PriceType.CLEAN;
  private static final boolean protectionStart = true;

  private static final double parSpread = 100.0;

  private static final double[] curveLevel = {
      48.40,
      50.36,
      53.03,
      55.33,
      57.61,
      59.72,
      61.89,
      64.04,
      74.02,
      83.00,
      90.86,
      99.13,
      106.86,
      113.52,
      121.65,
      128.84,
      135.95,
      142.17,
      148.32,
      154.53,
      159.36,
      164.47,
      169.63,
      173.98,
      178.58,
      183.07,
      187.44,
      191.88,
      196.94,
      200.34,
      204.38,
      209.10,
      211.92,
      215.56,
      219.95,
      222.41,
      225.78,
      229.92,
      231.24,
      233.54,
      237.10,
      237.98,
      240.16,
      243.65,
      244.19,
      246.18,
      249.59,
      249.91,
      251.75,
      255.05,
      255.66,
      257.87,
      261.91,
      262.13,
      264.27,
      268.40,
      268.32,
      270.35,
      274.50,
      274.24,
      276.15,
      280.26,
      279.05,
      280.18,
      283.80,
      282.38,
      283.48,
      287.17,
      285.60,
      286.63,
      290.34,
      288.65,
      289.65,
      293.34,
      291.95,
      293.26,
      297.62,
      295.89,
      297.18,
      301.69,
      299.68,
      300.91,
      305.52,
      303.29,
      304.47,
      309.19,
      306.07,
      306.55,
      310.69,
      307.52,
      308.00,
      312.14,
      308.94,
      309.40,
      313.52,
      310.30,
      310.75,
      314.84,
      311.61,
      312.04,
      316.20,
      312.92,
      313.35,
      317.52,
      314.20,
      314.62,
      318.79,
      315.44,
      315.86,
      320.00,
      316.58,
      316.91,
      321.07,
      317.58,
      317.92,
      322.09,
      318.56,
      318.89,
      323.07,
      319.51,
      319.82,
      324.01 };

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Define the parameters for yield curve construction

  private static final ZonedDateTime baseDate = valuationDate;

  private static final int spotDays = 0;

  private static final String mmDCC = "Act/360";
  private static final String swapDCC = "30/360";
  private static final String floatDCC = "ACT/360";

  private static final String swapIvl = "6M";
  private static final String floatIvl = "3M";
  private static final String badDayConv = "M";
  private static final String holidays = "None";

  private static final String spotDate = "20130130";

  // The list of instruments, tenors and rates used to construct the yield curve
  // In most cases the instruments and tenors will not change; only the rates will change on a day-to-day basis

  /*
  List<ZeroCurve.CurvePoint> curvepoints = Arrays.asList(
     curvepoint(true, "1M,M", 0.002017),
     curvepoint(true, "2M,M", 0.002465),
     curvepoint(true, "3M,M", 0.003005),
     curvepoint(true, "6M,M", 0.004758),
     curvepoint(true, "9M,M", 0.006428),
     curvepoint(true, "1Y", 0.007955),
     curvepoint(false, "2Y", 0.004395),
     curvepoint(false, "3Y", 0.005820),
     curvepoint(false, "4Y", 0.007870),
     curvepoint(false, "5Y", 0.010295),
     curvepoint(false, "6Y", 0.012770),
     curvepoint(false, "7Y", 0.015090),
     curvepoint(false, "8Y", 0.017130),
     curvepoint(false, "9Y", 0.018995),
     curvepoint(false, "10Y", 0.020620),
     curvepoint(false, "12Y", 0.023295),
     curvepoint(false, "15Y", 0.025940),
     curvepoint(false, "20Y", 0.028195),
     curvepoint(false, "25Y", 0.029305),
     curvepoint(false, "30Y", 0.029995)
  );
   */

  // Call DK's code to create the curve from the native code
  //ZeroCurve zeroCurve = new ZeroCurve(curvepoints, mmDCC, swapIvl, floatIvl, swapDCC, floatDCC, badDayConv, holidays, spotDate);

  // Get this output from the ZeroCurve object
  ZonedDateTime[] yieldCurveDates = {
      zdt(2013, 2, 28, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2013, 3, 29, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2013, 4, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2013, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2013, 10, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2014, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2014, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2015, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2015, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2016, 1, 29, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2016, 7, 29, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2017, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2017, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2018, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2018, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2019, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2019, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2020, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2020, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2021, 1, 29, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2021, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2022, 1, 31, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2022, 7, 29, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2023, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2023, 7, 31, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2024, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2024, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2025, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2025, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2026, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2026, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2027, 1, 29, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2027, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2028, 1, 31, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2028, 7, 31, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2029, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2029, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2030, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2030, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2031, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2031, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2032, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2032, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2033, 1, 31, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2033, 7, 29, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2034, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2034, 7, 31, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2035, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2035, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2036, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2036, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2037, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2037, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2038, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2038, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2039, 1, 31, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2039, 7, 29, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2040, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2040, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2041, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2041, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2042, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2042, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2043, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC)
  };

  // Get this output from the ZeroCurve object
  double[] discreteRates = {
      0.0020469398963709473,
      0.002501864423114286,
      0.0030502347816789843,
      0.004829949174541159,
      0.006522625025034312,
      0.008065486111111131,
      0.005629511430584877,
      0.004393593770308283,
      0.005251835895630608,
      0.005831262552165626,
      0.007009657447744511,
      0.00790751215674671,
      0.009285775302585675,
      0.010395963833218537,
      0.011788998281891505,
      0.012970711608828876,
      0.014279610037949197,
      0.015421571541585745,
      0.01658207458964589,
      0.017604240836319698,
      0.018669926523961378,
      0.019633226952841705,
      0.020569229333792682,
      0.02144095801122866,
      0.022301830509302345,
      0.02308941586503166,
      0.023805136016763262,
      0.024468745526579464,
      0.025078024702453794,
      0.025650117661823568,
      0.02617148723394913,
      0.026661324750865356,
      0.027115106275981793,
      0.027545656339872937,
      0.027886810280336016,
      0.02820851780672462,
      0.02850757614158117,
      0.02879380095239914,
      0.02905934751569972,
      0.029314371920297067,
      0.02955173975943559,
      0.029780400828259612,
      0.029994997820663727,
      0.030202217081450364,
      0.030357410307428667,
      0.030510216400872148,
      0.030653538858346208,
      0.0307911211738785,
      0.030921187940242012,
      0.03104768086276155,
      0.031167477211878625,
      0.031283546549800034,
      0.03139307749474618,
      0.0314994147598717,
      0.03159461239355821,
      0.03168764827055548,
      0.03177428791948356,
      0.03186053357866836,
      0.031942286759037986,
      0.03202198665755751,
      0.03209764335645926,
      0.03217190908790313,
      0.032242493745427314,
      0.03231186272591617
  };

  // Construct the ISDA yield curve
  ISDADateCurve yieldCurve = new ISDADateCurve("IR_CURVE", baseDate, yieldCurveDates, discreteRates, ACT_365.getDayCountFraction(valuationDate, baseDate));

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Define the tenor buckets of the CS01 grid 

  ZonedDateTime[] bucketedCDSTenors = {
      zdt(2013, 9, 20, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2014, 3, 20, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2015, 3, 20, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2016, 3, 20, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2017, 3, 20, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2018, 3, 20, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2019, 3, 20, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2020, 3, 20, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2021, 3, 20, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2022, 3, 20, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2023, 3, 20, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2028, 3, 20, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2033, 3, 20, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2043, 3, 20, 0, 0, 0, 0, ZoneOffset.UTC)
  };

  // Specify the maturity of the CDS in the first row in the CS01 grid
  private static final ZonedDateTime firstMaturityDate = zdt(2013, 2, 20, 0, 0, 0, 0, ZoneOffset.UTC);

  // Specify the duration (in months) between CDS maturitues in the CS01 grid
  private static final int cs01GridResolution = 1;

  // The magnitude (but not direction) of bump to apply (in bps) to the CDS spreads
  private static final double spreadBump = 1.0;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Construct the obligors party to the CDS contract

  private static final Obligor protectionBuyer = new Obligor(
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

  private static final Obligor protectionSeller = new Obligor(
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

  private static final Obligor referenceEntity = new Obligor(
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

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Construct a CDS contract
  private static final LegacyVanillaCreditDefaultSwapDefinition cds = new LegacyVanillaCreditDefaultSwapDefinition(
      buySellProtection,
      protectionBuyer,
      protectionSeller,
      referenceEntity,
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

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Unit test to generate the CS01 grid

  @Test
  public void RopemarkerCS01GridTest() {

    if (outputResults) {
      System.out.println("Running Ropemaker CS01 Grid test ...");
    }

    // The number of maturities (rows) in the grid
    final int numberOfMaturities = curveLevel.length;

    // The number of CDS instruments used to calibrate against
    final int numberOfCalibrationCDS = bucketedCDSTenors.length;

    // Define a vector to hold the maturities of the different CDS contracts
    final ZonedDateTime[] cdsMaturities = new ZonedDateTime[numberOfMaturities];

    // Define a vector to hold the business day adjusted maturities of the different CDS contracts
    final ZonedDateTime[] cdsBusinessdayAdjustedMaturities = new ZonedDateTime[numberOfMaturities];

    // Set the initial elements of the maturity vectors to the first CDS maturity
    cdsMaturities[0] = firstMaturityDate;
    cdsBusinessdayAdjustedMaturities[0] = firstMaturityDate;

    // Generate the full vector of CDS maturities (and their business day adjusted variants) - note the starting index in the loop
    for (int i = 1; i < numberOfMaturities; i++) {

      // The next CDS maturity (rolled forward by the amount specified in the CS01 grid resolution variable)
      cdsMaturities[i] = cdsMaturities[i - 1].plusMonths(cs01GridResolution);

      // The next CDS maturity (rolled forward by the amount specified in the CS01 grid resolution variable) and adjusted for non-business days 
      cdsBusinessdayAdjustedMaturities[i] = cdsMaturityBusinessDayConvention.adjustDate(calendar, cdsMaturities[i]);
    }

    // Create a CDS (whose maturity we will vary) from the original contract specification
    LegacyVanillaCreditDefaultSwapDefinition rollingCDS = cds;

    // Now loop over each of the maturities in the CS01 grid
    for (int i = 0; i < numberOfMaturities; i++) {

      // Define a vector to hold the CDS spreads at each tenor point
      final double[] cdsMarketSpreads = new double[numberOfCalibrationCDS];

      // Set the level of the (flat) spread curve for CDS maturity i
      for (int m = 0; m < numberOfCalibrationCDS; m++) {
        cdsMarketSpreads[m] = curveLevel[i];
      }

      // Determine if the current maturity date is an IMM date or not
      final boolean isMaturityAnIMMDate = schedule.isAnIMMDate(cdsMaturities[i]);

      // Is the current maturity date an IMM date ...
      if (isMaturityAnIMMDate) {
        // ... yes, the contract is a standard one therefore the start date is the previous IMM date prior to the valuation date
        rollingCDS = rollingCDS.withStartDate(startDate);
      } else {
        // ... no, the contract is a legacy one therefore the start date is simply the current valuation date
        rollingCDS = rollingCDS.withStartDate(valuationDate);
      }

      // Modify the maturity of the CDS to the current row in the CS01 grid
      rollingCDS = rollingCDS.withMaturityDate(cdsBusinessdayAdjustedMaturities[i]);

      // Compute the parallel CS01 for this CDS
      final double parallelCS01 = cs01.getCS01ParallelShiftCreditDefaultSwap(valuationDate, rollingCDS, yieldCurve, bucketedCDSTenors, cdsMarketSpreads, spreadBump, SpreadBumpType.ADDITIVE_PARALLEL,
          priceType);

      // Compute the bucketed CS01 for this CDS
      final double[] bucketedCS01 = cs01.getCS01BucketedCreditDefaultSwap(valuationDate, rollingCDS, yieldCurve, bucketedCDSTenors, cdsMarketSpreads, spreadBump, SpreadBumpType.ADDITIVE_BUCKETED,
          priceType);

      // Output the CS01 grid if required
      if (outputResults) {
        OutputGrid(cdsBusinessdayAdjustedMaturities[i], curveLevel[i], parallelCS01, bucketedCS01);
      }
    }
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private void OutputGrid(final ZonedDateTime cdsBusinessdayAdjustedMaturities, final double curveLevel, final double parallelCS01, final double[] bucketedCS01) {

    System.out.print(cdsBusinessdayAdjustedMaturities + "\t" + curveLevel + "\t");

    for (int m = 0; m < bucketedCS01.length; m++) {
      System.out.print(bucketedCS01[m] + "\t");
    }

    System.out.print(parallelCS01);
    System.out.println();

  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private static ZonedDateTime zdt(int y, int m, int d, int hr, int min, int sec, int nanos, ZoneId zone) {
    return LocalDateTime.of(y, m, d, hr, min, sec, nanos).atZone(zone);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
