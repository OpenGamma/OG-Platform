/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import org.apache.commons.lang.Validate;

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

  /*
   * TODO error must be returned if dt > (dividend interval)
   * TODO check 0<p<1, which is not necessarily satisfied with non-zero dividend
   * TODO Other types, such as Binary-type payoff, can be done with OptionDefinition
   * TODO Greeks with discrete dividends
   * TODO discrete dividends for other types of option(barrier, bermudan, asian, look back)
   * TODO Argument checker for barrier such as strike v.s. barrier, spot v.s. barrier, etc... which must give 0
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
    //These two MUST be computed via in-out parity//
    //      case DownAndIn:
    //        _function = new DownAndOutPayoff();
    //        break;
    //      case UpAndIn:
    //        _function = new UpAndOutPayoff();
    //        break;
    }
  }

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
      //      System.out.println(new DoubleMatrix1D(values));
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
    if (isCall) {
      return getEuropeanPrice(lattice, spot, strike, timeToExpiry, volatility, interestRate, dividend, nSteps, true);
    } else {
      final LatticeSpecification modLattice = (lattice instanceof TimeVaryingLatticeSpecification) ? new TrigeorgisLatticeSpecification() : lattice;

      final double dt = timeToExpiry / nSteps;
      final double discount = Math.exp(-interestRate * dt);
      final double[] params = modLattice.getParameters(spot, strike, timeToExpiry, volatility, interestRate - dividend, nSteps, dt);
      final double upFactor = params[0];
      final double downFactor = params[1];
      final double upProbability = params[2];
      final double downProbability = params[3];
      final double upOverDown = upFactor / downFactor;
      final double sig = -1.;

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
    }
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

  private abstract class PayoffFunction {
    public abstract double[] getPayoffAtExpiry(final double assetPrice, final double strike, final int nSteps, final double sig, final double upOverDown);

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
