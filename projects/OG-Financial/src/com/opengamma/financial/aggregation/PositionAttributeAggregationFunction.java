/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.util.ArgumentChecker;

/**
 * Function to classify positions by attribute value on the underlying trades.
 */
public class PositionAttributeAggregationFunction implements AggregationFunction<String> {

  private static final String UNKNOWN = "Unknown";

  private final String _attribute;
  private final String _unknownClassification;

  public PositionAttributeAggregationFunction(final String attribute) {
    this(attribute, UNKNOWN);
  }

  public PositionAttributeAggregationFunction(final String attribute, final String unknownClassification) {
    ArgumentChecker.notNull(attribute, "attribute");
    _attribute = attribute;
    _unknownClassification = unknownClassification;
  }

  public String getUnknownClassification() {
    return _unknownClassification;
  }

  public String getAttribute() {
    return _attribute;
  }

  @Override
  public String classifyPosition(final Position position) {
    for (Trade trade : position.getTrades()) {
      final String value = trade.getAttributes().get(getAttribute());
      if (value != null) {
        return value;
      }
    }
    return getUnknownClassification();
  }

  public String getName() {
    return _attribute;
  }
}
