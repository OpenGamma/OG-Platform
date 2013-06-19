/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.math.BigDecimal;
import java.util.List;

import com.opengamma.integration.marketdata.manipulator.dsl.volsurface.VolatilitySurfaceConstantMultiplicativeShift;
import com.opengamma.integration.marketdata.manipulator.dsl.volsurface.VolatilitySurfaceMultipleAdditiveShifts;
import com.opengamma.integration.marketdata.manipulator.dsl.volsurface.VolatilitySurfaceMultipleMultiplicativeShifts;
import com.opengamma.integration.marketdata.manipulator.dsl.volsurface.VolatilitySurfaceParallelShift;
import com.opengamma.integration.marketdata.manipulator.dsl.volsurface.VolatilitySurfaceSingleAdditiveShift;
import com.opengamma.integration.marketdata.manipulator.dsl.volsurface.VolatilitySurfaceSingleMultiplicativeShift;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class VolatilitySurfaceManipulatorBuilder {

  /** Selector whose selected items will be modified by the manipulators from this builder. */
  private final VolatilitySurfaceSelector _selector;
  /** The scenario to which manipulations are added. */
  private final Scenario _scenario;

  /* package */ VolatilitySurfaceManipulatorBuilder(Scenario scenario, VolatilitySurfaceSelector selector) {
    ArgumentChecker.notNull(scenario, "scenario");
    ArgumentChecker.notNull(selector, "selector");
    _scenario = scenario;
    _selector = selector;
  }

  public VolatilitySurfaceManipulatorBuilder parallelShift(double shift) {
    _scenario.add(_selector, new VolatilitySurfaceParallelShift(shift));
    return this;
  }

  public VolatilitySurfaceManipulatorBuilder singleAdditiveShift(double x, double y, double shift) {
    _scenario.add(_selector, new VolatilitySurfaceSingleAdditiveShift(x, y, shift));
    return this;
  }

  public VolatilitySurfaceManipulatorBuilder multipleAdditiveShifts(double[] x, double[] y, double[] shifts) {
    _scenario.add(_selector, new VolatilitySurfaceMultipleAdditiveShifts(x, y, shifts));
    return this;
  }

  public VolatilitySurfaceManipulatorBuilder multipleAdditiveShifts(List<BigDecimal> x, List<BigDecimal> y, List<BigDecimal> shifts) {
    _scenario.add(_selector, new VolatilitySurfaceMultipleAdditiveShifts(array(x), array(y), array(shifts)));
    return this;
  }

  public VolatilitySurfaceManipulatorBuilder constantMultiplicativeShift(double shift) {
    _scenario.add(_selector, new VolatilitySurfaceConstantMultiplicativeShift(shift));
    return this;
  }

  public VolatilitySurfaceManipulatorBuilder singleMultiplicativeShift(double x, double y, double shift) {
    _scenario.add(_selector, new VolatilitySurfaceSingleMultiplicativeShift(x, y, shift));
    return this;
  }

  public VolatilitySurfaceManipulatorBuilder multipleMultiplicativeShifts(double[] x, double[] y, double[] shifts) {
    _scenario.add(_selector, new VolatilitySurfaceMultipleMultiplicativeShifts(x, y, shifts));
    return this;
  }

  public VolatilitySurfaceManipulatorBuilder multipleMultiplicativeShifts(List<BigDecimal> x, List<BigDecimal> y, List<BigDecimal> shifts) {
    _scenario.add(_selector, new VolatilitySurfaceMultipleMultiplicativeShifts(array(x), array(y), array(shifts)));
    return this;
  }

  private static double[] array(List<BigDecimal> list) {
    double[] array = new double[list.size()];
    int index = 0;
    for (BigDecimal value : list) {
      array[index++] = value.doubleValue();
    }
    return array;
  }
}
