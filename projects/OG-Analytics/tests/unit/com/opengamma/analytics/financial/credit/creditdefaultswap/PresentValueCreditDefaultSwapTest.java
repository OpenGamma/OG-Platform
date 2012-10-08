/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.CalibrateHazardRate;
import com.opengamma.analytics.financial.credit.CreditRating;
import com.opengamma.analytics.financial.credit.CreditRatingFitch;
import com.opengamma.analytics.financial.credit.CreditRatingMoodys;
import com.opengamma.analytics.financial.credit.CreditRatingStandardAndPoors;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.Obligor;
import com.opengamma.analytics.financial.credit.Region;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.Sector;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.SurvivalCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.GenerateCreditDefaultSwapPremiumLegSchedule;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.PresentValueCreditDefaultSwap;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.math.random.NormalRandomNumberGenerator;
import com.opengamma.analytics.math.statistics.descriptive.PercentileCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
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

  private static final ZonedDateTime startDate = DateUtils.getUTCDate(2007, 10, 22);
  private static final ZonedDateTime effectiveDate = DateUtils.getUTCDate(2007, 10, 23);
  private static final ZonedDateTime maturityDate = DateUtils.getUTCDate(2012, 12, 20);
  private static final ZonedDateTime valuationDate = DateUtils.getUTCDate(2007, 10, 23);

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

  // Dummy yield curve
  private static final double interestRate = 0.0;
  private static final double[] TIME = new double[] {0, 3, 5, 10, 15, 40 };
  private static final double[] RATES = new double[] {interestRate, interestRate, interestRate, interestRate, interestRate, interestRate };
  private static final InterpolatedDoublesCurve R = InterpolatedDoublesCurve.from(TIME, RATES, new LinearInterpolator1D());
  private static final YieldCurve yieldCurve = YieldCurve.from(R);

  // Construct a survival curve based on a flat hazard rate term structure (for testing purposes only)
  private static final double hazardRate = (premiumLegCoupon / 10000.0) / (1 - recoveryRate);
  private static final double[] tenorsAsDoubles = new double[] {5 };
  private static final double[] hazardRates = new double[] {hazardRate };
  private static final SurvivalCurve flatSurvivalCurve = new SurvivalCurve(tenorsAsDoubles, hazardRates);

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
      includeAccruedPremium);

  // -----------------------------------------------------------------------------------------------

  // Simple test to compute the PV of a CDS assuming a flat term structure of market observed CDS par spreads

  //@Test
  public void testPresentValueCreditDefaultSwapFlatSurvivalCurve() {

    // -----------------------------------------------------------------------------------------------

    final boolean outputResults = false;

    double presentValue = 0.0;

    if (outputResults) {
      System.out.println("Running CDS PV test (with a simple flat survival curve) ...");
    }

    // -----------------------------------------------------------------------------------------------

    // Call the constructor to create a CDS whose PV we will compute
    final PresentValueCreditDefaultSwap creditDefaultSwap = new PresentValueCreditDefaultSwap();

    // Call the CDS PV calculator (with a flat survival curve) to get the current PV
    presentValue = creditDefaultSwap.getPresentValueCreditDefaultSwap(cds, yieldCurve, flatSurvivalCurve);

    if (outputResults) {
      System.out.println("CDS PV = " + presentValue);
    }

  }

  // -----------------------------------------------------------------------------------------------

  // Simple test to calibrate a single name CDS to a term structure of market observed par CDS spreads and compute the PV

  //@Test
  public void testPresentValueCreditSwapCalibratedSurvivalCurve() {

    // -----------------------------------------------------------------------------------------------

    final boolean outputResults = false;

    double presentValue = 0.0;

    if (outputResults) {
      System.out.println("Running CDS PV test (with a calibrated survival curve) ...");
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
    final SurvivalCurve survivalCurve = new SurvivalCurve(tenorsAsDoubles, calibratedHazardRateTermStructure);

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

  //@Test
  public void testPresentValueCreditDefaultSwapTimeDecay() {

    // -----------------------------------------------------------------------------------------------

    final boolean outputResults = false;

    double presentValue = 0.0;

    if (outputResults) {
      System.out.println("Running CDS PV time decay test ...");
    }

    // -----------------------------------------------------------------------------------------------

    // Create a valuation CDS whose valuationDate will vary (will be a modified version of the baseline CDS)
    CreditDefaultSwapDefinition valuationCDS = cds;

    // Call the constructor to create a CDS whose PV we will compute
    final PresentValueCreditDefaultSwap creditDefaultSwap = new PresentValueCreditDefaultSwap();

    // Call the CDS PV calculator to get the current PV
    presentValue = creditDefaultSwap.getPresentValueCreditDefaultSwap(cds, yieldCurve, flatSurvivalCurve);

    if (outputResults) {
      System.out.println(valuationDate + "\t" + presentValue);
    }

    // -----------------------------------------------------------------------------------------------

    // start at the initial valuation date
    ZonedDateTime rollingValuationDate = cds.getValuationDate();

    while (!rollingValuationDate.isAfter(cds.getMaturityDate().minusDays(10))) {

      // Roll the current valuation date
      rollingValuationDate = rollingValuationDate.plusDays(1);

      // Modify the CDS's valuation date
      valuationCDS = valuationCDS.withValuationDate(rollingValuationDate);

      // Calculate the CDS PV
      presentValue = creditDefaultSwap.getPresentValueCreditDefaultSwap(valuationCDS, yieldCurve, flatSurvivalCurve);

      if (outputResults) {
        System.out.println(rollingValuationDate + "\t" + presentValue);
      }
    }

    // -----------------------------------------------------------------------------------------------
  }

  // -----------------------------------------------------------------------------------------------

  @Test
  public void testPFECalculation() {

    // -----------------------------------------------------------------------------------------------

    int counter = 0;
    int numberOfSimulations = 1;

    double[] presentValue = new double[1];

    double mu = 0.90;
    double sigma = 1.0;

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
      presentValue[0] = creditDefaultSwap.getPresentValueCreditDefaultSwap(cds, yieldCurve, flatSurvivalCurve);

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
        double h = hazardRate * Math.exp((mu - 0.5 * sigma * sigma) * t + sigma * Math.sqrt(t) * epsilon[counter]);

        //System.out.println(currentTimenode + "\t" + t + "\t" + h);

        // Build a vector with this simulated value in
        double[] simulatedHazardRates = new double[] {h };

        // Construct a survival curve from this simulated rate
        SurvivalCurve simulatedSurvivalCurve = new SurvivalCurve(tenorsAsDoubles, simulatedHazardRates);

        // Roll the valuation date of the CDS to the current timenode
        rollingCDS = rollingCDS.withValuationDate(currentTimenode);

        // Re-val the CDS given the simulated hazard rate at the new timenode
        presentValue[0] = creditDefaultSwap.getPresentValueCreditDefaultSwap(rollingCDS, yieldCurve, simulatedSurvivalCurve);

        counter++;
      }
    }

    for (int i = 0; i < numberOfTimenodes; i++) {

      double[] x = new double[numberOfTimenodes + 1];

      for (int alpha = 0; alpha < numberOfSimulations; alpha++) {
        x[alpha] = results[i][alpha];
      }

      potentialFutureExposure[i] = quantile.evaluate(x);

      //System.out.println(i + "\t" + potentialFutureExposure[i]);
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