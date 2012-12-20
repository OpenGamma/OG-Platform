/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.paging;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * Simple immutable request for a page of results.
 * <p>
 * This class is follows the design of SQL OFFSET and FETCH/LIMIT, exposed as a first-item/size data model.
 * This can be used to implement traditional fixed paging or arbitrary paging starting from an index.
 * <p>
 * This class is immutable and thread-safe.
 */
@PublicAPI
public final class PagingRequest {

  /**
   * A default size for paging.
   */
  public static final int DEFAULT_PAGING_SIZE = 20;
  /**
   * Singleton constant to request all items (no paging).
   */
  public static final PagingRequest ALL = new PagingRequest(0, Integer.MAX_VALUE);
  /**
   * Singleton constant to request the first page of 20 items.
   */
  public static final PagingRequest FIRST_PAGE = new PagingRequest(0, DEFAULT_PAGING_SIZE);
  /**
   * Singleton constant to request the first matching item.
   */
  public static final PagingRequest ONE = new PagingRequest(0, 1);
  /**
   * Singleton constant to request no data, just the total count.
   */
  public static final PagingRequest NONE = new PagingRequest(0, 0);

  /**
   * The requested first item.
   */
  private final int _index;
  /**
   * The requested number of items.
   */
  private final int _size;

  /**
   * Obtains an instance based on a zero-based index and requested size.
   * <p>
   * This factory represents the internal state directly.
   * The index is the first item in the list of results that is required (SQL OFFSET).
   * The size is the requested number of items (SQL FETCH/LIMIT).
   * 
   * @param index  the zero-based start index, zero or greater
   * @param size  the number of items to request, zero or greater
   * @return the paging request, not null
   * @throws IllegalArgumentException if either input is invalid
   */
  public static PagingRequest ofIndex(int index, int size) {
    return new PagingRequest(index, size);
  }

  /**
   * Obtains an instance based on a page and paging size.
   * <p>
   * This implements paging on top of the basic first-item/size data model.
   * 
   * @param page  the page number, one or greater
   * @param pagingSize  the paging size, zero or greater
   * @return the paging request, not null
   * @throws IllegalArgumentException if either input is invalid
   */
  public static PagingRequest ofPage(int page, int pagingSize) {
    ArgumentChecker.notNegativeOrZero(page, "page");
    ArgumentChecker.notNegative(pagingSize, "pagingSize");
    int index = ((page - 1) * pagingSize);
    return new PagingRequest(index, pagingSize);
  }

  /**
   * Obtains an instance based on a page and paging size, applying default values.
   * <p>
   * This implements paging on top of the basic first-item/size data model.
   * The page will default to 1 if the input is 0.
   * The paging size will default to 20 if the input is 0.
   * 
   * @param page  the page number, page one chosen if zero, not negative
   * @param pagingSize  the paging size, size twenty chosen if zero, not negative
   * @return the paging request, not null
   * @throws IllegalArgumentException if either input is negative
   */
  public static PagingRequest ofPageDefaulted(int page, int pagingSize) {
    page = (page == 0 ? 1 : page);
    pagingSize = (pagingSize == 0 ? DEFAULT_PAGING_SIZE : pagingSize);
    return PagingRequest.ofPage(page, pagingSize);
  }

  /**
   * Creates an instance without using defaults.
   * <p>
   * A paging size of zero will only return the count of items and will
   * always have a first item index of zero.
   * 
   * @param index  the zero-based start index, zero or greater
   * @param size  the number of items to request, zero or greater
   * @throws IllegalArgumentException if either input is invalid
   */
  private PagingRequest(final int index, final int size) {
    ArgumentChecker.notNegative(index, "index");
    ArgumentChecker.notNegative(size, "size");
    _index = (size != 0 ? index : 0);
    _size = size;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the first item, using a zero-based index.
   * <p>
   * In SQL this corresponds to OFFSET.
   * 
   * @return the first item index, zero-based
   */
  public int getFirstItem() {
    return _index;
  }

  /**
   * Gets the requested number of items.
   * <p>
   * In SQL this corresponds to FETCH/LIMIT.
   * 
   * @return the number of requested items, zero or greater
   */
  public int getPagingSize() {
    return _size;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the first item, using a one-based index.
   * 
   * @return the first item number, one-based
   */
  public int getFirstItemOneBased() {
    return _index + 1;
  }

  /**
   * Gets the last item exclusive, using a zero-based index.
   * 
   * @return the last item index, exclusive, zero-based
   */
  public int getLastItem() {
    return _index + _size;
  }

  /**
   * Gets the last item inclusive, using a one-based index.
   * 
   * @return the last item number, inclusive, one-based
   */
  public int getLastItemOneBased() {
    return getLastItem();
  }

  //-------------------------------------------------------------------------
  /**
   * Selects the elements from the list matching this request.
   * <p>
   * This will return a new list consisting of the selected elements from the supplied list.
   * The elements are selected based on {@link #getFirstItem()} and {@link #getLastItem()}.
   * 
   * @param <T> the list type
   * @param list  the collection to select from, not null
   * @return the selected list, not linked to the original, not null
   */
  public <T> List<T> select(List<T> list) {
    int firstIndex = getFirstItem();
    int lastIndex = getLastItem();
    if (firstIndex > list.size()) {
      firstIndex = list.size();
    }
    if (lastIndex > list.size()) {
      lastIndex = list.size();
    }
    return new ArrayList<T>(list.subList(firstIndex, lastIndex));
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PagingRequest) {
      PagingRequest other = (PagingRequest) obj;
      return _index == other._index && _size == other._size;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _index << 16 + _size;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[first=" + _index + ", size=" + _size + "]";
  }

}
