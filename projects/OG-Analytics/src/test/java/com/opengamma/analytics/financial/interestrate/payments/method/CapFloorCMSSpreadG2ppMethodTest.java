package com.opengamma.analytics.financial.interestrate.payments.method;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.index.generator.USDDeposit;
import com.opengamma.analytics.financial.instrument.payment.CapFloorCMSSpreadDefinition;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMSSpread;
import com.opengamma.analytics.financial.model.interestrate.TestsDataSetG2pp;
import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantDataBundle;
import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class CapFloorCMSSpreadG2ppMethodTest {

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final GeneratorSwapFixedIbor GEN_USD6MLIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("USD6MLIBOR3M", NYC);
  private static final GeneratorDeposit GEN_USD_DEPOSIT = new USDDeposit(NYC);
  private static final IndexSwap SWAP_USD10Y = new IndexSwap(GEN_USD6MLIBOR3M, Period.ofYears(10));
  private static final IndexSwap SWAP_USD2Y = new IndexSwap(GEN_USD6MLIBOR3M, Period.ofYears(2));

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(60), GEN_USD_DEPOSIT);

  // CMS spread coupon
  private static final double NOTIONAL = 100000000;
  //  private static final double BP1 = 1.0E-4; // 1 basis point
  private static final ZonedDateTime ACCRUAL_START_DATE = ScheduleCalculator.getAdjustedDate(FIXING_DATE, GEN_USD6MLIBOR3M.getSpotLag(), NYC);
  private static final ZonedDateTime ACCRUAL_END_DATE = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATE, Period.ofMonths(6), GEN_USD_DEPOSIT);
  private static final ZonedDateTime PAYMENT_DATE = ACCRUAL_END_DATE;
  private static final DayCount PAYMENT_DAY_COUNT = DayCounts.ACT_360;
  private static final double PAYMENT_ACCRUAL_FACTOR = PAYMENT_DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double STRIKE = 0.0010; // 10 bps
  private static final boolean IS_CAP = true;
  private static final CapFloorCMSSpreadDefinition CMS_SPREAD_DEFINITION = CapFloorCMSSpreadDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL,
      SWAP_USD10Y, SWAP_USD2Y, STRIKE, IS_CAP, NYC, NYC);

  // Curves and parameters
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves2();
  private static final String[] CURVE_NAMES = TestsDataSetsSABR.curves2Names();
  private static final G2ppPiecewiseConstantParameters PARAMETERS_G2PP = TestsDataSetG2pp.createG2ppParameters1();
  private static final G2ppPiecewiseConstantDataBundle BUNDLE_G2PP = new G2ppPiecewiseConstantDataBundle(PARAMETERS_G2PP, CURVES);
  // Derivatives
  private static final CapFloorCMSSpread CMS_SPREAD = (CapFloorCMSSpread) CMS_SPREAD_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAMES);

  // Method and calculators
  private static final CapFloorCMSSpreadG2ppNumericalIntegrationMethod METHOD_NI = new CapFloorCMSSpreadG2ppNumericalIntegrationMethod();

  private static final double TOLERANCE_PV = 1.0E-2; // 0.01 currency unit for 100m notional.

  //  private static final double TOLERANCE_PV_APPROX = 1.0E-1; // 0.1 bp

  @Test
  /**
   * Tests the present value against a previous run.
   */
  public void presentValue() {
    final CurrencyAmount pv = METHOD_NI.presentValue(CMS_SPREAD, BUNDLE_G2PP);
    final double pvPreviousRun = 73582.631; // 5Y - 6M - strike 10bp
    assertEquals("CMS spread: G2++ - present value", pvPreviousRun, pv.getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value by numerical integration vs approximation.
   */
  public void presentValueNIntegrationVsApproximation() {

    // TODO
    //    double[] forward = new double[] {PRC.visit(CMS_SPREAD.getUnderlyingSwap1(), CURVES), PRC.visit(CMS_SPREAD.getUnderlyingSwap2(), CURVES)};
    //    double atm = forward[0] - forward[1];
    //    double[] shift = new double[] {-0.0100, -0.0050, 0.0, 0.0050, 0.0100};
    //    double[] pvNI = new double[shift.length];
    //    double[] pvApprox = new double[shift.length];
    //    for (int loopstrike = 0; loopstrike < shift.length; loopstrike++) {
    //      CapFloorCMSSpreadDefinition cmsSpreadDefinition = CapFloorCMSSpreadDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, SWAP_USD10Y, SWAP_USD2Y,
    //          atm + shift[loopstrike], IS_CAP);
    //      CapFloorCMSSpread cmsSpread = (CapFloorCMSSpread) cmsSpreadDefinition.toDerivative(REFERENCE_DATE, CURVE_NAMES);
    //      pvNI[loopstrike] = METHOD_NI.presentValue(cmsSpread, BUNDLE_G2PP).getAmount() / NOTIONAL / BP1;
    //      pvApprox[loopstrike] = METHOD_APPROX.presentValue(cmsSpread, BUNDLE_G2PP).getAmount() / NOTIONAL / BP1;
    //      assertEquals("CMS spread: G2++ - present value", pvNI[loopstrike], pvApprox[loopstrike], TOLERANCE_PV_APPROX);
    //    }
  }

}
