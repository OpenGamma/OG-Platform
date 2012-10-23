/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import java.util.Iterator;

import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.master.impl.AbstractSearchIterator;
import com.opengamma.util.ArgumentChecker;

/**
 * An iterator that searches a holiday master as an iterator.
 * <p>
 * Large systems may store a large amount of data in each master.
 * A simple search request that pulls back the entire database is unrealistic.
 * This remote iterator allows the database to be queried in a consistent way remotely.
 */
public class HolidaySearchIterator extends AbstractSearchIterator<HolidayDocument, HolidayMaster, HolidaySearchRequest> {

  /**
   * Creates an instance based on a request.
   * <p>
   * The request will be altered during the iteration.
   * 
   * @param master  the underlying master, not null
   * @param request  the request object, not null
   * @return an iterable suitable for use in a for-each loop, not null
   */
  public static Iterable<HolidayDocument> iterable(final HolidayMaster master, final HolidaySearchRequest request) {
    ArgumentChecker.notNull(master, "master");
    ArgumentChecker.notNull(request, "request");
    return new Iterable<HolidayDocument>() {
      @Override
      public Iterator<HolidayDocument> iterator() {
        return new HolidaySearchIterator(master, request);
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
  public HolidaySearchIterator(HolidayMaster master, HolidaySearchRequest request) {
    super(master, request);
  }

  //-------------------------------------------------------------------------
  @Override
  protected HolidaySearchResult doSearch(HolidaySearchRequest request) {
    return getMaster().search(request);
  }

}
