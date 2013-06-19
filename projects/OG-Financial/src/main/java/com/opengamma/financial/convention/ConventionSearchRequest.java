/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * A search request to retrieve convention information.
 */
public class ConventionSearchRequest {
  /** The identifiers of the request */
  private final ExternalIdBundle _identifiers;

  /**
   * @param identifier The identifier, not null
   */
  public ConventionSearchRequest(final ExternalId identifier) {
    ArgumentChecker.notNull(identifier, "identifier");
    _identifiers = ExternalIdBundle.of(identifier);
  }

  /**
   * @param identifiers The identifiers, not null
   */
  public ConventionSearchRequest(final ExternalIdBundle identifiers) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    _identifiers = identifiers;
  }

  /**
   * Gets the identifiers of this request.
   * @return The identifiers of this request
   */
  public ExternalIdBundle getIdentifiers() {
    return _identifiers;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _identifiers.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ConventionSearchRequest)) {
      return false;
    }
    final ConventionSearchRequest other = (ConventionSearchRequest) obj;
    return ObjectUtils.equals(_identifiers, other._identifiers);
  }

}
