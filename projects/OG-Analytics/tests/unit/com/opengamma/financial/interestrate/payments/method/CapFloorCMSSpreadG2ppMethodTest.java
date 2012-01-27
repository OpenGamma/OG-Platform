package com.opengamma.financial.interestrate.payments.method;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.GeneratorDeposit;
import com.opengamma.financial.instrument.index.GeneratorSwap;
import com.opengamma.financial.instrument.index.IndexSwap;
import com.opengamma.financial.instrument.index.generator.USD6MLIBOR3M;
import com.opengamma.financial.instrument.index.generator.USDDeposit;
import com.opengamma.financial.instrument.payment.CapFloorCMSSpreadDefinition;
import com.opengamma.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.CapFloorCMSSpread;
import com.opengamma.financial.model.interestrate.G2ppTestsDataSet;
import com.opengamma.financial.model.interestrate.definition.G2ppPiecewiseConstantDataBundle;
import com.opengamma.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;

public class CapFloorCMSSpreadG2ppMethodTest {

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final GeneratorSwap GEN_USD6MLIBOR3M = new USD6MLIBOR3M(NYC);
  private static final GeneratorDeposit GEN_USD_DEPOSIT = new USDDeposit(NYC);
  private static final IndexSwap SWAP_USD10Y = new IndexSwap(GEN_USD6MLIBOR3M, Period.ofYears(10));
  private static final IndexSwap SWAP_USD2Y = new IndexSwap(GEN_USD6MLIBOR3M, Period.ofYears(2));

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofYears(5), GEN_USD_DEPOSIT);
  //  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(FIXING_DATE, GEN_USD6MLIBOR3M.getSpotLag(), NYC);

  // CMS spread coupon
  private static final double NOTIONAL = 100000000;
  private static final ZonedDateTime ACCRUAL_START_DATE = ScheduleCalculator.getAdjustedDate(FIXING_DATE, GEN_USD6MLIBOR3M.getSpotLag(), NYC);
  private static final ZonedDateTime ACCRUAL_END_DATE = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATE, Period.ofMonths(6), GEN_USD_DEPOSIT);
  private static final ZonedDateTime PAYMENT_DATE = ACCRUAL_END_DATE;
  private static final DayCount PAYMENT_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final double PAYMENT_ACCRUAL_FACTOR = PAYMENT_DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double STRIKE = -0.010; // 10 bps
  private static final boolean IS_CAP = true;
  private static final CapFloorCMSSpreadDefinition CMS_SPREAD_DEFINITION = CapFloorCMSSpreadDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL,
      SWAP_USD10Y, SWAP_USD2Y, STRIKE, IS_CAP);

  // Curves and parameters
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final String[] CURVE_NAMES = TestsDataSetsSABR.curves1Names();
  private static final G2ppPiecewiseConstantParameters PARAMETERS_G2PP = G2ppTestsDataSet.createG2ppParameters();
  private static final G2ppPiecewiseConstantDataBundle BUNDLE_G2PP = new G2ppPiecewiseConstantDataBundle(PARAMETERS_G2PP, CURVES);
  // Derivatives
  private static final CapFloorCMSSpread CMS_SPREAD = (CapFloorCMSSpread) CMS_SPREAD_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAMES);

  // Method and calculators
  private static final CapFloorCMSSpreadG2ppNumericalIntegrationMethod METHOD_NI = new CapFloorCMSSpreadG2ppNumericalIntegrationMethod();
  //  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  private static final double TOLERANCE_PRICE = 1.0E-2; // 0.01 currency unit for 100m notional.

  @Test
  /**
   * Tests the present value against the price explicitly computed for constant correlation. 
   */
  public void presentValue() {
    CurrencyAmount pv = METHOD_NI.presentValue(CMS_SPREAD, BUNDLE_G2PP);
    double pvPreviousRun = 472708.788;
    assertEquals("CMS spread: G2++ - present value", pvPreviousRun, pv.getAmount(), TOLERANCE_PRICE);

    //    double[] forward = new double[] {PRC.visit(CMS_SPREAD.getUnderlyingSwap1(), CURVES), PRC.visit(CMS_SPREAD.getUnderlyingSwap2(), CURVES)};
    //    double pvForward = NOTIONAL * PAYMENT_ACCRUAL_FACTOR * Math.max(forward[0] - forward[1] - STRIKE, 0.0) * CURVES.getCurve(CURVE_NAMES[0]).getDiscountFactor(CMS_SPREAD.getPaymentTime());
    //
    //    double test = 0.0;
    //    test++;
  }
}
