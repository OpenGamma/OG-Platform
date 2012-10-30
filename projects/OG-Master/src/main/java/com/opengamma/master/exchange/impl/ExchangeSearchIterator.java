/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange.impl;

import java.util.Iterator;

import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.master.impl.AbstractSearchIterator;
import com.opengamma.util.ArgumentChecker;

/**
 * An iterator that searches an exchange master as an iterator.
 * <p>
 * Large systems may store a large amount of data in each master.
 * A simple search request that pulls back the entire database is unrealistic.
 * This remote iterator allows the database to be queried in a consistent way remotely.
 */
public class ExchangeSearchIterator extends AbstractSearchIterator<ExchangeDocument, ExchangeMaster, ExchangeSearchRequest> {

  /**
   * Creates an instance based on a request.
   * <p>
   * The request will be altered during the iteration.
   * 
   * @param master  the underlying master, not null
   * @param request  the request object, not null
   * @return an iterable suitable for use in a for-each loop, not null
   */
  public static Iterable<ExchangeDocument> iterable(final ExchangeMaster master, final ExchangeSearchRequest request) {
    ArgumentChecker.notNull(master, "master");
    ArgumentChecker.notNull(request, "request");
    return new Iterable<ExchangeDocument>() {
      @Override
      public Iterator<ExchangeDocument> iterator() {
        return new ExchangeSearchIterator(master, request);
      }
    };
  }

  /**
   * Creates an instance based on a request.
   * <p>
   * The request will be altered during the iteration.
   * 
   * @param master  the underlying master, not null
   * @param request  the request object, not null
   */
  public ExchangeSearchIterator(ExchangeMaster master, ExchangeSearchRequest request) {
    super(master, request);
  }

  //-------------------------------------------------------------------------
  @Override
  protected ExchangeSearchResult doSearch(ExchangeSearchRequest request) {
    return getMaster().search(request);
  }

}
