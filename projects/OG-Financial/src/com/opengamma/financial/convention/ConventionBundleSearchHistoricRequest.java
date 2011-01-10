/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import javax.time.Instant;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;

/**
 * A historic search request to retrieve convention set information.
 */
public class ConventionBundleSearchHistoricRequest {
  private Instant _version;
  private Instant _correction;
  private IdentifierBundle _identifiers;
  
  public ConventionBundleSearchHistoricRequest(Instant version, Instant correction, Identifier identifier) {
    _version = version;
    _correction = correction;
    _identifiers = IdentifierBundle.of(identifier);
  }
  
  public ConventionBundleSearchHistoricRequest(Instant version, Instant correction, IdentifierBundle identifiers) {
    _version = version;
    _correction = correction;
    _identifiers = identifiers;
  }

  public Instant getVersion() {
    return _version;
  }
  
  public Instant getCorrection() {
    return _correction;
  }
  
  public IdentifierBundle getIdentifiers() {
    return _identifiers;
  }
}
