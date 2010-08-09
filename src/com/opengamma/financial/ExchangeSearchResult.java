/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Result from searching for an Exchange
 * 
 * This potentially can contain multiple instances
 */
public class ExchangeSearchResult {
  
  private List<ExchangeDocument> _documents = new ArrayList<ExchangeDocument>();
  /**
   * Creates an instance
   */
  public ExchangeSearchResult() {
  }
  
  /**
   * Creates an instance using a collection of documents ready formed.
   * @param results a collection of exchange documents as the initial set of results
   */
  public ExchangeSearchResult(Collection<ExchangeDocument> results) {
    addExchanges(results);
  }
  
  /**
   * Gets the returned exchanges from within their documents.
   * @return the exchanges, not null
   */
  public List<Exchange> getExchanges() {
    List<Exchange> result = new ArrayList<Exchange>();
    if (_documents != null) {
      for (ExchangeDocument doc : _documents) {
        result.add(doc.getExchange());
      }
    }
    return result;
  }
  
  /**
   * Gets the first exchange in the result set.  This assumes that you expect a variable number of results.
   * @return the first matching exchange, or null if none
   */
  public Exchange getFirstExchange() {
    if (_documents.size() > 0) {
      return _documents.get(0).getExchange();
    } else {
      return null;
    }
  }
  
  /**
   * Gets the single result expected from a query.  This throws an exception if more than 1 result is actually
   * available (which would imply an incorrect assumption about uniqueness in the calling code).
   * @return the matching exchange, or null if none
   */
  public Exchange getSingleExchange() {
    if (_documents.size() > 1) {
      throw new OpenGammaRuntimeException("Expecting zero or single resulting match, and was " + _documents.size());
    } else {
      return getFirstExchange();
    }
  }
  
  /**
   * Add a result document to this list of results
   * @param exchangeDoc the result document
   */
  public void addExchange(ExchangeDocument exchangeDoc) {
    _documents.add(exchangeDoc);
  }
  
  /**
   * Add a collection of documents to this list of results
   * @param exchangeDocs the collection of result documents
   */
  public void addExchanges(Collection<ExchangeDocument> exchangeDocs) {
    _documents.addAll(exchangeDocs);
  }
}
