/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma
 group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Collection;
import java.util.Comparator;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePositionComparator;
import com.opengamma.id.ExternalId;

/**
 * Simple aggregator function to allow positions to be aggregated by RED code. This is
 * generally only applicable to CDS securities, and if applied to securities with no
 * RED code, the result of {@link #classifyPosition(Position)} will be "N/A".
 */
public class ObligorRedCodeAggregatorFunction implements AggregationFunction<String> {

  private static final Comparator<Position> COMPARATOR = new SimplePositionComparator();
  private static final String NAME = "RED Codes";
  private static final String NOT_APPLICABLE = "N/A";

  /**
   * Classify the position using the RED code contained in the security id (if applicable).
   * If the security id does contain a RED code then it is returned, else the string "N/A" is.
   *
   * @param position the position to classify
   * @return the RED code if the security associated with the position has one, "N/A" otherwise
   */
  @Override
  public String classifyPosition(Position position) {

    ExternalId redCode = position.getSecurityLink().getExternalId().getExternalId(ExternalSchemes.MARKIT_RED_CODE);
    return redCode == null ? NOT_APPLICABLE : redCode.getValue();
  }

  @Override
  public Collection<String> getRequiredEntries() {
    return ImmutableList.of();
  }

  @Override
  public Comparator<Position> getPositionComparator() {
    return COMPARATOR;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public int compare(String o1, String o2) {
    return o1.compareTo(o2);
  }
}
