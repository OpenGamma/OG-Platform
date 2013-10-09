/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.calibratehazardratecurve;

import static org.testng.AssertJUnit.assertEquals;

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
import com.opengamma.analytics.financial.credit.calibratehazardratecurve.legacy.CalibrateHazardRateCurveLegacyCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.PresentValueCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
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

  private static final Currency currency = Currency.EUR;

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

  private static final double notional = 100000000.0;
  private static final double recoveryRate = 0.40;
  private static final boolean includeAccruedPremium = false;
  private static final PriceType PRICE_TYPE_CLEAN = PriceType.CLEAN;
  private static final boolean protectionStart = true;

  private static final double parSpread = 100.0;

  private static final double TOLERANCE_HARDCODED = 1.0E-2; // 0.01 currency unit for 100m

  // ----------------------------------------------------------------------------------

  // Dummy yield curve

  protected static DayCount s_act365 = new ActualThreeSixtyFive();

  private static final ZonedDateTime BASE_DATE = zdt(2008, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC);

  private static final int NB_DATES = 64;
  private static final ZonedDateTime[] CURVE_DATES = new ZonedDateTime[NB_DATES];
  static {
    CURVE_DATES[0] = zdt(2008, 10, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[1] = zdt(2008, 11, 24, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[2] = zdt(2008, 12, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[3] = zdt(2009, 3, 23, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[4] = zdt(2009, 6, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[5] = zdt(2009, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[6] = zdt(2010, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[7] = zdt(2010, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[8] = zdt(2011, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[9] = zdt(2011, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[10] = zdt(2012, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[11] = zdt(2012, 9, 24, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[12] = zdt(2013, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[13] = zdt(2013, 9, 23, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[14] = zdt(2014, 3, 24, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[15] = zdt(2014, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[16] = zdt(2015, 3, 23, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[17] = zdt(2015, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[18] = zdt(2016, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[19] = zdt(2016, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[20] = zdt(2017, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[21] = zdt(2017, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[22] = zdt(2018, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[23] = zdt(2018, 9, 24, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[24] = zdt(2019, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[25] = zdt(2019, 9, 23, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[26] = zdt(2020, 3, 23, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[27] = zdt(2020, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[28] = zdt(2021, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[29] = zdt(2021, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[30] = zdt(2022, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[31] = zdt(2022, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[32] = zdt(2023, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[33] = zdt(2023, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[34] = zdt(2024, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[35] = zdt(2024, 9, 23, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[36] = zdt(2025, 3, 24, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[37] = zdt(2025, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[38] = zdt(2026, 3, 23, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[39] = zdt(2026, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[40] = zdt(2027, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[41] = zdt(2027, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[42] = zdt(2028, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[43] = zdt(2028, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[44] = zdt(2029, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[45] = zdt(2029, 9, 24, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[46] = zdt(2030, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[47] = zdt(2030, 9, 23, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[48] = zdt(2031, 3, 24, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[49] = zdt(2031, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[50] = zdt(2032, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[51] = zdt(2032, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[52] = zdt(2033, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[53] = zdt(2033, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[54] = zdt(2034, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[55] = zdt(2034, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[56] = zdt(2035, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[57] = zdt(2035, 9, 24, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[58] = zdt(2036, 3, 24, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[59] = zdt(2036, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[60] = zdt(2037, 3, 23, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[61] = zdt(2037, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[62] = zdt(2038, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC);
    CURVE_DATES[63] = zdt(2038, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC);
  }

  private static final double[] CURVE_TIMES = new double[NB_DATES];
  static {
    for (int loopdate = 0; loopdate < NB_DATES; loopdate++) {
      CURVE_TIMES[loopdate] = s_act365.getDayCountFraction(BASE_DATE, CURVE_DATES[loopdate]);
    }
  }

  //  double[] times = {
  //      s_act365.getDayCountFraction(baseDate, zdt(2008, 10, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2008, 11, 24, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2008, 12, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2009, 3, 23, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2009, 6, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2009, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2010, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2010, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2011, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2011, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2012, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2012, 9, 24, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2013, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2013, 9, 23, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2014, 3, 24, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2014, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2015, 3, 23, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2015, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2016, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2016, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2017, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2017, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2018, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2018, 9, 24, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2019, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2019, 9, 23, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2020, 3, 23, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2020, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2021, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2021, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2022, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2022, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2023, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2023, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2024, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2024, 9, 23, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2025, 3, 24, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2025, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2026, 3, 23, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2026, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2027, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2027, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2028, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2028, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2029, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2029, 9, 24, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2030, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2030, 9, 23, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2031, 3, 24, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2031, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2032, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2032, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2033, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2033, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2034, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2034, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2035, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2035, 9, 24, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2036, 3, 24, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2036, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2037, 3, 23, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2037, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2038, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //      s_act365.getDayCountFraction(baseDate, zdt(2038, 9, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
  //  };

  private static final double[] RATES = {
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
  //  private static final  ISDACurve yieldCurve = new ISDACurve("IR_CURVE", CURVE_TIMES, rates, s_act365.getDayCountFraction(valuationDate, BASE_DATE));

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

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Test to demonstrate calibration of a hazard rate curve to a single tenor

  @Test
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

    LegacyVanillaCreditDefaultSwapDefinition calibrationCDS = cds;

    calibrationCDS = calibrationCDS.withRecoveryRate(calibrationRecoveryRate);

    final CalibrateHazardRateCurveLegacyCreditDefaultSwap hazardRateCurve = new CalibrateHazardRateCurveLegacyCreditDefaultSwap();

    //final double[] calibratedHazardRateTermStructure = hazardRateCurve.getCalibratedHazardRateTermStructure(valuationDate, calibrationCDS, tenors, marketSpreads, yieldCurve, priceType);

    if (outputResults) {
      for (int i = 0; i < numberOfCalibrationCDS; i++) {
        //System.out.println(calibratedHazardRateTermStructure[i]);
      }
    }

    // -----------------------------------------------------------------------------------------------
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Test to demonstrate calibration of a hazard rate curve to a term structure of market data

  //@Test
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
    final LegacyVanillaCreditDefaultSwapDefinition calibrationCDS = cds;

    /*

    // Set the recovery rate of the calibration CDS used for the curve calibration (this appears in the calculation of the contingent leg)
    calibrationCDS = calibrationCDS.withRecoveryRate(calibrationRecoveryRate);

    // -------------------------------------------------------------------------------------

    // Create a calibrate survival curve object
    final CalibrateHazardRateCurveLegacyCreditDefaultSwap hazardRateCurve = new CalibrateHazardRateCurveLegacyCreditDefaultSwap();

    // Calibrate the hazard rate curve to the market observed par CDS spreads (returns calibrated hazard rates as a vector of doubles)
    final double[] calibratedHazardRateCurve = hazardRateCurve.getCalibratedHazardRateTermStructure(valuationDate, calibrationCDS, tenors, marketSpreads, yieldCurve, priceType);

    if (outputResults) {
      for (int i = 0; i < numberOfCalibrationCDS; i++) {
        System.out.println(calibratedHazardRateCurve[i]);
      }
    }

    */

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

  @Test
  public void testCalibrateHazardRateCurveData() {

    final ZonedDateTime valuationDate = DateUtils.getUTCDate(2008, 6, 19);
    final ZonedDateTime startDate = DateUtils.getUTCDate(2008, 6, 25); // TODO: meaningful dates
    final ZonedDateTime effectiveDate = DateUtils.getUTCDate(2008, 6, 27);
    final ZonedDateTime maturityDate = DateUtils.getUTCDate(2014, 6, 27);

    final LegacyVanillaCreditDefaultSwapDefinition cds = new LegacyVanillaCreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntity, currency, debtSeniority,
        restructuringClause, calendar, startDate, effectiveDate, maturityDate, stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate,
        adjustEffectiveDate, adjustMaturityDate, notional, recoveryRate, includeAccruedPremium, protectionStart, parSpread);

    final PresentValueCreditDefaultSwap pvcds = new PresentValueCreditDefaultSwap();

    final ISDADateCurve curveISDADate = new ISDADateCurve("Test", BASE_DATE, CURVE_DATES, RATES, 0.0);

    // Set the tenors at which we have market observed par CDS spread quotes
    final int nbTenors = 10;
    final ZonedDateTime[] tenors = new ZonedDateTime[nbTenors];
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

    //Note: The input are in bps. Should we change it to relative number to be coherent with the rest of the library? The scale is hard-coded (/10000.0 in the code).
    final double[] flat = {0.0, 1.0, 50.0, 100.0, 200.0, 1000.0 };    // Flat (tight, distressed, blown)
    // Note: Flat with 100000.0 fails to calibrate.
    final int nbFlat = flat.length;
    final double[][] zigzag = { {50.0, 60.0 }, {500.0, 550.0 } };
    // Note: Zig-zag with {500.0, 600.0 } fails to calibrate
    final int nbZigzag = zigzag.length;
    final double[][] upward = { {100.0, 20.0 }, {100.0, 40.0 } }; // Start, step
    // Note: Upward with {100.0, 100.0 } or {100.0, 50.0 } fails to calibrate
    final int nbUpward = upward.length;
    final int nbSpecific = 8;

    final int nbSpreads = nbFlat + nbZigzag + nbUpward + nbSpecific;
    final double[][] marketSpreads = new double[nbSpreads][nbTenors];
    for (int loopflat = 0; loopflat < nbFlat; loopflat++) {
      for (int loopten = 0; loopten < nbTenors; loopten++) {
        marketSpreads[loopflat][loopten] = flat[loopflat];
      }
    }
    for (int loopzz = 0; loopzz < nbZigzag; loopzz++) {
      for (int loopten = 0; loopten < nbTenors / 2; loopten++) {
        marketSpreads[nbFlat + loopzz][2 * loopten + 0] = zigzag[loopzz][0];
        marketSpreads[nbFlat + loopzz][2 * loopten + 1] = zigzag[loopzz][1];
      }
    }
    for (int loopup = 0; loopup < nbUpward; loopup++) {
      for (int loopten = 0; loopten < nbTenors; loopten++) {
        marketSpreads[nbFlat + nbZigzag + loopup][loopten] = upward[loopup][0] + loopten * upward[loopup][1];
      }
    }
    final int nbNonSpecific = nbFlat + nbZigzag + nbUpward;
    int loopspec = 0; // Upward, steep short end
    marketSpreads[nbNonSpecific + loopspec][0] = 100.0;
    marketSpreads[nbNonSpecific + loopspec][1] = 200.0;
    marketSpreads[nbNonSpecific + loopspec][2] = 300.0;
    marketSpreads[nbNonSpecific + loopspec][3] = 400.0;
    marketSpreads[nbNonSpecific + loopspec][4] = 500.0;
    marketSpreads[nbNonSpecific + loopspec][5] = 520.0;
    marketSpreads[nbNonSpecific + loopspec][6] = 540.0;
    marketSpreads[nbNonSpecific + loopspec][7] = 560.0;
    marketSpreads[nbNonSpecific + loopspec][8] = 580.0;
    marketSpreads[nbNonSpecific + loopspec][9] = 600.0;
    loopspec++;// Upward, steep long end
    marketSpreads[nbNonSpecific + loopspec][0] = 100.0;
    marketSpreads[nbNonSpecific + loopspec][1] = 120.0;
    marketSpreads[nbNonSpecific + loopspec][2] = 140.0;
    marketSpreads[nbNonSpecific + loopspec][3] = 160.0;
    marketSpreads[nbNonSpecific + loopspec][4] = 180.0;
    marketSpreads[nbNonSpecific + loopspec][5] = 220.0;
    marketSpreads[nbNonSpecific + loopspec][6] = 260.0;
    marketSpreads[nbNonSpecific + loopspec][7] = 300.0;
    marketSpreads[nbNonSpecific + loopspec][8] = 340.0;
    marketSpreads[nbNonSpecific + loopspec][9] = 380.0;
    loopspec++;//Downward, gentle
    marketSpreads[nbNonSpecific + loopspec][0] = 280.0;
    marketSpreads[nbNonSpecific + loopspec][1] = 260.0;
    marketSpreads[nbNonSpecific + loopspec][2] = 240.0;
    marketSpreads[nbNonSpecific + loopspec][3] = 220.0;
    marketSpreads[nbNonSpecific + loopspec][4] = 200.0;
    marketSpreads[nbNonSpecific + loopspec][5] = 180.0;
    marketSpreads[nbNonSpecific + loopspec][6] = 160.0;
    marketSpreads[nbNonSpecific + loopspec][7] = 140.0;
    marketSpreads[nbNonSpecific + loopspec][8] = 120.0;
    marketSpreads[nbNonSpecific + loopspec][9] = 100.0;
    loopspec++;//Downward, steep
    marketSpreads[nbNonSpecific + loopspec][0] = 1000.0;
    marketSpreads[nbNonSpecific + loopspec][1] = 900.0;
    marketSpreads[nbNonSpecific + loopspec][2] = 800.0;
    marketSpreads[nbNonSpecific + loopspec][3] = 700.0;
    marketSpreads[nbNonSpecific + loopspec][4] = 600.0;
    marketSpreads[nbNonSpecific + loopspec][5] = 500.0;
    marketSpreads[nbNonSpecific + loopspec][6] = 450.0;
    marketSpreads[nbNonSpecific + loopspec][7] = 400.0;
    marketSpreads[nbNonSpecific + loopspec][8] = 400.0;
    marketSpreads[nbNonSpecific + loopspec][9] = 380.0;
    loopspec++; //Downward, steep short end
    marketSpreads[nbNonSpecific + loopspec][0] = 600.0;
    marketSpreads[nbNonSpecific + loopspec][1] = 500.0;
    marketSpreads[nbNonSpecific + loopspec][2] = 400.0;
    marketSpreads[nbNonSpecific + loopspec][3] = 300.0;
    marketSpreads[nbNonSpecific + loopspec][4] = 200.0;
    marketSpreads[nbNonSpecific + loopspec][5] = 180.0;
    marketSpreads[nbNonSpecific + loopspec][6] = 160.0;
    marketSpreads[nbNonSpecific + loopspec][7] = 140.0;
    marketSpreads[nbNonSpecific + loopspec][8] = 120.0;
    marketSpreads[nbNonSpecific + loopspec][9] = 100.0;
    loopspec++; // Downward, steep long end
    marketSpreads[nbNonSpecific + loopspec][0] = 680.0;
    marketSpreads[nbNonSpecific + loopspec][1] = 660.0;
    marketSpreads[nbNonSpecific + loopspec][2] = 640.0;
    marketSpreads[nbNonSpecific + loopspec][3] = 620.0;
    marketSpreads[nbNonSpecific + loopspec][4] = 600.0;
    marketSpreads[nbNonSpecific + loopspec][5] = 580.0;
    marketSpreads[nbNonSpecific + loopspec][6] = 480.0;
    marketSpreads[nbNonSpecific + loopspec][7] = 380.0;
    marketSpreads[nbNonSpecific + loopspec][8] = 280.0;
    marketSpreads[nbNonSpecific + loopspec][9] = 180.0;
    loopspec++; // Inverted Cavale
    marketSpreads[nbNonSpecific + loopspec][0] = 1774.0;
    marketSpreads[nbNonSpecific + loopspec][1] = 1805.0;
    marketSpreads[nbNonSpecific + loopspec][2] = 1856.0;
    marketSpreads[nbNonSpecific + loopspec][3] = 1994.0;
    marketSpreads[nbNonSpecific + loopspec][4] = 2045.0;
    marketSpreads[nbNonSpecific + loopspec][5] = 2045.0;
    marketSpreads[nbNonSpecific + loopspec][6] = 2045.0;
    marketSpreads[nbNonSpecific + loopspec][7] = 2045.0;
    marketSpreads[nbNonSpecific + loopspec][8] = 2045.0;
    marketSpreads[nbNonSpecific + loopspec][9] = 2045.0;
    loopspec++; // BCPN
    marketSpreads[nbNonSpecific + loopspec][0] = 780.0;
    marketSpreads[nbNonSpecific + loopspec][1] = 812.0;
    marketSpreads[nbNonSpecific + loopspec][2] = 803.0;
    marketSpreads[nbNonSpecific + loopspec][3] = 826.0;
    marketSpreads[nbNonSpecific + loopspec][4] = 874.0;
    marketSpreads[nbNonSpecific + loopspec][5] = 896.0;
    marketSpreads[nbNonSpecific + loopspec][6] = 868.0;
    marketSpreads[nbNonSpecific + loopspec][7] = 838.0;
    marketSpreads[nbNonSpecific + loopspec][8] = 800.0;
    marketSpreads[nbNonSpecific + loopspec][9] = 780.0;

    final double[] pv = new double[nbSpreads];
    for (int loopspread = 0; loopspread < nbSpreads; loopspread++) {
      pv[loopspread] = pvcds.calibrateAndGetPresentValue(valuationDate, cds, tenors, marketSpreads[loopspread], curveISDADate, PRICE_TYPE_CLEAN);
    }
    final double[] pvExpected = {-5672458.043975232, -5612833.8411497325, -2764265.396014931, 72.73528969381005, 5126779.225961099, 3.2042108339047514E7, -2210334.5968965204, 1.9690546519727346E7,
        5196701.133692645, 1.00057853795825E7, 1.9044683964563813E7, 6220964.461377103, 4085113.125930696, 1.686216455050518E7, 4001404.223613698, 2.0290515510249116E7, 4.728163157144226E7,
        2.9981696893176883E7 }; // From previous run

    for (int loopspread = 0; loopspread < nbSpreads; loopspread++) {
      assertEquals("PresentValueCreditDefaultSwap.calibrateAndGetPresentValue - spread " + loopspread, pv[loopspread], pvExpected[loopspread], TOLERANCE_HARDCODED);
    }
  }

  //-------------------------------------------------------------------------
  private static ZonedDateTime zdt(final int y, final int m, final int d, final int hr, final int min, final int sec, final int nanos, final ZoneId zone) {
    return LocalDateTime.of(y, m, d, hr, min, sec, nanos).atZone(zone);
  }

}
