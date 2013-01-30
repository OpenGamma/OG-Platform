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

/**
 * Abstract aggregation function for bucketing equities and equity options by GICS code of the underlying
 */
public class CounterpartyAggregationFunction implements AggregationFunction<String> {

  private static final String UNKNOWN = "N/A";
  private final Comparator<Position> _comparator = new SimplePositionComparator();

  public CounterpartyAggregationFunction() {
  }


  @Override
  public String classifyPosition(Position position) {
    if (position.getTrades().size() == 0) {
      return UNKNOWN;
    } else {
      Trade trade = position.getTrades().iterator().next();
      if (trade.getCounterparty() != null) {
        return trade.getCounterparty().getExternalId().getValue();
      } else {
        return UNKNOWN;
      }
    }
  }

  @Override
  public String getName() {
    return "Counterparty";
  }

  @Override
  public Collection<String> getRequiredEntries() {
    return Collections.emptyList();
  }

  @Override
  public int compare(String o1, String o2) {
    if (o1.equals(UNKNOWN)) {
      if (o2.equals(UNKNOWN)) {
        return 0;
      }
      return 1;
    } else if (o2.equals(UNKNOWN)) {
      return -1;
    }
    return o1.compareTo(o2);
  }

  @Override
  public Comparator<Position> getPositionComparator() {
    return _comparator;
  }

}
