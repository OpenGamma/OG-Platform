/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.portfolio.impl;

import java.util.Iterator;

import com.opengamma.master.impl.AbstractSearchIterator;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * An iterator that searches a portfolio master as an iterator.
 * <p>
 * Large systems may store a large amount of data in each master.
 * A simple search request that pulls back the entire database is unrealistic.
 * This remote iterator allows the database to be queried in a consistent way remotely.
 */
public class PortfolioSearchIterator extends AbstractSearchIterator<PortfolioDocument, PortfolioMaster, PortfolioSearchRequest> {

  /**
   * Creates an instance based on a request.
   * <p>
   * The request will be altered during the iteration.
   * 
   * @param master  the underlying master, not null
   * @param request  the request object, not null
   * @return an iterable suitable for use in a for-each loop, not null
   */
  public static Iterable<PortfolioDocument> iterable(final PortfolioMaster master, final PortfolioSearchRequest request) {
    ArgumentChecker.notNull(master, "master");
    ArgumentChecker.notNull(request, "request");
    return new Iterable<PortfolioDocument>() {
      @Override
      public Iterator<PortfolioDocument> iterator() {
        return new PortfolioSearchIterator(master, request);
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
  public PortfolioSearchIterator(PortfolioMaster master, PortfolioSearchRequest request) {
    super(master, request);
  }

  //-------------------------------------------------------------------------
  @Override
  protected PortfolioSearchResult doSearch(PortfolioSearchRequest request) {
    return getMaster().search(request);
  }

}
