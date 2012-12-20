/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import com.opengamma.util.paging.PagingRequest;

/**
 * Abstract base class for RESTful resources intended for websites.
 * <p>
 * Websites and web-services are related but different RESTful elements.
 * This is because a website needs to bend the RESTful rules in order to be usable.
 */
public abstract class AbstractWebResource {

  /**
   * Creates the resource, used by the root resource.
   */
  protected AbstractWebResource() {
  }

  //-------------------------------------------------------------------------
  /**
   * Builds the paging request.
   * <p>
   * This method is lenient, applying sensible default values.
   * 
   * @param pgIdx  the paging first-item index, null if not input
   * @param pgNum  the paging page, null if not input
   * @param pgSze  the paging size, null if not input
   * @return the paging request, not null
   */
  protected PagingRequest buildPagingRequest(Integer pgIdx, Integer pgNum, Integer pgSze) {
    int size = (pgSze != null ? pgSze : PagingRequest.DEFAULT_PAGING_SIZE);
    if (pgIdx != null) {
      return PagingRequest.ofIndex(pgIdx, size);
    } else if (pgNum != null) {
      return PagingRequest.ofPage(pgNum, size);
    } else {
      return PagingRequest.ofPage(1, size);
    }
  }

  /**
   * Builds the sort order.
   * <p>
   * This method is lenient, returning the default in case of error.
   * 
   * @param <T>  the sort order type
   * @param order  the sort order, null or empty returns default
   * @param defaultOrder  the default order, not null
   * @return the sort order, not null
   */
  protected <T extends Enum<T>> T buildSortOrder(String order, T defaultOrder) {
    if (StringUtils.isEmpty(order)) {
      return defaultOrder;
    }
    order = order.toUpperCase(Locale.ENGLISH);
    if (order.endsWith(" ASC")) {
      order = StringUtils.replace(order, " ASC", "_ASC");
    } else if (order.endsWith(" DESC")) {
      order = StringUtils.replace(order, " DESC", "_DESC");
    } else if (order.endsWith("_ASC") == false && order.endsWith("_DESC") == false) {
      order = order + "_ASC";
    }
    try {
      Class<T> cls = defaultOrder.getDeclaringClass();
      return Enum.valueOf(cls, order);
    } catch (IllegalArgumentException ex) {
      return defaultOrder;
    }
  }

}
