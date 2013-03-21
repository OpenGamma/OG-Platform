/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma
 group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Collection;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePositionComparator;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.cds.CreditDefaultSwapSecurity;
import com.opengamma.id.ExternalId;

/**
 * Simple aggregator function to allow positions to be aggregated by RED code. This is
 * generally only applicable to CDS securities, and if applied to securities with no
 * RED code, the result of {@link #classifyPosition(Position)} will be "N/A".
 */
public class ObligorRedCodeAggregationFunction implements AggregationFunction<String> {

  private static final Logger s_logger = LoggerFactory.getLogger(ObligorRedCodeAggregationFunction.class);
  private static final Comparator<Position> COMPARATOR = new SimplePositionComparator();
  private static final String NAME = "RED Codes";
  private static final String NOT_APPLICABLE = "N/A";

  private final SecuritySource _securitySource;

  public ObligorRedCodeAggregationFunction(SecuritySource securitySource) {
    _securitySource = securitySource;
  }

  /**
   * Classify the position using the RED code of the reference entity contained in the
   * CDS security (if applicable). If the security is not a CDS the string "N/A" is returned.
   *
   * @param position the position to classify
   * @return the RED code if the security associated with the position has one, "N/A" otherwise
   */
  @Override
  public String classifyPosition(Position position) {

    Security security = resolveSecurity(position);

    if (security instanceof CreditDefaultSwapSecurity) {
      CreditDefaultSwapSecurity cds = (CreditDefaultSwapSecurity) security;
      ExternalId refEntityId = cds.getReferenceEntity();
      if (refEntityId.isScheme(ExternalSchemes.MARKIT_RED_CODE)) {
        return refEntityId.getValue();
      }
    }

    return NOT_APPLICABLE;
  }

  private Security resolveSecurity(Position position) {

    Security security = position.getSecurityLink().getTarget();
    return security != null ? security : position.getSecurityLink().resolveQuiet(_securitySource);
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
