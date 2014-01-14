/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.calculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.calculator.discounting.CashFlowEquivalentCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscountingDecoratedIssuer;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.RidderSingleRootFinder;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Computes the par rate for different instrument. The meaning of "par rate" is instrument dependent.
 */
public final class FuturesPriceCurveSensitivityHullWhiteIssuerCalculator extends InstrumentDerivativeVisitorAdapter<HullWhiteIssuerProviderInterface, MulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final FuturesPriceCurveSensitivityHullWhiteIssuerCalculator INSTANCE = new FuturesPriceCurveSensitivityHullWhiteIssuerCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static FuturesPriceCurveSensitivityHullWhiteIssuerCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private FuturesPriceCurveSensitivityHullWhiteIssuerCalculator() {
  }

  /** The number of points used in the numerical integration process.*/
  private static final int DEFAULT_NB_POINTS = 81;
  /** The normal distribution implementation. */
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  /** The cash flow equivalent calculator used in computations. */
  private static final CashFlowEquivalentCalculator CFEC = CashFlowEquivalentCalculator.getInstance();
  /** The model used in computations. */
  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();

  //     -----     Futures     -----

  @Override
  public MulticurveSensitivity visitBondFuturesSecurity(final BondFuturesSecurity futures, final HullWhiteIssuerProviderInterface multicurve) {
    return visitBondFuturesSecurity(futures, multicurve, DEFAULT_NB_POINTS);
  }

  /**
   * Computes the future price curve sensitivity.
   * @param futures The future security.
   * @param data The curve and Hull-White parameters.
   * @param nbPoint The number of point in the numerical cross estimation.
   * @return The curve sensitivity.
   */
  public MulticurveSensitivity visitBondFuturesSecurity(final BondFuturesSecurity futures, final HullWhiteIssuerProviderInterface data, final int nbPoint) {
    ArgumentChecker.notNull(futures, "Future");
    ArgumentChecker.notNull(data, "Hull-White data bundle");
    final int nbBond = futures.getDeliveryBasketAtDeliveryDate().length;
    final LegalEntity issuer = futures.getDeliveryBasketAtDeliveryDate()[0].getIssuerEntity();
    final HullWhiteOneFactorPiecewiseConstantParameters parameters = data.getHullWhiteParameters();
    final IssuerProviderInterface issuerProvider = data.getIssuerProvider();
    final MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(issuerProvider, futures.getCurrency(), issuer);

    final double expiry = futures.getNoticeLastTime();
    final double delivery = futures.getDeliveryLastTime();
    final double dfdelivery = data.getIssuerProvider().getDiscountFactor(issuer, delivery);
    // Constructing non-homogeneous point series for the numerical estimations.
    final int nbPtWing = ((int) Math.floor(nbPoint / 20.)); // Number of point on each wing.
    final int nbPtCenter = nbPoint - 2 * nbPtWing;
    final double prob = 1.0 / (2.0 * nbPtCenter);
    final double xStart = NORMAL.getInverseCDF(prob);
    final double[] x = new double[nbPoint];
    for (int loopwing = 0; loopwing < nbPtWing; loopwing++) {
      x[loopwing] = xStart * (1.0 + (nbPtWing - loopwing) / 2.0);
      x[nbPoint - 1 - loopwing] = -xStart * (1.0 + (nbPtWing - loopwing) / 2.0);
    }
    for (int loopcent = 0; loopcent < nbPtCenter; loopcent++) {
      x[nbPtWing + loopcent] = xStart + loopcent * (-2.0 * xStart) / (nbPtCenter - 1);
    }
    // Figures for each bond
    final double[][] cfTime = new double[nbBond][];
    final double[][] df = new double[nbBond][];
    final double[][] alpha = new double[nbBond][];
    final double[][] beta = new double[nbBond][];
    final double[][] cfaAdjusted = new double[nbBond][];
    final double[] e = new double[nbBond];
    final double[][] pv = new double[nbPoint][nbBond];
    final AnnuityPaymentFixed[] cf = new AnnuityPaymentFixed[nbBond];
    for (int loopbnd = 0; loopbnd < nbBond; loopbnd++) {
      cf[loopbnd] = futures.getDeliveryBasketAtDeliveryDate()[loopbnd].accept(CFEC, multicurvesDecorated);
      final int nbCf = cf[loopbnd].getNumberOfPayments();
      cfTime[loopbnd] = new double[nbCf];
      df[loopbnd] = new double[nbCf];
      alpha[loopbnd] = new double[nbCf];
      beta[loopbnd] = new double[nbCf];
      cfaAdjusted[loopbnd] = new double[nbCf];
      for (int loopcf = 0; loopcf < nbCf; loopcf++) {
        cfTime[loopbnd][loopcf] = cf[loopbnd].getNthPayment(loopcf).getPaymentTime();
        df[loopbnd][loopcf] = issuerProvider.getDiscountFactor(issuer, cfTime[loopbnd][loopcf]);
        alpha[loopbnd][loopcf] = MODEL.alpha(parameters, 0.0, expiry, delivery, cfTime[loopbnd][loopcf]);
        beta[loopbnd][loopcf] = MODEL.futuresConvexityFactor(parameters, expiry, cfTime[loopbnd][loopcf], delivery);
        cfaAdjusted[loopbnd][loopcf] = df[loopbnd][loopcf] / dfdelivery * beta[loopbnd][loopcf] * cf[loopbnd].getNthPayment(loopcf).getAmount() / futures.getConversionFactor()[loopbnd];
        for (int looppt = 0; looppt < nbPoint; looppt++) {
          pv[looppt][loopbnd] += cfaAdjusted[loopbnd][loopcf] * Math.exp(-alpha[loopbnd][loopcf] * alpha[loopbnd][loopcf] / 2.0 - alpha[loopbnd][loopcf] * x[looppt]);
        }
      }
      e[loopbnd] = futures.getDeliveryBasketAtDeliveryDate()[loopbnd].getAccruedInterest() / futures.getConversionFactor()[loopbnd];
      for (int looppt = 0; looppt < nbPoint; looppt++) {
        pv[looppt][loopbnd] -= e[loopbnd];
      }
    }
    // Minimum: create a list of index of the CTD in each interval and a first estimate of the crossing point (x[]).
    final double[] pvMin = new double[nbPoint];
    final int[] indMin = new int[nbPoint];
    for (int looppt = 0; looppt < nbPoint; looppt++) {
      pvMin[looppt] = Double.POSITIVE_INFINITY;
      for (int loopbnd = 0; loopbnd < nbBond; loopbnd++) {
        if (pv[looppt][loopbnd] < pvMin[looppt]) {
          pvMin[looppt] = pv[looppt][loopbnd];
          indMin[looppt] = loopbnd;
        }
      }
    }
    final ArrayList<Double> refx = new ArrayList<>();
    final ArrayList<Integer> ctd = new ArrayList<>();
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
    final int nbInt = ctd.size();
    final double[] kappa = new double[nbInt - 1];
    //    double price = 0.0;
    if (nbInt != 1) {
      // The intersections
      final BracketRoot bracketer = new BracketRoot();
      final double accuracy = 1.0E-8;
      final RidderSingleRootFinder rootFinder = new RidderSingleRootFinder(accuracy);
      for (int loopint = 1; loopint < nbInt; loopint++) {
        final BondDifference cross = new BondDifference(cfaAdjusted[ctd.get(loopint - 1)], alpha[ctd.get(loopint - 1)], e[ctd.get(loopint - 1)], cfaAdjusted[ctd.get(loopint)],
            alpha[ctd.get(loopint)], e[ctd.get(loopint)]);
        final double[] range = bracketer.getBracketedPoints(cross, refx.get(loopint - 1) - 0.01, refx.get(loopint - 1) + 0.01);
        kappa[loopint - 1] = rootFinder.getRoot(cross, range[0], range[1]);
      }
    }

    // === Backward Sweep ===
    final double priceBar = 1.0;
    final double[][] cfaAdjustedBar = new double[nbBond][];
    final double[][] dfBar = new double[nbBond][];
    for (int loopbnd = 0; loopbnd < nbBond; loopbnd++) {
      final int nbCf = cf[loopbnd].getNumberOfPayments();
      cfaAdjustedBar[loopbnd] = new double[nbCf];
      dfBar[loopbnd] = new double[nbCf];
    }
    double dfdeliveryBar = 0.0;
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    final List<DoublesPair> listCredit = new ArrayList<>();
    if (nbInt == 1) {
      for (int loopcf = 0; loopcf < cfaAdjusted[ctd.get(0)].length; loopcf++) {
        cfaAdjustedBar[ctd.get(0)][loopcf] = priceBar;
        dfBar[ctd.get(0)][loopcf] = beta[ctd.get(0)][loopcf] / dfdelivery * cf[ctd.get(0)].getNthPayment(loopcf).getAmount() / futures.getConversionFactor()[ctd.get(0)]
            * cfaAdjustedBar[ctd.get(0)][loopcf];
        listCredit.add(DoublesPair.of(cfTime[ctd.get(0)][loopcf], -cfTime[ctd.get(0)][loopcf] * df[ctd.get(0)][loopcf] * dfBar[ctd.get(0)][loopcf]));
        dfdeliveryBar += -cfaAdjusted[ctd.get(0)][loopcf] / dfdelivery * cfaAdjustedBar[ctd.get(0)][loopcf];
      }
      listCredit.add(DoublesPair.of(delivery, -delivery * dfdelivery * dfdeliveryBar));
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
          dfBar[loopbnd][loopcf] = beta[loopbnd][loopcf] / dfdelivery * cf[loopbnd].getNthPayment(loopcf).getAmount() / futures.getConversionFactor()[loopbnd] * cfaAdjustedBar[loopbnd][loopcf];
          listCredit.add(DoublesPair.of(cfTime[loopbnd][loopcf], -cfTime[loopbnd][loopcf] * df[loopbnd][loopcf] * dfBar[loopbnd][loopcf]));
          dfdeliveryBar += -cfaAdjusted[loopbnd][loopcf] / dfdelivery * cfaAdjustedBar[loopbnd][loopcf];
        }
      }
      listCredit.add(DoublesPair.of(delivery, -delivery * dfdelivery * dfdeliveryBar));
    }
    resultMap.put(data.getIssuerProvider().getName(issuer), listCredit);
    return MulticurveSensitivity.ofYieldDiscounting(resultMap);
  }

  /**
   * Internal class to estimate the price difference between two bonds (used for bond futures).
   */
  private static final class BondDifference extends Function1D<Double, Double> {
    private final double[] _cfa1;
    private final double[] _alpha1;
    private final double _e1;
    private final double[] _cfa2;
    private final double[] _alpha2;
    private final double _e2;

    public BondDifference(final double[] cfa1, final double[] alpha1, final double e1, final double[] cfa2, final double[] alpha2, final double e2) {
      _cfa1 = cfa1;
      _alpha1 = alpha1;
      _e1 = e1;
      _cfa2 = cfa2;
      _alpha2 = alpha2;
      _e2 = e2;
    }

    @Override
    public Double evaluate(final Double x) {
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
