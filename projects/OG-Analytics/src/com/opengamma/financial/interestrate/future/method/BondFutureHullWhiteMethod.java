/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.CashFlowEquivalentCalculator;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.future.definition.BondFuture;
import com.opengamma.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.rootfinding.BracketRoot;
import com.opengamma.math.rootfinding.RidderSingleRootFinder;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute the price of bond future using the Hull-White one factor model to estimate the delivery option.
 * <P> Reference: Henrard, M. Bonds futures and their options: more than the cheapest-to-deliver; quality option and margining. Journal of Fixed Income, 2006, 16, 62-75
 */
public final class BondFutureHullWhiteMethod extends BondFutureMethod {

  /**
   * The number of points used in the numerical integration process.
   */
  private static final int DEFAULT_NB_POINTS = 81;
  /**
   * The normal distribution implementation.
   */
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  /**
   * The cash flow equivalent calculator used in computations.
   */
  private static final CashFlowEquivalentCalculator CFEC = CashFlowEquivalentCalculator.getInstance();
  /**
   * The model used in computations.
   */
  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();

  /**
   * Creates the method unique instance.
   */
  private static final BondFutureHullWhiteMethod INSTANCE = new BondFutureHullWhiteMethod();

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static BondFutureHullWhiteMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private BondFutureHullWhiteMethod() {
  }

  /**
   * Computes the future price from the curves used to price the underlying bonds and a Hull-White one factor model.
   * @param future The future security.
   * @param hwData The curve and Hull-White parameters.
   * @param nbPoint The number of point in the numerical cross estimation.
   * @return The future price.
   */
  public double price(final BondFuture future, final HullWhiteOneFactorPiecewiseConstantDataBundle hwData, final int nbPoint) {
    Validate.notNull(future, "Future");
    Validate.notNull(hwData, "Hull-White data bundle");
    final int nbBond = future.getDeliveryBasket().length;
    YieldAndDiscountCurve bndCurve = hwData.getCurve(future.getDeliveryBasket()[0].getDiscountingCurveName());
    double expiry = future.getNoticeLastTime();
    double delivery = future.getDeliveryLastTime();
    double dfdelivery = bndCurve.getDiscountFactor(delivery);
    // Constructing non-homogeneous point series for the numerical estimations.
    final int nbPtWing = ((int) Math.floor(nbPoint / 20)); // Number of point on each wing.
    final int nbPtCenter = nbPoint - 2 * nbPtWing;
    final double prob = 1.0 / (2.0 * nbPtCenter);
    final double xStart = NORMAL.getInverseCDF(prob);
    double[] x = new double[nbPoint];
    for (int loopwing = 0; loopwing < nbPtWing; loopwing++) {
      x[loopwing] = xStart * (1.0 + (nbPtWing - loopwing) / 2.0);
      x[nbPoint - 1 - loopwing] = -xStart * (1.0 + (nbPtWing - loopwing) / 2.0);
    }
    for (int loopcent = 0; loopcent < nbPtCenter; loopcent++) {
      x[nbPtWing + loopcent] = xStart + loopcent * (-2.0 * xStart) / (nbPtCenter - 1);
    }
    // Figures for each bond
    double[][] cfTime = new double[nbBond][];
    double[][] df = new double[nbBond][];
    double[][] alpha = new double[nbBond][];
    double[][] beta = new double[nbBond][];
    double[][] cfaAdjusted = new double[nbBond][];
    double[] e = new double[nbBond];
    double[][] pv = new double[nbPoint][nbBond];
    AnnuityPaymentFixed[] cf = new AnnuityPaymentFixed[nbBond];
    for (int loopbnd = 0; loopbnd < nbBond; loopbnd++) {
      cf[loopbnd] = CFEC.visit(future.getDeliveryBasket()[loopbnd], hwData);
      int nbCf = cf[loopbnd].getNumberOfPayments();
      cfTime[loopbnd] = new double[nbCf];
      df[loopbnd] = new double[nbCf];
      alpha[loopbnd] = new double[nbCf];
      beta[loopbnd] = new double[nbCf];
      cfaAdjusted[loopbnd] = new double[nbCf];
      for (int loopcf = 0; loopcf < nbCf; loopcf++) {
        cfTime[loopbnd][loopcf] = cf[loopbnd].getNthPayment(loopcf).getPaymentTime();
        df[loopbnd][loopcf] = bndCurve.getDiscountFactor(cfTime[loopbnd][loopcf]);
        alpha[loopbnd][loopcf] = MODEL.alpha(0.0, expiry, delivery, cfTime[loopbnd][loopcf], hwData.getHullWhiteParameter());
        beta[loopbnd][loopcf] = MODEL.futureConvexityFactor(expiry, cfTime[loopbnd][loopcf], delivery, hwData.getHullWhiteParameter());
        cfaAdjusted[loopbnd][loopcf] = df[loopbnd][loopcf] / dfdelivery * beta[loopbnd][loopcf] * cf[loopbnd].getNthPayment(loopcf).getAmount() / future.getConversionFactor()[loopbnd];
        for (int looppt = 0; looppt < nbPoint; looppt++) {
          pv[looppt][loopbnd] += cfaAdjusted[loopbnd][loopcf] * Math.exp(-alpha[loopbnd][loopcf] * alpha[loopbnd][loopcf] / 2.0 - alpha[loopbnd][loopcf] * x[looppt]);
        }
      }
      e[loopbnd] = future.getDeliveryBasket()[loopbnd].getAccruedInterest() / future.getConversionFactor()[loopbnd];
      for (int looppt = 0; looppt < nbPoint; looppt++) {
        pv[looppt][loopbnd] -= e[loopbnd];
      }
    }
    // Minimum: create a list of index of the CTD in each interval and a first estimate of the crossing point (x[]).
    double[] pvMin = new double[nbPoint];
    int[] indMin = new int[nbPoint];
    for (int looppt = 0; looppt < nbPoint; looppt++) {
      pvMin[looppt] = Double.POSITIVE_INFINITY;
      for (int loopbnd = 0; loopbnd < nbBond; loopbnd++) {
        if (pv[looppt][loopbnd] < pvMin[looppt]) {
          pvMin[looppt] = pv[looppt][loopbnd];
          indMin[looppt] = loopbnd;
        }
      }
    }
    ArrayList<Double> refx = new ArrayList<Double>();
    ArrayList<Integer> ctd = new ArrayList<Integer>();
    int lastInd = indMin[0];
    ctd.add(indMin[0]);
    for (int looppt = 1; looppt < nbPoint; looppt++) {
      if (indMin[looppt] != lastInd) {
        ctd.add(indMin[looppt]);
        lastInd = indMin[looppt];
        refx.add(x[looppt]);
      }
    }

    // Sum on each interval
    int nbInt = ctd.size();
    double[] kappa = new double[nbInt - 1];
    double price = 0.0;
    if (nbInt == 1) {
      for (int loopcf = 0; loopcf < cfaAdjusted[ctd.get(0)].length; loopcf++) {
        price += cfaAdjusted[ctd.get(0)][loopcf];
      }
      price -= e[ctd.get(0)];
    } else {
      // The intersections
      final BracketRoot bracketer = new BracketRoot();
      double accuracy = 1.0E-8;
      final RidderSingleRootFinder rootFinder = new RidderSingleRootFinder(accuracy);
      for (int loopint = 1; loopint < nbInt; loopint++) {
        BondDifference cross = new BondDifference(cfaAdjusted[ctd.get(loopint - 1)], alpha[ctd.get(loopint - 1)], e[ctd.get(loopint - 1)], cfaAdjusted[ctd.get(loopint)], alpha[ctd.get(loopint)],
            e[ctd.get(loopint)]);
        final double[] range = bracketer.getBracketedPoints(cross, refx.get(loopint - 1) - 0.01, refx.get(loopint - 1) + 0.01);
        kappa[loopint - 1] = rootFinder.getRoot(cross, range[0], range[1]);
      }
      // From -infinity to first cross.
      for (int loopcf = 0; loopcf < cfaAdjusted[ctd.get(0)].length; loopcf++) {
        price += cfaAdjusted[ctd.get(0)][loopcf] * NORMAL.getCDF(kappa[0] + alpha[ctd.get(0)][loopcf]);
      }
      price -= e[ctd.get(0)] * NORMAL.getCDF(kappa[0]);
      // Between cross
      for (int loopint = 1; loopint < nbInt - 1; loopint++) {
        for (int loopcf = 0; loopcf < cfaAdjusted[ctd.get(loopint)].length; loopcf++) {
          price += cfaAdjusted[ctd.get(loopint)][loopcf] * (NORMAL.getCDF(kappa[loopint] + alpha[ctd.get(loopint)][loopcf]) - NORMAL.getCDF(kappa[loopint - 1] + alpha[ctd.get(loopint)][loopcf]));
        }
        price -= e[ctd.get(loopint)] * (NORMAL.getCDF(kappa[loopint]) - NORMAL.getCDF(kappa[loopint - 1]));
      }
      // From last cross to +infinity
      for (int loopcf = 0; loopcf < cfaAdjusted[ctd.get(nbInt - 1)].length; loopcf++) {
        price += cfaAdjusted[ctd.get(nbInt - 1)][loopcf] * (1.0 - NORMAL.getCDF(kappa[nbInt - 2] + alpha[ctd.get(nbInt - 1)][loopcf]));
      }
      price -= e[ctd.get(nbInt - 1)] * (1 - NORMAL.getCDF(kappa[nbInt - 2]));
    }

    return price;
  }

  /**
   * Computes the future price from the curves used to price the underlying bonds and a Hull-White one factor model. The default number of points is used for the numerical search.
   * @param future The future security.
   * @param hwData The curve and Hull-White parameters.
   * @return The future price.
   */
  public double price(final BondFuture future, final HullWhiteOneFactorPiecewiseConstantDataBundle hwData) {
    return price(future, hwData, DEFAULT_NB_POINTS);
  }

  /**
   * Computes the present value of future from the curves using the cheapest-to-deliver and computing the value as a forward.
   * @param future The future.
   * @param curves The yield curves. Should contain the credit and repo curves associated to the instrument.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final BondFuture future, final HullWhiteOneFactorPiecewiseConstantDataBundle curves) {
    Validate.notNull(future, "Future");
    final double futurePrice = price(future, curves);
    final double pv = presentValueFromPrice(future, futurePrice);
    return CurrencyAmount.of(future.getCurrency(), pv);
  }

  /**
   * Computes the present value of future from the curves using the cheapest-to-deliver and computing the value as a forward.
   * @param instrument The future.
   * @param curves The yield curves. Should contain the credit and repo curves associated to the instrument.
   * @return The present value.
   */
  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof BondFuture, "Bond future");
    Validate.isTrue(curves instanceof HullWhiteOneFactorPiecewiseConstantDataBundle, "Bundle should contain Hull-White data");
    return presentValue((BondFuture) instrument, (HullWhiteOneFactorPiecewiseConstantDataBundle) curves);
  }

  /**
   * Computes the future price curve sensitivity.
   * @param future The future security.
   * @param hwData The curve and Hull-White parameters.
   * @param nbPoint The number of point in the numerical cross estimation.
   * @return The curve sensitivity.
   */
  public InterestRateCurveSensitivity priceCurveSensitivity(final BondFuture future, final HullWhiteOneFactorPiecewiseConstantDataBundle hwData, final int nbPoint) {
    Validate.notNull(future, "Future");
    Validate.notNull(hwData, "Hull-White data bundle");
    final int nbBond = future.getDeliveryBasket().length;
    YieldAndDiscountCurve bndCurve = hwData.getCurve(future.getDeliveryBasket()[0].getDiscountingCurveName());
    double expiry = future.getNoticeLastTime();
    double delivery = future.getDeliveryLastTime();
    double dfdelivery = bndCurve.getDiscountFactor(delivery);
    // Constructing non-homogeneous point series for the numerical estimations.
    final int nbPtWing = ((int) Math.floor(nbPoint / 20)); // Number of point on each wing.
    final int nbPtCenter = nbPoint - 2 * nbPtWing;
    final double prob = 1.0 / (2.0 * nbPtCenter);
    final double xStart = NORMAL.getInverseCDF(prob);
    double[] x = new double[nbPoint];
    for (int loopwing = 0; loopwing < nbPtWing; loopwing++) {
      x[loopwing] = xStart * (1.0 + (nbPtWing - loopwing) / 2.0);
      x[nbPoint - 1 - loopwing] = -xStart * (1.0 + (nbPtWing - loopwing) / 2.0);
    }
    for (int loopcent = 0; loopcent < nbPtCenter; loopcent++) {
      x[nbPtWing + loopcent] = xStart + loopcent * (-2.0 * xStart) / (nbPtCenter - 1);
    }
    // Figures for each bond
    double[][] cfTime = new double[nbBond][];
    double[][] df = new double[nbBond][];
    double[][] alpha = new double[nbBond][];
    double[][] beta = new double[nbBond][];
    double[][] cfaAdjusted = new double[nbBond][];
    double[] e = new double[nbBond];
    double[][] pv = new double[nbPoint][nbBond];
    AnnuityPaymentFixed[] cf = new AnnuityPaymentFixed[nbBond];
    for (int loopbnd = 0; loopbnd < nbBond; loopbnd++) {
      cf[loopbnd] = CFEC.visit(future.getDeliveryBasket()[loopbnd], hwData);
      int nbCf = cf[loopbnd].getNumberOfPayments();
      cfTime[loopbnd] = new double[nbCf];
      df[loopbnd] = new double[nbCf];
      alpha[loopbnd] = new double[nbCf];
      beta[loopbnd] = new double[nbCf];
      cfaAdjusted[loopbnd] = new double[nbCf];
      for (int loopcf = 0; loopcf < nbCf; loopcf++) {
        cfTime[loopbnd][loopcf] = cf[loopbnd].getNthPayment(loopcf).getPaymentTime();
        df[loopbnd][loopcf] = bndCurve.getDiscountFactor(cfTime[loopbnd][loopcf]);
        alpha[loopbnd][loopcf] = MODEL.alpha(0.0, expiry, delivery, cfTime[loopbnd][loopcf], hwData.getHullWhiteParameter());
        beta[loopbnd][loopcf] = MODEL.futureConvexityFactor(expiry, cfTime[loopbnd][loopcf], delivery, hwData.getHullWhiteParameter());
        cfaAdjusted[loopbnd][loopcf] = df[loopbnd][loopcf] / dfdelivery * beta[loopbnd][loopcf] * cf[loopbnd].getNthPayment(loopcf).getAmount() / future.getConversionFactor()[loopbnd];
        for (int looppt = 0; looppt < nbPoint; looppt++) {
          pv[looppt][loopbnd] += cfaAdjusted[loopbnd][loopcf] * Math.exp(-alpha[loopbnd][loopcf] * alpha[loopbnd][loopcf] / 2.0 - alpha[loopbnd][loopcf] * x[looppt]);
        }
      }
      e[loopbnd] = future.getDeliveryBasket()[loopbnd].getAccruedInterest() / future.getConversionFactor()[loopbnd];
      for (int looppt = 0; looppt < nbPoint; looppt++) {
        pv[looppt][loopbnd] -= e[loopbnd];
      }
    }
    // Minimum: create a list of index of the CTD in each interval and a first estimate of the crossing point (x[]).
    double[] pvMin = new double[nbPoint];
    int[] indMin = new int[nbPoint];
    for (int looppt = 0; looppt < nbPoint; looppt++) {
      pvMin[looppt] = Double.POSITIVE_INFINITY;
      for (int loopbnd = 0; loopbnd < nbBond; loopbnd++) {
        if (pv[looppt][loopbnd] < pvMin[looppt]) {
          pvMin[looppt] = pv[looppt][loopbnd];
          indMin[looppt] = loopbnd;
        }
      }
    }
    ArrayList<Double> refx = new ArrayList<Double>();
    ArrayList<Integer> ctd = new ArrayList<Integer>();
    int lastInd = indMin[0];
    ctd.add(indMin[0]);
    for (int looppt = 1; looppt < nbPoint; looppt++) {
      if (indMin[looppt] != lastInd) {
        ctd.add(indMin[looppt]);
        lastInd = indMin[looppt];
        refx.add(x[looppt]);
      }
    }
    // Sum on each interval
    int nbInt = ctd.size();
    double[] kappa = new double[nbInt - 1];
    //    double price = 0.0;
    if (nbInt != 1) {
      // The intersections
      final BracketRoot bracketer = new BracketRoot();
      double accuracy = 1.0E-8;
      final RidderSingleRootFinder rootFinder = new RidderSingleRootFinder(accuracy);
      for (int loopint = 1; loopint < nbInt; loopint++) {
        BondDifference cross = new BondDifference(cfaAdjusted[ctd.get(loopint - 1)], alpha[ctd.get(loopint - 1)], e[ctd.get(loopint - 1)], cfaAdjusted[ctd.get(loopint)], alpha[ctd.get(loopint)],
            e[ctd.get(loopint)]);
        final double[] range = bracketer.getBracketedPoints(cross, refx.get(loopint - 1) - 0.01, refx.get(loopint - 1) + 0.01);
        kappa[loopint - 1] = rootFinder.getRoot(cross, range[0], range[1]);
      }
    }

    // === Backward Sweep ===
    double priceBar = 1.0;
    double[][] cfaAdjustedBar = new double[nbBond][];
    double[][] dfBar = new double[nbBond][];
    for (int loopbnd = 0; loopbnd < nbBond; loopbnd++) {
      int nbCf = cf[loopbnd].getNumberOfPayments();
      cfaAdjustedBar[loopbnd] = new double[nbCf];
      dfBar[loopbnd] = new double[nbCf];
    }
    double dfdeliveryBar = 0.0;
    Map<String, List<DoublesPair>> resultMap = new HashMap<String, List<DoublesPair>>();
    List<DoublesPair> listCredit = new ArrayList<DoublesPair>();
    if (nbInt == 1) {
      for (int loopcf = 0; loopcf < cfaAdjusted[ctd.get(0)].length; loopcf++) {
        cfaAdjustedBar[ctd.get(0)][loopcf] = priceBar;
        dfBar[ctd.get(0)][loopcf] = beta[ctd.get(0)][loopcf] / dfdelivery * cf[ctd.get(0)].getNthPayment(loopcf).getAmount() / future.getConversionFactor()[ctd.get(0)]
            * cfaAdjustedBar[ctd.get(0)][loopcf];
        listCredit.add(new DoublesPair(cfTime[ctd.get(0)][loopcf], -cfTime[ctd.get(0)][loopcf] * df[ctd.get(0)][loopcf] * dfBar[ctd.get(0)][loopcf]));
        dfdeliveryBar += -cfaAdjusted[ctd.get(0)][loopcf] / dfdelivery * cfaAdjustedBar[ctd.get(0)][loopcf];
      }
      listCredit.add(new DoublesPair(delivery, -delivery * dfdelivery * dfdeliveryBar));
    } else {
      // From -infinity to first cross.
      for (int loopcf = 0; loopcf < cfaAdjusted[ctd.get(0)].length; loopcf++) {
        cfaAdjustedBar[ctd.get(0)][loopcf] = NORMAL.getCDF(kappa[0] + alpha[ctd.get(0)][loopcf]) * priceBar;
      }
      // Between cross
      for (int loopint = 1; loopint < nbInt - 1; loopint++) {
        for (int loopcf = 0; loopcf < cfaAdjusted[ctd.get(loopint)].length; loopcf++) {
          cfaAdjustedBar[ctd.get(loopint)][loopcf] = (NORMAL.getCDF(kappa[loopint] + alpha[ctd.get(loopint)][loopcf]) - NORMAL.getCDF(kappa[loopint - 1] + alpha[ctd.get(loopint)][loopcf])) * priceBar;
        }
      }
      // From last cross to +infinity
      for (int loopcf = 0; loopcf < cfaAdjusted[ctd.get(nbInt - 1)].length; loopcf++) {
        cfaAdjustedBar[ctd.get(nbInt - 1)][loopcf] = (1.0 - NORMAL.getCDF(kappa[nbInt - 2] + alpha[ctd.get(nbInt - 1)][loopcf])) * priceBar;
      }
      for (int loopbnd = 0; loopbnd < nbBond; loopbnd++) { // Could be reduced to only the ctd intervals.
        for (int loopcf = 0; loopcf < cfaAdjusted[loopbnd].length; loopcf++) {
          dfBar[loopbnd][loopcf] = beta[loopbnd][loopcf] / dfdelivery * cf[loopbnd].getNthPayment(loopcf).getAmount() / future.getConversionFactor()[loopbnd] * cfaAdjustedBar[loopbnd][loopcf];
          listCredit.add(new DoublesPair(cfTime[loopbnd][loopcf], -cfTime[loopbnd][loopcf] * df[loopbnd][loopcf] * dfBar[loopbnd][loopcf]));
          dfdeliveryBar += -cfaAdjusted[loopbnd][loopcf] / dfdelivery * cfaAdjustedBar[loopbnd][loopcf];
        }
      }
      listCredit.add(new DoublesPair(delivery, -delivery * dfdelivery * dfdeliveryBar));
    }
    resultMap.put(future.getDeliveryBasket()[0].getDiscountingCurveName(), listCredit);
    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultMap);
    return result;
  }

  /**
   * Computes the future price curve sensitivity. The default number of points is used for the numerical search.
   * @param future The future derivative.
   * @param hwData The curve and Hull-White parameters.
   * @return The curve sensitivity.
   */
  public InterestRateCurveSensitivity priceCurveSensitivity(final BondFuture future, final HullWhiteOneFactorPiecewiseConstantDataBundle hwData) {
    return priceCurveSensitivity(future, hwData, DEFAULT_NB_POINTS);
  }

  /**
   * Compute the present value sensitivity to rates of a bond future by discounting.
   * @param future The future.
   * @param curves The yield curves. Should contain the credit and repo curves associated. 
   * @return The present value rate sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final BondFuture future, final HullWhiteOneFactorPiecewiseConstantDataBundle curves) {
    Validate.notNull(future, "Future");
    final InterestRateCurveSensitivity priceSensitivity = priceCurveSensitivity(future, curves);
    final InterestRateCurveSensitivity transactionSensitivity = priceSensitivity.multiply(future.getNotional());
    return transactionSensitivity;
  }

  /**
   * Internal class to estimate the price difference between two bonds.
   */
  private class BondDifference extends Function1D<Double, Double> {

    private final double[] _cfa1;
    private final double[] _alpha1;
    private final double _e1;
    private final double[] _cfa2;
    private final double[] _alpha2;
    private final double _e2;

    public BondDifference(double[] cfa1, double[] alpha1, double e1, double[] cfa2, double[] alpha2, double e2) {
      _cfa1 = cfa1;
      _alpha1 = alpha1;
      _e1 = e1;
      _cfa2 = cfa2;
      _alpha2 = alpha2;
      _e2 = e2;
    }

    @Override
    public Double evaluate(Double x) {
      double pv = 0.0;
      for (int loopcf = 0; loopcf < _cfa1.length; loopcf++) {
        pv += _cfa1[loopcf] * Math.exp(-_alpha1[loopcf] * _alpha1[loopcf] / 2.0 - _alpha1[loopcf] * x);
      }
      pv -= _e1;
      for (int loopcf = 0; loopcf < _cfa2.length; loopcf++) {
        pv -= _cfa2[loopcf] * Math.exp(-_alpha2[loopcf] * _alpha2[loopcf] / 2.0 - _alpha2[loopcf] * x);
      }
      pv += _e2;
      return pv;
    }

  }

}
