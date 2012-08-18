/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.cds;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatters;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.cds.CDSDefinition;
import com.opengamma.analytics.financial.instrument.cds.CDSPremiumDefinition;
import com.opengamma.analytics.financial.interestrate.PeriodicInterestRate;
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
  
  private static DateTimeFormatter formatter = DateTimeFormatters.pattern("yyyy-MM-dd");
  private static DayCount dayCount = new ActualThreeSixtyFive();
  private static CDSApproxISDAMethod calculator = new CDSApproxISDAMethod();
  
  private double maxError;
  
  @Test
  public void runAllTestGrids() throws Exception {
	  
	  final ISDATestGridManager manager = new ISDATestGridManager();
	  final ISDAStagedDataManager stagedManager = new ISDAStagedDataManager();
	  final String[] testFiles = manager.findAllTestGridFiles();
	  
	  ISDATestGrid testGrid;
	  ISDAStagedCurve stagedCurve;
	  ISDACurve discountCurve;
	  
	  for (String fileName : testFiles) {
		  
		  testGrid = manager.loadTestGrid(fileName);
		  stagedCurve = stagedManager.loadStagedCurveForGrid(fileName);
		  
		  if (stagedCurve == null)
		    continue;
		  
		  System.out.println("Running test grid: " + fileName);
		  
		  discountCurve = buildCurve(stagedCurve, "IR_CURVE");
		  runTestGrid(testGrid, discountCurve, fileName);
	  }
  }
  
  public void runTestGrid(ISDATestGrid testGrid, ISDACurve discountCurve, String testGridFileName) throws Exception {
    
    ISDAStagedDataManager stagedManager = new ISDAStagedDataManager();
    ISDAStagedCurve stagedCurve;
    ISDACurve hazardCurve = null;
    
    int i = 0;
    maxError = 0.0;
    
    for (ISDATestGridRow testCase : testGrid.getData()) {
      
      stagedCurve = stagedManager.loadStagedHazardCurveForGrid(testGridFileName, i);
      hazardCurve = stagedCurve != null ? buildCurve(stagedCurve, "HAZARD_RATE_CURVE") : null;
      
      runTestCase(testCase, discountCurve, hazardCurve, i++);
    }
    
    System.out.println( "Passed " + i + " test cases, largest relative error was " + maxError);
  }
  
  public void runTestCase(ISDATestGridRow testCase, ISDACurve discountCurve, ISDACurve hazardRateCurve, int i) {
       
    final Calendar calendar = new MondayToFridayCalendar("TestCalendar");
    final BusinessDayConvention convention = new FollowingBusinessDayConvention();
    final DateAdjuster adjuster = convention.getDateAdjuster(calendar);
    
    final ZonedDateTime pricingDate = testCase.getTradeDate().atStartOfDayInZone(TimeZone.UTC); 
    final ZonedDateTime maturity = testCase.getMaturityDate().atStartOfDayInZone(TimeZone.UTC);
    
    // Step-in date is always T+1 calendar
    final ZonedDateTime stepinDate = pricingDate.plusDays(1);
    
    // If settlement date is not supplied, use T+3 business days
    final ZonedDateTime settlementDate = testCase.getCashSettle() != null
      ? testCase.getCashSettle().atStartOfDayInZone(TimeZone.UTC)
      : pricingDate.plusDays(1).with(adjuster).plusDays(1).with(adjuster).plusDays(1).with(adjuster);
    
    // If start date is not supplied, take twenty years from the maturity date (to ensure it is before the pricing date)
    final ZonedDateTime startDate = testCase.getStartDate() != null
      ? testCase.getStartDate().atStartOfDayInZone(TimeZone.UTC)
      : maturity.minusYears(20).with(adjuster);
    
    // Spread and recovery are always given
    final double spread = testCase.getCoupon() / 10000.0;
    final double recoveryRate = testCase.getRecoveryRate();
    
    // Assume 1 billion notional, quarterly premiums and ACT360 day count
    final double notional = 1000000000;
    final Frequency couponFrequency = SimpleFrequency.QUARTERLY;
    final DayCount dayCount = new ActualThreeSixty();  
    
    // Now build the CDS object
    final CDSPremiumDefinition premiumDefinition = CDSPremiumDefinition.fromISDA(Currency.USD, startDate, maturity, couponFrequency, calendar, dayCount, convention, notional, spread, /* protect start */ true);
    final CDSDefinition cdsDefinition = new CDSDefinition(premiumDefinition, null, startDate, maturity, notional, spread, recoveryRate, /* accrualOnDefault */ true, /* payOnDefault */ true, /* protectStart */ true, dayCount);
    final CDSDerivative cds = cdsDefinition.toDerivative(pricingDate, "IR_CURVE");  
    
    // Par spread is always supplied
    final double marketSpread = testCase.getQuotedSpread() / 10000.0;
    
    if (hazardRateCurve != null) {
      System.out.println("Using a staged hazard rate curve for case " + i);
    }
    
    // Now go price
    final double result = hazardRateCurve != null
      ? calculator.calculateUpfrontCharge(cds, discountCurve, hazardRateCurve, pricingDate, stepinDate, settlementDate, false)
      : calculator.calculateUpfrontCharge(cds, discountCurve, marketSpread, pricingDate, stepinDate, settlementDate, false);
      
    final double expectedResult = testCase.getUpfront();
    final double actualError = Math.abs(result - expectedResult);
    final double relativeError = Math.abs(actualError / expectedResult);
    
    if (relativeError > maxError) {
      maxError = relativeError;
      // System.out.println("Case: " + i + ", Result: " + result + ", Exepcted: " + expectedResult + ", Error: " + actualError + ", relativeError: " + relativeError);
    }
    
    Assert.assertTrue( relativeError < 1E-10 );
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
  
  public ISDACurve buildCurve(final ISDAStagedTest.IRCurve curveData, final String curveName) {
  
    // Expect all curve objects to use annual compounding
    Assert.assertEquals(Double.valueOf(curveData.getBasis()), 1.0);
    
    final LocalDate baseDate = LocalDate.parse(curveData.getBaseDate(), formatter);
    final int nPoints = curveData.getPoints().getPoint().size();
    
    double times[] = new double[nPoints];
    double rates[] = new double[nPoints];
    
    LocalDate date;
    Double rate;
    int i = 0;
    
    for (ISDAStagedTestPoints.Point dataPoint : curveData.getPoints().getPoint()) {
      date = LocalDate.parse(dataPoint.getDate(), formatter);
      rate = (new PeriodicInterestRate(Double.valueOf(dataPoint.getRate()),1)).toContinuous().getRate();
      times[i] = dayCount.getDayCountFraction(baseDate, date);
      rates[i++] = rate;
    }
    
    return new ISDACurve(curveName, times, rates, 0.0);
  }
  
  public ISDACurve buildCurve(final ISDAStagedTest.Grid.GridTest.HazardCurve curveData, final String curveName) {
    
    // Expect all curve objects to use annual compounding
    Assert.assertEquals(Double.valueOf(curveData.getBasis()), 1.0);
    
    final LocalDate baseDate = LocalDate.parse(curveData.getBaseDate(), formatter);
    final int nPoints = curveData.getPoints().getPoint().size();
    
    double times[] = new double[nPoints];
    double rates[] = new double[nPoints];
    
    LocalDate date;
    Double rate;
    int i = 0;
    
    for (ISDAStagedTestPoints.Point dataPoint : curveData.getPoints().getPoint()) {
      date = LocalDate.parse(dataPoint.getDate(), formatter);
      rate = (new PeriodicInterestRate(Double.valueOf(dataPoint.getRate()),1)).toContinuous().getRate();
      times[i] = dayCount.getDayCountFraction(baseDate, date);
      rates[i++] = rate;
    }
    
    return new ISDACurve(curveName, times, rates, 0.0);
  }

}
