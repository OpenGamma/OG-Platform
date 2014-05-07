/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.options;

import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.isdastandardmodel.AnalyticCDSPricer;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDABaseTest;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PriceType;
import com.opengamma.analytics.financial.credit.isdastandardmodel.fastcalibration.CreditCurveCalibrator;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DefaultSwaptionTest extends ISDABaseTest {
  private static AnalyticCDSPricer PRICER = new AnalyticCDSPricer();
  private static ISDACompliantYieldCurve YIELD_CURVE;
  private static ISDACompliantCreditCurve CREDIT_CURVE;
  private static CDSAnalytic CDS;
  private static double T;

  static {

    final CDSAnalyticFactory factory = new CDSAnalyticFactory();
    final LocalDate tradeDate = LocalDate.of(2013, Month.NOVEMBER, 28);
    final LocalDate spotDate = addWorkDays(tradeDate.minusDays(1), 3, DEFAULT_CALENDAR);
    final LocalDate expiry = LocalDate.of(2014, 3, 20);

    final String[] yieldCurvePoints = new String[] {"1M", "2M", "3M", "6M", "9M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y" };
    final String[] yieldCurveInstruments = new String[] {"M", "M", "M", "M", "M", "M", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" };
    final double[] rates = new double[] {0.00445, 0.009488, 0.012337, 0.017762, 0.01935, 0.020838, 0.01652, 0.02018, 0.023033, 0.02525, 0.02696, 0.02825, 0.02931, 0.03017, 0.03092, 0.0316, 0.03231,
        0.03367, 0.03419, 0.03411, 0.03412 };
    YIELD_CURVE = makeYieldCurve(tradeDate, spotDate, yieldCurvePoints, yieldCurveInstruments, rates, ACT360, D30360, Period.ofYears(1));

    final Period[] tenors = new Period[] {Period.ofMonths(6), Period.ofYears(1), Period.ofYears(3), Period.ofYears(5), Period.ofYears(7), Period.ofYears(10) };
    final CDSAnalytic[] pillarCDS = factory.makeIMMCDS(tradeDate, tenors);
    final double[] spreads = new double[] {0.00886315689995649, 0.00886315689995649, 0.0133044689825873, 0.0171490070952563, 0.0183903639181293, 0.0194721890639724 };
    final CreditCurveCalibrator calibrator = new CreditCurveCalibrator(pillarCDS, YIELD_CURVE);
    CREDIT_CURVE = calibrator.calibrate(spreads);

    CDS = factory.makeForwardStartingCDS(tradeDate, expiry, LocalDate.of(2019, 3, 20));
    T = ACT365F.getDayCountFraction(tradeDate, expiry);
  }

  /**
   * Round trip test. Does not test that the default swaption is priced correctly, just that the implied vol formula is consistent with the pricing 
   */
  @Test
  public void impliedVolConsistencyTest() {
    final DefaultSwaption ds = new DefaultSwaption();

    final double fwdSpread = PRICER.parSpread(CDS, YIELD_CURVE, CREDIT_CURVE);

    final boolean hasFEP = true;
    for (int j = 0; j < 20; j++) {
      final double vol = 0.1 + 0.7 * j / 20.;
      for (int i = 0; i < 20; i++) {
        final double k = fwdSpread * Math.exp(vol * Math.sqrt(T) * 6 * (i / 20. - 0.5));
        final boolean isPayer = k > fwdSpread;
        final double price = ds.price(CDS, YIELD_CURVE, CREDIT_CURVE, k, T, vol, isPayer, hasFEP);
        final double iv = ds.impliedVol(CDS, YIELD_CURVE, CREDIT_CURVE, k, T, price, isPayer, hasFEP);
        //  System.out.println(k + " " + price + "\t" + vol + "\t" + iv);
        assertEquals(isPayer + " " + i + " " + j, vol, iv, 1e-9);
      }
    }
  }

  /**
   * Logic taken from {@link SingleNameCDSOptionTest} 
   */
  @Test
  public void priceTest() {
    final DefaultSwaption ds = new DefaultSwaption();

    final LocalDate tradeDate = LocalDate.of(2014, 2, 5);
    final LocalDate expiry = LocalDate.of(2014, 3, 20);
    final LocalDate maturity = LocalDate.of(2019, 6, 20);
    final double[] pillarParSpreads;

    final CDSAnalyticFactory factory = new CDSAnalyticFactory();
    final Period[] pillarTenors = new Period[] {Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(7),
        Period.ofYears(10) };
    final CDSAnalytic[] pillarCDS = factory.makeIMMCDS(tradeDate, pillarTenors);

    final double[] strikes = new double[] {100, 140, 150, 160, 170, 180, 182.767, 190, 200, 210, 220, 230, 250, 300 };

    final double[] spreads = new double[] {57.43, 74.97, 111.32, 139.32, 157.64, 173.66, 209.28, 228.35 };
    final int n = spreads.length;
    pillarParSpreads = new double[n];
    for (int i = 0; i < n; i++) {
      pillarParSpreads[i] = spreads[i] * ONE_BP;
    }

    final double tEAlt = ACT_ACT_ISDA.getDayCountFraction(tradeDate, expiry);
    final CDSAnalytic fwdCDS = factory.makeCDS(tradeDate, expiry.plusDays(1), expiry.plusDays(1), expiry.plusDays(1), maturity);
    final ISDACompliantCreditCurve cc = CREDIT_CURVE_BUILDER.calibrateCreditCurve(pillarCDS, pillarParSpreads, YIELD_CURVE);

    final double expFwdProt = PRICER.protectionLeg(fwdCDS, YIELD_CURVE, cc, 0.0);
    final double expFwdAnnuity = PRICER.annuity(fwdCDS, YIELD_CURVE, cc, PriceType.CLEAN, 0.0);
    final double fwdSpread = expFwdProt / expFwdAnnuity;

    final double vol = 0.33;
    final int m = strikes.length;
    for (int i = 0; i < m; i++) {
      final double pRef = expFwdAnnuity * BlackFormulaRepository.price(fwdSpread, strikes[i] * ONE_BP, tEAlt, vol, true);
      final double p = ds.price(fwdCDS, YIELD_CURVE, cc, strikes[i] * ONE_BP, tEAlt, vol, true, false);
      assertEquals(pRef, p, 1.e-12);
    }
  }
}
