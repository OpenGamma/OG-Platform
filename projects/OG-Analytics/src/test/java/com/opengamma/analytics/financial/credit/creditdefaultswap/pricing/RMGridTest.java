/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing;

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
import com.opengamma.analytics.financial.credit.calibratehazardratecurve.legacy.CalibrateHazardRateCurveLegacyCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.greeks.vanilla.CS01CreditDefaultSwap;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.legacy.PresentValueLegacyCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDAInstrumentTypes;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDAYieldCurve;
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
import com.opengamma.financial.convention.daycount.ActualThreeSixtyFive;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class RMGridTest {

  // ---------------------------------------------------------------------------------------

  // TODO : Should the price be clean or dirty?

  // ---------------------------------------------------------------------------------------

  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  protected static DayCount s_act365 = new ActualThreeSixtyFive();

  // ---------------------------------------------------------------------------------------

  // Flag to control if any test results are output to the console
  private static final boolean outputResults = false;

  // ----------------------------------------------------------------------------------

  // CDS contract parameters

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

  private static final ZonedDateTime valuationDate = DateUtils.getUTCDate(2013, 3, 19);
  private static final ZonedDateTime startDate = DateUtils.getUTCDate(2006, 4, 12);
  private static final ZonedDateTime effectiveDate = valuationDate.plusDays(1); //DateUtils.getUTCDate(2013, 2, 1);
  private static final ZonedDateTime maturityDate = DateUtils.getUTCDate(2023, 3, 20);

  private static final StubType stubType = StubType.FRONTSHORT;
  private static final PeriodFrequency couponFrequency = PeriodFrequency.QUARTERLY;
  private static final DayCount daycountFractionConvention = DayCountFactory.INSTANCE.getDayCount("ACT/360");
  private static final BusinessDayConvention businessdayAdjustmentConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");

  private static final boolean immAdjustMaturityDate = false;
  private static final boolean adjustEffectiveDate = false;
  private static final boolean adjustMaturityDate = false;

  private static final double notional = 1000000.0;
  private static final double recoveryRate = 0.25;
  private static final boolean includeAccruedPremium = false;
  private static final PriceType priceType = PriceType.DIRTY;
  private static final boolean protectionStart = true;

  private static final double parSpread = 100.0;

  private static final double flatSpread = 324.01;

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

  // ----------------------------------------------------------------------------------

  private static final ZonedDateTime baseDate = valuationDate.plusDays(1); //zdt(2013, 1, 31, 0, 0, 0, 0, ZoneOffset.UTC);

  /*
  double[] times = {
      s_act365.getDayCountFraction(baseDate, baseDate.plusMonths(1)),
      s_act365.getDayCountFraction(baseDate, baseDate.plusMonths(2)),
      s_act365.getDayCountFraction(baseDate, baseDate.plusMonths(3)),
      s_act365.getDayCountFraction(baseDate, baseDate.plusMonths(6)),
      s_act365.getDayCountFraction(baseDate, baseDate.plusMonths(9)),
      s_act365.getDayCountFraction(baseDate, baseDate.plusMonths(12))
  };
  */

  /*
  // 30/1/2013
  double[] times = {
      s_act365.getDayCountFraction(baseDate, zdt(2013, 2, 28, 0, 0, 0, 0, ZoneOffset.UTC)),     // 1M     
      s_act365.getDayCountFraction(baseDate, zdt(2013, 3, 29, 0, 0, 0, 0, ZoneOffset.UTC)),     // 2M
      s_act365.getDayCountFraction(baseDate, zdt(2013, 4, 30, 0, 0, 0, 0, ZoneOffset.UTC)),     // 3M
      s_act365.getDayCountFraction(baseDate, zdt(2013, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC)),     // 6M
      s_act365.getDayCountFraction(baseDate, zdt(2013, 10, 30, 0, 0, 0, 0, ZoneOffset.UTC)),    // 9M
      s_act365.getDayCountFraction(baseDate, zdt(2014, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC))      // 1Y
  };
  */

  /*
  double[] times = {
      s_act365.getDayCountFraction(baseDate, zdt(2013, 3, 15, 0, 0, 0, 0, ZoneOffset.UTC)),     // 1M     
      s_act365.getDayCountFraction(baseDate, zdt(2013, 4, 15, 0, 0, 0, 0, ZoneOffset.UTC)),     // 2M
      s_act365.getDayCountFraction(baseDate, zdt(2013, 5, 15, 0, 0, 0, 0, ZoneOffset.UTC)),     // 3M
      s_act365.getDayCountFraction(baseDate, zdt(2013, 8, 15, 0, 0, 0, 0, ZoneOffset.UTC)),     // 6M
      s_act365.getDayCountFraction(baseDate, zdt(2013, 11, 15, 0, 0, 0, 0, ZoneOffset.UTC)),    // 9M
      s_act365.getDayCountFraction(baseDate, zdt(2014, 2, 17, 0, 0, 0, 0, ZoneOffset.UTC))      // 1Y
  };
  */

  /*
  double[] times = {
      s_act365.getDayCountFraction(baseDate, zdt(2013, 4, 1, 0, 0, 0, 0, ZoneOffset.UTC)),     // 1M     
      s_act365.getDayCountFraction(baseDate, zdt(2013, 5, 1, 0, 0, 0, 0, ZoneOffset.UTC)),     // 2M
      s_act365.getDayCountFraction(baseDate, zdt(2013, 6, 3, 0, 0, 0, 0, ZoneOffset.UTC)),     // 3M
      s_act365.getDayCountFraction(baseDate, zdt(2013, 9, 2, 0, 0, 0, 0, ZoneOffset.UTC)),     // 6M
      s_act365.getDayCountFraction(baseDate, zdt(2013, 12, 2, 0, 0, 0, 0, ZoneOffset.UTC)),    // 9M
      s_act365.getDayCountFraction(baseDate, zdt(2014, 3, 3, 0, 0, 0, 0, ZoneOffset.UTC))      // 1Y
  };
  */

  // 19/3/2013
  double[] times = {
      s_act365.getDayCountFraction(baseDate, zdt(2013, 4, 19, 0, 0, 0, 0, ZoneOffset.UTC)),     // 1M     
      s_act365.getDayCountFraction(baseDate, zdt(2013, 5, 20, 0, 0, 0, 0, ZoneOffset.UTC)),     // 2M
      s_act365.getDayCountFraction(baseDate, zdt(2013, 6, 19, 0, 0, 0, 0, ZoneOffset.UTC)),     // 3M
      s_act365.getDayCountFraction(baseDate, zdt(2013, 9, 19, 0, 0, 0, 0, ZoneOffset.UTC)),     // 6M
      s_act365.getDayCountFraction(baseDate, zdt(2013, 12, 19, 0, 0, 0, 0, ZoneOffset.UTC)),    // 9M
      s_act365.getDayCountFraction(baseDate, zdt(2014, 3, 19, 0, 0, 0, 0, ZoneOffset.UTC))      // 1Y
  };

  /*
  // 20/3/2013
  double[] times = {
      s_act365.getDayCountFraction(baseDate, zdt(2013, 4, 22, 0, 0, 0, 0, ZoneOffset.UTC)),     // 1M     
      s_act365.getDayCountFraction(baseDate, zdt(2013, 5, 20, 0, 0, 0, 0, ZoneOffset.UTC)),     // 2M
      s_act365.getDayCountFraction(baseDate, zdt(2013, 6, 20, 0, 0, 0, 0, ZoneOffset.UTC)),     // 3M
      s_act365.getDayCountFraction(baseDate, zdt(2013, 9, 20, 0, 0, 0, 0, ZoneOffset.UTC)),     // 6M
      s_act365.getDayCountFraction(baseDate, zdt(2013, 12, 20, 0, 0, 0, 0, ZoneOffset.UTC)),    // 9M
      s_act365.getDayCountFraction(baseDate, zdt(2014, 3, 20, 0, 0, 0, 0, ZoneOffset.UTC))      // 1Y
  };
  */

  /*
  // 21/3/2013
  double[] times = {
      s_act365.getDayCountFraction(baseDate, zdt(2013, 4, 22, 0, 0, 0, 0, ZoneOffset.UTC)),     // 1M     
      s_act365.getDayCountFraction(baseDate, zdt(2013, 5, 21, 0, 0, 0, 0, ZoneOffset.UTC)),     // 2M
      s_act365.getDayCountFraction(baseDate, zdt(2013, 6, 21, 0, 0, 0, 0, ZoneOffset.UTC)),     // 3M
      s_act365.getDayCountFraction(baseDate, zdt(2013, 9, 23, 0, 0, 0, 0, ZoneOffset.UTC)),     // 6M
      s_act365.getDayCountFraction(baseDate, zdt(2013, 12, 23, 0, 0, 0, 0, ZoneOffset.UTC)),    // 9M
      s_act365.getDayCountFraction(baseDate, zdt(2014, 3, 20, 0, 0, 0, 0, ZoneOffset.UTC))      // 1Y
  };
  */

  /*
  // 10/6/2013
  double[] times = {
      s_act365.getDayCountFraction(baseDate, zdt(2013, 7, 10, 0, 0, 0, 0, ZoneOffset.UTC)),     // 1M     
      s_act365.getDayCountFraction(baseDate, zdt(2013, 8, 12, 0, 0, 0, 0, ZoneOffset.UTC)),     // 2M
      s_act365.getDayCountFraction(baseDate, zdt(2013, 9, 10, 0, 0, 0, 0, ZoneOffset.UTC)),     // 3M
      s_act365.getDayCountFraction(baseDate, zdt(2013, 12, 10, 0, 0, 0, 0, ZoneOffset.UTC)),    // 6M
      s_act365.getDayCountFraction(baseDate, zdt(2014, 3, 10, 0, 0, 0, 0, ZoneOffset.UTC)),     // 9M
      s_act365.getDayCountFraction(baseDate, zdt(2014, 6, 10, 0, 0, 0, 0, ZoneOffset.UTC))      // 1Y
  };
  */

  double[] rates = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };

  /*
  private static final double[] rates = {
      (new PeriodicInterestRate(0.002017, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.002465, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.003005, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.004758, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.006428, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.007955, 1)).toContinuous().getRate()
  };
  */

  ISDACurve yieldCurve = new ISDACurve("IR_CURVE", times, rates, s_act365.getDayCountFraction(valuationDate, baseDate));

  // ----------------------------------------------------------------------------------

  private static final int spotDays = 0;
  final ZonedDateTime spotDate = valuationDate.plusDays(spotDays);

  private static final ZonedDateTime isdaBaseDate = valuationDate.plusDays(spotDays);

  /*
  private static final ZonedDateTime[] isdaTimes = {
      isdaBaseDate.plusMonths(1),
      isdaBaseDate.plusMonths(2),
      isdaBaseDate.plusMonths(3),
      isdaBaseDate.plusMonths(6),
      isdaBaseDate.plusMonths(9),
      isdaBaseDate.plusMonths(12)
  };
  */

  private static final ZonedDateTime[] isdaTimes = {
      zdt(2013, 2, 28, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2013, 3, 29, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2013, 4, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2013, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2013, 10, 30, 0, 0, 0, 0, ZoneOffset.UTC),
      zdt(2014, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC) /*,
                                                   zdt(2015, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC)*/
  };

  private static final double[] isdaRates = {0.002017, 0.002465, 0.003005, 0.004758, 0.006428, 0.007955, /*0.004395*/};

  private static final ISDAInstrumentTypes[] rateTypes = {
      ISDAInstrumentTypes.MoneyMarket,
      ISDAInstrumentTypes.MoneyMarket,
      ISDAInstrumentTypes.MoneyMarket,
      ISDAInstrumentTypes.MoneyMarket,
      ISDAInstrumentTypes.MoneyMarket,
      ISDAInstrumentTypes.MoneyMarket /*,
                                      ISDAInstrumentTypes.Swap*/
  };

  private static final PeriodFrequency swapFixedLegCouponFrequency = PeriodFrequency.SEMI_ANNUAL;
  private static final PeriodFrequency swapFloatingLegCouponFrequency = PeriodFrequency.QUARTERLY;

  private static final DayCount moneyMarketDaycountFractionConvention = DayCountFactory.INSTANCE.getDayCount("ACT/360");          // MM DCC
  private static final DayCount swapFixedLegDaycountFractionConvention = DayCountFactory.INSTANCE.getDayCount("30/360");          // Swap DCC
  private static final DayCount swapFloatingLegDaycountFractionConvention = DayCountFactory.INSTANCE.getDayCount("ACT/360");      // Swap DCC

  private static final ISDAYieldCurve isdaYieldCurve = new ISDAYieldCurve(
      isdaBaseDate,
      isdaTimes,
      rateTypes,
      isdaRates,
      spotDays,
      moneyMarketDaycountFractionConvention,
      swapFixedLegDaycountFractionConvention,
      swapFloatingLegDaycountFractionConvention,
      swapFixedLegCouponFrequency,
      swapFloatingLegCouponFrequency,
      businessdayAdjustmentConvention,
      calendar);

  // ----------------------------------------------------------------------------------

  // Construct the obligors party to the contract

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

  // ----------------------------------------------------------------------------------

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

  // ----------------------------------------------------------------------------------

  //@Test
  public void testDiscountFactors() {

    for (long i = 0; i < 3000; i++)
    {
      ZonedDateTime testDate = isdaBaseDate.plusDays(i);

      final double Z = isdaYieldCurve.getDiscountFactor(isdaBaseDate, testDate);

      //System.out.println("i = " + "\t" + i + "\t" + testDate + "\t" + Z);
    }
  }

  // ----------------------------------------------------------------------------------

  @Test
  public void testPVCalculation() {

    if (outputResults) {
      System.out.println("Running PV test ...");
    }

    // The number of CDS instruments used to calibrate against
    final int numberOfCalibrationCDS = 14;

    // The CDS tenors to calibrate to
    final ZonedDateTime[] tenors = new ZonedDateTime[numberOfCalibrationCDS];

    tenors[0] = DateUtils.getUTCDate(2013, 9, 20);
    tenors[1] = DateUtils.getUTCDate(2014, 3, 20);
    tenors[2] = DateUtils.getUTCDate(2015, 3, 20);
    tenors[3] = DateUtils.getUTCDate(2016, 3, 20);
    tenors[4] = DateUtils.getUTCDate(2017, 3, 20);
    tenors[5] = DateUtils.getUTCDate(2018, 3, 20);
    tenors[6] = DateUtils.getUTCDate(2019, 3, 20);
    tenors[7] = DateUtils.getUTCDate(2020, 3, 20);
    tenors[8] = DateUtils.getUTCDate(2021, 3, 20);
    tenors[9] = DateUtils.getUTCDate(2022, 3, 20);
    tenors[10] = DateUtils.getUTCDate(2023, 3, 20);
    tenors[11] = DateUtils.getUTCDate(2028, 3, 20);
    tenors[12] = DateUtils.getUTCDate(2033, 3, 20);
    tenors[13] = DateUtils.getUTCDate(2043, 3, 20);

    // The market observed par CDS spreads at these tenors
    final double[] marketSpreads = new double[numberOfCalibrationCDS];

    for (int i = 0; i < numberOfCalibrationCDS; i++) {
      marketSpreads[i] = flatSpread;
    }

    // The recovery rate assumption used in the PV calculations when calibrating
    final double calibrationRecoveryRate = 0.25;

    // -------------------------------------------------------------------------------------

    // Create a calibration CDS (will be a modified version of the baseline CDS)
    LegacyVanillaCreditDefaultSwapDefinition calibrationCDS = cds;

    LegacyVanillaCreditDefaultSwapDefinition valuationCDS = cds;

    // Set the recovery rate of the calibration CDS used for the curve calibration (this appears in the calculation of the contingent leg)
    calibrationCDS = calibrationCDS.withRecoveryRate(calibrationRecoveryRate);

    valuationCDS = valuationCDS.withMaturityDate(maturityDate);

    // -------------------------------------------------------------------------------------

    // Create a calibrate survival curve object
    final CalibrateHazardRateCurveLegacyCreditDefaultSwap hazardRateCurve = new CalibrateHazardRateCurveLegacyCreditDefaultSwap();

    // Calibrate the hazard rate curve to the market observed par CDS spreads (returns calibrated hazard rates as a vector of doubles)
    final double[] calibratedHazardRates = hazardRateCurve.getCalibratedHazardRateTermStructure(valuationDate, calibrationCDS, tenors, marketSpreads, yieldCurve, priceType);

    final PresentValueLegacyCreditDefaultSwap creditDefaultSwap = new PresentValueLegacyCreditDefaultSwap();

    // Vector of time nodes for the hazard rate curve
    final double[] times = new double[tenors.length + 1];

    times[0] = 0.0;
    for (int m = 1; m <= tenors.length; m++) {
      times[m] = ACT_365.getDayCountFraction(valuationDate, tenors[m - 1]);
    }

    double[] modifiedHazardRateCurve = new double[calibratedHazardRates.length + 1];

    modifiedHazardRateCurve[0] = calibratedHazardRates[0];

    for (int m = 1; m < modifiedHazardRateCurve.length; m++) {
      modifiedHazardRateCurve[m] = calibratedHazardRates[m - 1];
    }

    // Build a hazard rate curve object based on the input market data
    final HazardRateCurve calibratedHazardRateCurve = new HazardRateCurve(times, modifiedHazardRateCurve, 0.0);

    final double presentValue = creditDefaultSwap.getPresentValueLegacyCreditDefaultSwap(valuationDate, valuationCDS, yieldCurve, calibratedHazardRateCurve, priceType);

    for (int i = 0; i < calibratedHazardRates.length; i++) {
      //System.out.println(tenors[i] + "\t" + calibratedHazardRates[i]);
    }

    //System.out.println(presentValue);
  }

  // ----------------------------------------------------------------------------------

  //@Test
  public void testRMGrid() {

    if (outputResults) {
      System.out.println("Running RM Grid test ...");
    }

    final int numberOfMaturities = 122;

    final ZonedDateTime[] bdaMaturities = new ZonedDateTime[numberOfMaturities];

    bdaMaturities[0] = DateUtils.getUTCDate(2013, 2, 20);

    GenerateCreditDefaultSwapPremiumLegSchedule schedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    final ZonedDateTime[] maturities = new ZonedDateTime[numberOfMaturities];

    maturities[0] = bdaMaturities[0];

    for (int i = 1; i < numberOfMaturities; i++) {

      maturities[i] = maturities[i - 1].plusMonths(1);

      bdaMaturities[i] = schedule.businessDayAdjustDate(maturities[i], calendar, businessdayAdjustmentConvention);
    }

    // The type of spread bump to apply
    //final SpreadBumpType spreadBumpType = SpreadBumpType.ADDITIVE_BUCKETED;
    final SpreadBumpType spreadBumpType = SpreadBumpType.ADDITIVE_PARALLEL;

    // The magnitude (but not direction) of bump to apply (in bps)
    final double spreadBump = 1.0;

    // The number of CDS instruments used to calibrate against
    final int numberOfCalibrationCDS = 14;

    // The CDS tenors to calibrate to
    final ZonedDateTime[] tenors = new ZonedDateTime[numberOfCalibrationCDS];

    tenors[0] = DateUtils.getUTCDate(2013, 9, 20);
    tenors[1] = DateUtils.getUTCDate(2014, 3, 20);
    tenors[2] = DateUtils.getUTCDate(2015, 3, 20);
    tenors[3] = DateUtils.getUTCDate(2016, 3, 20);
    tenors[4] = DateUtils.getUTCDate(2017, 3, 20);
    tenors[5] = DateUtils.getUTCDate(2018, 3, 20);
    tenors[6] = DateUtils.getUTCDate(2019, 3, 20);
    tenors[7] = DateUtils.getUTCDate(2020, 3, 20);
    tenors[8] = DateUtils.getUTCDate(2021, 3, 20);
    tenors[9] = DateUtils.getUTCDate(2022, 3, 20);
    tenors[10] = DateUtils.getUTCDate(2023, 3, 20);
    tenors[11] = DateUtils.getUTCDate(2028, 3, 20);
    tenors[12] = DateUtils.getUTCDate(2033, 3, 20);
    tenors[13] = DateUtils.getUTCDate(2043, 3, 20);

    // Create a CS01 calculator object
    final CS01CreditDefaultSwap cs01 = new CS01CreditDefaultSwap();

    // Create a CDS (whose maturity we will vary)
    LegacyVanillaCreditDefaultSwapDefinition rollingCDS = cds;

    // Loop over each of the maturities
    for (int i = 0; i < numberOfMaturities; i++) {

      final double[] marketSpreads = new double[numberOfCalibrationCDS];

      // Set the level of the (flat) spread curve
      for (int m = 0; m < numberOfCalibrationCDS; m++) {
        marketSpreads[m] = curveLevel[i];
      }

      /*
      if (schedule.isAnIMMDate(maturities[i])) {
        rollingCDS = rollingCDS.withStartDate(startDate);
      }
      else {
        rollingCDS = rollingCDS.withStartDate(valuationDate);
      }
       */

      // Set the maturity of the CDS to value
      rollingCDS = rollingCDS.withMaturityDate(bdaMaturities[i]);

      // Compute the bucketed CS01 for this CDS
      //final double bucketedCS01[] = cs01.getCS01BucketedCreditDefaultSwap(valuationDate, rollingCDS, yieldCurve, tenors, marketSpreads, spreadBump, spreadBumpType, priceType);

      // Compute the parallel CS01 for this CDS
      final double parallelCS01 = cs01.getCS01ParallelShiftCreditDefaultSwap(valuationDate, rollingCDS, yieldCurve, tenors, marketSpreads, spreadBump, spreadBumpType, priceType);

      // Output grid
      if (outputResults) {

        // System.out.print(marketSpreads[0] + "\t" + bdaMaturities[i] + "\t");

        //System.out.println(marketSpreads[0] + "\t" + bdaMaturities[i] + "\t" + parallelCS01);

        for (int m = 0; m < numberOfCalibrationCDS; m++) {
          //System.out.print(/*"Tenor = " + tenors[m] + "\t" + "CDS bucketed CS01 = " + "\t" + */bucketedCS01[m] + "\t");
        }
        //System.out.println();

      }
    }
  }

  // ----------------------------------------------------------------------------------

  private static ZonedDateTime zdt(int y, int m, int d, int hr, int min, int sec, int nanos, ZoneId zone) {
    return LocalDateTime.of(y, m, d, hr, min, sec, nanos).atZone(zone);
  }

  // ----------------------------------------------------------------------------------
}
