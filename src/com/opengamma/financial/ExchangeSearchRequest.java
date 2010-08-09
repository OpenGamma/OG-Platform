/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;

/**
 * 
 */
public class ExchangeSearchRequest {
  private IdentifierBundle _identifiers;
  
  /**
   * Construct an exchange search request using a single identifier
   * @param identifier the identifier to look up
   */
  public ExchangeSearchRequest(Identifier identifier) {
    _identifiers = IdentifierBundle.of(identifier);
  }
  /**
   * Construct an exchange search request using a bundle of identifiers
   * @param identifiers the bundle of identifiers to look up
   */
  public ExchangeSearchRequest(IdentifierBundle identifiers) {
    _identifiers = identifiers;
  }
  
  public IdentifierBundle getIdentifiers() {
    return _identifiers;
  }
}
