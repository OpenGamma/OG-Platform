/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.math.surface.Surface;

/**
 * 
 */
public abstract class BlackVolatilitySurfaceConverter {

  private static final  SurfaceConverter CAL = SurfaceConverter.getInstance();

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
    Surface<Double, Double, Double> surf = CAL.strikeToMoneyness(from.getSurface(), fwdCurve);
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

}
