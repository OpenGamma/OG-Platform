/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import static org.testng.AssertJUnit.assertEquals;

import org.springframework.http.client.AbstractClientHttpRequest;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDAInstrumentTypes;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ISDACompliantYieldCurveBuildTest {

  private static final DayCount ACT365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final DayCount ACT360 = DayCountFactory.INSTANCE.getDayCount("ACT/360");
  private static final DayCount D30360 = DayCountFactory.INSTANCE.getDayCount("30/360");

  private static final BusinessDayConvention FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final BusinessDayConvention MOD_FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");

  @SuppressWarnings("unused")
  @Test
  public void test() {
    final boolean print = false;
    if (print) {
      System.out.println("ISDACompliantYieldCurveBuildTest: print should be false for commit");
    }

    // date from ISDA excel
    double[] sampleTimes = new double[] {0.0767123287671233, 0.167123287671233, 0.249315068493151, 0.498630136986301, 0.747945205479452, 0.997260273972603, 1.4958904109589, 1.99452054794521,
        2.5013698630137, 3.0027397260274, 3.5041095890411, 4.0027397260274, 4.5041095890411, 5.0027397260274, 5.5041095890411, 6.0027397260274, 6.5013698630137, 7, 7.50684931506849, 8.00547945205479,
        8.50684931506849, 9.00547945205479, 9.50684931506849, 10.0054794520548, 10.5068493150685, 11.0082191780822, 11.5068493150685, 12.0054794520548, 12.5041095890411, 13.0027397260274,
        13.5095890410959, 14.0082191780822, 14.5095890410959, 15.0109589041096, 15.5123287671233, 16.0109589041096, 16.5123287671233, 17.0109589041096, 17.5095890410959, 18.0082191780822,
        18.5068493150685, 19.013698630137, 19.5150684931507, 20.013698630137, 20.5150684931507, 21.013698630137, 21.5150684931507, 22.013698630137, 22.5150684931507, 23.013698630137,
        23.5123287671233, 24.0109589041096, 24.5178082191781, 25.0164383561644, 25.5178082191781, 26.0164383561644, 26.5178082191781, 27.0191780821918, 27.5205479452055, 28.0191780821918,
        28.5178082191781, 29.0164383561644, 29.5150684931507, 30.013698630137};
    double[] zeroRates = new double[] {0.00344732957665484, 0.00645427070262317, 0.010390833731528, 0.0137267241507424, 0.016406009142171, 0.0206548075787697, 0.0220059788254565, 0.0226815644487997,
        0.0241475224808774, 0.0251107341245228, 0.0263549710022889, 0.0272832610741453, 0.0294785565070328, 0.0312254350680597, 0.0340228731758456, 0.0363415444446394, 0.0364040719835966,
        0.0364576914896066, 0.0398713425199977, 0.0428078389323812, 0.0443206903065534, 0.0456582004054368, 0.0473373527805339, 0.0488404232471453, 0.0496433764260127, 0.0503731885238783,
        0.0510359350109291, 0.0516436290741354, 0.0526405492486405, 0.0535610094687589, 0.05442700569164, 0.0552178073994544, 0.0559581527041068, 0.0566490425640605, 0.0572429526830672,
        0.0577967261153023, 0.0583198210222109, 0.0588094750567186, 0.0592712408001043, 0.0597074348516306, 0.0601201241459759, 0.0605174325075768, 0.0608901411604128, 0.0612422922398251,
        0.0618707980423834, 0.0624661234885966, 0.0630368977571603, 0.0635787665840882, 0.064099413535239, 0.0645947156962813, 0.0650690099353217, 0.0655236050526131, 0.0659667431709796,
        0.0663851731522577, 0.0668735344788778, 0.0673405584796377, 0.0677924400667054, 0.0682275513575991, 0.0686468089170376, 0.0690488939824011, 0.0694369182384849, 0.06981160656508,
        0.0701736348572483, 0.0705236340943412};

    final ISDACompliantYieldCurveBuild bob = new ISDACompliantYieldCurveBuild();
    final LocalDate spotDate = LocalDate.of(2013, 5, 31);

    final int nMoneyMarket = 6;
    final int nSwaps = 14;
    final int nInstruments = nMoneyMarket + nSwaps;

    final ISDAInstrumentTypes[] types = new ISDAInstrumentTypes[nInstruments];
    Period[] tenors = new Period[nInstruments];
    final int[] mmMonths = new int[] {1, 2, 3, 6, 9, 12};
    final int[] swapYears = new int[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 20, 25, 30};
    // check
    ArgumentChecker.isTrue(mmMonths.length == nMoneyMarket, "mmMonths");
    ArgumentChecker.isTrue(swapYears.length == nSwaps, "swapYears");

    for (int i = 0; i < nMoneyMarket; i++) {
      types[i] = ISDAInstrumentTypes.MoneyMarket;
      tenors[i] = Period.ofMonths(mmMonths[i]);
    }
    for (int i = nMoneyMarket; i < nInstruments; i++) {
      types[i] = ISDAInstrumentTypes.Swap;
      tenors[i] = Period.ofYears(swapYears[i - nMoneyMarket]);
    }

    final double[] rates = new double[] {0.00340055550701297, 0.00636929056400781, 0.0102617798438113, 0.0135851258907251, 0.0162809551414651, 0.020583125112332, 0.0227369218210212,
        0.0251978805237614, 0.0273223815467694, 0.0310882447627048, 0.0358397743454067, 0.036047665095421, 0.0415916567616181, 0.044066373237682, 0.046708518178509, 0.0491196954851753,
        0.0529297239911766, 0.0562025436376854, 0.0589772202773522, 0.0607471217692999};

    final DayCount moneyMarketDCC = ACT360;
    final DayCount swapDCC = D30360;
    final DayCount curveDCC = ACT365;
    final Period swapInterval = Period.ofMonths(6);

    ISDACompliantCurve hc = bob.build(spotDate, types, tenors, rates, moneyMarketDCC, swapDCC, swapInterval, curveDCC, MOD_FOLLOWING);

    final int nCurvePoints = hc.getNumberOfKnots();
    assertEquals(nInstruments, nCurvePoints);
    final int nSamplePoints = sampleTimes.length;
    for (int i = 0; i < nSamplePoints; i++) {
      double time = sampleTimes[i];
      double zr = hc.getZeroRate(time);
      assertEquals("time:" + time, zeroRates[i], zr, 1e-10);
    }

    if (print) {
      for (int i = 0; i < nCurvePoints; i++) {
        System.out.println(hc.getTimeAtIndex(i) + "\t" + hc.getZeroRateAtIndex(i));
      }
    }

    // TODO extract timing to another test
    final int warmup = 1;
    final int hotSpot = 0;

    for (int i = 0; i < warmup; i++) {
      ISDACompliantCurve hc1 = bob.build(spotDate, types, tenors, rates, moneyMarketDCC, swapDCC, swapInterval, curveDCC, MOD_FOLLOWING);
    }

    if (hotSpot > 0) {
      long t0 = System.nanoTime();
      for (int i = 0; i < warmup; i++) {
        ISDACompliantCurve hc1 = bob.build(spotDate, types, tenors, rates, moneyMarketDCC, swapDCC, swapInterval, curveDCC, MOD_FOLLOWING);
      }
      System.out.println("time to build yield curve: " + (System.nanoTime() - t0) / 1e6 / hotSpot + "ms");
    }

  }

  @Test
  public void anotherTest() {
    final boolean print = false;
    if (print) {
      System.out.println("ISDACompliantYieldCurveBuildTest: print should be false for commit");
    }

    // date from ISDA excel
    double[] sampleTimes = new double[] {0.0876712328767123, 0.167123287671233, 0.252054794520548, 0.495890410958904, 0.747945205479452, 1, 1.4958904109589, 2.00547945205479, 2.5041095890411,
        3.0027397260274, 3.5013698630137, 4.0027397260274, 4.4986301369863, 5.0027397260274, 5.4986301369863, 6.0027397260274, 6.5013698630137, 7.01095890410959, 7.5013698630137, 8.00821917808219,
        8.50684931506849, 9.00547945205479, 9.5041095890411, 10.0054794520548, 10.5041095890411, 11.0082191780822, 11.5041095890411, 12.0082191780822, 12.5041095890411, 13.013698630137,
        13.5041095890411, 14.0109589041096, 14.5095890410959, 15.0109589041096, 15.5068493150685, 16.0109589041096, 16.5068493150685, 17.0109589041096, 17.5068493150685, 18.0109589041096,
        18.5095890410959, 19.0164383561644, 19.5150684931507, 20.013698630137, 20.5123287671233, 21.013698630137, 21.5095890410959, 22.013698630137, 22.5123287671233, 23.0164383561644,
        23.5123287671233, 24.0219178082192, 24.5123287671233, 25.0191780821918, 25.5178082191781, 26.0164383561644, 26.5150684931507, 27.0191780821918, 27.5150684931507, 28.0191780821918,
        28.5150684931507, 29.0191780821918, 29.5150684931507, 30.0246575342466};
    double[] zeroRates = new double[] {0.00451091345592003, 0.0096120532508373, 0.0124886704800469, 0.0179287581253996, 0.019476202462918, 0.0209073273478429, 0.0180925538740485, 0.0166502405937304,
        0.0189037116841984, 0.0204087671935255, 0.0220943506849952, 0.0233657744039486, 0.0246460468575126, 0.0256873833598965, 0.026666390851819, 0.0274958283375808, 0.028228774560615,
        0.0288701107678566, 0.0294694929454103, 0.0300118234002438, 0.0305061047348909, 0.0309456497124306, 0.0313781991283657, 0.0317696564018493, 0.0321646717802045, 0.0325276505922571,
        0.0329486243843157, 0.0333409374474117, 0.0336496168922921, 0.0339423150176603, 0.0342031385938489, 0.034453517898306, 0.0346827676795623, 0.0348979210010215, 0.0349547278282821,
        0.0350088694020237, 0.0350589017641339, 0.0351067734588913, 0.035151174765217, 0.0351938059061586, 0.0352336892661124, 0.0352720864818463, 0.0353079147726051, 0.0353419577796079,
        0.0353037376607363, 0.0352671363539399, 0.0352326134807957, 0.0351991126433607, 0.035167451913752, 0.0351368377606211, 0.0351080035690964, 0.0350796130984763, 0.035053405709698,
        0.0350273994983831, 0.0350148748938213, 0.0350028303815154, 0.0349912388762854, 0.0349799549048451, 0.0349692583262832, 0.0349587725430485, 0.0349488194559029, 0.0349390500683469,
        0.0349297655642079, 0.0349205440948243};

    final ISDACompliantYieldCurveBuild bob = new ISDACompliantYieldCurveBuild();
    final LocalDate spotDate = LocalDate.of(2009, 11, 12);

    final int nMoneyMarket = 6;
    final int nSwaps = 15;
    final int nInstruments = nMoneyMarket + nSwaps;

    final ISDAInstrumentTypes[] types = new ISDAInstrumentTypes[nInstruments];
    Period[] tenors = new Period[nInstruments];
    final int[] mmMonths = new int[] {1, 2, 3, 6, 9, 12};
    final int[] swapYears = new int[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 15, 20, 25, 30};
    // check
    ArgumentChecker.isTrue(mmMonths.length == nMoneyMarket, "mmMonths");
    ArgumentChecker.isTrue(swapYears.length == nSwaps, "swapYears");

    for (int i = 0; i < nMoneyMarket; i++) {
      types[i] = ISDAInstrumentTypes.MoneyMarket;
      tenors[i] = Period.ofMonths(mmMonths[i]);
    }
    for (int i = nMoneyMarket; i < nInstruments; i++) {
      types[i] = ISDAInstrumentTypes.Swap;
      tenors[i] = Period.ofYears(swapYears[i - nMoneyMarket]);
    }

    final double[] rates = new double[] {0.00445, 0.009488, 0.012337, 0.017762, 0.01935, 0.020838, 0.01652, 0.02018, 0.023033, 0.02525, 0.02696, 0.02825, 0.02931, 0.03017, 0.03092, 0.0316, 0.03231,
        0.03367, 0.03419, 0.03411, 0.03412};

    final DayCount moneyMarketDCC = ACT360;
    final DayCount swapDCC = ACT360;
    final DayCount curveDCC = ACT365;
    final Period swapInterval = Period.ofMonths(6);

    ISDACompliantCurve hc = bob.build(spotDate, types, tenors, rates, moneyMarketDCC, swapDCC, swapInterval, curveDCC, FOLLOWING);

    final int nCurvePoints = hc.getNumberOfKnots();
    assertEquals(nInstruments, nCurvePoints);
    final int nSamplePoints = sampleTimes.length;
    for (int i = 0; i < nSamplePoints; i++) {
      double time = sampleTimes[i];
      double zr = hc.getZeroRate(time);
      assertEquals("time:" + time, zeroRates[i], zr, 1e-10);
    }

    if (print) {
      for (int i = 0; i < nCurvePoints; i++) {
        System.out.println(hc.getTimeAtIndex(i) + "\t" + hc.getZeroRateAtIndex(i));
      }
    }

  }
}
