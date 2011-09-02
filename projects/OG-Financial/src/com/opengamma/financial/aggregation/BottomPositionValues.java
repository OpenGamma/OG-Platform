/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.List;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;

/**
 * Function for producing the "bottom" N slice outputs for all positions underneath a node. The results are sorted in ascending
 * order.
 */
public class BottomPositionValues extends SlicedPositionValues {

  /**
   * Value name on the result produced.
   */
  public static final String VALUE_NAME = "BottomPositionValues";

  // SlicedPositionValues

  @Override
  protected String getValueName() {
    return VALUE_NAME;
  }

  @Override
  protected boolean validateConstraints(final ValueProperties constraints) {
    final Integer count = getIntegerConstraint(constraints, COUNT_PROPERTY);
    if ((count == null) || (count <= 0)) {
      return false;
    }
    return true;
  }

  @Override
  protected ValueProperties.Builder createRawResultsProperties() {
    return super.createRawResultsProperties().withAny(COUNT_PROPERTY);
  }

  @Override
  protected List<ComputedValue> sliceResults(final List<ComputedValue> ascendingOrder, final ValueProperties constraints, final ValueProperties.Builder properties) {
    final int count = getIntegerConstraint(constraints, COUNT_PROPERTY);
    properties.with(COUNT_PROPERTY, Integer.toString(count));
    return ascendingOrder.subList(0, Math.min(count, ascendingOrder.size()));
  }

}
