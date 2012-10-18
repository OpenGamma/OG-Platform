/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import java.util.Iterator;

import com.opengamma.master.impl.AbstractSearchIterator;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * An iterator that searches a security master as an iterator.
 * <p>
 * Large systems may store a large amount of data in each master.
 * A simple search request that pulls back the entire database is unrealistic.
 * This remote iterator allows the database to be queried in a consistent way remotely.
 */
public class SecuritySearchIterator extends AbstractSearchIterator<SecurityDocument, SecurityMaster, SecuritySearchRequest> {

  /**
   * Creates an instance based on a request.
   * <p>
   * The request will be altered during the iteration.
   * 
   * @param master  the underlying master, not null
   * @param request  the request object, not null
   * @return an iterable suitable for use in a for-each loop, not null
   */
  public static Iterable<SecurityDocument> iterable(final SecurityMaster master, final SecuritySearchRequest request) {
    ArgumentChecker.notNull(master, "master");
    ArgumentChecker.notNull(request, "request");
    return new Iterable<SecurityDocument>() {
      @Override
      public Iterator<SecurityDocument> iterator() {
        return new SecuritySearchIterator(master, request);
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
  public SecuritySearchIterator(SecurityMaster master, SecuritySearchRequest request) {
    super(master, request);
  }

  //-------------------------------------------------------------------------
  @Override
  protected SecuritySearchResult doSearch(SecuritySearchRequest request) {
    return getMaster().search(request);
  }

}
