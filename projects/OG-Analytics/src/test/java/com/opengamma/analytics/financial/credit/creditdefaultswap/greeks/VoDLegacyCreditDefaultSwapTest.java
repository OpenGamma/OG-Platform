/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.greeks;

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
import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.greeks.vanilla.VoDCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
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
 * Test of the implementation of the Value-on-Default calculations for a legacy CDS
 */
public class VoDLegacyCreditDefaultSwapTest {

  //----------------------------------------------------------------------------------

  // TODO : Add all the tests

  // ----------------------------------------------------------------------------------

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

  // Need to sort out understanding of this better

  protected static DayCount s_act365 = new ActualThreeSixtyFive();

  final int baseDateYear = 2008;
  final int baseDateMonth = 9;
  final int baseDateDay = 22;

  final ZonedDateTime baseDate = zdt(baseDateYear, baseDateMonth, baseDateDay, 0, 0, 0, 0, ZoneOffset.UTC);

  double[] times = {
      s_act365.getDayCountFraction(baseDate, zdt(2008, 10, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2008, 11, 24, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2008, 12, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2009, 3, 23, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2009, 6, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2009, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2010, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2010, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2011, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2011, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2012, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2012, 9, 24, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2013, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2013, 9, 23, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2014, 3, 24, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2014, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2015, 3, 23, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2015, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2016, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2016, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2017, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2017, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2018, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2018, 9, 24, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2019, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2019, 9, 23, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2020, 3, 23, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2020, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2021, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2021, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2022, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2022, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2023, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2023, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2024, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2024, 9, 23, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2025, 3, 24, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2025, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2026, 3, 23, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2026, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2027, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2027, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2028, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2028, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2029, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2029, 9, 24, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2030, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2030, 9, 23, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2031, 3, 24, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2031, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2032, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2032, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2033, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2033, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2034, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2034, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2035, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2035, 9, 24, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2036, 3, 24, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2036, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2037, 3, 23, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2037, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2038, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(baseDate, zdt(2038, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
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
  final double offset = s_act365.getDayCountFraction(valuationDate, baseDate);

  // Build the yield curve object
  ISDACurve yieldCurve = new ISDACurve("IR_CURVE", times, rates, offset);

  // ----------------------------------------------------------------------------------

  // Hazard rate term structure (assume this has been calibrated previously)

  static ZonedDateTime[] hazardRateDates = {zdt(2013, 06, 20, 0, 0, 0, 0, ZoneOffset.UTC), zdt(2015, 06, 20, 0, 0, 0, 0, ZoneOffset.UTC), zdt(2018, 06, 20, 0, 0, 0, 0, ZoneOffset.UTC) };

  static double[] hazardRateTimes = {
      0.0,
      s_act365.getDayCountFraction(valuationDate, zdt(2013, 06, 20, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(valuationDate, zdt(2015, 06, 20, 0, 0, 0, 0, ZoneOffset.UTC)),
      s_act365.getDayCountFraction(valuationDate, zdt(2018, 06, 20, 0, 0, 0, 0, ZoneOffset.UTC))
  };

  static double[] hazardRates = {
      (new PeriodicInterestRate(0.09709857471184660000, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.09709857471184660000, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.09705141266558010000, 1)).toContinuous().getRate(),
      (new PeriodicInterestRate(0.09701141671498870000, 1)).toContinuous().getRate()
  };

  // Build the hazard rate curve object (No offset - survival probability = 1 on valuationDate)
  private static final HazardRateCurve hazardRateCurve = new HazardRateCurve(hazardRateDates, hazardRateTimes, hazardRates, 0.0);

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

  // -----------------------------------------------------------------------------------------------

  @Test
  public void testVoDCalculation() {

    // -------------------------------------------------------------------------------------

    if (outputResults) {
      System.out.println("Running VoD calculation test ...");
    }

    // -------------------------------------------------------------------------------------

    // Define the market data to calibrate to

    // The number of CDS instruments used to calibrate against
    final int numberOfCalibrationCDS = 8;

    // The flat (unbumped) spread
    final double flatSpread = 550.0;

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

    marketSpreads[0] = flatSpread;
    marketSpreads[1] = flatSpread;
    marketSpreads[2] = flatSpread;
    marketSpreads[3] = flatSpread;
    marketSpreads[4] = flatSpread;
    marketSpreads[5] = flatSpread;
    marketSpreads[6] = flatSpread;
    marketSpreads[7] = flatSpread;

    // -------------------------------------------------------------------------------------

    // Create a RecoveryRate01 calculator object
    final VoDCreditDefaultSwap vod = new VoDCreditDefaultSwap();

    // Compute the VoD
    //final double valueOnDefault = vod.getValueOnDefaultCreditDefaultSwap(valuationDate, cds, yieldCurve, tenors, marketSpreads, priceType);

    // -------------------------------------------------------------------------------------

    if (outputResults) {
      //System.out.println("CDS VoD = " + valueOnDefault);
    }

    // -------------------------------------------------------------------------------------
  }

  //-------------------------------------------------------------------------
  private static ZonedDateTime zdt(int y, int m, int d, int hr, int min, int sec, int nanos, ZoneId zone) {
    return LocalDateTime.of(y, m, d, hr, min, sec, nanos).atZone(zone);
  }

}
