/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.List;

import org.threeten.bp.Period;

import com.google.common.collect.Lists;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class VolatilitySurfaceShiftManipulatorBuilder {

  private final VolatilitySurfaceSelector _selector;
  private final Scenario _scenario;
  private final ScenarioShiftType _shiftType;

  private final List<VolatilitySurfaceShift> _shifts = Lists.newArrayList();

  public VolatilitySurfaceShiftManipulatorBuilder(VolatilitySurfaceSelector selector,
                                                  Scenario scenario,
                                                  ScenarioShiftType shiftType) {
    _selector = ArgumentChecker.notNull(selector, "selector");
    _scenario = ArgumentChecker.notNull(scenario, "scenario");
    _shiftType = ArgumentChecker.notNull(shiftType, "shiftType");
  }

  /**
   *
   * @param x The x co-ordinate of the point to shift, must be a {@link Period} or {@link Number}.
   * @param y The x co-ordinate of the point to shift, must be a {@link Period} or {@link Number}.
   * @param shift The amount to shift
   */
  public void shift(Object x, Object y, Number shift) {
    _shifts.add(new VolatilitySurfaceShift(x, y, shift));
  }

  public void build() {
    VolatilitySurfaceShiftManipulator manipulator = VolatilitySurfaceShiftManipulator.create(_shiftType, _shifts);
    _scenario.add(_selector, manipulator);
  }
}
