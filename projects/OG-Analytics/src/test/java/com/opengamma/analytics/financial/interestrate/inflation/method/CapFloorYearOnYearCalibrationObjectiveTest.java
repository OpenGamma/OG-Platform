/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.method;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCapFloorInflationYearOnYearMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsBlack;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.method.CapFloorIborHullWhiteMethod;
import com.opengamma.analytics.financial.model.interestrate.definition.InflationYearOnYearCapFloorParameters;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.option.parameters.BlackSmileCapInflationYearOnYearParameters;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.inflation.BlackSmileCapInflationYearOnYearProvider;
import com.opengamma.analytics.financial.provider.description.inflation.BlackSmileCapInflationYearOnYearProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.method.SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationEngine;
import com.opengamma.analytics.financial.provider.method.SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationObjective;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.interpolation.Interpolator2D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 *  Tests related to the calibration engine for Hull-White one factor calibration to cap/floor.
 */
public class CapFloorYearOnYearCalibrationObjectiveTest {

  //Cap/floor description
  private static final Period TENOR_IBOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 9, 9);
  private static final double NOTIONAL = 10000; //100m
  private static final double[] STRIKES = {-.01, .00, .01, .02, .03, .04 };
  private static final boolean IS_CAP = true;
  private static final InflationIssuerProviderDiscount MARKET = MulticurveProviderDiscountDataSets.createMarket1();
  private static final IndexPrice[] PRICE_INDEXES = MARKET.getPriceIndexes().toArray(new IndexPrice[MARKET.getPriceIndexes().size()]);
  private static final IndexPrice PRICE_INDEX_EUR = PRICE_INDEXES[0];
  private static final Calendar CALENDAR_EUR = MulticurveProviderDiscountDataSets.getEURCalendar();
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final Period COUPON_TENOR = Period.ofYears(10);
  private static final Period COUPON_PAYMENT_TENOR = Period.ofYears(1);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, COUPON_TENOR, BUSINESS_DAY, CALENDAR_EUR);
  private static final ZonedDateTime PAYMENT_DATE_MINUS_1Y = ScheduleCalculator.getAdjustedDate(START_DATE, Period.ofYears(9), BUSINESS_DAY, CALENDAR_EUR);
  private static final int MONTH_LAG = 3;
  private static final ZonedDateTime PRICING_DATE = DateUtils.getUTCDate(2011, 8, 3);
  private static final double WEIGHT_START = 1.0 - (PAYMENT_DATE.getDayOfMonth() - 1.) / PAYMENT_DATE.toLocalDate().lengthOfMonth();
  private static final double WEIGHT_END = 1.0 - (PAYMENT_DATE.getDayOfMonth() - 1.) / PAYMENT_DATE.toLocalDate().lengthOfMonth();
  private static final ZonedDateTime LAST_KNOWN_FIXING_DATE = DateUtils.getUTCDate(2008, 7, 01);

  private static final InterpolatedDoublesSurface BLACK_SURF = TestsDataSetsBlack.createBlackSurfaceExpiryStrike();
  private static final BlackSmileCapInflationYearOnYearParameters BLACK_PARAM = new BlackSmileCapInflationYearOnYearParameters(BLACK_SURF, PRICE_INDEX_EUR);
  private static final BlackSmileCapInflationYearOnYearProviderDiscount BLACK_INFLATION = new BlackSmileCapInflationYearOnYearProviderDiscount(MARKET.getInflationProvider(), BLACK_PARAM);
  
  // To derivative
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 9, 7);
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final SABRInterestRateParameters SABR_PARAMETER = TestsDataSetsSABR.createSABR1();
  private static final SABRInterestRateDataBundle SABR_BUNDLE = new SABRInterestRateDataBundle(SABR_PARAMETER, CURVES);
  private static final String[] CURVES_NAME = CURVES.getAllNames().toArray(new String[CURVES.size()]);
 
  private static final CapFloorInflationYearOnYearMonthlyBlackNormalSmileMethod METHOD = CapFloorInflationYearOnYearMonthlyBlackNormalSmileMethod.getInstance();
  private static final CapFloorIborHullWhiteMethod METHOD_CAP_HW = new CapFloorIborHullWhiteMethod();
   double[][] marketPrices= new  double[6][10];
  
  
  
  private static final double[] expiryTimes = {1.00, 2.00, 3.00, 4.00, 5.00, 6.00, 7.00, 8.00, 9.00, 10.00 };
  private static  double[] expiryTimes1 = new double[10];
  private static final double[] strikes = {-.01, .00, .01, .02, .03, .04 };
  private static final double[][] volatilities = { {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 },
      {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 },
      {.01, .01, .01, .01, .01, .01 } };
  AnnuityCapFloorInflationYearOnYearMonthlyDefinition[] CAP_DEFINITIONS= new AnnuityCapFloorInflationYearOnYearMonthlyDefinition[6];
  Annuity<? extends Payment>[] CAPS= new  Annuity<?>[6];
 
  @Test
  /**
   * Tests the correctness of INFLATION YEAR ON YEAR CAP/FLOOR calibration to market prices.
   */
  public void calibration() {
    
    for (int loopexp = 0; loopexp < STRIKES.length; loopexp++) {
     CAP_DEFINITIONS[loopexp] = AnnuityCapFloorInflationYearOnYearMonthlyDefinition.from(PRICE_INDEX_EUR, SETTLEMENT_DATE, NOTIONAL,
        COUPON_TENOR, COUPON_PAYMENT_TENOR, BUSINESS_DAY, CALENDAR, IS_EOM, 3, 3, LAST_KNOWN_FIXING_DATE, STRIKES[loopexp], IS_CAP);
     CAPS[loopexp]  = CAP_DEFINITIONS[loopexp] .toDerivative(REFERENCE_DATE, CURVES_NAME);
    
    }
    for (int loopexp = 0; loopexp < CAPS[0].getNumberOfPayments(); loopexp++) {  
      expiryTimes1[loopexp]=CAPS[0].getNthPayment(loopexp).getPaymentTime();
    }
    final InflationYearOnYearCapFloorParameters parameters = new InflationYearOnYearCapFloorParameters(expiryTimes1, strikes, volatilities, PRICE_INDEX_EUR);
    final SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationObjective objective = new SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationObjective(parameters, CUR);
    final SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationEngine calibrationEngine = new SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationEngine(objective);
    for (int loop1 = 0; loop1 < STRIKES.length; loop1++) {
      for (int loop2 = 0; loop2 < CAPS[loop1].getNumberOfPayments(); loop2++) {
        marketPrices[loop1][loop2]=METHOD.presentValue(CAPS[loop1].getNthPayment(loop2), BLACK_INFLATION).getAmount(CUR);
      }
      }
   
    
    for (int loop1 = 0; loop1 < STRIKES.length; loop1++) {
    for (int loop2 = 0; loop2 < CAPS[loop1].getNumberOfPayments(); loop2++) {
      calibrationEngine.addInstrument(CAPS[loop1].getNthPayment(loop2),marketPrices[loop1][loop2]);
    }
    }
    calibrationEngine.calibrate(MARKET.getInflationProvider());
    final MultipleCurrencyAmount[][] pvCapYearOnYear = new MultipleCurrencyAmount[STRIKES.length][CAPS[0].getNumberOfPayments()];
    for (int loop1 = 0; loop1 < STRIKES.length; loop1++) {
      for (int loop2 = 0; loop2 < CAPS[loop1].getNumberOfPayments(); loop2++)  {
      Interpolator2D interpolator = objective.getInflationCapYearOnYearProvider().getBlackParameters().getVolatilitySurface().getInterpolator();
      BlackSmileCapInflationYearOnYearParameters CalibratedBlackSmileCapInflationYearOnYearParameters = new BlackSmileCapInflationYearOnYearParameters(objective.getInflationCapYearOnYearParameters(), interpolator);
      BlackSmileCapInflationYearOnYearProvider CalibratedBlackSmileCapInflationYearOnYearProvider = new BlackSmileCapInflationYearOnYearProvider(objective.getInflationCapYearOnYearProvider().getInflationProvider(),
          CalibratedBlackSmileCapInflationYearOnYearParameters);
      pvCapYearOnYear[loop1][loop2] = METHOD.presentValue(CAPS[loop1].getNthPayment(loop2), CalibratedBlackSmileCapInflationYearOnYearProvider);
      assertEquals("Inflaiton year on year calibration: cap/floor " + loop1, pvCapYearOnYear[loop1][loop2].getAmount(CUR), marketPrices[loop1][loop2], 1E-2);
    }
    }
  }

}
