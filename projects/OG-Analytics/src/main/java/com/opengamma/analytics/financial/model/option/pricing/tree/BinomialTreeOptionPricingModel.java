/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.model.option.definition.OptionPayoffFunction;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.descriptive.MeanCalculator;
import com.opengamma.analytics.math.statistics.descriptive.SampleMomentCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class BinomialTreeOptionPricingModel extends TreeOptionPricingModel {

  private enum BarrierTypes {
    DownAndOut, UpAndOut,
    //    DownAndIn, UpAndIn; //These two MUST be computed via in-out parity//
  }

  private final double _barrier;
  private final BarrierTypes _typeName;
  private PayoffFunction _function;

  private static final Function1D<double[], Double> MEAN_CALCULATOR = new MeanCalculator();
  private static final Function1D<double[], Double> MOMENT_CALCULATOR = new SampleMomentCalculator(2);

  /*
   * TODO error must be returned if dt > (dividend interval) (PLAT-4314)
   * TODO check 0<p<1, which is not necessarily satisfied with non-zero dividend, (PLAT-4314)
   *                   this must be checked for spread options
   * TODO Greeks with discrete dividends (PLAT-4290)
   * TODO discrete dividends for other types of option(barrier) (PLAT-4290)
   * TODO time-varying vol may not be compatible to discrete dividends due to limited control of dt
   * TODO Argument checker for barrier such as strike v.s. barrier, spot v.s. barrier, etc... which must give 0 (PLAT-4314)
   * TODO barrier American needs more tests (PLAT-4297)
   *       Test barrier option with nonzero dividend against analytic formula
   *       
   *       
   * TODO Other types, such as Binary-type payoff, can be done with OptionDefinition
   * TODO spread options need more tests
   * 
   * 
   * <<Slight modification of American>>
   * TODO Bermudan option
   * <<Full tree information may be needed>>
   * TODO Asian option
   * TODO Look-back option
   */

  public BinomialTreeOptionPricingModel() {
    _barrier = 0.;
    _typeName = null;
    _function = new VanillaPayoff();
  }

  public BinomialTreeOptionPricingModel(final double barrier, final String typeName) {
    _barrier = barrier;
    _typeName = BarrierTypes.valueOf(typeName);
    switch (_typeName) {
      case DownAndOut:
        _function = new DownAndOutPayoff();
        break;
      case UpAndOut:
        _function = new UpAndOutPayoff();
        break;
    }
  }

  public double getPrice(final LatticeSpecification lattice, final OptionFunctionProvider1D function, final double spot, final double timeToExpiry, final double volatility,
      final double interestRate, final double dividend) {
    final LatticeSpecification modLattice = (lattice instanceof TimeVaryingLatticeSpecification) ? new TrigeorgisLatticeSpecification() : lattice;

    final int nSteps = function.getNumberOfSteps();
    final double strike = function.getStrike();

    final double dt = timeToExpiry / nSteps;
    final double discount = Math.exp(-interestRate * dt);
    final double[] params = modLattice.getParameters(spot, strike, timeToExpiry, volatility, interestRate - dividend, nSteps, dt);
    final double upFactor = params[0];
    final double downFactor = params[1];
    final double upProbability = params[2];
    final double downProbability = params[3];
    final double upOverDown = upFactor / downFactor;

    double assetPrice = spot * Math.pow(downFactor, nSteps);
    double[] values = function.getPayoffAtExpiry(assetPrice, upOverDown);
    for (int i = nSteps - 1; i > -1; --i) {
      values = function.getNextOptionValues(discount, upProbability, downProbability, values, spot, 0., downFactor, upOverDown, i);
    }

    return values[0];
  }

  /*
   * Array is used for dividend to realize constant cost of carry given by b = r - q
   */
  @Override
  public double getPrice(final OptionFunctionProvider1D function, final double spot, final double timeToExpiry, final double[] volatility, final double[] interestRate, final double[] dividend) {
    final TimeVaryingLatticeSpecification vLattice = new TimeVaryingLatticeSpecification();

    final int nSteps = function.getNumberOfSteps();

    ArgumentChecker.isTrue(nSteps == interestRate.length, "Wrong interestRate length");
    ArgumentChecker.isTrue(nSteps == volatility.length, "Wrong volatility length");
    ArgumentChecker.isTrue(nSteps == dividend.length, "Wrong dividend length");

    final double[] nu = vLattice.getShiftedDrift(volatility, interestRate, dividend);
    final double spaceStep = vLattice.getSpaceStep(timeToExpiry, volatility, nSteps, nu);
    final double downFactor = Math.exp(-spaceStep);
    final double upOverDown = Math.exp(2. * spaceStep);

    final double[] upProbability = new double[nSteps];
    final double[] downProbability = new double[nSteps];
    final double[] df = new double[nSteps];
    for (int i = 0; i < nSteps; ++i) {
      final double[] params = vLattice.getParameters(volatility[i], nu[i], spaceStep);
      upProbability[i] = params[1];
      downProbability[i] = 1. - params[1];
      df[i] = Math.exp(-interestRate[i] * params[0]);
    }

    double assetPrice = spot * Math.pow(downFactor, nSteps);
    double[] values = function.getPayoffAtExpiry(assetPrice, upOverDown);
    for (int i = nSteps - 1; i > -1; --i) {
      values = function.getNextOptionValues(df[i], upProbability[i], downProbability[i], values, spot, 0., downFactor, upOverDown, i);
    }

    return values[0];
  }

  @Override
  public GreekResultCollection getGreeks(final LatticeSpecification lattice, final OptionFunctionProvider1D function, final double spot, final double timeToExpiry, final double volatility,
      final double interestRate, final double dividend) {
    final LatticeSpecification modLattice = (lattice instanceof TimeVaryingLatticeSpecification) ? new TrigeorgisLatticeSpecification() : lattice;

    final int nSteps = function.getNumberOfSteps();
    final double strike = function.getStrike();

    final double dt = timeToExpiry / nSteps;
    final double discount = Math.exp(-interestRate * dt);
    final double[] params = modLattice.getParameters(spot, strike, timeToExpiry, volatility, interestRate - dividend, nSteps, dt);
    final double upFactor = params[0];
    final double downFactor = params[1];
    final double upProbability = params[2];
    final double downProbability = params[3];
    final double upOverDown = upFactor / downFactor;

    double assetPrice = spot * Math.pow(downFactor, nSteps);
    double[] values = function.getPayoffAtExpiry(assetPrice, upOverDown);
    final double[] res = new double[4];

    double[] pForDelta = new double[] {spot * downFactor, spot * upFactor };
    double[] pForGamma = new double[] {pForDelta[0] * downFactor, pForDelta[0] * upFactor, pForDelta[1] * upFactor };

    for (int i = nSteps - 1; i > -1; --i) {
      values = function.getNextOptionValues(discount, upProbability, downProbability, values, spot, 0., downFactor, upOverDown, i);
      if (i == 2) {
        res[2] = 2. * ((values[2] - values[1]) / (pForGamma[2] - pForGamma[1]) - (values[1] - values[0]) / (pForGamma[1] - pForGamma[0])) / (pForGamma[2] - pForGamma[0]);
        res[3] = values[1];
      }
      if (i == 1) {
        res[1] = (values[1] - values[0]) / (pForDelta[1] - pForDelta[0]);
      }
    }
    res[0] = values[0];
    res[3] = modLattice.getTheta(spot, volatility, interestRate, dividend, dt, res);

    final GreekResultCollection collection = new GreekResultCollection();
    collection.put(Greek.FAIR_PRICE, res[0]);
    collection.put(Greek.DELTA, res[1]);
    collection.put(Greek.GAMMA, res[2]);
    collection.put(Greek.THETA, res[3]);

    return collection;
  }

  /*
   * Array is used for dividend to realize constant cost of carry given by b = r - q
   */
  @Override
  public GreekResultCollection getGreeks(final OptionFunctionProvider1D function, final double spot, final double timeToExpiry, final double[] volatility, final double[] interestRate,
      final double[] dividend) {
    final TimeVaryingLatticeSpecification vLattice = new TimeVaryingLatticeSpecification();

    final int nSteps = function.getNumberOfSteps();

    ArgumentChecker.isTrue(nSteps == interestRate.length, "Wrong interestRate length");
    ArgumentChecker.isTrue(nSteps == volatility.length, "Wrong volatility length");
    ArgumentChecker.isTrue(nSteps == dividend.length, "Wrong dividend length");

    final double[] nu = vLattice.getShiftedDrift(volatility, interestRate, dividend);
    final double spaceStep = vLattice.getSpaceStep(timeToExpiry, volatility, nSteps, nu);
    final double upFactor = Math.exp(spaceStep);
    final double downFactor = Math.exp(-spaceStep);
    final double upOverDown = Math.exp(2. * spaceStep);

    final double[] upProbability = new double[nSteps];
    final double[] downProbability = new double[nSteps];
    final double[] df = new double[nSteps];
    final double[] dt = new double[2];
    for (int i = 0; i < nSteps; ++i) {
      final double[] params = vLattice.getParameters(volatility[i], nu[i], spaceStep);
      upProbability[i] = params[1];
      downProbability[i] = 1. - params[1];
      df[i] = Math.exp(-interestRate[i] * params[0]);
      if (i == 0) {
        dt[0] = params[0];
      }
      if (i == 2) {
        dt[1] = params[1];
      }
    }

    double assetPrice = spot * Math.pow(downFactor, nSteps);
    double[] values = function.getPayoffAtExpiry(assetPrice, upOverDown);
    final double[] res = new double[4];

    double[] pForDelta = new double[] {spot * downFactor, spot * upFactor };
    double[] pForGamma = new double[] {pForDelta[0] * downFactor, pForDelta[0] * upFactor, pForDelta[1] * upFactor };

    for (int i = nSteps - 1; i > -1; --i) {
      values = function.getNextOptionValues(df[i], upProbability[i], downProbability[i], values, spot, 0., downFactor, upOverDown, i);
      if (i == 2) {
        res[2] = 2. * ((values[2] - values[1]) / (pForGamma[2] - pForGamma[1]) - (values[1] - values[0]) / (pForGamma[1] - pForGamma[0])) / (pForGamma[2] - pForGamma[0]);
        res[3] = values[1];
      }
      if (i == 1) {
        res[1] = (values[1] - values[0]) / (pForDelta[1] - pForDelta[0]);
      }
    }
    res[0] = values[0];
    res[3] = vLattice.getTheta(dt[0], dt[1], res);

    final GreekResultCollection collection = new GreekResultCollection();
    collection.put(Greek.FAIR_PRICE, res[0]);
    collection.put(Greek.DELTA, res[1]);
    collection.put(Greek.GAMMA, res[2]);
    collection.put(Greek.THETA, res[3]);

    return collection;
  }

  @Override
  public double getPriceWithDiscreteDividends(final LatticeSpecification lattice, final OptionFunctionProvider1D function, final double spot, final double timeToExpiry, final double volatility,
      final double interestRate, final DividendFunctionProvider dividend) {
    final LatticeSpecification modLattice = (lattice instanceof TimeVaryingLatticeSpecification) ? new TrigeorgisLatticeSpecification() : lattice;

    final int nSteps = function.getNumberOfSteps();
    final double strike = function.getStrike();

    final double dt = timeToExpiry / nSteps;
    final double discount = Math.exp(-interestRate * dt);
    final double[] params = modLattice.getParameters(spot, strike, timeToExpiry, volatility, interestRate, nSteps, dt);
    final double upFactor = params[0];
    final double downFactor = params[1];
    final double upProbability = params[2];
    final double downProbability = params[3];
    final double upOverDown = upFactor / downFactor;

    final int[] divSteps = dividend.getDividendSteps(dt);

    double assetPriceBase = dividend.spotModifier(spot, interestRate);
    double assetPriceTerminal = assetPriceBase * Math.pow(downFactor, nSteps);
    double[] values = function.getPayoffAtExpiry(assetPriceTerminal, upOverDown);

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
        values = function.getNextOptionValues(discount, upProbability, downProbability, values, assetPriceBase, 0., downFactor, upOverDown, i);
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
        values = function.getNextOptionValues(discount, upProbability, downProbability, values, assetPriceBase, sumDiscountDiv, downFactor, upOverDown, i);
      }
    }

    return values[0];
  }

  @Override
  public GreekResultCollection getGreeksWithDiscreteDividends(final LatticeSpecification lattice, final OptionFunctionProvider1D function, final double spot, final double timeToExpiry,
      final double volatility, final double interestRate, final DividendFunctionProvider dividend) {
    final LatticeSpecification modLattice = (lattice instanceof TimeVaryingLatticeSpecification) ? new TrigeorgisLatticeSpecification() : lattice;

    final int nSteps = function.getNumberOfSteps();
    final double strike = function.getStrike();

    final double dt = timeToExpiry / nSteps;
    final double discount = Math.exp(-interestRate * dt);
    final double[] params = modLattice.getParameters(spot, strike, timeToExpiry, volatility, interestRate, nSteps, dt);
    final double upFactor = params[0];
    final double downFactor = params[1];
    final double upProbability = params[2];
    final double downProbability = params[3];
    final double upOverDown = upFactor / downFactor;

    final int[] divSteps = dividend.getDividendSteps(dt);

    double assetPriceBase = dividend.spotModifier(spot, interestRate);
    double assetPriceTerminal = assetPriceBase * Math.pow(downFactor, nSteps);
    double[] values = function.getPayoffAtExpiry(assetPriceTerminal, upOverDown);

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
        values = function.getNextOptionValues(discount, upProbability, downProbability, values, assetPriceBase, 0., downFactor, upOverDown, i);
        if (i == 2) {
          double[] pForGamma = dividend.getAssetPricesForGamma(spot, interestRate, divSteps, upFactor, downFactor, 0.);
          res[2] = 2. * ((values[2] - values[1]) / (pForGamma[2] - pForGamma[1]) - (values[1] - values[0]) / (pForGamma[1] - pForGamma[0])) / (pForGamma[2] - pForGamma[0]);
          res[3] = values[1];
        }
        if (i == 1) {
          double[] pForDelta = dividend.getAssetPricesForDelta(spot, interestRate, divSteps, upFactor, downFactor, 0.);
          res[1] = (values[1] - values[0]) / (pForDelta[1] - pForDelta[0]);
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
        values = function.getNextOptionValues(discount, upProbability, downProbability, values, assetPriceBase, sumDiscountDiv, downFactor, upOverDown, i);
        if (i == 2) {
          double[] pForGamma = dividend.getAssetPricesForGamma(spot, interestRate, divSteps, upFactor, downFactor, sumDiscountDiv);
          res[2] = 2. * ((values[2] - values[1]) / (pForGamma[2] - pForGamma[1]) - (values[1] - values[0]) / (pForGamma[1] - pForGamma[0])) / (pForGamma[2] - pForGamma[0]);
          res[3] = values[1];
        }
        if (i == 1) {
          double[] pForDelta = dividend.getAssetPricesForDelta(spot, interestRate, divSteps, upFactor, downFactor, sumDiscountDiv);
          res[1] = (values[1] - values[0]) / (pForDelta[1] - pForDelta[0]);
        }
      }
    }

    res[0] = values[0];
    res[3] = modLattice.getTheta(spot, volatility, interestRate, 0., dt, res);
    final GreekResultCollection collection = new GreekResultCollection();
    collection.put(Greek.FAIR_PRICE, res[0]);
    collection.put(Greek.DELTA, res[1]);
    collection.put(Greek.GAMMA, res[2]);
    collection.put(Greek.THETA, res[3]);

    return collection;
  }

  /*
   * 
   * 
   * *********************************
   * Old methods below, removed later
   * *********************************
   * 
   * 
   */

  public double getEuropeanPrice(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate,
      final int nSteps, final boolean isCall) {
    return getEuropeanPrice(lattice, spot, strike, timeToExpiry, volatility, interestRate, 0., nSteps, isCall);
  }

  public double getEuropeanPrice(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate,
      final double dividend, final int nSteps, final boolean isCall) {
    final LatticeSpecification modLattice = (lattice instanceof TimeVaryingLatticeSpecification) ? new TrigeorgisLatticeSpecification() : lattice;

    final double dt = timeToExpiry / nSteps;
    final double discount = Math.exp(-interestRate * dt);
    final double[] params = modLattice.getParameters(spot, strike, timeToExpiry, volatility, interestRate - dividend, nSteps, dt);
    final double upFactor = params[0];
    final double downFactor = params[1];
    final double upProbability = params[2];
    final double downProbability = params[3];
    final double upOverDown = upFactor / downFactor;
    final double sig = isCall ? 1. : -1.;

    double assetPrice = spot * Math.pow(downFactor, nSteps);
    double[] values = _function.getPayoffAtExpiry(assetPrice, strike, nSteps, sig, upOverDown);
    for (int i = nSteps - 1; i > -1; --i) {
      //      for (int j = 0; j < i + 1; ++j) {
      //        values[j] = discount * (upProbability * values[j + 1] + downProbability * values[j]);
      //      }
      values = _function.getNextOptionValues(discount, upProbability, downProbability, values, spot, downFactor, upOverDown, i);
    }

    return values[0];
  }

  public double getEuropeanPricePropotinalDividends(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double volatility,
      final double interestRate, final double[] dividendTimes, final double[] dividends, final int nSteps, final boolean isCall) {
    final LatticeSpecification modLattice = (lattice instanceof TimeVaryingLatticeSpecification) ? new TrigeorgisLatticeSpecification() : lattice;

    final double dt = timeToExpiry / nSteps;
    final double discount = Math.exp(-interestRate * dt);
    final double[] params = modLattice.getParameters(spot, strike, timeToExpiry, volatility, interestRate, nSteps, dt);
    final double upFactor = params[0];
    final double downFactor = params[1];
    final double upProbability = params[2];
    final double downProbability = params[3];
    final double upOverDown = upFactor / downFactor;
    final double sig = isCall ? 1. : -1.;

    final int nDivs = dividendTimes.length;
    double assetPrice = spot * Math.pow(downFactor, nSteps);
    for (int i = 0; i < nDivs; ++i) {
      assetPrice *= (1. - dividends[i]);
    }
    final double[] values = _function.getPayoffAtExpiry(assetPrice, strike, nSteps, sig, upOverDown);

    for (int i = nSteps - 1; i > -1; --i) {
      for (int j = 0; j < i + 1; ++j) {
        values[j] = discount * (upProbability * values[j + 1] + downProbability * values[j]);
      }
    }

    return values[0];
  }

  public double getEuropeanPriceCashDividends(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double volatility,
      final double interestRate, final double[] dividendTimes, final double[] dividends, final int nSteps, final boolean isCall) {
    final LatticeSpecification modLattice = (lattice instanceof TimeVaryingLatticeSpecification) ? new TrigeorgisLatticeSpecification() : lattice;

    final double dt = timeToExpiry / nSteps;
    final double discount = Math.exp(-interestRate * dt);
    final double[] params = modLattice.getParameters(spot, strike, timeToExpiry, volatility, interestRate, nSteps, dt);
    final double upFactor = params[0];
    final double downFactor = params[1];
    final double upProbability = params[2];
    final double downProbability = params[3];
    final double upOverDown = upFactor / downFactor;
    final double sig = isCall ? 1. : -1.;

    final int nDivs = dividendTimes.length;
    double assetPrice = spot;
    for (int i = 0; i < nDivs; ++i) {
      assetPrice -= dividends[i] * Math.exp(-interestRate * dividendTimes[i]);
    }
    assetPrice *= Math.pow(downFactor, nSteps);
    final double[] values = _function.getPayoffAtExpiry(assetPrice, strike, nSteps, sig, upOverDown);
    for (int i = nSteps - 1; i > -1; --i) {
      for (int j = 0; j < i + 1; ++j) {
        values[j] = discount * (upProbability * values[j + 1] + downProbability * values[j]);
      }
    }

    return values[0];
  }

  public double getEuropeanPrice(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double[] volatility,
      final double[] interestRate, final int nSteps, final boolean isCall) {
    Validate.isTrue(lattice instanceof TimeVaryingLatticeSpecification);
    final TimeVaryingLatticeSpecification vLattice = (TimeVaryingLatticeSpecification) lattice;

    ArgumentChecker.isTrue(nSteps == interestRate.length, "Wrong interestRate length");
    ArgumentChecker.isTrue(nSteps == volatility.length, "Wrong volatility length");

    final double[] nu = vLattice.getShiftedDrift(volatility, interestRate);
    final double spaceStep = vLattice.getSpaceStep(timeToExpiry, volatility, nSteps, nu);
    final double downFactor = Math.exp(-spaceStep);
    final double upOverDown = Math.exp(2. * spaceStep);

    final double[] upProbability = new double[nSteps];
    final double[] downProbability = new double[nSteps];
    final double[] df = new double[nSteps];
    for (int i = 0; i < nSteps; ++i) {
      final double[] params = vLattice.getParameters(volatility[i], nu[i], spaceStep);
      upProbability[i] = params[1];
      downProbability[i] = 1. - params[1];
      df[i] = Math.exp(-interestRate[i] * params[0]);
    }
    final double sig = isCall ? 1. : -1.;

    double assetPrice = spot * Math.pow(downFactor, nSteps);
    double[] values = _function.getPayoffAtExpiry(assetPrice, strike, nSteps, sig, upOverDown);
    for (int i = nSteps - 1; i > -1; --i) {
      //      for (int j = 0; j < i + 1; ++j) {
      //        values[j] = df[i] * (upProbability[i] * values[j + 1] + downProbability[i] * values[j]);
      //      }
      values = _function.getNextOptionValues(df[i], upProbability[i], downProbability[i], values, spot, downFactor, upOverDown, i);
    }

    return values[0];
  }

  public double[] getEuropeanGreeks(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate,
      final int nSteps, final boolean isCall) {
    return getEuropeanGreeks(lattice, spot, strike, timeToExpiry, volatility, interestRate, 0., nSteps, isCall);
  }

  public double[] getEuropeanGreeks(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate,
      final double dividend, final int nSteps, final boolean isCall) {
    final LatticeSpecification modLattice = (lattice instanceof TimeVaryingLatticeSpecification) ? new TrigeorgisLatticeSpecification() : lattice;

    final double dt = timeToExpiry / nSteps;
    final double discount = Math.exp(-interestRate * dt);
    final double[] params = modLattice.getParameters(spot, strike, timeToExpiry, volatility, interestRate - dividend, nSteps, dt);
    final double upFactor = params[0];
    final double downFactor = params[1];
    final double upProbability = params[2];
    final double downProbability = params[3];
    final double upOverDown = upFactor / downFactor;
    final double sig = isCall ? 1. : -1.;

    double assetPrice = spot * Math.pow(downFactor, nSteps);
    double[] values = _function.getPayoffAtExpiry(assetPrice, strike, nSteps, sig, upOverDown);
    final double[] res = new double[4];

    double[] pForDelta = new double[] {spot * downFactor, spot * upFactor };
    double[] pForGamma = new double[] {pForDelta[0] * downFactor, pForDelta[0] * upFactor, pForDelta[1] * upFactor };

    for (int i = nSteps - 1; i > -1; --i) {
      //      for (int j = 0; j < i + 1; ++j) {
      //        values[j] = discount * (upProbability * values[j + 1] + downProbability * values[j]);
      //      }
      values = _function.getNextOptionValues(discount, upProbability, downProbability, values, spot, downFactor, upOverDown, i);
      if (i == 2) {
        res[2] = 2. * ((values[2] - values[1]) / (pForGamma[2] - pForGamma[1]) - (values[1] - values[0]) / (pForGamma[1] - pForGamma[0])) / (pForGamma[2] - pForGamma[0]);
        res[3] = values[1];
      }
      if (i == 1) {
        res[1] = (values[1] - values[0]) / (pForDelta[1] - pForDelta[0]);
      }
    }
    res[0] = values[0];
    res[3] = modLattice.getTheta(spot, volatility, interestRate, dividend, dt, res);
    return res;
  }

  public double getAmericanPrice(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate,
      final int nSteps, final boolean isCall) {
    return getAmericanPrice(lattice, spot, strike, timeToExpiry, volatility, interestRate, 0., nSteps, isCall);
  }

  public double getAmericanPrice(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate,
      final double dividend, final int nSteps, final boolean isCall) {
    //    if (isCall && dividend == 0. && interestRate >= 0.) {
    //      return getEuropeanPrice(lattice, spot, strike, timeToExpiry, volatility, interestRate, dividend, nSteps, true);
    //    } else {
    final LatticeSpecification modLattice = (lattice instanceof TimeVaryingLatticeSpecification) ? new TrigeorgisLatticeSpecification() : lattice;

    final double dt = timeToExpiry / nSteps;
    final double discount = Math.exp(-interestRate * dt);
    final double[] params = modLattice.getParameters(spot, strike, timeToExpiry, volatility, interestRate - dividend, nSteps, dt);
    final double upFactor = params[0];
    final double downFactor = params[1];
    final double upProbability = params[2];
    final double downProbability = params[3];
    final double upOverDown = upFactor / downFactor;
    final double sig = isCall ? 1. : -1.;

    double assetPrice = spot * Math.pow(downFactor, nSteps);
    double[] values = _function.getPayoffAtExpiry(assetPrice, strike, nSteps, sig, upOverDown);

    for (int i = nSteps - 1; i > -1; --i) {
      //        assetPrice = spot * Math.pow(downFactor, i);
      //        for (int j = 0; j < i + 1; ++j) {
      //          values[j] = Math.max(discount * (upProbability * values[j + 1] + downProbability * values[j]), strike - assetPrice);
      //          assetPrice *= upOverDown;
      //        }
      values = _function.getNextOptionValues(discount, upProbability, downProbability, values, spot, strike, sig, downFactor, upOverDown, i);
    }

    return values[0];
    //    }
  }

  public double getAmericanPrice(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double[] volatility,
      final double[] interestRate, final int nSteps, final boolean isCall) {
    if (isCall) {
      return getEuropeanPrice(lattice, spot, strike, timeToExpiry, volatility, interestRate, nSteps, true);
    } else {
      Validate.isTrue(lattice instanceof TimeVaryingLatticeSpecification);
      final TimeVaryingLatticeSpecification vLattice = (TimeVaryingLatticeSpecification) lattice;

      ArgumentChecker.isTrue(nSteps == interestRate.length, "Wrong interestRate length");
      ArgumentChecker.isTrue(nSteps == volatility.length, "Wrong volatility length");

      final double[] nu = vLattice.getShiftedDrift(volatility, interestRate);
      final double spaceStep = vLattice.getSpaceStep(timeToExpiry, volatility, nSteps, nu);
      final double downFactor = Math.exp(-spaceStep);
      final double upOverDown = Math.exp(2. * spaceStep);

      final double[] upProbability = new double[nSteps];
      final double[] downProbability = new double[nSteps];
      final double[] df = new double[nSteps];
      for (int i = 0; i < nSteps; ++i) {
        final double[] params = vLattice.getParameters(volatility[i], nu[i], spaceStep);
        upProbability[i] = params[1];
        downProbability[i] = 1. - params[1];
        df[i] = Math.exp(-interestRate[i] * params[0]);
      }
      final double sig = isCall ? 1. : -1.;

      double assetPrice = spot * Math.pow(downFactor, nSteps);
      double[] values = _function.getPayoffAtExpiry(assetPrice, strike, nSteps, sig, upOverDown);
      for (int i = nSteps - 1; i > -1; --i) {
        //        assetPrice = spot * Math.pow(downFactor, i);
        //        for (int j = 0; j < i + 1; ++j) {
        //          values[j] = Math.max(df[i] * (upProbability[i] * values[j + 1] + downProbability[i] * values[j]), sig * (assetPrice - strike));
        //          assetPrice *= upOverDown;
        //        }
        values = _function.getNextOptionValues(df[i], upProbability[i], downProbability[i], values, spot, strike, sig, downFactor, upOverDown, i);
      }

      return values[0];
    }
  }

  public double getAmericanPriceProportionalDividends(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double volatility,
      final double interestRate, final double[] dividendTimes, final double[] dividends, final int nSteps, final boolean isCall) {
    final LatticeSpecification modLattice = (lattice instanceof TimeVaryingLatticeSpecification) ? new TrigeorgisLatticeSpecification() : lattice;

    final double dt = timeToExpiry / nSteps;
    final double discount = Math.exp(-interestRate * dt);
    final double[] params = modLattice.getParameters(spot, strike, timeToExpiry, volatility, interestRate, nSteps, dt);
    final double upFactor = params[0];
    final double downFactor = params[1];
    final double upProbability = params[2];
    final double downProbability = params[3];
    final double upOverDown = upFactor / downFactor;
    final double sig = isCall ? 1. : -1.;

    final int nDivs = dividendTimes.length;
    final int[] divSteps = new int[nDivs];
    for (int i = 0; i < nDivs; ++i) {
      divSteps[i] = (int) (dividendTimes[i] / dt);
    }

    double assetPriceBase = spot;
    for (int i = 0; i < nDivs; ++i) {
      assetPriceBase *= (1. - dividends[i]);
    }
    assetPriceBase = assetPriceBase * Math.pow(downFactor, nSteps);
    final double[] values = _function.getPayoffAtExpiry(assetPriceBase, strike, nSteps, sig, upOverDown);

    int counter = 0;
    for (int i = nSteps - 1; i > -1; --i) {
      for (int k = nDivs - 1 - counter; k > -1; --k) {
        if (i == divSteps[k]) {
          assetPriceBase /= (1. - dividends[k]);
          ++counter;
        }
      }
      assetPriceBase /= downFactor;
      double assetPrice = assetPriceBase;

      for (int j = 0; j < i + 1; ++j) {
        values[j] = Math.max(discount * (upProbability * values[j + 1] + downProbability * values[j]), sig * (assetPrice - strike));
        assetPrice *= upOverDown;
      }
    }

    return values[0];
  }

  public double getAmericanPriceCashDividends(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double volatility,
      final double interestRate, final double[] dividendTimes, final double[] dividends, final int nSteps, final boolean isCall) {
    final LatticeSpecification modLattice = (lattice instanceof TimeVaryingLatticeSpecification) ? new TrigeorgisLatticeSpecification() : lattice;

    final double dt = timeToExpiry / nSteps;
    final double discount = Math.exp(-interestRate * dt);
    final double[] params = modLattice.getParameters(spot, strike, timeToExpiry, volatility, interestRate, nSteps, dt);
    final double upFactor = params[0];
    final double downFactor = params[1];
    final double upProbability = params[2];
    final double downProbability = params[3];
    final double upOverDown = upFactor / downFactor;
    final double sig = isCall ? 1. : -1.;

    final int nDivs = dividendTimes.length;
    final int[] divSteps = new int[nDivs];
    for (int i = 0; i < nDivs; ++i) {
      divSteps[i] = (int) (dividendTimes[i] / dt);
    }

    double assetPriceBase = spot;
    for (int i = 0; i < nDivs; ++i) {
      assetPriceBase -= dividends[i] * Math.exp(-interestRate * dividendTimes[i]);
    }
    double assetPrice = assetPriceBase * Math.pow(downFactor, nSteps);
    final double[] values = _function.getPayoffAtExpiry(assetPrice, strike, nSteps, sig, upOverDown);

    int counter = 0;
    double sumDiscountDiv = 0.;
    for (int i = nSteps - 1; i > -1; --i) {
      assetPrice = assetPriceBase * Math.pow(downFactor, i);
      sumDiscountDiv *= Math.exp(-interestRate * dt);
      for (int k = nDivs - 1 - counter; k > -1; --k) {
        if (i == divSteps[k]) {
          sumDiscountDiv += dividends[k] * Math.exp(-interestRate * (dividendTimes[k] - dt * i));
          ++counter;
        }
      }
      for (int j = 0; j < i + 1; ++j) {
        values[j] = Math.max(discount * (upProbability * values[j + 1] + downProbability * values[j]), sig * (assetPrice + sumDiscountDiv - strike));
        assetPrice *= upOverDown;
      }
    }

    return values[0];
  }

  public double[] getAmericanGreeks(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate,
      final int nSteps, final boolean isCall) {
    if (isCall) {
      return getEuropeanGreeks(lattice, spot, strike, timeToExpiry, volatility, interestRate, nSteps, true);
    } else {
      return getAmericanGreeks(lattice, spot, strike, timeToExpiry, volatility, interestRate, 0., nSteps, false);
    }
  }

  public double[] getAmericanGreeks(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate,
      final double dividend, final int nSteps, final boolean isCall) {
    final LatticeSpecification modLattice = (lattice instanceof TimeVaryingLatticeSpecification) ? new TrigeorgisLatticeSpecification() : lattice;
    final double[] res = new double[4];

    final double dt = timeToExpiry / nSteps;
    final double discount = Math.exp(-interestRate * dt);
    final double[] params = modLattice.getParameters(spot, strike, timeToExpiry, volatility, interestRate - dividend, nSteps, dt);
    final double upFactor = params[0];
    final double downFactor = params[1];
    final double upProbability = params[2];
    final double downProbability = params[3];
    final double upOverDown = upFactor / downFactor;
    final double sig = isCall ? 1. : -1.;

    double assetPrice = spot * Math.pow(downFactor, nSteps);
    double[] values = _function.getPayoffAtExpiry(assetPrice, strike, nSteps, sig, upOverDown);

    double[] pForDelta = new double[] {spot * downFactor, spot * upFactor };
    double[] pForGamma = new double[] {pForDelta[0] * downFactor, pForDelta[0] * upFactor, pForDelta[1] * upFactor };

    for (int i = nSteps - 1; i > -1; --i) {
      //      assetPrice = spot * Math.pow(downFactor, i);
      //      for (int j = 0; j < i + 1; ++j) {
      //        values[j] = Math.max(discount * (upProbability * values[j + 1] + downProbability * values[j]), sig * (assetPrice - strike));
      //        assetPrice *= upOverDown;
      //      }
      values = _function.getNextOptionValues(discount, upProbability, downProbability, values, spot, strike, sig, downFactor, upOverDown, i);
      if (i == 2) {
        res[2] = 2. * ((values[2] - values[1]) / (pForGamma[2] - pForGamma[1]) - (values[1] - values[0]) / (pForGamma[1] - pForGamma[0])) / (pForGamma[2] - pForGamma[0]);
        res[3] = values[1];
      }
      if (i == 1) {
        res[1] = (values[1] - values[0]) / (pForDelta[1] - pForDelta[0]);
      }
    }
    res[0] = values[0];
    res[3] = modLattice.getTheta(spot, volatility, interestRate, dividend, dt, res);

    return res;
  }

  public double getEuropeanSpreadPrice(final double spot1, final double spot2, final double strike, final double timeToExpiry, final double volatility1, final double volatility2,
      final double correlation, final double interestRate, final double dividend1, final double dividend2, final int nSteps, final boolean isCall) {
    ArgumentChecker.isTrue(_function instanceof VanillaPayoff, "Barrier option is not supported");
    final VanillaPayoff function = (VanillaPayoff) _function;

    final double vol12 = volatility1 * volatility2;
    final double vol11 = volatility1 * volatility1;
    final double vol22 = volatility2 * volatility2;

    final double dt = timeToExpiry / nSteps;
    final double discount = Math.exp(-interestRate * dt);
    final double rootDt = Math.sqrt(dt);
    final double dx1 = volatility1 * rootDt;
    final double dx2 = volatility2 * rootDt;

    final double dx12 = dx1 * dx2;
    final double nu1Factored = (interestRate - dividend1 - 0.5 * vol11) * dx2 * dt;
    final double nu2Factored = (interestRate - dividend2 - 0.5 * vol22) * dx1 * dt;
    final double vol12Factored = vol12 * correlation * dt;

    final double uuProbability = 0.25 * (dx12 + nu1Factored + nu2Factored + vol12Factored) / dx12;
    final double udProbability = 0.25 * (dx12 + nu1Factored - nu2Factored - vol12Factored) / dx12;
    final double duProbability = 0.25 * (dx12 - nu1Factored + nu2Factored - vol12Factored) / dx12;
    final double ddProbability = 0.25 * (dx12 - nu1Factored - nu2Factored + vol12Factored) / dx12;

    final double downFactor1 = Math.exp(-dx1);
    final double downFactor2 = Math.exp(-dx2);
    final double upOverDown1 = Math.exp(2. * dx1);
    final double upOverDown2 = Math.exp(2. * dx2);

    final double sign = isCall ? 1. : -1.;

    double assetPrice1 = spot1 * Math.pow(downFactor1, nSteps);
    double assetPrice2 = spot2 * Math.pow(downFactor2, nSteps);
    double[][] values = function.getPayoffAtExpiry(assetPrice1, assetPrice2, strike, nSteps, sign, upOverDown1, upOverDown2);
    for (int i = nSteps - 1; i > -1; --i) {
      values = function.getNextOptionValues(discount, uuProbability, udProbability, duProbability, ddProbability, values, i);
    }

    return values[0][0];
  }

  public double getAmericanSpreadPrice(final double spot1, final double spot2, final double strike, final double timeToExpiry, final double volatility1, final double volatility2,
      final double correlation, final double interestRate, final double dividend1, final double dividend2, final int nSteps, final boolean isCall) {
    ArgumentChecker.isTrue(_function instanceof VanillaPayoff, "Barrier option is not supported");
    final VanillaPayoff function = (VanillaPayoff) _function;

    final double vol12 = volatility1 * volatility2;
    final double vol11 = volatility1 * volatility1;
    final double vol22 = volatility2 * volatility2;

    final double dt = timeToExpiry / nSteps;
    final double discount = Math.exp(-interestRate * dt);
    final double rootDt = Math.sqrt(dt);
    final double dx1 = volatility1 * rootDt;
    final double dx2 = volatility2 * rootDt;

    final double dx12 = dx1 * dx2;
    final double nu1Factored = (interestRate - dividend1 - 0.5 * vol11) * dx2 * dt;
    final double nu2Factored = (interestRate - dividend2 - 0.5 * vol22) * dx1 * dt;
    final double vol12Factored = vol12 * correlation * dt;

    final double uuProbability = 0.25 * (dx12 + nu1Factored + nu2Factored + vol12Factored) / dx12;
    final double udProbability = 0.25 * (dx12 + nu1Factored - nu2Factored - vol12Factored) / dx12;
    final double duProbability = 0.25 * (dx12 - nu1Factored + nu2Factored - vol12Factored) / dx12;
    final double ddProbability = 0.25 * (dx12 - nu1Factored - nu2Factored + vol12Factored) / dx12;

    final double downFactor1 = Math.exp(-dx1);
    final double downFactor2 = Math.exp(-dx2);
    final double upOverDown1 = Math.exp(2. * dx1);
    final double upOverDown2 = Math.exp(2. * dx2);

    final double sign = isCall ? 1. : -1.;

    double assetPrice1 = spot1 * Math.pow(downFactor1, nSteps);
    double assetPrice2 = spot2 * Math.pow(downFactor2, nSteps);
    double[][] values = function.getPayoffAtExpiry(assetPrice1, assetPrice2, strike, nSteps, sign, upOverDown1, upOverDown2);
    for (int i = nSteps - 1; i > -1; --i) {
      values = function.getNextOptionValues(discount, strike, uuProbability, udProbability, duProbability, ddProbability, values, spot1, spot2, sign, downFactor1, downFactor2, upOverDown1,
          upOverDown2, i);
    }

    return values[0][0];
  }

  private abstract class PayoffFunction {
    public abstract double[] getPayoffAtExpiry(final double assetPrice, final double strike, final int nSteps, final double sig, final double upOverDown);

    public abstract double[] getPayoffAtExpiry(final StandardOptionDataBundle data, final OptionPayoffFunction<StandardOptionDataBundle> payoffFunction, final double assetPrice, final double strike,
        final int nSteps, final double sig,
        final double upOverDown);

    public abstract double[] getNextOptionValues(final double discount, final double upProbability, final double downProbability, final double[] values, final double baseAssetPrice,
        final double downFactor, final double upOverDown, final int steps);

    public abstract double[] getNextOptionValues(final double discount, final double upProbability, final double downProbability, final double[] values, final double baseAssetPrice,
        final double strike, final double sign, final double downFactor, final double upOverDown, final int steps);
  }

  private class VanillaPayoff extends PayoffFunction {
    @Override
    public double[] getPayoffAtExpiry(final double assetPrice, final double strike, final int nSteps, final double sig, final double upOverDown) {
      final double[] values = new double[nSteps + 1];
      double priceTmp = assetPrice;
      for (int i = 0; i < nSteps + 1; ++i) {
        values[i] = Math.max(sig * (priceTmp - strike), 0);
        priceTmp *= upOverDown;
      }
      return values;
    }

    @Override
    public double[] getPayoffAtExpiry(final StandardOptionDataBundle data, final OptionPayoffFunction<StandardOptionDataBundle> payoffFunction, final double assetPrice, final double strike,
        final int nSteps, final double sig, final double upOverDown) {
      final double[] values = new double[nSteps + 1];
      double priceTmp = assetPrice;
      for (int i = 0; i < nSteps + 1; ++i) {
        StandardOptionDataBundle dataAtExpiry = data.withSpot(priceTmp);
        values[i] = payoffFunction.getPayoff(dataAtExpiry, 0.);
        priceTmp *= upOverDown;
      }
      return values;
    }

    @Override
    public double[] getNextOptionValues(final double discount, final double upProbability, final double downProbability, final double[] values, final double baseAssetPrice, final double downFactor,
        final double upOverDown, final int steps) {
      final double[] res = new double[steps + 1];
      for (int j = 0; j < steps + 1; ++j) {
        res[j] = discount * (upProbability * values[j + 1] + downProbability * values[j]);
      }
      return res;
    }

    @Override
    public double[] getNextOptionValues(final double discount, final double upProbability, final double downProbability, final double[] values, final double baseAssetPrice, final double strike,
        final double sign, final double downFactor, final double upOverDown, final int steps) {
      final double[] res = new double[steps + 1];
      double assetPrice = baseAssetPrice * Math.pow(downFactor, steps);
      for (int j = 0; j < steps + 1; ++j) {
        res[j] = Math.max(discount * (upProbability * values[j + 1] + downProbability * values[j]), sign * (assetPrice - strike));
        assetPrice *= upOverDown;
      }
      return res;
    }

    /*
     * Functions for two-dimensional case
     */
    public double[][] getPayoffAtExpiry(final double assetPrice1, final double assetPrice2, final double strike, final int nSteps, final double sign, final double upOverDown1,
        final double upOverDown2) {
      final int nStepsP = nSteps + 1;
      final double[][] values = new double[nStepsP][nStepsP];
      double priceTmp1 = assetPrice1;
      for (int i = 0; i < nStepsP; ++i) {
        double priceTmp2 = assetPrice2;
        for (int j = 0; j < nStepsP; ++j) {
          values[i][j] = Math.max(sign * (priceTmp1 - priceTmp2 - strike), 0);
          priceTmp2 *= upOverDown2;
        }
        priceTmp1 *= upOverDown1;
      }
      return values;
    }

    public double[][] getNextOptionValues(final double discount, final double uuProbability, final double udProbability, final double duProbability, final double ddProbability,
        final double[][] values, final int steps) {
      final int stepsP = steps + 1;
      final double[][] res = new double[stepsP][stepsP];
      for (int j = 0; j < stepsP; ++j) {
        for (int i = 0; i < stepsP; ++i) {
          res[j][i] = discount * (uuProbability * values[j + 1][i + 1] + udProbability * values[j + 1][i] + duProbability * values[j][i + 1] + ddProbability * values[j][i]);
        }
      }
      return res;
    }

    public double[][] getNextOptionValues(final double discount, final double strike, final double uuProbability, final double udProbability, final double duProbability, final double ddProbability,
        final double[][] values, final double baseAssetPrice1, final double baseAssetPrice2, final double sign, final double downFactor1, final double downFactor2,
        final double upOverDown1, final double upOverDown2, final int steps) {
      final int stepsP = steps + 1;
      final double[][] res = new double[stepsP][stepsP];
      double assetPrice1 = baseAssetPrice1 * Math.pow(downFactor1, steps);
      for (int j = 0; j < stepsP; ++j) {
        double assetPrice2 = baseAssetPrice2 * Math.pow(downFactor2, steps);
        for (int i = 0; i < stepsP; ++i) {
          res[j][i] = discount * (uuProbability * values[j + 1][i + 1] + udProbability * values[j + 1][i] + duProbability * values[j][i + 1] + ddProbability * values[j][i]);
          res[j][i] = Math.max(res[j][i], sign * (assetPrice1 - assetPrice2 - strike));
          assetPrice2 *= upOverDown2;
        }
        assetPrice1 *= upOverDown1;
      }
      return res;
    }
  }

  private class DownAndOutPayoff extends PayoffFunction {
    @Override
    public double[] getPayoffAtExpiry(final double assetPrice, final double strike, final int nSteps, final double sig, final double upOverDown) {
      final double[] values = new double[nSteps + 1];
      double priceTmp = assetPrice;
      for (int i = 0; i < nSteps + 1; ++i) {
        values[i] = priceTmp <= _barrier ? 0. : Math.max(sig * (priceTmp - strike), 0);
        priceTmp *= upOverDown;
      }
      return values;
    }

    @Override
    public double[] getPayoffAtExpiry(final StandardOptionDataBundle data, final OptionPayoffFunction<StandardOptionDataBundle> payoffFunction, final double assetPrice, final double strike,
        final int nSteps, final double sig,
        final double upOverDown) {

      return null;
    }

    @Override
    public double[] getNextOptionValues(final double discount, final double upProbability, final double downProbability, final double[] values, final double baseAssetPrice, final double downFactor,
        final double upOverDown, final int steps) {
      final double[] res = new double[steps + 1];
      double assetPrice = baseAssetPrice * Math.pow(downFactor, steps);
      for (int j = 0; j < steps + 1; ++j) {
        res[j] = assetPrice <= _barrier ? 0. : discount * (upProbability * values[j + 1] + downProbability * values[j]);
        assetPrice *= upOverDown;
      }
      return res;
    }

    @Override
    public double[] getNextOptionValues(final double discount, final double upProbability, final double downProbability, final double[] values, final double baseAssetPrice, final double strike,
        final double sign, final double downFactor, final double upOverDown, final int steps) {
      final double[] res = new double[steps + 1];
      double assetPrice = baseAssetPrice * Math.pow(downFactor, steps);
      for (int j = 0; j < steps + 1; ++j) {
        res[j] = assetPrice <= _barrier ? 0. : Math.max(discount * (upProbability * values[j + 1] + downProbability * values[j]), sign * (assetPrice - strike));
        assetPrice *= upOverDown;
      }
      return res;
    }
  }

  private class UpAndOutPayoff extends PayoffFunction {
    @Override
    public double[] getPayoffAtExpiry(final double assetPrice, final double strike, final int nSteps, final double sig, final double upOverDown) {
      final double[] values = new double[nSteps + 1];
      double priceTmp = assetPrice;
      for (int i = 0; i < nSteps + 1; ++i) {
        values[i] = priceTmp >= _barrier ? 0. : Math.max(sig * (priceTmp - strike), 0);
        priceTmp *= upOverDown;
      }
      return values;
    }

    @Override
    public double[] getPayoffAtExpiry(final StandardOptionDataBundle data, final OptionPayoffFunction<StandardOptionDataBundle> payoffFunction, final double assetPrice, final double strike,
        final int nSteps, final double sig,
        final double upOverDown) {

      return null;
    }

    @Override
    public double[] getNextOptionValues(final double discount, final double upProbability, final double downProbability, final double[] values, final double baseAssetPrice, final double downFactor,
        final double upOverDown, final int steps) {
      final double[] res = new double[steps + 1];
      double assetPrice = baseAssetPrice * Math.pow(downFactor, steps);
      for (int j = 0; j < steps + 1; ++j) {
        res[j] = assetPrice >= _barrier ? 0. : discount * (upProbability * values[j + 1] + downProbability * values[j]);
        assetPrice *= upOverDown;
      }
      return res;
    }

    @Override
    public double[] getNextOptionValues(final double discount, final double upProbability, final double downProbability, final double[] values, final double baseAssetPrice, final double strike,
        final double sign, final double downFactor, final double upOverDown, final int steps) {
      final double[] res = new double[steps + 1];
      double assetPrice = baseAssetPrice * Math.pow(downFactor, steps);
      for (int j = 0; j < steps + 1; ++j) {
        res[j] = assetPrice >= _barrier ? 0. : Math.max(discount * (upProbability * values[j + 1] + downProbability * values[j]), sign * (assetPrice - strike));
        assetPrice *= upOverDown;
      }
      return res;
    }
  }

}
