/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * A search request to retrieve reference rate information.
 */
public class ConventionSearchRequest {
  private ExternalIdBundle _identifiers;
  
  public ConventionSearchRequest(ExternalId identifier) {
    _identifiers = ExternalIdBundle.of(identifier);
  }
  
  public ConventionSearchRequest(ExternalIdBundle identifiers) {
    _identifiers = identifiers;
  }
  
  public ExternalIdBundle getIdentifiers() {
    return _identifiers;
  }
}
