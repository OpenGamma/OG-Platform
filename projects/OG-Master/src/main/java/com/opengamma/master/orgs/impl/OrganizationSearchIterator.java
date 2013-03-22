/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.orgs.impl;

import com.opengamma.master.impl.AbstractSearchIterator;
import com.opengamma.master.orgs.OrganizationDocument;
import com.opengamma.master.orgs.OrganizationMaster;
import com.opengamma.master.orgs.OrganizationSearchRequest;
import com.opengamma.master.orgs.OrganizationSearchResult;
import com.opengamma.util.ArgumentChecker;

import java.util.Iterator;

/**
 * An iterator that searches a organization master as an iterator.
 * <p/>
 * Large systems may store a large amount of data in each master.
 * A simple search request that pulls back the entire database is unrealistic.
 * This remote iterator allows the database to be queried in a consistent way remotely.
 */
public class OrganizationSearchIterator extends AbstractSearchIterator<OrganizationDocument, OrganizationMaster, OrganizationSearchRequest> {

  /**
   * Creates an instance based on a request.
   * <p/>
   * The request will be altered during the iteration.
   *
   * @param master  the underlying master, not null
   * @param request the request object, not null
   * @return an iterable suitable for use in a for-each loop, not null
   */
  public static Iterable<OrganizationDocument> iterable(final OrganizationMaster master, final OrganizationSearchRequest request) {
    ArgumentChecker.notNull(master, "master");
    ArgumentChecker.notNull(request, "request");
    return new Iterable<OrganizationDocument>() {
      @Override
      public Iterator<OrganizationDocument> iterator() {
        return new OrganizationSearchIterator(master, request);
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
  public OrganizationSearchIterator(OrganizationMaster master, OrganizationSearchRequest request) {
    super(master, request);
  }

  //-------------------------------------------------------------------------
  @Override
  protected OrganizationSearchResult doSearch(OrganizationSearchRequest request) {
    return getMaster().search(request);
  }

}
