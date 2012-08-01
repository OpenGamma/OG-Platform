/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.cds;

import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.cds.CDSSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

public class CDSSimpleCalculatorTest {

  @Test
  public void testCalculation() {
    
    final ZonedDateTime pricingDate = ZonedDateTime.of(2010, 12, 31, 0, 0, 0, 0, TimeZone.UTC);
    
    final CDSSecurity cds = new CDSSecurity(
      1.0, /* notional */
      0.6, /* recovery rate */
      0.0025, /* premium rate */
      Currency.GBP, /* currency */
      ZonedDateTime.of(2020, 12, 20, 0, 0, 0, 0, TimeZone.UTC), /* maturity */
      ZonedDateTime.now(), /* first premium date */
      SimpleFrequency.QUARTERLY, /* premium frequency */
      ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "US912828KY53-A") /* underlying */
    );
    
    final GovernmentBondSecurity bond = new GovernmentBondSecurity(
      "US TREASURY N/B", "Sovereign", "US", "US GOVERNMENT", 
      Currency.USD, SimpleYieldConvention.US_STREET, 
      new Expiry(ZonedDateTime.of(2016, 06, 20, 0, 0, 0, 0, TimeZone.UTC)), /* last trade date, i.e. maturity, last coupon */
      "FIXED", 2.625, 
      SimpleFrequency.ANNUAL, /* term */
      DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"), 
      ZonedDateTime.of(LocalDateTime.of(2009, 5, 30, 18, 0), TimeZone.UTC), 
      ZonedDateTime.of(LocalDateTime.of(2011, 5, 28, 11, 0), TimeZone.UTC), 
      ZonedDateTime.of(LocalDateTime.of(2009, 12, 31, 11, 0), TimeZone.UTC), 
      99.651404, 3.8075E10, 100.0, 100.0, 100.0, 100.0);
    bond.addExternalId(ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "US912828KY53-A"));
    bond.setName("T 2 5/8 06/30/14 A");
    
    bond.setLastTradeDate(new Expiry(ZonedDateTime.of(2016, 06, 20, 0, 0, 0, 0, TimeZone.UTC)));
    bond.setCouponFrequency(PeriodFrequency.ANNUAL);

    final double[] timePoints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
    final double[] cdsCcyPoints = {
        0.00673222222222214000,
        0.00673222222222214000,
        0.01429905379554900000,
        0.02086135487690320000,
        0.02630494452289900000,
        0.03072389009716760000,
        0.03406975577292020000,
        0.03670379007549540000,
        0.03859767722179330000,
        0.04029139055351690000,
        0.04171488783608950000,
        0.04171488783608950000
    };
    final double[] bondCcyPoints = {
        0.00673222222222214000,
        0.00673222222222214000,
        0.01429905379554900000,
        0.02086135487690320000,
        0.02630494452289900000,
        0.03072389009716760000,
        0.03406975577292020000,
        0.03670379007549540000,
        0.03859767722179330000,
        0.04029139055351690000,
        0.04171488783608950000,
        0.04171488783608950000
    };
    final double[] riskyPoints = {
        0.01482679544756000000,
        0.01482679544756000000,
        0.02277108240490950000,
        0.02969454114090150000,
        0.03548377040735590000,
        0.04023357875426090000,
        0.04389623486790170000,
        0.04683365687667970000,
        0.04901816538208170000,
        0.05099033851262700000,
        0.05268070877392120000,
        0.05268070877392120000
    };

    final YieldCurve cdsCcyYieldCurve = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(timePoints, cdsCcyPoints, new LinearInterpolator1D()));
    final YieldCurve bondCcyYieldCurve = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(timePoints, bondCcyPoints, new LinearInterpolator1D()));
    final YieldCurve riskyCurve = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(timePoints, riskyPoints, new LinearInterpolator1D()));

    final double result = CDSSimpleCalculator.calculate(cds, bond, cdsCcyYieldCurve, bondCcyYieldCurve, riskyCurve, pricingDate);
    
    System.out.println( result );
    
  }

}
