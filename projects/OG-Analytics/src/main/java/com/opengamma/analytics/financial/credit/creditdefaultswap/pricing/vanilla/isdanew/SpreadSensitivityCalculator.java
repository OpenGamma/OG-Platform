/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.model.BumpType;
import com.opengamma.analytics.math.differentiation.FiniteDifferenceType;
import com.opengamma.util.ArgumentChecker;

/**
 * This calculates the sensitivity of the present value of a CDS to various (finite) shifts of the market spreads -
 * this is performed by a "bump and reprice" so is accurate for arbitrarily large shifts/bumps.<br> For small bumps (typically 
 * less than 1bps) it approximates the derivative $$\frac{\partial V}{\partial S}$$  where $V$ is the present value and $S$ is
 * either a single market spread or the entire market spread curve. However, it is better (in accuracy and speed) to use 
 * AnalyticSpreadSensitivityCalculator if this derivative is required.  
 */
public class SpreadSensitivityCalculator {

  // private static final ISDACompliantCreditCurveBuild BUILDER = new ISDACompliantCreditCurveBuild();
  private static final ISDACompliantCreditCurveBuilder BUILDER = new FastCreditCurveBuilder();
  private static final AnalyticCDSPricer PRICER = new AnalyticCDSPricer();

  /**
   * The credit DV01 by a parallel shift of the market spreads (CDS spread curve). This takes an extraneous yield curve, a set of reference CDSs 
   * (marketCDSs) and their par-spreads (expressed as <b>fractions not basis points</b>) and bootstraps a credit (hazard) curve - 
   * the target CDS is then priced with this credit curve. This is then repeated with the market spreads bumped in parallel by 
   * some amount. The result is the difference (bumped minus base price) is divided by the bump amount.<br>
   * For small bumps (<1e-4) this approximates $$\frac{\partial V}{\partial S}$$<br>
   * Credit DV01 is (often) defined as -( V(S + 1bp) - V(s)) - to achieve this use fracBumpAmount = 1e-4 and bumpType ADDITIVE
   * @param cds analytic description of a CDS traded at a certain time 
   * @param cdsFracSpread The <b>fraction</b> spread of the CDS
   * @param priceType Clean or dirty price
   * @param yieldCurve The yield (or discount) curve  
   * @param marketCDSs The market CDSs - these are the reference instruments used to build the credit curve 
   * @param marketFracSpreads The <b>fractional</b> spreads of the market CDSs 
   * @param fracBumpAmount The fraction bump amount, so a 1pb bump is 1e-4 
   * @param bumpType ADDITIVE or MULTIPLICATIVE
   * @return The credit DV01
   */
  public double parallelCreditDV01(final CDSAnalytic cds, final double cdsFracSpread, final PriceType priceType, final ISDACompliantYieldCurve yieldCurve, final CDSAnalytic[] marketCDSs,
      final double[] marketFracSpreads, final double fracBumpAmount, final BumpType bumpType) {
    ArgumentChecker.notNull(cds, "cds");
    ArgumentChecker.noNulls(marketCDSs, "curvePoints");
    ArgumentChecker.notEmpty(marketFracSpreads, "spreads");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    ArgumentChecker.notNull(priceType, "priceType");
    ArgumentChecker.notNull(bumpType, "bumpType");
    ArgumentChecker.isTrue(Math.abs(fracBumpAmount) > 1e-10, "bump amount too small");
    final int n = marketCDSs.length;
    ArgumentChecker.isTrue(n == marketFracSpreads.length, "speads length does not match curvePoints");
    final double[] bumpedSpreads = makeBumpedSpreads(marketFracSpreads, fracBumpAmount, bumpType);
    final double diff = fdCreditDV01(cds, cdsFracSpread, marketCDSs, bumpedSpreads, marketFracSpreads, yieldCurve, priceType);
    return diff / fracBumpAmount;
  }

  /**
   * The bucked credit DV01 by shifting each  market spread in turn. This takes an extraneous yield curve, a set of reference CDSs 
   * (marketCDSs) and their par-spreads (expressed as <b>fractions not basis points</b>) and bootstraps a credit (hazard) curve - 
   * the target CDS is then priced with this credit curve. This is then repeated with each market spreads bumped in turn. 
   * The result is the vector of differences (bumped minus base price) divided by the bump amount.<br>
   * For small bumps (<1e-4) this approximates $$\frac{\partial V}{\partial S_i}$$ where $$S_i$$ is the spread of the $$1^{th}$$
   * market CDS<br>
   * @param cds analytic description of a CDS traded at a certain time 
   * @param cdsFracSpread The <b>fraction</b> spread of the CDS
   * @param priceType Clean or dirty price
   * @param yieldCurve The yield (or discount) curve  
   * @param marketCDSs The market CDSs - these are the reference instruments used to build the credit curve 
   * @param marketFracSpreads The <b>fractional</b> spreads of the market CDSs 
   * @param fracBumpAmount The fraction bump amount, so a 1pb bump is 1e-4 
   * @param bumpType ADDITIVE or MULTIPLICATIVE
   * @return The credit DV01
   * @return
   */
  public double[] bucketedCreditDV01(final CDSAnalytic cds, final double cdsFracSpread, final PriceType priceType, final ISDACompliantYieldCurve yieldCurve, final CDSAnalytic[] marketCDSs,
      final double[] marketFracSpreads, final double fracBumpAmount, final BumpType bumpType) {
    ArgumentChecker.notNull(cds, "cds");
    ArgumentChecker.noNulls(marketCDSs, "curvePoints");
    ArgumentChecker.notEmpty(marketFracSpreads, "spreads");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    ArgumentChecker.notNull(priceType, "priceType");
    ArgumentChecker.notNull(bumpType, "bumpType");
    ArgumentChecker.isTrue(Math.abs(fracBumpAmount) > 1e-10, "bump amount too small");
    final int n = marketCDSs.length;
    ArgumentChecker.isTrue(n == marketFracSpreads.length, "speads length does not match curvePoints");

    final ISDACompliantCreditCurve baseCurve = BUILDER.calibrateCreditCurve(marketCDSs, marketFracSpreads, yieldCurve);
    final double basePrice = PRICER.pv(cds, yieldCurve, baseCurve, cdsFracSpread, priceType);

    double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      final double[] temp = makeBumpedSpreads(marketFracSpreads, fracBumpAmount, bumpType, i);
      final ISDACompliantCreditCurve bumpedCurve = BUILDER.calibrateCreditCurve(marketCDSs, temp, yieldCurve);
      final double price = PRICER.pv(cds, yieldCurve, bumpedCurve, cdsFracSpread, priceType);
      res[i] = (price - basePrice) / fracBumpAmount;
    }

    return res;
  }

  /**
   * The bucked credit DV01 by pricing off a flat curve using each market spread in turn. This takes an extraneous yield curve,
   *  a set of reference CDSs (marketCDSs) and their par-spreads (expressed as <b>fractions not basis points</b>) and
   *  bootstraps a credit (hazard) curve - 
   * the target CDS is then priced with this credit curve. This is then repeated with each market spreads bumped in turn. 
   * The result is the vector of differences (bumped minus base price) divided by the bump amount.<br>
   * For small bumps (<1e-4) this approximates $$\frac{\partial V}{\partial S_i}$$ for a flat curve where $$S_i$$ 
   * is the spread of the $$1^{th}$$ market CDS
   * @param cds analytic description of a CDS traded at a certain time 
   * @param cdsFracSpread The <b>fraction</b> spread of the CDS
   * @param priceType Clean or dirty price
   * @param yieldCurve The yield (or discount) curve  
   * @param marketCDSs The market CDSs - these are the reference instruments used to build the credit curve 
   * @param marketFracSpreads The <b>fractional</b> spreads of the market CDSs 
   * @param fracBumpAmount The fraction bump amount, so a 1pb bump is 1e-4 
   * @param bumpType ADDITIVE or MULTIPLICATIVE
   * @return The credit DV01
   * @return
   */
  public double[] bucketedFlatCreditDV01(final CDSAnalytic cds, final double cdsFracSpread, final PriceType priceType, final ISDACompliantYieldCurve yieldCurve, final CDSAnalytic[] marketCDSs,
      final double[] marketFracSpreads, final double fracBumpAmount, final BumpType bumpType) {
    ArgumentChecker.notNull(cds, "cds");
    ArgumentChecker.noNulls(marketCDSs, "curvePoints");
    ArgumentChecker.notEmpty(marketFracSpreads, "spreads");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    ArgumentChecker.notNull(priceType, "priceType");
    ArgumentChecker.notNull(bumpType, "bumpType");
    ArgumentChecker.isTrue(Math.abs(fracBumpAmount) > 1e-10, "bump amount too small");
    final int n = marketCDSs.length;
    ArgumentChecker.isTrue(n == marketFracSpreads.length, "speads length does not match curvePoints");

    double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      final CDSAnalytic[] tempCDSs = new CDSAnalytic[] {marketCDSs[i]};
      final double[] tempSpread = new double[] {marketFracSpreads[i]};
      final ISDACompliantCreditCurve baseCurve = BUILDER.calibrateCreditCurve(tempCDSs, tempSpread, yieldCurve);
      final double basePrice = PRICER.pv(cds, yieldCurve, baseCurve, cdsFracSpread, priceType);
      final double[] bumpedSpreads = makeBumpedSpreads(tempSpread, fracBumpAmount, bumpType);
      final ISDACompliantCreditCurve bumpedCurve = BUILDER.calibrateCreditCurve(tempCDSs, bumpedSpreads, yieldCurve);
      final double price = PRICER.pv(cds, yieldCurve, bumpedCurve, cdsFracSpread, priceType);
      res[i] = (price - basePrice) / fracBumpAmount;
    }

    return res;
  }

  /**
   * The difference in PV between two market spread 
   * @param cds analytic description of a CDS traded at a certain time 
   * @param cdsFracSpread The <b>fraction</b> spread of the CDS
   * @param priceType Clean or dirty price
   * @param yieldCurve The yield (or discount) curve  
   * @param marketCDSs The market CDSs - these are the reference instruments used to build the credit curve 
   * @param marketFracSpreads The <b>fractional</b> spreads of the market CDSs 
   * @param fracDeltaSpreads Non-negative shifts 
   * @param fdType
   * @return
   */
  public double finateDifferenceSpreadSensitivity(final CDSAnalytic cds, final double cdsFracSpread, final PriceType priceType, final ISDACompliantYieldCurve yieldCurve,
      final CDSAnalytic[] marketCDSs, final double[] marketFracSpreads, final double[] fracDeltaSpreads, final FiniteDifferenceType fdType) {
    ArgumentChecker.notNull(cds, "cds");
    ArgumentChecker.noNulls(marketCDSs, "curvePoints");
    ArgumentChecker.notEmpty(marketFracSpreads, "spreads");
    ArgumentChecker.notEmpty(fracDeltaSpreads, "deltaSpreads");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    ArgumentChecker.notNull(priceType, "priceType");

    final int n = marketCDSs.length;
    ArgumentChecker.isTrue(n == marketFracSpreads.length, "speads length does not match curvePoints");
    ArgumentChecker.isTrue(n == fracDeltaSpreads.length, "deltaSpreads length does not match curvePoints");
    for (int i = 0; i < n; i++) {
      ArgumentChecker.isTrue(marketFracSpreads[i] > 0, "spreads must be positive");
      ArgumentChecker.isTrue(fracDeltaSpreads[i] >= 0, "deltaSpreads must none negative");
      ArgumentChecker.isTrue(fdType == FiniteDifferenceType.FORWARD || fracDeltaSpreads[i] < marketFracSpreads[i], "deltaSpread must be less spread, unless forward difference is used");
    }

    switch (fdType) {
      case CENTRAL:
        return fdCentral(cds, cdsFracSpread, marketCDSs, marketFracSpreads, fracDeltaSpreads, yieldCurve, priceType);
      case FORWARD:
        return fdForward(cds, cdsFracSpread, marketCDSs, marketFracSpreads, fracDeltaSpreads, yieldCurve, priceType);
      case BACKWARD:
        return fdBackwards(cds, cdsFracSpread, marketCDSs, marketFracSpreads, fracDeltaSpreads, yieldCurve, priceType);
      default:
        throw new IllegalArgumentException("unknown type " + fdType);
    }
  }

  private double fdCreditDV01(final CDSAnalytic pricingCDS, final double cdsSpread, final CDSAnalytic[] curvePoints, final double[] spreadsUp, final double[] spreadsDown,
      final ISDACompliantYieldCurve yieldCurve, final PriceType priceType) {

    final ISDACompliantCreditCurve curveUp = BUILDER.calibrateCreditCurve(curvePoints, spreadsUp, yieldCurve);
    final ISDACompliantCreditCurve curveDown = BUILDER.calibrateCreditCurve(curvePoints, spreadsDown, yieldCurve);
    final double up = PRICER.pv(pricingCDS, yieldCurve, curveUp, cdsSpread, priceType);
    final double down = PRICER.pv(pricingCDS, yieldCurve, curveDown, cdsSpread, priceType);
    return up - down;
  }

  private double fdCentral(final CDSAnalytic pricingCDS, final double cdsSpread, final CDSAnalytic[] curvePoints, final double[] spreads, final double[] deltaSpreads,
      final ISDACompliantYieldCurve yieldCurve, final PriceType priceType) {
    final int n = curvePoints.length;
    final double[] spreadUp = new double[n];
    final double[] spreadDown = new double[n];
    for (int i = 0; i < n; i++) {
      spreadUp[i] = spreads[i] + deltaSpreads[i];
      spreadDown[i] = spreads[i] - deltaSpreads[i];
    }
    final ISDACompliantCreditCurve curveUp = BUILDER.calibrateCreditCurve(curvePoints, spreadUp, yieldCurve);
    final ISDACompliantCreditCurve curveDown = BUILDER.calibrateCreditCurve(curvePoints, spreadDown, yieldCurve);
    final double up = PRICER.pv(pricingCDS, yieldCurve, curveUp, cdsSpread, priceType);
    final double down = PRICER.pv(pricingCDS, yieldCurve, curveDown, cdsSpread, priceType);

    return up - down;
  }

  private double fdForward(final CDSAnalytic pricingCDS, final double cdsSpread, final CDSAnalytic[] curvePoints, final double[] spreads, final double[] deltaSpreads,
      final ISDACompliantYieldCurve yieldCurve, final PriceType priceType) {
    final int n = curvePoints.length;
    final double[] spreadUp = new double[n];
    for (int i = 0; i < n; i++) {
      spreadUp[i] = spreads[i] + deltaSpreads[i];
    }
    final ISDACompliantCreditCurve curveUp = BUILDER.calibrateCreditCurve(curvePoints, spreadUp, yieldCurve);
    final ISDACompliantCreditCurve curveMid = BUILDER.calibrateCreditCurve(curvePoints, spreads, yieldCurve);
    final double up = PRICER.pv(pricingCDS, yieldCurve, curveUp, cdsSpread, priceType);
    final double mid = PRICER.pv(pricingCDS, yieldCurve, curveMid, cdsSpread, priceType);

    return up - mid;
  }

  private double fdBackwards(final CDSAnalytic pricingCDS, final double cdsSpread, final CDSAnalytic[] curvePoints, final double[] spreads, final double[] deltaSpreads,
      final ISDACompliantYieldCurve yieldCurve, final PriceType priceType) {
    final int n = curvePoints.length;
    final double[] spreadDown = new double[n];
    for (int i = 0; i < n; i++) {
      spreadDown[i] = spreads[i] - deltaSpreads[i];
    }
    final ISDACompliantCreditCurve curveMid = BUILDER.calibrateCreditCurve(curvePoints, spreads, yieldCurve);
    final ISDACompliantCreditCurve curveDown = BUILDER.calibrateCreditCurve(curvePoints, spreadDown, yieldCurve);
    final double mid = PRICER.pv(pricingCDS, yieldCurve, curveMid, cdsSpread, priceType);
    final double down = PRICER.pv(pricingCDS, yieldCurve, curveDown, cdsSpread, priceType);

    return mid - down;
  }

  private double[] makeBumpedSpreads(final double[] spreads, final double amount, final BumpType bumpType) {
    final int n = spreads.length;
    final double[] res = new double[n];

    if (bumpType == BumpType.ADDITIVE) {
      for (int i = 0; i < n; i++) {
        res[i] = spreads[i] + amount;
      }
    } else if (bumpType == BumpType.MULTIPLICATIVE) {
      final double a = 1 + amount;
      for (int i = 0; i < n; i++) {
        res[i] = spreads[i] * a;
      }
    } else {
      throw new IllegalArgumentException("BumpType " + bumpType + " is not supported");
    }
    return res;
  }

  private double[] makeBumpedSpreads(final double[] spreads, final double amount, final BumpType bumpType, final int index) {
    final int n = spreads.length;
    final double[] res = new double[n];
    System.arraycopy(spreads, 0, res, 0, n);

    switch (bumpType) {
      case ADDITIVE:
        res[index] += amount;
        break;
      case MULTIPLICATIVE:
        res[index] += res[index] * amount;
        break;
      default:
        throw new IllegalArgumentException("BumpType " + bumpType + " is not supported");
    }
    return res;
  }
}
