/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Collection;
import java.util.Comparator;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.legalentity.LegalEntity;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePositionComparator;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.cds.AbstractCreditDefaultSwapSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexDefinitionSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapSecurity;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstract aggregation function for CDS reference entity data. If used with
 * non-CDS securities, all items will be classified as "N/A".
 */
public class EntityNameAggregationFunction implements AggregationFunction<String> {

  /**
   * Classification indicating that this aggregation does not apply to the security.
   */
  private static final String NOT_APPLICABLE = "N/A";

  /**
   * The security source used for resolution of the CDS security, not null.
   */
  private final SecuritySource _securitySource;
  /**
   * The organization source, not null.
   */
  private final LegalEntitySource _legalEntitySource;

  /**
   * The name of this aggregation.
   */
  private static final String NAME = "Reference Entity Names";

  /**
   * Creates the aggregation function.
   *
   * @param legalEntitySource the organization source used for the finding the
   *  organization from the red code of the CDS security, not null
   * @param securitySource the security source used for resolution of the CDS security, not null
   */
  public EntityNameAggregationFunction(final LegalEntitySource legalEntitySource, final SecuritySource securitySource) {
    ArgumentChecker.notNull(legalEntitySource, "legalEntitySource");
    ArgumentChecker.notNull(securitySource, "securitySource");
    _securitySource = securitySource;
    _legalEntitySource = legalEntitySource;
  }

  //-------------------------------------------------------------------------
  @Override
  public Collection<String> getRequiredEntries() {
    return ImmutableList.of();
  }

  @Override
  public String classifyPosition(final Position position) {
    final Security security = resolveSecurity(position);
    if (security instanceof CreditDefaultSwapOptionSecurity) {
      final CreditDefaultSwapOptionSecurity cdsOption = (CreditDefaultSwapOptionSecurity) security;
      final ExternalId underlyingId = cdsOption.getUnderlyingId();
      final Security underlying = _securitySource.getSingle(underlyingId.toBundle());
      final String redCode = ((CreditDefaultSwapSecurity) underlying).getReferenceEntity().getValue();
      final LegalEntity legalEntity = _legalEntitySource.getSingle(ExternalId.of(ExternalSchemes.MARKIT_RED_CODE, redCode));
      return legalEntity.getName();

    } else if (security instanceof CreditDefaultSwapIndexSecurity) {
      final CreditDefaultSwapIndexSecurity cdsIndex = (CreditDefaultSwapIndexSecurity) security;
      final CreditDefaultSwapIndexDefinitionSecurity definition = (CreditDefaultSwapIndexDefinitionSecurity) _securitySource.getSingle(ExternalIdBundle.of(cdsIndex.getReferenceEntity()));
      return definition.getName();
    } else if (security instanceof CreditDefaultSwapSecurity) {
      final AbstractCreditDefaultSwapSecurity cds = (AbstractCreditDefaultSwapSecurity) security;
      final String redCode = cds.getReferenceEntity().getValue();
      final LegalEntity legalEntity = _legalEntitySource.getSingle(ExternalId.of(ExternalSchemes.MARKIT_RED_CODE, redCode));
      if (legalEntity != null) {
        return legalEntity.getName();
      } else {
        return redCode;
      }
    }

    return NOT_APPLICABLE;
  }

  /**
   * Gets the security source.
   * @return The security source
   */
  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  private Security resolveSecurity(final Position position) {
    final Security security = position.getSecurityLink().getTarget();
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
  public int compare(final String sector1, final String sector2) {
    return sector1.compareTo(sector2);
  }

}
