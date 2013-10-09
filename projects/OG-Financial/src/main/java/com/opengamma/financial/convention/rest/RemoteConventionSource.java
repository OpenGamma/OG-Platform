/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.rest;

import java.net.URI;

import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.rest.AbstractRemoteClient;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;

/**
 * Provides remote access to a {@link ConventionSource}.
 * <p>
 * This is the client to {@link DataConventionSourceResource}.
 */
public class RemoteConventionSource extends AbstractRemoteClient implements ConventionSource {

  /**
   * @param uri The uri
   */
  public RemoteConventionSource(final URI uri) {
    super(uri);
  }

  @Override
  public Convention getConvention(final ExternalId identifier) {
    try {
      return accessRemote(DataConventionSourceResource.uriGetByIdentifier(getBaseUri(), identifier)).get(Convention.class);
    } catch (final UniformInterfaceException404NotFound e) {
      return null;
    }
  }

  @Override
  public Convention getConvention(final ExternalIdBundle identifiers) {
    try {
      return accessRemote(DataConventionSourceResource.uriGetByBundle(getBaseUri(), identifiers)).get(Convention.class);
    } catch (final UniformInterfaceException404NotFound e) {
      return null;
    }
  }

  @Override
  public Convention getConvention(final UniqueId identifier) {
    try {
      return accessRemote(DataConventionSourceResource.uriGetByUniqueId(getBaseUri(), identifier)).get(Convention.class);
    } catch (final UniformInterfaceException404NotFound e) {
      return null;
    }
  }

  @Override
  public <T extends Convention> T getConvention(final Class<T> clazz, final ExternalId identifier) {
    try {
      return accessRemote(DataConventionSourceResource.uriGetByIdentifier(getBaseUri(), identifier)).get(clazz);
    } catch (final UniformInterfaceException404NotFound e) {
      return null;
    }
  }

  @Override
  public <T extends Convention> T getConvention(final Class<T> clazz, final ExternalIdBundle identifiers) {
    try {
      return accessRemote(DataConventionSourceResource.uriGetByBundle(getBaseUri(), identifiers)).get(clazz);
    } catch (final UniformInterfaceException404NotFound e) {
      return null;
    }
  }

  @Override
  public <T extends Convention> T getConvention(final Class<T> clazz, final UniqueId identifier) {
    try {
      return accessRemote(DataConventionSourceResource.uriGetByUniqueId(getBaseUri(), identifier)).get(clazz);
    } catch (final UniformInterfaceException404NotFound e) {
      return null;
    }
  }
}
