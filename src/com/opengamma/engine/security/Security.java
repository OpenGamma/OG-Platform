/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.util.Collection;

import com.opengamma.id.DomainSpecificIdentifier;

/**
 * 
 *
 * @author kirk
 */
public interface Security {

  /**
   * Obtain all the security identifiers which are part of this
   * {@code Security}'s description.
   * 
   * @return All identifiers for this security.
   */
  Collection<DomainSpecificIdentifier> getIdentifiers();
  
  /**
   * Obtain a SecurityKey that uniquely identifies the security in question
   */
  String getIdentityKey();
  
  /**
   * Obtain the dedicated market data definition for this security.
   * This may combine a set of keys (for example, a Bloomberg Ticker and a
   * Reuters RIC) in one analytic value definition.
   * In the case where there is no specific market data definition for this security
   * (for example, in the case of an instrument which isn't quoted or traded), this
   * method should return {@code null}.
   * 
   * @return The market data definition for this {@code Security}, or {@code null}.
   */
  // TODO kirk 2009-12-31 -- Come up with new system for this.
  //AnalyticValueDefinition<FudgeMsg> getMarketDataDefinition();
  
  /**
   * Obtain the text-based type of this Security.
   * @return The text-based type of this security.
   */
  String getSecurityType();
}
