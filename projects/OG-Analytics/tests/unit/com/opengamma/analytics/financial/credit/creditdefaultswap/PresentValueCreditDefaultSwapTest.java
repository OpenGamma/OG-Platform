/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap;

import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.cds.ISDAExtrapolator1D;
import com.opengamma.analytics.financial.credit.cds.ISDAInterpolator1D;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.GenerateCreditDefaultSwapPremiumLegSchedule;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.PresentValueCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.hazardratemodel.CalibrateHazardRate;
import com.opengamma.analytics.financial.credit.hazardratemodel.HazardRateCurve;
import com.opengamma.analytics.financial.credit.obligormodel.CreditRating;
import com.opengamma.analytics.financial.credit.obligormodel.CreditRatingFitch;
import com.opengamma.analytics.financial.credit.obligormodel.CreditRatingMoodys;
import com.opengamma.analytics.financial.credit.obligormodel.CreditRatingStandardAndPoors;
import com.opengamma.analytics.financial.credit.obligormodel.Obligor;
import com.opengamma.analytics.financial.credit.obligormodel.Region;
import com.opengamma.analytics.financial.credit.obligormodel.Sector;
import com.opengamma.analytics.financial.interestrate.PeriodicInterestRate;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.FlatExtrapolator1D;
import com.opengamma.analytics.math.random.NormalRandomNumberGenerator;
import com.opengamma.analytics.math.statistics.descriptive.PercentileCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
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
 *  Test of the implementation of the valuation model for a CDS 
 */
public class PresentValueCreditDefaultSwapTest {

  // ----------------------------------------------------------------------------------

  // TODO : Add all the tests
  // TODO : Move the calendar into a seperate TestCalendar class
  // TODO : Fix the time decay test

  // ----------------------------------------------------------------------------------

  // CDS contract parameters

  private static final BuySellProtection buySellProtection = BuySellProtection.BUY;

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

  private static final CreditRatingMoodys protectionSellerCreditRatingMoodys = CreditRatingMoodys.AA;
  private static final CreditRatingStandardAndPoors protectionSellerCreditRatingStandardAndPoors = CreditRatingStandardAndPoors.A;
  private static final CreditRatingFitch protectionSellerCreditRatingFitch = CreditRatingFitch.AA;

  private static final CreditRatingMoodys referenceEntityCreditRatingMoodys = CreditRatingMoodys.AA;
  private static final CreditRatingStandardAndPoors referenceEntityCreditRatingStandardAndPoors = CreditRatingStandardAndPoors.A;
  private static final CreditRatingFitch referenceEntityCreditRatingFitch = CreditRatingFitch.AA;

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

  private static final Calendar calendar = new MyCalendar();

  private static final ZonedDateTime startDate = DateUtils.getUTCDate(2008, 3, 20);
  private static final ZonedDateTime effectiveDate = DateUtils.getUTCDate(2008, 3, 21);
  private static final ZonedDateTime maturityDate = DateUtils.getUTCDate(2013, 3, 20);
  private static final ZonedDateTime valuationDate = DateUtils.getUTCDate(2008, 9, 18);

  private static final StubType stubType = StubType.FRONTSHORT;
  private static final PeriodFrequency couponFrequency = PeriodFrequency.QUARTERLY;
  private static final DayCount daycountFractionConvention = DayCountFactory.INSTANCE.getDayCount("ACT/360");
  private static final BusinessDayConvention businessdayAdjustmentConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");

  private static final boolean immAdjustMaturityDate = true;
  private static final boolean adjustEffectiveDate = true;
  private static final boolean adjustMaturityDate = true;

  private static final double notional = 10000000.0;
  private static final double premiumLegCoupon = 100.0;
  private static final double recoveryRate = 0.40;
  private static final boolean includeAccruedPremium = false;
  private static final boolean protectionStart = true;

  // ----------------------------------------------------------------------------------

  // Dummy yield curve

  protected static DayCount s_act365 = new ActualThreeSixtyFive();

  private static final ZonedDateTime baseDate = ZonedDateTime.of(2008, 9, 22, 0, 0, 0, 0, TimeZone.UTC);

  private static final double[] TIME = {
      s_act365.getDayCountFraction(baseDate, baseDate),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2008, 10, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2008, 11, 24, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2008, 12, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2009, 3, 23, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2009, 6, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2009, 9, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2010, 3, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2010, 9, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2011, 3, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2011, 9, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2012, 3, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2012, 9, 24, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2013, 3, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2013, 9, 23, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2014, 3, 24, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2014, 9, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2015, 3, 23, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2015, 9, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2016, 3, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2016, 9, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2017, 3, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2017, 9, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2018, 3, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2018, 9, 24, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2019, 3, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2019, 9, 23, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2020, 3, 23, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2020, 9, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2021, 3, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2021, 9, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2022, 3, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2022, 9, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2023, 3, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2023, 9, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2024, 3, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2024, 9, 23, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2025, 3, 24, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2025, 9, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2026, 3, 23, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2026, 9, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2027, 3, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2027, 9, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2028, 3, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2028, 9, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2029, 3, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2029, 9, 24, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2030, 3, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2030, 9, 23, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2031, 3, 24, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2031, 9, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2032, 3, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2032, 9, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2033, 3, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2033, 9, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2034, 3, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2034, 9, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2035, 3, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2035, 9, 24, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2036, 3, 24, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2036, 9, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2037, 3, 23, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2037, 9, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2038, 3, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2038, 9, 22, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(baseDate, ZonedDateTime.of(2040, 9, 22, 0, 0, 0, 0, TimeZone.UTC))
  };

  private static final double interestRate = 0.0;

  //private static final double[] TIME = new double[] {0, 3, 5, 10, 15, 40 };
  //private static final double[] RATES = new double[] {interestRate, interestRate, interestRate, interestRate, interestRate, interestRate };

  private static final double[] RATES = new double[] {
      interestRate, interestRate, interestRate, interestRate, interestRate,
      interestRate, interestRate, interestRate, interestRate, interestRate,
      interestRate, interestRate, interestRate, interestRate, interestRate,
      interestRate, interestRate, interestRate, interestRate, interestRate,
      interestRate, interestRate, interestRate, interestRate, interestRate,
      interestRate, interestRate, interestRate, interestRate, interestRate,
      interestRate, interestRate, interestRate, interestRate, interestRate,
      interestRate, interestRate, interestRate, interestRate, interestRate,
      interestRate, interestRate, interestRate, interestRate, interestRate,
      interestRate, interestRate, interestRate, interestRate, interestRate,
      interestRate, interestRate, interestRate, interestRate, interestRate,
      interestRate, interestRate, interestRate, interestRate, interestRate,
      interestRate, interestRate, interestRate, interestRate, interestRate, interestRate };

  //private static final InterpolatedDoublesCurve R = InterpolatedDoublesCurve.from(TIME, RATES, new LinearInterpolator1D());

  private static final InterpolatedDoublesCurve R = InterpolatedDoublesCurve.from(TIME, RATES, new CombinedInterpolatorExtrapolator(
      new ISDAInterpolator1D(),    // ISDA style interpolation - from RiskCare
      new FlatExtrapolator1D(),    // Flat rate extrapolated to the left
      new ISDAExtrapolator1D()));  // ISDA style extrapolation to the right  - from RiskCare;

  private static final YieldCurve yieldCurve = YieldCurve.from(R);

  // ----------------------------------------------------------------------------------

  // Hazard rate term structure

  static double[] hazardRateTimes = {
      0.0,
      s_act365.getDayCountFraction(valuationDate, ZonedDateTime.of(2013, 06, 20, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(valuationDate, ZonedDateTime.of(2015, 06, 20, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(valuationDate, ZonedDateTime.of(2018, 06, 20, 0, 0, 0, 0, TimeZone.UTC))
  };

  static double[] hazardRates = {
      (new PeriodicInterestRate(0.09709857471184660000, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.09709857471184660000, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.09705141266558010000, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.09701141671498870000, 1)).toContinuous().getRate()
  };

  private static final HazardRateCurve hazardRateCurve = new HazardRateCurve(hazardRateTimes, hazardRates, 0.0);

  // ----------------------------------------------------------------------------------

  private static final Obligor protectionBuyer = new Obligor(
      protectionBuyerTicker,
      protectionBuyerShortName,
      protectionBuyerREDCode,
      protectionBuyerCompositeRating,
      protectionBuyerImpliedRating,
      protectionBuyerCreditRatingMoodys,
      protectionBuyerCreditRatingStandardAndPoors,
      protectionBuyerCreditRatingFitch,
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
      referenceEntitySector,
      referenceEntityRegion,
      referenceEntityCountry);

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Construct a CDS contract 
  private static final CreditDefaultSwapDefinition cds = new CreditDefaultSwapDefinition(
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
      valuationDate,
      stubType,
      couponFrequency,
      daycountFractionConvention,
      businessdayAdjustmentConvention,
      immAdjustMaturityDate,
      adjustEffectiveDate,
      adjustMaturityDate,
      notional,
      premiumLegCoupon,
      recoveryRate,
      includeAccruedPremium,
      protectionStart);

  // -----------------------------------------------------------------------------------------------

  // Simple test to compute the PV of a CDS assuming a flat term structure of market observed CDS par spreads

  //@Test
  public void testPresentValueCreditDefaultSwap() {

    // -----------------------------------------------------------------------------------------------

    final boolean outputResults = false;

    if (outputResults) {
      System.out.println("Running CDS PV test  ...");
    }

    // -----------------------------------------------------------------------------------------------

    // Call the constructor to create a CDS whose PV we will compute
    final PresentValueCreditDefaultSwap creditDefaultSwap = new PresentValueCreditDefaultSwap();

    // Calculate the present value
    double presentValue = creditDefaultSwap.getPresentValueCreditDefaultSwap(cds, yieldCurve, hazardRateCurve);

    if (outputResults) {
      System.out.println("CDS PV = " + presentValue);
    }

  }

  // -----------------------------------------------------------------------------------------------

  // Simple test to calibrate a single name CDS to a term structure of market observed par CDS spreads and compute the PV

  //@Test
  public void testPresentValueCreditSwapCalibrateSurvivalCurve() {

    // -----------------------------------------------------------------------------------------------

    final boolean outputResults = false;

    double presentValue = 0.0;

    if (outputResults) {
      System.out.println("Running CDS calibration test ...");
    }

    // -----------------------------------------------------------------------------------------------

    // Define the market data to calibrate to

    // The number of CDS instruments used to calibrate against
    int numberOfCalibrationCDS = 10;

    // The CDS tenors to calibrate to
    final ZonedDateTime[] tenors = new ZonedDateTime[numberOfCalibrationCDS];

    tenors[0] = DateUtils.getUTCDate(2008, 12, 20);
    tenors[1] = DateUtils.getUTCDate(2009, 6, 20);
    tenors[2] = DateUtils.getUTCDate(2010, 6, 20);
    tenors[3] = DateUtils.getUTCDate(2011, 6, 20);
    tenors[4] = DateUtils.getUTCDate(2012, 6, 20);
    tenors[5] = DateUtils.getUTCDate(2014, 6, 20);
    tenors[6] = DateUtils.getUTCDate(2017, 6, 20);
    tenors[7] = DateUtils.getUTCDate(2022, 6, 20);
    tenors[8] = DateUtils.getUTCDate(2030, 6, 20);
    tenors[9] = DateUtils.getUTCDate(2040, 6, 20);

    // The market observed par CDS spreads at these tenors
    final double[] marketSpreads = new double[numberOfCalibrationCDS];

    final double flatSpread = 100.0;

    marketSpreads[0] = flatSpread;
    marketSpreads[1] = flatSpread;
    marketSpreads[2] = flatSpread;
    marketSpreads[3] = flatSpread;
    marketSpreads[4] = flatSpread;
    marketSpreads[5] = flatSpread;
    marketSpreads[6] = flatSpread;
    marketSpreads[7] = flatSpread;
    marketSpreads[8] = flatSpread;
    marketSpreads[9] = flatSpread;

    // The recovery rate assumption used in the PV calculations when calibrating
    final double calibrationRecoveryRate = 0.40;

    // -------------------------------------------------------------------------------------

    // Calibrate the hazard rate term structure to the market observed par spreads

    // Create a calibration CDS (will be a modified version of the baseline CDS)
    CreditDefaultSwapDefinition calibrationCDS = cds;

    // Set the recovery rate of the calibration CDS used for the curve calibration (this appears in the calculation of the contingent leg)
    calibrationCDS = calibrationCDS.withRecoveryRate(calibrationRecoveryRate);

    // Create a calibrate survival curve object
    final CalibrateHazardRate hazardRateCurve = new CalibrateHazardRate();

    // Calibrate the survival curve to the market observed par CDS spreads (returns hazard rate term structure as a vector of doubles)
    double[] calibratedHazardRateTermStructure = hazardRateCurve.getCalibratedHazardRateTermStructure(calibrationCDS, tenors, marketSpreads, yieldCurve);

    // -------------------------------------------------------------------------------------

    // Now want to create a new CDS and price it using the calibrated survival curve

    // Create a cashflow schedule object (to facilitate the conversion of tenors into doubles)
    GenerateCreditDefaultSwapPremiumLegSchedule cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    // Convert the ZonedDateTime tenors into doubles (measured from valuationDate)
    double[] tenorsAsDoubles = cashflowSchedule.convertTenorsToDoubles(cds, tenors);

    // Build a survival curve using the input tenors (converted to doubles) and the previously calibrated hazard rates
    final HazardRateCurve survivalCurve = new HazardRateCurve(tenorsAsDoubles, calibratedHazardRateTermStructure, 0.0);

    // Call the constructor to create a CDS whose PV we will compute
    final PresentValueCreditDefaultSwap creditDefaultSwap = new PresentValueCreditDefaultSwap();

    // Call the CDS PV calculator to get the current PV (should be equal to zero)
    presentValue = creditDefaultSwap.getPresentValueCreditDefaultSwap(cds, yieldCurve, survivalCurve);

    if (outputResults) {
      System.out.println("CDS PV = " + presentValue);
    }

    // -------------------------------------------------------------------------------------
  }

  // -----------------------------------------------------------------------------------------------

  // Test to vary the valuationDate of a CDS from adjustedEffectiveDate to adjustedMaturityDate and compute PV

  @Test
  public void testPresentValueCreditDefaultSwapTimeDecay() {

    // -----------------------------------------------------------------------------------------------

    final boolean outputResults = false;

    double premiumLegPresentValue = 0.0;
    double contingentLegPresentValue = 0.0;
    double presentValue = 0.0;

    if (outputResults) {
      System.out.println("Running CDS PV time decay test ...");
    }

    // -----------------------------------------------------------------------------------------------

    // Create a valuation CDS whose valuationDate will vary (will be a modified version of the baseline CDS)
    CreditDefaultSwapDefinition valuationCDS = cds;

    // Call the constructor to create a CDS whose PV we will compute
    final PresentValueCreditDefaultSwap creditDefaultSwap = new PresentValueCreditDefaultSwap();

    // -----------------------------------------------------------------------------------------------

    // start at the initial valuation date
    ZonedDateTime rollingdate = ZonedDateTime.of(2008, 3, 20, 0, 0, 0, 0, TimeZone.UTC);

    // Specify the end date
    ZonedDateTime endDate = ZonedDateTime.of(2013, 3, 10, 0, 0, 0, 0, TimeZone.UTC);

    while (rollingdate.isBefore(endDate)) {

      // Roll the current valuation date
      rollingdate = rollingdate.plusDays(1);

      // Modify the CDS's valuation date
      valuationCDS = valuationCDS.withValuationDate(rollingdate);

      // Set the recovery rate to 100% (kill contingent leg) to get the premium leg value
      valuationCDS = valuationCDS.withSpread(premiumLegCoupon);
      valuationCDS = valuationCDS.withRecoveryRate(1.0);
      premiumLegPresentValue = creditDefaultSwap.getPresentValueCreditDefaultSwap(valuationCDS, yieldCurve, hazardRateCurve);

      // Set the coupon to 0 (kill premium leg) and reset the recovery rate to get the contingent leg value
      valuationCDS = valuationCDS.withSpread(0.0);
      valuationCDS = valuationCDS.withRecoveryRate(recoveryRate);
      contingentLegPresentValue = creditDefaultSwap.getPresentValueCreditDefaultSwap(valuationCDS, yieldCurve, hazardRateCurve);

      if (outputResults) {
        System.out.println(rollingdate + "\t" + premiumLegPresentValue + "\t" + contingentLegPresentValue);
      }
    }

    // -----------------------------------------------------------------------------------------------
  }

  // -----------------------------------------------------------------------------------------------

  // Work-in-Progress

  //@Test
  public void testPFECalculation() {

    // -----------------------------------------------------------------------------------------------

    int counter = 0;
    int numberOfSimulations = 1;

    double[] presentValue = new double[1];

    double mu = 0.90;
    double sigma = 1.0;

    double hazRate = 0.01;

    // -----------------------------------------------------------------------------------------------

    // Create an N(0, 1) random number generator
    NormalRandomNumberGenerator normRand = new NormalRandomNumberGenerator(0.0, 1.0);

    PercentileCalculator quantile = new PercentileCalculator(0.5);

    // -----------------------------------------------------------------------------------------------

    // The simulation start date
    ZonedDateTime simulationStartDate = DateUtils.getUTCDate(2007, 10, 23);

    // The simulation end date
    ZonedDateTime simulationEndDate = DateUtils.getUTCDate(2008, 10, 23);

    // Initialise the current timenode to be the start of the simulation
    ZonedDateTime currentTimenode = simulationStartDate;

    // -----------------------------------------------------------------------------------------------

    // Call the constructor to create a CDS whose PV we will compute
    final PresentValueCreditDefaultSwap creditDefaultSwap = new PresentValueCreditDefaultSwap();

    // Create a CDS object whose valuation date we will vary
    CreditDefaultSwapDefinition rollingCDS = cds;

    // -----------------------------------------------------------------------------------------------

    // Determine how many timenodes there are
    while (currentTimenode.isBefore(simulationEndDate.minusDays(5))) {
      currentTimenode = currentTimenode.plusDays(1);
      counter++;
    }

    int numberOfTimenodes = counter;

    // -----------------------------------------------------------------------------------------------

    // Create an array to store the simulated PV's in
    double[][] results = new double[numberOfTimenodes + 1][numberOfSimulations];

    // Create a vector to hold the PFE
    double[] potentialFutureExposure = new double[numberOfTimenodes + 1];

    // Create a vector to hold the random numbers for each simulation
    double[] epsilon = new double[numberOfTimenodes + 1];

    // -----------------------------------------------------------------------------------------------

    // Main Monte Carlo loop
    for (int alpha = 0; alpha < numberOfSimulations; alpha++) {

      if (alpha % 100 == 0) {
        //System.out.println("Simulation = " + alpha);
      }

      // -----------------------------------------------------------------------------------------------

      // Reset the current timenode to the start of the simulation
      currentTimenode = simulationStartDate;

      // -----------------------------------------------------------------------------------------------

      // Call the CDS PV calculator (with a flat survival curve) to get the current PV at time zero
      presentValue[0] = creditDefaultSwap.getPresentValueCreditDefaultSwap(cds, yieldCurve, hazardRateCurve);

      // Get a vector of N(0, 1) normal random variables
      epsilon = normRand.getVector(numberOfTimenodes + 1);

      // Reset the counter
      counter = 0;

      // Loop over all the timenodes
      while (currentTimenode.isBefore(simulationEndDate.minusDays(5))) {

        // Store the simulated PV
        results[counter][alpha] = presentValue[0];

        // Roll the timenode to the next one
        currentTimenode = currentTimenode.plusDays(1);

        // Calculate the time from simulationStartDate to the current timenode
        double t = TimeCalculator.getTimeBetween(simulationStartDate, currentTimenode);

        // Calculate the simulated hazard rate (assume it is a simple GBM)
        double h = hazRate * Math.exp((mu - 0.5 * sigma * sigma) * t + sigma * Math.sqrt(t) * epsilon[counter]);

        //System.out.println(currentTimenode + "\t" + t + "\t" + h);

        // Build a vector with this simulated value in
        double[] simulatedHazardRates = new double[] {h };

        // Construct a survival curve from this simulated rate
        //HazardRateCurveTemp simulatedSurvivalCurve = new HazardRateCurveTemp(tenorsAsDoubles, simulatedHazardRates, 0.0);

        // Roll the valuation date of the CDS to the current timenode
        //rollingCDS = rollingCDS.withValuationDate(currentTimenode);

        // Re-val the CDS given the simulated hazard rate at the new timenode
        //presentValue[0] = creditDefaultSwap.getPresentValueCreditDefaultSwap(rollingCDS, yieldCurve, simulatedSurvivalCurve);

        counter++;
      }
    }

    for (int i = 0; i < numberOfTimenodes; i++) {

      double[] x = new double[numberOfTimenodes + 1];

      for (int alpha = 0; alpha < numberOfSimulations; alpha++) {
        x[alpha] = results[i][alpha];
      }

      potentialFutureExposure[i] = quantile.evaluate(x);
    }

    // -----------------------------------------------------------------------------------------------
  }

  // -----------------------------------------------------------------------------------------------

  // Bespoke calendar class (have made this public - may want to change this)
  public static class MyCalendar implements Calendar {

    private static final Calendar weekend = new MondayToFridayCalendar("GBP");

    @Override
    public boolean isWorkingDay(LocalDate date) {

      if (!weekend.isWorkingDay(date)) {
        return false;
      }

      /*
      // Custom bank holiday
      if (date.equals(LocalDate.of(2012, 8, 27))) {
        return false;
      }

      // Custom bank holiday
      if (date.equals(LocalDate.of(2012, 8, 28))) {
        return false;
      }

      // Custom bank holiday
      if (date.equals(LocalDate.of(2017, 8, 28))) {
        return false;
      }

      // Custom bank holiday
      if (date.equals(LocalDate.of(2017, 8, 29))) {
        return false;
      }
       */

      return true;
    }

    @Override
    public String getConventionName() {
      return "";
    }

  }

  // -----------------------------------------------------------------------------------------------
}

//-----------------------------------------------------------------------------------------------