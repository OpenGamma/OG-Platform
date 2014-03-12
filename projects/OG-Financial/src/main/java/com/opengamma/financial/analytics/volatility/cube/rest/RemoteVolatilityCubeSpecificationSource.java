/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube.rest;

import java.net.URI;

import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeSpecification;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeSpecificationSource;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Provides remote access to a {@link VolatilityCubeSpecificationSource}.
 */
public class RemoteVolatilityCubeSpecificationSource extends AbstractRemoteClient implements VolatilityCubeSpecificationSource {

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteVolatilityCubeSpecificationSource(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  @Override
  public VolatilityCubeSpecification getSpecification(final String name) {
    ArgumentChecker.notNull(name, "name");

    final URI uri = DataVolatilityCubeSpecificationSourceResource.uriSearchSingle(getBaseUri(), name, null);
    return accessRemote(uri).get(VolatilityCubeSpecification.class);
  }

  @Override
  public VolatilityCubeSpecification getSpecification(final String name, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(name, "name");

    final URI uri = DataVolatilityCubeSpecificationSourceResource.uriSearchSingle(getBaseUri(), name, versionCorrection.getVersionAsOf());
    return accessRemote(uri).get(VolatilityCubeSpecification.class);
  }

}
