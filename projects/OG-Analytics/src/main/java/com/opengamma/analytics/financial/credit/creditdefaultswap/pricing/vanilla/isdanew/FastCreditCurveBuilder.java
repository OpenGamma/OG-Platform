/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.DoublesScheduleGenerator.getIntegrationsPoints;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.DoublesScheduleGenerator.truncateSetInclusive;
import static com.opengamma.analytics.math.utilities.Epsilon.epsilon;
import static com.opengamma.analytics.math.utilities.Epsilon.epsilonP;

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

  private static final AnalyticCDSPricer PRICER = new AnalyticCDSPricer();

  private static final ArbitrageHandling DEFAULT_ARBITRAGE_HANDLING = ArbitrageHandling.Ignore;
  private static final boolean DEFAULT_USE_CORRECT_ACC_ON_DEFAULT_FORMULA = false;

  private static final BracketRoot BRACKER = new BracketRoot();
  private static final RealSingleRootFinder ROOTFINDER = new BrentSingleRootFinder();

  private final ArbitrageHandling _arbHandling;
  private final boolean _useCorrectAccOnDefaultFormula;

  /**
   * For consistency with the ISDA model version 1.8.2 and lower, a bug in the accrual on default calculation
   * has been reproduced.
   */
  public FastCreditCurveBuilder() {
    _arbHandling = DEFAULT_ARBITRAGE_HANDLING;
    _useCorrectAccOnDefaultFormula = DEFAULT_USE_CORRECT_ACC_ON_DEFAULT_FORMULA;
  }

  /**
   * For consistency with the ISDA model version 1.8.2 and lower, a bug in the accrual on default calculation
   * has been reproduced.
   * @param useCorrectAccOnDefaultFormula Set to true to use correct accrual on default formulae.
   */
  public FastCreditCurveBuilder(final boolean useCorrectAccOnDefaultFormula) {
    _arbHandling = DEFAULT_ARBITRAGE_HANDLING;
    _useCorrectAccOnDefaultFormula = useCorrectAccOnDefaultFormula;
  }

  /**
   * For consistency with the ISDA model version 1.8.2 and lower, a bug in the accrual on default calculation
   * has been reproduced.
   * @param useCorrectAccOnDefaultFormula Set to true to use correct accrual on default formulae.
   * @param arbHandling How should any arbitrage in the input date be handled
   */
  public FastCreditCurveBuilder(final boolean useCorrectAccOnDefaultFormula, final ArbitrageHandling arbHandling) {
    ArgumentChecker.notNull(arbHandling, "arbHandling");
    _arbHandling = arbHandling;
    _useCorrectAccOnDefaultFormula = useCorrectAccOnDefaultFormula;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic calibrationCDS, final CDSQuoteConvention marketQuote, final ISDACompliantYieldCurve yieldCurve) {
    double puf;
    double coupon;
    if (marketQuote instanceof ParSpread) {
      puf = 0.0;
      coupon = marketQuote.getCoupon();
    } else if (marketQuote instanceof QuotedSpread) {
      puf = 0.0;
      coupon = ((QuotedSpread) marketQuote).getQuotedSpread();
    } else if (marketQuote instanceof PointsUpFront) {
      final PointsUpFront temp = (PointsUpFront) marketQuote;
      puf = temp.getPointsUpFront();
      coupon = temp.getCoupon();
    } else {
      throw new IllegalArgumentException("Unknown CDSQuoteConvention type " + marketQuote.getClass());
    }

    return calibrateCreditCurve(new CDSAnalytic[] {calibrationCDS }, new double[] {coupon }, yieldCurve, new double[] {puf });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic[] calibrationCDSs, final CDSQuoteConvention[] marketQuotes, final ISDACompliantYieldCurve yieldCurve) {
    ArgumentChecker.noNulls(marketQuotes, "marketQuotes");
    final int n = marketQuotes.length;
    final double[] coupons = new double[n];
    final double[] pufs = new double[n];
    for (int i = 0; i < n; i++) {
      final double[] temp = getStandardQuoteForm(calibrationCDSs[i], marketQuotes[i], yieldCurve);
      coupons[i] = temp[0];
      pufs[i] = temp[1];
    }

    return calibrateCreditCurve(calibrationCDSs, coupons, yieldCurve, pufs);
  }

  /**
   * Put any CDS market quote into the form needed for the curve builder, namely coupon and points up-front (which can be zero)
   * @param calibrationCDS
   * @param marketQuote
   * @param yieldCurve
   * @return The market quotes in the form required by the curve builder
   */
  private double[] getStandardQuoteForm(final CDSAnalytic calibrationCDS, final CDSQuoteConvention marketQuote, final ISDACompliantYieldCurve yieldCurve) {
    final double[] res = new double[2];
    if (marketQuote instanceof ParSpread) {
      res[0] = marketQuote.getCoupon();
    } else if (marketQuote instanceof QuotedSpread) {
      final QuotedSpread temp = (QuotedSpread) marketQuote;
      final double coupon = temp.getCoupon();
      final double qSpread = temp.getQuotedSpread();
      final ISDACompliantCreditCurve cc = calibrateCreditCurve(new CDSAnalytic[] {calibrationCDS }, new double[] {qSpread }, yieldCurve, new double[1]);
      res[0] = coupon;
      res[1] = PRICER.pv(calibrationCDS, yieldCurve, cc, coupon, PriceType.CLEAN);
    } else if (marketQuote instanceof PointsUpFront) {
      final PointsUpFront temp = (PointsUpFront) marketQuote;
      res[0] = temp.getCoupon();
      res[1] = temp.getPointsUpFront();
    } else {
      throw new IllegalArgumentException("Unknown CDSQuoteConvention type " + marketQuote.getClass());
    }
    return res;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic cds, final double premium, final ISDACompliantYieldCurve yieldCurve, final double pointsUpfront) {
    return calibrateCreditCurve(new CDSAnalytic[] {cds }, new double[] {premium }, yieldCurve, new double[] {pointsUpfront });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic cds, final double parSpread, final ISDACompliantYieldCurve yieldCurve) {
    return calibrateCreditCurve(new CDSAnalytic[] {cds }, new double[] {parSpread }, yieldCurve, new double[1]);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic[] cds, final double[] parSpreads, final ISDACompliantYieldCurve yieldCurve) {
    ArgumentChecker.notNull(cds, "null CDS");
    final int n = cds.length;
    final double[] pointsUpfront = new double[n];
    return calibrateCreditCurve(cds, parSpreads, yieldCurve, pointsUpfront);
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
    final double proStart = cds[0].getProtectionStart();
    for (int i = 1; i < n; i++) {
      ArgumentChecker.isTrue(proStart == cds[i].getProtectionStart(), "all CDSs must has same protection start");
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

      switch (_arbHandling) {
        case Ignore: {
          final double minValue = 0.0;
          final double[] bracket = BRACKER.getBracketedPoints(func, 0.8 * guess[i], 1.25 * guess[i], minValue, Double.POSITIVE_INFINITY);
          final double zeroRate = ROOTFINDER.getRoot(func, bracket[0], bracket[1]);
          creditCurve = creditCurve.withRate(zeroRate, i);
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
          throw new IllegalArgumentException("unknow case " + _arbHandling);
      }

    }
    return creditCurve;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final LocalDate today, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate startDate, final LocalDate endDate,
      final double fractionalParSpread, final boolean payAccOnDefault, final Period tenor, final StubType stubType, final boolean protectStart, final ISDACompliantYieldCurve yieldCurve,
      final double recoveryRate) {
    return calibrateCreditCurve(today, stepinDate, valueDate, startDate, new LocalDate[] {endDate }, new double[] {fractionalParSpread }, payAccOnDefault, tenor, stubType, protectStart, yieldCurve,
        recoveryRate);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final LocalDate today, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate startDate, final LocalDate[] endDates,
      final double[] couponRates, final boolean payAccOnDefault, final Period tenor, final StubType stubType, final boolean protectStart, final ISDACompliantYieldCurve yieldCurve,
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
      _proLegIntPoints = getIntegrationsPoints(cds.getProtectionStart(), cds.getProtectionEnd(), yieldCurve.getKnotTimes(), creditCurveKnots);
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
        final double[] integrationSchedule = getIntegrationsPoints(cds.getAccStart(0), cds.getAccEnd(_nPayments - 1), yieldCurve.getKnotTimes(), creditCurveKnots);
        final double offsetStepin = cds.getStepin() + offset;

        _accRate = new double[_nPayments];
        _offsetAccStart = new double[_nPayments];
        _premLegIntPoints = new double[_nPayments][];
        _premDF = new double[_nPayments][];
        _rt = new double[_nPayments][];
        _premDt = new double[_nPayments][];
        for (int i = 0; i < _nPayments; i++) {
          final double offsetAccStart = cds.getAccStart(i) + offset;
          _offsetAccStart[i] = offsetAccStart;
          final double offsetAccEnd = cds.getAccEnd(i) + offset;
          _accRate[i] = cds.getAccrualFraction(i) / (offsetAccEnd - offsetAccStart);
          final double start = Math.max(offsetAccStart, offsetStepin);
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
            // _rt[i][k - 1] = Math.log(_premDF[i][k - 1] / _premDF[i][k]) / dt;
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

      final double obsOffset = _cds.isProtectionFromStartOfDay() ? -_cds.getCurveOneDay() : 0.0;
      double pv = 0.0;
      for (int i = 0; i < _nPayments; i++) {
        final double q = creditCurve.getDiscountFactor(_cds.getAccEnd(i) + obsOffset);
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

      double t0 = _useCorrectAccOnDefaultFormula ? 0 : t - accStart + 1 / 730.0; // TODO not entirely clear why ISDA adds half a day
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
        if (_useCorrectAccOnDefaultFormula) {
          if (Math.abs(dhrt) < 1e-5) {
            tPV = dht * dt * b0 * epsilonP(-dhrt);
          } else {
            tPV = dht * dt / dhrt * ((b0 - b1) / dhrt - b1);
          }
        } else {
          final double t1 = t - accStart + 1 / 730.0;
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
