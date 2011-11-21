/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdata;

import com.opengamma.core.security.Security;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.math.curve.Curve;
import com.opengamma.math.curve.CurveShiftFunctionFactory;

/**
 * Static functions for the EL compiler.
 * <p>
 * Note that the EL evaluation requires static methods, but we need to associate state (e.g. which
 * security source to use for getSecurity). An evaluator instance must hold the class monitor for
 * the duration of the evaluation and set the {@link #s_compiler} reference to itself.
 */
public final class MarketDataELFunctions {

  private static MarketDataELCompiler s_compiler;

  private MarketDataELFunctions() {
  }

  protected static MarketDataELCompiler getCompiler() {
    return s_compiler;
  }

  protected static void setCompiler(final MarketDataELCompiler compiler) {
    s_compiler = compiler;
  }

  public static Security getSecurity(final Object id) {
    if (id instanceof ExternalId) {
      return getCompiler().getSecuritySource().getSecurity(ExternalIdBundle.of((ExternalId) id));
    } else if (id instanceof UniqueId) {
      return getCompiler().getSecuritySource().getSecurity((UniqueId) id);
    } else if (id instanceof ExternalIdBundle) {
      return getCompiler().getSecuritySource().getSecurity((ExternalIdBundle) id);
    } else if (id instanceof Security) {
      return (Security) id;
    } else {
      throw new UnsupportedOperationException("Invalid ID - " + id);
    }
  }

  public static Object pointShiftCurve(final Object curve, final double x, final double shift) {
    if (curve instanceof YieldCurve) {
      final Curve<Double, Double> shifted = CurveShiftFunctionFactory.getShiftedCurve(((YieldCurve) curve).getCurve(), x, shift);
      return new YieldCurve(shifted);
    } else {
      throw new UnsupportedOperationException("Invalid curve - " + curve);
    }
  }

  public static Object parallelShiftCurve(final Object curve, final double shift) {
    if (curve instanceof YieldCurve) {
      final Curve<Double, Double> shifted = CurveShiftFunctionFactory.getShiftedCurve(((YieldCurve) curve).getCurve(), shift);
      return new YieldCurve(shifted);
    } else {
      throw new UnsupportedOperationException("Invalid curve - " + curve);
    }
  }

}
