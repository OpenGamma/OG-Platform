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
public class DefaultExchangeSource implements ExchangeSource {

  private ExchangeRepository _exchangeMaster;
  
  public DefaultExchangeSource(ExchangeRepository exchangeMaster) {
    _exchangeMaster = exchangeMaster;
  }
  
  @Override
  public Exchange getSingleExchange(Identifier identifier) {
    ExchangeSearchRequest searchRequest = new ExchangeSearchRequest(identifier);
    return _exchangeMaster.searchExchange(searchRequest).getSingleExchange();
  }
  
  @Override
  public Exchange getSingleExchange(IdentifierBundle identifiers) {
    ExchangeSearchRequest searchRequest = new ExchangeSearchRequest(identifiers);
    return _exchangeMaster.searchExchange(searchRequest).getSingleExchange();
  }

}
