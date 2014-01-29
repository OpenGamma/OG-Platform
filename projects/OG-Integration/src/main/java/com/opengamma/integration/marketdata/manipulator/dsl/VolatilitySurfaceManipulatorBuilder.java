/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Arrays;
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
public class VolatilitySurfaceManipulatorBuilder {

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

  public VolatilitySurfaceManipulatorBuilder shifts(ScenarioShiftType shiftType, VolatilitySurfaceShift... shifts) {
    _scenario.add(_selector, VolatilitySurfaceShiftManipulator.create(shiftType, Arrays.asList(shifts)));
    return this;
  }

  public VolatilitySurfaceManipulatorBuilder parallelShift(ScenarioShiftType shiftType, Number shift) {
    if (shiftType == ScenarioShiftType.ABSOLUTE) {
      _scenario.add(_selector, new VolatilitySurfaceParallelShift(shift.doubleValue()));
    } else {
      _scenario.add(_selector, new VolatilitySurfaceConstantMultiplicativeShift(shift.doubleValue() + 1));
    }
    return this;
  }

  /**
   * @deprecated Use {@link #parallelShift} with {@link ScenarioShiftType#ABSOLUTE}
   */
  @Deprecated
  public VolatilitySurfaceManipulatorBuilder parallelShift(Number shift) {
    _scenario.add(_selector, new VolatilitySurfaceParallelShift(shift.doubleValue()));
    return this;
  }

  /**
   * @deprecated Use {@link #parallelShift} with {@link ScenarioShiftType#RELATIVE}
   */
  @Deprecated
  public VolatilitySurfaceManipulatorBuilder constantMultiplicativeShift(Number shift) {
    _scenario.add(_selector, new VolatilitySurfaceConstantMultiplicativeShift(shift.doubleValue()));
    return this;
  }

  /**
   * @deprecated Use {@link #shifts)} with {@link ScenarioShiftType#ABSOLUTE} and one shift
   */
  @Deprecated
  public VolatilitySurfaceManipulatorBuilder singleAdditiveShift(Number x, Number y, Number shift) {
    _scenario.add(_selector, new VolatilitySurfaceSingleAdditiveShift(x.doubleValue(), y.doubleValue(), shift.doubleValue()));
    return this;
  }

  /**
   * @deprecated Use {@link #shifts)} with {@link ScenarioShiftType#ABSOLUTE} and multiple shifts
   */
  @Deprecated
  public VolatilitySurfaceManipulatorBuilder multipleAdditiveShifts(List<Number> x, List<Number> y, List<Number> shifts) {
    _scenario.add(_selector, new VolatilitySurfaceMultipleAdditiveShifts(array(x), array(y), array(shifts)));
    return this;
  }

  /**
   * @deprecated Use {@link #shifts)} with {@link ScenarioShiftType#RELATIVE} and one shift
   */
  @Deprecated
  public VolatilitySurfaceManipulatorBuilder singleMultiplicativeShift(Number x, Number y, Number shift) {
    _scenario.add(_selector, new VolatilitySurfaceSingleMultiplicativeShift(x.doubleValue(), y.doubleValue(), shift.doubleValue()));
    return this;
  }

  /**
   * @deprecated Use {@link #shifts)} with {@link ScenarioShiftType#RELATIVE} and multiple shifts
   */
  @Deprecated
  public VolatilitySurfaceManipulatorBuilder multipleMultiplicativeShifts(List<Number> x, List<Number> y, List<Number> shifts) {
    _scenario.add(_selector, new VolatilitySurfaceMultipleMultiplicativeShifts(array(x), array(y), array(shifts)));
    return this;
  }

  private static double[] array(List<Number> list) {
    double[] array = new double[list.size()];
    int index = 0;
    for (Number value : list) {
      array[index++] = value.doubleValue();
    }
    return array;
  }

  /* package */ VolatilitySurfaceSelector getSelector() {
    return _selector;
  }

  /* package */ Scenario getScenario() {
    return _scenario;
  }
}
