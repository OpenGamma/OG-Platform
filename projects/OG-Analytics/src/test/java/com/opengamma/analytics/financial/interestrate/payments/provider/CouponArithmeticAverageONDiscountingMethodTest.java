/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import static org.threeten.bp.temporal.ChronoUnit.MONTHS;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponArithmeticAverageONDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponArithmeticAverageON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.time.DateUtils;

public class CouponArithmeticAverageONDiscountingMethodTest {

  //  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IndexON FEDFUND = MulticurveProviderDiscountDataSets.getIndexesON()[0];
  //  private static final Currency USD = FEDFUND.getCurrency();
  private static final Calendar NYC = FEDFUND.getCalendar();
  private static final GeneratorSwapFixedON GENERATOR_SWAP_EONIA = GeneratorSwapFixedONMaster.getInstance().getGenerator("USD1YFEDFUND", NYC);
  private static final IborIndex USDLIBOR3M = IndexIborMaster.getInstance().getIndex("USDLIBOR3M", NYC);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 5, 23);
  private static final Period TENOR_3M = Period.of(3, MONTHS);
  private static final double NOTIONAL = 100000000; // 100m

  private static final String NOT_USED = "NOT USED";

  @Test(enabled = false)
  /**
   * Reports the error of the arithmetic average approximation by the log of the compounded rate.
   */
  public void averageApproximation() {

    MulticurveProviderDiscount multicurvesCst = new MulticurveProviderDiscount();
    YieldAndDiscountCurve curveCst = YieldCurve.from(ConstantDoublesCurve.from(0.0, "CST"));
    multicurvesCst.setCurve(FEDFUND, curveCst);

    double[] rateLevel = {0.01, 0.05, 0.10};
    int nbLevel = rateLevel.length;
    int nbStart = 36;
    Period step = Period.of(1, MONTHS);
    ZonedDateTime[] effectiveDate = new ZonedDateTime[nbStart];
    effectiveDate[0] = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, GENERATOR_SWAP_EONIA.getSpotLag(), NYC);

    double[][] payComp = new double[nbLevel][nbStart];
    double[][] payAA = new double[nbLevel][nbStart];
    double[][] payAAApprox = new double[nbLevel][nbStart];
    double[][] rateComp = new double[nbLevel][nbStart];
    double[][] rateAA = new double[nbLevel][nbStart];
    double[][] rateAAApprox = new double[nbLevel][nbStart];

    for (int looplevel = 0; looplevel < nbLevel; looplevel++) {
      curveCst = YieldCurve.from(ConstantDoublesCurve.from(rateLevel[looplevel], "CST"));
      multicurvesCst.replaceCurve(FEDFUND, curveCst);

      for (int loopstart = 0; loopstart < nbStart; loopstart++) {
        effectiveDate[loopstart] = ScheduleCalculator.getAdjustedDate(effectiveDate[0], step.multipliedBy(loopstart), USDLIBOR3M);
        ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(effectiveDate[loopstart], TENOR_3M, USDLIBOR3M);
        final CouponArithmeticAverageONDefinition cpnONDefinition = CouponArithmeticAverageONDefinition.from(FEDFUND, effectiveDate[loopstart], endDate, NOTIONAL, 0);
        final CouponArithmeticAverageON cpnON = cpnONDefinition.toDerivative(REFERENCE_DATE, NOT_USED);
        // Compute daily forwards
        int nbON = cpnON.getFixingPeriodAccrualFactors().length;
        double fwdON[] = new double[nbON];
        for (int loopon = 0; loopon < nbON; loopon++) {
          fwdON[loopon] = multicurvesCst.getForwardRate(FEDFUND, cpnON.getFixingPeriodTimes()[loopon], cpnON.getFixingPeriodTimes()[loopon + 1], cpnON.getFixingPeriodAccrualFactors()[loopon]);
        }
        // Compounded period forward
        payComp[looplevel][loopstart] = multicurvesCst.getForwardRate(FEDFUND, cpnON.getFixingPeriodTimes()[0], cpnON.getFixingPeriodTimes()[nbON], cpnON.getFixingPeriodTotalAccrualFactor())
            * cpnON.getFixingPeriodTotalAccrualFactor();
        payAA[looplevel][loopstart] = 0;
        for (int loopon = 0; loopon < nbON; loopon++) {
          payAA[looplevel][loopstart] += fwdON[loopon] * cpnON.getFixingPeriodAccrualFactors()[loopon];
        }
        payAAApprox[looplevel][loopstart] = Math.log(1 + payComp[looplevel][loopstart]);
        rateComp[looplevel][loopstart] = payComp[looplevel][loopstart] / cpnON.getFixingPeriodTotalAccrualFactor();
        rateAA[looplevel][loopstart] = payAA[looplevel][loopstart] / cpnON.getFixingPeriodTotalAccrualFactor();
        rateAAApprox[looplevel][loopstart] = payAAApprox[looplevel][loopstart] / cpnON.getFixingPeriodTotalAccrualFactor();
      }

    }
    //    int t = 0;
    //    t++;
  }

}
