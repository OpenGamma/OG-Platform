/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.fastcalibration;

import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.DoublesScheduleGenerator.getIntegrationsPoints;
import static com.opengamma.analytics.math.utilities.Epsilon.epsilon;
import static com.opengamma.analytics.math.utilities.Epsilon.epsilonP;

import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantYieldCurve;

/**
 * 
 */
public class ProtectionLegElement {

  private final double[] _knots;
  private final double[] _rt;
  private final double[] _p;
  private final int _n;
  private final int _index;

  private double _pv;
  private double _dPVdh;

  public ProtectionLegElement(final double start, final double end, final ISDACompliantYieldCurve yieldCurve, final double[] creditCurveNodes, final int index) {
    _knots = getIntegrationsPoints(start, end, yieldCurve.getKnotTimes(), creditCurveNodes);
    _n = _knots.length;
    _rt = new double[_n];
    _p = new double[_n];
    for (int i = 0; i < _n; i++) {
      _rt[i] = yieldCurve.getRT(_knots[i]);
      _p[i] = Math.exp(-_rt[i]);
    }
    _index = index;
  }

  public double getPV() {
    return _pv;
  }

  public double dPVdH(final int index) {
    return index == _index ? _dPVdh : 0.0;
  }

  public void update(final ISDACompliantCreditCurve creditCurve) {
    double t = _knots[0];
    double ht0 = creditCurve.getRT(t);
    double rt0 = _rt[0];
    double dqdr0 = creditCurve.getSingleNodeDiscountFactorSensitivity(t, _index);
    double q0 = Math.exp(-ht0);
    double p0 = _p[0];
    double b0 = p0 * q0; // risky discount factor

    double pv = 0.0;
    double pvSense = 0.0;
    for (int i = 1; i < _n; ++i) {
      t = _knots[i];
      final double ht1 = creditCurve.getRT(t);
      final double rt1 = _rt[i];
      final double q1 = Math.exp(-ht1);
      final double p1 = _p[i];
      final double b1 = p1 * q1;
      final double dqdr1 = creditCurve.getSingleNodeDiscountFactorSensitivity(t, _index);
      final double dht = ht1 - ht0;
      final double drt = rt1 - rt0;
      final double dhrt = dht + drt;

      // The formula has been modified from ISDA (but is equivalent) to avoid log(exp(x)) and explicitly calculating the time
      // step - it also handles the limit
      double dPV;
      double dPVSense;
      if (Math.abs(dhrt) < 1e-5) {
        final double e = epsilon(-dhrt);
        final double eP = epsilonP(-dhrt);
        dPV = dht * b0 * e;
        final double dPVdq0 = p0 * ((1 + dht) * e - dht * eP);
        final double dPVdq1 = -p0 * q0 / q1 * (e - dht * eP);
        dPVSense = dPVdq0 * dqdr0 + dPVdq1 * dqdr1;
      } else {
        final double w1 = (b0 - b1) / dhrt;
        dPV = dht * w1;
        final double w = drt * w1;
        dPVSense = ((w / q0 + dht * p0) / dhrt) * dqdr0 - ((w / q1 + dht * p1) / dhrt) * dqdr1;
      }

      pv += dPV;
      pvSense += dPVSense;
      ht0 = ht1;
      dqdr0 = dqdr1;
      rt0 = rt1;
      p0 = p1;
      q0 = q1;
      b0 = b1;
    }
    _pv = pv;
    _dPVdh = pvSense;
  }

}
