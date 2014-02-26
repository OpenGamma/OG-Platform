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

import java.util.Arrays;

import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class MultiAnalyticCDSPricer {
  private static final double HALFDAY = 1 / 730.;
  /** Default value for determining if results consistent with ISDA model versions 1.8.2 or lower are to be calculated */
  private static final AccrualOnDefaultFormulae DEFAULT_FORMULA = AccrualOnDefaultFormulae.OrignalISDA;
  /** True if results consistent with ISDA model versions 1.8.2 or lower are to be calculated */
  private final AccrualOnDefaultFormulae _formula;
  private final double _omega;

  /**
   * For consistency with the ISDA model version 1.8.2 and lower, a bug in the accrual on default calculation
   * has been reproduced.
   */
  public MultiAnalyticCDSPricer() {
    _formula = DEFAULT_FORMULA;
    _omega = HALFDAY;
  }

  /**
   *  For consistency with the ISDA model version 1.8.2 and lower, a bug in the accrual on default calculation
   * has been reproduced.
   * @param formula which accrual on default formulae to use.
   */
  public MultiAnalyticCDSPricer(final AccrualOnDefaultFormulae formula) {
    ArgumentChecker.notNull(formula, "formula");
    _formula = formula;
    if (formula == AccrualOnDefaultFormulae.OrignalISDA) {
      _omega = HALFDAY;
    } else {
      _omega = 0.0;
    }
  }

  /**
   * Present value for the payer of premiums (i.e. the buyer of protection)
   * @param cds analytic description of a CDS traded at a certain time
   * @param yieldCurve The yield (or discount) curve
   * @param creditCurve the credit (or survival) curve
   * @param premium The common CDS premium (as a fraction)
   * @param cleanOrDirty Clean or dirty price
   * @return The PV on unit notional
   */
  public double[] pv(final MultiCDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final double premium, final PriceType cleanOrDirty) {
    final int n = cds.getNumMaturities();
    final double[] premiums = new double[n];
    Arrays.fill(premiums, premium);
    return pv(cds, yieldCurve, creditCurve, premiums, cleanOrDirty);
  }

  /**
   * Present value for the payer of premiums (i.e. the buyer of protection)
   * @param cds analytic description of a CDS traded at a certain time
   * @param yieldCurve The yield (or discount) curve
   * @param creditCurve the credit (or survival) curve
   * @param premiums The CDS premiums (as fractions)
   * @param cleanOrDirty Clean or dirty price
   * @return The PV on unit notional
   */
  public double[] pv(final MultiCDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final double[] premiums, final PriceType cleanOrDirty) {
    final int n = cds.getNumMaturities();
    ArgumentChecker.notEmpty(premiums, "premiums");
    ArgumentChecker.isTrue(n == premiums.length, "premiums wrong length. Should be {}, but is {}", n, premiums.length);
    final double[] pv = new double[n];

    if (cds.getProtectionEnd(cds.getNumMaturities() - 1) <= 0.0) { //all CDSs have expired
      return pv;
    }
    // TODO check for any repeat calculations
    final double[] rpv01 = pvPremiumLegPerUnitSpread(cds, yieldCurve, creditCurve, cleanOrDirty);
    final double[] proLeg = protectionLeg(cds, yieldCurve, creditCurve);
    for (int i = 0; i < n; i++) {
      pv[i] = proLeg[i] - premiums[i] * rpv01[i];
    }
    return pv;
  }

  /**
   * Present value (clean price) for the payer of premiums (i.e. the buyer of protection)
   * @param cds analytic description of a CDS traded at a certain time
   * @param yieldCurve The yield (or discount) curve
   * @param creditCurve the credit (or survival) curve
   * @param premiums The CDS premiums (as fractions)
   * @return The PV
   */
  public double[] pv(final MultiCDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final double[] premiums) {
    return pv(cds, yieldCurve, creditCurve, premiums, PriceType.CLEAN);
  }

  /**
   * Present value (clean price) for the payer of premiums (i.e. the buyer of protection)
  * @param cds analytic description of a CDS traded at a certain time
   * @param yieldCurve The yield (or discount) curve
   * @param creditCurve the credit (or survival) curve
   * @param premium The common CDS premium (as a fraction)
   * @return The PV
   */
  public double[] pv(final MultiCDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final double premium) {
    return pv(cds, yieldCurve, creditCurve, premium, PriceType.CLEAN);
  }

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
  //  public double[] pvCreditSensitivity(final MultiCDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final int creditCurveNode) {
  //    if (cds.getProtectionEnd(cds.getNumMaturities() - 1) <= 0.0) { //all CDSs have expired
  //      return 0.0;
  //    }
  //    final double rpv01Sense = pvPremiumLegCreditSensitivity(cds, yieldCurve, creditCurve, creditCurveNode);
  //    final double proLegSense = protectionLegCreditSensitivity(cds, yieldCurve, creditCurve, creditCurveNode);
  //    return proLegSense - fractionalSpread * rpv01Sense;
  //  }

  /**
   * The par spread par spread for a given yield and credit (hazard rate/survival) curve)
   * @param cds analytic description of a CDS traded at a certain time
   * @param yieldCurve The yield (or discount) curve
   * @param creditCurve the credit (or survival) curve
   * @return the par spread
   */
  public double[] parSpread(final MultiCDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve) {
    if (cds.getProtectionEnd(0) <= 0.0) { //short cut already expired CDSs
      throw new IllegalArgumentException("A CDSs has expired - cannot compute a par spread for it");
    }

    final double[] rpv01 = pvPremiumLegPerUnitSpread(cds, yieldCurve, creditCurve, PriceType.CLEAN);
    final double[] proLeg = protectionLeg(cds, yieldCurve, creditCurve);
    final int n = cds.getNumMaturities();
    final double[] s = new double[n];
    for (int i = 0; i < n; i++) {
      s[i] = proLeg[i] / rpv01[i];
    }

    return s;
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
  //  public double parSpreadCreditSensitivity(final MultiCDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final int creditCurveNode) {
  //    if (cds.getProtectionEnd(0) <= 0.0) { //short cut already expired CDSs
  //      throw new IllegalArgumentException("A CDSs has expired - cannot compute a par spread for it");
  //    }
  //
  //    final double a = protectionLeg(cds, yieldCurve, creditCurve);
  //    final double b = pvPremiumLegPerUnitSpread(cds, yieldCurve, creditCurve, PriceType.CLEAN);
  //    final double spread = a / b;
  //    final double dadh = protectionLegCreditSensitivity(cds, yieldCurve, creditCurve, creditCurveNode);
  //    final double dbdh = pvPremiumLegCreditSensitivity(cds, yieldCurve, creditCurve, creditCurveNode);
  //    return spread * (dadh / a - dbdh / b);
  //  }

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
  public double[] pvPremiumLegPerUnitSpread(final MultiCDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final PriceType cleanOrDirty) {
    ArgumentChecker.notNull(cds, "null cds");
    ArgumentChecker.notNull(yieldCurve, "null yieldCurve");
    ArgumentChecker.notNull(creditCurve, "null creditCurve");

    double[] integrationSchedule = null;
    final int nMat = cds.getNumMaturities();
    if (cds.isPayAccOnDefault()) {
      integrationSchedule = getIntegrationsPoints(cds.getEffectiveProtectionStart(), cds.getProtectionEnd(nMat - 1), yieldCurve, creditCurve);
    }
    final double df = yieldCurve.getDiscountFactor(cds.getCashSettleTime());

    final double[] pv = new double[nMat];
    int start = 0;
    double runningPV = 0;
    for (int matIndex = 0; matIndex < nMat; matIndex++) {
      if (cds.getProtectionEnd(matIndex) <= 0.0) { //skip expired CDSs (they have zero pv)
        continue;
      }

      final int end = cds.getPaymentIndexForMaturity(matIndex);
      for (int i = start; i < end; i++) {
        final CDSCoupon coupon = cds.getStandardCoupon(i);
        final double q = creditCurve.getDiscountFactor(coupon.getEffEnd());
        final double p = yieldCurve.getDiscountFactor(coupon.getPaymentTime());
        runningPV += coupon.getYearFrac() * p * q;
      }

      if (cds.isPayAccOnDefault()) {
        double accPV = 0;
        for (int i = start; i < end; i++) {
          final CDSCoupon coupon = cds.getStandardCoupon(i);
          accPV += calculateSinglePeriodAccrualOnDefault(coupon, cds.getEffectiveProtectionStart(), integrationSchedule, yieldCurve, creditCurve);
        }
        runningPV += accPV;
      }

      double pvMat = runningPV;
      final CDSCoupon terminalCoupon = cds.getTerminalCoupon(matIndex);
      final double q = creditCurve.getDiscountFactor(terminalCoupon.getEffEnd());
      final double p = yieldCurve.getDiscountFactor(terminalCoupon.getPaymentTime());
      pvMat += terminalCoupon.getYearFrac() * p * q;
      if (cds.isPayAccOnDefault()) {
        pvMat += calculateSinglePeriodAccrualOnDefault(terminalCoupon, cds.getEffectiveProtectionStart(), integrationSchedule, yieldCurve, creditCurve);
      }

      pv[matIndex] = pvMat / df;
      if (cleanOrDirty == PriceType.CLEAN) {
        pv[matIndex] -= cds.getAccruedPremiumPerUnitSpread(matIndex);
      }
      start = Math.max(0, end);
    }

    return pv;
  }

  /**
   * The sensitivity (on a unit notional) of the (scaled) RPV01 to the zero hazard rate of a given node (knot) of the credit curve.
   * @param cds analytic description of a CDS traded at a certain time
   * @param yieldCurve The yield (or discount) curve
   * @param creditCurve the credit (or survival) curve
   * @param creditCurveNode The credit curve node
   * @return  sensitivity (on a unit notional)
   */
  //  public double pvPremiumLegCreditSensitivity(final MultiCDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final int creditCurveNode) {
  //    ArgumentChecker.notNull(cds, "null cds");
  //    ArgumentChecker.notNull(yieldCurve, "null yieldCurve");
  //    ArgumentChecker.notNull(creditCurve, "null creditCurve");
  //
  //    final int n = cds.getNumPayments();
  //    double pvSense = 0.0;
  //    for (int i = 0; i < n; i++) {
  //      final double dqdh = creditCurve.getSingleNodeDiscountFactorSensitivity(cds.getCreditObservationTime(i), creditCurveNode);
  //      final double p = yieldCurve.getDiscountFactor(cds.getPaymentTime(i));
  //      pvSense += cds.getAccrualFraction(i) * p * dqdh;
  //    }
  //
  //    if (cds.isPayAccOnDefault()) {
  //      final double offset = cds.isProtectionFromStartOfDay() ? -cds.getCurveOneDay() : 0.0;
  //      final double[] integrationSchedule = getIntegrationsPoints(cds.getAccStart(0), cds.getAccEnd(n - 1), yieldCurve, creditCurve);
  //      final double offsetStepin = cds.getStepin() + offset;
  //
  //      double accPVSense = 0.0;
  //      for (int i = 0; i < n; i++) {
  //        final double offsetAccStart = cds.getAccStart(i) + offset;
  //        final double offsetAccEnd = cds.getAccEnd(i) + offset;
  //        final double accRate = cds.getAccrualFraction(i) / (offsetAccEnd - offsetAccStart);
  //        accPVSense += calculateSinglePeriodAccrualOnDefaultSensitivity(accRate, offsetStepin, offsetAccStart, offsetAccEnd, integrationSchedule, yieldCurve, creditCurve, creditCurveNode);
  //      }
  //      pvSense += accPVSense;
  //    }
  //
  //    final double df = yieldCurve.getDiscountFactor(cds.getValuationTime());
  //    pvSense /= df;
  //    return pvSense;
  //  }

  //TODO this is identical to the function in AnalyticCDSPricer 
  private double calculateSinglePeriodAccrualOnDefault(final CDSCoupon coupon, final double stepin, final double[] integrationPoints, final ISDACompliantYieldCurve yieldCurve,
      final ISDACompliantCreditCurve creditCurve) {

    final double start = Math.max(coupon.getEffStart(), stepin);
    if (start >= coupon.getEffEnd()) {
      return 0.0; //this coupon has already expired 
    }
    final double[] knots = truncateSetInclusive(start, coupon.getEffEnd(), integrationPoints);

    double t = knots[0];
    double ht0 = creditCurve.getRT(t);
    double rt0 = yieldCurve.getRT(t);
    double b0 = Math.exp(-rt0 - ht0); // this is the risky discount factor

    double t0 = t - coupon.getEffStart() + _omega;
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
      if (_formula == AccrualOnDefaultFormulae.MarkitFix) {
        if (Math.abs(dhrt) < 1e-5) {
          tPV = dht * dt * b0 * epsilonP(-dhrt);
        } else {
          tPV = dht * dt / dhrt * ((b0 - b1) / dhrt - b1);
        }
      } else {
        final double t1 = t - coupon.getEffStart() + _omega;
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
    return coupon.getYFRatio() * pv;
  }

  //  private double calculateSinglePeriodAccrualOnDefaultSensitivity(final double accRate, final double stepin, final double accStart, final double accEnd, final double[] integrationPoints,
  //      final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final int creditCurveNode) {
  //
  //    final double start = Math.max(accStart, stepin);
  //    if (start >= accEnd) {
  //      return 0.0;
  //    }
  //    final double[] knots = truncateSetInclusive(start, accEnd, integrationPoints);
  //
  //    double t = knots[0];
  //    double ht0 = creditCurve.getRT(t);
  //    double rt0 = yieldCurve.getRT(t);
  //    double p0 = Math.exp(-rt0);
  //    double q0 = Math.exp(-ht0);
  //    double b0 = p0 * q0; // this is the risky discount factor
  //    double dqdr0 = creditCurve.getSingleNodeDiscountFactorSensitivity(t, creditCurveNode);
  //
  //    double t0 = t - accStart + _omega;
  //    double pvSense = 0.0;
  //    final int nItems = knots.length;
  //    for (int j = 1; j < nItems; ++j) {
  //      t = knots[j];
  //      final double ht1 = creditCurve.getRT(t);
  //      final double rt1 = yieldCurve.getRT(t);
  //      final double p1 = Math.exp(-rt1);
  //      final double q1 = Math.exp(-ht1);
  //      final double b1 = p1 * q1;
  //      final double dqdr1 = creditCurve.getSingleNodeDiscountFactorSensitivity(t, creditCurveNode);
  //
  //      final double dt = knots[j] - knots[j - 1];
  //
  //      final double dht = ht1 - ht0;
  //      final double drt = rt1 - rt0;
  //      final double dhrt = dht + drt + 1e-50; // to keep consistent with ISDA c code
  //
  //      double tPvSense;
  //      // TODO once the maths is written up in a white paper, check these formula again, since tests again finite difference
  //      // could miss some subtle error
  //
  //      if (_formula == AccrualOnDefaultFormulae.MarkitFix) {
  //        if (Math.abs(dhrt) < 1e-5) {
  //          final double eP = epsilonP(-dhrt);
  //          final double ePP = epsilonPP(-dhrt);
  //          final double dPVdq0 = p0 * dt * ((1 + dht) * eP - dht * ePP);
  //          final double dPVdq1 = b0 * dt / q1 * (-eP + dht * ePP);
  //          tPvSense = dPVdq0 * dqdr0 + dPVdq1 * dqdr1;
  //        } else {
  //          final double w5 = (b0 - b1) / dhrt;
  //          final double w1 = w5 - b1;
  //          final double w2 = dht / dhrt;
  //          final double w3 = dt / dhrt;
  //          final double w4 = (1 - w2) * w1;
  //          final double dPVdq0 = w3 / q0 * (w4 + w2 * (b0 - w5));
  //          final double dPVdq1 = w3 / q1 * (w4 + w2 * (b1 * (1 + dhrt) - w5));
  //          tPvSense = dPVdq0 * dqdr0 - dPVdq1 * dqdr1;
  //        }
  //      } else {
  //        final double t1 = t - accStart + _omega;
  //        if (Math.abs(dhrt) < 1e-5) {
  //          final double e = epsilon(-dhrt);
  //          final double eP = epsilonP(-dhrt);
  //          final double ePP = epsilonPP(-dhrt);
  //          final double w1 = t0 * e + dt * eP;
  //          final double w2 = t0 * eP + dt * ePP;
  //          final double dPVdq0 = p0 * ((1 + dhrt) * w1 - dht * w2);
  //          final double dPVdq1 = b0 / q1 * (-w1 + dht * w2);
  //          tPvSense = dPVdq0 * dqdr0 + dPVdq1 * dqdr1;
  //
  //        } else {
  //          final double w1 = dt / dhrt;
  //          final double w2 = dht / dhrt;
  //          final double w3 = (t0 + w1) * b0 - (t1 + w1) * b1;
  //          final double w4 = (1 - w2) / dhrt;
  //          final double w5 = w1 / dhrt * (b0 - b1);
  //          final double dPVdq0 = w4 * w3 / q0 + w2 * ((t0 + w1) * p0 - w5 / q0);
  //          final double dPVdq1 = w4 * w3 / q1 + w2 * ((t1 + w1) * p1 - w5 / q1);
  //          tPvSense = dPVdq0 * dqdr0 - dPVdq1 * dqdr1;
  //        }
  //        t0 = t1;
  //      }
  //
  //      pvSense += tPvSense;
  //      ht0 = ht1;
  //      rt0 = rt1;
  //      p0 = p1;
  //      q0 = q1;
  //      b0 = b1;
  //      dqdr0 = dqdr1;
  //    }
  //    return accRate * pvSense;
  //  }

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
  public double[] protectionLeg(final MultiCDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve) {
    ArgumentChecker.notNull(cds, "null cds");
    ArgumentChecker.notNull(yieldCurve, "null yieldCurve");
    ArgumentChecker.notNull(creditCurve, "null creditCurve");

    // Compute the discount factor discounting the upfront payment made on the cash settlement date back to the valuation date
    final double df = yieldCurve.getDiscountFactor(cds.getCashSettleTime());
    final double factor = cds.getLGD() / df;
    final int nMat = cds.getNumMaturities();
    double start = cds.getEffectiveProtectionStart();
    final double[] fullIntegrationSchedule = getIntegrationsPoints(start, cds.getProtectionEnd(nMat - 1), yieldCurve, creditCurve);
    final double[] pv = new double[nMat];
    double runningPV = 0;
    for (int matIndex = 0; matIndex < nMat; matIndex++) {
      final double end = cds.getProtectionEnd(matIndex);
      if (end <= 0.0) {
        continue; //short cut already expired CDSs
      }

      final double[] integrationSchedule = truncateSetInclusive(start, end, fullIntegrationSchedule);
      runningPV += protectionLegInterval(integrationSchedule, yieldCurve, creditCurve);
      pv[matIndex] = runningPV * factor;
      start = end;
    }

    return pv;
  }

  private double protectionLegInterval(final double[] integrationSchedule, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve) {
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
    return pv;
  }

  /**
   * The sensitivity of the PV of the protection leg to the zero hazard rate of a given node (knot) of the credit curve.
   * @param cds analytic description of a CDS traded at a certain time
   * @param yieldCurve The yield (or discount) curve
   * @param creditCurve the credit (or survival) curve
   * @param creditCurveNode The credit curve node
   * @return  sensitivity (on a unit notional)
   */
  //  public double protectionLegCreditSensitivity(final MultiCDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final int creditCurveNode) {
  //    ArgumentChecker.notNull(cds, "null cds");
  //    ArgumentChecker.notNull(yieldCurve, "null yieldCurve");
  //    ArgumentChecker.notNull(creditCurve, "null creditCurve");
  //    ArgumentChecker.isTrue(creditCurveNode >= 0 && creditCurveNode < creditCurve.getNumberOfKnots(), "creditCurveNode out of range");
  //    if ((creditCurveNode != 0 && cds.getProtectionEnd() <= creditCurve.getTimeAtIndex(creditCurveNode - 1)) ||
  //        (creditCurveNode != creditCurve.getNumberOfKnots() - 1 && cds.getProtectionStart() >= creditCurve.getTimeAtIndex(creditCurveNode + 1))) {
  //      return 0.0; // can't have any sensitivity in this case
  //    }
  //    if (cds.getProtectionEnd() <= 0.0) { //short cut already expired CDSs
  //      return 0.0;
  //    }
  //
  //    final double[] integrationSchedule = getIntegrationsPoints(cds.getProtectionStart(), cds.getProtectionEnd(), yieldCurve, creditCurve);
  //
  //    double t = integrationSchedule[0];
  //    double ht0 = creditCurve.getRT(t);
  //    double rt0 = yieldCurve.getRT(t);
  //    double dqdr0 = creditCurve.getSingleNodeDiscountFactorSensitivity(t, creditCurveNode);
  //    double q0 = Math.exp(-ht0);
  //    double p0 = Math.exp(-rt0);
  //    // double pv = 0.0;
  //    double pvSense = 0.0;
  //    final int n = integrationSchedule.length;
  //    for (int i = 1; i < n; ++i) {
  //
  //      t = integrationSchedule[i];
  //      final double ht1 = creditCurve.getRT(t);
  //      final double dqdr1 = creditCurve.getSingleNodeDiscountFactorSensitivity(t, creditCurveNode);
  //      final double rt1 = yieldCurve.getRT(t);
  //      final double q1 = Math.exp(-ht1);
  //      final double p1 = Math.exp(-rt1);
  //
  //      if (dqdr0 == 0.0 && dqdr1 == 0.0) {
  //        ht0 = ht1;
  //        rt0 = rt1;
  //        p0 = p1;
  //        q0 = q1;
  //        continue;
  //      }
  //
  //      final double dht = ht1 - ht0;
  //      final double drt = rt1 - rt0;
  //      final double dhrt = dht + drt;
  //
  //      double dPVSense;
  //      if (Math.abs(dhrt) < 1e-5) {
  //        final double e = epsilon(-dhrt);
  //        final double eP = epsilonP(-dhrt);
  //        final double dPVdq0 = p0 * ((1 + dht) * e - dht * eP);
  //        final double dPVdq1 = -p0 * q0 / q1 * (e - dht * eP);
  //        dPVSense = dPVdq0 * dqdr0 + dPVdq1 * dqdr1;
  //      } else {
  //        final double w2 = dht / dhrt;
  //        final double w3 = (1 - w2) * (p0 * q0 - p1 * q1);
  //        dPVSense = ((w3 / q0 + dht * p0) / dhrt) * dqdr0 - ((w3 / q1 + dht * p1) / dhrt) * dqdr1;
  //      }
  //
  //      pvSense += dPVSense;
  //
  //      ht0 = ht1;
  //      dqdr0 = dqdr1;
  //      rt0 = rt1;
  //      p0 = p1;
  //      q0 = q1;
  //
  //    }
  //    pvSense *= cds.getLGD();
  //
  //    // Compute the discount factor discounting the upfront payment made on the cash settlement date back to the valuation date
  //    final double df = yieldCurve.getDiscountFactor(cds.getValuationTime());
  //
  //    pvSense /= df;
  //
  //    return pvSense;
  //  }

}
