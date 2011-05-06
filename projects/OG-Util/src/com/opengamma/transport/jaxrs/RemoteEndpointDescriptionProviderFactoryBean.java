/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import com.opengamma.util.SingletonFactoryBean;

/**
 * Spring factory bean for {@code RemoteEndPointDescriptionProvider}.
 */
public class RemoteEndpointDescriptionProviderFactoryBean extends SingletonFactoryBean<RemoteEndPointDescriptionProvider> {

  /**
   * The URI to fetch from.
   */
  private String _uri;

  /**
   * Gets the URI.
   * 
   * @return the URI
   */
  public String getUri() {
    return _uri;
  }

  /**
   * Sets the URI.
   * 
   * @param uri  the URI
   */
  public void setUri(String uri) {
    _uri = uri;
  }

  @Override
  protected RemoteEndPointDescriptionProvider createObject() {
    if (getUri() == null) {
      throw new IllegalArgumentException("uri must be set");
    }
    RemoteEndPointDescriptionProvider provider = new RemoteEndPointDescriptionProvider();
    provider.setUri(getUri());
    return provider;
  }

}
