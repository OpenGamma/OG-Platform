/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static com.opengamma.analytics.financial.credit.isdastandardmodel.DoublesScheduleGenerator.getIntegrationsPoints;
import static com.opengamma.analytics.financial.credit.isdastandardmodel.DoublesScheduleGenerator.truncateSetInclusive;
import static com.opengamma.analytics.math.utilities.Epsilon.epsilon;
import static com.opengamma.analytics.math.utilities.Epsilon.epsilonP;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.BrentSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.RealSingleRootFinder;
import com.opengamma.util.ArgumentChecker;

/**
 * This is a fast bootstrapper for the credit curve that is consistent with ISDA in that it will produce the same curve from
 * the same inputs (up to numerical round-off)
 */

public class FastCreditCurveBuilder extends ISDACompliantCreditCurveBuilder {
  private static final double HALFDAY = 1 / 730.;
  private static final BracketRoot BRACKER = new BracketRoot();
  private static final RealSingleRootFinder ROOTFINDER = new BrentSingleRootFinder();

  private final double _omega;

  /**
   * For consistency with the ISDA model version 1.8.2 and lower, a bug in the accrual on default calculation
   * has been reproduced.
   */
  public FastCreditCurveBuilder() {
    super();
    _omega = HALFDAY;
  }

  /**
   * For consistency with the ISDA model version 1.8.2 and lower, a bug in the accrual on default calculation
   * has been reproduced.
   * @param formula The accrual on default formulae.
   */
  public FastCreditCurveBuilder(final AccrualOnDefaultFormulae formula) {
    super(formula);
    if (formula == AccrualOnDefaultFormulae.OrignalISDA) {
      _omega = HALFDAY;
    } else {
      _omega = 0.0;
    }
  }

  /**
   * For consistency with the ISDA model version 1.8.2 and lower, a bug in the accrual on default calculation
   * has been reproduced.
  * @param formula The accrual on default formulae.
   * @param arbHandling How should any arbitrage in the input date be handled
   */
  public FastCreditCurveBuilder(final AccrualOnDefaultFormulae formula, final ArbitrageHandling arbHandling) {
    super(formula, arbHandling);
    if (formula == AccrualOnDefaultFormulae.OrignalISDA) {
      _omega = HALFDAY;
    } else {
      _omega = 0.0;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic[] cds, final double[] premiums, final ISDACompliantYieldCurve yieldCurve, final double[] pointsUpfront) {
    ArgumentChecker.noNulls(cds, "null CDSs");
    ArgumentChecker.notEmpty(premiums, "empty fractionalSpreads");
    ArgumentChecker.notEmpty(pointsUpfront, "empty pointsUpfront");
    ArgumentChecker.notNull(yieldCurve, "null yieldCurve");
    final int n = cds.length;
    ArgumentChecker.isTrue(n == premiums.length, "Number of CDSs does not match number of spreads");
    ArgumentChecker.isTrue(n == pointsUpfront.length, "Number of CDSs does not match number of pointsUpfront");
    final double proStart = cds[0].getEffectiveProtectionStart();
    for (int i = 1; i < n; i++) {
      ArgumentChecker.isTrue(proStart == cds[i].getEffectiveProtectionStart(), "all CDSs must has same protection start");
      ArgumentChecker.isTrue(cds[i].getProtectionEnd() > cds[i - 1].getProtectionEnd(), "protection end must be ascending");
    }

    // use continuous premiums as initial guess
    final double[] guess = new double[n];
    final double[] t = new double[n];
    for (int i = 0; i < n; i++) {
      t[i] = cds[i].getProtectionEnd();
      guess[i] = (premiums[i] + pointsUpfront[i] / t[i]) / cds[i].getLGD();
    }

    ISDACompliantCreditCurve creditCurve = new ISDACompliantCreditCurve(t, guess);
    for (int i = 0; i < n; i++) {
      final Pricer pricer = new Pricer(cds[i], yieldCurve, t, premiums[i], pointsUpfront[i]);
      final Function1D<Double, Double> func = pricer.getPointFunction(i, creditCurve);

      switch (getArbHanding()) {
        case Ignore: {
          try {
            double[] bracket = BRACKER.getBracketedPoints(func, 0.8 * guess[i], 1.25 * guess[i], Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            double zeroRate = bracket[0] > bracket[1] ? ROOTFINDER.getRoot(func, bracket[1], bracket[0]) : ROOTFINDER.getRoot(func, bracket[0], bracket[1]); //Negative guess handled
            creditCurve = creditCurve.withRate(zeroRate, i);
          } catch (final MathException e) { //handling bracketing failure due to small survival probability
            if (Math.abs(func.evaluate(creditCurve.getZeroRateAtIndex(i - 1))) < 1.e-12) {
              creditCurve = creditCurve.withRate(creditCurve.getZeroRateAtIndex(i - 1), i);
            } else {
              throw new MathException(e);
            }
          }
          break;
        }
        case Fail: {
          final double minValue = i == 0 ? 0.0 : creditCurve.getRTAtIndex(i - 1) / creditCurve.getTimeAtIndex(i);
          if (i > 0 && func.evaluate(minValue) > 0.0) { //can never fail on the first spread
            final StringBuilder msg = new StringBuilder();
            if (pointsUpfront[i] == 0.0) {
              msg.append("The par spread of " + premiums[i] + " at index " + i);
            } else {
              msg.append("The premium of " + premiums[i] + "and points up-front of " + pointsUpfront[i] + " at index " + i);
            }
            msg.append(" is an arbitrage; cannot fit a curve with positive forward hazard rate. ");
            throw new IllegalArgumentException(msg.toString());
          }
          guess[i] = Math.max(minValue, guess[i]);
          final double[] bracket = BRACKER.getBracketedPoints(func, guess[i], 1.2 * guess[i], minValue, Double.POSITIVE_INFINITY);
          final double zeroRate = ROOTFINDER.getRoot(func, bracket[0], bracket[1]);
          creditCurve = creditCurve.withRate(zeroRate, i);
          break;
        }
        case ZeroHazardRate: {
          final double minValue = i == 0 ? 0.0 : creditCurve.getRTAtIndex(i - 1) / creditCurve.getTimeAtIndex(i);
          if (i > 0 && func.evaluate(minValue) > 0.0) { //can never fail on the first spread
            creditCurve = creditCurve.withRate(minValue, i);
          } else {
            guess[i] = Math.max(minValue, guess[i]);
            final double[] bracket = BRACKER.getBracketedPoints(func, guess[i], 1.2 * guess[i], minValue, Double.POSITIVE_INFINITY);
            final double zeroRate = ROOTFINDER.getRoot(func, bracket[0], bracket[1]);
            creditCurve = creditCurve.withRate(zeroRate, i);
          }
          break;
        }
        default:
          throw new IllegalArgumentException("unknow case " + getArbHanding());
      }

    }
    return creditCurve;
  }

  /**
   * Prices the CDS
   */
  protected class Pricer {

    private final CDSAnalytic _cds;
    private final double _lgdDF;
    private final double _valuationDF;
    private final double _fracSpread;
    private final double _pointsUpfront;
    private final double[] _ccKnotTimes;

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
    private final double[][] _rt;
    private final double[][] _premDt;
    private final double[] _accRate;
    private final double[] _offsetAccStart;

    public Pricer(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final double[] creditCurveKnots, final double fractionalSpread, final double pointsUpfront) {

      _cds = cds;
      _fracSpread = fractionalSpread;
      _pointsUpfront = pointsUpfront;
      _ccKnotTimes = creditCurveKnots;

      // protection leg
      _proLegIntPoints = getIntegrationsPoints(cds.getEffectiveProtectionStart(), cds.getProtectionEnd(), yieldCurve.getKnotTimes(), creditCurveKnots);
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
        final double tmp = cds.getNumPayments() == 1 ? cds.getEffectiveProtectionStart() : cds.getStart();
        final double[] integrationSchedule = getIntegrationsPoints(tmp, cds.getProtectionEnd(), yieldCurve.getKnotTimes(), creditCurveKnots);

        _accRate = new double[_nPayments];
        _offsetAccStart = new double[_nPayments];
        _premLegIntPoints = new double[_nPayments][];
        _premDF = new double[_nPayments][];
        _rt = new double[_nPayments][];
        _premDt = new double[_nPayments][];
        for (int i = 0; i < _nPayments; i++) {
          _offsetAccStart[i] = cds.getEffectiveAccStart(i);
          final double offsetAccEnd = cds.getEffectiveAccEnd(i);
          _accRate[i] = cds.getAccRatio(i);
          final double start = Math.max(_offsetAccStart[i], cds.getEffectiveProtectionStart());
          if (start >= offsetAccEnd) {
            continue;
          }
          _premLegIntPoints[i] = truncateSetInclusive(start, offsetAccEnd, integrationSchedule);
          final int n = _premLegIntPoints[i].length;
          _rt[i] = new double[n];
          _premDF[i] = new double[n];
          for (int k = 0; k < n; k++) {
            _rt[i][k] = yieldCurve.getRT(_premLegIntPoints[i][k]);
            _premDF[i][k] = Math.exp(-_rt[i][k]);
          }
          _premDt[i] = new double[n - 1];

          for (int k = 1; k < n; k++) {
            final double dt = _premLegIntPoints[i][k] - _premLegIntPoints[i][k - 1];
            _premDt[i][k - 1] = dt;
          }

        }
      } else {
        _accRate = null;
        _offsetAccStart = null;
        _premDF = null;
        _premDt = null;
        _rt = null;
        _premLegIntPoints = null;
      }

    }

    public Function1D<Double, Double> getPointFunction(final int index, final double[] zeroHazardRates) {
      final ISDACompliantCreditCurve creditCurve = new ISDACompliantCreditCurve(_ccKnotTimes, zeroHazardRates);
      return getPointFunction(index, creditCurve);
    }

    public Function1D<Double, Double> getPointFunction(final int index, final ISDACompliantCreditCurve creditCurve) {
      return new Function1D<Double, Double>() {
        @Override
        public Double evaluate(final Double x) {
          final ISDACompliantCreditCurve cc = creditCurve.withRate(x, index);
          final double rpv01 = rpv01(cc, PriceType.CLEAN);
          final double pro = protectionLeg(cc);
          return pro - _fracSpread * rpv01 - _pointsUpfront;
        }
      };

    }

    public double rpv01(final double[] zeroHazardRates, final PriceType cleanOrDirty) {
      final ISDACompliantCreditCurve creditCurve = new ISDACompliantCreditCurve(_ccKnotTimes, zeroHazardRates);
      return rpv01(creditCurve, cleanOrDirty);
    }

    public double rpv01(final ISDACompliantCreditCurve creditCurve, final PriceType cleanOrDirty) {

      //   final double obsOffset = _cds.isProtectionFromStartOfDay() ? -_cds.getCurveOneDay() : 0.0;
      double pv = 0.0;
      for (int i = 0; i < _nPayments; i++) {
        final double q = creditCurve.getDiscountFactor(_cds.getEffectiveAccEnd(i));
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
        pv -= _cds.getAccruedPremiumPerUnitSpread();
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
      final double[] rt = _rt[paymentIndex];
      final double accRate = _accRate[paymentIndex];
      final double accStart = _offsetAccStart[paymentIndex];

      double t = knots[0];
      double ht0 = creditCurve.getRT(t);
      double rt0 = rt[0];
      double b0 = df[0] * Math.exp(-ht0);

      double t0 = t - accStart + _omega;
      double pv = 0.0;
      final int nItems = knots.length;
      for (int j = 1; j < nItems; ++j) {
        t = knots[j];
        final double ht1 = creditCurve.getRT(t);
        final double rt1 = rt[j];
        final double b1 = df[j] * Math.exp(-ht1);
        final double dt = deltaT[j - 1];

        final double dht = ht1 - ht0;
        final double drt = rt1 - rt0;
        final double dhrt = dht + drt + 1e-50; // to keep consistent with ISDA c code

        double tPV;
        if (getAccOnDefaultFormula() == AccrualOnDefaultFormulae.MarkitFix) {
          if (Math.abs(dhrt) < 1e-5) {
            tPV = dht * dt * b0 * epsilonP(-dhrt);
          } else {
            tPV = dht * dt / dhrt * ((b0 - b1) / dhrt - b1);
          }
        } else {
          final double t1 = t - accStart + _omega;
          if (Math.abs(dhrt) < 1e-5) {
            tPV = dht * b0 * (t0 * epsilon(-dhrt) + dt * epsilonP(-dhrt));
          } else {
            tPV = dht / dhrt * (t0 * b0 - t1 * b1 + dt / dhrt * (b0 - b1));
          }
          t0 = t1;
        }
        pv += tPV;
        ht0 = ht1;
        rt0 = rt1;
        b0 = b1;

      }
      return accRate * pv;
    }

    public double protectionLeg(final double[] zeroHazardRates) {
      final ISDACompliantCreditCurve creditCurve = new ISDACompliantCreditCurve(_ccKnotTimes, zeroHazardRates);
      return protectionLeg(creditCurve);
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
