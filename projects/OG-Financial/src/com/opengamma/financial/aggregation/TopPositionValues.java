/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;

/**
 * Function for producing the "top" N slice outputs for all positions underneath a node. The results are sorted in descending order.
 */
public class TopPositionValues extends SlicedPositionValues {

  /**
   * Value name on the result produced.
   */
  public static final String VALUE_NAME = "TopPositionValues";

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
    final List<ComputedValue> result = new ArrayList<ComputedValue>(ascendingOrder.subList(Math.max(ascendingOrder.size() - count, 0), ascendingOrder.size()));
    Collections.reverse(result);
    return result;
  }

}
