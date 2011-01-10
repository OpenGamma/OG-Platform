/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public final class RealFunctionIntegrator1DFactory {
  // TODO add more integration types
  /**
   * 
   */
  public static final String ROMBERG = "Romberg";
  /**
   * 
   */
  public static final RombergIntegrator1D ROMBERG_INSTANCE = new RombergIntegrator1D();
  /**
   * 
   */
  public static final String SIMPSON = "Simpson";
  /**
   * 
   */
  public static final SimpsonIntegrator1D SIMPSON_INSTANCE = new SimpsonIntegrator1D();
  /**
   * 
   */
  public static final String EXTENDED_TRAPEZOID = "ExtendedTrapezoid";
  /**
   * 
   */
  public static final ExtendedTrapezoidIntegrator1D EXTENDED_TRAPEZOID_INSTANCE = new ExtendedTrapezoidIntegrator1D();

  private static final Map<String, Integrator1D<Double, Function1D<Double, Double>, Double>> s_staticInstances;
  private static final Map<Class<?>, String> s_instanceNames;

  static {
    final Map<String, Integrator1D<Double, Function1D<Double, Double>, Double>> staticInstances = new HashMap<String, Integrator1D<Double, Function1D<Double, Double>, Double>>();
    final Map<Class<?>, String> instanceNames = new HashMap<Class<?>, String>();
    staticInstances.put(ROMBERG, ROMBERG_INSTANCE);
    instanceNames.put(ROMBERG_INSTANCE.getClass(), ROMBERG);
    staticInstances.put(SIMPSON, SIMPSON_INSTANCE);
    instanceNames.put(SIMPSON_INSTANCE.getClass(), SIMPSON);
    staticInstances.put(EXTENDED_TRAPEZOID, EXTENDED_TRAPEZOID_INSTANCE);
    instanceNames.put(EXTENDED_TRAPEZOID_INSTANCE.getClass(), EXTENDED_TRAPEZOID);
    s_staticInstances = new HashMap<String, Integrator1D<Double, Function1D<Double, Double>, Double>>(staticInstances);
    s_instanceNames = new HashMap<Class<?>, String>(instanceNames);
  }

  private RealFunctionIntegrator1DFactory() {

  }

  public static Integrator1D<Double, Function1D<Double, Double>, Double> getIntegrator(final String integratorName) {
    final Integrator1D<Double, Function1D<Double, Double>, Double> integrator = s_staticInstances.get(integratorName);
    if (integrator != null) {
      return integrator;
    }
    throw new IllegalArgumentException("Integrator " + integratorName + " not handled");
  }

  public static String getIntegratorName(final Integrator1D<Double, Function1D<Double, Double>, Double> integrator) {
    if (integrator == null) {
      return null;
    }
    return s_instanceNames.get(integrator.getClass());
  }
}
