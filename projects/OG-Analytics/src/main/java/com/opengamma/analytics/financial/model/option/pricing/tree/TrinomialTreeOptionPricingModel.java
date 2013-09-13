/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import com.google.common.primitives.Doubles;
import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class TrinomialTreeOptionPricingModel extends TreeOptionPricingModel {

  @Override
  public double getPrice(LatticeSpecification lattice, OptionFunctionProvider1D function, double spot, double volatility, double interestRate, double dividend) {
    ArgumentChecker.notNull(lattice, "lattice");
    ArgumentChecker.notNull(function, "function");
    ArgumentChecker.isTrue(spot > 0., "Spot should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(spot), "Spot should be finite");
    ArgumentChecker.isTrue(volatility > 0., "volatility should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(volatility), "volatility should be finite");
    ArgumentChecker.isTrue(Doubles.isFinite(interestRate), "interestRate should be finite");
    ArgumentChecker.isTrue(Doubles.isFinite(dividend), "dividend should be finite");

    final LatticeSpecification modLattice = (lattice instanceof TimeVaryingLatticeSpecification) ? new TrigeorgisLatticeSpecification() : lattice;
    if (function instanceof BarrierOptionFunctionProvider) {
      final BarrierOptionFunctionProvider barrierFunction = (BarrierOptionFunctionProvider) function;
      if (barrierFunction.getChecker().checkOut(spot) || barrierFunction.getChecker().checkStrikeBehindBarrier()) {
        return 0.;
      }
    }

    final int nSteps = function.getNumberOfSteps();
    final double strike = function.getStrike();
    final double timeToExpiry = function.getTimeToExpiry();

    final double dt = timeToExpiry / nSteps;
    final double discount = Math.exp(-interestRate * dt);
    final double[] params = modLattice.getParametersTrinomial(spot, strike, timeToExpiry, volatility, interestRate - dividend, nSteps, dt);
    final double middleFactor = params[1];
    final double downFactor = params[2];
    final double upProbability = params[3];
    final double middleProbability = params[4];
    final double downProbability = params[5];
    final double middleOverDown = middleFactor / downFactor;
    ArgumentChecker.isTrue(upProbability > 0., "upProbability should be greater than 0.");
    ArgumentChecker.isTrue(upProbability < 1., "upProbability should be smaller than 1.");
    ArgumentChecker.isTrue(middleProbability > 0., "middleProbability should be greater than 0.");
    ArgumentChecker.isTrue(middleProbability < 1., "middleProbability should be smaller than 1.");
    ArgumentChecker.isTrue(downProbability > 0., "downProbability should be greater than 0.");

    final double assetPrice = spot * Math.pow(downFactor, nSteps);
    double[] values = function.getPayoffAtExpiryTrinomial(assetPrice, middleOverDown);
    for (int i = nSteps - 1; i > -1; --i) {
      values = function.getNextOptionValues(discount, upProbability, middleProbability, downProbability, values, spot, 0., downFactor, middleOverDown, i);
    }

    return values[0];
  }

  @Override
  public double getPrice(OptionFunctionProvider1D function, double spot, double[] volatility, double[] interestRate, double[] dividend) {
    return 0;
  }

  @Override
  public double getPrice(LatticeSpecification lattice, OptionFunctionProvider1D function, double spot, double volatility, double interestRate, DividendFunctionProvider dividend) {
    ArgumentChecker.notNull(lattice, "lattice");
    ArgumentChecker.notNull(function, "function");
    ArgumentChecker.notNull(dividend, "dividend");

    ArgumentChecker.isTrue(spot > 0., "Spot should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(spot), "Spot should be finite");
    ArgumentChecker.isTrue(volatility > 0., "volatility should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(volatility), "volatility should be finite");
    ArgumentChecker.isTrue(Doubles.isFinite(interestRate), "interestRate should be finite");

    final LatticeSpecification modLattice = (lattice instanceof TimeVaryingLatticeSpecification) ? new TrigeorgisLatticeSpecification() : lattice;
    if (function instanceof BarrierOptionFunctionProvider) {
      final BarrierOptionFunctionProvider barrierFunction = (BarrierOptionFunctionProvider) function;
      if (barrierFunction.getChecker().checkOut(spot) || barrierFunction.getChecker().checkStrikeBehindBarrier()) {
        return 0.;
      }
    }

    final int nSteps = function.getNumberOfSteps();
    final double strike = function.getStrike();
    final double timeToExpiry = function.getTimeToExpiry();

    final double dt = timeToExpiry / nSteps;
    ArgumentChecker.isTrue(dividend.checkTimeSteps(dt), "Number of steps is too small");
    ArgumentChecker.isTrue(dividend.checkDividendBeforeExpiry(timeToExpiry), "Dividend is paid after expiry");

    final double discount = Math.exp(-interestRate * dt);
    final double[] params = modLattice.getParametersTrinomial(spot, strike, timeToExpiry, volatility, interestRate, nSteps, dt);
    final double middleFactor = params[1];
    final double downFactor = params[2];
    final double upProbability = params[3];
    final double middleProbability = params[4];
    final double downProbability = params[5];
    final double middleOverDown = middleFactor / downFactor;
    ArgumentChecker.isTrue(upProbability > 0., "upProbability should be greater than 0.");
    ArgumentChecker.isTrue(upProbability < 1., "upProbability should be smaller than 1.");
    ArgumentChecker.isTrue(middleProbability > 0., "middleProbability should be greater than 0.");
    ArgumentChecker.isTrue(middleProbability < 1., "middleProbability should be smaller than 1.");
    ArgumentChecker.isTrue(downProbability > 0., "downProbability should be greater than 0.");

    final int[] divSteps = dividend.getDividendSteps(dt);

    double assetPriceBase = dividend.spotModifier(spot, interestRate);
    final double assetPriceTerminal = assetPriceBase * Math.pow(downFactor, nSteps);
    double[] values = function.getPayoffAtExpiryTrinomial(assetPriceTerminal, middleOverDown);

    int counter = 0;
    final int nDivs = dividend.getNumberOfDividends();

    if (dividend instanceof ProportionalDividendFunctionProvider) {
      for (int i = nSteps - 1; i > -1; --i) {
        for (int k = nDivs - 1 - counter; k > -1; --k) {
          if (i == divSteps[k]) {
            assetPriceBase = dividend.dividendCorrections(assetPriceBase, 0., 0., k);
            ++counter;
          }
        }
        values = function.getNextOptionValues(discount, upProbability, middleProbability, downProbability, values, assetPriceBase, 0., downFactor, middleOverDown, i);
      }
    } else {
      double sumDiscountDiv = 0.;
      for (int i = nSteps - 1; i > -1; --i) {
        sumDiscountDiv *= Math.exp(-interestRate * dt);
        for (int k = nDivs - 1 - counter; k > -1; --k) {
          if (i == divSteps[k]) {
            sumDiscountDiv = dividend.dividendCorrections(sumDiscountDiv, interestRate, dt * i, k);
            ++counter;
          }
        }
        values = function.getNextOptionValues(discount, upProbability, middleProbability, downProbability, values, assetPriceBase, sumDiscountDiv, downFactor, middleOverDown, i);
      }
    }

    return values[0];
  }

  @Override
  public double getPrice(OptionFunctionProvider2D function, double spot1, double spot2, double volatility1, double volatility2, double correlation, double interestRate, double dividend1,
      double dividend2) {
    return 0;
  }

  @Override
  public GreekResultCollection getGreeks(LatticeSpecification lattice, OptionFunctionProvider1D function, double spot, double volatility, double interestRate, double dividend) {
    ArgumentChecker.notNull(lattice, "lattice");
    ArgumentChecker.notNull(function, "function");
    ArgumentChecker.isTrue(spot > 0., "Spot should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(spot), "Spot should be finite");
    ArgumentChecker.isTrue(volatility > 0., "volatility should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(volatility), "volatility should be finite");
    ArgumentChecker.isTrue(Doubles.isFinite(interestRate), "interestRate should be finite");
    ArgumentChecker.isTrue(Doubles.isFinite(dividend), "dividend should be finite");

    final GreekResultCollection collection = new GreekResultCollection();
    final LatticeSpecification modLattice = (lattice instanceof TimeVaryingLatticeSpecification) ? new TrigeorgisLatticeSpecification() : lattice;

    final int nSteps = function.getNumberOfSteps();
    final double strike = function.getStrike();
    final double timeToExpiry = function.getTimeToExpiry();

    final double dt = timeToExpiry / nSteps;
    final double discount = Math.exp(-interestRate * dt);
    final double[] params = modLattice.getParametersTrinomial(spot, strike, timeToExpiry, volatility, interestRate - dividend, nSteps, dt);
    final double upFactor = params[0];
    final double middleFactor = params[1];
    final double downFactor = params[2];
    final double upProbability = params[3];
    final double middleProbability = params[4];
    final double downProbability = params[5];
    final double middleOverDown = middleFactor / downFactor;
    ArgumentChecker.isTrue(upProbability > 0., "upProbability should be greater than 0.");
    ArgumentChecker.isTrue(upProbability < 1., "upProbability should be smaller than 1.");
    ArgumentChecker.isTrue(middleProbability > 0., "middleProbability should be greater than 0.");
    ArgumentChecker.isTrue(middleProbability < 1., "middleProbability should be smaller than 1.");
    ArgumentChecker.isTrue(downProbability > 0., "downProbability should be greater than 0.");

    final double assetPrice = spot * Math.pow(downFactor, nSteps);
    double[] values = function.getPayoffAtExpiryTrinomial(assetPrice, middleOverDown);
    final double[] res = new double[4];

    final double[] pForDelta = new double[] {spot * downFactor, spot * middleFactor, spot * upFactor };
    final double[] pForGamma = new double[] {pForDelta[0] * downFactor, pForDelta[0] * middleFactor, pForDelta[1] * middleFactor, pForDelta[2] * middleFactor, pForDelta[2] * upFactor };

    for (int i = nSteps - 1; i > -1; --i) {
      values = function.getNextOptionValues(discount, upProbability, middleProbability, downProbability, values, spot, 0., downFactor, middleOverDown, i);
      if (i == 2) {
        final double delta1 = (values[4] - values[3]) / (pForGamma[4] - pForGamma[3]);
        final double delta2 = (values[3] - values[2]) / (pForGamma[3] - pForGamma[2]);
        final double delta3 = (values[2] - values[1]) / (pForGamma[2] - pForGamma[1]);
        final double delta4 = (values[1] - values[0]) / (pForGamma[1] - pForGamma[0]);
        final double gamma1 = 2. * (delta1 - delta2) / (pForGamma[4] - pForGamma[2]);
        final double gamma2 = 2. * (delta2 - delta3) / (pForGamma[3] - pForGamma[1]);
        final double gamma3 = 2. * (delta3 - delta4) / (pForGamma[2] - pForGamma[0]);
        res[2] = (gamma1 + gamma2 + gamma3) / 3.;
        res[3] = values[2];
      }
      if (i == 1) {
        final double delta1 = (values[1] - values[0]) / (pForDelta[1] - pForDelta[0]);
        final double delta2 = (values[2] - values[1]) / (pForDelta[2] - pForDelta[1]);
        res[1] = 0.5 * (delta1 + delta2);
      }
    }
    res[0] = values[0];
    res[3] = modLattice.getTheta(spot, volatility, interestRate, dividend, dt, res);

    collection.put(Greek.FAIR_PRICE, res[0]);
    collection.put(Greek.DELTA, res[1]);
    collection.put(Greek.GAMMA, res[2]);
    collection.put(Greek.THETA, res[3]);

    return collection;
  }

  @Override
  public GreekResultCollection getGreeks(OptionFunctionProvider1D function, double spot, double[] volatility, double[] interestRate, double[] dividend) {
    return null;
  }

  @Override
  public GreekResultCollection getGreeks(LatticeSpecification lattice, OptionFunctionProvider1D function, double spot, double volatility, double interestRate, DividendFunctionProvider dividend) {
    ArgumentChecker.notNull(lattice, "lattice");
    ArgumentChecker.notNull(function, "function");
    ArgumentChecker.notNull(dividend, "dividend");

    ArgumentChecker.isTrue(spot > 0., "Spot should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(spot), "Spot should be finite");
    ArgumentChecker.isTrue(volatility > 0., "volatility should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(volatility), "volatility should be finite");
    ArgumentChecker.isTrue(Doubles.isFinite(interestRate), "interestRate should be finite");

    final GreekResultCollection collection = new GreekResultCollection();
    final LatticeSpecification modLattice = (lattice instanceof TimeVaryingLatticeSpecification) ? new TrigeorgisLatticeSpecification() : lattice;

    final int nSteps = function.getNumberOfSteps();
    final double strike = function.getStrike();
    final double timeToExpiry = function.getTimeToExpiry();

    final double dt = timeToExpiry / nSteps;
    ArgumentChecker.isTrue(dividend.checkTimeSteps(dt), "Number of steps is too small");
    ArgumentChecker.isTrue(dividend.checkDividendBeforeExpiry(timeToExpiry), "Dividend is paid after expiry");

    final double discount = Math.exp(-interestRate * dt);
    final double[] params = modLattice.getParametersTrinomial(spot, strike, timeToExpiry, volatility, interestRate, nSteps, dt);
    final double upFactor = params[0];
    final double middleFactor = params[1];
    final double downFactor = params[2];
    final double upProbability = params[3];
    final double middleProbability = params[4];
    final double downProbability = params[5];
    final double middleOverDown = middleFactor / downFactor;
    ArgumentChecker.isTrue(upProbability > 0., "upProbability should be greater than 0.");
    ArgumentChecker.isTrue(upProbability < 1., "upProbability should be smaller than 1.");
    ArgumentChecker.isTrue(middleProbability > 0., "middleProbability should be greater than 0.");
    ArgumentChecker.isTrue(middleProbability < 1., "middleProbability should be smaller than 1.");
    ArgumentChecker.isTrue(downProbability > 0., "downProbability should be greater than 0.");

    final int[] divSteps = dividend.getDividendSteps(dt);

    double assetPriceBase = dividend.spotModifier(spot, interestRate);
    final double assetPriceTerminal = assetPriceBase * Math.pow(downFactor, nSteps);
    double[] values = function.getPayoffAtExpiryTrinomial(assetPriceTerminal, middleOverDown);

    int counter = 0;
    final int nDivs = dividend.getNumberOfDividends();
    final double[] res = new double[4];

    if (dividend instanceof ProportionalDividendFunctionProvider) {
      for (int i = nSteps - 1; i > -1; --i) {
        for (int k = nDivs - 1 - counter; k > -1; --k) {
          if (i == divSteps[k]) {
            assetPriceBase = dividend.dividendCorrections(assetPriceBase, 0., 0., k);
            ++counter;
          }
        }
        values = function.getNextOptionValues(discount, upProbability, middleProbability, downProbability, values, assetPriceBase, 0., downFactor, middleOverDown, i);
        if (i == 2) {
          final double[] pForGamma = dividend.getAssetPricesForGamma(spot, interestRate, divSteps, upFactor, middleFactor, downFactor, 0.);
          final double delta1 = (values[4] - values[3]) / (pForGamma[4] - pForGamma[3]);
          final double delta2 = (values[3] - values[2]) / (pForGamma[3] - pForGamma[2]);
          final double delta3 = (values[2] - values[1]) / (pForGamma[2] - pForGamma[1]);
          final double delta4 = (values[1] - values[0]) / (pForGamma[1] - pForGamma[0]);
          final double gamma1 = 2. * (delta1 - delta2) / (pForGamma[4] - pForGamma[2]);
          final double gamma2 = 2. * (delta2 - delta3) / (pForGamma[3] - pForGamma[1]);
          final double gamma3 = 2. * (delta3 - delta4) / (pForGamma[2] - pForGamma[0]);
          res[2] = (gamma1 + gamma2 + gamma3) / 3.;
          res[3] = values[2];
        }
        if (i == 1) {
          final double[] pForDelta = dividend.getAssetPricesForDelta(spot, interestRate, divSteps, upFactor, middleFactor, downFactor, 0.);
          final double delta1 = (values[1] - values[0]) / (pForDelta[1] - pForDelta[0]);
          final double delta2 = (values[2] - values[1]) / (pForDelta[2] - pForDelta[1]);
          res[1] = 0.5 * (delta1 + delta2);
        }
      }
    } else {
      double sumDiscountDiv = 0.;
      for (int i = nSteps - 1; i > -1; --i) {
        sumDiscountDiv *= Math.exp(-interestRate * dt);
        for (int k = nDivs - 1 - counter; k > -1; --k) {
          if (i == divSteps[k]) {
            sumDiscountDiv = dividend.dividendCorrections(sumDiscountDiv, interestRate, dt * i, k);
            ++counter;
          }
        }
        values = function.getNextOptionValues(discount, upProbability, middleProbability, downProbability, values, assetPriceBase, sumDiscountDiv, downFactor, middleOverDown, i);
        if (i == 2) {
          final double[] pForGamma = dividend.getAssetPricesForGamma(assetPriceBase, interestRate, divSteps, upFactor, middleFactor, downFactor, sumDiscountDiv);
          final double delta1 = (values[4] - values[3]) / (pForGamma[4] - pForGamma[3]);
          final double delta2 = (values[3] - values[2]) / (pForGamma[3] - pForGamma[2]);
          final double delta3 = (values[2] - values[1]) / (pForGamma[2] - pForGamma[1]);
          final double delta4 = (values[1] - values[0]) / (pForGamma[1] - pForGamma[0]);
          final double gamma1 = 2. * (delta1 - delta2) / (pForGamma[4] - pForGamma[2]);
          final double gamma2 = 2. * (delta2 - delta3) / (pForGamma[3] - pForGamma[1]);
          final double gamma3 = 2. * (delta3 - delta4) / (pForGamma[2] - pForGamma[0]);
          res[2] = (gamma1 + gamma2 + gamma3) / 3.;
          res[3] = values[2];
        }
        if (i == 1) {
          final double[] pForDelta = dividend.getAssetPricesForDelta(assetPriceBase, interestRate, divSteps, upFactor, middleFactor, downFactor, sumDiscountDiv);
          final double delta1 = (values[1] - values[0]) / (pForDelta[1] - pForDelta[0]);
          final double delta2 = (values[2] - values[1]) / (pForDelta[2] - pForDelta[1]);
          res[1] = 0.5 * (delta1 + delta2);
        }
      }
    }
    res[0] = values[0];
    res[3] = modLattice.getTheta(spot, volatility, interestRate, 0., dt, res);
    collection.put(Greek.FAIR_PRICE, res[0]);
    collection.put(Greek.DELTA, res[1]);
    collection.put(Greek.GAMMA, res[2]);
    collection.put(Greek.THETA, res[3]);

    return collection;
  }

  @Override
  public double[] getGreeks(OptionFunctionProvider2D function, double spot1, double spot2, double volatility1, double volatility2, double correlation, double interestRate, double dividend1,
      double dividend2) {
    return null;
  }

}
