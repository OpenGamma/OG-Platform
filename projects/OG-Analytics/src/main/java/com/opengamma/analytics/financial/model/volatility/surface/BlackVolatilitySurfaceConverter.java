/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.math.surface.Surface;

/**
 * 
 */
public abstract class BlackVolatilitySurfaceConverter {

  private static final SurfaceConverter CAL = SurfaceConverter.getInstance();

  //********************************
  //Conversion to delta surface
  //********************************
  public static BlackVolatilitySurfaceDelta toDeltaSurface(final BlackVolatilitySurfaceLogMoneyness from) {
    Surface<Double, Double, Double> surf = CAL.logMoneynessToDelta(from.getSurface());
    return new BlackVolatilitySurfaceDelta(surf, from.getForwardCurve());
  }

  public static BlackVolatilitySurfaceDelta toDeltaSurface(final BlackVolatilitySurfaceMoneyness from) {
    Surface<Double, Double, Double> surf = CAL.moneynessToDelta(from.getSurface());
    return new BlackVolatilitySurfaceDelta(surf, from.getForwardCurve());
  }

  public static BlackVolatilitySurfaceDelta toDeltaSurface(final BlackVolatilitySurfaceStrike from, final ForwardCurve forwardCurve) {
    Surface<Double, Double, Double> surf = CAL.strikeToDelta(from.getSurface(), forwardCurve);
    return new BlackVolatilitySurfaceDelta(surf, forwardCurve);
  }

  //********************************
  //Conversion to log-moneyness surface
  //********************************
  public static BlackVolatilitySurfaceLogMoneyness toLogMoneynessSurface(final BlackVolatilitySurfaceDelta from) {
    Surface<Double, Double, Double> surf = CAL.deltaToLogMoneyness(from.getSurface());
    return new BlackVolatilitySurfaceLogMoneyness(surf, from.getForwardCurve());
  }

  public static BlackVolatilitySurfaceLogMoneyness toLogMoneynessSurface(final BlackVolatilitySurfaceMoneyness from) {
    Surface<Double, Double, Double> surf = CAL.moneynessToLogMoneyness(from.getSurface());
    return new BlackVolatilitySurfaceLogMoneyness(surf, from.getForwardCurve());
  }

  public static BlackVolatilitySurfaceLogMoneyness toLogMoneynessSurface(final BlackVolatilitySurfaceStrike from, final ForwardCurve fwdCurve) {
    Surface<Double, Double, Double> surf = CAL.strikeToLogMoneyness(from.getSurface(), fwdCurve);
    return new BlackVolatilitySurfaceLogMoneyness(surf, fwdCurve);
  }

  //********************************
  //Conversion to moneyness surface
  //********************************
  public static BlackVolatilitySurfaceMoneyness toMoneynessSurface(final BlackVolatilitySurfaceDelta from) {
    Surface<Double, Double, Double> surf = CAL.deltaToMoneyness(from.getSurface());
    return new BlackVolatilitySurfaceMoneyness(surf, from.getForwardCurve());
  }

  public static BlackVolatilitySurfaceMoneyness toMoneynessSurface(final BlackVolatilitySurfaceLogMoneyness from) {
    Surface<Double, Double, Double> surf = CAL.logMoneynessToMoneyness(from.getSurface());
    return new BlackVolatilitySurfaceMoneyness(surf, from.getForwardCurve());
  }

  public static BlackVolatilitySurfaceMoneyness toMoneynessSurface(final BlackVolatilitySurfaceStrike from, final ForwardCurve fwdCurve) {
    Surface<Double, Double, Double> surf = CAL.strikeToMoneyness(from.getSurface(), fwdCurve);
    return new BlackVolatilitySurfaceMoneyness(surf, fwdCurve);
  }

  //********************************
  // Conversion to strike surface
  //********************************
  public static BlackVolatilitySurfaceStrike toStrikeSurface(final BlackVolatilitySurfaceDelta from) {
    final Surface<Double, Double, Double> surf = CAL.deltaToStrike(from.getSurface(), from.getForwardCurve());
    return new BlackVolatilitySurfaceStrike(surf);
  }

  public static BlackVolatilitySurfaceStrike toStrikeSurface(final BlackVolatilitySurfaceMoneyness from) {
    Surface<Double, Double, Double> surf = CAL.moneynessToStrike(from.getSurface(), from.getForwardCurve());
    return new BlackVolatilitySurfaceStrike(surf);
  }

  public static BlackVolatilitySurfaceStrike toStrikeSurface(final BlackVolatilitySurfaceLogMoneyness from) {
    Surface<Double, Double, Double> surf = CAL.logMoneynessToStrike(from.getSurface(), from.getForwardCurve());
    return new BlackVolatilitySurfaceStrike(surf);
  }

  //********************************
  // Finding of single points 
  //********************************
  public static double strikeForDelta(final double delta, final BlackVolatilitySurfaceStrike strikeSurface, final ForwardCurve fwdCurve, final double t) {
    final BlackVolatilitySurfaceLogMoneyness logMSurf = toLogMoneynessSurface(strikeSurface, fwdCurve);
    final double x = CAL.getlogMoneynessForDelta(delta, logMSurf.getSurface(), t);
    return fwdCurve.getForward(t) * Math.exp(x);
  }

  public static double deltaForStrike(final double strike, final BlackVolatilitySurfaceDelta deltaSurface, final double t) {
    final double fwd = deltaSurface.getForwardCurve().getForward(t);
    final double x = Math.log(strike / fwd);
    return CAL.getDeltaForLogMoneyness(x, deltaSurface.getSurface(), t);
  }

}
