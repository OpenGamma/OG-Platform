/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.cds;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatters;
import javax.time.Duration;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.Convention;
import com.opengamma.analytics.financial.instrument.cds.ISDACDSDefinition;
import com.opengamma.analytics.financial.instrument.cds.ISDACDSPremiumDefinition;
import com.opengamma.analytics.financial.interestrate.PeriodicInterestRate;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.FollowingBusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.ActualThreeSixty;
import com.opengamma.financial.convention.daycount.ActualThreeSixtyFive;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.util.money.Currency;


public class ISDATestGridHarness {
  
  private static final double dirtyAbsoluteErrorLimit = 1E-5; // One thousandth of a cent
  private static final double cleanPercentageErrorLimit = 1E-8;
  
  // This test file is missing the last four d.p. for each result
  final String lowResTest = "EUR_20090525.xls";
  final double lowResAbsoluteErrorLimit = 1E-1;
  final double lowResPercentageErrorLimit = 1E-6;
  
  private static final Map<String,String[]> selectedUnitTestGrids;
  static {
    selectedUnitTestGrids = new HashMap<String,String[]>();
    selectedUnitTestGrids.put("benchmark", new String[] { "USD_20090911.xls", "HKD_20090908.xls", "SGD_20090909.xls" } );
    selectedUnitTestGrids.put("corporate", new String[] { "CAD_20090501.xls", "CHF_20090507.xls", "EUR_20090525.xls", "GBP_20090512.xls", "JPY_20090526.xls", "USD_20090528.xls" } );
  }
  
  private static final DateTimeFormatter formatter = DateTimeFormatters.pattern("dd/MM/yyyy");
  private static final DayCount dayCount = new ActualThreeSixtyFive();
  private static final ISDAApproxCDSPricingMethod calculator = new ISDAApproxCDSPricingMethod();
  
  private ISDATestGridManager testGridManager;
  private ISDAStagedDataManager stagedDataManager;
  
  private class TestResult {
    public final double dirty;
    public final double dirtyExpected;
    public final double dirtyAbsoluteError;
    public final double dirtyRelativeError;
    
    public final double clean;
    public final double cleanExpected;
    public final double cleanPercentageError;
    
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
    public final double seconds;
    public final double maxAbsoluteError;
    public final double maxRelativeError;
    public final double maxCleanPercentageError;
    
    public TestGridResult(final int cases, final int failures, final double seconds, final double maxAbsoluteError, final double maxRelativeError, final double maxCleanPercentageError) {
      this.cases = cases;
      this.failures = failures;
      this.seconds = seconds;
      this.maxAbsoluteError = maxAbsoluteError;
      this.maxRelativeError = maxRelativeError;
      this.maxCleanPercentageError = maxCleanPercentageError;
    }
  }
  
  public static void main(String[] args) {
    try {
      ISDATestGridHarness harness = new ISDATestGridHarness();
      harness.runAllTestGrids();
    }
    catch (Throwable e) {
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
	  
	  long grids = 0, gridFailures = 0, gridsMissingData = 0, cases = 0, caseFailures = 0;
    double totalTime = 0.0;
	  double maxAbsoluteError = 0.0;
	  double maxRelativeError = 0.0;
	  double maxCleanPercentageError = 0.0;
	  
	  Set<String> testGridFilter = new HashSet<String>();
	  Set<String> failedGrids = new HashSet<String>();
	  
	  for (Entry<String, String[]> batch : testGrids.entrySet()) {
  	  for (String fileName : batch.getValue()) {
  	    
  	    // If fast filter is set, only run one grid for each category/currency combination
  	    if (fastFilter) {   
  	      classification = batch.getKey() + ":" + fileName.substring(0, 3);
        
          if (testGridFilter.contains(classification))
            continue;
          else
            testGridFilter.add(classification);
        }
  	    
        // Load the IR curve for the current grid
  		  testGrid = testGridManager.loadTestGrid(batch.getKey(), fileName);
  		  stagedCurve = stagedDataManager.loadStagedCurveForGrid(fileName);
  		  
  		  if (stagedCurve == null) {
  		    ++gridsMissingData;
  		    System.out.println("Skipping test grid: " + batch.getKey() + " " + fileName + " (missing IR curve)");
  		    continue;
  		  }
  		  
  		  // Run the grid
  		  System.out.println("Running test grid: " + batch.getKey() + " " + fileName);
  		  discountCurve = buildCurve(stagedCurve, "IR_CURVE");
  		  result = runTestGrid(testGrid, discountCurve, fileName);
  		  
  		  // Record results
  		  ++grids;
  		  gridFailures += result.failures > 0 ? 1 : 0;
  		  cases += result.cases;
  		  caseFailures += result.failures;
  		  totalTime += result.seconds;
  		  maxAbsoluteError = result.maxAbsoluteError > maxAbsoluteError ? result.maxAbsoluteError : maxAbsoluteError;
  		  maxRelativeError = result.maxRelativeError > maxRelativeError ? result.maxRelativeError : maxRelativeError;
  		  maxCleanPercentageError = result.maxCleanPercentageError > maxCleanPercentageError ? result.maxCleanPercentageError : maxCleanPercentageError;
  		  
  		  if (result.failures > 0) {
  		    failedGrids.add(batch.getKey() + " " + fileName + " (" + result.failures + " failures)");
  		  }
  	  }
	  }
	  
	  System.out.println(" --- ISDA Test Grid run complete --- ");
	  System.out.println("Total execution time: " + totalTime + "s");
	  System.out.println("Total test grids with missing data: " + gridsMissingData);
	  System.out.println("Total test grids executed: " + grids);
	  System.out.println("Total test cases executed: " + cases);
	  System.out.println("Total test grids failed: " + gridFailures);
	  System.out.println("Total test cases failed: " + caseFailures);
	  System.out.println("Largest dirty absolute error: " + maxAbsoluteError);
	  System.out.println("Largest dirty relative error: " + maxRelativeError);
	  System.out.println("Largest clean percentage error: " + maxCleanPercentageError);
	  
	  System.out.println("Failed grids:");
	  
	  for (String failedGrid : failedGrids) {
	    System.out.println(failedGrid);
	  }
	  
	  System.out.println( " --- ISDA Test Grid run complete --- ");
  }
  
  public TestGridResult runTestGrid(ISDATestGrid testGrid, ISDACurve discountCurve, String testGridFileName) throws Exception {
    
    if (lowResTest.equals(testGridFileName)) {
      System.out.println("This is a low resolution test file, using low resolution error limits");
    }
    
    final double absoluteErrorLimit = lowResTest.equals(testGridFileName) ? lowResAbsoluteErrorLimit : dirtyAbsoluteErrorLimit;
    final double percentageErrorLimit = lowResTest.equals(testGridFileName) ? lowResPercentageErrorLimit : cleanPercentageErrorLimit;
    
    int i = 0, failures = 0;
    TestResult result;
    double maxAbsoluteError = 0.0;
    double maxRelativeError = 0.0;
    double maxCleanPercentageError = 0.0;
    
    final ZonedDateTime start = ZonedDateTime.now();
    
    for (ISDATestGridRow testCase : testGrid.getData()) {
      
      // Handle bad rows in the test data
      if (!testCase.isTestValid())
        continue;
      
      result = runTestCase(testCase, discountCurve);
      
      if (result.dirtyAbsoluteError >= absoluteErrorLimit || result.cleanPercentageError >= percentageErrorLimit) {
        
        ++failures;
        
        // Only print out worst cases, to avoid excessive output if the test results are massively out
        if (result.dirtyAbsoluteError > maxAbsoluteError || result.dirtyRelativeError > maxRelativeError || result.cleanPercentageError > maxCleanPercentageError) {
          System.out.println("Grid marked to fail: " + testGridFileName + " row " + (i+2) + ": "
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
    final double seconds = elapsedTime.getSeconds() + (elapsedTime.getNanoOfSecond() / 1000000) / 1000.0;
    
    System.out.println( "Executed " + i + " test cases in " + seconds + "s with " + failures + " failure(s)"
      + ", largest dirty absolute error was " + maxAbsoluteError
      + ", largest dirty relative error was " + maxRelativeError
      + ", largest clean percentage error was " + maxCleanPercentageError);
    
    return new TestGridResult(i, failures, seconds, maxAbsoluteError, maxRelativeError, maxCleanPercentageError);
  }
  
  public TestResult runTestCase(ISDATestGridRow testCase, ISDACurve discountCurve) {
       
    final DayCount dayCount = new ActualThreeSixty();  
    final BusinessDayConvention businessDays = new FollowingBusinessDayConvention();
    final Calendar calendar = new MondayToFridayCalendar("TestCalendar");
    final Convention convention = new Convention(3, dayCount, businessDays, calendar, "");
    final DateAdjuster adjuster = businessDays.getDateAdjuster(calendar);
    
    final ZonedDateTime pricingDate = testCase.getTradeDate().atStartOfDayInZone(TimeZone.UTC); 
    final ZonedDateTime maturity = testCase.getMaturityDate().atStartOfDayInZone(TimeZone.UTC);
    
    // Step-in date is always T+1 calendar
    final ZonedDateTime stepinDate = pricingDate.plusDays(1);
    
    // If settlement date is not supplied, use T+3 business days
    final ZonedDateTime settlementDate = testCase.getCashSettle() != null
      ? testCase.getCashSettle().atStartOfDayInZone(TimeZone.UTC)
      : pricingDate.plusDays(1).with(adjuster).plusDays(1).with(adjuster).plusDays(1).with(adjuster);
    
    // If start date is not supplied, construct one that is before the pricing date
    final Period yearsToMaturity = Period.yearsBetween(pricingDate, maturity);
    final ZonedDateTime startDate = testCase.getStartDate() != null
      ? testCase.getStartDate().atStartOfDayInZone(TimeZone.UTC)
      : maturity.minusYears(yearsToMaturity.getYears() + 1).with(adjuster);
    
    // Spread and recovery are always given
    final double spread = testCase.getCoupon() / 10000.0;
    final double recoveryRate = testCase.getRecoveryRate();
    
    // Assume 1 billion notional, quarterly premiums and ACT360 day count
    final double notional = 1000000000;
    final Frequency couponFrequency = SimpleFrequency.QUARTERLY;
    final StubType stubType = StubType.SHORT_START;
    
    // Now build the CDS object
    final ISDACDSPremiumDefinition premiumDefinition = ISDACDSPremiumDefinition.from(startDate, maturity, couponFrequency, convention, stubType, /* protect start */ true, /*notional*/ 1.0, spread, Currency.USD);
    final ISDACDSDefinition cdsDefinition = new ISDACDSDefinition(startDate, maturity, premiumDefinition, /*notional*/1.0, spread, recoveryRate, /* accrualOnDefault */ true, /* payOnDefault */ true, /* protectStart */ true, couponFrequency, convention, stubType);
    final ISDACDSDerivative cds = cdsDefinition.toDerivative(pricingDate, stepinDate, settlementDate, "IR_CURVE");  
    
    // Par spread is always supplied
    final double marketSpread = testCase.getQuotedSpread() / 10000.0;
    
    // Now go price
    final double dirtyPrice = calculator.calculateUpfrontCharge(cds, discountCurve, marketSpread, false, pricingDate, stepinDate, settlementDate);
    final double cleanPrice = calculator.calculateUpfrontCharge(cds, discountCurve, marketSpread, true, pricingDate, stepinDate, settlementDate);
      
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
    double times[] = new double[nPoints];
    double rates[] = new double[nPoints];
    
    LocalDate date;
    Double rate;
    int i = 0;
    
    for (ISDAStagedCurve.Point dataPoint : curveData.getPoints()) {
      date = LocalDate.parse(dataPoint.getDate(), formatter);
      rate = (new PeriodicInterestRate(Double.valueOf(dataPoint.getRate()),1)).toContinuous().getRate();
      times[i] = dayCount.getDayCountFraction(baseDate, date);
      rates[i++] = rate;
    }
    
    return new ISDACurve(curveName, times, rates, offset);
  }

}
