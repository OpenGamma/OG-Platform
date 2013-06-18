/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;

/**
 * Manipulations that act on {@link VolatilitySurface}s. Each inner class implements a single transformation.
 */
public class VolatilitySurfaceManipulations {

  public static class ParallelShift implements StructureManipulator<VolatilitySurface> {

    private final double _shift;

    public ParallelShift(double shift) {
      _shift = shift;
    }

    @Override
    public VolatilitySurface execute(VolatilitySurface surface) {
      return surface.withParallelShift(_shift);
    }
  }

  public static class SingleAdditiveShift implements StructureManipulator<VolatilitySurface> {

    private final double _x;
    private final double _y;
    private final double _shift;

    public SingleAdditiveShift(double x, double y, double shift) {
      _x = x;
      _y = y;
      _shift = shift;
    }

    @Override
    public VolatilitySurface execute(VolatilitySurface surface) {
      return surface.withSingleAdditiveShift(_x, _y, _shift);
    }
  }

  public static class MultipleAdditiveShifts implements StructureManipulator<VolatilitySurface> {

    private final double[] _x;
    private final double[] _y;
    private final double[] _shifts;

    public MultipleAdditiveShifts(double[] x, double[] y, double[] shifts) {
      _x = x;
      _y = y;
      _shifts = shifts;
    }

    @Override
    public VolatilitySurface execute(VolatilitySurface surface) {
      return surface.withMultipleAdditiveShifts(_x, _y, _shifts);
    }
  }

  public static class ConstantMultiplicativeShift implements StructureManipulator<VolatilitySurface> {

    private final double _shift;

    public ConstantMultiplicativeShift(double shift) {
      _shift = shift;
    }

    @Override
    public VolatilitySurface execute(VolatilitySurface surface) {
      return surface.withConstantMultiplicativeShift(_shift);
    }
  }

  public static class SingleMultiplicativeShift implements StructureManipulator<VolatilitySurface> {

    private final double _x;
    private final double _y;
    private final double _shift;

    public SingleMultiplicativeShift(double x, double y, double shift) {
      _x = x;
      _y = y;
      _shift = shift;
    }

    @Override
    public VolatilitySurface execute(VolatilitySurface surface) {
      return surface.withSingleMultiplicativeShift(_x, _y, _shift);
    }
  }

  public static class MultipleMultiplicativeShifts implements StructureManipulator<VolatilitySurface> {

    private final double[] _x;
    private final double[] _y;
    private final double[] _shifts;

    public MultipleMultiplicativeShifts(double[] x, double[] y, double[] shifts) {
      _x = x;
      _y = y;
      _shifts = shifts;
    }

    @Override
    public VolatilitySurface execute(VolatilitySurface surface) {
      return surface.withMultipleMultiplicativeShifts(_x, _y, _shifts);
    }
  }
}
