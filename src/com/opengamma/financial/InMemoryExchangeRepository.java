/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierBundleMapper;
import com.opengamma.id.UniqueIdentifier;

/**
 * In-memory implementation of ExchangeRepository.  Doesn't currently support versioning.
 */
public class InMemoryExchangeRepository implements ExchangeRepository {
  /**
   * The unique id scheme name used.  Primarily exposed for unit testing. 
   */
  public static final String EXCHANGE_SCHEME = "EXCHANGE_SCHEME";
  
  private IdentifierBundleMapper<Exchange> _idMapper = new IdentifierBundleMapper<Exchange>(EXCHANGE_SCHEME);
  
  public InMemoryExchangeRepository() {
  }
  
  private Set<ExchangeDocument> wrapExchangesWithDocuments(Collection<Exchange> exchanges) {
    Set<ExchangeDocument> results = new HashSet<ExchangeDocument>();
    for (Exchange exchange : exchanges) {
      results.add(new ExchangeDocument(exchange));
    }
    return results;
  }

  @Override
  public ExchangeDocument getExchange(UniqueIdentifier uniqueIdentifier) {
    return new ExchangeDocument(_idMapper.get(uniqueIdentifier));
  }
    
  @Override
  public ExchangeSearchResult searchExchange(ExchangeSearchRequest search) {
    Collection<Exchange> results = _idMapper.get(search.getIdentifiers());
    return new ExchangeSearchResult(wrapExchangesWithDocuments(results));
  }
  
  @Override
  public ExchangeSearchResult searchHistoricExchange(ExchangeSearchHistoricRequest search) {
    Collection<Exchange> results = _idMapper.get(search.getIdentifiers());
    return new ExchangeSearchResult(wrapExchangesWithDocuments(results));
  }
  
  public ExchangeDocument addExchange(IdentifierBundle identifiers, String name, Identifier regionIdentifier) {
    Exchange exchange = new Exchange(identifiers, name, regionIdentifier);
    UniqueIdentifier uid = _idMapper.add(identifiers, exchange);
    exchange = _idMapper.get(uid);
    exchange.setUniqueIdentifier(uid);
    if (exchange == null) { // probably a bit over the top...
      throw new OpenGammaRuntimeException("This should be impossible: problem adding exchange, add returned uid of " + uid + 
                                          " but get(uid) returned null");
    }
    return new ExchangeDocument(exchange);
  }
}
