/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.util.Collection;

import com.opengamma.id.Identifier;
import com.opengamma.id.Identifiable;
import com.opengamma.id.IdentificationScheme;

/**
 * 
 *
 * @author kirk
 */
public interface Security extends Identifiable {
  
  public static final IdentificationScheme SECURITY_IDENTITY_KEY_DOMAIN = new IdentificationScheme("SecurityIdentityKey"); 

  /**
   * Obtain all the security identifiers which are part of this
   * {@code Security}'s description.
   * 
   * @return All identifiers for this security.
   */
  Collection<Identifier> getIdentifiers();
  
  /**
   * Returns a displayable name for the {@code Security} that is more user friently than the
   * collection of identifiers.
   * 
   * @return a displayable name
   */
  String getDisplayName ();
  
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
