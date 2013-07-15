/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.Epsilon.epsilon;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.Epsilon.epsilonP;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.Epsilon.epsilonPP;

import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.DoublesScheduleGenerator.getIntegrationsPoints;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.DoublesScheduleGenerator.truncateSetInclusive;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.BrentSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.RealSingleRootFinder;
import com.opengamma.util.ArgumentChecker;

/**
 * This is a fast bootstrapper for the credit curve that is consistent with ISDA in that it will produce the same curve from
 * the same inputs (up to numerical round-off) 
 */

public class FastCreditCurveBuilder implements ISDACompliantCreditCurveBuilder {

  private static final BracketRoot BRACKER = new BracketRoot();
  private static final RealSingleRootFinder ROOTFINDER = new BrentSingleRootFinder();

  /**
   * {@inheritDoc}
   */
  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic[] cds, final double[] fractionalSpreads, final ISDACompliantYieldCurve yieldCurve) {
    ArgumentChecker.noNulls(cds, "null CDSs");
    ArgumentChecker.notEmpty(fractionalSpreads, "empty fractionalSpreads");
    ArgumentChecker.notNull(yieldCurve, "null yieldCurve");
    final int n = cds.length;
    ArgumentChecker.isTrue(n == fractionalSpreads.length, "Number of CDSs does not match number of spreads");
    double proStart = cds[0].getProtectionStart();
    for (int i = 1; i < n; i++) {
      ArgumentChecker.isTrue(proStart == cds[i].getProtectionStart(), "all CDSs must has same protection start");
      ArgumentChecker.isTrue(cds[i].getProtectionEnd() > cds[i - 1].getProtectionEnd(), "protection end must be ascending");
    }

    // use continuous premiums as initial guess
    double[] guess = new double[n];
    double[] t = new double[n];
    for (int i = 0; i < n; i++) {
      guess[i] = fractionalSpreads[i] / cds[i].getLGD();
      t[i] = cds[i].getProtectionEnd();
    }

    ISDACompliantCreditCurve creditCurve = new ISDACompliantCreditCurve(t, guess);
    for (int i = 0; i < n; i++) {
      final Pricer pricer = new Pricer(cds[i], yieldCurve, creditCurve, fractionalSpreads[i]);
      final Function1D<Double, Double> func = pricer.getPointFunction(i, creditCurve);
      double[] bracket = BRACKER.getBracketedPoints(func, 0.8 * guess[i], 1.25 * guess[i], 0.0, Double.POSITIVE_INFINITY);
      double zeroRate = ROOTFINDER.getRoot(func, bracket[0], bracket[1]);
      creditCurve = creditCurve.withRate(zeroRate, i);
    }
    return creditCurve;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final LocalDate today, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate startDate, final LocalDate[] endDates,
      final double[] couponRates, final boolean payAccOnDefault, final Period tenor, StubType stubType, final boolean protectStart, final ISDACompliantDateYieldCurve yieldCurve,
      final double recoveryRate) {

    ArgumentChecker.notNull(today, "null today");
    ArgumentChecker.notNull(stepinDate, "null stepinDate");
    ArgumentChecker.notNull(valueDate, "null valueDate");
    ArgumentChecker.notNull(startDate, "null startDate");
    ArgumentChecker.noNulls(endDates, "null endDates");
    ArgumentChecker.notEmpty(couponRates, "no or null couponRates");
    ArgumentChecker.notNull(tenor, "null tenor");
    ArgumentChecker.notNull(stubType, "null stubType");
    ArgumentChecker.notNull(yieldCurve, "null yieldCurve");
    ArgumentChecker.isInRangeExcludingHigh(0, 1.0, recoveryRate);
    ArgumentChecker.isFalse(valueDate.isBefore(today), "Require valueDate >= today");
    ArgumentChecker.isFalse(stepinDate.isBefore(today), "Require stepin >= today");

    final int n = endDates.length;
    ArgumentChecker.isTrue(n == couponRates.length, "length of couponRates does not match endDates");

    final CDSAnalytic[] cds = new CDSAnalytic[n];
    for (int i = 0; i < n; i++) {
      cds[i] = new CDSAnalytic(today, stepinDate, valueDate, startDate, endDates[i], payAccOnDefault, tenor, stubType, protectStart, recoveryRate);
    }

    return calibrateCreditCurve(cds, couponRates, yieldCurve);
  }

  protected class Pricer {

    private final CDSAnalytic _cds;
    private final double _lgdDF;
    private final double _valuationDF;
    private final double _fracSpread;

    // protection leg
    private final int _nProPoints;
    private final double[] _proLegIntPoints;
    private final double[] _proYieldCurveRT;
    private final double[] _proDF;

    // premium leg
    private final int _nPayments;
    private final double[] _paymentDF;
    private final double[][] _premLegIntPoints;
    private final double[][] _premDF;
    private final double[][] _premFR;
    private final double[][] _premDt;
    private final double[] _accRate;
    private final double[] _offsetAccStart;

    public Pricer(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final double fractionalSpread) {

      _cds = cds;
      _fracSpread = fractionalSpread;

      // protection leg
      _proLegIntPoints = getIntegrationsPoints(cds.getProtectionStart(), cds.getProtectionEnd(), yieldCurve, creditCurve);
      _nProPoints = _proLegIntPoints.length;
      final double lgd = cds.getLGD();
      _valuationDF = yieldCurve.getDiscountFactor(cds.getValuationTime());
      _lgdDF = lgd / _valuationDF;
      _proYieldCurveRT = new double[_nProPoints];
      _proDF = new double[_nProPoints];
      for (int i = 0; i < _nProPoints; i++) {
        _proYieldCurveRT[i] = yieldCurve.getRT(_proLegIntPoints[i]);
        _proDF[i] = Math.exp(-_proYieldCurveRT[i]);
      }

      // premium leg
      _nPayments = cds.getNumPayments();
      _paymentDF = new double[_nPayments];
      for (int i = 0; i < _nPayments; i++) {
        _paymentDF[i] = yieldCurve.getDiscountFactor(cds.getPaymentTime(i));
      }

      if (cds.isPayAccOnDefault()) {
        final double offset = cds.isProtectionFromStartOfDay() ? -cds.getCurveOneDay() : 0.0;
        final double[] integrationSchedule = getIntegrationsPoints(cds.getAccStart(0), cds.getAccEnd(_nPayments - 1), yieldCurve, creditCurve);
        final double offsetStepin = cds.getStepin() + offset;

        _accRate = new double[_nPayments];
        _offsetAccStart = new double[_nPayments];
        _premLegIntPoints = new double[_nPayments][];
        _premDF = new double[_nPayments][];
        _premFR = new double[_nPayments][];
        _premDt = new double[_nPayments][];
        for (int i = 0; i < _nPayments; i++) {
          double offsetAccStart = cds.getAccStart(i) + offset;
          _offsetAccStart[i] = offsetAccStart;
          double offsetAccEnd = cds.getAccEnd(i) + offset;
          _accRate[i] = cds.getAccrualFraction(i) / (offsetAccEnd - offsetAccStart);
          double start = Math.max(offsetAccStart, offsetStepin);
          if (start >= offsetAccEnd) {
            continue;
          }
          _premLegIntPoints[i] = truncateSetInclusive(start, offsetAccEnd, integrationSchedule);
          final int n = _premLegIntPoints[i].length;
          _premDF[i] = new double[n];
          for (int k = 0; k < n; k++) {
            _premDF[i][k] = yieldCurve.getDiscountFactor(_premLegIntPoints[i][k]);
          }
          _premDt[i] = new double[n - 1];
          _premFR[i] = new double[n - 1];
          for (int k = 1; k < n; k++) {
            final double dt = _premLegIntPoints[i][k] - _premLegIntPoints[i][k - 1];
            _premDt[i][k - 1] = dt;
            _premFR[i][k - 1] = Math.log(_premDF[i][k - 1] / _premDF[i][k]) / dt;
          }

        }
      } else {
        _accRate = null;
        _offsetAccStart = null;
        _premDF = null;
        _premDt = null;
        _premFR = null;
        _premLegIntPoints = null;
      }

    }

    public Function1D<Double, Double> getPointFunction(final int index, final ISDACompliantCreditCurve creditCurve) {
      return new Function1D<Double, Double>() {

        @Override
        public Double evaluate(Double x) {
          ISDACompliantCreditCurve cc = creditCurve.withRate(x, index);
          final double rpv01 = rpv01(cc, PriceType.CLEAN);
          final double pro = protectionLeg(cc);
          return pro - _fracSpread * rpv01;
        }
      };

    }

    public double rpv01(final ISDACompliantCreditCurve creditCurve, final PriceType cleanOrDirty) {

      double pv = 0.0;
      for (int i = 0; i < _nPayments; i++) {
        final double q = creditCurve.getDiscountFactor(_cds.getCreditObservationTime(i));
        pv += _cds.getAccrualFraction(i) * _paymentDF[i] * q;
      }

      if (_cds.isPayAccOnDefault()) {
        double accPV = 0.0;
        for (int i = 0; i < _nPayments; i++) {
          accPV += calculateSinglePeriodAccrualOnDefault(i, creditCurve);
        }
        pv += accPV;
      }

      pv /= _valuationDF;

      if (cleanOrDirty == PriceType.CLEAN) {
        pv -= _cds.getAccrued();
      }
      return pv;
    }

    private double calculateSinglePeriodAccrualOnDefault(final int paymentIndex, final ISDACompliantCreditCurve creditCurve) {

      final double[] knots = _premLegIntPoints[paymentIndex];
      if (knots == null) {
        return 0.0;
      }
      final double[] df = _premDF[paymentIndex];
      final double[] deltaT = _premDt[paymentIndex];
      final double[] fwdRates = _premFR[paymentIndex];
      final double accRate = _accRate[paymentIndex];
      final double accStart = _offsetAccStart[paymentIndex];

      double t = knots[0];
      double s0 = creditCurve.getDiscountFactor(t);
      double df0 = df[0];
      double t0 = t - accStart + 1 / 730.0; // TODO not entirely clear why ISDA adds half a day
      double pv = 0.0;
      final int nItems = knots.length;
      for (int j = 1; j < nItems; ++j) {
        t = knots[j];
        final double s1 = creditCurve.getDiscountFactor(t);
        final double df1 = df[j];
        final double t1 = t - accStart + 1 / 730.0;
        final double dt = deltaT[j - 1];

        // TODO this is a know bug that is fixed in ISDA v.1.8.2
        final double lambda = Math.log(s0 / s1) / dt;
        final double fwdRate = fwdRates[j - 1];
        final double lambdafwdRate = lambda + fwdRate + 1.0e-50;
        final double tPV = lambda * accRate * s0 * df0 * ((t0 + 1.0 / (lambdafwdRate)) / (lambdafwdRate) - (t1 + 1.0 / (lambdafwdRate)) / (lambdafwdRate) * s1 / s0 * df1 / df0);
        pv += tPV;
        s0 = s1;
        df0 = df1;
        t0 = t1;
      }
      return pv;
    }

    public double protectionLeg(final ISDACompliantCreditCurve creditCurve) {

      double ht0 = creditCurve.getRT(_proLegIntPoints[0]);
      double rt0 = _proYieldCurveRT[0];
      double b0 = _proDF[0] * Math.exp(-ht0);

      double pv = 0.0;

      for (int i = 1; i < _nProPoints; ++i) {
        final double ht1 = creditCurve.getRT(_proLegIntPoints[i]);
        final double rt1 = _proYieldCurveRT[i];
        final double b1 = _proDF[i] * Math.exp(-ht1);
        final double dht = ht1 - ht0;
        final double drt = rt1 - rt0;
        final double dhrt = dht + drt;

        // this is equivalent to the ISDA code without explicitly calculating the time step - it also handles the limit
        double dPV;
        if (Math.abs(dhrt) < 1e-5) {
          dPV = dht * b0 * epsilon(-dhrt);
        } else {
          dPV = (b0 - b1) * dht / dhrt;
        }
        pv += dPV;
        ht0 = ht1;
        rt0 = rt1;
        b0 = b1;
      }
      pv *= _lgdDF; // multiply by LGD and adjust to valuation date

      return pv;
    }

  }

}
