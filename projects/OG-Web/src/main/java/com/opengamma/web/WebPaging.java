/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;

/**
 * Output of paging for website tables.
 */
public class WebPaging {

  /**
   * The paging.
   */
  private final Paging _paging;
  /**
   * The URI.
   */
  private final UriInfo _uriInfo;

  /**
   * Creates an instance.
   * @param paging  the paging to display, not null
   * @param uriInfo  the URI, not null
   */
  public WebPaging(Paging paging, UriInfo uriInfo) {
    ArgumentChecker.notNull(paging, "Paging must not be null");
    ArgumentChecker.notNull(uriInfo, "UriInfo must not be null");
    _paging = paging;
    _uriInfo = uriInfo;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the first item, using a zero-based index.
   * 
   * @return the first item index, zero-based
   */
  public int getFirstItem() {
    return _paging.getFirstItem();
  }

  /**
   * Gets the page size, which is the number of items requested.
   * <p>
   * This is zero if no data was requested.
   * 
   * @return the number of items in the page, zero or greater
   */
  public int getPagingSize() {
    return _paging.getPagingSize();
  }

  /**
   * Gets the total number of items.
   * 
   * @return the number of items, zero or greater
   */
  public int getTotalItems() {
    return _paging.getTotalItems();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the first item, using a one-based index.
   * 
   * @return the first item number, one-based
   */
  public int getFirstItemOneBased() {
    return _paging.getFirstItemOneBased();
  }

  /**
   * Gets the last item exclusive, using a zero-based index.
   * 
   * @return the last item index, exclusive, zero-based
   */
  public int getLastItem() {
    return _paging.getLastItem();
  }

  /**
   * Gets the last item inclusive, using a one-based index.
   * 
   * @return the last item number, inclusive, one-based
   */
  public int getLastItemOneBased() {
    return _paging.getLastItemOneBased();
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
    return _paging.getPageNumber();
  }

  /**
   * Gets the total number of pages, one-based, when viewed as traditional paging.
   * 
   * @return the number of pages, one or greater
   * @throws ArithmeticException if a paging request of NONE was used
   */
  public int getTotalPages() {
    return _paging.getTotalPages();
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if there is a previous page.
   * 
   * @return true if there is a previous page
   */
  public boolean isPreviousPageExists() {
    return (getPageNumber() > 1);
  }

  /**
   * Checks if there is a next page.
   * 
   * @return true if there is a next page
   */
  public boolean isNextPageExists() {
    return (getPageNumber() < getTotalPages());
  }

  /**
   * Gets an object representing the first page.
   * 
   * @return the first page, not null
   */
  public WebPagingPage getFirstPage() {
    return new WebPagingPage(1);
  }

  /**
   * Gets an object representing the first page.
   * 
   * @return the last page, not null
   */
  public WebPagingPage getLastPage() {
    return new WebPagingPage(getTotalPages());
  }

  /**
   * Gets an object representing the previous page.
   * 
   * @return the previous page, not null
   */
  public WebPagingPage getPreviousPage() {
    int page = getPageNumber();
    return (page > 1 ? new WebPagingPage(page - 1) : null);
  }

  /**
   * Gets an object representing the next page.
   * 
   * @return the next page, not null
   */
  public WebPagingPage getNextPage() {
    int page = getPageNumber();
    return (page < getTotalPages() ? new WebPagingPage(page + 1) : null);
  }

  /**
   * Gets the paging items, excluding the first and last pages.
   * 
   * @return the paging item list, not null
   */
  public List<WebPagingPage> getPagesIncludeLast() {
    int basePage = getPageNumber();
    List<WebPagingPage> list = new ArrayList<WebPagingPage>();
    for (int i = basePage - 3; i <= basePage + 3; i++) {
      if (i < 1 || i > getTotalPages()) {
        continue;
      }
      list.add(new WebPagingPage(i));
    }
    return list;
  }

  /**
   * Gets the paging items, excluding the last page.
   * 
   * @return the paging item list, not null
   */
  public List<WebPagingPage> getPagesExcludeLast() {
    int basePage = getPageNumber();
    List<WebPagingPage> list = new ArrayList<WebPagingPage>();
    for (int i = basePage - 3; i <= basePage + 3; i++) {
      if (i < 1 || i >= getTotalPages()) {
        continue;
      }
      list.add(new WebPagingPage(i));
    }
    return list;
  }

  /**
   * Gets the paging items, excluding the last page.
   * 
   * @return the paging item list, not null
   */
  public List<WebPagingPage> getPagesExcludeFirstLast() {
    int basePage = getPageNumber();
    List<WebPagingPage> list = new ArrayList<WebPagingPage>();
    for (int i = basePage - 3; i <= basePage + 3; i++) {
      if (i <= 1 || i >= getTotalPages()) {
        continue;
      }
      list.add(new WebPagingPage(i));
    }
    return list;
  }

  /**
   * Gets the standard paging items.
   * 
   * @return the paging item list, not null
   */
  public List<WebPagingPage> getPagesStandard() {
    int basePage = getPageNumber();
    List<WebPagingPage> list = new ArrayList<WebPagingPage>();
    list.add(new WebPagingPage(1));
    for (int i = basePage - 3; i <= basePage + 3; i++) {
      if (i <= 1 || i >= getTotalPages()) {
        continue;
      }
      list.add(new WebPagingPage(i));
    }
    if (getTotalPages() > 1) {
      list.add(new WebPagingPage(getTotalPages()));
    }
    return list;
  }

  //-------------------------------------------------------------------------
  /**
   * A page within the paging.
   */
  public final class WebPagingPage {
    /**
     * The page number.
     */
    private final int _page;

    /**
     * Creates an instance.
     */
    WebPagingPage(int page) {
      _page = page;
    }

    /**
     * Gets the page number.
     * 
     * @return the page number, not null
     */
    public int getPageNumber() {
      return _page;
    }

    /**
     * Checks if this page is the current page.
     * 
     * @return the page, not null
     */
    public boolean isCurrentPage() {
      return _page == WebPaging.this.getPageNumber();
    }

    /**
     * Gets the URI for the page.
     * 
     * @return the URI, not null
     */
    public URI getUri() {
      UriBuilder builder = _uriInfo.getRequestUriBuilder();
      builder.replaceQueryParam("pgNum", _page);
      return builder.build();
    }
  }

}
