/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.Instant;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * A historic search request to retrieve convention information.
 */
public class ConventionSearchHistoricRequest {
  /** The version of the convention */
  private final Instant _version;
  /** The correction time */
  private final Instant _correction;
  /** The identifiers of the convention */
  private final ExternalIdBundle _identifiers;

  /**
   * @param version The version, not null
   * @param correction The correction, not null
   * @param identifier The convention identifier, not null
   */
  public ConventionSearchHistoricRequest(final Instant version, final Instant correction, final ExternalId identifier) {
    ArgumentChecker.notNull(version, "version");
    ArgumentChecker.notNull(correction, "correction");
    ArgumentChecker.notNull(identifier, "identifier");
    _version = version;
    _correction = correction;
    _identifiers = ExternalIdBundle.of(identifier);
  }

  /**
   * @param version The version, not null
   * @param correction The correction, not null
   * @param identifiers The convention identifiers, not null
   */
  public ConventionSearchHistoricRequest(final Instant version, final Instant correction, final ExternalIdBundle identifiers) {
    ArgumentChecker.notNull(version, "version");
    ArgumentChecker.notNull(correction, "correction");
    ArgumentChecker.notNull(identifiers, "identifiers");
    _version = version;
    _correction = correction;
    _identifiers = identifiers;
  }

  /**
   * Gets the version of the convention.
   * @return The version
   */
  public Instant getVersion() {
    return _version;
  }

  /**
   * Gets the correction time of the convention.
   * @return The correction
   */
  public Instant getCorrection() {
    return _correction;
  }

  /**
   * Gets the identifiers of the convention.
   * @return The identifiers
   */
  public ExternalIdBundle getIdentifiers() {
    return _identifiers;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _correction.hashCode();
    result = prime * result + _identifiers.hashCode();
    result = prime * result + _version.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ConventionSearchHistoricRequest)) {
      return false;
    }
    final ConventionSearchHistoricRequest other = (ConventionSearchHistoricRequest) obj;
    return ObjectUtils.equals(_correction, other._correction) &&
        ObjectUtils.equals(_version, other._version) &&
        ObjectUtils.equals(_identifiers, other._identifiers);
  }

}
