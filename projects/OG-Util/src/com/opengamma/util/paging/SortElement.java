/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.paging;

import java.util.Comparator;

import org.joda.beans.Bean;
import org.joda.beans.BeanQuery;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.query.ChainedBeanQuery;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * Simple immutable representation of an element in a sort order.
 * <p>
 * This describes how to sort and whether it is ascending or descending.
 * <p>
 * The use of a {@link BeanQuery} links this method to the bean framework.
 * See {@link MetaProperty} and {@link ChainedBeanQuery} for two implementations.
 * <p>
 * This class is immutable and thread-safe.
 */
@PublicAPI
public final class SortElement {

  /**
   * The query defining how to sort.
   */
  private final BeanQuery<?> _query;
  /**
   * True for ascending, false for descending.
   */
  private final boolean _ascending;
  /**
   * The effective comparator.
   */
  private final Comparator<Bean> _comparator;

  /**
   * Obtains an instance based on a bean query and direction.
   * 
   * @param query  the query that describes how to sort, not null
   * @param ascending  true for ascending, false for descending
   * @return the sort order, not null
   */
  public static SortElement of(BeanQuery<?> query, boolean ascending) {
    return new SortElement(query, ascending);
  }

  /**
   * Obtains an instance based on a bean query sorted in ascending order.
   * 
   * @param query  the query that describes how to sort, not null
   * @return the sort order, not null
   */
  public static SortElement ofAscending(BeanQuery<?> query) {
    return of(query, true);
  }

  /**
   * Obtains an instance based on a bean query sorted in descending order.
   * 
   * @param query  the query that describes how to sort, not null
   * @return the sort order, not null
   */
  public static SortElement ofDescending(BeanQuery<?> query) {
    return of(query, false);
  }

  /**
   * Creates an instance.
   * 
   * @param query  the query that describes the property to sort on, not null
   * @param ascending  true for ascending, false for descending
   */
  private SortElement(BeanQuery<?> query, boolean ascending) {
    ArgumentChecker.notNull(query, "query");
    _query = query;
    _ascending = ascending;
    _comparator = JodaBeanUtils.comparator(getBeanQuery(), isAscending());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the bean query used to describe and implement the sort.
   * 
   * @return the bean query, not null
   */
  public BeanQuery<?> getBeanQuery() {
    return _query;
  }

  /**
   * Gets whether the sort is to be ascending.
   * 
   * @return true for ascending, false for descending
   */
  public boolean isAscending() {
    return _ascending;
  }

  /**
   * Gets whether the sort is to be descending.
   * 
   * @return true for descending, false for ascending
   */
  public boolean isDescending() {
    return !isAscending();
  }

  /**
   * Gets the effective comparator.
   * 
   * @return the comparator, not null
   */
  public Comparator<Bean> getComparator() {
    return _comparator;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    String ascDesc = (isAscending() ? "ASC" : "DESC");
    return "[" + getBeanQuery() + " " + ascDesc + "]";
  }

}
