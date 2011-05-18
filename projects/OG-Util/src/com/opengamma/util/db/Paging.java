/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import java.util.Collection;
import java.util.NoSuchElementException;

import com.opengamma.util.ArgumentChecker;

/**
 * Simple immutable model describing the current paging state.
 */
public final class Paging {

  /**
   * The page number.
   */
  private final int _page;
  /**
   * The page size.
   */
  private final int _pagingSize;
  /**
   * The total number of items.
   */
  private final int _totalItems;

  /**
   * Creates an instance based on the specified collection setting the total count of items.
   * <p>
   * This combines {@link PagingRequest#ALL} with the collection size.
   * 
   * @param coll  the collection to base the paging on, not null
   * @return the created paging, not null
   */
  public static Paging of(final Collection<?> coll) {
    ArgumentChecker.notNull(coll, "coll");
    return new Paging(1, Integer.MAX_VALUE, coll.size());
  }

  /**
   * Creates an instance based on the specified collection setting the total count of items.
   * <p>
   * This combines the specified paging request with the collection size.
   * 
   * @param pagingRequest  the paging request to base the result on, not null
   * @param coll  the collection to base the paging on, not null
   * @return the created paging, not null
   */
  public static Paging of(PagingRequest pagingRequest, Collection<?> coll) {
    ArgumentChecker.notNull(coll, "coll");
    ArgumentChecker.notNull(pagingRequest, "pagingRequest");
    if (pagingRequest.getFirstItemIndex() >= coll.size()) {
      return new Paging(1, pagingRequest.getPagingSize(), coll.size());
    }
    return new Paging(pagingRequest.getPage(), pagingRequest.getPagingSize(), coll.size());
  }

  /**
   * Creates an instance from a paging request and total number of items.
   * 
   * @param pagingRequest  the paging request
   * @param totalItems  the total number of items
   * @return the created paging, not null
   */
  public static Paging of(final PagingRequest pagingRequest, final int totalItems) {
    ArgumentChecker.notNull(pagingRequest, "pagingRequest");
    return new Paging(pagingRequest.getPage(), pagingRequest.getPagingSize(), totalItems);
  }

  /**
   * Creates an instance from a page number, paging size and total number of items.
   * 
   * @param page  the page number, one or greater
   * @param pagingSize  the paging size, zero or greater
   * @param totalItems  the total number of items, zero or greater
   * @return the created paging, not null
   */
  public static Paging of(final int page, final int pagingSize, final int totalItems) {
    return new Paging(page, pagingSize, totalItems);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   * 
   * @param page  the page number, one or greater
   * @param pagingSize  the paging size, zero or greater
   * @param totalItems  the total number of items, zero or greater
   */
  private Paging(final int page, final int pagingSize, final int totalItems) {
    ArgumentChecker.notNegativeOrZero(page, "page");
    ArgumentChecker.notNegative(pagingSize, "pagingSize");
    ArgumentChecker.notNegative(totalItems, "totalItems");
    _page = page;
    _pagingSize = pagingSize;
    _totalItems = totalItems;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the one-based page number.
   * 
   * @return the page number, one or greater
   */
  public int getPage() {
    return _page;
  }

  /**
   * Gets the size of each page.
   * 
   * @return the paging size, zero or greater
   */
  public int getPagingSize() {
    return _pagingSize;
  }

  /**
   * Gets the total number of items.
   * 
   * @return the number of items, zero or greater
   */
  public int getTotalItems() {
    return _totalItems;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the first item, using a one-based index.
   * 
   * @return the first item number, one-based
   */
  public int getFirstItem() {
    return (_page - 1) * _pagingSize + 1;
  }

  /**
   * Gets the first item, using a zero-based index.
   * 
   * @return the first item index, zero-based
   */
  public int getFirstItemIndex() {
    return getFirstItem() - 1;
  }

  /**
   * Gets the last item inclusive, using a one-based index.
   * 
   * @return the last item number, inclusive, one-based
   */
  public int getLastItem() {
    return Math.min(_page * _pagingSize, _totalItems);
  }

  /**
   * Gets the last item exclusive, using a zero-based index.
   * 
   * @return the last item index, exclusive, zero-based
   */
  public int getLastItemIndex() {
    return getLastItem();
  }

  /**
   * Gets the total number of pages.
   * 
   * @return the number of pages
   */
  public int getTotalPages() {
    return (_totalItems - 1) / _pagingSize + 1;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks whether a paging request of NONE was used, returning only the
   * total item count.
   * 
   * @return true if there is another page
   */
  public boolean isSizeOnly() {
    return _pagingSize == 0;
  }

  /**
   * Checks whether there is a next page available.
   * This is the opposite of {@link #isLastPage()}.
   * 
   * @return true if there is another page
   * @throws IllegalStateException if insufficient information was requested - PagingRequest.NONE
   */
  public boolean isNextPage() {
    checkPaging();
    return _page < getTotalPages();
  }

  /**
   * Checks whether this is the last page.
   * This is the opposite of {@link #isNextPage()}.
   * 
   * @return true if this is the last page
   * @throws IllegalStateException if insufficient information was requested - PagingRequest.NONE
   */
  public boolean isLastPage() {
    checkPaging();
    return _page == getTotalPages();
  }

  /**
   * Checks whether there is a previous page available.
   * This is the opposite of {@link #isFirstPage()}.
   * 
   * @return true if there is a previous page
   * @throws IllegalStateException if insufficient information was requested - PagingRequest.NONE
   */
  public boolean isPreviousPage() {
    checkPaging();
    return _page > 1;
  }

  /**
   * Checks whether this is the first page.
   * This is the opposite of {@link #isPreviousPage()}.
   * 
   * @return true if this is the last page
   * @throws IllegalStateException if insufficient information was requested - PagingRequest.NONE
   */
  public boolean isFirstPage() {
    checkPaging();
    return _page == 1;
  }

  //-------------------------------------------------------------------------
  /**
   * Converts this object to a {@code PagingRequest} for the same page.
   * 
   * @return the request for the same page, not null
   */
  public PagingRequest toPagingRequest() {
    return new PagingRequest(_page, _pagingSize);
  }

  /**
   * Gets the {@code PagingRequest} for the next page.
   * 
   * @return the request for the next page, not null
   * @throws IllegalStateException if insufficient information was requested - PagingRequest.NONE
   * @throws NoSuchElementException if there are no more pages
   */
  public PagingRequest nextPagingRequest() {
    checkPaging();
    if (isLastPage()) {
      throw new NoSuchElementException("Unable to return next page as this is the last page");
    }
    return new PagingRequest(_page + 1, _pagingSize);
  }

  /**
   * Gets the {@code PagingRequest} for the next page.
   * 
   * @return the request for the previous page, not null
   * @throws IllegalStateException if insufficient information was requested - PagingRequest.NONE
   * @throws NoSuchElementException if there are no more pages
   */
  public PagingRequest previousPagingRequest() {
    checkPaging();
    if (isFirstPage()) {
      throw new NoSuchElementException("Unable to return previous page as this is the first page");
    }
    return new PagingRequest(_page - 1, _pagingSize);
  }

  /**
   * Checks if this represents a valid paging request for paging.
   * 
   * @throws IllegalStateException if insufficient information was requested - PagingRequest.NONE
   */
  private void checkPaging() {
    if (isSizeOnly()) {
      throw new IllegalStateException("Paging base on PagingRequest.NONE");
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Paging) {
      Paging other = (Paging) obj;
      return _page == other._page && _pagingSize == other._pagingSize && _totalItems == other._totalItems;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _page << 24 + _pagingSize << 16 + _totalItems;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[page=" + _page + ", pagingSize=" + _pagingSize + ", totalItems=" + _totalItems + "]";
  }

}
