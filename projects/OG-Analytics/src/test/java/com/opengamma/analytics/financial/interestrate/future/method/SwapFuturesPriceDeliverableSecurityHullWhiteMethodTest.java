/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.SwapFuturesPriceDeliverableSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.CashFlowEquivalentCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableSecurity;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.TestsDataSetHullWhite;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing of deliverable interest rate swap futures as traded on CME.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class SwapFuturesPriceDeliverableSecurityHullWhiteMethodTest {

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("USD6MLIBOR3M", NYC);
  private static final ZonedDateTime EFFECTIVE_DATE = DateUtils.getUTCDate(2012, 12, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(EFFECTIVE_DATE, -USD6MLIBOR3M.getSpotLag(), NYC);
  private static final Period TENOR = Period.ofYears(10);
  private static final double NOTIONAL = 100000;
  private static final double RATE = 0.0175;
  private static final SwapFixedIborDefinition SWAP_DEFINITION = SwapFixedIborDefinition.from(EFFECTIVE_DATE, TENOR, USD6MLIBOR3M, 1.0, RATE, false);
  private static final SwapFuturesPriceDeliverableSecurityDefinition SWAP_FUTURES_SECURITY_DEFINITION = new SwapFuturesPriceDeliverableSecurityDefinition(LAST_TRADING_DATE, SWAP_DEFINITION, NOTIONAL);

  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves2();
  private static final String[] CURVES_NAMES = TestsDataSetsSABR.curves2Names();
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 9, 20);
  private static final SwapFuturesPriceDeliverableSecurity SWAP_FUTURES_SECURITY = SWAP_FUTURES_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAMES);

  private static final HullWhiteOneFactorPiecewiseConstantParameters PARAMETERS_HW = TestsDataSetHullWhite.createHullWhiteParameters();
  private static final HullWhiteOneFactorPiecewiseConstantDataBundle BUNDLE_HW = new HullWhiteOneFactorPiecewiseConstantDataBundle(PARAMETERS_HW, CURVES);

  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();
  private static final CashFlowEquivalentCalculator CFEC = CashFlowEquivalentCalculator.getInstance();
  private static final SwapFuturesDeliverableSecurityHullWhiteMethod METHOD_SWAP_FUT_HW = SwapFuturesDeliverableSecurityHullWhiteMethod.getInstance();

  private static final double TOLERANCE_PRICE = 1.0E-10;

  @Test
  public void price() {
    final AnnuityPaymentFixed cfe = SWAP_FUTURES_SECURITY.getUnderlyingSwap().accept(CFEC, CURVES);
    final int nbCfe = cfe.getNumberOfPayments();
    final double[] adj = new double[nbCfe];
    final double[] df = new double[nbCfe];
    for (int loopcf = 0; loopcf < nbCfe; loopcf++) {
      df[loopcf] = CURVES.getCurve(CURVES_NAMES[0]).getDiscountFactor(cfe.getNthPayment(loopcf).getPaymentTime());
      adj[loopcf] = MODEL.futuresConvexityFactor(PARAMETERS_HW, SWAP_FUTURES_SECURITY.getTradingLastTime(), cfe.getNthPayment(loopcf).getPaymentTime(), SWAP_FUTURES_SECURITY.getDeliveryTime());
      assertTrue("DeliverableSwapFuturesSecurityHullWhiteMethod: price", adj[loopcf] <= 1.0);
    }
    double priceExpected = 1.0;
    for (int loopcf = 0; loopcf < nbCfe; loopcf++) {
      priceExpected += (cfe.getNthPayment(loopcf).getAmount() * df[loopcf] * adj[loopcf]) / df[0];
    }
    final double priceComputed = METHOD_SWAP_FUT_HW.price(SWAP_FUTURES_SECURITY, BUNDLE_HW);
    assertEquals("DeliverableSwapFuturesSecurityDefinition: price", priceExpected, priceComputed, TOLERANCE_PRICE);
  }

}
