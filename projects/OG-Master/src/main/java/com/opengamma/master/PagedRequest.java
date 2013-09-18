/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master;

import com.opengamma.util.paging.PagingRequest;

/**
 * A request with paging information associated.
 */
public interface PagedRequest {

  /**
   * Get the {@link PagingRequest} associated with this request.
   * @return the paging request
   */
  PagingRequest getPagingRequest();
  
  
  /**
   * Set the {@link PagingRequest} associated with this request.
   * @param pagingRequest the paging request
   */
  void setPagingRequest(PagingRequest pagingRequest);
}
