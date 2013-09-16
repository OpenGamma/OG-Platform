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
import static com.opengamma.analytics.math.utilities.Epsilon.epsilonPP;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class AnalyticCDSPricer {
  /** Default value for determining if results consistent with ISDA model versions 1.8.2 or lower are to be calculated */
  private static final boolean DEFAULT_USE_CORRECT_ACC_ON_DEFAULT_FORMULA = false;
  /** True if results consistent with ISDA model versions 1.8.2 or lower are to be calculated */
  private final boolean _useCorrectAccOnDefaultFormula;

  /**
   * For consistency with the ISDA model version 1.8.2 and lower, a bug in the accrual on default calculation
   * has been reproduced.
   */
  public AnalyticCDSPricer() {
    _useCorrectAccOnDefaultFormula = DEFAULT_USE_CORRECT_ACC_ON_DEFAULT_FORMULA;
  }

  /**
   *  For consistency with the ISDA model version 1.8.2 and lower, a bug in the accrual on default calculation
   * has been reproduced.
   * @param useCorrectAccOnDefaultFormula Set to true to use correct accrual on default formulae.
   */
  public AnalyticCDSPricer(final boolean useCorrectAccOnDefaultFormula) {
    _useCorrectAccOnDefaultFormula = useCorrectAccOnDefaultFormula;
  }

  /**
   * Present value for the payer of premiums (i.e. the buyer of protection)
  * @param cds analytic description of a CDS traded at a certain time
   * @param yieldCurve The yield (or discount) curve
   * @param creditCurve the credit (or survival) curve
   * @param fractionalSpread The <b>fraction</b> spread
  * @param cleanOrDirty Clean or dirty price
   * @return The PV on unit notional
   */
  public double pv(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final double fractionalSpread, final PriceType cleanOrDirty) {
    if (cds.getProtectionEnd() <= 0.0) { //short cut already expired CDSs
      return 0.0;
    }
    // TODO check for any repeat calculations
    final double rpv01 = pvPremiumLegPerUnitSpread(cds, yieldCurve, creditCurve, cleanOrDirty);
    final double proLeg = protectionLeg(cds, yieldCurve, creditCurve);
    return proLeg - fractionalSpread * rpv01;
  }

  /**
   * Present value (clean price) for the payer of premiums (i.e. the buyer of protection)
  * @param cds analytic description of a CDS traded at a certain time
   * @param yieldCurve The yield (or discount) curve
   * @param creditCurve the credit (or survival) curve
   * @param fractionalSpread The <b>fraction</b> spread
   * @return The PV
   */
  public double pv(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final double fractionalSpread) {
    return pv(cds, yieldCurve, creditCurve, fractionalSpread, PriceType.CLEAN);
  }

  /**
   * The par spread par spread for a given yield and credit (hazard rate/survival) curve)
   * @param cds analytic description of a CDS traded at a certain time
   * @param yieldCurve The yield (or discount) curve
   * @param creditCurve the credit (or survival) curve
   * @return the par spread
   */
  public double parSpread(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve) {
    if (cds.getProtectionEnd() <= 0.0) { //short cut already expired CDSs
      throw new IllegalArgumentException("CDSs has expired - cannot compute a par spread for it");
    }

    final double rpv01 = pvPremiumLegPerUnitSpread(cds, yieldCurve, creditCurve, PriceType.CLEAN);
    final double proLeg = protectionLeg(cds, yieldCurve, creditCurve);
    return proLeg / rpv01;
  }

  /**
   * Compute the present value of the protection leg with a notional of 1, which is given by the integral
   * $\frac{1-R}{P(T_{v})} \int_{T_a} ^{T_b} P(t) \frac{dQ(t)}{dt} dt$ where $P(t)$ and $Q(t)$ are the discount and survival curves
   * respectively, $T_a$ and $T_b$ are the start and end of the protection respectively, $T_v$ is the valuation time (all measured
   * from $t = 0$, 'today') and $R$ is the recovery rate.
   * @param cds analytic description of a CDS traded at a certain time
   * @param yieldCurve The yield (or discount) curve
   * @param creditCurve the credit (or survival) curve
   * @return The value of the protection leg (on a unit notional)
   */
  public double protectionLeg(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve) {
    ArgumentChecker.notNull(cds, "null cds");
    ArgumentChecker.notNull(yieldCurve, "null yieldCurve");
    ArgumentChecker.notNull(creditCurve, "null creditCurve");
    if (cds.getProtectionEnd() <= 0.0) { //short cut already expired CDSs
      return 0.0;
    }

    final double[] integrationSchedule = getIntegrationsPoints(cds.getProtectionStart(), cds.getProtectionEnd(), yieldCurve, creditCurve);

    double ht0 = creditCurve.getRT(integrationSchedule[0]);
    double rt0 = yieldCurve.getRT(integrationSchedule[0]);
    double b0 = Math.exp(-ht0 - rt0); // risky discount factor

    double pv = 0.0;
    final int n = integrationSchedule.length;
    for (int i = 1; i < n; ++i) {

      final double ht1 = creditCurve.getRT(integrationSchedule[i]);
      final double rt1 = yieldCurve.getRT(integrationSchedule[i]);
      final double b1 = Math.exp(-ht1 - rt1);

      final double dht = ht1 - ht0;
      final double drt = rt1 - rt0;
      final double dhrt = dht + drt;

      // The formula has been modified from ISDA (but is equivalent) to avoid log(exp(x)) and explicitly calculating the time
      // step - it also handles the limit
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
    pv *= cds.getLGD();

    // Compute the discount factor discounting the upfront payment made on the cash settlement date back to the valuation date
    final double df = yieldCurve.getDiscountFactor(cds.getValuationTime());
    pv /= df;

    return pv;
  }

  /**
   * This is the present value of the premium leg per unit of fractional spread - hence it is equal to 10,000 times the RPV01
   * (Risky PV01). The actual PV of the leg is this multiplied by the notional and the fractional spread (i.e. spread in basis
   * points divided by 10,000)
   * @param cds analytic description of a CDS traded at a certain time
   * @param yieldCurve The yield (or discount) curve
   * @param creditCurve the credit (or survival) curve
   * @param cleanOrDirty Clean or dirty price
   * @return 10,000 times the RPV01 (on a notional of 1)
   */
  public double pvPremiumLegPerUnitSpread(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final PriceType cleanOrDirty) {
    ArgumentChecker.notNull(cds, "null cds");
    ArgumentChecker.notNull(yieldCurve, "null yieldCurve");
    ArgumentChecker.notNull(creditCurve, "null creditCurve");
    if (cds.getProtectionEnd() <= 0.0) { //short cut already expired CDSs
      return 0.0;
    }
    final double obsOffset = cds.isProtectionFromStartOfDay() ? -cds.getCurveOneDay() : 0.0;

    final int n = cds.getNumPayments();
    double pv = 0.0;
    for (int i = 0; i < n; i++) {
      final double paymentTime = cds.getPaymentTime(i);
      final double creditObsTime = cds.getAccEnd(i) + obsOffset;
      final double q = creditCurve.getDiscountFactor(creditObsTime /*cds.getCreditObservationTime(i)*/);
      final double p = yieldCurve.getDiscountFactor(paymentTime);
      pv += cds.getAccrualFraction(i) * p * q;
    }

    if (cds.isPayAccOnDefault()) {

      final double[] integrationSchedule = getIntegrationsPoints(cds.getAccStart(0), cds.getAccEnd(n - 1), yieldCurve, creditCurve);
      final double offsetStepin = cds.getStepin() + obsOffset;

      double accPV = 0.0;
      for (int i = 0; i < n; i++) {
        final double offsetAccStart = cds.getAccStart(i) + obsOffset;
        final double offsetAccEnd = cds.getAccEnd(i) + obsOffset;
        final double accRate = cds.getAccrualFraction(i) / (offsetAccEnd - offsetAccStart);
        accPV += calculateSinglePeriodAccrualOnDefault(accRate, offsetStepin, offsetAccStart, offsetAccEnd, integrationSchedule, yieldCurve, creditCurve);
      }
      pv += accPV;
    }

    final double df = yieldCurve.getDiscountFactor(cds.getValuationTime());
    pv /= df;

    if (cleanOrDirty == PriceType.CLEAN) {
      pv -= cds.getAccruedPremiumPerUnitSpread();
    }
    return pv;
  }

  private double calculateSinglePeriodAccrualOnDefault(final double accRate, final double stepin, final double accStart, final double accEnd, final double[] integrationPoints,
      final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve) {

    final double start = Math.max(accStart, stepin);
    if (start >= accEnd) {
      return 0.0;
    }
    final double[] knots = truncateSetInclusive(start, accEnd, integrationPoints);

    double t = knots[0];
    double ht0 = creditCurve.getRT(t);
    double rt0 = yieldCurve.getRT(t);
    double b0 = Math.exp(-rt0 - ht0); // this is the risky discount factor

    double t0 = _useCorrectAccOnDefaultFormula ? 0.0 : t - accStart + 1 / 730.0; // TODO not entirely clear why ISDA adds half a day
    double pv = 0.0;
    final int nItems = knots.length;
    for (int j = 1; j < nItems; ++j) {
      t = knots[j];
      final double ht1 = creditCurve.getRT(t);
      final double rt1 = yieldCurve.getRT(t);
      final double b1 = Math.exp(-rt1 - ht1);

      final double dt = knots[j] - knots[j - 1];

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
        // This is a know bug - a fix is proposed by Markit (and appears commented out in ISDA v.1.8.2)
        // This is the correct term plus dht*t0/dhrt*(b0-b1) which is an error
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

  //****************************************************************************************************************************
  // Sensitivities
  //****************************************************************************************************************************

  /**
   * Sensitivity of the present value (for the payer of premiums, i.e. the buyer of protection) to the zero hazard rate
   *  of a given node (knot) of the credit curve. This is per unit of notional
  * @param cds analytic description of a CDS traded at a certain time
   * @param yieldCurve The yield (or discount) curve
   * @param creditCurve the credit (or survival) curve
   * @param fractionalSpread The <b>fraction</b> spread
   * @param creditCurveNode The credit curve node
   * @return PV sensitivity to one node (knot) on the credit (hazard rate/survival) curve
   */
  public double pvCreditSensitivity(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final double fractionalSpread,
      final int creditCurveNode) {
    if (cds.getProtectionEnd() <= 0.0) { //short cut already expired CDSs
      return 0.0;
    }
    final double rpv01Sense = pvPremiumLegCreditSensitivity(cds, yieldCurve, creditCurve, creditCurveNode);
    final double proLegSense = protectionLegCreditSensitivity(cds, yieldCurve, creditCurve, creditCurveNode);
    return proLegSense - fractionalSpread * rpv01Sense;
  }

  /**
   * Sensitivity of the present value (for the payer of premiums, i.e. the buyer of protection) to the zero rate
   *  of a given node (knot) of the yield curve. This is per unit of notional
  * @param cds analytic description of a CDS traded at a certain time
   * @param yieldCurve The yield (or discount) curve
   * @param creditCurve the credit (or survival) curve
   * @param fractionalSpread The <b>fraction</b> spread
   * @param yieldCurveNode The yield curve node
   * @return PV sensitivity to one node (knot) on the yield curve
   */
  public double pvYieldSensitivity(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve,
      final double fractionalSpread, final int yieldCurveNode) {
    if (cds.getProtectionEnd() <= 0.0) { //short cut already expired CDSs
      return 0.0;
    }
    final double rpv01Sense = pvPremiumLegYieldSensitivity(cds, yieldCurve, creditCurve, yieldCurveNode);
    final double proLegSense = protectionLegYieldSensitivity(cds, yieldCurve, creditCurve, yieldCurveNode);
    return proLegSense - fractionalSpread * rpv01Sense;
  }

  /**
   * Sensitivity of the par spread (the fixed payment on the premium leg that make the PV of the CDS zero for a given yield
   * and credit (hazard rate/survival) curve) to the zero hazard rate of a given node (knot) of the credit curve.
  * @param cds analytic description of a CDS traded at a certain time
   * @param yieldCurve The yield (or discount) curve
   * @param creditCurve the credit (or survival) curve
   * @param creditCurveNode The credit curve node
   * @return Par spread sensitivity to one node (knot) on the credit (hazard rate/survival) curve
   */
  public double parSpreadCreditSensitivity(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final int creditCurveNode) {
    if (cds.getProtectionEnd() <= 0.0) { //short cut already expired CDSs
      throw new IllegalArgumentException("CDSs has expired - cannot compute a par spread sensitivity for it");
    }

    final double a = protectionLeg(cds, yieldCurve, creditCurve);
    final double b = pvPremiumLegPerUnitSpread(cds, yieldCurve, creditCurve, PriceType.CLEAN);
    final double spread = a / b;
    final double dadh = protectionLegCreditSensitivity(cds, yieldCurve, creditCurve, creditCurveNode);
    final double dbdh = pvPremiumLegCreditSensitivity(cds, yieldCurve, creditCurve, creditCurveNode);
    return spread * (dadh / a - dbdh / b);
  }

  /**
   * The sensitivity (on a unit notional) of the (scaled) RPV01 to the zero hazard rate of a given node (knot) of the credit curve.
   * @param cds analytic description of a CDS traded at a certain time
   * @param yieldCurve The yield (or discount) curve
   * @param creditCurve the credit (or survival) curve
   * @param creditCurveNode The credit curve node
   * @return  sensitivity (on a unit notional)
   */
  public double pvPremiumLegCreditSensitivity(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final int creditCurveNode) {
    ArgumentChecker.notNull(cds, "null cds");
    ArgumentChecker.notNull(yieldCurve, "null yieldCurve");
    ArgumentChecker.notNull(creditCurve, "null creditCurve");
    if (cds.getProtectionEnd() <= 0.0) { //short cut already expired CDSs
      return 0.0;
    }
    final double obsOffset = cds.isProtectionFromStartOfDay() ? -cds.getCurveOneDay() : 0.0;

    final int n = cds.getNumPayments();
    double pvSense = 0.0;
    for (int i = 0; i < n; i++) {
      final double paymentTime = cds.getPaymentTime(i);
      final double creditObsTime = cds.getAccEnd(i) + obsOffset;
      final double dqdh = creditCurve.getSingleNodeDiscountFactorSensitivity(creditObsTime, creditCurveNode);
      if (dqdh == 0) {
        continue;
      }
      final double p = yieldCurve.getDiscountFactor(paymentTime);
      pvSense += cds.getAccrualFraction(i) * p * dqdh;
    }

    if (cds.isPayAccOnDefault()) {
      final double[] integrationSchedule = getIntegrationsPoints(cds.getAccStart(0), cds.getAccEnd(n - 1), yieldCurve, creditCurve);
      final double offsetStepin = cds.getStepin() + obsOffset;

      double accPVSense = 0.0;
      for (int i = 0; i < n; i++) {
        final double offsetAccStart = cds.getAccStart(i) + obsOffset;
        final double offsetAccEnd = cds.getAccEnd(i) + obsOffset;
        final double accRate = cds.getAccrualFraction(i) / (offsetAccEnd - offsetAccStart);
        accPVSense += calculateSinglePeriodAccrualOnDefaultCreditSensitivity(accRate, offsetStepin, offsetAccStart, offsetAccEnd, integrationSchedule, yieldCurve, creditCurve, creditCurveNode);
      }
      pvSense += accPVSense;
    }

    final double df = yieldCurve.getDiscountFactor(cds.getValuationTime());
    pvSense /= df;
    return pvSense;
  }

  /**
   * The sensitivity (on a unit notional) of the (scaled) RPV01 to the zero hazard rate of a given node (knot) of the credit curve.
   * @param cds analytic description of a CDS traded at a certain time
   * @param yieldCurve The yield (or discount) curve
   * @param creditCurve the credit (or survival) curve
   * @param yieldCurveNode The yield curve node
   * @return  sensitivity (on a unit notional)
   */
  public double pvPremiumLegYieldSensitivity(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final int yieldCurveNode) {
    ArgumentChecker.notNull(cds, "null cds");
    ArgumentChecker.notNull(yieldCurve, "null yieldCurve");
    ArgumentChecker.notNull(creditCurve, "null creditCurve");
    if (cds.getProtectionEnd() <= 0.0) { //short cut already expired CDSs
      return 0.0;
    }
    final double obsOffset = cds.isProtectionFromStartOfDay() ? -cds.getCurveOneDay() : 0.0;

    final int n = cds.getNumPayments();
    double pvSense = 0.0;
    for (int i = 0; i < n; i++) {
      final double paymentTime = cds.getPaymentTime(i);
      final double creditObsTime = cds.getAccEnd(i) + obsOffset;
      final double dpdr = yieldCurve.getSingleNodeDiscountFactorSensitivity(paymentTime, yieldCurveNode);
      if (dpdr == 0) {
        continue;
      }
      final double q = creditCurve.getSurvivalProbability(creditObsTime);
      pvSense += cds.getAccrualFraction(i) * q * dpdr;
    }

    if (cds.isPayAccOnDefault()) {
      final double[] integrationSchedule = getIntegrationsPoints(cds.getAccStart(0), cds.getAccEnd(n - 1), yieldCurve, creditCurve);
      final double offsetStepin = cds.getStepin() + obsOffset;

      double accPVSense = 0.0;
      for (int i = 0; i < n; i++) {
        final double offsetAccStart = cds.getAccStart(i) + obsOffset;
        final double offsetAccEnd = cds.getAccEnd(i) + obsOffset;
        final double accRate = cds.getAccrualFraction(i) / (offsetAccEnd - offsetAccStart);
        accPVSense += calculateSinglePeriodAccrualOnDefaultYieldSensitivity(accRate, offsetStepin, offsetAccStart, offsetAccEnd, integrationSchedule, yieldCurve, creditCurve, yieldCurveNode);
      }
      pvSense += accPVSense;
    }

    final double df = yieldCurve.getDiscountFactor(cds.getValuationTime());
    pvSense /= df;

    //TODO this was put in quickly the get the right sensitivity to the first node
    final double dfSense = yieldCurve.getSingleNodeDiscountFactorSensitivity(cds.getValuationTime(), yieldCurveNode);
    if (dfSense != 0.0) {
      final double pro = pvPremiumLegPerUnitSpread(cds, yieldCurve, creditCurve, PriceType.DIRTY);
      pvSense -= pro / df * dfSense;
    }

    return pvSense;
  }

  private double calculateSinglePeriodAccrualOnDefaultCreditSensitivity(final double accRate, final double stepin, final double accStart, final double accEnd, final double[] integrationPoints,
      final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final int creditCurveNode) {

    final double start = Math.max(accStart, stepin);
    if (start >= accEnd) {
      return 0.0;
    }
    final double[] knots = truncateSetInclusive(start, accEnd, integrationPoints);

    double t = knots[0];
    double ht0 = creditCurve.getRT(t);
    double rt0 = yieldCurve.getRT(t);
    double p0 = Math.exp(-rt0);
    double q0 = Math.exp(-ht0);
    double b0 = p0 * q0; // this is the risky discount factor
    double dqdr0 = creditCurve.getSingleNodeDiscountFactorSensitivity(t, creditCurveNode);

    double t0 = _useCorrectAccOnDefaultFormula ? 0.0 : t - accStart + 1 / 730.0; // TODO not entirely clear why ISDA adds half a day
    double pvSense = 0.0;
    final int nItems = knots.length;
    for (int j = 1; j < nItems; ++j) {
      t = knots[j];
      final double ht1 = creditCurve.getRT(t);
      final double rt1 = yieldCurve.getRT(t);
      final double p1 = Math.exp(-rt1);
      final double q1 = Math.exp(-ht1);
      final double b1 = p1 * q1;
      final double dqdr1 = creditCurve.getSingleNodeDiscountFactorSensitivity(t, creditCurveNode);

      final double dt = knots[j] - knots[j - 1];

      final double dht = ht1 - ht0;
      final double drt = rt1 - rt0;
      final double dhrt = dht + drt + 1e-50; // to keep consistent with ISDA c code

      double tPvSense;
      // TODO once the maths is written up in a white paper, check these formula again, since tests again finite difference
      // could miss some subtle error

      if (_useCorrectAccOnDefaultFormula) {
        if (Math.abs(dhrt) < 1e-5) {
          final double eP = epsilonP(-dhrt);
          final double ePP = epsilonPP(-dhrt);
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
          tPvSense = dPVdq0 * dqdr0 - dPVdq1 * dqdr1;
        }
      } else {
        // this is a know bug - a fix is proposed by Markit (and appears commented out in ISDA v.1.8.2)
        final double t1 = t - accStart + 1 / 730.0;
        if (Math.abs(dhrt) < 1e-5) {
          final double e = epsilon(-dhrt);
          final double eP = epsilonP(-dhrt);
          final double ePP = epsilonPP(-dhrt);
          final double w1 = t0 * e + dt * eP;
          final double w2 = t0 * eP + dt * ePP;
          final double dPVdq0 = p0 * ((1 + dhrt) * w1 - dht * w2);
          final double dPVdq1 = b0 / q1 * (-w1 + dht * w2);
          tPvSense = dPVdq0 * dqdr0 + dPVdq1 * dqdr1;

        } else {
          final double w1 = dt / dhrt;
          final double w2 = dht / dhrt;
          final double w3 = (t0 + w1) * b0 - (t1 + w1) * b1;
          final double w4 = (1 - w2) / dhrt;
          final double w5 = w1 / dhrt * (b0 - b1);
          final double dPVdq0 = w4 * w3 / q0 + w2 * ((t0 + w1) * p0 - w5 / q0);
          final double dPVdq1 = w4 * w3 / q1 + w2 * ((t1 + w1) * p1 - w5 / q1);
          tPvSense = dPVdq0 * dqdr0 - dPVdq1 * dqdr1;
        }
        t0 = t1;
      }

      pvSense += tPvSense;
      ht0 = ht1;
      rt0 = rt1;
      p0 = p1;
      q0 = q1;
      b0 = b1;
      dqdr0 = dqdr1;
    }
    return accRate * pvSense;
  }

  private double calculateSinglePeriodAccrualOnDefaultYieldSensitivity(final double accRate, final double stepin, final double accStart, final double accEnd, final double[] integrationPoints,
      final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final int yieldCurveNode) {
    final double start = Math.max(accStart, stepin);
    if (start >= accEnd) {
      return 0.0;
    }
    if (_useCorrectAccOnDefaultFormula == false) {
      throw new NotImplementedException();
    }

    final double[] knots = truncateSetInclusive(start, accEnd, integrationPoints);

    double t = knots[0];
    double ht0 = creditCurve.getRT(t);
    double rt0 = yieldCurve.getRT(t);
    double p0 = Math.exp(-rt0);
    double q0 = Math.exp(-ht0);
    double b0 = p0 * q0; // this is the risky discount factor
    double dpdr0 = yieldCurve.getSingleNodeDiscountFactorSensitivity(t, yieldCurveNode);

    final double t0 = 0.0;
    double pvSense = 0.0;
    final int nItems = knots.length;
    for (int j = 1; j < nItems; ++j) {
      t = knots[j];
      final double ht1 = creditCurve.getRT(t);
      final double rt1 = yieldCurve.getRT(t);
      final double p1 = Math.exp(-rt1);
      final double q1 = Math.exp(-ht1);
      final double b1 = p1 * q1;
      final double dpdr1 = yieldCurve.getSingleNodeDiscountFactorSensitivity(t, yieldCurveNode);

      final double dt = knots[j] - knots[j - 1];

      final double dht = ht1 - ht0;
      final double drt = rt1 - rt0;
      final double dhrt = dht + drt;

      double tPvSense;
      // TODO once the maths is written up in a white paper, check these formula again, since tests again finite difference
      // could miss some subtle error

      //  if (Math.abs(dhrt) < 1e-5) {
      final double eP = epsilonP(-dhrt);
      final double ePP = epsilonPP(-dhrt);
      final double dPVdp0 = q0 * dt * dht * (eP - ePP);
      final double dPVdp1 = b0 * dt * dht / p1 * ePP;
      tPvSense = dPVdp0 * dpdr0 + dPVdp1 * dpdr1;
      //        } else {
      //          final double w1 = (b0 - b1) / dhrt;
      //          final double w2 = w1 - b1;
      //          final double w3 = dht / dhrt;
      //          final double w4 = dt / dhrt;
      //          final double w5 = (1 - w3) * w2;
      //          final double dPVdq0 = w4 / q0 * (w5 + w3 * (b0 - w1));
      //          final double dPVdq1 = w4 / q1 * (w5 + w3 * (b1 * (1 + dhrt) - w1));
      //          tPvSense = dPVdq0 * dqdr0 - dPVdq1 * dqdr1;
      //        }

      pvSense += tPvSense;
      ht0 = ht1;
      rt0 = rt1;
      p0 = p1;
      q0 = q1;
      b0 = b1;
      dpdr0 = dpdr1;
    }
    return accRate * pvSense;
  }

  /**
   * The sensitivity of the PV of the protection leg to the zero hazard rate of a given node (knot) of the credit curve.
   * @param cds analytic description of a CDS traded at a certain time
   * @param yieldCurve The yield (or discount) curve
   * @param creditCurve the credit (or survival) curve
   * @param creditCurveNode The credit curve node
   * @return  sensitivity (on a unit notional)
   */
  public double protectionLegCreditSensitivity(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final int creditCurveNode) {
    ArgumentChecker.notNull(cds, "null cds");
    ArgumentChecker.notNull(yieldCurve, "null yieldCurve");
    ArgumentChecker.notNull(creditCurve, "null creditCurve");
    ArgumentChecker.isTrue(creditCurveNode >= 0 && creditCurveNode < creditCurve.getNumberOfKnots(), "creditCurveNode out of range");
    if ((creditCurveNode != 0 && cds.getProtectionEnd() <= creditCurve.getTimeAtIndex(creditCurveNode - 1)) ||
        (creditCurveNode != creditCurve.getNumberOfKnots() - 1 && cds.getProtectionStart() >= creditCurve.getTimeAtIndex(creditCurveNode + 1))) {
      return 0.0; // can't have any sensitivity in this case
    }
    if (cds.getProtectionEnd() <= 0.0) { //short cut already expired CDSs
      return 0.0;
    }

    final double[] integrationSchedule = getIntegrationsPoints(cds.getProtectionStart(), cds.getProtectionEnd(), yieldCurve, creditCurve);

    double t = integrationSchedule[0];
    double ht0 = creditCurve.getRT(t);
    double rt0 = yieldCurve.getRT(t);
    double dqdr0 = creditCurve.getSingleNodeDiscountFactorSensitivity(t, creditCurveNode);
    double q0 = Math.exp(-ht0);
    double p0 = Math.exp(-rt0);
    // double pv = 0.0;
    double pvSense = 0.0;
    final int n = integrationSchedule.length;
    for (int i = 1; i < n; ++i) {

      t = integrationSchedule[i];
      final double ht1 = creditCurve.getRT(t);
      final double dqdr1 = creditCurve.getSingleNodeDiscountFactorSensitivity(t, creditCurveNode);
      final double rt1 = yieldCurve.getRT(t);
      final double q1 = Math.exp(-ht1);
      final double p1 = Math.exp(-rt1);

      if (dqdr0 == 0.0 && dqdr1 == 0.0) {
        ht0 = ht1;
        rt0 = rt1;
        p0 = p1;
        q0 = q1;
        continue;
      }

      final double hBar = ht1 - ht0;
      final double fBar = rt1 - rt0;
      final double fhBar = hBar + fBar;

      double dPVSense;
      if (Math.abs(fhBar) < 1e-5) {
        final double e = epsilon(-fhBar);
        final double eP = epsilonP(-fhBar);
        final double dPVdq0 = p0 * ((1 + hBar) * e - hBar * eP);
        final double dPVdq1 = -p0 * q0 / q1 * (e - hBar * eP);
        dPVSense = dPVdq0 * dqdr0 + dPVdq1 * dqdr1;
      } else {
        final double w = fBar / fhBar * (p0 * q0 - p1 * q1);
        dPVSense = ((w / q0 + hBar * p0) / fhBar) * dqdr0 - ((w / q1 + hBar * p1) / fhBar) * dqdr1;
      }

      pvSense += dPVSense;

      ht0 = ht1;
      dqdr0 = dqdr1;
      rt0 = rt1;
      p0 = p1;
      q0 = q1;

    }
    pvSense *= cds.getLGD();

    // Compute the discount factor discounting the upfront payment made on the cash settlement date back to the valuation date
    final double df = yieldCurve.getDiscountFactor(cds.getValuationTime());

    pvSense /= df;

    return pvSense;
  }

  /**
   * The sensitivity of the PV of the protection leg to the zero rate of a given node (knot) of the yield curve.
   * @param cds analytic description of a CDS traded at a certain time
   * @param yieldCurve The yield (or discount) curve
   * @param creditCurve the credit (or survival) curve
   * @param yieldCurveNode The yield curve node
   * @return  sensitivity (on a unit notional)
   */
  public double protectionLegYieldSensitivity(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final int yieldCurveNode) {
    ArgumentChecker.notNull(cds, "null cds");
    ArgumentChecker.notNull(yieldCurve, "null yieldCurve");
    ArgumentChecker.notNull(creditCurve, "null creditCurve");
    ArgumentChecker.isTrue(yieldCurveNode >= 0 && yieldCurveNode < yieldCurve.getNumberOfKnots(), "yieldCurveNode out of range");
    if ((yieldCurveNode != 0 && cds.getProtectionEnd() <= yieldCurve.getTimeAtIndex(yieldCurveNode - 1)) ||
        (yieldCurveNode != creditCurve.getNumberOfKnots() - 1 && cds.getProtectionStart() >= yieldCurve.getTimeAtIndex(yieldCurveNode + 1))) {
      return 0.0; // can't have any sensitivity in this case
    }
    if (cds.getProtectionEnd() <= 0.0) { //short cut already expired CDSs
      return 0.0;
    }

    final double[] integrationSchedule = getIntegrationsPoints(cds.getProtectionStart(), cds.getProtectionEnd(), yieldCurve, creditCurve);

    double t = integrationSchedule[0];
    double ht0 = creditCurve.getRT(t);
    double rt0 = yieldCurve.getRT(t);
    double dpdr0 = yieldCurve.getSingleNodeDiscountFactorSensitivity(t, yieldCurveNode);
    double q0 = Math.exp(-ht0);
    double p0 = Math.exp(-rt0);
    // double pv = 0.0;
    double pvSense = 0.0;
    final int n = integrationSchedule.length;
    for (int i = 1; i < n; ++i) {

      t = integrationSchedule[i];
      final double ht1 = creditCurve.getRT(t);
      final double dpdr1 = yieldCurve.getSingleNodeDiscountFactorSensitivity(t, yieldCurveNode);
      final double rt1 = yieldCurve.getRT(t);
      final double q1 = Math.exp(-ht1);
      final double p1 = Math.exp(-rt1);

      if (dpdr0 == 0.0 && dpdr1 == 0.0) {
        ht0 = ht1;
        rt0 = rt1;
        p0 = p1;
        q0 = q1;
        continue;
      }

      final double hBar = ht1 - ht0;
      final double fBar = rt1 - rt0;
      final double fhBar = hBar + fBar;

      double dPVSense;
      //  if (Math.abs(fhBar) < 1e-5) {
      // throw new NotImplementedException();
      final double e = epsilon(-fhBar);
      final double eP = epsilonP(-fhBar);
      final double dPVdp0 = q0 * hBar * (e - eP);
      final double dPVdp1 = hBar * p0 * q0 / p1 * eP;
      dPVSense = dPVdp0 * dpdr0 + dPVdp1 * dpdr1;
      //      } else {
      //        final double w1 = hBar / fhBar;
      //        final double w2 = (p0 * q0 - p1 * q1) / fhBar;
      //        dPVSense = w1 * ((-w2 / p0 + q0) * dpdr0 - (w2 / p1 - q1) * dpdr1);
      //      }

      pvSense += dPVSense;

      ht0 = ht1;
      dpdr0 = dpdr1;
      rt0 = rt1;
      p0 = p1;
      q0 = q1;

    }
    pvSense *= cds.getLGD();

    // Compute the discount factor discounting the upfront payment made on the cash settlement date back to the valuation date
    final double df = yieldCurve.getDiscountFactor(cds.getValuationTime());

    pvSense /= df;

    //TODO this was put in quickly the get the right sensitivity to the first node
    final double dfSense = yieldCurve.getSingleNodeDiscountFactorSensitivity(cds.getValuationTime(), yieldCurveNode);
    if (dfSense != 0.0) {
      final double pro = protectionLeg(cds, yieldCurve, creditCurve);
      pvSense -= pro / df * dfSense;
    }

    return pvSense;
  }

}
