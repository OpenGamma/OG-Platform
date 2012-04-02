/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.analytics.math.surface.SurfaceShiftFunctionFactory;

/**
 *  A surface that contains the Black (implied) volatility as a function of time to maturity and (call) delta. Delta is in the range [0,1], where 0.5 is
 *  the ATM (DNS), Delta > 0.5 is ITM calls and Delta < 0.5 is OTM calls. Since prices are normally quoted for OTM options, Delta > 0.5 will actually be populated
 *  by OTM puts (Delta_put > -0.5, since Delta_call - Delta_put = 1 always holds).
 */
public class BlackVolatilitySurfaceDelta extends BlackVolatilitySurface<Delta> {
  private static final double EPS = 1e-6;
  private static final BracketRoot BRACKETER = new BracketRoot();
  private static final BisectionSingleRootFinder ROOT_FINDER = new BisectionSingleRootFinder(EPS);
  private final ForwardCurve _fc;

  /**
   * 
   * @param surface A implied volatility surface parameterised by time and (call) delta
   * @param forwardCurve the forward curve
   */
  public BlackVolatilitySurfaceDelta(final Surface<Double, Double, Double> surface, final ForwardCurve forwardCurve) {
    super(surface);
    Validate.notNull(forwardCurve, "null forward curve");
    _fc = forwardCurve;
  }

  /**
   * Return a volatility for the expiry, strike pair provided.
   * Interpolation/extrapolation behaviour depends on underlying surface, which is parameterised by time and (call) delta
   * @param t time to maturity
   * @param k strike
   * @return The Black (implied) volatility
   */
  @Override
  public double getVolatility(final double t, final double k) {
    final double fwd = _fc.getForward(t);
    final boolean isCall = k >= fwd;
    final double omega = isCall ? 0 : 1;

    final Function1D<Double, Double> func = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double delta) {
        final double sigma = getVolatilityForDelta(t, omega + delta);
        return BlackFormulaRepository.delta(fwd, k, t, sigma, isCall) - delta;
      }
    };

    final double deltaApprox = BlackFormulaRepository.delta(fwd, k, t, getVolatilityForDelta(t, 0.5), isCall);
    double l, h;
    double minDelta;
    double maxDelta;
    if (isCall) {
      l = Math.max(0.0, deltaApprox - 0.2);
      h = Math.min(1.0, deltaApprox + 0.2);
      minDelta = 0.0;
      maxDelta = 1.0;
    } else {
      l = Math.max(-1.0, deltaApprox - 0.2);
      h = Math.min(0.0, deltaApprox + 0.2);
      minDelta = -1.0;
      maxDelta = 0.0;
    }
    final double[] range = BRACKETER.getBracketedPoints(func, l, h, minDelta, maxDelta);
    final double delta = omega + ROOT_FINDER.getRoot(func, range[0], range[1]);
    return getVolatilityForDelta(t, delta);
  }

  /**
   * Return a volatility for the expiry, (call) delta pair provided.
   * Interpolation/extrapolation behaviour depends on underlying surface
   * @param t time to maturity
   * @param delta the call delta
   * @return The Black (implied) volatility
   */
  public double getVolatilityForDelta(final double t, final double delta) {
    return getVolatility(t, new Delta(delta));
  }

  public ForwardCurve getForwardCurve() {
    return _fc;
  }

  @Override
  public double getAbsoluteStrike(final double t, final Delta s) {
    final double vol = getVolatility(t, s);
    final double fwd = _fc.getForward(t);
    return BlackFormulaRepository.impliedStrike(s.value(), true, fwd, t, vol);
  }

  @Override
  public BlackVolatilitySurface<Delta> withShift(final double shift, final boolean useAdditive) {
    return new BlackVolatilitySurfaceDelta(SurfaceShiftFunctionFactory.getShiftedSurface(getSurface(), shift, useAdditive), _fc);
  }

  @Override
  public BlackVolatilitySurface<Delta> withSurface(final Surface<Double, Double, Double> surface) {
    return new BlackVolatilitySurfaceDelta(surface, _fc);
  }

  @Override
  public <S, U> U accept(final BlackVolatilitySurfaceVisitor<S, U> visitor, final S data) {
    return visitor.visitDelta(this, data);
  }

  @Override
  public <U> U accept(final BlackVolatilitySurfaceVisitor<?, U> visitor) {
    return visitor.visitDelta(this);
  }

}
