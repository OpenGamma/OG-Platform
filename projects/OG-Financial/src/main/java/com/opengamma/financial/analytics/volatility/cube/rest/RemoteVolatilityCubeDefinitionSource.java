/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube.rest;

import java.net.URI;

import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinition;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Provides remote access to a {@link VolatilityCubeDefinitionSource}.
 */
public class RemoteVolatilityCubeDefinitionSource extends AbstractRemoteClient implements VolatilityCubeDefinitionSource {

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteVolatilityCubeDefinitionSource(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  @Override
  public VolatilityCubeDefinition<?, ?, ?> getDefinition(final String name) {
    ArgumentChecker.notNull(name, "name");

    final URI uri = DataVolatilityCubeDefinitionSourceResource.uriSearchSingle(getBaseUri(), name, null);
    return accessRemote(uri).get(VolatilityCubeDefinition.class);
  }

  @Override
  public VolatilityCubeDefinition<?, ?, ?> getDefinition(final String name, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(name, "name");

    final URI uri = DataVolatilityCubeDefinitionSourceResource.uriSearchSingle(getBaseUri(), name, versionCorrection.getVersionAsOf());
    return accessRemote(uri).get(VolatilityCubeDefinition.class);
  }

}
