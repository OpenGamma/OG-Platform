/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.cds;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

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
import com.opengamma.util.test.TestGroup;

/**
 * Tests of the RiskCare implementation of the ISDA CDS model
 */
@Test(groups = TestGroup.UNIT)
public class ISDAModelTest {

  // -----------------------------------------------------------------------------------------------------------

  // Flag to control if any test results are output to the console
  private static final boolean outputResults = false;

  // -----------------------------------------------------------------------------------------------------------

  protected ISDACDSDefinition loadCDS_ISDAExampleCDSCalcualtor2() {

    // Contract start date
    final ZonedDateTime startDate = zdt(2008, 3, 20, 0, 0, 0, 0, ZoneOffset.UTC);

    // Contract maturity date
    final ZonedDateTime maturity = zdt(2013, 3, 20, 0, 0, 0, 0, ZoneOffset.UTC);

    final int settlementDays = 0;

    final double notional = 10000000;
    final double spread = 0.01; /* 100bp */
    final double recoveryRate = 0.40;

    final Frequency couponFrequency = SimpleFrequency.QUARTERLY;

    final Calendar calendar = new MondayToFridayCalendar("TestCalendar");

    final DayCount dayCount = new ActualThreeSixty();
    final BusinessDayConvention businessDays = new FollowingBusinessDayConvention();
    final Convention convention = new Convention(settlementDays, dayCount, businessDays, calendar, "");

    final StubType stubType = StubType.SHORT_START;

    // Include the accrued coupon (for a default that occurs between coupon dates)
    final boolean accrualOnDefault = true;

    // Pay contingent leg on default or at maturity?
    final boolean payOnDefault = true;

    final boolean protectStart = true;

    final ISDACDSPremiumDefinition premiumDefinition = ISDACDSPremiumDefinition.from(startDate, maturity, couponFrequency, convention, stubType, protectStart, notional, spread,
        Currency.EUR, calendar);

    return new ISDACDSDefinition(startDate, maturity, premiumDefinition, notional, spread, recoveryRate, accrualOnDefault, payOnDefault, protectStart, couponFrequency, convention, stubType);
  }

  // -----------------------------------------------------------------------------------------------------------

  @Test(enabled = false)
  @SuppressWarnings("deprecation")
  public void testISDAModel() {

    final DayCount s_act365 = new ActualThreeSixtyFive();

    // -----------------------------------------------------------------------------------------------------------

    // The baseline date for calculating hazard rates
    final ZonedDateTime baseDate = zdt(2008, 9, 18, 0, 0, 0, 0, ZoneOffset.UTC);

    // Remember this ...
    final ZonedDateTime baseDate2 = zdt(2008, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC);

    // -----------------------------------------------------------------------------------------------------------

    // Interest rate term structure

    // The the time nodes ...
    final double[] timeNodesRates = {
        s_act365.getDayCountFraction(baseDate2, zdt(2008, 10, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2008, 11, 24, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2008, 12, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2009, 3, 23, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2009, 6, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2009, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2010, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2010, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2011, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2011, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2012, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2012, 9, 24, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2013, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2013, 9, 23, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2014, 3, 24, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2014, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2015, 3, 23, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2015, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2016, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2016, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2017, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2017, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2018, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2018, 9, 24, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2019, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2019, 9, 23, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2020, 3, 23, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2020, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2021, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2021, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2022, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2022, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2023, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2023, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2024, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2024, 9, 23, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2025, 3, 24, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2025, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2026, 3, 23, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2026, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2027, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2027, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2028, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2028, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2029, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2029, 9, 24, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2030, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2030, 9, 23, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2031, 3, 24, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2031, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2032, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2032, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2033, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2033, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2034, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2034, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2035, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2035, 9, 24, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2036, 3, 24, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2036, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2037, 3, 23, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2037, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2038, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate2, zdt(2038, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
    };

    // The rates at each timenode ...
    final double[] interestRates = {
        (new PeriodicInterestRate(0.00452115893602745000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.00965814197655757000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.01256719569422680000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.01808999617970230000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.01966710100627830000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.02112741666666660000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.01809534760435110000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.01655763824251000000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.01880609764411780000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.02033274208031280000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.02201082479582110000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.02329627269146610000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.02457991990962620000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.02564349380607000000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.02664198869678810000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.02747534265210970000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.02822421752113560000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.02887011718207980000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.02947938315126190000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03001849170997110000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03051723047721790000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03096814372457490000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03140378315953840000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03180665717369410000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03220470040815960000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03257895748982500000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03300576868204530000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03339934269742980000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03371439235915700000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03401013049588440000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03427957764613110000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03453400145380310000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03476707646146720000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03498827591548650000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03504602653686710000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03510104623115760000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03515188034751750000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03519973661653090000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03524486925430900000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03528773208373260000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03532784361012300000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03536647655059340000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03540272683370320000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03543754047166620000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03539936837313170000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03536201961264760000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03532774866571060000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03529393446018300000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03526215518920560000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03523175393297300000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03520264296319420000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03517444167763210000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03514783263597550000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03512186451200650000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03510945878934860000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03509733233582990000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03508585365890470000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03507449693456950000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03506379166273740000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03505346751846350000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03504350450444570000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03503383205190350000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03502458863645770000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.03501550511625420000, 1)).toContinuous().getRate()
    };

    // -----------------------------------------------------------------------------------------------------------

    // The hazard rate term structure (assumed to have been calibrated previously)

    final double[] timeNodesHazardRate = {
        0.0,
        s_act365.getDayCountFraction(baseDate, zdt(2013, 06, 20, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate, zdt(2015, 06, 20, 0, 0, 0, 0, ZoneOffset.UTC)),
        s_act365.getDayCountFraction(baseDate, zdt(2018, 06, 20, 0, 0, 0, 0, ZoneOffset.UTC))
    };

    final double[] hazardRates = {
        (new PeriodicInterestRate(0.09709857471184660000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.09709857471184660000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.09705141266558010000, 1)).toContinuous().getRate(),
        (new PeriodicInterestRate(0.09701141671498870000, 1)).toContinuous().getRate()
    };

    // ----------------------------------------------------------------------------------------------------------

    // The valuation date (today)
    final ZonedDateTime valuationDate = zdt(2008, 9, 18, 0, 0, 0, 0, ZoneOffset.UTC);

    // ----------------------------------------------------------------------------------------------------------

    // The shift to apply to the rates timenodes (if spot days is non-zero)
    final double offsetInterestRateCurve = s_act365.getDayCountFraction(valuationDate, baseDate2);

    final double offsetHazardRateCurve = 0.0;

    // Build the yield and hazard rate curves
    final ISDACurve discountCurve = new ISDACurve("IR_CURVE", timeNodesRates, interestRates, offsetInterestRateCurve);
    final ISDACurve hazardRateCurve = new ISDACurve("HAZARD_RATE_CURVE", timeNodesHazardRate, hazardRates, offsetHazardRateCurve);

    // ----------------------------------------------------------------------------------------------------------

    // start at the initial valuation date
    ZonedDateTime rollingDate = valuationDate.minusDays(1);

    // Specify the end date
    final ZonedDateTime endDate = valuationDate.plusDays(1647);

    while (rollingDate.isBefore(endDate)) {

      // Roll the current valuation date
      rollingDate = rollingDate.plusDays(1);

      // Build the CDS and pricing objects
      final ISDACDSDerivative cds = loadCDS_ISDAExampleCDSCalcualtor2().toDerivative(rollingDate, "IR_CURVE", "HAZARD_RATE_CURVE");
      final ISDAApproxCDSPricingMethod method = new ISDAApproxCDSPricingMethod();

      // Calculate the clean and dirty prices
      final double cleanPrice = method.calculateUpfrontCharge(cds, discountCurve, hazardRateCurve, true);
      final double dirtyPrice = method.calculateUpfrontCharge(cds, discountCurve, hazardRateCurve, false);

      if (outputResults) {
        //System.out.println(rollingDate + "\t" + "Dirty Price = " + "\t" + dirtyPrice);
        System.out.println(rollingDate + "\t" + "clean Price = " + "\t" + cleanPrice);
      }
    }
  }

  //-------------------------------------------------------------------------
  private static ZonedDateTime zdt(final int y, final int m, final int d, final int hr, final int min, final int sec, final int nanos, final ZoneOffset offset) {
    return LocalDateTime.of(y, m, d, hr, min, sec, nanos).atZone(offset);
  }

}
