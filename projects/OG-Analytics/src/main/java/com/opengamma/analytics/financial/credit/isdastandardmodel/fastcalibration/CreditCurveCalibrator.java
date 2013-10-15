/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel.fastcalibration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AccrualOnDefaultFormulae;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSCoupon;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.NewtonRaphsonSingleRootFinder;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class CreditCurveCalibrator {

  private static final NewtonRaphsonSingleRootFinder ROOTFINDER = new NewtonRaphsonSingleRootFinder();

  private final int _n;
  private final Node[] _nodes;
  private final double[] _t;
  private final CDS[] _cds;

  public CreditCurveCalibrator(final CDSAnalytic[] cds, final ISDACompliantYieldCurve yieldCurve, final AccrualOnDefaultFormulae formula) {
    _n = cds.length;
    _nodes = new Node[_n];
    _cds = new CDS[_n];
    _t = new double[_n];
    final ProtectionLegElement[] protLeg = new ProtectionLegElement[_n];

    final List<CDSPremiumPayment> coupons = new ArrayList<>();
    final List<List<CDSPremiumPayment>> nodes = new ArrayList<>(_n);
    final CDSPremiumPayment[][] premLegs = new CDSPremiumPayment[_n][];

    //The protection leg
    double start = cds[0].getEffectiveProtectionStart();
    for (int i = 0; i < _n; i++) {
      _t[i] = cds[i].getProtectionEnd();
      protLeg[i] = new ProtectionLegElement(start, _t[i], yieldCurve, _t, i);
      start = _t[i];
    }

    for (int i = 0; i < _n; i++) {
      final CDSAnalytic lCDS = cds[i];
      final CDSCoupon[] c = lCDS.getCoupons();
      int nodeIndex = 0;
      final int nPayments = c.length;

      nodes.add(new ArrayList<CDSPremiumPayment>());
      premLegs[i] = new CDSPremiumPayment[nPayments];

      for (int k = 0; k < nPayments; k++) {
        while (c[k].getEffEnd() > _t[nodeIndex]) {
          nodeIndex++;
        }
        CDSPremiumPayment coupon = new CDSPremiumPayment(c[k], lCDS.isPayAccOnDefault(), nodeIndex, formula);
        if (coupons.contains(coupon)) {
          final int index = coupons.indexOf(coupon); //if the coupon already exists, get it from list 
          coupon = coupons.get(index);
        } else {
          coupons.add(coupon);
        }

        final List<CDSPremiumPayment> node = nodes.get(nodeIndex);
        if (!node.contains(coupon)) {
          node.add(coupon);
        }

        premLegs[i][k] = coupon;
      }
    }

    //TODO check all CDSs have same protection start
    final double proStart = cds[0].getEffectiveProtectionStart();
    final Iterator<CDSPremiumPayment> iter = coupons.iterator();
    while (iter.hasNext()) {
      iter.next().initialise(proStart, yieldCurve, _t);
    }

    for (int i = 0; i < _n; i++) {
      final ProtectionLegElement[] temp = new ProtectionLegElement[i + 1];
      System.arraycopy(protLeg, 0, temp, 0, i + 1);
      final double valueDF = yieldCurve.getDiscountFactor(cds[i].getValuationTime());
      final double accrued = cds[i].getAccruedPremiumPerUnitSpread();
      _cds[i] = new CDS(new CDSProtectionLeg(valueDF, temp), new CDSPremiumLeg(valueDF, accrued, premLegs[i]));

      final List<CDSPremiumPayment> temp1 = nodes.get(i);
      final CDSPremiumPayment[] a = new CDSPremiumPayment[temp1.size()];
      temp1.toArray(a);
      _nodes[i] = new Node(i, protLeg[i], a);
    }

  }

  public ISDACompliantCreditCurve calibrate(final CDSMarketInfo[] info) {
    ArgumentChecker.noNulls(info, "info");
    ArgumentChecker.isTrue(_n == info.length, "info wrong length");

    // use continuous premiums as initial guess
    final double[] guess = new double[_n];
    for (int i = 0; i < _n; i++) {
      guess[i] = (info[i].getCoupon() + info[i].getPuf() / _t[i]) / info[i].getLGD();
    }

    final ISDACompliantCreditCurve creditCurve = new ISDACompliantCreditCurve(_t, guess);
    for (int i = 0; i < _n; i++) {
      final Function1D<Double, Double> func = getPointFunction(creditCurve, info[i], i);
      final Function1D<Double, Double> grad = getPointDerivative(creditCurve, info[i], i);

      // final double minValue = 0.0;
      //    final double[] bracket = BRACKER.getBracketedPoints(func, 0.8 * guess[i], 1.25 * guess[i], minValue, Double.POSITIVE_INFINITY);
      final double zeroRate = ROOTFINDER.getRoot(func, grad, guess[i]);
      creditCurve.setRate(zeroRate, i);
      _nodes[i].update(creditCurve);
    }
    return creditCurve;
  }

  public double[] price(final CDSMarketInfo[] info, final ISDACompliantCreditCurve cc) {
    final double[] res = new double[_n];
    for (int i = 0; i < _n; i++) {
      _nodes[i].update(cc);
      res[i] = _cds[i].pv(info[i].getCoupon(), info[i].getLGD(), PriceType.CLEAN) - info[i].getPuf();
    }
    return res;
  }

  public double[] protLeg(final double rr, final ISDACompliantCreditCurve cc) {
    final double[] res = new double[_n];
    for (int i = 0; i < _n; i++) {
      _nodes[i].update(cc);
      res[i] = _cds[i].protLeg(1 - rr);
    }
    return res;
  }

  public double[] premLeg(final double[] coupons, final ISDACompliantCreditCurve cc) {
    final double[] res = new double[_n];
    for (int i = 0; i < _n; i++) {
      _nodes[i].update(cc);
      res[i] = _cds[i].premiumLeg(coupons[i], PriceType.CLEAN);
    }
    return res;
  }

  private Function1D<Double, Double> getPointFunction(final ISDACompliantCreditCurve creditCurve, final CDSMarketInfo info, final int index) {
    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        // final ISDACompliantCreditCurve cc = creditCurve.withRate(x, index);
        creditCurve.setRate(x, index);
        _nodes[index].update(creditCurve); //update prices that are sensitive to this node only 
        return _cds[index].pv(info.getCoupon(), info.getLGD(), PriceType.CLEAN) - info.getPuf();
      }
    };
  }

  private Function1D<Double, Double> getPointDerivative(final ISDACompliantCreditCurve creditCurve, final CDSMarketInfo info, final int index) {
    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        //Review this relies on the fact that the function is evaluated first (with the same x) and update is called there.
        return _cds[index].dPVdH(info.getCoupon(), info.getLGD(), index);
      }
    };
  }

  class Node {

    private final int _index;
    private final ProtectionLegElement _protElements;
    private final CDSPremiumPayment[] _payments;

    public Node(final int index, final ProtectionLegElement protElements, final CDSPremiumPayment[] payments) {
      _index = index;
      _protElements = protElements;
      _payments = payments;

    }

    public void update(final ISDACompliantCreditCurve creditCurve) {
      _protElements.update(creditCurve);
      for (final CDSPremiumPayment e : _payments) {
        e.update(creditCurve);
      }
    }

    public int getIndex() {
      return _index;
    }

  }

}
