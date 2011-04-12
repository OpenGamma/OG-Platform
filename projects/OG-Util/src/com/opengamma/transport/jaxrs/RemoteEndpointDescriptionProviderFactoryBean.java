/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import com.opengamma.util.SingletonFactoryBean;

/**
 * Spring factory bean for {@link RemoteEndPointDescriptorProvider}
 */
public class RemoteEndpointDescriptionProviderFactoryBean extends SingletonFactoryBean<RemoteEndPointDescriptionProvider> {

  private String _uri;
  
  public String getUri() {
    return _uri;
  }
  
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
