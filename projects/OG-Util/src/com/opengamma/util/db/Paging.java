/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import java.util.Collection;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.MutableFudgeFieldContainer;

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
   * Creates an instance that indicates a single page over the specified collection.
   * @param coll  the collection to generate 
   * @return the created paging, not null
   */
  public static Paging of(final Collection<?> coll) {
    ArgumentChecker.notNull(coll, "coll");
    return new Paging(1, Integer.MAX_VALUE, coll.size());
  }

  /**
   * Creates an instance that indicates a single page over the specified collection.
   * @param coll  the collection to generate 
   * @param pagingRequest  the paging request to base the result on, not null
   * @return the created paging, not null
   */
  public static Paging of(Collection<?> coll, PagingRequest pagingRequest) {
    ArgumentChecker.notNull(coll, "coll");
    ArgumentChecker.notNull(pagingRequest, "pagingRequest");
    if (pagingRequest.getFirstItemIndex() >= coll.size()) {
      return new Paging(1, pagingRequest.getPagingSize(), coll.size());
    }
    return new Paging(pagingRequest.getPage(), pagingRequest.getPagingSize(), coll.size());
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   * @param pagingRequest  the paging request
   * @param totalItems  the total number of items
   */
  public Paging(final PagingRequest pagingRequest, final int totalItems) {
    ArgumentChecker.notNull(pagingRequest, "pagingRequest");
    ArgumentChecker.notNegative(totalItems, "totalItems");
    _page = pagingRequest.getPage();
    _pagingSize = pagingRequest.getPagingSize();
    _totalItems = totalItems;
  }

  /**
   * Creates an instance.
   * @param page  the page number, one or greater
   * @param pagingSize  the paging size, one or greater
   * @param totalItems  the total number of items, zero or greater
   */
  public Paging(final int page, final int pagingSize, final int totalItems) {
    ArgumentChecker.notNegativeOrZero(page, "page");
    ArgumentChecker.notNegativeOrZero(pagingSize, "pagingSize");
    ArgumentChecker.notNegative(totalItems, "totalItems");
    _page = page;
    _pagingSize = pagingSize;
    _totalItems = totalItems;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the one-based page number.
   * @return the page number, one or greater
   */
  public int getPage() {
    return _page;
  }

  /**
   * Gets the size of each page.
   * @return the paging size, one or greater
   */
  public int getPagingSize() {
    return _pagingSize;
  }

  /**
   * Gets the total number of items.
   * @return the number of items, zero or greater
   */
  public int getTotalItems() {
    return _totalItems;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the first item, using a one-based index.
   * @return the first item number, one-based
   */
  public int getFirstItem() {
    return (_page - 1) * _pagingSize + 1;
  }

  /**
   * Gets the first item, using a zero-based index.
   * @return the first item index, zero-based
   */
  public int getFirstItemIndex() {
    return getFirstItem() - 1;
  }

  /**
   * Gets the last item inclusive, using a one-based index.
   * @return the last item number, inclusive, one-based
   */
  public int getLastItem() {
    return Math.min(_page * _pagingSize, _totalItems);
  }

  /**
   * Gets the last item exclusive, using a zero-based index.
   * @return the last item index, exclusive, zero-based
   */
  public int getLastItemIndex() {
    return getLastItem();
  }

  /**
   * Gets the total number of pages.
   * @return the number of pages
   */
  public int getTotalPages() {
    return (_totalItems - 1) / _pagingSize + 1;
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

  //-------------------------------------------------------------------------
  /** Field name. */
  private static final String PAGE_FIELD_NAME = "page";
  /** Field name. */
  private static final String PAGING_SIZE_FIELD_NAME = "pagingSize";
  /** Field name. */
  private static final String TOTAL_FIELD_NAME = "totalItems";

  /**
   * Serializes to a Fudge message.
   * @param messageFactory Fudge context, not null
   * @return the Fudge message, not null
   */
  public FudgeFieldContainer toFudgeMsg(final FudgeMessageFactory messageFactory) {
    MutableFudgeFieldContainer msg = messageFactory.newMessage();
    msg.add(PAGE_FIELD_NAME, _page);
    msg.add(PAGING_SIZE_FIELD_NAME, _pagingSize);
    msg.add(TOTAL_FIELD_NAME, _totalItems);
    return msg;
  }

  /**
   * Deserializes this pair from a Fudge message.
   * @param msg  the Fudge message, not null
   * @return the pair, not null
   */
  public static Paging fromFudgeMsg(final FudgeFieldContainer msg) {
    int page = msg.getInt(PAGE_FIELD_NAME);
    int pagingSize = msg.getInt(PAGING_SIZE_FIELD_NAME);
    int totalItems = msg.getInt(TOTAL_FIELD_NAME);
    return new Paging(page, pagingSize, totalItems);
  }

}
