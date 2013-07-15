/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.orgs.impl;

import com.opengamma.master.impl.AbstractSearchIterator;
import com.opengamma.master.orgs.OrganisationDocument;
import com.opengamma.master.orgs.OrganisationMaster;
import com.opengamma.master.orgs.OrganisationSearchRequest;
import com.opengamma.master.orgs.OrganisationSearchResult;
import com.opengamma.util.ArgumentChecker;

import java.util.Iterator;

/**
 * An iterator that searches a organisation master as an iterator.
 * <p/>
 * Large systems may store a large amount of data in each master.
 * A simple search request that pulls back the entire database is unrealistic.
 * This remote iterator allows the database to be queried in a consistent way remotely.
 */
public class OrganisationSearchIterator extends AbstractSearchIterator<OrganisationDocument, OrganisationMaster, OrganisationSearchRequest> {

  /**
   * Creates an instance based on a request.
   * <p/>
   * The request will be altered during the iteration.
   *
   * @param master  the underlying master, not null
   * @param request the request object, not null
   * @return an iterable suitable for use in a for-each loop, not null
   */
  public static Iterable<OrganisationDocument> iterable(final OrganisationMaster master, final OrganisationSearchRequest request) {
    ArgumentChecker.notNull(master, "master");
    ArgumentChecker.notNull(request, "request");
    return new Iterable<OrganisationDocument>() {
      @Override
      public Iterator<OrganisationDocument> iterator() {
        return new OrganisationSearchIterator(master, request);
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
  public OrganisationSearchIterator(OrganisationMaster master, OrganisationSearchRequest request) {
    super(master, request);
  }

  //-------------------------------------------------------------------------
  @Override
  protected OrganisationSearchResult doSearch(OrganisationSearchRequest request) {
    return getMaster().search(request);
  }

}
