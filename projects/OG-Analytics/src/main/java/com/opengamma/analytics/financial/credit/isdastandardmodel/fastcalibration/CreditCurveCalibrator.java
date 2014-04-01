/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel.fastcalibration;

import static com.opengamma.analytics.financial.credit.isdastandardmodel.DoublesScheduleGenerator.getIntegrationsPoints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opengamma.analytics.financial.credit.isdastandardmodel.AccrualOnDefaultFormulae;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSCoupon;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurveBuilder.ArbitrageHandling;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.MultiCDSAnalytic;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.NewtonRaphsonSingleRootFinder;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class CreditCurveCalibrator {

  private static final NewtonRaphsonSingleRootFinder ROOTFINDER = new NewtonRaphsonSingleRootFinder();

  private final int _nCDS;
  private final int _nCoupons;
  private final double[] _t;
  private final double _valuationDF;
  private final double[] _lgd;
  private final double[] _unitAccured;

  private final int[][] _cds2CouponsMap;
  private final int[][] _cdsCouponsUpdateMap;
  private final int[][] _knot2CouponsMap;
  private final ProtectionLegElement[] _protElems;
  private final CouponOnlyElement[] _premElems;
  private final ArbitrageHandling _arbHandle;

  public CreditCurveCalibrator(final MultiCDSAnalytic multiCDS, final ISDACompliantYieldCurve yieldCurve) {
    this(multiCDS, yieldCurve, AccrualOnDefaultFormulae.OrignalISDA, ArbitrageHandling.Ignore);
  }

  public CreditCurveCalibrator(final MultiCDSAnalytic multiCDS, final ISDACompliantYieldCurve yieldCurve, final AccrualOnDefaultFormulae formula, final ArbitrageHandling arbHandle) {
    ArgumentChecker.notNull(multiCDS, "multiCDS");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    _arbHandle = arbHandle;

    _nCDS = multiCDS.getNumMaturities();
    _t = new double[_nCDS];
    _lgd = new double[_nCDS];
    _unitAccured = new double[_nCDS];
    for (int i = 0; i < _nCDS; i++) {
      _t[i] = multiCDS.getProtectionEnd(i);
      _lgd[i] = multiCDS.getLGD();
      _unitAccured[i] = multiCDS.getAccruedPremiumPerUnitSpread(i);
    }
    _valuationDF = yieldCurve.getDiscountFactor(multiCDS.getCashSettleTime());

    //This is the global set of knots - it will be truncated down for the various leg elements 
    //TODO this will not match ISDA C for forward starting (i.e. accStart > tradeDate) CDS, and will give different answers 
    //if the Markit 'fix' is used in that case
    final double[] knots = getIntegrationsPoints(multiCDS.getEffectiveProtectionStart(), _t[_nCDS - 1], yieldCurve.getKnotTimes(), _t);

    //The protection leg
    _protElems = new ProtectionLegElement[_nCDS];
    for (int i = 0; i < _nCDS; i++) {
      _protElems[i] = new ProtectionLegElement(i == 0 ? multiCDS.getEffectiveProtectionStart() : _t[i - 1], _t[i], yieldCurve, i, knots);
    }

    _cds2CouponsMap = new int[_nCDS][];
    _cdsCouponsUpdateMap = new int[_nCDS][];
    _knot2CouponsMap = new int[_nCDS][];

    final List<CDSCoupon> allCoupons = new ArrayList<>(_nCDS + multiCDS.getTotalPayments() - 1);
    allCoupons.addAll(Arrays.asList(multiCDS.getStandardCoupons()));
    allCoupons.add(multiCDS.getTerminalCoupon(_nCDS - 1));
    final int[] temp = new int[multiCDS.getTotalPayments()];
    for (int i = 0; i < multiCDS.getTotalPayments(); i++) {
      temp[i] = i;
    }
    _cds2CouponsMap[_nCDS - 1] = temp;

    //complete the list of unique coupons and fill out the cds2CouponsMap
    for (int i = 0; i < _nCDS - 1; i++) {
      final CDSCoupon c = multiCDS.getTerminalCoupon(i);
      final int nPayments = Math.max(0, multiCDS.getPaymentIndexForMaturity(i)) + 1;
      _cds2CouponsMap[i] = new int[nPayments];
      for (int jj = 0; jj < nPayments - 1; jj++) {
        _cds2CouponsMap[i][jj] = jj;
      }
      //because of business-day adjustment, a terminal coupon can be identical to a standard coupon,
      //in which case it is not added again 
      int index = allCoupons.indexOf(c);
      if (index == -1) {
        index = allCoupons.size();
        allCoupons.add(c);
      }
      _cds2CouponsMap[i][nPayments - 1] = index;
    }

    //loop over the coupons to populate the couponUpdateMap
    _nCoupons = allCoupons.size();
    final int[] sizes = new int[_nCDS];
    final int[] map = new int[_nCoupons];
    for (int i = 0; i < _nCoupons; i++) {
      final CDSCoupon c = allCoupons.get(i);
      int index = Arrays.binarySearch(_t, c.getEffEnd());
      if (index < 0) {
        index = -(index + 1);
      }
      sizes[index]++;
      map[i] = index;
    }

    //make the protection leg elements 
    _premElems = new CouponOnlyElement[_nCoupons];
    if (multiCDS.isPayAccOnDefault()) {
      for (int i = 0; i < _nCoupons; i++) {
        _premElems[i] = new PremiumLegElement(multiCDS.getEffectiveProtectionStart(), allCoupons.get(i), yieldCurve, map[i], knots, formula);
      }
    } else {
      for (int i = 0; i < _nCoupons; i++) {
        _premElems[i] = new CouponOnlyElement(allCoupons.get(i), yieldCurve, map[i]);
      }
    }

    //sort a map from coupon to curve node, to a map from curve node to coupons 
    for (int i = 0; i < _nCDS; i++) {
      _knot2CouponsMap[i] = new int[sizes[i]];
    }
    final int[] indexes = new int[_nCDS];
    for (int i = 0; i < _nCoupons; i++) {
      final int index = map[i];
      _knot2CouponsMap[index][indexes[index]++] = i;
    }

    //the cdsCouponsUpdateMap is the intersection of the cds2CouponsMap and knot2CouponsMap
    for (int i = 0; i < _nCDS; i++) {
      _cdsCouponsUpdateMap[i] = intersection(_knot2CouponsMap[i], _cds2CouponsMap[i]);
    }

  }

  public CreditCurveCalibrator(final CDSAnalytic[] cds, final ISDACompliantYieldCurve yieldCurve) {
    this(cds, yieldCurve, AccrualOnDefaultFormulae.OrignalISDA, ArbitrageHandling.Ignore);
  }

  public CreditCurveCalibrator(final CDSAnalytic[] cds, final ISDACompliantYieldCurve yieldCurve, final AccrualOnDefaultFormulae formula, final ArbitrageHandling arbHandle) {
    ArgumentChecker.noNulls(cds, "cds");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    _arbHandle = arbHandle;

    _nCDS = cds.length;
    final boolean payAccOnDefault = cds[0].isPayAccOnDefault();
    final double accStart = cds[0].getAccStart();
    final double effectProtStart = cds[0].getEffectiveProtectionStart();
    final double cashSettleTime = cds[0].getCashSettleTime();
    _t = new double[_nCDS];
    _t[0] = cds[0].getProtectionEnd();
    //Check all the CDSs match
    for (int i = 1; i < _nCDS; i++) {
      ArgumentChecker.isTrue(payAccOnDefault == cds[i].isPayAccOnDefault(), "All CDSs must have same pay-accrual on default status");
      ArgumentChecker.isTrue(accStart == cds[i].getAccStart(), "All CDSs must has same accrual start");
      ArgumentChecker.isTrue(effectProtStart == cds[i].getEffectiveProtectionStart(), "All CDSs must has same effective protection start");
      ArgumentChecker.isTrue(cashSettleTime == cds[i].getCashSettleTime(), "All CDSs must has same cash-settle time");
      _t[i] = cds[i].getProtectionEnd();
      ArgumentChecker.isTrue(_t[i] > _t[i - 1], "CDS maturities must be increasing");
    }

    _valuationDF = yieldCurve.getDiscountFactor(cashSettleTime);
    _lgd = new double[_nCDS];
    _unitAccured = new double[_nCDS];
    for (int i = 0; i < _nCDS; i++) {
      _lgd[i] = cds[i].getLGD();
      _unitAccured[i] = cds[i].getAccruedYearFraction();
    }

    //This is the global set of knots - it will be truncated down for the various leg elements 
    //TODO this will not match ISDA C for forward starting (i.e. accStart > tradeDate) CDS, and will give different answers 
    //if the Markit 'fix' is used in that case
    final double[] knots = getIntegrationsPoints(effectProtStart, _t[_nCDS - 1], yieldCurve.getKnotTimes(), _t);

    //The protection leg
    _protElems = new ProtectionLegElement[_nCDS];
    for (int i = 0; i < _nCDS; i++) {
      _protElems[i] = new ProtectionLegElement(i == 0 ? effectProtStart : _t[i - 1], _t[i], yieldCurve, i, knots);
    }

    _cds2CouponsMap = new int[_nCDS][];
    _cdsCouponsUpdateMap = new int[_nCDS][];
    _knot2CouponsMap = new int[_nCDS][];

    final int nPaymentsFinalCDS = cds[_nCDS - 1].getNumPayments();
    final List<CDSCoupon> allCoupons = new ArrayList<>(_nCDS + nPaymentsFinalCDS - 1);
    allCoupons.addAll(Arrays.asList(cds[_nCDS - 1].getCoupons()));
    final int[] temp = new int[nPaymentsFinalCDS];
    for (int i = 0; i < nPaymentsFinalCDS; i++) {
      temp[i] = i;
    }
    _cds2CouponsMap[_nCDS - 1] = temp;

    //complete the list of unique coupons and fill out the cds2CouponsMap
    for (int i = 0; i < _nCDS - 1; i++) {
      final CDSCoupon[] c = cds[i].getCoupons();
      final int nPayments = c.length;
      _cds2CouponsMap[i] = new int[nPayments];
      for (int k = 0; k < nPayments; k++) {
        int index = allCoupons.indexOf(c[k]);
        if (index == -1) {
          index = allCoupons.size();
          allCoupons.add(c[k]);
        }
        _cds2CouponsMap[i][k] = index;
      }
    }

    //loop over the coupons to populate the couponUpdateMap
    _nCoupons = allCoupons.size();
    final int[] sizes = new int[_nCDS];
    final int[] map = new int[_nCoupons];
    for (int i = 0; i < _nCoupons; i++) {
      final CDSCoupon c = allCoupons.get(i);
      int index = Arrays.binarySearch(_t, c.getEffEnd());
      if (index < 0) {
        index = -(index + 1);
      }
      sizes[index]++;
      map[i] = index;
    }

    //make the protection leg elements 
    _premElems = new CouponOnlyElement[_nCoupons];
    if (payAccOnDefault) {
      for (int i = 0; i < _nCoupons; i++) {
        _premElems[i] = new PremiumLegElement(effectProtStart, allCoupons.get(i), yieldCurve, map[i], knots, formula);
      }
    } else {
      for (int i = 0; i < _nCoupons; i++) {
        _premElems[i] = new CouponOnlyElement(allCoupons.get(i), yieldCurve, map[i]);
      }
    }

    //sort a map from coupon to curve node, to a map from curve node to coupons 
    for (int i = 0; i < _nCDS; i++) {
      _knot2CouponsMap[i] = new int[sizes[i]];
    }
    final int[] indexes = new int[_nCDS];
    for (int i = 0; i < _nCoupons; i++) {
      final int index = map[i];
      _knot2CouponsMap[index][indexes[index]++] = i;
    }

    //the cdsCouponsUpdateMap is the intersection of the cds2CouponsMap and knot2CouponsMap
    for (int i = 0; i < _nCDS; i++) {
      _cdsCouponsUpdateMap[i] = intersection(_knot2CouponsMap[i], _cds2CouponsMap[i]);
    }

  }

  public ISDACompliantCreditCurve calibrate(final double[] premiums) {
    ArgumentChecker.notEmpty(premiums, "premiums");
    ArgumentChecker.isTrue(_nCDS == premiums.length, "premiums wrong length");
    final double[] puf = new double[_nCDS];
    final CalibrationImpl imp = new CalibrationImpl();
    return imp.calibrate(premiums, puf);
  }

  public ISDACompliantCreditCurve calibrate(final double[] premiums, final double[] puf) {
    ArgumentChecker.notEmpty(premiums, "premiums");
    ArgumentChecker.notEmpty(puf, "puf");
    ArgumentChecker.isTrue(_nCDS == premiums.length, "premiums wrong length");
    ArgumentChecker.isTrue(_nCDS == puf.length, "puf wrong length");

    final CalibrationImpl imp = new CalibrationImpl();
    return imp.calibrate(premiums, puf);
  }

  private class CalibrationImpl {

    private double[][] _protLegElmtPV;
    private double[][] _premLegElmtPV;
    private ISDACompliantCreditCurve _creditCurve;

    public ISDACompliantCreditCurve calibrate(final double[] premiums, final double[] puf) {
      _protLegElmtPV = new double[_nCDS][2];
      _premLegElmtPV = new double[_nCoupons][2];

      // use continuous premiums as initial guess
      final double[] guess = new double[_nCDS];
      for (int i = 0; i < _nCDS; i++) {
        guess[i] = (premiums[i] + puf[i] / _t[i]) / _lgd[i];
      }

      _creditCurve = new ISDACompliantCreditCurve(_t, guess);
      for (int i = 0; i < _nCDS; i++) {
        final Function1D<Double, Double> func = getPointFunction(i, premiums[i], puf[i]);
        final Function1D<Double, Double> grad = getPointDerivative(i, premiums[i]);
        switch (_arbHandle) {
          case Ignore: {
            final double zeroRate = ROOTFINDER.getRoot(func, grad, guess[i]);
            updateAll(zeroRate, i);
            break;
          }
          case Fail: {
            final double minValue = i == 0 ? 0.0 : _creditCurve.getRTAtIndex(i - 1) / _creditCurve.getTimeAtIndex(i);
            if (i > 0 && func.evaluate(minValue) > 0.0) { //can never fail on the first spread
              final StringBuilder msg = new StringBuilder();
              if (puf[i] == 0.0) {
                msg.append("The par spread of " + premiums[i] + " at index " + i);
              } else {
                msg.append("The premium of " + premiums[i] + "and points up-front of " + puf[i] + " at index " + i);
              }
              msg.append(" is an arbitrage; cannot fit a curve with positive forward hazard rate. ");
              throw new IllegalArgumentException(msg.toString());
            }
            guess[i] = Math.max(minValue, guess[i]);
            final double zeroRate = ROOTFINDER.getRoot(func, grad, guess[i]);
            updateAll(zeroRate, i);
            break;
          }
          case ZeroHazardRate: {
            final double minValue = i == 0 ? 0.0 : _creditCurve.getRTAtIndex(i - 1) / _creditCurve.getTimeAtIndex(i);
            if (i > 0 && func.evaluate(minValue) > 0.0) { //can never fail on the first spread
              updateAll(minValue, i); //this is setting the forward hazard rate for this period to zero, rather than letting it go negative
            } else {
              guess[i] = Math.max(minValue, guess[i]);
              final double zeroRate = ROOTFINDER.getRoot(func, grad, guess[i]);
              updateAll(zeroRate, i);
            }
            break;
          }
        }
      }

      return _creditCurve;
    }

    private Function1D<Double, Double> getPointFunction(final int index, final double premium, final double puf) {
      final int[] iCoupons = _cds2CouponsMap[index];
      final int nCoupons = iCoupons.length;
      final double dirtyPV = puf - premium * _unitAccured[index];
      final double lgd = _lgd[index];
      return new Function1D<Double, Double>() {
        @Override
        public Double evaluate(final Double h) {
          update(h, index);
          double protLegPV = 0.0;
          for (int i = 0; i <= index; i++) {
            protLegPV += _protLegElmtPV[i][0];
          }
          double premLegPV = 0.0;
          for (int i = 0; i < nCoupons; i++) {
            final int jj = iCoupons[i];
            premLegPV += _premLegElmtPV[jj][0];
          }
          final double pv = (lgd * protLegPV - premium * premLegPV) / _valuationDF - dirtyPV;
          return pv;
        }
      };
    }

    private Function1D<Double, Double> getPointDerivative(final int index, final double premium) {
      final int[] iCoupons = _cdsCouponsUpdateMap[index];
      final int nCoupons = iCoupons.length;
      final double lgd = _lgd[index];
      return new Function1D<Double, Double>() {
        @Override
        public Double evaluate(final Double x) {
          //do not call update - all ready called for getting the value 

          final double protLegPVSense = _protLegElmtPV[index][1];

          double premLegPVSense = 0.0;
          for (int i = 0; i < nCoupons; i++) {
            final int jj = iCoupons[i];
            premLegPVSense += _premLegElmtPV[jj][1];
          }
          final double pvSense = (lgd * protLegPVSense - premium * premLegPVSense) / _valuationDF;
          return pvSense;
        }
      };
    }

    private void update(final double h, final int index) {
      _creditCurve.setRate(h, index);
      _protLegElmtPV[index] = _protElems[index].pvAndSense(_creditCurve);
      final int[] iCoupons = _cdsCouponsUpdateMap[index];
      final int n = iCoupons.length;
      for (int i = 0; i < n; i++) {
        final int jj = iCoupons[i];
        _premLegElmtPV[jj] = _premElems[jj].pvAndSense(_creditCurve);
      }
    }

    private void updateAll(final double h, final int index) {
      _creditCurve.setRate(h, index);
      _protLegElmtPV[index] = _protElems[index].pvAndSense(_creditCurve);
      final int[] iCoupons = _knot2CouponsMap[index];
      final int n = iCoupons.length;
      for (int i = 0; i < n; i++) {
        final int jj = iCoupons[i];
        _premLegElmtPV[jj] = _premElems[jj].pvAndSense(_creditCurve);
      }
    }

  }

  private static int[] intersection(final int[] first, final int[] second) {
    final int n1 = first.length;
    final int n2 = second.length;
    int[] a;
    int[] b;
    int n;
    if (n1 > n2) {
      a = second;
      b = first;
      n = n2;
    } else {
      a = first;
      b = second;
      n = n1;
    }
    final int[] temp = new int[n];
    int count = 0;
    for (int i = 0; i < n; i++) {
      final int index = Arrays.binarySearch(b, a[i]);
      if (index >= 0) {
        temp[count++] = a[i];
      }
    }
    final int[] res = new int[count];
    System.arraycopy(temp, 0, res, 0, count);
    return res;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_arbHandle == null) ? 0 : _arbHandle.hashCode());
    //    Correction made PLAT-6314
    //    result = prime * result + Arrays.hashCode(_cds2CouponsMap);
    //    result = prime * result + Arrays.hashCode(_cdsCouponsUpdateMap);
    //    result = prime * result + Arrays.hashCode(_knot2CouponsMap);
    result = prime * result + Arrays.deepHashCode(_cds2CouponsMap);
    result = prime * result + Arrays.deepHashCode(_cdsCouponsUpdateMap);
    result = prime * result + Arrays.deepHashCode(_knot2CouponsMap);
    result = prime * result + Arrays.hashCode(_lgd);
    result = prime * result + _nCDS;
    result = prime * result + _nCoupons;
    result = prime * result + Arrays.hashCode(_premElems);
    result = prime * result + Arrays.hashCode(_protElems);
    result = prime * result + Arrays.hashCode(_t);
    result = prime * result + Arrays.hashCode(_unitAccured);
    long temp;
    temp = Double.doubleToLongBits(_valuationDF);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final CreditCurveCalibrator other = (CreditCurveCalibrator) obj;
    if (_arbHandle != other._arbHandle) {
      return false;
    }
    if (!Arrays.deepEquals(_cds2CouponsMap, other._cds2CouponsMap)) {
      return false;
    }
    if (!Arrays.deepEquals(_cdsCouponsUpdateMap, other._cdsCouponsUpdateMap)) {
      return false;
    }
    if (!Arrays.deepEquals(_knot2CouponsMap, other._knot2CouponsMap)) {
      return false;
    }
    if (!Arrays.equals(_lgd, other._lgd)) {
      return false;
    }
    if (_nCDS != other._nCDS) {
      return false;
    }
    if (_nCoupons != other._nCoupons) {
      return false;
    }
    if (!Arrays.equals(_premElems, other._premElems)) {
      return false;
    }
    if (!Arrays.equals(_protElems, other._protElems)) {
      return false;
    }
    if (!Arrays.equals(_t, other._t)) {
      return false;
    }
    if (!Arrays.equals(_unitAccured, other._unitAccured)) {
      return false;
    }
    if (Double.doubleToLongBits(_valuationDF) != Double.doubleToLongBits(other._valuationDF)) {
      return false;
    }
    return true;
  }

}
