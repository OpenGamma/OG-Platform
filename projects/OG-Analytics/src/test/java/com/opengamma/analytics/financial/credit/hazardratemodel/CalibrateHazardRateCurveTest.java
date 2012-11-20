/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.hazardratemodel;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.obligormodel.CreditRating;
import com.opengamma.analytics.financial.credit.obligormodel.CreditRatingFitch;
import com.opengamma.analytics.financial.credit.obligormodel.CreditRatingMoodys;
import com.opengamma.analytics.financial.credit.obligormodel.CreditRatingStandardAndPoors;
import com.opengamma.analytics.financial.credit.obligormodel.Region;
import com.opengamma.analytics.financial.credit.obligormodel.Sector;
import com.opengamma.analytics.financial.credit.obligormodel.definition.Obligor;
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
 * Class to test the implementation of the calibration of the hazard rate term structure
 */
public class CalibrateHazardRateCurveTest {

  // ---------------------------------------------------------------------------------------

  // Flag to control if any test results are output to the console
  private static final boolean outputResults = false;

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

  private static final ZonedDateTime startDate = DateUtils.getUTCDate(2008, 3, 20);
  private static final ZonedDateTime effectiveDate = DateUtils.getUTCDate(2008, 3, 20);
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
  private static final double recoveryRate = 0.40;
  private static final boolean includeAccruedPremium = false;
  private static final PriceType priceType = PriceType.CLEAN;
  private static final boolean protectionStart = true;

  private static final double parSpread = 100.0;

  // ----------------------------------------------------------------------------------

  // Dummy yield curve

  protected static DayCount s_act365 = new ActualThreeSixtyFive();

  final ZonedDateTime baseDate = ZonedDateTime.of(2008, 9, 22, 0, 0, 0, 0, TimeZone.UTC);

  double[] times = {
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
  };

  double[] rates = {
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

  // Use an ISDACurve object (from RiskCare implementation) for the yield curve
  ISDACurve yieldCurve = new ISDACurve("IR_CURVE", times, rates, s_act365.getDayCountFraction(valuationDate, baseDate));

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
  private static final LegacyCreditDefaultSwapDefinition cds = new LegacyCreditDefaultSwapDefinition(
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

  // Test to demonstrate calibration of a hazard rate curve to a single tenor

  //@Test
  public void testCalibrateHazardRateCurveSingleTenor() {

    // -----------------------------------------------------------------------------------------------

    if (outputResults) {
      System.out.println("Running CDS Calibration test  ...");
    }

    // -----------------------------------------------------------------------------------------------

    final int numberOfCalibrationCDS = 1;

    final ZonedDateTime[] tenors = new ZonedDateTime[numberOfCalibrationCDS];
    final double[] marketSpreads = new double[numberOfCalibrationCDS];

    final double flatSpread = 550;
    final double calibrationRecoveryRate = 0.40;

    tenors[0] = DateUtils.getUTCDate(2013, 6, 20);
    marketSpreads[0] = flatSpread;

    LegacyCreditDefaultSwapDefinition calibrationCDS = cds;
    calibrationCDS = calibrationCDS.withRecoveryRate(calibrationRecoveryRate);

    final CalibrateHazardRateCurve hazardRateCurve = new CalibrateHazardRateCurve();

    final double[] calibratedHazardRateTermStructure = hazardRateCurve.getCalibratedHazardRateTermStructure(valuationDate, calibrationCDS, tenors, marketSpreads, yieldCurve, priceType);

    if (outputResults) {
      for (int i = 0; i < numberOfCalibrationCDS; i++) {
        System.out.println(calibratedHazardRateTermStructure[i]);
      }
    }

    // -----------------------------------------------------------------------------------------------
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Test to demonstrate calibration of a hazard rate curve to a term structure of market data

  @Test
  public void testCalibrateHazardRateCurveFlatTermStructure() {

    // -------------------------------------------------------------------------------------

    if (outputResults) {
      System.out.println("Running Hazard Rate Curve Calibration test ...");
    }

    // -------------------------------------------------------------------------------------

    // Define the market data to calibrate to

    // The number of CDS instruments used to calibrate against
    final int numberOfCalibrationCDS = 8;

    // The CDS tenors to calibrate to
    final ZonedDateTime[] tenors = new ZonedDateTime[numberOfCalibrationCDS];

    tenors[0] = DateUtils.getUTCDate(2008, 12, 20);
    tenors[1] = DateUtils.getUTCDate(2009, 6, 20);
    tenors[2] = DateUtils.getUTCDate(2010, 6, 20);
    tenors[3] = DateUtils.getUTCDate(2011, 6, 20);
    tenors[4] = DateUtils.getUTCDate(2012, 6, 20);
    tenors[5] = DateUtils.getUTCDate(2013, 6, 20);
    tenors[6] = DateUtils.getUTCDate(2015, 6, 20);
    tenors[7] = DateUtils.getUTCDate(2018, 6, 20);

    // The market observed par CDS spreads at these tenors
    final double[] marketSpreads = new double[numberOfCalibrationCDS];

    final double flatSpread = 550.0;

    marketSpreads[0] = flatSpread;
    marketSpreads[1] = flatSpread;
    marketSpreads[2] = flatSpread;
    marketSpreads[3] = flatSpread;
    marketSpreads[4] = flatSpread;
    marketSpreads[5] = flatSpread;
    marketSpreads[6] = flatSpread;
    marketSpreads[7] = flatSpread;

    /*
    marketSpreads[0] = 500.0;
    marketSpreads[1] = 600.0;
    marketSpreads[2] = 500.0;
    marketSpreads[3] = 600.0;
    marketSpreads[4] = 500.0;
    marketSpreads[5] = 400.0;
    marketSpreads[6] = 500.0;
    marketSpreads[7] = 600.0;

    marketSpreads[0] = 3865.0;
    marketSpreads[1] = 3072.0;
    marketSpreads[2] = 2559.0;
    marketSpreads[3] = 2243.0;
    marketSpreads[4] = 2141.0;
    marketSpreads[5] = 2045.0;
    marketSpreads[6] = 1944.0;
    marketSpreads[7] = 1856.0;

    marketSpreads[0] = 780.0;
    marketSpreads[1] = 812.0;
    marketSpreads[2] = 803.0;
    marketSpreads[3] = 826.0;
    marketSpreads[4] = 874.0;
    marketSpreads[5] = 896.0;
    marketSpreads[6] = 868.0;
    marketSpreads[7] = 838.0;
     */

    // The recovery rate assumption used in the PV calculations when calibrating
    final double calibrationRecoveryRate = 0.40;

    // -------------------------------------------------------------------------------------

    // Create a calibration CDS (will be a modified version of the baseline CDS)
    LegacyCreditDefaultSwapDefinition calibrationCDS = cds;

    // Set the recovery rate of the calibration CDS used for the curve calibration (this appears in the calculation of the contingent leg)
    calibrationCDS = calibrationCDS.withRecoveryRate(calibrationRecoveryRate);

    // -------------------------------------------------------------------------------------

    // Create a calibrate survival curve object
    final CalibrateHazardRateCurve hazardRateCurve = new CalibrateHazardRateCurve();

    // Calibrate the hazard rate curve to the market observed par CDS spreads (returns calibrated hazard rates as a vector of doubles)
    final double[] calibratedHazardRateCurve = hazardRateCurve.getCalibratedHazardRateTermStructure(valuationDate, calibrationCDS, tenors, marketSpreads, yieldCurve, priceType);

    if (outputResults) {
      for (int i = 0; i < numberOfCalibrationCDS; i++) {
        System.out.println(calibratedHazardRateCurve[i]);
      }
    }

    // -------------------------------------------------------------------------------------

  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Test to demonstrate calibration of a hazard rate curve to a (flat) term structure of market data and recompute calibration CDS PV's

  //@Test
  public void testCalibrateHazardRateCurveAndRepriceCalibrationCDS() {

    // -----------------------------------------------------------------------------------------------

    if (outputResults) {
      System.out.println("Running CDS calibration and re-pricing test ...");
    }

    // -----------------------------------------------------------------------------------------------
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Don't remove this code yet

  public void testCalibrateHazardRateCurveData() {

    /*
    // Set the tenors at which we have market observed par CDS spread quotes
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
     */

    /*
    // Flat (tight)

    marketSpreads[0] = 100.0;
    marketSpreads[1] = 100.0;
    marketSpreads[2] = 100.0;
    marketSpreads[3] = 100.0;
    marketSpreads[4] = 100.0;
    marketSpreads[5] = 100.0;
    marketSpreads[6] = 100.0;
    marketSpreads[7] = 100.0;
    marketSpreads[8] = 100.0;
    marketSpreads[9] = 100.0;
    //curveRecoveryRate = 0.40;
     */

    // Flat (distressed)
    /*
        marketSpreads[0] = 1000.0;
        marketSpreads[1] = 1000.0;
        marketSpreads[2] = 1000.0;
        marketSpreads[3] = 1000.0;
        marketSpreads[4] = 1000.0;
        marketSpreads[5] = 1000.0;
        marketSpreads[6] = 1000.0;
        marketSpreads[7] = 1000.0;
        marketSpreads[8] = 1000.0;
        marketSpreads[9] = 1000.0;
        curveRecoveryRate = 0.40;
     */
    // Flat (blown)
    /*
        marketSpreads[0] = 100000.0;
        marketSpreads[1] = 100000.0;
        marketSpreads[2] = 100000.0;
        marketSpreads[3] = 100000.0;
        marketSpreads[4] = 100000.0;
        marketSpreads[5] = 100000.0;
        marketSpreads[6] = 100000.0;
        marketSpreads[7] = 100000.0;
        marketSpreads[8] = 100000.0;
        marketSpreads[9] = 100000.0;
        curveRecoveryRate = 0.40;
     */
    // Zig-zag (Tight)
    /*
    marketSpreads[0] = 50.0;
    marketSpreads[1] = 60.0;
    marketSpreads[2] = 50.0;
    marketSpreads[3] = 60.0;
    marketSpreads[4] = 50.0;
    marketSpreads[5] = 40.0;
    marketSpreads[6] = 50.0;
    marketSpreads[7] = 60.0;
    marketSpreads[8] = 50.0;
    marketSpreads[9] = 60.0;
    curveRecoveryRate = 0.40;
     */

    // Zig-zag (Distressed)
    /*
        marketSpreads[0] = 500.0;
        marketSpreads[1] = 600.0;
        marketSpreads[2] = 500.0;
        marketSpreads[3] = 600.0;
        marketSpreads[4] = 500.0;
        marketSpreads[5] = 400.0;
        marketSpreads[6] = 500.0;
        marketSpreads[7] = 600.0;
        marketSpreads[8] = 500.0;
        marketSpreads[9] = 600.0;
        curveRecoveryRate = 0.40;
     */
    // Upward, gentle
    /*
        marketSpreads[0] = 100.0;
        marketSpreads[1] = 120.0;
        marketSpreads[2] = 140.0;
        marketSpreads[3] = 160.0;
        marketSpreads[4] = 180.0;
        marketSpreads[5] = 200.0;
        marketSpreads[6] = 220.0;
        marketSpreads[7] = 240.0;
        marketSpreads[8] = 260.0;
        marketSpreads[9] = 280.0;
        curveRecoveryRate = 0.40;
     */
    // Upward, steep

    /* marketSpreads[0] = 100.0;
     marketSpreads[1] = 200.0;
     marketSpreads[2] = 300.0;
     marketSpreads[3] = 400.0;
     marketSpreads[4] = 500.0;
     marketSpreads[5] = 600.0;
     marketSpreads[6] = 700.0;
     marketSpreads[7] = 800.0;
     marketSpreads[8] = 900.0;
     marketSpreads[9] = 1000.0;
     curveRecoveryRate = 0.40;
     */

    // Upward, steep short end
    /*
        marketSpreads[0] = 100.0;
        marketSpreads[1] = 200.0;
        marketSpreads[2] = 300.0;
        marketSpreads[3] = 400.0;
        marketSpreads[4] = 500.0;
        marketSpreads[5] = 520.0;
        marketSpreads[6] = 540.0;
        marketSpreads[7] = 560.0;
        marketSpreads[8] = 580.0;
        marketSpreads[9] = 600.0;
        curveRecoveryRate = 0.40;
     */
    // Upward, steep long end

    /*  marketSpreads[0] = 100.0;
      marketSpreads[1] = 120.0;
      marketSpreads[2] = 140.0;
      marketSpreads[3] = 160.0;
      marketSpreads[4] = 180.0;
      marketSpreads[5] = 280.0;
      marketSpreads[6] = 380.0;
      marketSpreads[7] = 480.0;
      marketSpreads[8] = 580.0;
      marketSpreads[9] = 680.0;
      curveRecoveryRate = 0.40;
     */
    // Downward, gentle

    /*
    marketSpreads[0] = 280.0;
    marketSpreads[1] = 260.0;
    marketSpreads[2] = 240.0;
    marketSpreads[3] = 220.0;
    marketSpreads[4] = 200.0;
    marketSpreads[5] = 180.0;
    marketSpreads[6] = 160.0;
    marketSpreads[7] = 140.0;
    marketSpreads[8] = 120.0;
    marketSpreads[9] = 100.0;
    curveRecoveryRate = 0.40;
     */

    // Downward, steep
    /*
    marketSpreads[0] = 1000.0;
    marketSpreads[1] = 900.0;
    marketSpreads[2] = 800.0;
    marketSpreads[3] = 700.0;
    marketSpreads[4] = 600.0;
    marketSpreads[5] = 500.0;
    marketSpreads[6] = 400.0;
    marketSpreads[7] = 300.0;
    marketSpreads[8] = 200.0;
    marketSpreads[9] = 100.0;
    curveRecoveryRate = 0.40;
     */

    // Downward, steep short end
    /*
    marketSpreads[0] = 600.0;
    marketSpreads[1] = 500.0;
    marketSpreads[2] = 400.0;
    marketSpreads[3] = 300.0;
    marketSpreads[4] = 200.0;
    marketSpreads[5] = 180.0;
    marketSpreads[6] = 160.0;
    marketSpreads[7] = 140.0;
    marketSpreads[8] = 120.0;
    marketSpreads[9] = 100.0;
    curveRecoveryRate = 0.40;
     */

    // Downward, steep long end
    /*
    marketSpreads[0] = 680.0;
    marketSpreads[1] = 660.0;
    marketSpreads[2] = 640.0;
    marketSpreads[3] = 620.0;
    marketSpreads[4] = 600.0;
    marketSpreads[5] = 580.0;
    marketSpreads[6] = 480.0;
    marketSpreads[7] = 380.0;
    marketSpreads[8] = 280.0;
    marketSpreads[9] = 180.0;
    curveRecoveryRate = 0.40;
     */

    // Inverted Cavale
    /*
    marketSpreads[0] = 1774.0;
    marketSpreads[1] = 1805.0;
    marketSpreads[2] = 1856.0;
    marketSpreads[3] = 1994.0;
    marketSpreads[4] = 2045.0;
    marketSpreads[5] = 2141.0;
    marketSpreads[6] = 2243.0;
    marketSpreads[7] = 2559.0;
    marketSpreads[8] = 3072.0;
    marketSpreads[9] = 3865.0;
    curveRecoveryRate = 0.20;
     */

    // Cavale
    /*
    marketSpreads[0] = 3865.0;
    marketSpreads[1] = 3072.0;
    marketSpreads[2] = 2559.0;
    marketSpreads[3] = 2243.0;
    marketSpreads[4] = 2141.0;
    marketSpreads[5] = 2045.0;
    marketSpreads[6] = 1944.0;
    marketSpreads[7] = 1856.0;
    marketSpreads[8] = 1805.0;
    marketSpreads[9] = 1774.0;
    curveRecoveryRate = 0.20;
     */
    // BCPN
    /*
    marketSpreads[0] = 780.0;
    marketSpreads[1] = 812.0;
    marketSpreads[2] = 803.0;
    marketSpreads[3] = 826.0;
    marketSpreads[4] = 874.0;
    marketSpreads[5] = 896.0;
    marketSpreads[6] = 868.0;
    marketSpreads[7] = 838.0;
    marketSpreads[8] = 800.0;
    marketSpreads[9] = 780.0;
    curveRecoveryRate = 0.40;
     */
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------
}
