/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Metadata document object to contain an exchange
 */
public class ExchangeDocument {
  private String _name;
  private Exchange _exchange;
  private IdentifierBundle _identifiers;
  private UniqueIdentifier _uniqueIdentifier;
  // TODO: deal with versioning.
  
  public ExchangeDocument(Exchange exchange) {
    _exchange = exchange;
    _name = exchange.getName();
    _identifiers = exchange.getIdentifiers();
    _uniqueIdentifier = exchange.getUniqueIdentifier();
  }
  
  public String getName() {
    return _name;
  }
  
  public Exchange getExchange() {
    return _exchange;
  }
  
  public IdentifierBundle getIdentifiers() {
    return _identifiers;
  }
  
  public UniqueIdentifier getUniqueIdentifier() {
    return _uniqueIdentifier;
  }
}
