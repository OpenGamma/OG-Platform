/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.varswap.pricing;

import com.opengamma.financial.equity.varswap.derivative.VarianceSwap;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.Integrator1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.tuple.DoublesPair;

import org.apache.commons.lang.Validate;

/**
 * We construct a model independent method to price variance as a static replication
 * of an (in)finite sum of call and put option prices on the underlying.
 * We assume the existence of a smooth function of these option prices / Implied volatilities.
 * That said, this method is very sensitive to strike near zero as the portfolio weighting is 1/k^2,
 * so we allow the caller to override the Volatilities below a cutoff point (defined as a fraction of the forward rate).
 * We fit a ShiftedLognormal model to the price of the linear (call) and digital (call spread) at the cutoff. 
 */
public class VarSwapStaticReplication {

  // Vol Extrapolation 
  private final Double _strikeCutoff; // Lowest interpolated strike. ShiftedLognormal hits Put(_strikeCutoff)
  private final Double _strikeSpread; // Match derivative near cutoff by also fitting to Put(_strikeCutoff + _strikeSpread)
  private final boolean _cutoffProvided; // False if both the above are null 

  // Integration parameters
  private final double _lowerBound; // Integrate over strikes in 'moneyness' or 'relative strike', defined as strike/forward. Start close to zero
  private final double _upperBound; // Upper bound in 'moneyness' (K/F). This represents 'large'
  private final Integrator1D<Double, Double> _integrator;

  /**
   * Default constructor with sensible inputs.
   */
  public VarSwapStaticReplication() {
    _lowerBound = 1e-4; // almost zero
    _upperBound = 5.0; // multiple of the atm forward
    _integrator = new RungeKuttaIntegrator1D();
    _strikeCutoff = 0.25; // TODO Choose how caller tells impliedVariance not to use ShiftedLognormal..
    _strikeSpread = 0.05;
    _cutoffProvided = true;
  }

  public VarSwapStaticReplication(final double lowerBound, final double upperBound, final Integrator1D<Double, Double> integrator, Double strikeCutoff, Double strikeSpread) {
    _lowerBound = lowerBound;
    _upperBound = upperBound;
    _integrator = integrator;

    _strikeCutoff = strikeCutoff;
    _strikeSpread = strikeSpread;
    if (_strikeCutoff == null || _strikeSpread == null) {
      Validate.isTrue(_strikeCutoff == null && _strikeSpread == null, "Both a cutoff moneyness and a spread must be provided to specify where to tie ShiftedLognormal.");
      _cutoffProvided = false;
    } else {
      Validate.isTrue(strikeCutoff < 1, "strikeCutoff should be less than the forward, i.e. less than one. Note its defined as moneyness: strike / fwd.");
      _cutoffProvided = true;
    }

  }

  // ************************** Black *****************
  // * TODO Review where a Black(fwd,strike,variance,isCall) function might live
  // * I've rewritten the Black function instead of using BlackPriceFunction as that was based on an 'option'
  // * where my usage here is simply to create a Function1D:  k -> price. 
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  private static final double black(double fwd, double strike, double variance, boolean isCall) {

    final double small = 1.0E-16;

    if (strike < small) {
      return isCall ? fwd : 0.0;
    }
    final int sign = isCall ? 1 : -1;
    final double sigmaRootT = Math.sqrt(variance);
    if (Math.abs(fwd - strike) < small) {
      return fwd * (2 * NORMAL.getCDF(sigmaRootT / 2) - 1);
    }
    if (sigmaRootT < small) {
      return Math.max(sign * (fwd - strike), 0.0);
    }
    final double d1 = (Math.log(fwd / strike) + 0.5 * variance) / sigmaRootT;
    final double d2 = d1 - sigmaRootT;

    return sign * (fwd * NORMAL.getCDF(sign * d1) - strike * NORMAL.getCDF(sign * d2));
  }

  private static final double blackStrikeSensitivity(double fwd, double strike, double variance, boolean isCall) {

    final double small = 1.0E-16;

    if (strike < small) {
      Validate.isTrue(false); //return isCall ? fwd : 0.0;
    }
    final int sign = isCall ? 1 : -1;
    final double sigmaRootT = Math.sqrt(variance);
    if (Math.abs(fwd - strike) < small) {
      return -sign * NORMAL.getCDF(sign * 0.5 * sigmaRootT);
    }
    if (sigmaRootT < small) {
      return sign;
    }
    final double d2 = (Math.log(fwd / strike) - 0.5 * variance) / sigmaRootT;
    return -sign * NORMAL.getCDF(sign * d2);
  }

  /**
   * @param deriv VarianceSwap derivative to be priced
   * @param market VarianceSwapDataBundle containing volatility surface, spot underlying, and funding curve
   * @return presentValue of the *remaining* variance in the swap. 
   */
  public double impliedVariance(final VarianceSwap deriv, final VarianceSwapDataBundle market) {
    Validate.notNull(deriv, "VarianceSwap deriv");
    Validate.notNull(market, "VarianceSwapDataBundle market");

    // 1. Unpack market data
    // TODO Review whether fwd=spot/df is sufficient, or whether the fwd itself should be in the VarianceSwapDataBundle
    final double expiry = deriv.getTimeToObsEnd(); // TODO Confirm treatmen t of which date should be used, obsEnd or settlement
    final double df = market.getDiscountCurve().getDiscountFactor(expiry);
    final double spot = market.getSpotUnderlying();
    final double fwd = spot / df;
    System.err.println("fwd = " + fwd);
    final VolatilitySurface vsurf = market.getVolatilitySurface();

    // *********************************************************
    // 1B. Fit the leftExtrapolator
    // TODO Cover the case in which a spread point isn't given
    // TODO Test what happens when a cutoff IS given but the vol is ConstantDoublesSurface

    final DoubleMatrix1D shiftedLnParams; // Two entries, Lognormal Variance and Forward Shift parameters, respectively

    // Targets
    if (_cutoffProvided) {

      final DoublesPair cutoffCoords = DoublesPair.of(expiry, _strikeCutoff * fwd + 0.0); // TODO Review
      final double cutoffVol = vsurf.getVolatility(cutoffCoords);
      final double cutoffVar = cutoffVol * cutoffVol * expiry;
      final double cutoffPut = black(fwd, _strikeCutoff * fwd, cutoffVar, false);

      final DoublesPair spreadCoords = DoublesPair.of(expiry, (_strikeCutoff + _strikeSpread) * fwd);
      final double spreadVol = vsurf.getVolatility(spreadCoords);
      double spreadVar = spreadVol * spreadVol * expiry;
      final double spreadPut = black(fwd, (_strikeCutoff + _strikeSpread) * fwd, spreadVar, false);

      // Function
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> fitShiftedLognormal = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
        @Override
        public DoubleMatrix1D evaluate(final DoubleMatrix1D varianceShiftPair) {
          double[] diffs = new double[] {100, 100 };
          double variance = Math.max(1e-9, varianceShiftPair.getEntry(0));
          double shift = varianceShiftPair.getEntry(1);

          diffs[0] = cutoffPut - black(fwd + shift, _strikeCutoff * fwd + shift, variance, false);
          diffs[1] = spreadPut - black(fwd + shift, (_strikeCutoff + _strikeSpread) * fwd + shift, variance, false);
          //          System.err.println(varianceShiftPair.getEntry(0) + "\t" + varianceShiftPair.getEntry(1) + "\t" + diffs[0] + "\t" + diffs[1]);
          return new DoubleMatrix1D(diffs);
        }
      };
      // 

      final BroydenVectorRootFinder solver = new BroydenVectorRootFinder(1E-6, 1E-6, 10000); // TODO PASS ARGS AS CONTOL PARAMS !!!
      DoubleMatrix1D guess = new DoubleMatrix1D(new double[] {0.5 * cutoffVar, 0.5 * fwd }); // Start with the Lognormal that hits the cutoff price // TODO SET TO REASONABLE VALS
      try {
        //        System.err.println("Variance" + "\t" + "Shift" + "\t" + "diffCutoff" + "\t" + "diffSpread");
        shiftedLnParams = solver.getRoot(fitShiftedLognormal, guess);
        System.err.println("cutoff vol is " + cutoffVol);
        System.err.println("spread vol is " + spreadVol);
        System.err.println("fit vol is " + Math.sqrt(shiftedLnParams.getEntry(0) / expiry));
        System.err.println("fit shift is " + shiftedLnParams.getEntry(1));
      } catch (java.lang.IllegalArgumentException e) {
        System.err.println("VarSwapStaticReplication.impliedVariance failed to find roots to fit a Shifted Lognormal Distribution to your cutoff and spread targets."
            + e.getMessage()); // "Matrix is singular; could not perform LU decomposition"
        throw new RuntimeException(e);
      } catch (com.opengamma.math.MathException e) {
        System.err.println("VarSwapStaticReplication.impliedVariance failed to find roots to fit a Shifted Lognormal Distribution to your cutoff and spread targets."
            + e.getMessage()); // "Failed to converge in backtracking, even after a Jacobian recalculation."
        throw new RuntimeException(e);
      }

      // TEST 888888888888 REMOVE ME
      double zeroStrikePut = black(fwd + shiftedLnParams.getEntry(1), 0.0 + shiftedLnParams.getEntry(1), shiftedLnParams.getEntry(0), false);
      //      System.err.println("Price of zero strike put: " + zeroStrikePut);

    } else {
      shiftedLnParams = null;
    }

    // *********************************************************    
    // 2. Define integrand, the position to hold in each otmOption(k) = 2 / strike^2, where otmOption is a call if k>fwd and a put otherwise
    //    Note:  strike space is parameterised wrt the forward, moneyness := strike/fwd i.e. atm moneyness=1
    final Function1D<Double, Double> otmOptionAndWeight = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double moneyness) {

        final double strike = moneyness * fwd;
        final boolean isCall = moneyness > 1; // if strike > fwd, the call is out of the money..

        final double weight = 2 / (fwd * moneyness * moneyness);
        double otmPrice;
        double itmPrice;

        if (_cutoffProvided && moneyness < _strikeCutoff) { // Extrapolate with ShiftedLognormal
          otmPrice = black(fwd + shiftedLnParams.getEntry(1), moneyness * fwd + shiftedLnParams.getEntry(1), shiftedLnParams.getEntry(0), isCall);
        } else {
          DoublesPair coord = DoublesPair.of(expiry, strike);
          double vol = vsurf.getVolatility(coord);
          otmPrice = black(fwd, moneyness * fwd, vol * vol * expiry, isCall);
        }

        final double anotherVol = 0.25; // !!! Why do I have to input a vol?!?
        final BlackFunctionData blackData = new BlackFunctionData(fwd, 1.0, anotherVol);
        final EuropeanVanillaOption blackDataRest = new EuropeanVanillaOption(strike, expiry, isCall);
        final BlackImpliedVolatilityFormula theFormulaItself = new BlackImpliedVolatilityFormula();
        try {
          double impVol = theFormulaItself.getImpliedVolatility(blackData, blackDataRest, otmPrice);
          System.err.println(strike + "," + impVol + "," + otmPrice * weight);
        } catch (com.opengamma.math.MathException e) {
          System.err.println("VarSwapStaticReplication.impliedVariance failed to compute an ImpliedVolatility with relative strike = " + moneyness
              + ". Message: " + e.getMessage());
        }

        return otmPrice * weight;
      }
    };

    // 3. Compute variance hedge by integrating positions over all strikes
    System.err.println("strike" + "," + "impVol" + "," + "weight*price");
    double variance = _integrator.integrate(otmOptionAndWeight, _lowerBound, _upperBound);
    return variance;
  }
}
