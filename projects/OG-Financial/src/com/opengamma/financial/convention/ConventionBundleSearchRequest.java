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
public class ConventionBundleSearchRequest {
  private ExternalIdBundle _identifiers;
  
  public ConventionBundleSearchRequest(ExternalId identifier) {
    _identifiers = ExternalIdBundle.of(identifier);
  }
  
  public ConventionBundleSearchRequest(ExternalIdBundle identifiers) {
    _identifiers = identifiers;
  }
  
  public ExternalIdBundle getIdentifiers() {
    return _identifiers;
  }
}
