/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimplePositionComparator;
import com.opengamma.util.ArgumentChecker;

/**
 * Function to classify positions by attribute value on the underlying trades.
 */
public class PositionAttributeAggregationFunction implements AggregationFunction<String> {

  private static final String UNKNOWN = "Unknown";

  private final String _attribute;
  private final String _unknownClassification;
  private final Comparator<Position> _comparator = new SimplePositionComparator();

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

  /**
   * Returns the position's value for this function's attribute if it has one.  If not its trades are searched and
   * the value is returned from the first one with a matching attribute.  If neither the position nor any of its
   * trades have a matching attribute then {@link #UNKNOWN} is returned.
   * @param position The position to classify
   * @return The attribute value from the position or one of its trades or {@link #UNKNOWN} if there are no
   * matching attributes
   */
  @Override
  public String classifyPosition(final Position position) {
    String positionAttribute = position.getAttributes().get(getAttribute());
    if (positionAttribute != null) {
      return positionAttribute;
    }
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

  @Override
  public Collection<String> getRequiredEntries() {
    return Collections.emptyList();
  }

  @Override
  public int compare(String o1, String o2) {
    return o1.compareTo(o2);
  }

  @Override
  public Comparator<Position> getPositionComparator() {
    return _comparator;
  }
}
