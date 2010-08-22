/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Interface for retrieving information about exchanges.
 * @author jim
 */
public interface ExchangeRepository {
  /**
   * Identification scheme for the MIC exchange code ISO standard.
   */
  IdentificationScheme ISO_MIC = new IdentificationScheme("ISO_MIC");
  /**
   * Identification scheme for the Copp-clark holiday data provider's 'name' field.
   */
  IdentificationScheme COPP_CLARK_NAME = new IdentificationScheme("COPP_CLARK_NAME");
  /**
   * Identification scheme for the Copp-clark holiday data provider's 'centre id' field.
   */
  IdentificationScheme COPP_CLARK_CENTER_ID = new IdentificationScheme("COPP_CLARK_CENTER_ID");
  
  ExchangeDocument getExchange(UniqueIdentifier identifier);
  ExchangeSearchResult searchExchange(ExchangeSearchRequest search);
  ExchangeSearchResult searchHistoricExchange(ExchangeSearchHistoricRequest search);
  ExchangeDocument addExchange(IdentifierBundle identifiers, String name, Identifier regionIdentifier);
}
