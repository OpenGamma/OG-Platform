/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.config;

import java.net.URI;

import org.fudgemsg.FudgeMsg;

import com.opengamma.util.rest.AbstractRemoteClient;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;

/**
 * Remote client for accessing remote {@code Configuration}.
 */
public final class RemoteConfiguration extends AbstractRemoteClient {

  /**
   * Creates the resource.
   * 
   * @param baseUri the base URI, not null
   */
  public RemoteConfiguration(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the remote configuration at the URI.
   * 
   * @return the configuration message, null if not found
   */
  public FudgeMsg getConfigurationMsg() {
    try {
      return accessRemote(getBaseUri()).get(FudgeMsg.class);
    } catch (final UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

}
