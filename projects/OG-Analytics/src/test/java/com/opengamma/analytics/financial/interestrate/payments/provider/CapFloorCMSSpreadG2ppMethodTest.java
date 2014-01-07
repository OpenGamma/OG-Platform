package com.opengamma.analytics.financial.interestrate.payments.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.index.generator.EURDeposit;
import com.opengamma.analytics.financial.instrument.payment.CapFloorCMSSpreadDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMSSpread;
import com.opengamma.analytics.financial.model.interestrate.TestsDataSetG2pp;
import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.G2ppProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CapFloorCMSSpreadG2ppMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final G2ppPiecewiseConstantParameters PARAMETERS_G2PP = TestsDataSetG2pp.createG2ppParameters1();
  private static final G2ppProviderDiscount G2PP_MULTICURVES = new G2ppProviderDiscount(MULTICURVES, PARAMETERS_G2PP, EUR);

  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("EUR1YEURIBOR6M", CALENDAR);
  private static final GeneratorDeposit GEN_EUR_DEPOSIT = new EURDeposit(CALENDAR);
  private static final IndexSwap SWAP_EUR10Y = new IndexSwap(EUR1YEURIBOR6M, Period.ofYears(10));
  private static final IndexSwap SWAP_EUR2Y = new IndexSwap(EUR1YEURIBOR6M, Period.ofYears(2));

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(60), GEN_EUR_DEPOSIT);

  // CMS spread coupon
  private static final double NOTIONAL = 100000000;
  //  private static final double BP1 = 1.0E-4; // 1 basis point
  private static final ZonedDateTime ACCRUAL_START_DATE = ScheduleCalculator.getAdjustedDate(FIXING_DATE, EUR1YEURIBOR6M.getSpotLag(), CALENDAR);
  private static final ZonedDateTime ACCRUAL_END_DATE = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATE, Period.ofMonths(6), GEN_EUR_DEPOSIT);
  private static final ZonedDateTime PAYMENT_DATE = ACCRUAL_END_DATE;
  private static final DayCount PAYMENT_DAY_COUNT = DayCounts.ACT_360;
  private static final double PAYMENT_ACCRUAL_FACTOR = PAYMENT_DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double STRIKE = 0.0010; // 10 bps
  private static final boolean IS_CAP = true;
  private static final CapFloorCMSSpreadDefinition CMS_SPREAD_DEFINITION = CapFloorCMSSpreadDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL,
      SWAP_EUR10Y, SWAP_EUR2Y, STRIKE, IS_CAP, CALENDAR, CALENDAR);

  // Derivatives
  private static final CapFloorCMSSpread CMS_SPREAD = (CapFloorCMSSpread) CMS_SPREAD_DEFINITION.toDerivative(REFERENCE_DATE);

  // Method and calculators
  private static final CapFloorCMSSpreadG2ppNumericalIntegrationMethod METHOD_NI = CapFloorCMSSpreadG2ppNumericalIntegrationMethod.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2; // 0.01 currency unit for 100m notional.

  @Test
  /**
   * Tests the present value against a previous run.
   */
  public void presentValue() {
    final MultipleCurrencyAmount pv = METHOD_NI.presentValue(CMS_SPREAD, G2PP_MULTICURVES);
    final double pvPreviousRun = 102469.279; // 5Y - 6M - strike 10bp
    assertEquals("CMS spread: G2++ - present value", pvPreviousRun, pv.getAmount(EUR), TOLERANCE_PV);
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
