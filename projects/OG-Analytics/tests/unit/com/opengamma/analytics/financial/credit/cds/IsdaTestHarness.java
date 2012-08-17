/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.cds;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatters;
import javax.xml.bind.JAXBException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
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


public class IsdaTestHarness {
  
  private static DateTimeFormatter formatter = DateTimeFormatters.pattern("dd/MM/yyyy");
  private static DayCount dayCount = new ActualThreeSixtyFive();
  private static CDSApproxISDAMethod calculator = new CDSApproxISDAMethod();
  
  private int failures;
  private double maxError;
  
  private List<String> badTestFiles = new ArrayList<String>();
  
  @Test
  public void runTestHarnessTest() throws JAXBException, IOException, InterruptedException 
  {
    
    
    
    IsdaTest[] result = IsdaTestManager.getAllTests();
    
    for (int i = 0; i < result.length; i++) {
      
      failures = 0;
      maxError = 0.0;
      
      IsdaTest test = result[i];
      System.out.println( "Running grid: " + IsdaTestManager.getTestFileName(i) + ", test cases: " + test.getGrid().getGridTest().size());
      
      String excelFile = IsdaTestManager.getNameOfExcelTestGridFile(i);
      TestGrid excelGrid = TestGridManager.getTestGrid(excelFile);
      List<TestGridRow> excelRows = excelGrid.getData();
      
      IsdaTest.IRCurve irCurve = test.getIRCurve();
      IsdaTest.Grid testGrid = test.getGrid();  
      List<IsdaTest.Grid.GridTest> testList = testGrid.getGridTest();
      
      ISDACurve discountCurve = buildDiscountCurve(irCurve);
      
      for (int j = 0; j < testList.size(); j++) {
        IsdaTest.Grid.GridTest testCase = testList.get(j);
        TestGridRow excelRow = excelRows.get(j);
        runTestCase(excelRow, discountCurve, testCase);
      }
      
      if (failures > 0) {
        System.out.println( "Errors: " + failures + ", max error = " + maxError);
        badTestFiles.add(IsdaTestManager.getTestFileName(i));
      }
    }
    
    System.out.println("Bad test files: " + badTestFiles);
  }
  
  public ISDACurve buildDiscountCurve(IsdaTest.IRCurve irCurve) {
    
    // Expect all curve objects to use annual compounding
    Assert.assertEquals(Double.valueOf(irCurve.getBasis()), 1.0);
    
    final LocalDate baseDate = LocalDate.parse(irCurve.getBaseDate(), formatter);
    final int nPoints = irCurve.getPoints().getPoint().size();
    
    double times[] = new double[nPoints];
    double rates[] = new double[nPoints];
    
    LocalDate date;
    Double rate;
    int i = 0;
    
    for (Points.Point dataPoint : irCurve.getPoints().getPoint()) {
      date = LocalDate.parse(dataPoint.getDate(), formatter);
      rate = (new PeriodicInterestRate(Double.valueOf(dataPoint.getRate()),1)).toContinuous().getRate();
      times[i] = dayCount.getDayCountFraction(baseDate, date);
      rates[i++] = rate;
    }
    
    return new ISDACurve("IR_CURVE", times, rates, 0.0);
  }
  
  public ISDACurve buildHazardCurve(IsdaTest.Grid.GridTest.HazardCurve hazardRateCurve) {
    
    // Expect all curve objects to use annual compounding
    Assert.assertEquals(Double.valueOf(hazardRateCurve.getBasis()), 1.0);
    
    final LocalDate baseDate = LocalDate.parse(hazardRateCurve.getBaseDate(), formatter);
    final int nPoints = hazardRateCurve.getPoints().getPoint().size();
    
    double times[] = new double[nPoints];
    double rates[] = new double[nPoints];
    
    LocalDate date;
    Double rate;
    int i = 0;
    
    for (Points.Point dataPoint : hazardRateCurve.getPoints().getPoint()) {
      date = LocalDate.parse(dataPoint.getDate(), formatter);
      rate = (new PeriodicInterestRate(Double.valueOf(dataPoint.getRate()),1)).toContinuous().getRate();
      times[i] = dayCount.getDayCountFraction(baseDate, date);
      rates[i++] = rate;
    }
    
    return new ISDACurve("HAZARD_RATE_CURVE", times, rates, 0.0);
  }
  
  public void runTestCase(TestGridRow testCase, ISDACurve discountCurve, IsdaTest.Grid.GridTest interpretedTestCase) {
    
    final Calendar calendar = new MondayToFridayCalendar("TestCalendar");
    final BusinessDayConvention convention = new FollowingBusinessDayConvention();
    final DateAdjuster adjuster = convention.getDateAdjuster(calendar);
    
    final ZonedDateTime maturity = testCase.getMaturityDate().atStartOfDayInZone(TimeZone.UTC);
    final ZonedDateTime startDate = testCase.getStartDate() != null
      ? testCase.getStartDate().atStartOfDayInZone(TimeZone.UTC)
      : maturity.minusYears(10).with(adjuster);
    
    final double notional = 1000000000;
    final double spread = testCase.getCoupon() / 10000.0;
    final double recoveryRate = testCase.getRecoveryRate();
    
    final Frequency couponFrequency = SimpleFrequency.QUARTERLY;
    final DayCount dayCount = new ActualThreeSixty();  
    
    final CDSPremiumDefinition premiumDefinition = CDSPremiumDefinition.fromISDA(Currency.USD, startDate, maturity, couponFrequency, calendar, dayCount, convention, notional, spread, /* protect start */ true);
    final CDSDefinition cdsDefinition = new CDSDefinition(premiumDefinition, null, startDate, maturity, notional, spread, recoveryRate, /* accrualOnDefault */ true, /* payOnDefault */ true, /* protectStart */ true, dayCount);
    
    final double marketSpread = testCase.getQuotedSpread() / 10000.0;
    final ISDACurve hazardCurve = buildHazardCurve(interpretedTestCase.getHazardCurve());  
    
    final ZonedDateTime pricingDate = testCase.getTradeDate().atStartOfDayInZone(TimeZone.UTC);                                          // LocalDate.parse(interpretedTestCase.getToday(), formatter)
    final ZonedDateTime stepinDate = pricingDate.plusDays(1);                                      // Step in date = T+1 calendar        // LocalDate.parse(interpretedTestCase.getStepinDate(), formatter)
    
    final ZonedDateTime settlementDate = testCase.getCashSettle() != null
      ? testCase.getCashSettle().atStartOfDayInZone(TimeZone.UTC)
      : pricingDate.plusDays(1).with(adjuster).plusDays(1).with(adjuster).plusDays(1).with(adjuster);   // LocalDate.parse(interpretedTestCase.getValueDate(), formatter)
    
    if (!pricingDate.equals(LocalDate.parse(interpretedTestCase.getToday(), formatter).atStartOfDayInZone(TimeZone.UTC))) {
      throw new OpenGammaRuntimeException("price dates do not match: " + pricingDate + ", " + LocalDate.parse(interpretedTestCase.getToday(), formatter).atStartOfDayInZone(TimeZone.UTC));
    }
    
    if (!stepinDate.equals(LocalDate.parse(interpretedTestCase.getStepinDate(), formatter).atStartOfDayInZone(TimeZone.UTC))) {
      System.out.println("stepin dates do not match: " + stepinDate + ", " + LocalDate.parse(interpretedTestCase.getStepinDate(), formatter).atStartOfDayInZone(TimeZone.UTC));
    }
    
    if (!settlementDate.equals(LocalDate.parse(interpretedTestCase.getValueDate(), formatter).atStartOfDayInZone(TimeZone.UTC))) {
      System.out.println("settlement dates do not match!");
    }
    
    final CDSDerivative cds = cdsDefinition.toDerivative(pricingDate, "IR_CURVE");
    
    final double result = calculator.calculateUpfrontCharge(cds, discountCurve, hazardCurve, pricingDate, stepinDate, settlementDate, false);
    final double expectedResult = testCase.getUpfront();
    final int magnitude = (int) Math.log10(Math.abs(expectedResult));
    final double actualError = Math.abs(result - expectedResult);
    final double relativeError = actualError * Math.pow(10, -magnitude);
    
    if (relativeError >= 1E-10) {
      
      //final double calculateddResult = Double.valueOf(interpretedTestCase.getDirtyResult()) * notional;
      //System.out.println("Result: " + result + ", Exepcted: " + expectedResult + ", calculated result: " + calculateddResult + ", Error: " + actualError + ", relativeError: " + relativeError);
      
      failures++;
      
      if (relativeError > maxError)
        maxError = relativeError;
    }
    
    //Assert.assertTrue( relativeError < 1E-8 );
    
  }

}
