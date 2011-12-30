/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import com.google.common.collect.Lists;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Remote client for accessing remote components.
 */
public class RemoteComponents extends AbstractRemoteClient {

  /**
   * Creates the resource.
   * 
   * @param baseUri  the base URI, not null
   */
  public RemoteComponents(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the remote components at the URI.
   * 
   * @return the components, not null
   */
  public List<ComponentInfo> getComponentInfos() {
    URI uri = DataComponentsResource.uri(getBaseUri());
    ComponentInfosMsg msg = accessRemote(uri).get(ComponentInfosMsg.class);
    for (ComponentInfo info : msg.getInfos()) {
      if (info.getUri() != null && info.getUri().isAbsolute() == false) {
        URI combinedUri = UriBuilder.fromUri(getBaseUri()).path(info.getUri().toString()).build();
        info.setUri(combinedUri);
      }
    }
    return Lists.newArrayList(msg.getInfos());
  }

}
