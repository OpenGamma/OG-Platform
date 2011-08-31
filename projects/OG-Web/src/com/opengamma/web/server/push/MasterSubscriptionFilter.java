/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

import java.util.List;

/**
 *
 */
public class MasterSubscriptionFilter implements ResourceFilter {

  private final List<MasterType> _masterTypes;

  public MasterSubscriptionFilter(List<MasterType> masterTypes) {
    _masterTypes = masterTypes;
  }

  @Override
  public ContainerRequestFilter getRequestFilter() {
    return null;
  }

  @Override
  public ContainerResponseFilter getResponseFilter() {
    return new ResponseFilter(_masterTypes);
  }

  private class ResponseFilter implements ContainerResponseFilter {

    private final List<MasterType> _masterTypes;

    public ResponseFilter(List<MasterType> masterTypes) {
      _masterTypes = masterTypes;
    }

    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
      // TODO check response status
      return response;
    }
  }
}
