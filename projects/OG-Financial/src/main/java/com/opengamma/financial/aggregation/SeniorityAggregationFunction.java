/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Collection;
import java.util.Comparator;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePositionComparator;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.cds.CreditDefaultSwapSecurity;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstract aggregation function for CDS reference entity data. If used with
 * non-CDS securities, all items will be classified as "N/A".
 */
public class SeniorityAggregationFunction implements AggregationFunction<String> {

  /**
   * Function name.
   */
  private static final String NAME = "Seniority";
  /**
   * Classification indicating that this aggregation does not apply to the security.
   */
  private static final String NOT_APPLICABLE = "N/A";

  /**
   * The security source used for resolution of the CDS security, not null.
   */
  private final SecuritySource _securitySource;

  /**
   * Creates the aggregation function.
   *
   * @param securitySource the security source used for resolution of the CDS security, not null
   */
  public SeniorityAggregationFunction(SecuritySource securitySource) {
    ArgumentChecker.notNull(securitySource, "securitySource");
    _securitySource = securitySource;
  }

  //-------------------------------------------------------------------------
  @Override
  public Collection<String> getRequiredEntries() {
    return ImmutableList.of();
  }

  @Override
  public String classifyPosition(Position position) {
    Security security = resolveSecurity(position);
    if (security instanceof CreditDefaultSwapOptionSecurity) {
      CreditDefaultSwapOptionSecurity cdsOption = (CreditDefaultSwapOptionSecurity) security;
      ExternalId underlyingId = cdsOption.getUnderlyingId();
      Security underlying = _securitySource.getSingle(underlyingId.toBundle());
      return  ((CreditDefaultSwapSecurity) underlying).getDebtSeniority().toString();
    } else if (security instanceof CreditDefaultSwapSecurity) {
      CreditDefaultSwapSecurity cds = (CreditDefaultSwapSecurity) security;
      return cds.getDebtSeniority().toString();
    }
    return NOT_APPLICABLE;
  }

  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

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
    return NAME;
  }

  @Override
  public int compare(String sector1, String sector2) {
    return sector1.compareTo(sector2);
  }

}
