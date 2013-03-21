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
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.cds.CreditDefaultSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

public abstract class AbstractCdsObligorAggregationFunction implements AggregationFunction<String> {

  private static final String NOT_APPLICABLE = "N/A";
  private final SecuritySource _securitySource;
  private final String _name;

  public AbstractCdsObligorAggregationFunction(SecuritySource securitySource, String name) {
    ArgumentChecker.notNull(securitySource, "securitySource");
    _securitySource = securitySource;
    _name = name;
  }

  @Override
  public Collection<String> getRequiredEntries() {
    return ImmutableList.of();
  }

  @Override
  public String classifyPosition(Position position) {
    Security security = resolveSecurity(position);

    if (security instanceof CreditDefaultSwapSecurity) {
      CreditDefaultSwapSecurity cds = (CreditDefaultSwapSecurity) security;
      ExternalId refEntityId = cds.getReferenceEntity();
      if (refEntityId.isScheme(ExternalSchemes.MARKIT_RED_CODE)) {

        return extractDataUsingRedCode(refEntityId.getValue());
      }
    }

    return NOT_APPLICABLE;
  }

  protected abstract String extractDataUsingRedCode(String redCode);

  private Security resolveSecurity(Position position) {

    Security security = position.getSecurityLink().getTarget();
    return security != null ? security : position.getSecurityLink().resolveQuiet(_securitySource);
  }

  @Override
  public Comparator<Position> getPositionComparator() {
    return new SimplePositionComparator();
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public int compare(String sector1, String sector2) {
    return sector1.compareTo(sector2);
  }
}