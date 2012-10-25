/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.paging;

import java.util.Collection;
import java.util.NoSuchElementException;

import com.opengamma.util.ArgumentChecker;

/**
 * Simple immutable description of a range of results.
 * <p>
 * This class is the result of using {@link PagingRequest} to obtain an indexed subset of results.
 * This may represent traditional fixed paging or arbitrary paging starting from an index.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class Paging {

  /**
   * The request.
   */
  private final PagingRequest _request;
  /**
   * The total number of items.
   */
  private final int _totalItems;

  /**
   * Creates an instance from a paging request and total number of items.
   * 
   * @param pagingRequest  the paging request to base the result on, not null
   * @param totalItems  the total number of items
   * @return the created paging, not null
   */
  public static Paging of(final PagingRequest pagingRequest, final int totalItems) {
    return new Paging(pagingRequest, totalItems);
  }

  /**
   * Creates an instance based on the specified collection setting the total count of items.
   * <p>
   * This combines {@link PagingRequest#ALL} with the collection size.
   * 
   * @param coll  the collection to base the paging on, not null
   * @return the created paging, not null
   */
  public static Paging ofAll(final Collection<?> coll) {
    ArgumentChecker.notNull(coll, "coll");
    return new Paging(PagingRequest.ALL, coll.size());
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
    ArgumentChecker.notNull(pagingRequest, "pagingRequest");
    ArgumentChecker.notNull(coll, "coll");
    if (pagingRequest.getFirstItem() >= coll.size()) {
      return new Paging(PagingRequest.ofIndex(coll.size(), pagingRequest.getPagingSize()), coll.size());
    }
    return new Paging(pagingRequest, coll.size());
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   * 
   * @param pagingRequest  the request, not null
   * @param totalItems  the total number of items, zero or greater
   */
  private Paging(final PagingRequest pagingRequest, final int totalItems) {
    ArgumentChecker.notNull(pagingRequest, "pagingRequest");
    ArgumentChecker.notNegative(totalItems, "totalItems");
    _request = pagingRequest;
    _totalItems = totalItems;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the request that represents the results.
   * <p>
   * This request represents that request that matches the results.
   * This is not necessarily the same as the request actually used.
   * 
   * @return the request, not null
   */
  public PagingRequest getRequest() {
    return _request;
  }

  /**
   * Gets the total number of items in the complete result set.
   * <p>
   * This is the number of results that would be returned if  {@link PagingRequest#ALL} was used.
   * 
   * @return the number of items, zero or greater
   */
  public int getTotalItems() {
    return _totalItems;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the first item, using a zero-based index.
   * 
   * @return the first item index, zero-based
   */
  public int getFirstItem() {
    return getRequest().getFirstItem();
  }

  /**
   * Gets the first item, using a one-based index.
   * 
   * @return the first item number, one-based
   */
  public int getFirstItemOneBased() {
    return getRequest().getFirstItemOneBased();
  }

  /**
   * Gets the last item exclusive, using a zero-based index.
   * 
   * @return the last item index, exclusive, zero-based
   */
  public int getLastItem() {
    return Math.min(getRequest().getLastItem(), getTotalItems());
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
   * Gets the current page number, one-based, when viewed as traditional paging.
   * <p>
   * If the request was for index-based paging rather than traditional paging
   * then the result of this method will be the effective page of the first item.
   * 
   * @return the current page, one or greater
   */
  public int getPageNumber() {
    return (getFirstItem() / getPagingSize()) + 1;
  }

  /**
   * Gets the page size, which is the number of items requested.
   * <p>
   * This is zero if no data was requested.
   * 
   * @return the number of items in the page, zero or greater
   */
  public int getPagingSize() {
    return getRequest().getPagingSize();
  }

  /**
   * Gets the total number of pages, one-based, when viewed as traditional paging.
   * 
   * @return the number of pages, one or greater
   * @throws ArithmeticException if a paging request of NONE was used
   */
  public int getTotalPages() {
    return (getTotalItems() - 1) / getPagingSize() + 1;
  }

  /**
   * Checks whether a paging request of NONE was used, returning only the
   * total item count.
   * 
   * @return true if unable to use paging
   */
  public boolean isSizeOnly() {
    return getPagingSize() == 0;
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
    return getPageNumber() < getTotalPages();
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
    return getPageNumber() == getTotalPages();
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
    return getPageNumber() > 1;
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
    return getPageNumber() == 1;
  }

  //-------------------------------------------------------------------------
  /**
   * Converts this object to a {@code PagingRequest} for the same page.
   * <p>
   * This can convert an index-based original request into a page-based one.
   * 
   * @return the request for the same page, not null
   */
  public PagingRequest toPagingRequest() {
    if (isSizeOnly()) {
      return PagingRequest.NONE;
    }
    return PagingRequest.ofPage(getPageNumber(), getPagingSize());
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
    return PagingRequest.ofPage(getPageNumber() + 1, getPagingSize());
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
    return PagingRequest.ofPage(getPageNumber() - 1, getPagingSize());
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
      return _request.equals(other._request) && _totalItems == other._totalItems;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _request.hashCode() ^ _totalItems;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[first=" + getFirstItem() + ", size=" + getPagingSize() + ", totalItems=" + _totalItems + "]";
  }

}
