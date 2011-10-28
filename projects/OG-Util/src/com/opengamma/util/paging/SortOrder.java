/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.paging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections.ComparatorUtils;
import org.joda.beans.Bean;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * Simple immutable representation of a sort order.
 * <p>
 * In order to apply paging to a set of results the data must be sorted.
 * This class is intended to be easy to convert to SQL ORDER BY.
 * <p>
 * This class is immutable and thread-safe.
 */
@PublicAPI
public final class SortOrder {

  /**
   * Singleton constant to request no sort order (no sort order).
   */
  public static final SortOrder UNORDERED = new SortOrder(Collections.<SortElement>emptyList());

  /**
   * The sort order.
   */
  private final List<SortElement> _order;
  /**
   * The effective comparator.
   */
  private final Comparator<Bean> _comparator;

  /**
   * Obtains an instance based on individual elements.
   * <p>
   * A simple sort order is formed by sorting by a single element.
   * However, a more complex order can be formed by sorting by one element within
   * another, handling the case where two data points are equal by the first sort.
   * <p>
   * For example, {@code SortOrder.of(bySurname, byForename)} would be expected to
   * sort by first name within surname.
   * 
   * @param order  the sort order, not null
   * @return the sort order, not null
   */
  public static SortOrder of(SortElement... order) {
    return new SortOrder(Arrays.asList(order));
  }

  /**
   * Creates an instance specifying the sort order.
   * 
   * @param order  the sort order, not null
   */
  private SortOrder(List<SortElement> order) {
    ArgumentChecker.noNulls(order, "order");
    _order = order;
    _comparator = createComparator(order);
  }

  @SuppressWarnings("unchecked")
  private static Comparator<Bean> createComparator(List<SortElement> order) {
    switch (order.size()) {
      case 0: {
        return null;
      }
      case 1: {
        return order.get(0).getComparator();
      }
      default: {
        List<Comparator<Bean>> comparators = new ArrayList<Comparator<Bean>>();
        for (SortElement element : order) {
          comparators.add(element.getComparator());
        }
        return ComparatorUtils.chainedComparator(comparators);
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the sort order, expressed as elements.
   * <p>
   * In SQL, each element corresponds to a comma separated ORDER BY clause.
   * A zero-size result means to perform no sorting.
   * 
   * @return the sort order, not null
   */
  public List<SortElement> getSortElements() {
    return _order;
  }

  /**
   * Gets the effective comparator.
   * 
   * @return the comparator, null if unordered
   */
  public Comparator<Bean> getComparator() {
    return _comparator;
  }

  //-------------------------------------------------------------------------
  /**
   * Sorts the specified list in place.
   * <p>
   * The list is sorted using the stored order.
   * 
   * @param <T> the list type
   * @param list  the list to sort, not null
   */
  public <T extends Bean> void sort(List<T> list) {
    Comparator<Bean> comp = getComparator();
    if (comp != null) {
      Collections.sort(list, comp);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + getSortElements();
  }

}
