/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;

/**
 * A search request to retrieve reference rate information.
 */
public class ConventionBundleSearchRequest {
  private IdentifierBundle _identifiers;
  
  public ConventionBundleSearchRequest(Identifier identifier) {
    _identifiers = IdentifierBundle.of(identifier);
  }
  
  public ConventionBundleSearchRequest(IdentifierBundle identifiers) {
    _identifiers = identifiers;
  }
  
  public IdentifierBundle getIdentifiers() {
    return _identifiers;
  }
}
