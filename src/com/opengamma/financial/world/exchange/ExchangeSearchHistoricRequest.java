/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.exchange;

import javax.time.Instant;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;

/**
 * 
 */
public class ExchangeSearchHistoricRequest {
  private IdentifierBundle _identifiers;
  private Instant _version;
  private Instant _correction;
  
  /**
   * Construct an exchange search request using a single identifier
   * @param version the instant of the point in history you're looking at
   * @param correction the date you're considering history from - e.g. recent corrections may be omitted if later than correction date
   * @param identifier the identifier to look up
   */
  public ExchangeSearchHistoricRequest(Instant version, Instant correction, Identifier identifier) {
    _version = version;
    _correction = correction;
    _identifiers = IdentifierBundle.of(identifier);
  }
  /**
   * Construct an exchange search request using a bundle of identifiers
   * @param version the instant of the point in history you're looking at
   * @param correction the date you're considering history from - e.g. recent corrections may be omitted if later than correction date
   * @param identifiers the bundle of identifiers to look up
   */
  public ExchangeSearchHistoricRequest(Instant version, Instant correction, IdentifierBundle identifiers) {
    _version = version;
    _correction = correction;
    _identifiers = identifiers;
  }
  
  public IdentifierBundle getIdentifiers() {
    return _identifiers;
  }
  
  public Instant getVersion() {
    return _version;
  }
  
  public Instant getCorrection() {
    return _correction;
  }
}
