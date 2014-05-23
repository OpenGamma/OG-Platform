/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtils;

// CSOFF

/**
 * 
 */
public abstract class ISDAModelDatasets {

  // to convert LocalDate to ZonedDateTime (actual date used here is irrelevant)
  protected static final LocalTime LOCAL_TIME = DateUtils.getUTCDate(2013, 4, 21).toLocalTime(); // to convert LocalDate to ZonedDateTime
  protected static final ZoneId TIME_ZONE = ZoneId.of("Z");

  protected static final DayCount ACT360 = DayCounts.ACT_360;
  protected static final DayCount ACT365 = DayCounts.ACT_365;
  protected static final double OFFSET = 0.0;// 1. / 365;

  protected static class ISDA_Results {

    // global inputs
    public LocalDate today;
    public double notional;
    public double recoveryRate;

    // curves
    public ISDACompliantDateYieldCurve yieldCurve;
    public ISDACompliantDateCreditCurve creditCurve;

    // inputs for the CDS to be priced
    public LocalDate startDate;
    public LocalDate endDate;
    public double fracSpread;

    // outputs
    public double protectionLeg;
    public double premiumLeg;
    public double defaultAcc;
    public double accruedPremium;
    public int accruedDays;

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + accruedDays;
      long temp;
      temp = Double.doubleToLongBits(accruedPremium);
      result = prime * result + (int) (temp ^ (temp >>> 32));
      result = prime * result + ((creditCurve == null) ? 0 : creditCurve.hashCode());
      temp = Double.doubleToLongBits(defaultAcc);
      result = prime * result + (int) (temp ^ (temp >>> 32));
      result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
      temp = Double.doubleToLongBits(fracSpread);
      result = prime * result + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(notional);
      result = prime * result + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(premiumLeg);
      result = prime * result + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(protectionLeg);
      result = prime * result + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(recoveryRate);
      result = prime * result + (int) (temp ^ (temp >>> 32));
      result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
      result = prime * result + ((today == null) ? 0 : today.hashCode());
      result = prime * result + ((yieldCurve == null) ? 0 : yieldCurve.hashCode());
      return result;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final ISDA_Results other = (ISDA_Results) obj;
      if (accruedDays != other.accruedDays) {
        return false;
      }
      if (Double.doubleToLongBits(accruedPremium) != Double.doubleToLongBits(other.accruedPremium)) {
        return false;
      }
      if (creditCurve == null) {
        if (other.creditCurve != null) {
          return false;
        }
      } else if (!creditCurve.equals(other.creditCurve)) {
        return false;
      }
      if (Double.doubleToLongBits(defaultAcc) != Double.doubleToLongBits(other.defaultAcc)) {
        return false;
      }
      if (endDate == null) {
        if (other.endDate != null) {
          return false;
        }
      } else if (!endDate.equals(other.endDate)) {
        return false;
      }
      if (Double.doubleToLongBits(fracSpread) != Double.doubleToLongBits(other.fracSpread)) {
        return false;
      }
      if (Double.doubleToLongBits(notional) != Double.doubleToLongBits(other.notional)) {
        return false;
      }
      if (Double.doubleToLongBits(premiumLeg) != Double.doubleToLongBits(other.premiumLeg)) {
        return false;
      }
      if (Double.doubleToLongBits(protectionLeg) != Double.doubleToLongBits(other.protectionLeg)) {
        return false;
      }
      if (Double.doubleToLongBits(recoveryRate) != Double.doubleToLongBits(other.recoveryRate)) {
        return false;
      }
      if (startDate == null) {
        if (other.startDate != null) {
          return false;
        }
      } else if (!startDate.equals(other.startDate)) {
        return false;
      }
      if (today == null) {
        if (other.today != null) {
          return false;
        }
      } else if (!today.equals(other.today)) {
        return false;
      }
      if (yieldCurve == null) {
        if (other.yieldCurve != null) {
          return false;
        }
      } else if (!yieldCurve.equals(other.yieldCurve)) {
        return false;
      }
      return true;
    }

  }

  public static ISDA_Results[] getExample1() {

    // global data
    final double notional = 1e7;
    final double recoveryRate = 0.4;

    // curve data
    final int nCurvePoints = 7;
    final LocalDate[] parSpreadDates = new LocalDate[] {LocalDate.of(2013, 6, 20), LocalDate.of(2013, 9, 20), LocalDate.of(2014, 3, 20), LocalDate.of(2015, 3, 20), LocalDate.of(2016, 3, 20),
      LocalDate.of(2018, 3, 20), LocalDate.of(2023, 3, 20) };
    // final double[] parSpreads = new double[] {50, 70, 100, 150, 200, 400, 1000};
    // check data
    ArgumentChecker.isTrue(nCurvePoints == parSpreadDates.length, "parSpreadDates should have {} entires", nCurvePoints);
    // ArgumentChecker.isTrue(nCurvePoints == parSpreads.length, "parSpreads should have {} entires", nCurvePoints);

    final int nSets = 1;
    final LocalDate[] today = new LocalDate[] {LocalDate.of(2013, 2, 2) };

    final LocalDate[] curveInstrumentsStartDate = new LocalDate[] {LocalDate.of(2012, 7, 29) };

    final double[][] q = new double[][] {{0.996810615509364, 0.99256914801695, 0.981114953972081, 0.947234616931817, 0.898378314399555, 0.689993952066726, 0.0366572089666166 } };

    final LocalDate[] cdsStartDate = new LocalDate[] {LocalDate.of(2012, 9, 12) };

    final LocalDate[] cdsEndDate = new LocalDate[] {LocalDate.of(2018, 7, 4) };

    final double[] spreadBP = new double[] {100 };

    final double[] premLeg = new double[] {483704.040209604 };

    final double[] protectionLeg = new double[] {2508592.58471593 };

    final double[] defaultAcc = new double[] {5497.55738374288 };

    final double[] accruedPrem = new double[] {8333.33333333325 };

    final int[] accruedDays = new int[] {30 };

    // check data hasn't been messed up
    ArgumentChecker.isTrue(nSets == today.length, "today");
    ArgumentChecker.isTrue(nSets == curveInstrumentsStartDate.length, "curveInstrumentsStartDate");
    ArgumentChecker.isTrue(nSets == q.length, "q");
    ArgumentChecker.isTrue(nCurvePoints == q[0].length, "q");
    ArgumentChecker.isTrue(nSets == cdsStartDate.length, "cdsStartDate");
    ArgumentChecker.isTrue(nSets == cdsEndDate.length, "cdsEndDate");
    ArgumentChecker.isTrue(nSets == spreadBP.length, "spreadBP");
    ArgumentChecker.isTrue(nSets == premLeg.length, "premLeg");
    ArgumentChecker.isTrue(nSets == protectionLeg.length, "protectionLeg");
    ArgumentChecker.isTrue(nSets == defaultAcc.length, "defaultAcc");
    ArgumentChecker.isTrue(nSets == accruedPrem.length, "accruedPrem");
    ArgumentChecker.isTrue(nSets == accruedDays.length, "accruedDays");

    // since current OG ISDA Java code used ZoneDateTime, 'upscale' LocalTime
    final ZonedDateTime[] curveTenors = new ZonedDateTime[nCurvePoints];
    for (int j = 0; j < nCurvePoints; j++) {
      curveTenors[j] = ZonedDateTime.of(parSpreadDates[j], LOCAL_TIME, TIME_ZONE);
    }

    final ISDA_Results[] res = new ISDA_Results[nSets];
    for (int i = 0; i < nSets; i++) {
      final ISDA_Results temp = new ISDA_Results();
      temp.notional = notional;
      temp.recoveryRate = recoveryRate;
      temp.today = today[i];

      // build the credit curve
      final double[] surProb = q[i];
      final double[] t = new double[nCurvePoints];
      final double[] r = new double[nCurvePoints];
      for (int j = 0; j < nCurvePoints; j++) {
        t[j] = ACT365.getDayCountFraction(temp.today, parSpreadDates[j]);
        r[j] = -Math.log(surProb[j]) / t[j];

      }
      // note offset is zero in keeping with ISDA code
      temp.creditCurve = new ISDACompliantDateCreditCurve(temp.today, parSpreadDates, r, ACT365);
      // temp.creditCurve = new HazardRateCurve(curveTenors, t, r, OFFSET);

      // cds inputs
      temp.startDate = cdsStartDate[i];
      temp.endDate = cdsEndDate[i];
      temp.fracSpread = spreadBP[i] / 10000;

      // cds outputs
      temp.premiumLeg = premLeg[i];
      temp.protectionLeg = protectionLeg[i];
      temp.defaultAcc = defaultAcc[i];
      temp.accruedPremium = accruedPrem[i];
      temp.accruedDays = accruedDays[i];

      res[i] = temp;
    }

    return res;
  }

  // TODO replace with a CSV reader
  public static ISDA_Results[] getExample3() {

    // global data
    final double notional = 1e7;
    final double recoveryRate = 0.4;

    // curve data
    final int nCurvePoints = 7;
    final LocalDate[] parSpreadDates = new LocalDate[] {LocalDate.of(2013, 6, 20), LocalDate.of(2013, 9, 20), LocalDate.of(2014, 3, 20), LocalDate.of(2015, 3, 20), LocalDate.of(2016, 3, 20),
      LocalDate.of(2018, 3, 20), LocalDate.of(2023, 3, 20) };
    // final double[] parSpreads = new double[] {50, 70, 100, 150, 200, 400, 1000};
    // check data
    ArgumentChecker.isTrue(nCurvePoints == parSpreadDates.length, "parSpreadDates should have {} entires", nCurvePoints);
    // ArgumentChecker.isTrue(nCurvePoints == parSpreads.length, "parSpreads should have {} entires", nCurvePoints);

    final int nSets = 100;
    final LocalDate[] today = new LocalDate[] {LocalDate.of(2013, 2, 2), LocalDate.of(2012, 9, 22), LocalDate.of(2012, 7, 12), LocalDate.of(2013, 3, 17), LocalDate.of(2013, 5, 1),
      LocalDate.of(2012, 7, 16), LocalDate.of(2012, 9, 20), LocalDate.of(2013, 3, 20), LocalDate.of(2012, 10, 21), LocalDate.of(2013, 4, 12), LocalDate.of(2012, 6, 9), LocalDate.of(2013, 3, 13),
      LocalDate.of(2013, 5, 18), LocalDate.of(2012, 8, 22), LocalDate.of(2012, 9, 17), LocalDate.of(2013, 2, 21), LocalDate.of(2012, 12, 10), LocalDate.of(2012, 7, 5), LocalDate.of(2012, 9, 12),
      LocalDate.of(2012, 8, 29), LocalDate.of(2012, 7, 7), LocalDate.of(2013, 3, 7), LocalDate.of(2013, 1, 9), LocalDate.of(2013, 5, 13), LocalDate.of(2012, 9, 9), LocalDate.of(2012, 5, 26),
      LocalDate.of(2012, 6, 2), LocalDate.of(2012, 6, 15), LocalDate.of(2012, 8, 15), LocalDate.of(2013, 1, 20), LocalDate.of(2013, 1, 17), LocalDate.of(2013, 4, 17), LocalDate.of(2012, 8, 13),
      LocalDate.of(2013, 3, 1), LocalDate.of(2013, 4, 14), LocalDate.of(2013, 3, 8), LocalDate.of(2012, 9, 10), LocalDate.of(2012, 6, 25), LocalDate.of(2012, 8, 25), LocalDate.of(2012, 8, 23),
      LocalDate.of(2012, 11, 23), LocalDate.of(2012, 10, 1), LocalDate.of(2012, 9, 7), LocalDate.of(2012, 6, 21), LocalDate.of(2012, 6, 11), LocalDate.of(2013, 3, 26), LocalDate.of(2012, 7, 20),
      LocalDate.of(2012, 9, 13), LocalDate.of(2012, 6, 10), LocalDate.of(2012, 10, 28), LocalDate.of(2012, 8, 12), LocalDate.of(2012, 10, 23), LocalDate.of(2012, 8, 6), LocalDate.of(2012, 11, 23),
      LocalDate.of(2012, 12, 18), LocalDate.of(2012, 7, 10), LocalDate.of(2012, 10, 3), LocalDate.of(2012, 11, 18), LocalDate.of(2013, 1, 17), LocalDate.of(2012, 7, 22), LocalDate.of(2012, 6, 20),
      LocalDate.of(2012, 7, 19), LocalDate.of(2012, 8, 16), LocalDate.of(2012, 7, 3), LocalDate.of(2013, 5, 16), LocalDate.of(2013, 2, 25), LocalDate.of(2012, 8, 22), LocalDate.of(2013, 2, 1),
      LocalDate.of(2013, 5, 15), LocalDate.of(2013, 2, 13), LocalDate.of(2013, 4, 14), LocalDate.of(2013, 4, 2), LocalDate.of(2012, 8, 9), LocalDate.of(2012, 7, 22), LocalDate.of(2012, 5, 31),
      LocalDate.of(2013, 1, 14), LocalDate.of(2012, 11, 4), LocalDate.of(2012, 7, 6), LocalDate.of(2012, 10, 25), LocalDate.of(2012, 12, 17), LocalDate.of(2012, 11, 4), LocalDate.of(2012, 11, 1),
      LocalDate.of(2012, 11, 30), LocalDate.of(2012, 11, 23), LocalDate.of(2013, 1, 10), LocalDate.of(2012, 9, 11), LocalDate.of(2012, 6, 9), LocalDate.of(2012, 8, 30), LocalDate.of(2012, 12, 21),
      LocalDate.of(2013, 2, 26), LocalDate.of(2012, 8, 30), LocalDate.of(2012, 10, 2), LocalDate.of(2012, 10, 12), LocalDate.of(2012, 6, 10), LocalDate.of(2012, 6, 18), LocalDate.of(2012, 10, 8),
      LocalDate.of(2013, 5, 10), LocalDate.of(2013, 5, 23), LocalDate.of(2013, 3, 17), LocalDate.of(2012, 10, 27) };

    final LocalDate[] curveInstrumentsStartDate = new LocalDate[] {LocalDate.of(2012, 7, 29), LocalDate.of(2012, 1, 18), LocalDate.of(2011, 8, 13), LocalDate.of(2012, 6, 14),
      LocalDate.of(2013, 2, 10), LocalDate.of(2012, 4, 21), LocalDate.of(2012, 5, 5), LocalDate.of(2013, 3, 4), LocalDate.of(2012, 8, 28), LocalDate.of(2012, 4, 20), LocalDate.of(2012, 2, 1),
      LocalDate.of(2012, 6, 28), LocalDate.of(2012, 6, 5), LocalDate.of(2012, 4, 11), LocalDate.of(2012, 1, 9), LocalDate.of(2012, 7, 31), LocalDate.of(2012, 2, 1), LocalDate.of(2011, 8, 3),
      LocalDate.of(2011, 12, 18), LocalDate.of(2011, 9, 16), LocalDate.of(2012, 5, 8), LocalDate.of(2012, 6, 14), LocalDate.of(2012, 10, 7), LocalDate.of(2013, 1, 1), LocalDate.of(2012, 5, 1),
      LocalDate.of(2011, 8, 7), LocalDate.of(2011, 11, 10), LocalDate.of(2011, 9, 2), LocalDate.of(2011, 8, 26), LocalDate.of(2012, 6, 24), LocalDate.of(2012, 10, 19), LocalDate.of(2012, 11, 13),
      LocalDate.of(2011, 9, 29), LocalDate.of(2012, 12, 17), LocalDate.of(2012, 6, 13), LocalDate.of(2012, 9, 16), LocalDate.of(2012, 2, 21), LocalDate.of(2011, 12, 14), LocalDate.of(2011, 12, 31),
      LocalDate.of(2012, 6, 26), LocalDate.of(2012, 3, 8), LocalDate.of(2012, 3, 14), LocalDate.of(2012, 5, 7), LocalDate.of(2011, 11, 19), LocalDate.of(2011, 12, 27), LocalDate.of(2013, 2, 15),
      LocalDate.of(2011, 8, 19), LocalDate.of(2011, 12, 3), LocalDate.of(2011, 11, 11), LocalDate.of(2011, 11, 7), LocalDate.of(2012, 1, 20), LocalDate.of(2012, 8, 10), LocalDate.of(2012, 7, 1),
      LocalDate.of(2012, 9, 20), LocalDate.of(2012, 6, 18), LocalDate.of(2011, 10, 16), LocalDate.of(2012, 7, 6), LocalDate.of(2012, 9, 29), LocalDate.of(2012, 9, 8), LocalDate.of(2012, 2, 3),
      LocalDate.of(2011, 8, 13), LocalDate.of(2012, 4, 7), LocalDate.of(2012, 7, 3), LocalDate.of(2011, 8, 21), LocalDate.of(2013, 3, 12), LocalDate.of(2012, 9, 3), LocalDate.of(2011, 10, 23),
      LocalDate.of(2012, 3, 23), LocalDate.of(2012, 7, 4), LocalDate.of(2012, 12, 26), LocalDate.of(2012, 5, 28), LocalDate.of(2012, 10, 24), LocalDate.of(2011, 11, 8), LocalDate.of(2012, 1, 16),
      LocalDate.of(2011, 8, 28), LocalDate.of(2012, 9, 25), LocalDate.of(2012, 5, 6), LocalDate.of(2012, 3, 1), LocalDate.of(2012, 9, 20), LocalDate.of(2012, 9, 20), LocalDate.of(2012, 8, 27),
      LocalDate.of(2012, 10, 27), LocalDate.of(2012, 3, 3), LocalDate.of(2012, 11, 19), LocalDate.of(2012, 6, 11), LocalDate.of(2012, 7, 15), LocalDate.of(2012, 1, 24), LocalDate.of(2012, 1, 24),
      LocalDate.of(2012, 3, 21), LocalDate.of(2012, 6, 8), LocalDate.of(2012, 4, 7), LocalDate.of(2012, 8, 14), LocalDate.of(2012, 3, 2), LocalDate.of(2012, 4, 24), LocalDate.of(2012, 5, 17),
      LocalDate.of(2012, 9, 19), LocalDate.of(2013, 4, 11), LocalDate.of(2012, 11, 24), LocalDate.of(2012, 7, 10), LocalDate.of(2012, 4, 22) };

    final double[][] q = new double[][] { {0.996810615509364, 0.99256914801695, 0.981114953972081, 0.947234616931817, 0.898378314399555, 0.689993952066726, 0.0366572089666166 },
      {0.993746414813576, 0.988290197064791, 0.975045515235276, 0.938320716277356, 0.886840433108674, 0.669175224711777, 0.0188242490787626 },
      {0.99209153108799, 0.985979263146004, 0.971767594245475, 0.933506604843209, 0.880609219838575, 0.657933928941863, 0.0110022612206662 },
      {0.997803316721235, 0.993955388904857, 0.983081255685436, 0.95012244125823, 0.902116239223084, 0.696739672371817, 0.0431165357233294 },
      {0.998843248749633, 0.995407584555727, 0.985141110917374, 0.953147668357938, 0.906032021348897, 0.703806914511085, 0.050159852764171 },
      {0.992183396731176, 0.986107547337713, 0.971949557630114, 0.933773844636906, 0.880955124964931, 0.658557910693825, 0.0113935046733042 },
      {0.9937004085676, 0.988225952430774, 0.974954388060495, 0.938186882383622, 0.886667203066574, 0.668862691364456, 0.0185865498883494 },
      {0.997872611826308, 0.994052154890198, 0.983218512601285, 0.950324024859726, 0.902377164037371, 0.697210574731877, 0.0435774882732853 },
      {0.994413744788034, 0.989222078594331, 0.976367333629797, 0.940262005488561, 0.889353173444264, 0.673708724315407, 0.0223855617337246 },
      {0.998404034201266, 0.994794250750736, 0.98427113245126, 0.951869965154919, 0.904378191827133, 0.700821999032775, 0.0471530439851326 },
      {0.991333964007871, 0.984921371699888, 0.970267039237213, 0.931302822247595, 0.877756728811298, 0.652788480686015, 0.00799767802645814 },
      {0.997710930733155, 0.993826378044462, 0.982898261295652, 0.949853684938101, 0.901768367755253, 0.696111857479687, 0.042503938927208 },
      {0.999236393965294, 0.99595658562518, 0.985919837208777, 0.95429135379345, 0.907512382396159, 0.706478828396975, 0.0528886104346636 },
      {0.993033557297951, 0.987294739419055, 0.97363351816701, 0.936246988356684, 0.884156271773075, 0.664332724285906, 0.0152638866228995 },
      {0.993631403192157, 0.988129591056753, 0.974817705211048, 0.937986143182133, 0.886407373095089, 0.668393920814312, 0.0182319996176127 },
      {0.997249129086333, 0.993181502906159, 0.981983543545481, 0.948510277235025, 0.900029495195454, 0.692973724591921, 0.0394763243229297 },
      {0.995565366270288, 0.990830240802057, 0.978648416393746, 0.94361212351379, 0.893689461945762, 0.681532860442745, 0.0289813828559238 },
      {0.991930786678981, 0.985754794392357, 0.971449198874674, 0.933038994854976, 0.880003963214603, 0.656842112286063, 0.0103311795596041 },
      {0.993516404881653, 0.987969003637589, 0.974589921560936, 0.937651608881577, 0.885974363384028, 0.667612715155902, 0.0176464818926271 },
      {0.993194480413191, 0.987519457738426, 0.97395226762497, 0.93671511918981, 0.884762203754179, 0.665425861991048, 0.0160439927608838 },
      {0.991976710995173, 0.985818924610301, 0.971540163714448, 0.933172589962441, 0.880176883634192, 0.657154040237611, 0.010521130520247 },
      {0.997572367789465, 0.993632884151747, 0.982623801489248, 0.949450597200102, 0.90124662115731, 0.69517025283383, 0.0415894010648925 },
      {0.996256979228349, 0.991796031991266, 0.980018334276049, 0.945624059777567, 0.896293653785793, 0.686232053838553, 0.0331813292400002 },
      {0.999120746959038, 0.995795092293739, 0.985690768242335, 0.953954928918335, 0.907076921403032, 0.705692853729235, 0.0520823742345248 },
      {0.993447412283948, 0.987872660107854, 0.974453264029177, 0.937450906920834, 0.88571458170024, 0.667144038917877, 0.0172984578386433 },
      {0.991012746705505, 0.984472813367998, 0.969630786475138, 0.930368393619608, 0.876547241686893, 0.650606857541573, 0.00685149229674776 },
      {0.991173342344262, 0.984697074361908, 0.969948887073421, 0.930835570007958, 0.877151936071447, 0.65169757271293, 0.00741452875929987 },
      {0.991471660436389, 0.98511365549239, 0.970539782140155, 0.931703384547941, 0.878275200882868, 0.653723698103688, 0.00851313707063755 },
      {0.992872660256406, 0.987070057511957, 0.973314820371246, 0.935778933515804, 0.883550438327101, 0.663239779186411, 0.0144984046028552 },
      {0.996510691010363, 0.992150323517327, 0.98052087598721, 0.946362120533712, 0.897248978959881, 0.687955976577164, 0.0347622318950049 },
      {0.996441490480935, 0.992053689608129, 0.980383806462036, 0.946160812563696, 0.896988411519702, 0.687485768731714, 0.0343289912106049 },
      {0.998519598248896, 0.994955628229373, 0.984500037045047, 0.952206148258982, 0.904813339356099, 0.70160736583717, 0.0479397793718927 },
      {0.992826694461562, 0.9870058693685, 0.973223773345974, 0.935645217517317, 0.883377361152934, 0.66292754453674, 0.0142824392623333 },
      {0.997433824089514, 0.993439417133169, 0.982349379812069, 0.949047565545883, 0.900724947275178, 0.694228789424616, 0.0406801787421653 },
      {0.99845025821525, 0.994858799500692, 0.984362691108521, 0.952004433718786, 0.904552244773818, 0.701136133988165, 0.0474673434284499 },
      {0.99759546027697, 0.993665131267507, 0.982669542142311, 0.949517774594854, 0.901333573873659, 0.695327177133553, 0.0417414575698686 },
      {0.993470409284162, 0.987904773874325, 0.97449881548496, 0.937517806022865, 0.885801173583038, 0.667300260400696, 0.0174141894552897 },
      {0.991701196987572, 0.98543418783684, 0.970994437861553, 0.932371112261372, 0.879139481634125, 0.655282708518134, 0.00940335159561497 },
      {0.993102521154396, 0.987391042811224, 0.973770118751093, 0.936447606550712, 0.884415944841401, 0.664801188290377, 0.0155964508552471 },
      {0.993056544717966, 0.987326839806648, 0.973679050640676, 0.936313859537119, 0.884242827451537, 0.664488875023399, 0.0153744445196588 },
      {0.995173665404518, 0.990283256786349, 0.977872551914065, 0.942472647493212, 0.892214558674925, 0.678871552629207, 0.0266789309500379 },
      {0.993953469280551, 0.98857933473009, 0.975455639751399, 0.938923045625519, 0.887620067913603, 0.6705818192723, 0.0199068485556507 },
      {0.993401419880522, 0.987808434805147, 0.974362164281892, 0.937317113371242, 0.885541403969813, 0.666831607742029, 0.0170678292734334 },
      {0.99160937599096, 0.985305965994694, 0.97081256293935, 0.932104002592368, 0.878793745236654, 0.654659057147084, 0.00904266981418888 },
      {0.991379860692423, 0.984985463329862, 0.970357949327823, 0.931436336821064, 0.877929544804401, 0.653100204088356, 0.00816791401759945 },
      {0.998011216473937, 0.994245707023014, 0.98349305503916, 0.950727234139053, 0.902899068220048, 0.698152485368124, 0.0445032126384183 },
      {0.992275270880932, 0.986235843408966, 0.972131537869612, 0.934041109223361, 0.881301062239183, 0.659181955365535, 0.011790286096015 },
      {0.993539403478954, 0.988001119634414, 0.974635476181175, 0.937718512638327, 0.886060961302309, 0.667768948428193, 0.0177630405433067 },
      {0.991356912084538, 0.984953417143949, 0.970312493756243, 0.931369578760195, 0.877843135804045, 0.652944340420035, 0.00808259681020849 },
      {0.994574891565474, 0.989447109261025, 0.976686526249825, 0.940730788184241, 0.889959950650867, 0.674803512305031, 0.0232758133493548 },
      {0.992803712362154, 0.98697377641121, 0.973178251414518, 0.935578361843907, 0.883290825581643, 0.662771433108602, 0.0141749200226504 },
      {0.994459784059981, 0.989286369349569, 0.976458526241894, 0.940395935636093, 0.889526528291527, 0.67402150124387, 0.0226387513034908 },
      {0.99266583093713, 0.986781234268515, 0.972905141960745, 0.935177260362835, 0.882771654372125, 0.661834847096393, 0.0135363711910294 },
      {0.995173665404518, 0.990283256786349, 0.977872551914065, 0.942472647493212, 0.892214558674925, 0.678871552629207, 0.0266789309500379 },
      {0.995749749433058, 0.991087719539776, 0.979013634784352, 0.944148502990588, 0.894383735651481, 0.682785633218917, 0.0300847893179116 },
      {0.992045601456115, 0.985915125504635, 0.971676618873242, 0.933372994242935, 0.880436279329952, 0.657621961661772, 0.0108087440303113 },
      {0.993999487242706, 0.988643595725915, 0.975546790140502, 0.939056913665998, 0.887793342231412, 0.670894439060933, 0.0201502567165372 },
      {0.995058488599706, 0.990122420097454, 0.977644414597277, 0.942137592974468, 0.891780874438286, 0.678089031003418, 0.0260130807125308 },
      {0.996441490480935, 0.992053689608129, 0.980383806462036, 0.946160812563696, 0.896988411519702, 0.687485768731714, 0.0343289912106049 },
      {0.99232121114602, 0.986299995899764, 0.972222534310419, 0.934174750814582, 0.881474042932673, 0.659494001295312, 0.0119907260316164 },
      {0.991586422070221, 0.985273912389306, 0.970767096840912, 0.932037229046856, 0.878707316157676, 0.65450315413854, 0.00895345068819959 },
      {0.992252301545959, 0.986203768277385, 0.972086041229509, 0.933974290752301, 0.881214574906594, 0.65902593829905, 0.0116905763832623 },
      {0.992895643951867, 0.987102152698158, 0.973360345465109, 0.935845793840949, 0.883636979930068, 0.663395902407844, 0.0146068482358994 },
      {0.991884864488891, 0.985690667143544, 0.971358238247544, 0.932905405944139, 0.879831050829998, 0.656530200066639, 0.0101426750146768 },
      {0.999190133556558, 0.995891986049478, 0.985828206439641, 0.954156779162282, 0.907338191929712, 0.706164426766019, 0.0525657550289073 },
      {0.997341472312527, 0.993310454049015, 0.982166453207573, 0.94877890893025, 0.900377205079896, 0.693601225619879, 0.0400770329152726 },
      {0.993033557297951, 0.987294739419055, 0.97363351816701, 0.936246988356684, 0.884156271773075, 0.664332724285906, 0.0152638866228995 },
      {0.996787541189561, 0.992536926272628, 0.981069249316216, 0.947167492485045, 0.898291430333663, 0.689837161174731, 0.0365104426536449 },
      {0.999167004155319, 0.995859687383208, 0.985782392646377, 0.954089494187295, 0.907251099731133, 0.706007231832446, 0.0524045074529077 },
      {0.99706446828316, 0.992923636439912, 0.981617775042237, 0.947973088596662, 0.899334172347409, 0.691718910874389, 0.0382823430925391 },
      {0.99845025821525, 0.994858799500692, 0.984362691108521, 0.952004433718786, 0.904552244773818, 0.701136133988165, 0.0474673434284499 },
      {0.998172946229008, 0.994471551825756, 0.983813402762672, 0.951197715880036, 0.903508048378022, 0.699251559591606, 0.0455895579362812 },
      {0.992734769255839, 0.986877501996884, 0.973041691944571, 0.935377804126572, 0.883031230930395, 0.66230312241151, 0.0138542309451229 },
      {0.99232121114602, 0.986299995899764, 0.972222534310419, 0.934174750814582, 0.881474042932673, 0.659494001295312, 0.0119907260316164 },
      {0.991127455220633, 0.984632996083899, 0.969857995926777, 0.930702083300464, 0.876979156211717, 0.651385920132391, 0.00725158628597777 },
      {0.996372294756988, 0.991957062409848, 0.980246746458372, 0.945959518598879, 0.896727862238367, 0.687015596209667, 0.0338973058665759 },
      {0.994736064457078, 0.989672176396504, 0.977005770612162, 0.941199646988974, 0.890566826541675, 0.675898492758713, 0.0241773420261816 },
      {0.991953748571308, 0.985786859130179, 0.971494680767968, 0.933105791634112, 0.880090422420001, 0.656998074295347, 0.0104259749531909 },
      {0.99450582546345, 0.989350663081506, 0.976549723077365, 0.940529871995899, 0.889699891193735, 0.674334293884495, 0.022892878807351 },
      {0.995726699670341, 0.991055532089751, 0.978967978785541, 0.94408145011364, 0.894296944381751, 0.682629022880929, 0.0299461864973449 },
      {0.994736064457078, 0.989672176396504, 0.977005770612162, 0.941199646988974, 0.890566826541675, 0.675898492758713, 0.0241773420261816 },
      {0.994666987162751, 0.989575714586782, 0.976868945263402, 0.940998698180972, 0.890306724789404, 0.675429191855279, 0.0237896105603135 },
      {0.995334935328645, 0.990508459429059, 0.97819198853548, 0.942941789095683, 0.892821801243612, 0.679967247830445, 0.0276198853062397 },
      {0.995173665404518, 0.990283256786348, 0.977872551914065, 0.942472647493211, 0.892214558674923, 0.678871552629204, 0.0266789309500353 },
      {0.996280041266341, 0.991828236583875, 0.980064014596916, 0.945691148429991, 0.896380491441555, 0.686388754462718, 0.033324171924509 },
      {0.993493406816726, 0.987936888384231, 0.974544367995539, 0.937584706676434, 0.885887767477618, 0.667456485813383, 0.0175301978358158 },
      {0.991333964007871, 0.984921371699888, 0.970267039237213, 0.931302822247595, 0.877756728811298, 0.652788480686015, 0.00799767802645814 },
      {0.993217471558364, 0.987551563328254, 0.973997807479631, 0.936782001227274, 0.884848773510357, 0.66558204024235, 0.0161566066956788 },
      {0.995818901922689, 0.991184286360755, 0.979150609124149, 0.944349670951949, 0.894644121558546, 0.683255487788007, 0.0305017435957189 },
      {0.997364559455106, 0.993342693700515, 0.982212183270286, 0.948846070747782, 0.900464137599463, 0.693758110685821, 0.0402275920763386 },
      {0.993217471558364, 0.987551563328254, 0.973997807479631, 0.936782001227274, 0.884848773510357, 0.66558204024235, 0.0161566066956788 },
      {0.993976477995318, 0.988611464856096, 0.975501214418285, 0.938989978869598, 0.887706704066116, 0.670738127202158, 0.0200284256297426 },
      {0.994206594439206, 0.988932807028717, 0.975957019134369, 0.939659396693887, 0.888573176301768, 0.672301422584131, 0.0212580074461545 },
      {0.991356912084538, 0.984953417143949, 0.970312493756242, 0.931369578760195, 0.877843135804044, 0.652944340420034, 0.00808259681020791 },
      {0.991540515822752, 0.985209807404586, 0.970676167802393, 0.931903686601666, 0.878534464023874, 0.654191359922872, 0.0087761643792165 },
      {0.994114541469404, 0.988804261232809, 0.975774684582405, 0.939391610934048, 0.888226563251213, 0.67167605728706, 0.0207631883142279 },
      {0.99905136517992, 0.995698205266963, 0.985553339592118, 0.953753092716894, 0.906815669082847, 0.70522131598465, 0.0516000843236912 },
      {0.999352054357548, 0.996118097650341, 0.986148932697975, 0.954627817679993, 0.907947893968587, 0.707264901095387, 0.0536978349158753 },
      {0.997803316721235, 0.993955388904857, 0.983081255685436, 0.95012244125823, 0.902116239223084, 0.696739672371817, 0.0431165357233294 },
      {0.99455186899854, 0.98941495979028, 0.976640924136405, 0.940663814568261, 0.889873262151253, 0.67464710223696, 0.0231479372238698 } };

    final LocalDate[] cdsStartDate = new LocalDate[] {LocalDate.of(2012, 9, 12), LocalDate.of(2012, 8, 28), LocalDate.of(2012, 6, 21), LocalDate.of(2013, 1, 2), LocalDate.of(2013, 2, 10),
      LocalDate.of(2012, 5, 9), LocalDate.of(2012, 8, 18), LocalDate.of(2012, 11, 12), LocalDate.of(2012, 9, 2), LocalDate.of(2012, 11, 16), LocalDate.of(2012, 6, 23), LocalDate.of(2013, 2, 10),
      LocalDate.of(2013, 4, 10), LocalDate.of(2012, 4, 6), LocalDate.of(2012, 6, 5), LocalDate.of(2012, 9, 26), LocalDate.of(2012, 12, 4), LocalDate.of(2012, 3, 3), LocalDate.of(2012, 9, 2),
      LocalDate.of(2012, 8, 4), LocalDate.of(2012, 6, 12), LocalDate.of(2012, 11, 6), LocalDate.of(2012, 9, 22), LocalDate.of(2013, 3, 3), LocalDate.of(2012, 6, 24), LocalDate.of(2012, 6, 14),
      LocalDate.of(2012, 6, 10), LocalDate.of(2012, 2, 15), LocalDate.of(2012, 5, 31), LocalDate.of(2012, 11, 23), LocalDate.of(2012, 9, 4), LocalDate.of(2012, 11, 25), LocalDate.of(2012, 5, 22),
      LocalDate.of(2013, 2, 18), LocalDate.of(2013, 3, 10), LocalDate.of(2013, 3, 16), LocalDate.of(2012, 8, 31), LocalDate.of(2012, 2, 5), LocalDate.of(2012, 5, 7), LocalDate.of(2012, 6, 11),
      LocalDate.of(2012, 9, 4), LocalDate.of(2012, 5, 27), LocalDate.of(2012, 9, 12), LocalDate.of(2012, 3, 15), LocalDate.of(2012, 4, 13), LocalDate.of(2012, 11, 25), LocalDate.of(2012, 6, 24),
      LocalDate.of(2012, 6, 5), LocalDate.of(2012, 6, 19), LocalDate.of(2012, 7, 24), LocalDate.of(2012, 3, 19), LocalDate.of(2012, 7, 11), LocalDate.of(2012, 7, 10), LocalDate.of(2012, 10, 13),
      LocalDate.of(2012, 8, 25), LocalDate.of(2012, 4, 11), LocalDate.of(2012, 9, 15), LocalDate.of(2012, 10, 27), LocalDate.of(2013, 1, 16), LocalDate.of(2012, 7, 23), LocalDate.of(2012, 2, 27),
      LocalDate.of(2012, 7, 8), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 2, 5), LocalDate.of(2013, 4, 13), LocalDate.of(2013, 1, 20), LocalDate.of(2012, 6, 23), LocalDate.of(2012, 10, 13),
      LocalDate.of(2013, 1, 26), LocalDate.of(2012, 11, 20), LocalDate.of(2013, 2, 26), LocalDate.of(2013, 3, 10), LocalDate.of(2012, 7, 3), LocalDate.of(2012, 4, 8), LocalDate.of(2012, 1, 11),
      LocalDate.of(2012, 10, 27), LocalDate.of(2012, 10, 28), LocalDate.of(2012, 2, 25), LocalDate.of(2012, 6, 1), LocalDate.of(2012, 11, 28), LocalDate.of(2012, 10, 29), LocalDate.of(2012, 9, 19),
      LocalDate.of(2012, 11, 8), LocalDate.of(2012, 10, 27), LocalDate.of(2012, 10, 16), LocalDate.of(2012, 7, 31), LocalDate.of(2012, 4, 30), LocalDate.of(2012, 6, 28), LocalDate.of(2012, 8, 3),
      LocalDate.of(2013, 3, 27), LocalDate.of(2012, 5, 19), LocalDate.of(2012, 5, 22), LocalDate.of(2012, 6, 7), LocalDate.of(2012, 3, 17), LocalDate.of(2012, 4, 27), LocalDate.of(2012, 8, 24),
      LocalDate.of(2013, 6, 8), LocalDate.of(2013, 4, 6), LocalDate.of(2013, 3, 11), LocalDate.of(2012, 8, 19) };

    final LocalDate[] cdsEndDate = new LocalDate[] {LocalDate.of(2018, 7, 4), LocalDate.of(2022, 5, 21), LocalDate.of(2017, 5, 22), LocalDate.of(2014, 6, 8), LocalDate.of(2020, 10, 1),
      LocalDate.of(2021, 6, 11), LocalDate.of(2019, 10, 7), LocalDate.of(2019, 8, 7), LocalDate.of(2012, 10, 24), LocalDate.of(2016, 2, 3), LocalDate.of(2018, 6, 9), LocalDate.of(2014, 6, 3),
      LocalDate.of(2020, 9, 7), LocalDate.of(2019, 4, 6), LocalDate.of(2016, 10, 25), LocalDate.of(2020, 5, 2), LocalDate.of(2017, 3, 26), LocalDate.of(2018, 4, 13), LocalDate.of(2020, 11, 6),
      LocalDate.of(2019, 7, 2), LocalDate.of(2018, 2, 22), LocalDate.of(2013, 1, 19), LocalDate.of(2017, 10, 20), LocalDate.of(2019, 7, 27), LocalDate.of(2017, 1, 30), LocalDate.of(2015, 6, 27),
      LocalDate.of(2012, 10, 26), LocalDate.of(2020, 7, 1), LocalDate.of(2018, 5, 5), LocalDate.of(2020, 1, 21), LocalDate.of(2013, 6, 19), LocalDate.of(2021, 4, 30), LocalDate.of(2020, 8, 21),
      LocalDate.of(2017, 9, 7), LocalDate.of(2017, 4, 20), LocalDate.of(2016, 7, 30), LocalDate.of(2020, 12, 2), LocalDate.of(2019, 3, 17), LocalDate.of(2019, 11, 2), LocalDate.of(2015, 1, 26),
      LocalDate.of(2017, 5, 23), LocalDate.of(2017, 10, 14), LocalDate.of(2016, 1, 9), LocalDate.of(2017, 9, 9), LocalDate.of(2020, 8, 10), LocalDate.of(2022, 3, 13), LocalDate.of(2022, 1, 29),
      LocalDate.of(2017, 9, 25), LocalDate.of(2021, 12, 8), LocalDate.of(2016, 12, 8), LocalDate.of(2015, 12, 24), LocalDate.of(2016, 6, 24), LocalDate.of(2019, 7, 12), LocalDate.of(2020, 2, 12),
      LocalDate.of(2019, 10, 4), LocalDate.of(2017, 9, 5), LocalDate.of(2015, 9, 25), LocalDate.of(2016, 11, 27), LocalDate.of(2014, 9, 27), LocalDate.of(2019, 12, 20), LocalDate.of(2013, 10, 30),
      LocalDate.of(2021, 3, 15), LocalDate.of(2019, 10, 29), LocalDate.of(2019, 8, 23), LocalDate.of(2014, 11, 29), LocalDate.of(2013, 11, 12), LocalDate.of(2020, 4, 24), LocalDate.of(2015, 8, 2),
      LocalDate.of(2017, 12, 2), LocalDate.of(2022, 1, 5), LocalDate.of(2016, 6, 6), LocalDate.of(2021, 1, 9), LocalDate.of(2022, 3, 30), LocalDate.of(2021, 9, 25), LocalDate.of(2020, 5, 21),
      LocalDate.of(2017, 12, 30), LocalDate.of(2017, 3, 6), LocalDate.of(2016, 7, 10), LocalDate.of(2017, 4, 30), LocalDate.of(2021, 7, 4), LocalDate.of(2022, 2, 12), LocalDate.of(2018, 5, 8),
      LocalDate.of(2019, 3, 17), LocalDate.of(2017, 7, 29), LocalDate.of(2017, 10, 13), LocalDate.of(2019, 1, 17), LocalDate.of(2014, 1, 7), LocalDate.of(2017, 1, 9), LocalDate.of(2021, 10, 21),
      LocalDate.of(2015, 12, 23), LocalDate.of(2014, 12, 31), LocalDate.of(2014, 1, 19), LocalDate.of(2021, 11, 29), LocalDate.of(2017, 6, 30), LocalDate.of(2021, 12, 26), LocalDate.of(2021, 10, 17),
      LocalDate.of(2021, 11, 13), LocalDate.of(2017, 8, 28), LocalDate.of(2014, 12, 26), LocalDate.of(2016, 10, 22) };

    final double[] spreadBP = new double[] {853.4334114872, 486.104631338346, 547.185135615289, 20.2589142248781, 779.715384758487, 217.224286020558, 23.3973598592314, 230.020727698053,
      714.924642816986, 1.88636918418628, 190.368520096771, 42.0937744802438, 856.641597187312, 523.943003763926, 876.327109978633, 943.163919478434, 132.418196316325, 949.31143359757,
      168.442881066102, 133.260334162093, 291.826532957656, 232.968045008752, 172.032575205497, 275.439343733498, 368.350670913277, 234.836415073065, 127.674951587375, 911.776731027477,
      692.509930033304, 964.435221077889, 868.957950947645, 273.248008844612, 736.150401377194, 245.369600326114, 862.663801690618, 275.322242565119, 938.347950145965, 989.788048725982,
      30.1448477246492, 993.582563410252, 859.002081661297, 355.863576739135, 44.8376872976076, 348.983562443387, 588.167581404844, 26.5060195071317, 406.761436722698, 510.393823571833,
      995.169288947145, 820.260077967483, 531.299233592956, 796.650319566975, 810.086228360314, 98.5500513559251, 747.201910483556, 620.971845769242, 63.2836623952931, 401.330704185965,
      645.237736263407, 839.134349684959, 66.6387204289207, 651.081485393604, 955.477015165084, 215.943718994612, 529.774322164089, 832.577159674559, 990.868137367917, 413.975442625128,
      877.304488183553, 187.531088761532, 123.330342596009, 124.212085870368, 970.224903509538, 665.274594699967, 156.466191220056, 41.6620659156347, 436.865349679545, 232.678013308551,
      440.680217751777, 775.759981669268, 63.9202372233905, 810.770168450968, 259.37229244116, 811.074475148002, 647.350293893752, 595.666412657612, 396.303017317336, 690.486065285177,
      393.646252708885, 26.0690778765597, 395.187726875103, 622.888007547649, 352.965003198789, 497.772735042066, 937.443004073382, 273.572935657675, 374.922544020349, 419.343015867115,
      141.127907176057, 671.946838835976 };

    final double[] premLeg = new double[] {4128091.89186224, 2851204.4544253, 2486562.75404386, 24974.843923837, 4242496.67235433, 1278551.84681265, 131087.920708078, 1201946.91606566,
      5957.56744776915, 5192.63530380837, 1010029.47595597, 51782.3546830491, 4627373.0934737, 2893731.55500327, 3439502.79794793, 5157825.88588247, 536275.214668605, 4926957.45334703,
      973800.188253928, 743219.695753248, 1486847.69128116, 0, 758754.908872319, 1407480.09297923, 1532475.16908384, 701948.380265128, 49210.088176646, 5337158.82714618, 3554156.31784695,
      5266372.68693693, 368658.178482064, 1517465.02786027, 4267210.28997898, 1030340.92200028, 3272009.74973729, 898503.761262718, 5433527.93959136, 5559441.68607455, 170651.153965608,
      2393738.74912709, 3621542.02627871, 1651124.67072333, 145588.820961206, 1681058.82489612, 3451074.71935228, 150322.578059084, 2402742.66710022, 2371058.44305806, 5881344.89271973,
      3215519.34618681, 1746024.96055727, 2825862.51857924, 4556745.08375955, 548978.920148822, 4068098.42560203, 2957787.69160265, 185021.705486787, 1542042.85531367, 1091960.97409604,
      4815047.33209329, 91397.4176029599, 3821824.44652444, 5420862.28417965, 1232661.0296352, 814820.277046724, 598947.915133879, 5692889.53322257, 1020476.56907252, 3679479.86433224,
      1069452.82264639, 376997.364315388, 687002.688465676, 5720916.76332698, 3920757.31894601, 916952.961035144, 189138.166927533, 1789548.22839247, 899323.130151801, 1868067.68278979,
      4443096.84300359, 371709.38678292, 4018638.69126047, 1376505.28326205, 3532772.77424647, 2844451.83082439, 3222716.19684957, 629858.830640163, 2858330.2420715, 2262322.84133395,
      70042.4601512506, 917731.885652886, 814100.492050405, 2056661.49566407, 2340706.4229424, 5557011.54289956, 1593368.45471259, 2065891.63313401, 1667246.2127173, 250152.675542246, 2564509.7658855 };

    final double[] protectionLeg = new double[] {2508592.58471593, 5795728.92414002, 1546431.14671226, 145434.439424099, 4893657.71293914, 5712187.77009411, 4678288.96452547, 4056843.04845952,
      416.657021660605, 538713.539641292, 2776255.58667553, 143905.636711606, 4821950.73487845, 4189844.65643782, 1113427.46571978, 4765748.89643159, 1327809.16539737, 2268267.96400652,
      5409253.46292355, 4466338.71162181, 2015826.36157812, 0, 1648765.45620338, 3908105.68701182, 1299228.98202447, 504546.350496534, 19271.629855668, 5459918.96147796, 2385944.57429244,
      4624075.90489323, 21212.6592592885, 5208257.37488703, 5380783.95358663, 1536168.30921091, 1273156.63367088, 838919.389797266, 5443164.14423483, 4304956.68896559, 4818331.44304174,
      350104.976769913, 1440945.98347237, 1726407.94289344, 627304.47330763, 1755774.40612232, 5518785.71395403, 5532149.98851326, 5823465.18811683, 1710634.45703818, 5849780.91895354,
      1166972.59629447, 627307.377759155, 854050.357380709, 4568556.85015652, 4809549.89975457, 4435529.77284777, 1732716.99504314, 526646.768917781, 1132120.56648788, 226057.730648069,
      5028648.47055934, 107703.369264546, 5644511.92387374, 4835136.64005412, 4801448.6488364, 217924.999525015, 59794.4070802457, 5181346.87773492, 426975.993979611, 1603831.31579172,
      5539386.79710934, 718418.00060799, 5095266.41667093, 5823702.14089112, 5764155.77648506, 5445808.71304477, 1755792.10807515, 1320251.71847222, 951071.041033061, 1424377.02916586,
      5476990.83570606, 5698551.24320652, 2295452.0400592, 3838156.93257475, 1553871.25448054, 1636736.62851744, 3810781.94374628, 143579.468768425, 1267996.05788072, 5560155.58049757,
      524911.580233515, 331713.355640885, 121033.346688453, 5686163.65525327, 1641497.48480778, 5847992.35438665, 5665349.74083528, 5369787.63754756, 1449616.65046328, 254350.647500457,
      1080758.51125515 };

    final double[] defaultAcc = new double[] {46917.9915285462, 60514.2026230739, 18452.7370199111, 65.152593380466, 81437.7345848732, 26267.2087058573, 2354.77098351158, 20370.5375148686,
      7.1711436167534, 22.4141537467949, 11604.6913183645, 136.240146409691, 88446.1441948697, 47369.0323833541, 21673.1215397999, 97603.706799575, 3768.73565699672, 48919.7485222527,
      19718.3700654171, 12732.3461663429, 12777.342339724, 0, 6184.10846570495, 23470.1542603098, 10576.6310492492, 2547.34484842603, 44.6560359773648, 105813.418150894, 37825.867552218,
      96752.5292972527, 446.204029776331, 30758.3059020293, 85336.2385019856, 8148.04540129041, 24059.351463319, 5139.71646144334, 109660.269371366, 89608.4908447182, 3166.4164372785,
      7727.69783425936, 27002.8929505841, 13363.2508765307, 609.451058833045, 13227.8502884142, 70214.1100504438, 3111.69736088532, 51191.3474180405, 18722.5871556417, 123814.408690379,
      20846.1603613246, 7181.54428554885, 14699.5371189306, 80145.5002946205, 10298.4951475439, 71098.7329965135, 23290.3167237935, 718.179260768404, 9947.45940635813, 3133.84603629482,
      88439.8991264584, 169.583173511674, 77575.8193896876, 100704.419093274, 22460.5650033354, 2545.13221919315, 1190.9386984962, 111305.601888739, 3910.50401462091, 30535.845683607,
      22191.3912663869, 1960.53473820846, 13574.0548641476, 119783.092248275, 80723.6168967274, 18368.1915319003, 1574.42860226868, 12483.4125631584, 4879.88710127049, 13769.2934601217,
      90607.980607953, 7857.77077820338, 42345.116230044, 21026.621048069, 27623.7196124864, 23018.4828465112, 49799.0176085061, 1266.97303751315, 19174.3797261596, 47192.6996539323,
      293.037713131111, 2863.17126571061, 1723.26392257924, 43000.3582526533, 17616.2228559143, 115355.861682519, 33337.3041691901, 43358.8078988394, 13188.9655943232, 771.95566513158,
      16090.1277558121 };

    final double[] accruedPrem = new double[] {71119.4509572666, 35107.5567077696, 33439.0916209343, 562.747617357731, 67142.1581319808, 21722.4286020556, 2209.75065337214, 26835.7515647732,
      99295.089280137, 356.314179235254, 0, 1169.27151334011, 92802.8396952924, 69859.0671685233, 133883.308468957, 47158.195973922, 2574.79826170625, 221506.001172767, 5146.86581035237,
      9624.35746726254, 21076.3607136085, 0, 38229.4611567771, 11476.6393222287, 42974.2449398823, 0, 0, 189953.485630724, 19236.3869453691, 0, 72413.1625789705, 59203.735249666, 171768.426988012,
      8178.98667753715, 86266.3801690615, 0, 20852.1766699099, 21995.289971688, 2009.65651497617, 80038.5953858255, 2386.11689350334, 77103.7749601461, 0, 10663.3866302147, 53915.3616287771,
      1030.78964750003, 30507.1077542025, 114838.610303662, 0, 111646.51061224, 72315.7290168193, 66387.5266305814, 58506.2276038006, 3285.00171186402, 157742.625546529, 62097.1845769241,
      1582.09155988227, 25640.5727674367, 3584.65409035224, 0, 9625.5929508441, 21702.7161797867, 47773.8507582537, 25193.433882704, 50034.2415377194, 32378.0006540107, 82572.3447806592,
      105793.724226422, 177897.854548332, 19794.9482581624, 13703.3713995566, 8280.80572469066, 102412.628703784, 51743.5795877752, 4780.91139839123, 1735.91941315168, 9708.11888176773,
      56876.8476976457, 107721.83100599, 43097.7767594037, 1242.89350156672, 99094.1316995623, 16571.0075726294, 58577.6009829114, 156442.98769099, 71149.0437341034, 45134.5103055855,
      101654.892944762, 66701.170597895, 0, 65864.6211458505, 131498.57937117, 44120.6253998484, 100937.249050197, 138012.442266359, 34956.5417784806, 0, 55912.4021156152, 2744.15375064556,
      11199.1139805994 };

    final int[] accruedDays = new int[] {30, 26, 22, 10, 31, 36, 34, 42, 50, 68, 0, 10, 39, 48, 55, 18, 7, 84, 11, 26, 26, 0, 80, 15, 42, 0, 0, 75, 10, 0, 30, 78, 84, 12, 36, 0, 8, 8, 24, 29, 1, 78,
      0, 11, 33, 14, 27, 81, 0, 49, 49, 30, 26, 12, 76, 36, 9, 23, 2, 0, 52, 12, 18, 42, 34, 14, 30, 92, 73, 38, 40, 24, 38, 28, 11, 15, 8, 88, 88, 20, 7, 44, 23, 26, 87, 43, 41, 53, 61, 0, 60, 76,
      45, 73, 53, 46, 0, 48, 7, 6 };

    // check data hasn't been messed up
    ArgumentChecker.isTrue(nSets == today.length, "today");
    ArgumentChecker.isTrue(nSets == curveInstrumentsStartDate.length, "curveInstrumentsStartDate");
    ArgumentChecker.isTrue(nSets == q.length, "q");
    ArgumentChecker.isTrue(nCurvePoints == q[0].length, "q");
    ArgumentChecker.isTrue(nSets == cdsStartDate.length, "cdsStartDate");
    ArgumentChecker.isTrue(nSets == cdsEndDate.length, "cdsEndDate");
    ArgumentChecker.isTrue(nSets == spreadBP.length, "spreadBP");
    ArgumentChecker.isTrue(nSets == premLeg.length, "premLeg");
    ArgumentChecker.isTrue(nSets == protectionLeg.length, "protectionLeg");
    ArgumentChecker.isTrue(nSets == defaultAcc.length, "defaultAcc");
    ArgumentChecker.isTrue(nSets == accruedPrem.length, "accruedPrem");
    ArgumentChecker.isTrue(nSets == accruedDays.length, "accruedDays");

    // since current OG ISDA Java code used ZoneDateTime, 'upscale' LocalTime
    final ZonedDateTime[] curveTenors = new ZonedDateTime[nCurvePoints];
    for (int j = 0; j < nCurvePoints; j++) {
      curveTenors[j] = ZonedDateTime.of(parSpreadDates[j], LOCAL_TIME, TIME_ZONE);
    }

    final ISDA_Results[] res = new ISDA_Results[nSets];
    for (int i = 0; i < nSets; i++) {
      final ISDA_Results temp = new ISDA_Results();
      temp.notional = notional;
      temp.recoveryRate = recoveryRate;
      temp.today = today[i];

      // build the credit curve
      final double[] surProb = q[i];
      final double[] t = new double[nCurvePoints];
      final double[] r = new double[nCurvePoints];
      for (int j = 0; j < nCurvePoints; j++) {
        t[j] = ACT365.getDayCountFraction(temp.today, parSpreadDates[j]);
        r[j] = -Math.log(surProb[j]) / t[j];

      }
      temp.creditCurve = new ISDACompliantDateCreditCurve(temp.today, parSpreadDates, r, ACT365);

      // cds inputs
      temp.startDate = cdsStartDate[i];
      temp.endDate = cdsEndDate[i];
      temp.fracSpread = spreadBP[i] / 10000;

      // cds outputs
      temp.premiumLeg = premLeg[i];
      temp.protectionLeg = protectionLeg[i];
      temp.defaultAcc = defaultAcc[i];
      temp.accruedPremium = accruedPrem[i];
      temp.accruedDays = accruedDays[i];

      res[i] = temp;
    }

    return res;
  }

}
