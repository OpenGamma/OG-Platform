/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DStandardCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.DirichletBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;
import com.opengamma.analytics.financial.model.finitedifference.PDE1DDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEResults1D;
import com.opengamma.analytics.financial.model.finitedifference.ThetaMethodFiniteDifference;
import com.opengamma.analytics.financial.model.option.definition.Barrier;
import com.opengamma.analytics.financial.model.option.definition.Barrier.BarrierType;
import com.opengamma.analytics.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackBarrierPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SurfaceArrayUtils;
import com.opengamma.analytics.math.function.Function1D;

/**
 * 
 */
public class BarrierOptionPricer {

  private static final InitialConditionsProvider ICP = new InitialConditionsProvider();
  private static final PDE1DCoefficientsProvider PDE = new PDE1DCoefficientsProvider();
  private static final ThetaMethodFiniteDifference SOLVER = new ThetaMethodFiniteDifference();

  private static final int DEFAULT_XNODES = 100;
  private static final int DEFAULT_TNODES = 50;
  private static final double DEFAULT_LAMBDA = 0.0;
  private static final double DEFAULT_BUNCHING = 1.0;
  private static final double DEFAULT_Z = 2.0;

  private final int _nTNodes;
  private final int _nXNodes;
  private final double _lambda;
  private final double _bunching;
  private final double _z = DEFAULT_Z;

  public BarrierOptionPricer() {
    _nTNodes = DEFAULT_TNODES;
    _nXNodes = DEFAULT_XNODES;
    _lambda = DEFAULT_LAMBDA;
    _bunching = DEFAULT_BUNCHING;
  }

  public BarrierOptionPricer(final int numXNodes, final int numTNodes, final double lambda, final double bunching) {

    _nXNodes = numXNodes;
    _nTNodes = numTNodes;
    _lambda = lambda;
    _bunching = bunching;

  }

  /**
   * Computes the price of a barrier option in the Black world.
   * @param option The underlying European vanilla option.
   * @param barrier The barrier.
   * @param rebate The rebate. This is paid <b>immediately</b> if the knock-out barrier is hit and at expiry if the knock-in barrier is not hit
   * @param spot The spot price.
   * @param costOfCarry The cost of carry (i.e. the forward = spot*exp(costOfCarry*T) )
   * @param rate The interest rate.
   * @param sigma The Black volatility.
   * @return The price.
   */
  public double getPrice(final EuropeanVanillaOption option, final Barrier barrier, final double rebate, final double spot, final double costOfCarry, final double rate, final double sigma) {
    Validate.notNull(option, "option");
    Validate.notNull(barrier, "barrier");
    final boolean isKnockIn = (barrier.getKnockType() == KnockType.IN);
    final boolean isDown = (barrier.getBarrierType() == BarrierType.DOWN);

    //in these pathological cases the barrier is hit immediately so the value is just from the rebate (knock-out) or a European option (knock-in)
    if (isDown && spot <= barrier.getBarrierLevel() || !isDown && spot >= barrier.getBarrierLevel()) {
      if (isKnockIn) {
        return blackPrice(spot, option.getStrike(), option.getTimeToExpiry(), rate, costOfCarry, sigma, option.isCall());
      }
      return rebate;
    }
    if (isKnockIn) {
      return inBarrier(spot, barrier.getBarrierLevel(), option.getStrike(), option.getTimeToExpiry(), rate, costOfCarry, sigma, option.isCall(), rebate);
    }
    return outBarrier(spot, barrier.getBarrierLevel(), option.getStrike(), option.getTimeToExpiry(), rate, costOfCarry, sigma, option.isCall(), rebate);
  }

  /**
   * Computes the price of a one-touch out barrier option in the Black-Scholes world by solving the BS PDE on a finite difference grid. If a barrier is hit at any time before expiry,
   * the option is cancelled (knocked-out) and a rebate (which is often zero) is paid <b>immediately</b>. If the barrier is not hit, then a normal European option payment is made. <p>
   * If the barrier is above the spot it is assumed to be an up-and-out barrier (otherwise it would expire immediately) otherwise it is a down-and-out barrier
   * As there are exact formulae for this case (see {@link BlackBarrierPriceFunction}), this is purely for testing purposes.
   * @param spot The current (i.e. option price time) of the underlying
   * @param barrierLevel The barrier level
   * @param strike The strike of the European option
   * @param expiry The expiry of the option
   * @param rate The interest rate.
   * @param carry The cost-of-carry (i.e. the forward = spot*exp(costOfCarry*T) )
   * @param vol The Black volatility.
   * @param isCall true for call
   * @param rebate The rebate amount.
   * @return The price.
   */
  public double outBarrier(final double spot, final double barrierLevel, final double strike, final double expiry, final double rate, final double carry,
      final double vol, final boolean isCall, final double rebate) {

    final Function1D<Double, Double> intCon = ICP.getEuropeanPayoff(strike, isCall);
    final ConvectionDiffusionPDE1DStandardCoefficients pde = PDE.getBlackScholes(rate, rate - carry, vol);
    final boolean isUp = barrierLevel > spot;

    final double adj = 0.0; // _lambda == 0 ? ZETA * vol * Math.sqrt(expiry / (_nTNodes - 1)) : 0.0;

    double sMin;
    double sMax;
    BoundaryCondition lower;
    BoundaryCondition upper;
    if (isUp) {
      sMin = 0.0;
      sMax = barrierLevel * Math.exp(-adj); //bring the barrier DOWN slightly to adjust for discrete monitoring
      if (isCall) {
        lower = new DirichletBoundaryCondition(0.0, sMin);
      } else {
        final Function1D<Double, Double> lowerValue = new Function1D<Double, Double>() {
          @Override
          public Double evaluate(final Double tau) {
            return Math.exp(-rate * tau) * strike;
          }
        };
        lower = new DirichletBoundaryCondition(lowerValue, sMin);
      }
      upper = new DirichletBoundaryCondition(rebate, sMax);
    } else {
      sMin = barrierLevel * Math.exp(adj); //bring the barrier UP slightly to adjust for discrete monitoring
      sMax = spot * Math.exp(_z * Math.sqrt(expiry));
      lower = new DirichletBoundaryCondition(rebate, sMin);

      if (isCall) {
        final Function1D<Double, Double> upperValue = new Function1D<Double, Double>() {
          @Override
          public Double evaluate(final Double tau) {
            return Math.exp(-rate * tau) * (spot * Math.exp(carry * tau) - strike);
          }
        };
        upper = new DirichletBoundaryCondition(upperValue, sMax);
      } else {
        upper = new DirichletBoundaryCondition(0.0, sMax);
      }
    }

    final MeshingFunction tMesh = new ExponentialMeshing(0, expiry, _nTNodes, _lambda);
    final MeshingFunction xMesh = new HyperbolicMeshing(sMin, sMax, spot, _nXNodes, _bunching);
    final PDEGrid1D grid = new PDEGrid1D(tMesh, xMesh);
    final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> pdeData = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(pde, intCon, lower, upper, grid);

    final PDEResults1D res = SOLVER.solve(pdeData);
    //for now just do linear interpolation on price. TODO replace this with something more robust
    final double[] xNodes = grid.getSpaceNodes();

    final int index = SurfaceArrayUtils.getLowerBoundIndex(xNodes, spot);
    final double w = (xNodes[index + 1] - spot) / (xNodes[index + 1] - xNodes[index]);
    return w * res.getFunctionValue(index) + (1 - w) * res.getFunctionValue(index + 1);
  }

  /**
   * Computes the price of a one-touch in barrier option in the Black-Scholes world by solving the BS PDE on a finite difference grid. If a barrier is hit at any time before expiry,
   * the option becomes a simple European (call or put). If the barrier is not hit a rebate is paid at the option expiry<p>
   * If the barrier is above the spot it is assumed to be an up-and-in barrier  otherwise it is a down-and-in barrier
   * As there are exact formulae for this case (see {@link BlackBarrierPriceFunction}), this is purely for testing purposes.
   * @param spot The current (i.e. option price time) of the underlying
   * @param barrierLevel The barrier level
   * @param strike The strike of the European option
   * @param expiry The expiry of the option
   * @param rate The interest rate.
   * @param carry The cost-of-carry (i.e. the forward = spot*exp(costOfCarry*T) )
   * @param vol The Black volatility.
   * @param isCall true for call
   * @param rebate The rebate amount.
   * @return The price.
   */
  public double inBarrier(final double spot, final double barrierLevel, final double strike, final double expiry, final double rate, final double carry,
      final double vol, final boolean isCall, final double rebate) {
    final double outPrice = outBarrierSpecial(spot, barrierLevel, strike, expiry, rate, carry, vol, isCall, rebate);
    final double bsPrice = blackPrice(spot, strike, expiry, rate, carry, vol, isCall);
    return bsPrice + Math.exp(-rate * expiry) * rebate - outPrice;
  }

  /**
   * Computes the price of a one-touch out barrier option in the Black-Scholes world assuming the rebate is paid at the option expiry. This is NOT the case for a out barrier,
   * but is for an in, and as we must price an in barrier and a European option plus a bond (the rebate) minus an out, we need this special case.
   * @param spot The current (i.e. option price time) of the underlying
   * @param barrierLevel The barrier level
   * @param strike The strike of the European option
   * @param expiry The expiry of the option
   * @param rate The interest rate.
   * @param carry The cost-of-carry (i.e. the forward = spot*exp(costOfCarry*T) )
   * @param vol The Black volatility.
   * @param isCall true for call
   * @param rebate The rebate amount.
   * @return The price.
   */
  protected double outBarrierSpecial(final double spot, final double barrierLevel, final double strike, final double expiry, final double rate, final double carry,
      final double vol, final boolean isCall, final double rebate) {

    final Function1D<Double, Double> intCon = ICP.getEuropeanPayoff(strike, isCall);
    final ConvectionDiffusionPDE1DStandardCoefficients pde = PDE.getBlackScholes(rate, rate - carry, vol);
    final boolean isUp = barrierLevel > spot;

    final Function1D<Double, Double> rebateValue = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double tau) {
        return rebate * Math.exp(-rate * tau);
      }
    };

    double sMin;
    double sMax;
    BoundaryCondition lower;
    BoundaryCondition upper;
    if (isUp) {
      sMin = 0.0;
      sMax = barrierLevel;
      if (isCall) {
        lower = new DirichletBoundaryCondition(0.0, sMin);
      } else {
        final Function1D<Double, Double> lowerValue = new Function1D<Double, Double>() {
          @Override
          public Double evaluate(final Double tau) {
            return Math.exp(-rate * tau) * strike;
          }
        };
        lower = new DirichletBoundaryCondition(lowerValue, sMin);
      }
      upper = new DirichletBoundaryCondition(rebateValue, sMax);
    } else {
      sMin = barrierLevel;
      sMax = spot * Math.exp(_z * Math.sqrt(expiry));
      lower = new DirichletBoundaryCondition(rebateValue, sMin);
      if (isCall) {
        final Function1D<Double, Double> upperValue = new Function1D<Double, Double>() {
          @Override
          public Double evaluate(final Double tau) {
            return Math.exp(-rate * tau) * (spot * Math.exp(carry * tau) - strike);
          }
        };
        upper = new DirichletBoundaryCondition(upperValue, sMax);
      } else {
        upper = new DirichletBoundaryCondition(0.0, sMax);
      }
    }

    final MeshingFunction tMesh = new ExponentialMeshing(0, expiry, _nTNodes, _lambda);
    final MeshingFunction xMesh = new HyperbolicMeshing(sMin, sMax, spot, _nXNodes, _bunching);
    final PDEGrid1D grid = new PDEGrid1D(tMesh, xMesh);
    final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> pdeData = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(pde, intCon, lower, upper, grid);

    final PDEResults1D res = SOLVER.solve(pdeData);
    //for now just do linear interpolation on price. TODO replace this with something more robust
    final double[] xNodes = grid.getSpaceNodes();
    final int index = SurfaceArrayUtils.getLowerBoundIndex(xNodes, spot);
    final double w = (xNodes[index + 1] - spot) / (xNodes[index + 1] - xNodes[index]);
    return w * res.getFunctionValue(index) + (1 - w) * res.getFunctionValue(index + 1);
  }

  protected double blackPrice(final double spot, final double strike, final double expiry, final double rate, final double carry,
      final double vol, final boolean isCall) {

    final Function1D<Double, Double> intCon = ICP.getEuropeanPayoff(strike, isCall);
    final ConvectionDiffusionPDE1DStandardCoefficients pde = PDE.getBlackScholes(rate, rate - carry, vol);

    final double sMin = 0.0;
    final double sMax = spot * Math.exp(_z * Math.sqrt(expiry));
    BoundaryCondition lower;
    BoundaryCondition upper;
    if (isCall) {
      lower = new DirichletBoundaryCondition(0.0, sMin);
      final Function1D<Double, Double> upperValue = new Function1D<Double, Double>() {
        @Override
        public Double evaluate(final Double tau) {
          return Math.exp(-rate * tau) * (spot * Math.exp(carry * tau) - strike);
        }
      };
      upper = new DirichletBoundaryCondition(upperValue, sMax);
    } else {
      final Function1D<Double, Double> lowerValue = new Function1D<Double, Double>() {
        @Override
        public Double evaluate(final Double tau) {
          return Math.exp(-rate * tau) * strike;
        }
      };
      lower = new DirichletBoundaryCondition(lowerValue, sMin);
      upper = new DirichletBoundaryCondition(0.0, sMax);
    }

    final MeshingFunction tMesh = new ExponentialMeshing(0, expiry, _nTNodes, _lambda);
    final MeshingFunction xMesh = new HyperbolicMeshing(sMin, sMax, spot, _nXNodes, _bunching);
    final PDEGrid1D grid = new PDEGrid1D(tMesh, xMesh);
    final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> pdeData = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(pde, intCon, lower, upper, grid);

    final PDEResults1D res = SOLVER.solve(pdeData);
    //for now just do linear interpolation on price. TODO replace this with something more robust
    final double[] xNodes = grid.getSpaceNodes();
    final int index = SurfaceArrayUtils.getLowerBoundIndex(xNodes, spot);
    final double w = (xNodes[index + 1] - spot) / (xNodes[index + 1] - xNodes[index]);
    return w * res.getFunctionValue(index) + (1 - w) * res.getFunctionValue(index + 1);
  }
}
