/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import java.util.Iterator;

import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.impl.AbstractSearchIterator;
import com.opengamma.util.ArgumentChecker;

/**
 * An iterator that searches an config master as an iterator.
 * <p>
 * Large systems may store a large amount of data in each master.
 * A simple search request that pulls back the entire database is unrealistic.
 * This remote iterator allows the database to be queried in a consistent way remotely.
 * 
 * @param <T>  the type of the configuration item
 */
public class ConfigSearchIterator<T> extends AbstractSearchIterator<ConfigDocument, ConfigMaster, ConfigSearchRequest<T>> {

  /**
   * Creates an instance based on a request.
   * <p>
   * The request will be altered during the iteration.
   * 
   * @param <R>  the type of the configuration item
   * @param master  the underlying master, not null
   * @param request  the request object, not null
   * @return an iterable suitable for use in a for-each loop, not null
   */
  public static <R> Iterable<ConfigDocument> iterable(final ConfigMaster master, final ConfigSearchRequest<R> request) {
    ArgumentChecker.notNull(master, "master");
    ArgumentChecker.notNull(request, "request");
    return new Iterable<ConfigDocument>() {
      @Override
      public Iterator<ConfigDocument> iterator() {
        return new ConfigSearchIterator<R>(master, request);
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
  public ConfigSearchIterator(ConfigMaster master, ConfigSearchRequest<T> request) {
    super(master, request);
  }

  //-------------------------------------------------------------------------
  @Override
  protected ConfigSearchResult<T> doSearch(ConfigSearchRequest<T> request) {
    return getMaster().search(request);
  }

}
