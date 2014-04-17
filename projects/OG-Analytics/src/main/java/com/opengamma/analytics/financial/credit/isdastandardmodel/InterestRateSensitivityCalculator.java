/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class InterestRateSensitivityCalculator {

  private static final MarketQuoteConverter CONVERTER = new MarketQuoteConverter();
  private static final double ONE_BPS = 1e-4;

  private final AnalyticCDSPricer _pricer;

  public InterestRateSensitivityCalculator() {
    _pricer = new AnalyticCDSPricer();
  }

  public InterestRateSensitivityCalculator(final AccrualOnDefaultFormulae formula) {
    _pricer = new AnalyticCDSPricer(formula);
  }

  /**
   *  The IR01 (Interest-Rate 01) is by definition the change in the price of a CDS when the market interest rates (these are 
   * money-market and swap rates) all increased by 1bps. This assumes that the quoted (or flat) spread is invariant to a change in the yield curve 
   * @param cds analytic description of a CDS traded at a certain time 
   * @param quote This can be a QuotedSpread or PointsUpFront. For quoted spread this is taken as invariant; for PUF this is converted to a quoted spread (which is
   * then invariant). ParSpread is not supported 
   * @param yieldCurveBuilder yield curve builder 
   * @param marketRates the money-market and swap rates (in the correct order for the yield curve builder, i.e. in ascending order of
   * maturity)
   * @return the parallel IR01 
   */
  public double parallelIR01(final CDSAnalytic cds, final CDSQuoteConvention quote, final ISDACompliantYieldCurveBuild yieldCurveBuilder, final double[] marketRates) {
    ArgumentChecker.notNull(cds, "cds");
    ArgumentChecker.notNull(quote, "quote");
    ArgumentChecker.notNull(yieldCurveBuilder, "yieldCurveBuilder");
    ArgumentChecker.notEmpty(marketRates, "marketRates");
    final ISDACompliantYieldCurve ycUP = bumpYieldCurve(yieldCurveBuilder, marketRates, ONE_BPS);
    final ISDACompliantYieldCurve yc = yieldCurveBuilder.build(marketRates);

    if (quote instanceof QuotedSpread) {
      final QuotedSpread qs = (QuotedSpread) quote;
      final double puf = CONVERTER.quotedSpreadToPUF(cds, qs.getCoupon(), yc, qs.getQuotedSpread());
      final double pufUp = CONVERTER.quotedSpreadToPUF(cds, qs.getCoupon(), ycUP, qs.getQuotedSpread());
      return pufUp - puf;
    } else if (quote instanceof PointsUpFront) {
      final QuotedSpread qs = CONVERTER.convert(cds, (PointsUpFront) quote, yc);
      final PointsUpFront pufUp = CONVERTER.convert(cds, qs, ycUP);
      return pufUp.getPointsUpFront() - ((PointsUpFront) quote).getPointsUpFront();
    } else if (quote instanceof ParSpread) {
      throw new NotImplementedException("This type of claculation don't make sense for par spreads. Use a fixed credit curve method.");
    } else {
      throw new IllegalArgumentException("Unknown quote type: " + quote.getClass());
    }
  }

  /**
   *  The IR01 (Interest-Rate 01) is by definition the change in the price of a CDS when the market interest rates (these are 
   * money-market and swap rates) all increased by 1bps. This assumes that the quoted (or flat) spread is invariant to a change in the yield curve. In addition
   * the bumps are applied directly to the yield curve and NOT the instruments.  
   * @param cds analytic description of a CDS traded at a certain time 
   * @param quote This can be a QuotedSpread or PointsUpFront. For quoted spread this is taken as invariant; for PUF this is converted to a quoted spread (which is
   * then invariant). ParSpread is not supported 
   * @param yieldCurve yield curve 
   * @param marketRates the money-market and swap rates (in the correct order for the yield curve builder, i.e. in ascending order of
   * maturity)
   * @return the parallel IR01 
   */
  public double parallelIR01(final CDSAnalytic cds, final CDSQuoteConvention quote, final ISDACompliantYieldCurve yieldCurve, final double[] marketRates) {
    ArgumentChecker.notNull(cds, "cds");
    ArgumentChecker.notNull(quote, "quote");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    ArgumentChecker.notEmpty(marketRates, "marketRates");
    final ISDACompliantYieldCurve ycUP = bumpYieldCurve(yieldCurve, ONE_BPS);

    if (quote instanceof QuotedSpread) {
      final QuotedSpread qs = (QuotedSpread) quote;
      final double puf = CONVERTER.quotedSpreadToPUF(cds, qs.getCoupon(), yieldCurve, qs.getQuotedSpread());
      final double pufUp = CONVERTER.quotedSpreadToPUF(cds, qs.getCoupon(), ycUP, qs.getQuotedSpread());
      return pufUp - puf;
    } else if (quote instanceof PointsUpFront) {
      final QuotedSpread qs = CONVERTER.convert(cds, (PointsUpFront) quote, yieldCurve);
      final PointsUpFront pufUp = CONVERTER.convert(cds, qs, ycUP);
      return pufUp.getPointsUpFront() - ((PointsUpFront) quote).getPointsUpFront();
    } else if (quote instanceof ParSpread) {
      throw new NotImplementedException("This type of claculation don't make sense for par spreads. Use a fixed credit curve method.");
    } else {
      throw new IllegalArgumentException("Unknown quote type: " + quote.getClass());
    }
  }

  /**
   * The IR01 (Interest-Rate 01) is by definition the change in the price of a CDS when the market interest rates (these are 
   * money-market and swap rates) all increased by 1bps. This assumes that the credit curve is invariant  
   * @param cds analytic description of a CDS traded at a certain time 
   * @param coupon The cds's coupon (as a fraction)
   * @param creditCurve the credit (or survival) curve 
   * @param yieldCurveBuilder yield curve builder 
   * @param marketRates the money-market and swap rates (in the correct order for the yield curve builder, i.e. in ascending order of
   * maturity)
   * @return the parallel IR01 
   */
  public double parallelIR01(final CDSAnalytic cds, final double coupon, final ISDACompliantCreditCurve creditCurve, final ISDACompliantYieldCurveBuild yieldCurveBuilder, final double[] marketRates) {
    ArgumentChecker.notNull(cds, "cds");
    ArgumentChecker.notNull(creditCurve, "creditCurve");
    ArgumentChecker.notNull(yieldCurveBuilder, "yieldCurveBuilder");
    ArgumentChecker.notEmpty(marketRates, "marketRates");
    final ISDACompliantYieldCurve ycUP = bumpYieldCurve(yieldCurveBuilder, marketRates, ONE_BPS);
    final ISDACompliantYieldCurve yc = yieldCurveBuilder.build(marketRates);
    return priceDiff(cds, creditCurve, coupon, ycUP, yc);
  }

  /**
   * The IR01 (Interest-Rate 01) is by definition the change in the price of a CDS when the yield curve is bumped by 1bps.
   *
   * Note, this bumps the yield curve not the underlying instrument market data. See methods which take a {@code ISDACompliantYieldCurveBuild}
   * for bumping of the underlying instruments.
   *
   * @param cds analytic description of a CDS traded at a certain time
   * @param coupon The cds's coupon (as a fraction)
   * @param creditCurve the credit (or survival) curve
   * @param yieldCurve yield curve
   * @return the parallel IR01
   */
  public double parallelIR01(final CDSAnalytic cds, final double coupon, final ISDACompliantCreditCurve creditCurve, final ISDACompliantYieldCurve yieldCurve) {
    ArgumentChecker.notNull(cds, "cds");
    ArgumentChecker.notNull(creditCurve, "creditCurve");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    final ISDACompliantYieldCurve ycUP = bumpYieldCurve(yieldCurve, ONE_BPS);
    return priceDiff(cds, creditCurve, coupon, ycUP, yieldCurve);
  }

  /**
   * The bucketed IR01 (Interest-Rate 01) is by definition the vector of changes in the price of a CDS when the market interest rates
   * (these are money-market and swap rates) increased by 1bps in turn 
   * @param cds analytic description of a CDS traded at a certain time 
   * @param coupon The cds's coupon (as a fraction)
   * @param creditCurve the credit (or survival) curve 
   * @param yieldCurveBuilder yield curve builder 
   * @param marketRates the money-market and swap rates (in the correct order for the yield curve builder, i.e. in ascending order of
   * maturity)
   * @return the bucketed IR01 
   */
  public double[] bucketedIR01(final CDSAnalytic cds, final double coupon, final ISDACompliantCreditCurve creditCurve, final ISDACompliantYieldCurveBuild yieldCurveBuilder, final double[] marketRates) {
    ArgumentChecker.notNull(cds, "cds");
    ArgumentChecker.notNull(creditCurve, "creditCurve");
    ArgumentChecker.notNull(yieldCurveBuilder, "yieldCurveBuilder");
    ArgumentChecker.notEmpty(marketRates, "marketRates");
    final ISDACompliantYieldCurve baseYC = yieldCurveBuilder.build(marketRates);
    final int n = marketRates.length;
    final ISDACompliantYieldCurve[] bumpedYC = new ISDACompliantYieldCurve[n];
    for (int i = 0; i < n; i++) {
      bumpedYC[i] = bumpYieldCurve(yieldCurveBuilder, marketRates, ONE_BPS, i);
    }
    return priceDiff(cds, creditCurve, coupon, bumpedYC, baseYC);
  }

  /**
   * The bucketed IR01 (Interest-Rate 01) is by definition the vector of changes in the price of a CDS when the points on the
   * yield curve are bumped.
   *
   * Note, this bumps the yield curve not the underlying instrument market data. See methods which take a {@code ISDACompliantYieldCurveBuild}
   * for bumping of the underlying instruments.
   *
   * @param cds analytic description of a CDS traded at a certain time
   * @param coupon The cds's coupon (as a fraction)
   * @param creditCurve the credit (or survival) curve
   * @param yieldCurve yield curve
   * @return the bucketed IR01
   */
  public double[] bucketedIR01(final CDSAnalytic cds, final double coupon, final ISDACompliantCreditCurve creditCurve, final ISDACompliantYieldCurve yieldCurve) {
    ArgumentChecker.notNull(cds, "cds");
    ArgumentChecker.notNull(creditCurve, "creditCurve");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    final ISDACompliantYieldCurve baseYC = yieldCurve;
    final int n = yieldCurve.getNumberOfKnots();
    final ISDACompliantYieldCurve[] bumpedYC = new ISDACompliantYieldCurve[n];
    for (int i = 0; i < n; i++) {
      bumpedYC[i] = bumpYieldCurve(yieldCurve, ONE_BPS, i);
    }
    return priceDiff(cds, creditCurve, coupon, bumpedYC, baseYC);
  }

  private double priceDiff(final CDSAnalytic cds, final ISDACompliantCreditCurve creditCurve, final double coupon, final ISDACompliantYieldCurve yc1, final ISDACompliantYieldCurve yc2) {
    final double pv1 = _pricer.pv(cds, yc1, creditCurve, coupon, PriceType.DIRTY);
    final double pv2 = _pricer.pv(cds, yc2, creditCurve, coupon, PriceType.DIRTY);
    return pv1 - pv2;
  }

  private double[] priceDiff(final CDSAnalytic cds, final ISDACompliantCreditCurve creditCurve, final double coupon, final ISDACompliantYieldCurve[] bumpedYC, final ISDACompliantYieldCurve baseYC) {
    final double basePV = _pricer.pv(cds, baseYC, creditCurve, coupon, PriceType.DIRTY);
    final int n = bumpedYC.length;
    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      final double pv = _pricer.pv(cds, bumpedYC[i], creditCurve, coupon, PriceType.DIRTY);
      res[i] = pv - basePV;
    }
    return res;
  }

  private ISDACompliantYieldCurve bumpYieldCurve(final ISDACompliantYieldCurveBuild builder, final double[] rates, final double bumpAmount) {
    final int n = rates.length;
    final double[] bumped = new double[n];
    System.arraycopy(rates, 0, bumped, 0, n);
    for (int i = 0; i < n; i++) {
      bumped[i] += bumpAmount;
    }
    return builder.build(bumped);
  }

  private ISDACompliantYieldCurve bumpYieldCurve(final ISDACompliantYieldCurve curve, final double bumpAmount) {
    final int n = curve.getNumberOfKnots();
    final double[] bumped = curve.getKnotZeroRates();
    for (int i = 0; i < n; i++) {
      bumped[i] += bumpAmount;
    }
    return curve.withRates(bumped);
  }

  private ISDACompliantYieldCurve bumpYieldCurve(final ISDACompliantYieldCurveBuild builder, final double[] rates, final double bumpAmount, final int index) {
    final int n = rates.length;
    final double[] bumped = new double[n];
    System.arraycopy(rates, 0, bumped, 0, n);
    bumped[index] += bumpAmount;
    return builder.build(bumped);
  }

  private ISDACompliantYieldCurve bumpYieldCurve(final ISDACompliantYieldCurve curve, final double bumpAmount, final int index) {
    return curve.withRate(curve.getZeroRateAtIndex(index) + bumpAmount, index);
  }

  //  @SuppressWarnings("unused")
  //  private ISDACompliantYieldCurve bumpYieldCurve(final ISDACompliantYieldCurveBuild builder, final double[] rates, final double[] bumpAmounts) {
  //    final int n = bumpAmounts.length;
  //    ArgumentChecker.isTrue(n == rates.length, "rates length does not match bumpAmounts");
  //    final double[] bumped = new double[n];
  //    System.arraycopy(rates, 0, bumped, 0, n);
  //    for (int i = 0; i < n; i++) {
  //      bumped[i] += bumpAmounts[i];
  //    }
  //    return builder.build(bumped);
  //  }

}
