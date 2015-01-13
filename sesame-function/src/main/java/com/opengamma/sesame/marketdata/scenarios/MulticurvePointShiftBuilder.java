/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import com.google.common.collect.ImmutableMap;
import com.opengamma.analytics.ShiftType;
import com.opengamma.sesame.CurveNodeId;
import com.opengamma.util.ArgumentChecker;

/**
 * Builder which provides a fluent API for creating instances of {@link MulticurvePointShift}.
 */
public class MulticurvePointShiftBuilder {

  private final ShiftType _shiftType;
  private final ImmutableMap.Builder<CurveNodeId, Double> _shifts = ImmutableMap.builder();
  
  private MulticurvePointShiftBuilder(ShiftType shiftType) {
    _shiftType = shiftType;
  }

  /**
   * Creates a builder for building a perturbation to apply relative shifts to curve points.
   * <p>
   * A shift of 0.1 (+10%) scales the point value by 1.1, a shift of -0.2 (-20%) scales the point value by 0.8.
   *
   * @return a builder for building a perturbation to apply relative shifts to curve points
   */
  public static MulticurvePointShiftBuilder relative() {
    return new MulticurvePointShiftBuilder(ShiftType.RELATIVE);
  }

  /**
   * Creates a builder for building a perturbation to apply absolute shifts to curve points
   * <p>
   * The shift amount is added to the value at the curve node.
   *
   * @return a builder for building a perturbation to apply absolute shifts to curve points
   */
  public static MulticurvePointShiftBuilder absolute() {
    return new MulticurvePointShiftBuilder(ShiftType.ABSOLUTE);
  }

  /**
   * Adds a shift to the builder.
   *
   * @param curveNodeId the ID of the curve node to which the shift should be applied
   * @param shiftAmount the amount to shift the node's value
   * @return this builder
   */
  public MulticurvePointShiftBuilder shift(CurveNodeId curveNodeId, double shiftAmount) {
    ArgumentChecker.notNull(curveNodeId, "curveNodeId");
    _shifts.put(curveNodeId, shiftAmount);
    return this;
  }

  /**
   * Builds a {@link MulticurvePointShift} instance from the data in this builder.
   *
   * @return a {@link MulticurvePointShift} instance built from the data in this builder
   */
  public MulticurvePointShift build() {
    return MulticurvePointShift.builder().shifts(_shifts.build()).shiftType(_shiftType).build();
  }
}
