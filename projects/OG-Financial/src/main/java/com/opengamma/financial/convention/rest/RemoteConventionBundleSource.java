/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.rest;

import java.net.URI;

import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.rest.AbstractRemoteClient;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;

/**
 * Provides remote access to a {@link ConventionBundleSource}.
 * <p>
 * This is the client to {@link DataConventionBundleSourceResource}.
 */
public class RemoteConventionBundleSource extends AbstractRemoteClient implements ConventionBundleSource {

  public RemoteConventionBundleSource(final URI uri) {
    super(uri);
  }

  // ConventionBundleSource

  @Override
  public ConventionBundle getConventionBundle(final ExternalId identifier) {
    try {
      return accessRemote(DataConventionBundleSourceResource.uriGetByIdentifier(getBaseUri(), identifier)).get(ConventionBundle.class);
    } catch (final UniformInterfaceException404NotFound e) {
      return null;
    }
  }

  @Override
  public ConventionBundle getConventionBundle(final ExternalIdBundle identifiers) {
    try {
      return accessRemote(DataConventionBundleSourceResource.uriGetByBundle(getBaseUri(), identifiers)).get(ConventionBundle.class);
    } catch (final UniformInterfaceException404NotFound e) {
      return null;
    }
  }

  @Override
  public ConventionBundle getConventionBundle(final UniqueId identifier) {
    try {
      return accessRemote(DataConventionBundleSourceResource.uriGetByUniqueId(getBaseUri(), identifier)).get(ConventionBundle.class);
    } catch (final UniformInterfaceException404NotFound e) {
      return null;
    }
  }

}
