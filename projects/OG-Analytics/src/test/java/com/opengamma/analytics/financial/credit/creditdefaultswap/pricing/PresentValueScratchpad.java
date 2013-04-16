/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.analytics.financial.credit.calibratehazardratecurve.legacy.CalibrateHazardRateCurveLegacyCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.obligor.CreditRating;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingFitch;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingMoodys;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingStandardAndPoors;
import com.opengamma.analytics.financial.credit.obligor.Region;
import com.opengamma.analytics.financial.credit.obligor.Sector;
import com.opengamma.analytics.financial.credit.obligor.definition.Obligor;
import com.opengamma.analytics.financial.interestrate.PeriodicInterestRate;
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
public class PresentValueScratchpad {

  // ---------------------------------------------------------------------------------------

  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  // ---------------------------------------------------------------------------------------

  // Flag to control if any test results are output to the console
  private static final boolean outputResults = true;

  // ----------------------------------------------------------------------------------

  // CDS contract parameters

  private static final BuySellProtection buySellProtection = BuySellProtection.BUY;

  private static final String protectionBuyerTicker = "MSFT";
  private static final String protectionBuyerShortName = "Microsoft";
  private static final String protectionBuyerREDCode = "ABC123";

  private static final String protectionSellerTicker = "IBM";
  private static final String protectionSellerShortName = "International Business Mac" +
      "hines";
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

  private static final ZonedDateTime startDate = DateUtils.getUTCDate(2012, 6, 20);
  private static final ZonedDateTime effectiveDate = DateUtils.getUTCDate(2012, 6, 20);
  private static final ZonedDateTime maturityDate = DateUtils.getUTCDate(2017, 9, 20);
  private static final ZonedDateTime valuationDate = DateUtils.getUTCDate(2012, 11, 15);

  private static final StubType stubType = StubType.FRONTSHORT;
  private static final PeriodFrequency couponFrequency = PeriodFrequency.QUARTERLY;
  private static final DayCount daycountFractionConvention = DayCountFactory.INSTANCE.getDayCount("ACT/360");
  private static final BusinessDayConvention businessdayAdjustmentConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");

  private static final boolean immAdjustMaturityDate = false;
  private static final boolean adjustEffectiveDate = true;
  private static final boolean adjustMaturityDate = true;

  private static final double notional = 8280000.0;
  private static final double recoveryRate = 1.0;
  private static final boolean includeAccruedPremium = false;
  private static final PriceType priceType = PriceType.DIRTY;
  private static final boolean protectionStart = true;

  private static final double parSpread = 100.0;

  // ----------------------------------------------------------------------------------

  // Dummy yield curve

  protected static DayCount s_act365 = new ActualThreeSixtyFive();

  final ZonedDateTime baseDate = zdt(2012, 11, 15, 0, 0, 0, 0, ZoneOffset.UTC);

  double[] times2 = {
      s_act365.getDayCountFraction(baseDate, baseDate.plusMonths(1)),
      s_act365.getDayCountFraction(baseDate, baseDate.plusMonths(2)),
      s_act365.getDayCountFraction(baseDate, baseDate.plusMonths(3)),
      s_act365.getDayCountFraction(baseDate, baseDate.plusMonths(6)),
      s_act365.getDayCountFraction(baseDate, baseDate.plusMonths(9)),
      s_act365.getDayCountFraction(baseDate, baseDate.plusYears(2)),
      s_act365.getDayCountFraction(baseDate, baseDate.plusYears(3)),
      s_act365.getDayCountFraction(baseDate, baseDate.plusYears(4)),
      s_act365.getDayCountFraction(baseDate, baseDate.plusYears(5)),
      s_act365.getDayCountFraction(baseDate, baseDate.plusYears(6)),
      s_act365.getDayCountFraction(baseDate, baseDate.plusYears(7)),
      s_act365.getDayCountFraction(baseDate, baseDate.plusYears(8)),
      s_act365.getDayCountFraction(baseDate, baseDate.plusYears(9)),
      s_act365.getDayCountFraction(baseDate, baseDate.plusYears(10)),
      s_act365.getDayCountFraction(baseDate, baseDate.plusYears(12)),
      s_act365.getDayCountFraction(baseDate, baseDate.plusYears(15)),
      s_act365.getDayCountFraction(baseDate, baseDate.plusYears(20)),
      s_act365.getDayCountFraction(baseDate, baseDate.plusYears(25)),
      s_act365.getDayCountFraction(baseDate, baseDate.plusYears(30))
  };

  double[] times = {
      s_act365.getDayCountFraction(baseDate, zdt(2012, 12, 17, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2013, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2013, 2, 17, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2013, 5, 15, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2013, 8, 15, 0, 0, 0, 0, ZoneOffset.UTC))
  };

  /*
  ,
  s_act365.getDayCountFraction(baseDate, zdt(2014, 11, 16, 0, 0, 0, 0, ZoneOffset.UTC)),
  s_act365.getDayCountFraction(baseDate, zdt(2015, 11, 15, 0, 0, 0, 0, ZoneOffset.UTC)),
  s_act365.getDayCountFraction(baseDate, zdt(2016, 11, 15, 0, 0, 0, 0, ZoneOffset.UTC)),
  s_act365.getDayCountFraction(baseDate, zdt(2017, 11, 15, 0, 0, 0, 0, ZoneOffset.UTC)),
  s_act365.getDayCountFraction(baseDate, zdt(2018, 11, 15, 0, 0, 0, 0, ZoneOffset.UTC)),
  s_act365.getDayCountFraction(baseDate, zdt(2019, 11, 17, 0, 0, 0, 0, ZoneOffset.UTC)),
  s_act365.getDayCountFraction(baseDate, zdt(2020, 11, 15, 0, 0, 0, 0, ZoneOffset.UTC)),
  s_act365.getDayCountFraction(baseDate, zdt(2021, 11, 15, 0, 0, 0, 0, ZoneOffset.UTC)),
  s_act365.getDayCountFraction(baseDate, zdt(2022, 11, 15, 0, 0, 0, 0, ZoneOffset.UTC)),
  s_act365.getDayCountFraction(baseDate, zdt(2024, 11, 17, 0, 0, 0, 0, ZoneOffset.UTC)),
  s_act365.getDayCountFraction(baseDate, zdt(2027, 11, 15, 0, 0, 0, 0, ZoneOffset.UTC)),
  s_act365.getDayCountFraction(baseDate, zdt(2032, 11, 15, 0, 0, 0, 0, ZoneOffset.UTC)),
  s_act365.getDayCountFraction(baseDate, zdt(2037, 11, 15, 0, 0, 0, 0, ZoneOffset.UTC)),
  s_act365.getDayCountFraction(baseDate, zdt(2042, 11, 16, 0, 0, 0, 0, ZoneOffset.UTC))
  };
  */

  double[] rates = {
      (new PeriodicInterestRate(0.002075, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.00257, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.00311, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.00523, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.006965, 1)).toContinuous().getRate()
  };

  //double[] rates = {0.0, 0.0, 0.0, 0.0, 0.0 };

  /*
      (new PeriodicInterestRate(0.00377, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.00451, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.00583, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.00763, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.00962, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.01155, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.01329, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.01488, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.01638, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.01879, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.02122, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.02318, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.02420, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.02481, 1)).toContinuous().getRate(),
  };
  */

  final double flatRate = 0.0;

  double[] flatRates = {
      flatRate,
      flatRate,
      flatRate,
      flatRate,
      flatRate,
      flatRate,
      flatRate,
      flatRate,
      flatRate,
      flatRate,
      flatRate,
      flatRate,
      flatRate,
      flatRate,
      flatRate,
      flatRate,
      flatRate,
      flatRate,
      flatRate };

  // Use an ISDACurve object (from RiskCare implementation) for the yield curve
  ISDACurve yieldCurve = new ISDACurve("IR_CURVE", times, rates, s_act365.getDayCountFraction(valuationDate, baseDate));

  //ISDACurve yieldCurve = new ISDACurve("IR_CURVE", times, flatRates, s_act365.getDayCountFraction(valuationDate, baseDate));

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

  // --------------------------------------------------------------------------------------------------------------------------------------------------

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

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Test to demonstrate calibration of a hazard rate curve to a term structure of market data

  @Test
  public void testCalibrateHazardRateCurveFlatTermStructure() {

    // -------------------------------------------------------------------------------------

    if (outputResults) {
      System.out.println("Running Hazard Rate Curve Calibration test ...");
    }

    // -------------------------------------------------------------------------------------

    /*
    final GenerateCreditDefaultSwapPremiumLegSchedule cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    // Build the premium leg cashflow schedule from the contract specification
    final ZonedDateTime[] premiumLegSchedule = cashflowSchedule.constructCreditDefaultSwapPremiumLegSchedule(cds);

    for (int i = 0; i < premiumLegSchedule.length; i++) {
      System.out.println("i = " + "\t" + i + "\t" + premiumLegSchedule[i]);
    }
    */

    // Define the market data to calibrate to

    // The number of CDS instruments used to calibrate against
    final int numberOfCalibrationCDS = 1;

    // The CDS tenors to calibrate to
    final ZonedDateTime[] tenors = new ZonedDateTime[numberOfCalibrationCDS];

    tenors[0] = DateUtils.getUTCDate(2013, 6, 20);

    /*
    tenors[1] = DateUtils.getUTCDate(2013, 12, 20);
    tenors[2] = DateUtils.getUTCDate(2014, 12, 20);
    tenors[3] = DateUtils.getUTCDate(2015, 12, 20);
    tenors[4] = DateUtils.getUTCDate(2016, 12, 20);
    tenors[5] = DateUtils.getUTCDate(2017, 12, 20);
    tenors[6] = DateUtils.getUTCDate(2019, 12, 20);
    tenors[7] = DateUtils.getUTCDate(2022, 12, 20);
    */

    // The market observed par CDS spreads at these tenors
    final double[] marketSpreads = new double[numberOfCalibrationCDS];

    final double flatSpread = 0.0000001; //550.0;

    /*
    marketSpreads[0] = 128.76;
    marketSpreads[1] = 164.62;
    marketSpreads[2] = 263.77;
    marketSpreads[3] = 333.90;
    marketSpreads[4] = 373.52;
    marketSpreads[5] = 406.02;
    marketSpreads[6] = 422.60;
    marketSpreads[7] = 432.96;
    */

    marketSpreads[0] = flatSpread;

    /*
    marketSpreads[1] = flatSpread;
    marketSpreads[2] = flatSpread;
    marketSpreads[3] = flatSpread;
    marketSpreads[4] = flatSpread;
    marketSpreads[5] = flatSpread;
    marketSpreads[6] = flatSpread;
    marketSpreads[7] = flatSpread;
    */

    // The recovery rate assumption used in the PV calculations when calibrating
    final double calibrationRecoveryRate = 0.25;

    // -------------------------------------------------------------------------------------

    // Create a calibration CDS (will be a modified version of the baseline CDS)
    LegacyVanillaCreditDefaultSwapDefinition calibrationCDS = cds;

    // Set the recovery rate of the calibration CDS used for the curve calibration (this appears in the calculation of the contingent leg)
    calibrationCDS = calibrationCDS.withRecoveryRate(calibrationRecoveryRate);

    // -------------------------------------------------------------------------------------

    // Create a calibrate survival curve object
    final CalibrateHazardRateCurveLegacyCreditDefaultSwap hazardRateCurve = new CalibrateHazardRateCurveLegacyCreditDefaultSwap();

    // Calibrate the hazard rate curve to the market observed par CDS spreads (returns calibrated hazard rates as a vector of doubles)
    //final double[] calibratedHazardRateCurve = hazardRateCurve.getCalibratedHazardRateTermStructure(valuationDate, calibrationCDS, tenors, marketSpreads, yieldCurve, priceType);

    // -------------------------------------------------------------------------------------

    /*
    // Vector of time nodes for the hazard rate curve
    final double[] times = new double[tenors.length + 1];

    times[0] = 0.0;
    for (int m = 1; m <= tenors.length; m++) {
      times[m] = ACT_365.getDayCountFraction(valuationDate, tenors[m - 1]);
    }

    double[] modifiedHazardRateCurve = new double[calibratedHazardRateCurve.length + 1];

    modifiedHazardRateCurve[0] = calibratedHazardRateCurve[0];

    for (int m = 1; m < calibratedHazardRateCurve.length; m++) {
      modifiedHazardRateCurve[m] = calibratedHazardRateCurve[m - 1];
    }
    */

    // Build a hazard rate curve object based on the input market data
    //final HazardRateCurve newCalibratedHazardRateCurve = new HazardRateCurve(times, modifiedHazardRateCurve, 0.0);

    // -------------------------------------------------------------------------------------

    // Call the constructor to create a CDS present value object
    //final PresentValueLegacyVanillaCreditDefaultSwap creditDefaultSwap = new PresentValueLegacyVanillaCreditDefaultSwap();

    // Calculate the CDS MtM
    //final double presentValue = creditDefaultSwap.getPresentValueLegacyVanillaCreditDefaultSwap(valuationDate, cds, yieldCurve, newCalibratedHazardRateCurve, priceType);

    if (outputResults) {
      for (int i = 0; i < numberOfCalibrationCDS; i++) {
        // System.out.println(calibratedHazardRateCurve[i]);
      }

      //System.out.println("PV = " + presentValue);
    }

    // -------------------------------------------------------------------------------------

  }

  //-------------------------------------------------------------------------
  private static ZonedDateTime zdt(int y, int m, int d, int hr, int min, int sec, int nanos, ZoneId zone) {
    return LocalDateTime.of(y, m, d, hr, min, sec, nanos).atZone(zone);
  }

}
