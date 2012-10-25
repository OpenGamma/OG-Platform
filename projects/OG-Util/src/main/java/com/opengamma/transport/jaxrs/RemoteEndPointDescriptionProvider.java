/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import java.net.URI;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;

import com.opengamma.transport.EndPointDescriptionProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.FudgeRestClient;

/**
 * An implementation of {@code EndPointDescriptionProvider} that operates over a REST call.
 */
public class RemoteEndPointDescriptionProvider implements EndPointDescriptionProvider {

  /**
   * The URI to access.
   */
  private URI _uri;

  /**
   * Creates an instance.
   */
  public RemoteEndPointDescriptionProvider() {
  }

  /**
   * Creates an instance.
   * 
   * @param uri  the URI
   */
  public RemoteEndPointDescriptionProvider(final URI uri) {
    setUri(uri);
  }

  //-------------------------------------------------------------------------
  /**
   * Sets the URI to access.
   * 
   * @return the URI
   */
  public URI getUri() {
    return _uri;
  }

  /**
   * Sets the URI to access.
   * 
   * @param uri  the URI
   */
  public void setUri(final URI uri) {
    _uri = uri;
  }

  //-------------------------------------------------------------------------
  @Override
  public FudgeMsg getEndPointDescription(final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(getUri(), "URI");
    FudgeRestClient restClient = FudgeRestClient.create();
    return restClient.accessFudge(getUri()).get(FudgeMsg.class);
  }

}
