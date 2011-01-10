/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.util.db.Paging;

/**
 * Output of paging for website tables.
 */
public class WebPaging {

  /**
   * The website paging.
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
    _paging = paging;
    _uriInfo = uriInfo;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the one-based page number.
   * @return the page number, one or greater
   */
  public int getPage() {
    return _paging.getPage();
  }

  /**
   * Gets the size of each page.
   * @return the paging size, one or greater
   */
  public int getPagingSize() {
    return _paging.getPagingSize();
  }

  /**
   * Gets the total number of items.
   * @return the number of items, zero or greater
   */
  public int getTotalItems() {
    return _paging.getTotalItems();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the first item, using a one-based index.
   * @return the first item number, one-based
   */
  public int getFirstItem() {
    return _paging.getFirstItem();
  }

  /**
   * Gets the first item, using a zero-based index.
   * @return the first item index, zero-based
   */
  public int getFirstItemIndex() {
    return _paging.getFirstItemIndex();
  }

  /**
   * Gets the last item inclusive, using a one-based index.
   * @return the last item number, inclusive, one-based
   */
  public int getLastItem() {
    return _paging.getLastItem();
  }

  /**
   * Gets the last item exclusive, using a zero-based index.
   * @return the last item index, exclusive, zero-based
   */
  public int getLastItemIndex() {
    return _paging.getLastItemIndex();
  }

  /**
   * Gets the total number of pages.
   * @return the number of pages
   */
  public int getTotalPages() {
    return _paging.getTotalPages();
  }

  /**
   * Gets the total number of pages.
   * @return the number of pages
   */
  int getUriBuilder() {
    return _paging.getTotalPages();
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if there is a previous page.
   * @return true if there is a previous page
   */
  public boolean isPreviousPageExists() {
    return (getPage() > 1);
  }

  /**
   * Checks if there is a next page.
   * @return true if there is a next page
   */
  public boolean isNextPageExists() {
    return (getPage() < getTotalPages());
  }

  /**
   * Gets an object representing the first page.
   * @return the first page, not null
   */
  public WebPagingPage getFirstPage() {
    return new WebPagingPage(1);
  }

  /**
   * Gets an object representing the first page.
   * @return the last page, not null
   */
  public WebPagingPage getLastPage() {
    return new WebPagingPage(getTotalPages());
  }

  /**
   * Gets an object representing the previous page.
   * @return the previous page, not null
   */
  public WebPagingPage getPreviousPage() {
    int page = getPage();
    return (page > 1 ? new WebPagingPage(page - 1) : null);
  }

  /**
   * Gets an object representing the next page.
   * @return the next page, not null
   */
  public WebPagingPage getNextPage() {
    int page = getPage();
    return (page < getTotalPages() ? new WebPagingPage(page + 1) : null);
  }

  /**
   * Gets the paging items, excluding the first and last pages.
   * @return the paging item list, not null
   */
  public List<WebPagingPage> getPagesIncludeLast() {
    int basePage = getPage();
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
   * @return the paging item list, not null
   */
  public List<WebPagingPage> getPagesExcludeLast() {
    int basePage = getPage();
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
   * @return the paging item list, not null
   */
  public List<WebPagingPage> getPagesExcludeFirstLast() {
    int basePage = getPage();
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
   * @return the paging item list, not null
   */
  public List<WebPagingPage> getPagesStandard() {
    int basePage = getPage();
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
   * An page within the paging.
   */
  public final class WebPagingPage {
    private final int _page;
    /**
     * Creates an instance.
     */
    WebPagingPage(int page) {
      _page = page;
    }

    /**
     * Gets the page.
     * @return the page, not null
     */
    public int getPage() {
      return _page;
    }

    /**
     * Checks if this page is the current page.
     * @return the page, not null
     */
    public boolean isCurrentPage() {
      return _page == WebPaging.this.getPage();
    }

    /**
     * Gets the URI for the page.
     * @return the URI, not null
     */
    public URI getUri() {
      UriBuilder builder = _uriInfo.getRequestUriBuilder();
      builder.replaceQueryParam("page", _page);
      return builder.build();
    }
  }

}
