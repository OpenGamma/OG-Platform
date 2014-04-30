/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.legalentity.impl;

import java.util.Iterator;

import com.opengamma.master.impl.AbstractSearchIterator;
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.legalentity.LegalEntitySearchRequest;
import com.opengamma.master.legalentity.LegalEntitySearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * An iterator that searches a legalentity master as an iterator.
 * <p/>
 * Large systems may store a large amount of data in each master.
 * A simple search request that pulls back the entire database is unrealistic.
 * This remote iterator allows the database to be queried in a consistent way remotely.
 */
public class LegalEntitySearchIterator extends AbstractSearchIterator<LegalEntityDocument, LegalEntityMaster, LegalEntitySearchRequest> {

  /**
   * Creates an instance based on a request.
   * <p/>
   * The request will be altered during the iteration.
   *
   * @param master  the underlying master, not null
   * @param request the request object, not null
   * @return an iterable suitable for use in a for-each loop, not null
   */
  public static Iterable<LegalEntityDocument> iterable(final LegalEntityMaster master, final LegalEntitySearchRequest request) {
    ArgumentChecker.notNull(master, "master");
    ArgumentChecker.notNull(request, "request");
    return new Iterable<LegalEntityDocument>() {
      @Override
      public Iterator<LegalEntityDocument> iterator() {
        return new com.opengamma.master.legalentity.impl.LegalEntitySearchIterator(master, request);
      }
    };
  }

  /**
   * Creates an instance based on a request.
   * <p/>
   * The request will be altered during the iteration.
   *
   * @param master  the underlying master, not null
   * @param request the request object, not null
   */
  public LegalEntitySearchIterator(LegalEntityMaster master, LegalEntitySearchRequest request) {
    super(master, request);
  }

  //-------------------------------------------------------------------------
  @Override
  protected LegalEntitySearchResult doSearch(LegalEntitySearchRequest request) {
    return getMaster().search(request);
  }

}
