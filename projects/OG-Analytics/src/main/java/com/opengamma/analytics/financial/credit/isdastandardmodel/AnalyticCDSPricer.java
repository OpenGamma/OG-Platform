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
import static com.opengamma.analytics.math.utilities.Epsilon.epsilonPP;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class AnalyticCDSPricer {

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
  public AnalyticCDSPricer() {
    _formula = DEFAULT_FORMULA;
    _omega = HALFDAY;
  }

  /**
   * Which formula to use for the accrued on default calculation.  
   * @param formula Options are the formula given in the ISDA model (version 1.8.2 and lower); the proposed fix by Markit (given as a comment in  
   * version 1.8.2, or the mathematically correct formula 
   */
  public AnalyticCDSPricer(final AccrualOnDefaultFormulae formula) {
    ArgumentChecker.notNull(formula, "formula");
    _formula = formula;
    if (_formula == AccrualOnDefaultFormulae.OrignalISDA) {
      _omega = HALFDAY;
    } else {
      _omega = 0.0;
    }
  }

  /**
   * CDS value for the payer of premiums (i.e. the buyer of protection) at the cash-settle date
   * @param cds analytic description of a CDS traded at a certain time
   * @param yieldCurve The yield (or discount) curve
   * @param creditCurve the credit (or survival) curve
   * @param fractionalSpread The <b>fraction</b> spread
   * @param cleanOrDirty Clean or dirty price
   * @return  Value of a unit notional payer CDS on the cash-settle date 
   */
  public double pv(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final double fractionalSpread, final PriceType cleanOrDirty) {
    ArgumentChecker.notNull(cds, "cds");
    if (cds.getProtectionEnd() <= 0.0) { //short cut already expired CDSs
      return 0.0;
    }
    // TODO check for any repeat calculations
    final double rpv01 = annuity(cds, yieldCurve, creditCurve, cleanOrDirty);
    final double proLeg = protectionLeg(cds, yieldCurve, creditCurve);
    return proLeg - fractionalSpread * rpv01;
  }

  /**
   * CDS value for the payer of premiums (i.e. the buyer of protection) at the specified valuation time 
   * @param cds analytic description of a CDS traded at a certain time
   * @param yieldCurve The yield (or discount) curve
   * @param creditCurve the credit (or survival) curve
   * @param fractionalSpread The <b>fraction</b> spread
   * @param cleanOrDirty Clean or dirty price
   * @param valuationTime The valuation time. If time is zero, leg is valued today. Value often quoted for cash-settlement date. 
   * @return Value of a unit notional payer CDS at the specified valuation time
   */
  public double pv(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final double fractionalSpread, final PriceType cleanOrDirty,
      final double valuationTime) {
    ArgumentChecker.notNull(cds, "cds");
    if (cds.getProtectionEnd() <= 0.0) { //short cut already expired CDSs
      return 0.0;
    }

    final double rpv01 = annuity(cds, yieldCurve, creditCurve, cleanOrDirty, 0.0);
    final double proLeg = protectionLeg(cds, yieldCurve, creditCurve, 0.0);
    final double df = yieldCurve.getDiscountFactor(valuationTime);
    return (proLeg - fractionalSpread * rpv01) / df;
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
    ArgumentChecker.notNull(cds, "cds");
    if (cds.getProtectionEnd() <= 0.0) { //short cut already expired CDSs
      throw new IllegalArgumentException("CDSs has expired - cannot compute a par spread for it");
    }

    final double rpv01 = annuity(cds, yieldCurve, creditCurve, PriceType.CLEAN, 0.0);
    final double proLeg = protectionLeg(cds, yieldCurve, creditCurve, 0.0);
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
    return protectionLeg(cds, yieldCurve, creditCurve, cds.getCashSettleTime());
  }

  /**
   * Compute the present value of the protection leg with a notional of 1, which is given by the integral
   * $\frac{1-R}{P(T_{v})} \int_{T_a} ^{T_b} P(t) \frac{dQ(t)}{dt} dt$ where $P(t)$ and $Q(t)$ are the discount and survival curves
   * respectively, $T_a$ and $T_b$ are the start and end of the protection respectively, $T_v$ is the valuation time (all measured
   * from $t = 0$, 'today') and $R$ is the recovery rate.
   * @param cds analytic description of a CDS traded at a certain time
   * @param yieldCurve The yield (or discount) curve
   * @param creditCurve the credit (or survival) curve
   * @param valuationTime The valuation time. If time is zero, leg is valued today. Leg is usually quoted valued on cash-settlement date. 
   * @return The value of the protection leg (on a unit notional)
   */
  public double protectionLeg(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final double valuationTime) {
    ArgumentChecker.notNull(cds, "null cds");
    ArgumentChecker.notNull(yieldCurve, "null yieldCurve");
    ArgumentChecker.notNull(creditCurve, "null creditCurve");
    if (cds.getProtectionEnd() <= 0.0) { //short cut already expired CDSs
      return 0.0;
    }

    final double[] integrationSchedule = getIntegrationsPoints(cds.getEffectiveProtectionStart(), cds.getProtectionEnd(), yieldCurve, creditCurve);

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

    // roll to the valuation date
    final double df = yieldCurve.getDiscountFactor(valuationTime);
    pv /= df;

    return pv;
  }

  /**
   * The value of the full (or dirty) annuity (or RPV01 - the premium leg per unit of coupon) today (t=0). 
   * The cash flows from premium payments and accrual-on-default are risky discounted to t=0
   * The actual value of the leg is this multiplied by the notional and the fractional coupon (i.e. coupon in basis points divided by 10,000). <br>
   * This is valid for both spot and forward starting CDS.
   * @param cds analytic description of a CDS traded at a certain time
   * @param yieldCurve The yield (or discount) curve
   * @param creditCurve the credit (or survival) curve
   * @return The full (or dirty) annuity valued today. <b>Note</b> what is usually quoted is the clean annuity  
   */
  public double dirtyAnnuity(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve) {
    ArgumentChecker.notNull(cds, "null cds");
    ArgumentChecker.notNull(yieldCurve, "null yieldCurve");
    ArgumentChecker.notNull(creditCurve, "null creditCurve");
    if (cds.getProtectionEnd() <= 0.0) { //short cut already expired CDSs
      return 0.0;
    }

    double pv = 0.0;
    for (final CDSCoupon coupon : cds.getCoupons()) {
      final double q = creditCurve.getDiscountFactor(coupon.getEffEnd());
      final double p = yieldCurve.getDiscountFactor(coupon.getPaymentTime());
      pv += coupon.getYearFrac() * p * q;
    }

    if (cds.isPayAccOnDefault()) {
      //This is needed so that the code is consistent with ISDA C when the Markit `fix' is used. For forward starting CDS (accStart > trade-date),
      //and more than one coupon, the C code generates an extra integration point (a node at protection start and one the day before) - normally
      //the second point could be ignored (since is doesn't correspond to a node of the curves, nor is it the start point), but the Markit fix is 
      //mathematically incorrect, so this point affects the result.  
      final double start = cds.getNumPayments() == 1 ? cds.getEffectiveProtectionStart() : cds.getAccStart();
      final double[] integrationSchedule = getIntegrationsPoints(start, cds.getProtectionEnd(), yieldCurve, creditCurve);
      double accPV = 0.0;
      for (final CDSCoupon coupon : cds.getCoupons()) {
        accPV += calculateSinglePeriodAccrualOnDefault(coupon, cds.getEffectiveProtectionStart(), integrationSchedule, yieldCurve, creditCurve);
      }
      pv += accPV;
    }

    return pv;
  }

  /**
   * This is the present value of the (clean) premium leg per unit coupon, seen at the cash-settlement date. It is equal to 10,000 times the RPV01
   * (Risky PV01). The actual PV of the leg is this multiplied by the notional and the fractional spread (i.e. coupon in basis
   * points divided by 10,000)<br>
   * @see  dirtyAnnuity
   * @param cds analytic description of a CDS traded at a certain time
   * @param yieldCurve The yield (or discount) curve
   * @param creditCurve the credit (or survival) curve
   * @return 10,000 times the RPV01 (on a notional of 1)
   */
  public double annuity(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve) {
    return annuity(cds, yieldCurve, creditCurve, PriceType.CLEAN, cds.getCashSettleTime());
  }

  /**
   * This is the present value of the premium leg per unit coupon, seen at the cash-settlement date. It is equal to 10,000 times the RPV01
   * (Risky PV01). The actual PV of the leg is this multiplied by the notional and the fractional spread (i.e. coupon in basis
   * points divided by 10,000)<br>
   * @see annuity, cleanAnnuity, dirtyAnnuity
   * @param cds analytic description of a CDS traded at a certain time
   * @param yieldCurve The yield (or discount) curve
   * @param creditCurve the credit (or survival) curve
   * @param cleanOrDirty Clean or dirty price
   * @return 10,000 times the RPV01 (on a notional of 1)
   */
  public double annuity(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final PriceType cleanOrDirty) {
    return annuity(cds, yieldCurve, creditCurve, cleanOrDirty, cds.getCashSettleTime());
  }

  /**
   * The value of the annuity (or RPV01 - the premium leg per unit of coupon) at a specified valuation time. The actual value of the leg is this
   * multiplied by the notional and the fractional coupon (i.e. coupon in basis points divided by 10,000). <br>
   * If this is a spot starting CDS (effective protection start = 0) then cash flows from premium payments and accrual-on-default are risky discounted to t=0
   * ('today'), then rolled forward (risk-free) to the valuation time; if the annuity is requested clean, the accrued premium (paid at the cash-settle time) is
   * rolled (again risk-free) to the valuation time; the absolute value of this amount is subtracted from the other cash flows to give the clean annuity<br>
   * If this is a forward starting CDS (effective protection start > 0), then the premium payments are again risky discounted to t=0; if the annuity is requested
   * clean, the accrued premium is risk-free discounted to the effective protection start, then risky discounted to t=0 - this gives the t=0 value of the annuity
   * including the chance that a default occurs before protection starts. 
   * If valuationTime > 0, the value of the annuity is rolled forward (risk-free) to that time. To compute the expected value of the annuity conditional on
   * no default before the valuationTime, one must divide this number by the survival probability to the valuationTime.
   *  (for unit coupon)
   * @param cds analytic description of a CDS traded at a certain time
   * @param yieldCurve The yield (or discount) curve
   * @param creditCurve the credit (or survival) curve
   * @param cleanOrDirty Clean or dirty price
   * @param valuationTime 
   * @return 10,000 times the RPV01 (on a notional of 1)
   */
  public double annuity(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final PriceType cleanOrDirty, final double valuationTime) {

    double pv = dirtyAnnuity(cds, yieldCurve, creditCurve);
    final double valDF = yieldCurve.getDiscountFactor(valuationTime);

    if (cleanOrDirty == PriceType.CLEAN) {
      final double csTime = cds.getCashSettleTime();
      final double protStart = cds.getEffectiveProtectionStart();
      final double csDF = valuationTime == csTime ? valDF : yieldCurve.getDiscountFactor(csTime);
      final double q = protStart == 0 ? 1.0 : creditCurve.getSurvivalProbability(protStart);
      final double acc = cds.getAccruedYearFraction();
      pv -= acc * csDF * q; //subtract the accrued risky discounted to today
    }

    pv /= valDF; //roll forward to valuation date
    return pv;
  }

  private double calculateSinglePeriodAccrualOnDefault(final CDSCoupon coupon, final double effectiveStart, final double[] integrationPoints, final ISDACompliantYieldCurve yieldCurve,
      final ISDACompliantCreditCurve creditCurve) {

    final double start = Math.max(coupon.getEffStart(), effectiveStart);
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
      final double dhrt = dht + drt;

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
  public double pvYieldSensitivity(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final double fractionalSpread, final int yieldCurveNode) {
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
    final double b = annuity(cds, yieldCurve, creditCurve, PriceType.CLEAN);
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
    //   final double obsOffset = cds.isProtectionFromStartOfDay() ? -cds.getCurveOneDay() : 0.0;

    final int n = cds.getNumPayments();
    double pvSense = 0.0;
    for (int i = 0; i < n; i++) {
      final CDSCoupon c = cds.getCoupon(i);
      final double paymentTime = c.getPaymentTime();
      final double creditObsTime = c.getEffEnd();
      final double dqdh = creditCurve.getSingleNodeDiscountFactorSensitivity(creditObsTime, creditCurveNode);
      if (dqdh == 0) {
        continue;
      }
      final double p = yieldCurve.getDiscountFactor(paymentTime);
      pvSense += c.getYearFrac() * p * dqdh;
    }

    if (cds.isPayAccOnDefault()) {
      final double start = cds.getNumPayments() == 1 ? cds.getEffectiveProtectionStart() : cds.getAccStart();
      final double[] integrationSchedule = getIntegrationsPoints(start, cds.getProtectionEnd(), yieldCurve, creditCurve);
      //      final double offsetStepin = cds.getStepin() + obsOffset;

      double accPVSense = 0.0;
      for (int i = 0; i < n; i++) {
        accPVSense += calculateSinglePeriodAccrualOnDefaultCreditSensitivity(cds.getCoupon(i), cds.getEffectiveProtectionStart(), integrationSchedule, yieldCurve, creditCurve, creditCurveNode);
      }
      pvSense += accPVSense;
    }

    final double df = yieldCurve.getDiscountFactor(cds.getCashSettleTime());
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
    //   final double obsOffset = cds.isProtectionFromStartOfDay() ? -cds.getCurveOneDay() : 0.0;

    final int n = cds.getNumPayments();
    double pvSense = 0.0;
    for (int i = 0; i < n; i++) {
      final CDSCoupon c = cds.getCoupon(i);
      final double paymentTime = c.getPaymentTime();
      final double creditObsTime = c.getEffEnd();
      final double dpdr = yieldCurve.getSingleNodeDiscountFactorSensitivity(paymentTime, yieldCurveNode);
      if (dpdr == 0) {
        continue;
      }
      final double q = creditCurve.getSurvivalProbability(creditObsTime);
      pvSense += c.getYearFrac() * q * dpdr;
    }

    if (cds.isPayAccOnDefault()) {
      final double start = cds.getNumPayments() == 1 ? cds.getEffectiveProtectionStart() : cds.getAccStart();
      final double[] integrationSchedule = getIntegrationsPoints(start, cds.getProtectionEnd(), yieldCurve, creditCurve);
      //  final double offsetStepin = cds.getStepin() + obsOffset;

      double accPVSense = 0.0;
      for (int i = 0; i < n; i++) {

        accPVSense += calculateSinglePeriodAccrualOnDefaultYieldSensitivity(cds.getCoupon(i), cds.getEffectiveProtectionStart(), integrationSchedule, yieldCurve, creditCurve, yieldCurveNode);
      }
      pvSense += accPVSense;
    }

    final double df = yieldCurve.getDiscountFactor(cds.getCashSettleTime());
    pvSense /= df;

    //TODO this was put in quickly the get the right sensitivity to the first node
    final double dfSense = yieldCurve.getSingleNodeDiscountFactorSensitivity(cds.getCashSettleTime(), yieldCurveNode);
    if (dfSense != 0.0) {
      final double pro = annuity(cds, yieldCurve, creditCurve, PriceType.DIRTY);
      pvSense -= pro / df * dfSense;
    }

    return pvSense;
  }

  private double calculateSinglePeriodAccrualOnDefaultCreditSensitivity(final CDSCoupon coupon, final double effStart, final double[] integrationPoints, final ISDACompliantYieldCurve yieldCurve,
      final ISDACompliantCreditCurve creditCurve, final int creditCurveNode) {

    final double start = Math.max(coupon.getEffStart(), effStart);
    if (start >= coupon.getEffEnd()) {
      return 0.0;
    }
    final double[] knots = truncateSetInclusive(start, coupon.getEffEnd(), integrationPoints);

    double t = knots[0];
    double ht0 = creditCurve.getRT(t);
    double rt0 = yieldCurve.getRT(t);
    double p0 = Math.exp(-rt0);
    double q0 = Math.exp(-ht0);
    double b0 = p0 * q0; // this is the risky discount factor
    double dqdr0 = creditCurve.getSingleNodeDiscountFactorSensitivity(t, creditCurveNode);

    double t0 = t - coupon.getEffStart() + _omega;
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

      if (_formula == AccrualOnDefaultFormulae.MarkitFix) {
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
        final double t1 = t - coupon.getEffStart() + _omega;
        if (Math.abs(dhrt) < 1e-5) {
          final double e = epsilon(-dhrt);
          final double eP = epsilonP(-dhrt);
          final double ePP = epsilonPP(-dhrt);
          final double w1 = t0 * e + dt * eP;
          final double w2 = t0 * eP + dt * ePP;
          final double dPVdq0 = p0 * ((1 + dht) * w1 - dht * w2);
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
    return coupon.getYFRatio() * pvSense;
  }

  private double calculateSinglePeriodAccrualOnDefaultYieldSensitivity(final CDSCoupon coupon, final double effStart, final double[] integrationPoints, final ISDACompliantYieldCurve yieldCurve,
      final ISDACompliantCreditCurve creditCurve, final int yieldCurveNode) {
    final double start = Math.max(coupon.getEffStart(), effStart);
    if (start >= coupon.getEffEnd()) {
      return 0.0;
    }
    if (_formula != AccrualOnDefaultFormulae.MarkitFix) {
      throw new NotImplementedException();
    }

    final double[] knots = truncateSetInclusive(start, coupon.getEffEnd(), integrationPoints);

    double t = knots[0];
    double ht0 = creditCurve.getRT(t);
    double rt0 = yieldCurve.getRT(t);
    double p0 = Math.exp(-rt0);
    double q0 = Math.exp(-ht0);
    double b0 = p0 * q0; // this is the risky discount factor
    double dpdr0 = yieldCurve.getSingleNodeDiscountFactorSensitivity(t, yieldCurveNode);

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
    return coupon.getYFRatio() * pvSense;
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
        (creditCurveNode != creditCurve.getNumberOfKnots() - 1 && cds.getEffectiveProtectionStart() >= creditCurve.getTimeAtIndex(creditCurveNode + 1))) {
      return 0.0; // can't have any sensitivity in this case
    }
    if (cds.getProtectionEnd() <= 0.0) { //short cut already expired CDSs
      return 0.0;
    }

    final double[] integrationSchedule = getIntegrationsPoints(cds.getEffectiveProtectionStart(), cds.getProtectionEnd(), yieldCurve, creditCurve);

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
    final double df = yieldCurve.getDiscountFactor(cds.getCashSettleTime());

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
        (yieldCurveNode != creditCurve.getNumberOfKnots() - 1 && cds.getEffectiveProtectionStart() >= yieldCurve.getTimeAtIndex(yieldCurveNode + 1))) {
      return 0.0; // can't have any sensitivity in this case
    }
    if (cds.getProtectionEnd() <= 0.0) { //short cut already expired CDSs
      return 0.0;
    }

    final double[] integrationSchedule = getIntegrationsPoints(cds.getEffectiveProtectionStart(), cds.getProtectionEnd(), yieldCurve, creditCurve);

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
    final double df = yieldCurve.getDiscountFactor(cds.getCashSettleTime());

    pvSense /= df;

    //TODO this was put in quickly the get the right sensitivity to the first node
    final double dfSense = yieldCurve.getSingleNodeDiscountFactorSensitivity(cds.getCashSettleTime(), yieldCurveNode);
    if (dfSense != 0.0) {
      final double pro = protectionLeg(cds, yieldCurve, creditCurve);
      pvSense -= pro / df * dfSense;
    }

    return pvSense;
  }

}
