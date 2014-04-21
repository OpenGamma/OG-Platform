/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import org.apache.commons.lang.Validate;

import com.google.common.primitives.Doubles;
import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.volatility.BlackScholesFormulaRepository;
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

    double[] values = function.getPayoffAtExpiryTrinomial(spot, downFactor, middleOverDown);
    for (int i = nSteps - 1; i > -1; --i) {
      values = function.getNextOptionValues(discount, upProbability, middleProbability, downProbability, values, spot, 0., downFactor, middleOverDown, i);
    }

    return values[0];
  }

  @Override
  public double getPrice(OptionFunctionProvider1D function, double spot, double[] volatility, double[] interestRate, double[] dividend) {
    ArgumentChecker.notNull(function, "function");
    ArgumentChecker.notNull(volatility, "volatility");
    ArgumentChecker.notNull(interestRate, "interestRate");
    ArgumentChecker.notNull(dividend, "dividend");

    ArgumentChecker.isTrue(spot > 0., "Spot should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(spot), "Spot should be finite");

    final TimeVaryingLatticeSpecification vLattice = new TimeVaryingLatticeSpecification();
    final int nSteps = function.getNumberOfSteps();
    final double timeToExpiry = function.getTimeToExpiry();

    ArgumentChecker.isTrue(nSteps == interestRate.length, "Wrong interestRate length");
    ArgumentChecker.isTrue(nSteps == volatility.length, "Wrong volatility length");
    ArgumentChecker.isTrue(nSteps == dividend.length, "Wrong dividend length");

    for (int i = 0; i < nSteps; ++i) {
      ArgumentChecker.isTrue(volatility[i] > 0., "volatility should be positive");
      ArgumentChecker.isTrue(Doubles.isFinite(volatility[i]), "volatility should be finite");
      ArgumentChecker.isTrue(Doubles.isFinite(interestRate[i]), "interestRate should be finite");
      ArgumentChecker.isTrue(Doubles.isFinite(dividend[i]), "dividend should be finite");
    }

    if (function instanceof BarrierOptionFunctionProvider) {
      final BarrierOptionFunctionProvider barrierFunction = (BarrierOptionFunctionProvider) function;
      if (barrierFunction.getChecker().checkOut(spot) || barrierFunction.getChecker().checkStrikeBehindBarrier()) {
        return 0.;
      }
    }

    final double[] nu = vLattice.getShiftedDrift(volatility, interestRate, dividend);
    final double dt = timeToExpiry / nSteps;
    final double spaceStep = vLattice.getSpaceStepTrinomial(volatility, nu, dt);
    final double downFactor = Math.exp(-spaceStep);
    final double middleOverDown = Math.exp(spaceStep);

    final double[] upProbability = new double[nSteps];
    final double[] middleProbability = new double[nSteps];
    final double[] downProbability = new double[nSteps];
    final double[] df = new double[nSteps];
    for (int i = 0; i < nSteps; ++i) {
      final double[] params = vLattice.getParametersTrinomial(volatility[i], nu[i], dt, spaceStep);
      upProbability[i] = params[0];
      middleProbability[i] = params[1];
      downProbability[i] = params[2];
      df[i] = Math.exp(-interestRate[i] * dt);
      ArgumentChecker.isTrue(upProbability[i] > 0., "upProbability should be greater than 0.");
      ArgumentChecker.isTrue(upProbability[i] < 1., "upProbability should be smaller than 1.");
      ArgumentChecker.isTrue(middleProbability[i] > 0., "middleProbability should be greater than 0.");
      ArgumentChecker.isTrue(middleProbability[i] < 1., "middleProbability should be smaller than 1.");
      ArgumentChecker.isTrue(downProbability[i] > 0., "downProbability should be greater than 0.");
    }

    double[] values = function.getPayoffAtExpiryTrinomial(spot, downFactor, middleOverDown);
    for (int i = nSteps - 1; i > -1; --i) {
      values = function.getNextOptionValues(df[i], upProbability[i], middleProbability[i], downProbability[i], values, spot, 0., downFactor, middleOverDown, i);
    }

    return values[0];
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
    double[] values = function.getPayoffAtExpiryTrinomial(assetPriceBase, downFactor, middleOverDown);

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
    ArgumentChecker.notNull(function, "function");

    ArgumentChecker.isTrue(spot1 > 0., "spot1 should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(spot1), "spot1 should be finite");
    ArgumentChecker.isTrue(spot2 > 0., "spot2 should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(spot2), "spot2 should be finite");
    ArgumentChecker.isTrue(volatility1 > 0., "volatility1 should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(volatility1), "volatility1 should be finite");
    ArgumentChecker.isTrue(volatility2 > 0., "volatility2 should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(volatility2), "volatility2 should be finite");
    ArgumentChecker.isTrue(correlation >= -1. && correlation <= 1., "correlation should be -1. <= rho <= 1.");
    ArgumentChecker.isTrue(Doubles.isFinite(interestRate), "interestRate should be finite");
    ArgumentChecker.isTrue(Doubles.isFinite(dividend1), "dividend1 should be finite");
    ArgumentChecker.isTrue(Doubles.isFinite(dividend2), "dividend2 should be finite");

    final int nSteps = function.getNumberOfSteps();
    final double timeToExpiry = function.getTimeToExpiry();
    final double dt = timeToExpiry / nSteps;

    final CoxRossRubinsteinLatticeSpecification crr = new CoxRossRubinsteinLatticeSpecification();
    final double[] params1 = crr.getParametersTrinomial(volatility1, interestRate - dividend1, dt);
    final double[] params2 = crr.getParametersTrinomial(volatility2, interestRate - dividend2, dt);
    final double downFactor1 = params1[2];
    final double downFactor2 = params2[2];
    final double middleOverDown1 = params1[0];
    final double middleOverDown2 = params2[0];

    final double up1 = params1[3];
    final double md1 = params1[4];
    final double dw1 = params1[5];
    final double up2 = params2[3];
    final double md2 = params2[4];
    final double dw2 = params2[5];

    final double discount = Math.exp(-interestRate * dt);
    final double uuProbability = up1 / 3. + up2 / 3. - 1. / 9. + correlation / 4.;
    final double umProbability = up1 / 3. + md2 / 3. - 1. / 9.;
    final double udProbability = up1 / 3. + dw2 / 3. - 1. / 9. - correlation / 4.;
    final double muProbability = md1 / 3. + up2 / 3. - 1. / 9.;
    final double mmProbability = md1 / 3. + md2 / 3. - 1. / 9.;
    final double mdProbability = md1 / 3. + dw2 / 3. - 1. / 9.;
    final double duProbability = dw1 / 3. + up2 / 3. - 1. / 9. - correlation / 4.;
    final double dmProbability = dw1 / 3. + md2 / 3. - 1. / 9.;
    final double ddProbability = dw1 / 3. + dw2 / 3. - 1. / 9. + correlation / 4.;

    final double assetPrice1 = spot1 * Math.pow(downFactor1, nSteps);
    final double assetPrice2 = spot2 * Math.pow(downFactor2, nSteps);
    double[][] values = function.getPayoffAtExpiryTrinomial(assetPrice1, assetPrice2, middleOverDown1, middleOverDown2);
    for (int i = nSteps - 1; i > -1; --i) {
      values = function.getNextOptionValues(discount, uuProbability, umProbability, udProbability, muProbability, mmProbability, mdProbability, duProbability, dmProbability, ddProbability, values,
          spot1, spot2, downFactor1, downFactor2, middleOverDown1, middleOverDown2, i);
    }

    return values[0][0];
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

    double[] values = function.getPayoffAtExpiryTrinomial(spot, downFactor, middleOverDown);
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
    ArgumentChecker.notNull(function, "function");
    ArgumentChecker.notNull(volatility, "volatility");
    ArgumentChecker.notNull(interestRate, "interestRate");
    ArgumentChecker.notNull(dividend, "dividend");

    ArgumentChecker.isTrue(spot > 0., "Spot should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(spot), "Spot should be finite");

    final GreekResultCollection collection = new GreekResultCollection();
    final TimeVaryingLatticeSpecification vLattice = new TimeVaryingLatticeSpecification();
    final int nSteps = function.getNumberOfSteps();
    final double timeToExpiry = function.getTimeToExpiry();

    ArgumentChecker.isTrue(nSteps == interestRate.length, "Wrong interestRate length");
    ArgumentChecker.isTrue(nSteps == volatility.length, "Wrong volatility length");
    ArgumentChecker.isTrue(nSteps == dividend.length, "Wrong dividend length");

    for (int i = 0; i < nSteps; ++i) {
      ArgumentChecker.isTrue(volatility[i] > 0., "volatility should be positive");
      ArgumentChecker.isTrue(Doubles.isFinite(volatility[i]), "volatility should be finite");
      ArgumentChecker.isTrue(Doubles.isFinite(interestRate[i]), "interestRate should be finite");
      ArgumentChecker.isTrue(Doubles.isFinite(dividend[i]), "dividend should be finite");
    }

    final double[] nu = vLattice.getShiftedDrift(volatility, interestRate, dividend);
    final double dt = timeToExpiry / nSteps;
    final double spaceStep = vLattice.getSpaceStepTrinomial(volatility, nu, dt);
    final double downFactor = Math.exp(-spaceStep);
    final double middleOverDown = Math.exp(spaceStep);

    final double[] upProbability = new double[nSteps];
    final double[] middleProbability = new double[nSteps];
    final double[] downProbability = new double[nSteps];
    final double[] df = new double[nSteps];
    for (int i = 0; i < nSteps; ++i) {
      final double[] params = vLattice.getParametersTrinomial(volatility[i], nu[i], dt, spaceStep);
      upProbability[i] = params[0];
      middleProbability[i] = params[1];
      downProbability[i] = params[2];
      df[i] = Math.exp(-interestRate[i] * dt);
      ArgumentChecker.isTrue(upProbability[i] > 0., "upProbability should be greater than 0.");
      ArgumentChecker.isTrue(upProbability[i] < 1., "upProbability should be smaller than 1.");
      ArgumentChecker.isTrue(middleProbability[i] > 0., "middleProbability should be greater than 0.");
      ArgumentChecker.isTrue(middleProbability[i] < 1., "middleProbability should be smaller than 1.");
      ArgumentChecker.isTrue(downProbability[i] > 0., "downProbability should be greater than 0.");
    }

    double[] values = function.getPayoffAtExpiryTrinomial(spot, downFactor, middleOverDown);
    final double[] res = new double[4];

    final double[] pForDelta = new double[] {spot * downFactor, spot, spot * middleOverDown };
    final double[] pForGamma = new double[] {pForDelta[0] * downFactor, pForDelta[0], spot, pForDelta[2], pForDelta[2] * middleOverDown };

    for (int i = nSteps - 1; i > -1; --i) {
      values = function.getNextOptionValues(df[i], upProbability[i], middleProbability[i], downProbability[i], values, spot, 0., downFactor, middleOverDown, i);
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
    res[3] = vLattice.getTheta(spot, 0., 0., 0., dt, res);

    collection.put(Greek.FAIR_PRICE, res[0]);
    collection.put(Greek.DELTA, res[1]);
    collection.put(Greek.GAMMA, res[2]);
    collection.put(Greek.THETA, res[3]);

    return collection;
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
    double[] values = function.getPayoffAtExpiryTrinomial(assetPriceBase, downFactor, middleOverDown);

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
    ArgumentChecker.notNull(function, "function");

    ArgumentChecker.isTrue(spot1 > 0., "spot1 should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(spot1), "spot1 should be finite");
    ArgumentChecker.isTrue(spot2 > 0., "spot2 should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(spot2), "spot2 should be finite");
    ArgumentChecker.isTrue(volatility1 > 0., "volatility1 should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(volatility1), "volatility1 should be finite");
    ArgumentChecker.isTrue(volatility2 > 0., "volatility2 should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(volatility2), "volatility2 should be finite");
    ArgumentChecker.isTrue(correlation >= -1. && correlation <= 1., "correlation should be -1. <= rho <= 1.");
    ArgumentChecker.isTrue(Doubles.isFinite(interestRate), "interestRate should be finite");
    ArgumentChecker.isTrue(Doubles.isFinite(dividend1), "dividend1 should be finite");
    ArgumentChecker.isTrue(Doubles.isFinite(dividend2), "dividend2 should be finite");

    final int nSteps = function.getNumberOfSteps();
    final double timeToExpiry = function.getTimeToExpiry();
    final double dt = timeToExpiry / nSteps;

    final CoxRossRubinsteinLatticeSpecification crr = new CoxRossRubinsteinLatticeSpecification();
    final double[] params1 = crr.getParametersTrinomial(volatility1, interestRate - dividend1, dt);
    final double[] params2 = crr.getParametersTrinomial(volatility2, interestRate - dividend2, dt);
    final double downFactor1 = params1[2];
    final double downFactor2 = params2[2];
    final double middleOverDown1 = params1[0];
    final double middleOverDown2 = params2[0];

    final double up1 = params1[3];
    final double md1 = params1[4];
    final double dw1 = params1[5];
    final double up2 = params2[3];
    final double md2 = params2[4];
    final double dw2 = params2[5];

    final double discount = Math.exp(-interestRate * dt);
    final double uuProbability = up1 / 3. + up2 / 3. - 1. / 9. + correlation / 4.;
    final double umProbability = up1 / 3. + md2 / 3. - 1. / 9.;
    final double udProbability = up1 / 3. + dw2 / 3. - 1. / 9. - correlation / 4.;
    final double muProbability = md1 / 3. + up2 / 3. - 1. / 9.;
    final double mmProbability = md1 / 3. + md2 / 3. - 1. / 9.;
    final double mdProbability = md1 / 3. + dw2 / 3. - 1. / 9.;
    final double duProbability = dw1 / 3. + up2 / 3. - 1. / 9. - correlation / 4.;
    final double dmProbability = dw1 / 3. + md2 / 3. - 1. / 9.;
    final double ddProbability = dw1 / 3. + dw2 / 3. - 1. / 9. + correlation / 4.;

    final double[] pForDelta1 = new double[] {spot1 * downFactor1, spot1, spot1 * middleOverDown1 };
    final double[] pForDelta2 = new double[] {spot2 * downFactor2, spot2, spot2 * middleOverDown2 };
    final double[] pForGamma1 = new double[] {pForDelta1[0] * downFactor1, pForDelta1[0], spot1, pForDelta1[2], pForDelta1[2] * middleOverDown1 };
    final double[] pForGamma2 = new double[] {pForDelta2[0] * downFactor2, pForDelta2[0], spot2, pForDelta2[2], pForDelta2[2] * middleOverDown2 };

    final double assetPrice1 = spot1 * Math.pow(downFactor1, nSteps);
    final double assetPrice2 = spot2 * Math.pow(downFactor2, nSteps);
    double[][] values = function.getPayoffAtExpiryTrinomial(assetPrice1, assetPrice2, middleOverDown1, middleOverDown2);
    final double[] res = new double[7];

    for (int i = nSteps - 1; i > -1; --i) {
      values = function.getNextOptionValues(discount, uuProbability, umProbability, udProbability, muProbability, mmProbability, mdProbability, duProbability, dmProbability, ddProbability, values,
          spot1, spot2, downFactor1, downFactor2, middleOverDown1, middleOverDown2, i);
      if (i == 2) {
        final double diff11 = pForGamma1[1] - pForGamma1[0];
        final double diff12 = pForGamma1[2] - pForGamma1[1];
        final double diff13 = pForGamma1[3] - pForGamma1[2];
        final double diff14 = pForGamma1[4] - pForGamma1[3];
        final double delta11 = (values[1][0] - values[0][0] + values[1][1] - values[0][1] + values[1][2] - values[0][2] + values[1][3] - values[0][3] + values[1][4] - values[0][4]) / diff11 / 5.;
        final double delta12 = (values[2][0] - values[1][0] + values[2][1] - values[1][1] + values[2][2] - values[1][2] + values[2][3] - values[1][3] + values[2][4] - values[1][4]) / diff12 / 5.;
        final double delta13 = (values[3][0] - values[2][0] + values[3][1] - values[2][1] + values[3][2] - values[2][2] + values[3][3] - values[2][3] + values[3][4] - values[2][4]) / diff13 / 5.;
        final double delta14 = (values[4][0] - values[3][0] + values[4][1] - values[3][1] + values[4][2] - values[3][2] + values[4][3] - values[3][3] + values[4][4] - values[3][4]) / diff14 / 5.;
        res[4] = (2. * (delta12 - delta11) / (pForGamma1[2] - pForGamma1[0]) + 2. * (delta13 - delta12) / (pForGamma1[3] - pForGamma1[1])
            + 2. * (delta14 - delta13) / (pForGamma1[4] - pForGamma1[2])) / 3.;

        final double diff21 = pForGamma2[1] - pForGamma2[0];
        final double diff22 = pForGamma2[2] - pForGamma2[1];
        final double diff23 = pForGamma2[3] - pForGamma2[2];
        final double diff24 = pForGamma2[4] - pForGamma2[3];
        final double delta21 = (values[0][1] - values[0][0] + values[1][1] - values[1][0] + values[2][1] - values[2][0] + values[3][1] - values[3][0] + values[4][1] - values[4][0]) / diff21 / 5.;
        final double delta22 = (values[0][2] - values[0][1] + values[1][2] - values[1][1] + values[2][2] - values[2][1] + values[3][2] - values[3][1] + values[4][2] - values[4][1]) / diff22 / 5.;
        final double delta23 = (values[0][3] - values[0][2] + values[1][3] - values[1][2] + values[2][3] - values[2][2] + values[3][3] - values[3][2] + values[4][3] - values[4][2]) / diff23 / 5.;
        final double delta24 = (values[0][4] - values[0][3] + values[1][4] - values[1][3] + values[2][4] - values[2][3] + values[3][4] - values[3][3] + values[4][4] - values[4][3]) / diff24 / 5.;
        res[5] = (2. * (delta22 - delta21) / (pForGamma2[2] - pForGamma2[0]) + 2. * (delta23 - delta22) / (pForGamma2[3] - pForGamma2[1])
            + 2. * (delta24 - delta23) / (pForGamma2[4] - pForGamma2[2])) / 3.;

        res[3] = values[2][2];
      }
      if (i == 1) {
        final double diff1L = pForDelta1[1] - pForDelta1[0];
        final double diff1H = pForDelta1[2] - pForDelta1[1];
        final double delta1d = 0.5 * ((values[1][0] - values[0][0]) / diff1L + (values[2][0] - values[1][0]) / diff1H);
        final double delta1m = 0.5 * ((values[1][1] - values[0][1]) / diff1L + (values[2][1] - values[1][1]) / diff1H);
        final double delta1u = 0.5 * ((values[1][2] - values[0][2]) / diff1L + (values[2][2] - values[1][2]) / diff1H);
        res[1] = (delta1d + delta1m + delta1u) / 3.;
        final double diff2L = pForDelta2[1] - pForDelta2[0];
        final double diff2H = pForDelta2[2] - pForDelta2[1];
        final double delta2d = 0.5 * ((values[0][1] - values[0][0]) / diff2L + (values[0][2] - values[0][1]) / diff2H);
        final double delta2m = 0.5 * ((values[1][1] - values[1][0]) / diff2L + (values[1][2] - values[1][1]) / diff2H);
        final double delta2u = 0.5 * ((values[2][1] - values[2][0]) / diff2L + (values[2][2] - values[2][1]) / diff2H);
        res[2] = (delta2d + delta2m + delta2u) / 3.;

        res[6] = 0.25 * ((delta1m - delta1d) / diff2L + (delta1u - delta1m) / diff2H + (delta2m - delta2d) / diff1L + (delta2u - delta2m) / diff1H);
      }
    }
    res[0] = values[0][0];
    res[3] = 0.5 * (res[3] - values[0][0]) / dt;

    return res;
  }

  @Override
  public double getPrice(final OptionFunctionProvider1D function, final StandardOptionDataBundle data) {
    ArgumentChecker.notNull(function, "function");
    Validate.notNull(data, "data");

    final int nSteps = function.getNumberOfSteps();
    final double strike = function.getStrike();
    final double timeToExpiry = function.getTimeToExpiry();

    final double spot = data.getSpot();
    final double interestRate = data.getInterestRate(timeToExpiry);
    final double cost = data.getCostOfCarry();
    double volatility = data.getVolatility(timeToExpiry, strike);

    final double dt = timeToExpiry / nSteps;
    final double discount = Math.exp(-interestRate * dt);
    final double dx = volatility * Math.sqrt(2. * dt);

    final double upFactor = Math.exp(dx);
    final double downFactor = 1 / upFactor;

    final double[] adSec = new double[2 * nSteps + 1];
    final double[] assetPrice = new double[2 * nSteps + 1];
    final double[] values = function.getPayoffAtExpiryTrinomial(spot, downFactor, upFactor);

    for (int i = nSteps; i > -1; --i) {
      if (i == 0) {
        final double upProb = adSec[2] / discount;
        final double midProb = getMiddle(upProb, 1. / discount, spot, assetPrice[0], assetPrice[1], assetPrice[2]);
        final double dwProb = 1. - upProb - midProb;
        values[0] = discount * (dwProb * values[0] + midProb * values[1] + upProb * values[2]);
      } else {
        final int nNodes = 2 * i + 1;
        final double[] assetPriceLocal = new double[nNodes];
        final double[] callOptionPrice = new double[nNodes];
        final double[] putOptionPrice = new double[nNodes];
        final double time = dt * i;
        int position = i - 1;
        double assetTmp = spot * Math.pow(upFactor, i);
        for (int j = nNodes - 1; j > -1; --j) {
          assetPriceLocal[j] = assetTmp;
          final double impliedVol = data.getVolatility(time, assetPriceLocal[j]);
          callOptionPrice[j] = BlackScholesFormulaRepository.price(spot, assetPriceLocal[j], time, impliedVol, interestRate, cost, true);
          putOptionPrice[j] = BlackScholesFormulaRepository.price(spot, assetPriceLocal[j], time, impliedVol, interestRate, cost, false);
          assetTmp *= downFactor;
        }

        final double[] adSecLocal = new double[nNodes];
        for (int j = nNodes - 1; j > position; --j) {
          adSecLocal[j] = callOptionPrice[j - 1];
          for (int k = j + 1; k < nNodes; ++k) {
            adSecLocal[j] -= (assetPriceLocal[k] - assetPriceLocal[j - 1]) * adSecLocal[k];
          }
          adSecLocal[j] /= (assetPriceLocal[j] - assetPriceLocal[j - 1]);
        }
        ++position;
        for (int j = 0; j < position; ++j) {
          adSecLocal[j] = putOptionPrice[j + 1];
          for (int k = 0; k < j; ++k) {
            adSecLocal[j] -= (assetPriceLocal[j + 1] - assetPriceLocal[k]) * adSecLocal[k];
          }
          adSecLocal[j] /= (assetPriceLocal[j + 1] - assetPriceLocal[j]);
        }

        if (i != nSteps) {
          final double[][] prob = new double[nNodes][3];
          prob[nNodes - 1][2] = adSec[nNodes + 1] / adSecLocal[nNodes - 1] / discount;
          prob[nNodes - 1][1] = getMiddle(prob[nNodes - 1][2], 1. / discount, assetPriceLocal[nNodes - 1], assetPrice[nNodes - 1], assetPrice[nNodes], assetPrice[nNodes + 1]);
          prob[nNodes - 1][0] = 1. - prob[nNodes - 1][2] - prob[nNodes - 1][1];

          prob[nNodes - 2][2] = (adSec[nNodes] / discount - prob[nNodes - 1][1] * adSecLocal[nNodes - 1]) / adSecLocal[nNodes - 2];
          prob[nNodes - 2][1] = getMiddle(prob[nNodes - 2][2], 1. / discount, assetPriceLocal[nNodes - 2], assetPrice[nNodes - 2], assetPrice[nNodes - 1], assetPrice[nNodes]);
          prob[nNodes - 2][0] = 1. - prob[nNodes - 2][2] - prob[nNodes - 2][1];

          for (int j = nNodes - 3; j > -1; --j) {
            prob[j][2] = (adSec[j + 2] / discount - prob[j + 2][0] * adSecLocal[j + 2] - prob[j + 1][1] * adSecLocal[j + 1]) / adSecLocal[j];
            prob[j][1] = getMiddle(prob[j][2], 1. / discount, assetPriceLocal[j], assetPrice[j], assetPrice[j + 1], assetPrice[j + 2]);
            prob[j][0] = 1. - prob[j][1] - prob[j][2];
            if (prob[j][2] <= 0. || prob[j][1] <= 0. || prob[j][0] <= 0.) {
              final double fwd = assetPriceLocal[j] / discount;
              if (fwd < assetPrice[j + 1] && fwd > assetPrice[j]) {
                prob[j][2] = 0.5 * (fwd - assetPrice[j]) / (assetPrice[j + 2] - assetPrice[j]);
                prob[j][0] = 0.5 * ((assetPrice[j + 2] - fwd) / (assetPrice[j + 2] - assetPrice[j]) + (assetPrice[j + 1] - fwd) / (assetPrice[j + 1] - assetPrice[j]));
              } else if (fwd < assetPrice[j + 2] && fwd > assetPrice[j + 1]) {
                prob[j][2] = 0.5 * ((fwd - assetPrice[j + 1]) / (assetPrice[j + 2] - assetPrice[j]) + (fwd - assetPrice[j]) / (assetPrice[j + 2] - assetPrice[j]));
                prob[j][0] = 0.5 * (assetPrice[j + 2] - fwd) / (assetPrice[j + 2]);
              }
              prob[j][1] = 1. - prob[j][0] - prob[j][2];
            }

          }

          for (int j = 0; j < nNodes; ++j) {
            values[j] = discount * (prob[j][0] * values[j] + prob[j][1] * values[j + 1] + prob[j][2] * values[j + 2]);
          }
        }

        System.arraycopy(adSecLocal, 0, adSec, 0, nNodes);
        System.arraycopy(assetPriceLocal, 0, assetPrice, 0, nNodes);
      }
    }

    return values[0];
  }

  private double getMiddle(final double upProbability, final double factor, final double assetBase, final double assetPrevDw, final double assetPrevMd, final double assetPrevUp) {
    return (factor * assetBase - assetPrevDw - upProbability * (assetPrevUp - assetPrevDw)) / (assetPrevMd - assetPrevDw);
  }

}
