/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.cds;

import static org.threeten.bp.temporal.ChronoUnit.YEARS;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.threeten.bp.Duration;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.TemporalAdjuster;

import com.opengamma.analytics.financial.instrument.Convention;
import com.opengamma.analytics.financial.instrument.cds.ISDACDSDefinition;
import com.opengamma.analytics.financial.instrument.cds.ISDACDSPremiumDefinition;
import com.opengamma.analytics.financial.interestrate.PeriodicInterestRate;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.FollowingBusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.ActualThreeSixtyFive;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.util.money.Currency;


/**
 * Test harness to run through the official ISDA test grids
 */
public class ISDATestGridHarness {

  private static final Logger s_logger = LoggerFactory.getLogger(ISDATestGridHarness.class);

  private static final double cleanPercentageErrorLimit = 1E-8;
  private static final double dirtyAbsoluteErrorLimit = 1E-5; // One thousandth of a cent

  // In the case of an absolute error failure, optionally consider the relative error
  private static final double dirtyRelativeErrorConsideration = 1E-10;
  private static final boolean considerRelativeErrorForFailures = true;

  // This test file is missing the last four d.p. for each result
  final String lowResTest = "EUR_20090525.xls";
  final double lowResAbsoluteErrorLimit = 1E-1;
  final double lowResPercentageErrorLimit = 1E-6;

  // The ISDA approximate method is not expected to give exact results in 100% of cases
  // These limits are a sanity check and will trigger assert failures, intended to give warning of serious regressions
  // Minor changes, e.g. changing the root finding algorithm, should not violate these limits
  final double SANITY_FAILURE_LIMIT = 1.0 / 10000.0;
  final double SANITY_MARGINAL_LIMIT = 5.0 / 10000.0;
  final double SANITY_RELATIVE_ERROR_LIMIT = 1E-8;

  private static final Map<String,String[]> selectedUnitTestGrids;
  static {
    selectedUnitTestGrids = new HashMap<>();
    selectedUnitTestGrids.put("benchmark", new String[] { "USD_20090911.xls", "HKD_20090908.xls", "SGD_20090909.xls" } );
    selectedUnitTestGrids.put("corporate", new String[] { "CAD_20090501.xls", "CHF_20090507.xls", "EUR_20090525.xls", "GBP_20090512.xls", "JPY_20090526.xls", "USD_20090528.xls" } );
  }

  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DayCount dayCount = new ActualThreeSixtyFive();
  private static final ISDAApproxCDSPricingMethod calculator = new ISDAApproxCDSPricingMethod();

  private final ISDATestGridManager testGridManager;
  private final ISDAStagedDataManager stagedDataManager;

  private class TestResult {
    public final double dirty;
    public final double dirtyExpected;
    public final double dirtyAbsoluteError;
    public final double dirtyRelativeError;

    public final double clean;
    public final double cleanExpected;
    public final double cleanPercentageError;

    @SuppressWarnings("hiding")
    public TestResult (final double dirty, final double dirtyExpected, final double dirtyAbsoluteError, final double dirtyRelativeError,
      final double clean, final double cleanExpected, final double cleanPercentageError) {

      this.dirty = dirty;
      this.dirtyExpected = dirtyExpected;
      this.dirtyAbsoluteError = dirtyAbsoluteError;
      this.dirtyRelativeError = dirtyRelativeError;

      this.clean = clean;
      this.cleanExpected = cleanExpected;
      this.cleanPercentageError = cleanPercentageError;
    }
  }

  private class TestGridResult {
    public final int cases;
    public final int failures;
    public final int marginals;
    public final double seconds;
    public final double maxAbsoluteError;
    public final double maxRelativeError;
    public final double maxCleanPercentageError;

    @SuppressWarnings("hiding")
    public TestGridResult(final int cases, final int failures, final int marginals, final double seconds, final double maxAbsoluteError, final double maxRelativeError, final double maxCleanPercentageError) {
      this.cases = cases;
      this.failures = failures;
      this.marginals = marginals;
      this.seconds = seconds;
      this.maxAbsoluteError = maxAbsoluteError;
      this.maxRelativeError = maxRelativeError;
      this.maxCleanPercentageError = maxCleanPercentageError;
    }
  }

  public static void main(final String[] args) {
    try {
      final ISDATestGridHarness harness = new ISDATestGridHarness();
      harness.runAllTestGrids();
    }
    catch (final Throwable e) {
      System.err.println("Unexpected error: " + e.getMessage());
      System.exit(-1);
    }
  }

  public ISDATestGridHarness() {
    testGridManager = new ISDATestGridManager();
    stagedDataManager = new ISDAStagedDataManager();
  }

  @Test
  public void runSelectedTestGrids() throws Exception {
    runTestGrids(selectedUnitTestGrids, false);
  }

  @Test(groups="slow")
  public void runAllTestGrids() throws Exception {
	  runTestGrids(testGridManager.findAllTestGrids(), false);
  }

  public void runTestGrids(final Map<String,String[]> testGrids, final boolean fastFilter) throws Exception {

    String classification;
	  ISDATestGrid testGrid;
	  ISDAStagedCurve stagedCurve;
	  ISDACurve discountCurve;
	  TestGridResult result;

	  long grids = 0, gridFailures = 0, gridsMarginal = 0, gridsMissingData = 0, cases = 0, caseFailures = 0, casesMarginal = 0;
    double totalTime = 0.0;
	  double maxAbsoluteError = 0.0;
	  double maxRelativeError = 0.0;
	  double maxCleanPercentageError = 0.0;

	  final Set<String> testGridFilter = new HashSet<>();
	  final Set<String> failedGrids = new HashSet<>();
	  final Set<String> marginalGrids = new HashSet<>();

	  for (final Entry<String, String[]> batch : testGrids.entrySet()) {
  	  for (final String fileName : batch.getValue()) {

  	    // If fast filter is set, only run one grid for each category/currency combination
  	    if (fastFilter) {
  	      classification = batch.getKey() + ":" + fileName.substring(0, 3);

          if (testGridFilter.contains(classification)) {
            continue;
          }
          testGridFilter.add(classification);
        }

        // Load the IR curve for the current grid
  		  testGrid = testGridManager.loadTestGrid(batch.getKey(), fileName);
  		  stagedCurve = stagedDataManager.loadStagedCurveForGrid(fileName);

  		  if (stagedCurve == null) {
  		    ++gridsMissingData;
  		    s_logger.debug("Skipping test grid: " + batch.getKey() + " " + fileName + " (missing IR curve)");
  		    continue;
  		  }

  		  // Run the grid
  		  s_logger.debug("Running test grid: " + batch.getKey() + " " + fileName);
  		  discountCurve = buildCurve(stagedCurve, "IR_CURVE");
  		  result = runTestGrid(testGrid, discountCurve, fileName);

  		  // Record results
  		  ++grids;
  		  gridFailures += result.failures > 0 ? 1 : 0;
  		  gridsMarginal += result.failures == 0 && result.marginals > 0 ? 1 : 0;
  		  cases += result.cases;
  		  caseFailures += result.failures;
  		  casesMarginal += result.marginals;
  		  totalTime += result.seconds;
  		  maxAbsoluteError = result.maxAbsoluteError > maxAbsoluteError ? result.maxAbsoluteError : maxAbsoluteError;
  		  maxRelativeError = result.maxRelativeError > maxRelativeError ? result.maxRelativeError : maxRelativeError;
  		  maxCleanPercentageError = result.maxCleanPercentageError > maxCleanPercentageError ? result.maxCleanPercentageError : maxCleanPercentageError;

  		  if (result.failures > 0) {
  		    failedGrids.add(batch.getKey() + " " + fileName + " (" + result.failures + " failures"
  		      + (considerRelativeErrorForFailures ? ", " + result.marginals + " marginal" : "")
  		      +")");
  		  } else if (result.marginals > 0) {
  		    marginalGrids.add(batch.getKey() + " " + fileName + " (" + result.marginals + " marginal cases)");
  		  }
  	  }
	  }

	  final double failureRate = (double)caseFailures / (double)cases;
	  final double marginalRate = (double)(caseFailures + casesMarginal) / (double)cases;

	  s_logger.info(" --- ISDA Test Grid run complete --- ");
	  s_logger.info("Failure rate: " + (failureRate * 100.0) + "%");
	  s_logger.info("Marginal rate: " + (marginalRate * 100.0) + "%");
	  s_logger.info("Largest dirty absolute error: " + maxAbsoluteError);
	  s_logger.info("Largest dirty relative error: " + maxRelativeError);
	  s_logger.info("Largest clean percentage error: " + maxCleanPercentageError);
	  s_logger.info("Total test grids executed: " + grids);
	  s_logger.info("Total test grids failed: " + gridFailures);
	  s_logger.info("Total test grids marginal: " + gridsMarginal);
	  s_logger.info("Total test grids with missing data: " + gridsMissingData);
	  s_logger.info("Total test cases executed: " + cases);
	  s_logger.info("Total test cases failed: " + caseFailures);
	  s_logger.info("Total test cases marginal: " + casesMarginal);
	  s_logger.info("Total execution time: " + totalTime + "s");

	  if (!failedGrids.isEmpty()) {
	    s_logger.info("Failed grids:");
	  }

	  for (final String failedGrid : failedGrids) {
	    s_logger.info(failedGrid);
	  }

	  if (!marginalGrids.isEmpty()) {
	    s_logger.info("Marginal grids:");
	  }

	  for (final String marginalGrid : marginalGrids) {
	    s_logger.info(marginalGrid);
	  }

	  s_logger.info( " --- ISDA Test Grid run complete --- ");

	  // Break the test run if sanity limits are exceeded
	  Assert.assertTrue(failureRate <= SANITY_FAILURE_LIMIT, "Sanity limit exceeded: " + (failureRate * 100.0) + "% of test cases failed (largest acceptable value is " + (SANITY_FAILURE_LIMIT * 100.0) + "%)");
	  Assert.assertTrue(marginalRate <= SANITY_MARGINAL_LIMIT, "Sanity limit exceeded: " + (marginalRate * 100.0) + "% of test cases marginal (largest acceptable value is " + (SANITY_MARGINAL_LIMIT * 100.0) + "%)");
	  Assert.assertTrue(maxRelativeError <= SANITY_RELATIVE_ERROR_LIMIT, "Sanity limit exceeded: Maximum relative error is too high (largest acceptable value is " + SANITY_RELATIVE_ERROR_LIMIT + ")");
  }

  public TestGridResult runTestGrid(final ISDATestGrid testGrid, final ISDACurve discountCurve, final String testGridFileName) throws Exception {

    if (lowResTest.equals(testGridFileName)) {
      s_logger.debug("This is a low resolution test file, using low resolution error limits");
    }

    final double absoluteErrorLimit = lowResTest.equals(testGridFileName) ? lowResAbsoluteErrorLimit : dirtyAbsoluteErrorLimit;
    final double percentageErrorLimit = lowResTest.equals(testGridFileName) ? lowResPercentageErrorLimit : cleanPercentageErrorLimit;

    int i = 0, failures = 0, marginalCases = 0;
    TestResult result;
    double maxAbsoluteError = 0.0;
    double maxRelativeError = 0.0;
    double maxCleanPercentageError = 0.0;

    final ZonedDateTime start = ZonedDateTime.now();

    for (final ISDATestGridRow testCase : testGrid.getData()) {

      // Handle bad rows in the test data
      if (!testCase.isTestValid()) {
        continue;
      }

      result = runTestCase(testCase, discountCurve);

      if (result.dirtyAbsoluteError > absoluteErrorLimit || result.cleanPercentageError > percentageErrorLimit) {

        if (considerRelativeErrorForFailures && result.dirtyRelativeError <= dirtyRelativeErrorConsideration && result.cleanPercentageError <= percentageErrorLimit) {

          ++marginalCases;
          s_logger.debug("Test case marginal: " + testGridFileName + " row " + (i+2) + ": "
            + "dirty = " + result.dirty + " (exepcted = " + result.dirtyExpected + "), "
            + "clean = " + result.clean + " (expected = " + result.cleanExpected + "), "
            + "absolute error = " + result.dirtyAbsoluteError + ", relative error = " + result.dirtyRelativeError + ", percentage error = " + result.cleanPercentageError);

        } else {

          ++failures;
          s_logger.debug("Test case exceeds bounds: " + testGridFileName + " row " + (i+2) + ": "
            + "dirty = " + result.dirty + " (exepcted = " + result.dirtyExpected + "), "
            + "clean = " + result.clean + " (expected = " + result.cleanExpected + "), "
            + "absolute error = " + result.dirtyAbsoluteError + ", relative error = " + result.dirtyRelativeError + ", percentage error = " + result.cleanPercentageError);
        }
      }

      maxAbsoluteError = result.dirtyAbsoluteError > maxAbsoluteError ? result.dirtyAbsoluteError : maxAbsoluteError;
      maxRelativeError = result.dirtyRelativeError > maxRelativeError ? result.dirtyRelativeError : maxRelativeError;
      maxCleanPercentageError = result.cleanPercentageError > maxCleanPercentageError ? result.cleanPercentageError : maxCleanPercentageError;

      ++i;
    }

    final ZonedDateTime stop = ZonedDateTime.now();
    final Duration elapsedTime = Duration.between(start, stop);
    final double seconds = elapsedTime.getSeconds() + (elapsedTime.getNano() / 1000000) / 1000.0;

    s_logger.debug( "Executed " + i + " test cases in " + seconds + "s with " + failures + " failure(s)"
      + (considerRelativeErrorForFailures ? " and " + marginalCases + " marginal case(s)" : "")
      + ", largest dirty absolute error was " + maxAbsoluteError
      + ", largest dirty relative error was " + maxRelativeError
      + ", largest clean percentage error was " + maxCleanPercentageError);

    return new TestGridResult(i, failures, marginalCases, seconds, maxAbsoluteError, maxRelativeError, maxCleanPercentageError);
  }

  @SuppressWarnings("deprecation")
  public TestResult runTestCase(final ISDATestGridRow testCase, final ISDACurve discountCurve) {

    final BusinessDayConvention businessDays = new FollowingBusinessDayConvention();
    final Calendar calendar = new MondayToFridayCalendar("TestCalendar");
    final Convention convention = new Convention(3, dayCount, businessDays, calendar, "");
    final TemporalAdjuster adjuster = businessDays.getTemporalAdjuster(calendar);

    final ZonedDateTime pricingDate = testCase.getTradeDate().atStartOfDay(ZoneOffset.UTC);
    final ZonedDateTime maturity = testCase.getMaturityDate().atStartOfDay(ZoneOffset.UTC);

    // Step-in date is always T+1 calendar
    final ZonedDateTime stepinDate = pricingDate.plusDays(1);

    // If settlement date is not supplied, use T+3 business days
    final ZonedDateTime settlementDate = testCase.getCashSettle() != null
      ? testCase.getCashSettle().atStartOfDay(ZoneOffset.UTC)
      : pricingDate.plusDays(1).with(adjuster).plusDays(1).with(adjuster).plusDays(1).with(adjuster);

    // If start date is not supplied, construct one that is before the pricing date
    final Period yearsToMaturity = Period.ZERO.plusYears(YEARS.between(pricingDate, maturity));
    final ZonedDateTime startDate = testCase.getStartDate() != null
      ? testCase.getStartDate().atStartOfDay(ZoneOffset.UTC)
      : maturity.minusYears(yearsToMaturity.getYears() + 1).with(adjuster);

    // Spread and recovery are always given
    final double spread = testCase.getCoupon() / 10000.0;
    final double recoveryRate = testCase.getRecoveryRate();

    // Assume 1 billion notional, quarterly premiums and ACT360 day count
    final double notional = 1000000000;
    final Frequency couponFrequency = SimpleFrequency.QUARTERLY;
    final StubType stubType = StubType.SHORT_START;

    // Now build the CDS object
    final ISDACDSPremiumDefinition premiumDefinition = ISDACDSPremiumDefinition.from(startDate, maturity, couponFrequency, convention, stubType, /* protect start */ true, /*notional*/ 1.0, spread, Currency.EUR, calendar);
    final ISDACDSDefinition cdsDefinition = new ISDACDSDefinition(startDate, maturity, premiumDefinition, /*notional*/1.0, spread, recoveryRate, /* accrualOnDefault */ true, /* payOnDefault */ true, /* protectStart */ true, couponFrequency, convention, stubType);
    final ISDACDSDerivative cds = cdsDefinition.toDerivative(pricingDate, stepinDate, settlementDate, "IR_CURVE");

    // Par spread is always supplied
    final double marketSpread = testCase.getQuotedSpread() / 10000.0;

    // Now go price
    final double dirtyPrice = calculator.calculateUpfrontCharge(cds, discountCurve, marketSpread, false, pricingDate, stepinDate, settlementDate, calendar);
    final double cleanPrice = calculator.calculateUpfrontCharge(cds, discountCurve, marketSpread, true, pricingDate, stepinDate, settlementDate, calendar);

    final double dirtyExpected = testCase.getUpfront();
    final double dirtyAbsoluteError = Math.abs(notional * dirtyPrice - dirtyExpected);
    final double dirtyRelativeError = Math.abs(dirtyAbsoluteError / dirtyExpected);

    final double cleanPercentage = 100.0 - (cleanPrice * 100.0);
    final double cleanExpected = testCase.getCleanPrice();
    final double cleanPercentageError = Math.abs(cleanPercentage - cleanExpected);

    return new TestResult(notional * dirtyPrice, dirtyExpected, dirtyAbsoluteError, dirtyRelativeError, cleanPercentage, cleanExpected, cleanPercentageError);
  }

  public ISDACurve buildCurve(final ISDAStagedCurve curveData, final String curveName) {

    // Expect all curve objects to use annual compounding
    // Assert.assertEquals(Double.valueOf(curveData.getBasis()), 1.0);

    final LocalDate effectiveDate = LocalDate.parse(curveData.getEffectiveDate(), formatter);
    final LocalDate baseDate = LocalDate.parse(curveData.getSpotDate(), formatter);
    final double offset = dayCount.getDayCountFraction(effectiveDate, baseDate);

    final int nPoints = curveData.getPoints().size();
    final double times[] = new double[nPoints];
    final double rates[] = new double[nPoints];

    LocalDate date;
    Double rate;
    int i = 0;

    for (final ISDAStagedCurve.Point dataPoint : curveData.getPoints()) {
      date = LocalDate.parse(dataPoint.getDate(), formatter);
      rate = (new PeriodicInterestRate(Double.valueOf(dataPoint.getRate()),1)).toContinuous().getRate();
      times[i] = dayCount.getDayCountFraction(baseDate, date);
      rates[i++] = rate;
    }

    return new ISDACurve(curveName, times, rates, offset);
  }

}
