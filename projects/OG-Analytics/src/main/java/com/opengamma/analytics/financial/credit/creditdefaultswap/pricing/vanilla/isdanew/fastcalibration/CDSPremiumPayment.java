/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.fastcalibration;

import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.DoublesScheduleGenerator.getIntegrationsPoints;
import static com.opengamma.analytics.math.utilities.Epsilon.epsilon;
import static com.opengamma.analytics.math.utilities.Epsilon.epsilonP;
import static com.opengamma.analytics.math.utilities.Epsilon.epsilonPP;

import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantYieldCurve;

/**
 * 
 */
public class CDSPremiumPayment {

  private final double _paymentTimes;
  private final double _accFractions;
  private final double _accStart;
  private final double _accEnd;
  private final double _accRate;
  private final boolean _payAccDefault;
  private final boolean _useCorrectAccOnDefaultFormula = true;
  private final int _index;

  private double[] _knots;
  private double[] _rt;
  private double[] _p;
  private double _paymentDF;
  private int _n;

  private double _pv;
  private double _dPVdh;

  public CDSPremiumPayment(final double paymentTime, final double accFrac, final double accStart, final double accEnd, final double accRate, final boolean payAccDefault, final int index) {
    _paymentTimes = paymentTime;
    _accFractions = accFrac;
    _accStart = accStart;
    _accEnd = accEnd;
    _accRate = accRate;
    _payAccDefault = payAccDefault;
    _index = index;
  }

  public void initialise(final ISDACompliantYieldCurve yieldCurve, final double[] creditCurveNodes) {
    _paymentDF = yieldCurve.getDiscountFactor(_paymentTimes);
    _knots = getIntegrationsPoints(_accStart, _accEnd, yieldCurve.getKnotTimes(), creditCurveNodes);
    _n = _knots.length;
    _rt = new double[_n];
    _p = new double[_n];
    for (int i = 0; i < _n; i++) {
      _rt[i] = yieldCurve.getRT(_knots[i]);
      _p[i] = Math.exp(-_rt[i]);
    }
  }

  public void update(final ISDACompliantCreditCurve creditCurve) {
    final double w = _accFractions * _paymentDF;
    double pv = w * creditCurve.getDiscountFactor(_accEnd);
    double pvSense = w * creditCurve.getSingleNodeDiscountFactorSensitivity(_accEnd, _index);
    if (_payAccDefault) {
      final double[] temp = accOnDefault(creditCurve);
      pv += temp[0];
      pvSense += temp[1];
    }
    _pv = pv;
    _dPVdh = pvSense;
  }

  private double[] accOnDefault(final ISDACompliantCreditCurve creditCurve) {

    double t = _knots[0];
    double ht0 = creditCurve.getRT(t);
    double rt0 = _rt[0];
    double p0 = _p[0];
    double q0 = Math.exp(-ht0);
    double b0 = p0 * q0; // this is the risky discount factor
    double dqdr0 = creditCurve.getSingleNodeDiscountFactorSensitivity(t, _index);

    double t0 = _useCorrectAccOnDefaultFormula ? 0.0 : t - _accStart + 1 / 730.0; // TODO not entirely clear why ISDA adds half a day
    double pv = 0.0;
    double pvSense = 0.0;

    for (int j = 1; j < _n; ++j) {
      t = _knots[j];
      final double ht1 = creditCurve.getRT(t);
      final double rt1 = _rt[j];
      final double p1 = _p[j];
      final double q1 = Math.exp(-ht1);
      final double b1 = p1 * q1;
      final double dqdr1 = creditCurve.getSingleNodeDiscountFactorSensitivity(t, _index);

      final double dt = _knots[j] - _knots[j - 1];

      final double dht = ht1 - ht0;
      final double drt = rt1 - rt0;
      final double dhrt = dht + drt;

      double tPV;
      double tPvSense;
      if (_useCorrectAccOnDefaultFormula) {
        if (Math.abs(dhrt) < 1e-5) {
          final double eP = epsilonP(-dhrt);
          final double ePP = epsilonPP(-dhrt);
          tPV = dht * dt * b0 * eP;
          final double dPVdq0 = p0 * dt * ((1 + dht) * eP - dht * ePP);
          final double dPVdq1 = b0 * dt / q1 * (-eP + dht * ePP);
          tPvSense = dPVdq0 * dqdr0 + dPVdq1 * dqdr1;
        } else {
          final double w1 = (b0 - b1) / dhrt;
          final double w2 = w1 - b1;
          final double w3 = dht / dhrt;
          final double w4 = dt / dhrt;
          final double w5 = (1 - w3) * w2;
          final double dPVdq0 = w4 / q0 * (w5 + w3 * (b0 - w1));
          final double dPVdq1 = w4 / q1 * (w5 + w3 * (b1 * (1 + dhrt) - w1));
          tPV = dt * w3 * w2;
          tPvSense = dPVdq0 * dqdr0 - dPVdq1 * dqdr1;

        }
      } else {
        // This is a know bug - a fix is proposed by Markit (and appears commented out in ISDA v.1.8.2)
        // This is the correct term plus dht*t0/dhrt*(b0-b1) which is an error
        final double t1 = t - _accStart + 1 / 730.0;
        if (Math.abs(dhrt) < 1e-5) {
          final double e = epsilon(-dhrt);
          final double eP = epsilonP(-dhrt);
          final double ePP = epsilonPP(-dhrt);
          final double w1 = t0 * e + dt * eP;
          final double w2 = t0 * eP + dt * ePP;
          final double dPVdq0 = p0 * ((1 + dhrt) * w1 - dht * w2);
          final double dPVdq1 = b0 / q1 * (-w1 + dht * w2);
          tPV = dht * b0 * w1;
          tPvSense = dPVdq0 * dqdr0 + dPVdq1 * dqdr1;
        } else {
          final double w1 = dt / dhrt;
          final double w2 = dht / dhrt;
          final double w3 = (t0 + w1) * b0 - (t1 + w1) * b1;
          final double w4 = (1 - w2) / dhrt;
          final double w5 = w1 / dhrt * (b0 - b1);
          final double dPVdq0 = w4 * w3 / q0 + w2 * ((t0 + w1) * p0 - w5 / q0);
          final double dPVdq1 = w4 * w3 / q1 + w2 * ((t1 + w1) * p1 - w5 / q1);
          tPV = dht / dhrt * (t0 * b0 - t1 * b1 + dt / dhrt * (b0 - b1));
          tPvSense = dPVdq0 * dqdr0 - dPVdq1 * dqdr1;
        }
        t0 = t1;
      }

      pv += tPV;
      pvSense += tPvSense;
      ht0 = ht1;
      rt0 = rt1;
      p0 = p1;
      q0 = q1;
      b0 = b1;
      dqdr0 = dqdr1;
    }
    return new double[] {_accRate * pv, _accRate * pvSense };
  }

  /**
   * Gets the paymentTimes.
   * @return the paymentTimes
   */
  public double getPaymentTimes() {
    return _paymentTimes;
  }

  /**
   * Gets the accFractions.
   * @return the accFractions
   */
  public double getAccFractions() {
    return _accFractions;
  }

  /**
   * Gets the accStart.
   * @return the accStart
   */
  public double getAccStart() {
    return _accStart;
  }

  /**
   * Gets the accEnd.
   * @return the accEnd
   */
  public double getAccEnd() {
    return _accEnd;
  }

  /**
   * Gets the accRate.
   * @return the accRate
   */
  public double getAccRate() {
    return _accRate;
  }

  public double getPV() {
    return _pv;
  }

  public double dPVdH(final int index) {
    return index == _index ? _dPVdh : 0.0;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_accEnd);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_accFractions);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_accRate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_accStart);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_paymentTimes);
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
    final CDSPremiumPayment other = (CDSPremiumPayment) obj;
    if (Double.doubleToLongBits(_accEnd) != Double.doubleToLongBits(other._accEnd)) {
      return false;
    }
    if (Double.doubleToLongBits(_accFractions) != Double.doubleToLongBits(other._accFractions)) {
      return false;
    }
    if (Double.doubleToLongBits(_accRate) != Double.doubleToLongBits(other._accRate)) {
      return false;
    }
    if (Double.doubleToLongBits(_accStart) != Double.doubleToLongBits(other._accStart)) {
      return false;
    }
    if (Double.doubleToLongBits(_paymentTimes) != Double.doubleToLongBits(other._paymentTimes)) {
      return false;
    }
    return true;
  }

}
