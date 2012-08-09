package com.opengamma.analytics.financial.credit.cds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.time.calendar.Period;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.BeforeClass;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityPaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.cds.CDSPremiumDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.PeriodicInterestRate;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.FollowingBusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.ActualThreeSixty;
import com.opengamma.financial.convention.daycount.ActualThreeSixtyFive;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.util.money.Currency;

public class CDSTestSetup {
  
  protected static DayCount s_act365 = new ActualThreeSixtyFive();

  protected static CDSDerivative _isdaTestCDS;
  protected static YieldCurveBundle _isdaTestCurveBundle;
  protected static YieldCurveBundle _simpleTestCurveBundle;
  protected static CDSDerivative _simpleTestCDS;
  protected static YieldCurve _isdaTestSpreadCurve;
  protected static YieldCurve _isdaTestCdsCcyYieldCurve;
  protected static ZonedDateTime _simpleTestPricingDate;
  protected static ZonedDateTime _isdaTestPricingDate;

  @BeforeClass
  public static void setupBeforeClass() {
    setupSimpleTestData();
  }

  public static void setupSimpleTestData() {
    
    _simpleTestPricingDate = ZonedDateTime.of(2010, 12, 31, 0, 0, 0, 0, TimeZone.UTC);
  
    double notional = 1.0;
    double recoveryRate = 0.6;
    double spread = 0.0025;
    Currency currency = Currency.GBP;
    ZonedDateTime protectionStartDate = ZonedDateTime.of(2010, 12, 20, 0, 0, 0, 0, TimeZone.UTC);
    ZonedDateTime maturity = ZonedDateTime.of(2020, 12, 20, 0, 0, 0, 0, TimeZone.UTC);
    Frequency premiumFrequency = SimpleFrequency.QUARTERLY;
  
    ZonedDateTime bondMaturity = ZonedDateTime.of(2016, 6, 20, 0, 0, 0, 0, TimeZone.UTC);
    PeriodFrequency bondPremiumFrequency = PeriodFrequency.ANNUAL;
  
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
    final double[] spreadPoints = {
      0.008094573225337830000000000000,
      0.008094573225337830000000000000,
      0.008472028609360500000000000000,
      0.008833186263998250000000000000,
      0.009178825884456880000000000000,
      0.009509688657093270000000000000,
      0.009826479094981490000000000000,
      0.010129866801184300000000000000,
      0.010420488160288400000000000000,
      0.010698947959110100000000000000,
      0.010965820937831700000000000000,
      0.010965820937831700000000000000
    };
  
    final YieldCurve cdsCcyYieldCurve = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(timePoints, cdsCcyPoints, new LinearInterpolator1D()));
    final YieldCurve bondCcyYieldCurve = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(timePoints, bondCcyPoints, new LinearInterpolator1D()));
    final YieldCurve spreadCurve = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(timePoints, spreadPoints, new LinearInterpolator1D()));
  
    _simpleTestCurveBundle = new YieldCurveBundle();
    _simpleTestCurveBundle.setCurve("CDS_CCY", cdsCcyYieldCurve);
    _simpleTestCurveBundle.setCurve("BOND_CCY", bondCcyYieldCurve);
    _simpleTestCurveBundle.setCurve("SPREAD", spreadCurve);
  
    Calendar calendar = new MondayToFridayCalendar("TestCalendar");
    DayCount dayCount = new ActualThreeSixtyFive();
    BusinessDayConvention convention = new FollowingBusinessDayConvention();
  
    final AnnuityCouponFixedDefinition premiumDefinition = AnnuityCouponFixedDefinition.from(currency, protectionStartDate, maturity, premiumFrequency, calendar, dayCount, convention, /*EOM*/ false, notional, spread, /*isPayer*/false);
    final AnnuityCouponFixed premiums = premiumDefinition.toDerivative(_simpleTestPricingDate, "CDS_CCY");
  
    List<ZonedDateTime> possibleDefaultDates = scheduleDatesInRange(bondMaturity, bondPremiumFrequency.getPeriod(), _simpleTestPricingDate, maturity, calendar, convention);
    if (maturity.isAfter(bondMaturity)) {
      possibleDefaultDates.add(convention.adjustDate(calendar, maturity));
    }
  
    PaymentFixedDefinition[] defaultPayments = new PaymentFixedDefinition[possibleDefaultDates.size()];
  
    for (int i = 0; i < defaultPayments.length; ++i) {
      defaultPayments[i] = new PaymentFixedDefinition(currency, possibleDefaultDates.get(i), notional * (1.0 - recoveryRate));
    }
  
    final AnnuityPaymentFixed payouts = (new AnnuityPaymentFixedDefinition(defaultPayments)).toDerivative(_simpleTestPricingDate, "CDS_CCY");
  
    
    _simpleTestCDS = new CDSDerivative(
      "CDS_CCY", "SPREAD", "BOND_CCY",
      premiums, payouts,
      TimeCalculator.getTimeBetween(_simpleTestPricingDate, protectionStartDate), TimeCalculator.getTimeBetween(_simpleTestPricingDate, maturity),
      notional, spread, recoveryRate, /*accrual on default*/ true, /*pay on default*/ true, /*protect start*/ false);
  }

  public static void setupIsdaTestData(double spread) {
      _isdaTestPricingDate = ZonedDateTime.of(2008, 2, 1, 0, 0, 0, 0, TimeZone.UTC);
  
      Currency currency = Currency.USD;
      double notional = 1.0e7;
      double recoveryRate = 0.4;
      spread = spread/10000.0;
      ZonedDateTime cdsStartDate = ZonedDateTime.of(2008, 2, 8, 0, 0, 0, 0, TimeZone.UTC);
      ZonedDateTime maturity = ZonedDateTime.of(2008, 2, 12, 0, 0, 0, 0, TimeZone.UTC);
      Frequency premiumFrequency = SimpleFrequency.QUARTERLY;
      boolean protectStart = true;
  
      double[] discountCurveTimePoints = {
        0.0,
        TimeCalculator.getTimeBetween(_isdaTestPricingDate, ZonedDateTime.of(2008, 2, 3, 0, 0, 0, 0, TimeZone.UTC)),
        TimeCalculator.getTimeBetween(_isdaTestPricingDate, ZonedDateTime.of(2008, 3, 3, 0, 0, 0, 0, TimeZone.UTC)),
        TimeCalculator.getTimeBetween(_isdaTestPricingDate, ZonedDateTime.of(2008, 4, 3, 0, 0, 0, 0, TimeZone.UTC)),
        TimeCalculator.getTimeBetween(_isdaTestPricingDate, ZonedDateTime.of(2008, 7, 3, 0, 0, 0, 0, TimeZone.UTC)),
        TimeCalculator.getTimeBetween(_isdaTestPricingDate, ZonedDateTime.of(2008, 10, 3, 0, 0, 0, 0, TimeZone.UTC)),
        TimeCalculator.getTimeBetween(_isdaTestPricingDate, ZonedDateTime.of(2009, 1, 5, 0, 0, 0, 0, TimeZone.UTC)),
        TimeCalculator.getTimeBetween(_isdaTestPricingDate, ZonedDateTime.of(2009, 7, 3, 0, 0, 0, 0, TimeZone.UTC)),
        TimeCalculator.getTimeBetween(_isdaTestPricingDate, ZonedDateTime.of(2010, 1, 4, 0, 0, 0, 0, TimeZone.UTC)),
        TimeCalculator.getTimeBetween(_isdaTestPricingDate, ZonedDateTime.of(2010, 7, 5, 0, 0, 0, 0, TimeZone.UTC)),
        TimeCalculator.getTimeBetween(_isdaTestPricingDate, ZonedDateTime.of(2011, 1, 3, 0, 0, 0, 0, TimeZone.UTC)),
        TimeCalculator.getTimeBetween(_isdaTestPricingDate, ZonedDateTime.of(2011, 7, 4, 0, 0, 0, 0, TimeZone.UTC)),
        TimeCalculator.getTimeBetween(_isdaTestPricingDate, ZonedDateTime.of(2012, 1, 3, 0, 0, 0, 0, TimeZone.UTC)),
        TimeCalculator.getTimeBetween(_isdaTestPricingDate, ZonedDateTime.of(2012, 7, 3, 0, 0, 0, 0, TimeZone.UTC)),
        TimeCalculator.getTimeBetween(_isdaTestPricingDate, ZonedDateTime.of(2013, 1, 3, 0, 0, 0, 0, TimeZone.UTC)),
        TimeCalculator.getTimeBetween(_isdaTestPricingDate, ZonedDateTime.of(2013, 7, 3, 0, 0, 0, 0, TimeZone.UTC)),
        TimeCalculator.getTimeBetween(_isdaTestPricingDate, ZonedDateTime.of(2014, 1, 3, 0, 0, 0, 0, TimeZone.UTC)),
        TimeCalculator.getTimeBetween(_isdaTestPricingDate, ZonedDateTime.of(2014, 7, 3, 0, 0, 0, 0, TimeZone.UTC)),
        TimeCalculator.getTimeBetween(_isdaTestPricingDate, ZonedDateTime.of(2015, 1, 5, 0, 0, 0, 0, TimeZone.UTC)),
        TimeCalculator.getTimeBetween(_isdaTestPricingDate, ZonedDateTime.of(2015, 7, 3, 0, 0, 0, 0, TimeZone.UTC)),
        TimeCalculator.getTimeBetween(_isdaTestPricingDate, ZonedDateTime.of(2016, 1, 4, 0, 0, 0, 0, TimeZone.UTC)),
        TimeCalculator.getTimeBetween(_isdaTestPricingDate, ZonedDateTime.of(2016, 7, 4, 0, 0, 0, 0, TimeZone.UTC)),
        TimeCalculator.getTimeBetween(_isdaTestPricingDate, ZonedDateTime.of(2017, 1, 3, 0, 0, 0, 0, TimeZone.UTC))
      };
  
      double[] discountCurveRates = {
        0.000000001013888972778431700000, /* copied first value for zero point */
        0.000000001013888972778431700000,
        0.000000001013888972778431700000,
        0.000000001013888972778431700000,
        0.000000001013888972778431700000,
        0.000000001013888972778431700000,
        0.000000000997358196030972980000,
        0.000000000995057147790134880000,
        0.000000000993861384526237670000,
        0.000000000983571668555782710000,
        0.000000000976699218749570080000,
        0.000000000989587300992411660000,
        0.000000000999308693520927250000,
        0.000000000999070826068759740000,
        0.000000000998878776248821030000,
        0.000000000998947369268421430000,
        0.000000000999005352108518340000,
        0.000000000999007765400961030000,
        0.000000000999009754006644690000,
        0.000000000998901406035201940000,
        0.000000000998803566131306970000,
        0.000000000998365168314307990000,
        0.000000000997973248135311370000
      };
      
      double ccRate = (new PeriodicInterestRate(0.81582707425206369, 1)).toContinuous().getRate();
      
      double[] flatSpreadCurveTimePoints = {
        0.0,
        s_act365.getDayCountFraction(_isdaTestPricingDate, ZonedDateTime.of(2008, 2, 12, 0, 0, 0, 0, TimeZone.UTC)), // 0.030136986301369864
      };
      double[] flatSpreadCurveRates = {
        ccRate, ccRate
      };
  
      _isdaTestCdsCcyYieldCurve = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(discountCurveTimePoints, discountCurveRates, new LinearInterpolator1D()));
      _isdaTestSpreadCurve = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(flatSpreadCurveTimePoints, flatSpreadCurveRates, new LinearInterpolator1D()));
  
      _isdaTestCurveBundle = new YieldCurveBundle();
      _isdaTestCurveBundle.setCurve("CDS_CCY", _isdaTestCdsCcyYieldCurve);
      _isdaTestCurveBundle.setCurve("SPREAD", _isdaTestSpreadCurve);
  
      Calendar calendar = new MondayToFridayCalendar("TestCalendar");
      DayCount dayCount = new ActualThreeSixty();
      BusinessDayConvention convention = new FollowingBusinessDayConvention();
  
      final CDSPremiumDefinition premiumDefinition = CDSPremiumDefinition.fromISDA(currency, cdsStartDate, maturity, premiumFrequency, calendar, dayCount, convention, notional, spread, protectStart);
      final AnnuityCouponFixed premiums = premiumDefinition.toDerivative(_isdaTestPricingDate, "CDS_CCY");

  //    List<ZonedDateTime> possibleDefaultDates = scheduleDatesInRange(bondMaturity, bondPremiumFrequency.getPeriod(), pricingDate, maturity, calendar, convention);
  //    if (maturity.isAfter(bondMaturity)) {
  //      possibleDefaultDates.add(convention.adjustDate(calendar, maturity));
  //    }
  //
  //    PaymentFixedDefinition[] defaultPayments = new PaymentFixedDefinition[possibleDefaultDates.size()];
  //
  //    for (int i = 0; i < defaultPayments.length; ++i) {
  //      defaultPayments[i] = new PaymentFixedDefinition(currency, possibleDefaultDates.get(i), notional * (1.0 - recoveryRate));
  //    }
  //
  //    final AnnuityPaymentFixed payouts = (new AnnuityPaymentFixedDefinition(defaultPayments)).toDerivative(pricingDate, "CDS_CCY");
  
      final AnnuityPaymentFixed payouts = null;
      
      _isdaTestCDS = new CDSDerivative(
        "CDS_CCY", "SPREAD", /*bond CCY curve*/ null,
        premiums, payouts,
        TimeCalculator.getTimeBetween(_isdaTestPricingDate, cdsStartDate), TimeCalculator.getTimeBetween(_isdaTestPricingDate, maturity.plusDays(1)),
        notional, spread, recoveryRate, /*accrual on default*/ true, /*pay on default*/ true, /*protect start*/ true);
    }

  public CDSTestSetup() {
    super();
  }

  protected static List<ZonedDateTime> scheduleDatesInRange(ZonedDateTime maturity, Period schedulePeriod, ZonedDateTime earliest, ZonedDateTime latest,
    Calendar calendar, BusinessDayConvention convention) {
  
    List<ZonedDateTime> datesInRange = new ArrayList<ZonedDateTime>();
    ZonedDateTime scheduleDate = maturity;
    int periods = 0;
  
    while (scheduleDate.isAfter(latest)) {
      scheduleDate = convention.adjustDate(calendar, maturity.minus(schedulePeriod.multipliedBy(++periods)));
    }
  
    while (!scheduleDate.isBefore(earliest)) {
      datesInRange.add(scheduleDate);
      scheduleDate = convention.adjustDate(calendar, maturity.minus(schedulePeriod.multipliedBy(++periods)));
    }
  
    Collections.reverse(datesInRange);
    return datesInRange;
  }

}
