/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.organization.impl;

import java.net.URI;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.AbstractRemoteSource;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.organization.Organization;
import com.opengamma.core.organization.OrganizationSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;

/**
 * Provides access to a remote {@link OrganizationSource} via a RESTful API.
 */
public class RemoteOrganizationSource extends AbstractRemoteSource<Organization> implements OrganizationSource {

  /**
   * The change manager.
   */
  private final ChangeManager _changeManager;

  /**
   * Creates an instance.
   *
   * @param baseUri the base target URI for all RESTful web services, not null
   */
  public RemoteOrganizationSource(URI baseUri) {
    this(baseUri, new BasicChangeManager());
  }

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteOrganizationSource(final URI baseUri, final ChangeManager changeManager) {
    super(baseUri);
    ArgumentChecker.notNull(changeManager, "changeManager");
    _changeManager = changeManager;
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  @Override
  public Organization getOrganizationByRedCode(String redCode) {
    ArgumentChecker.notNull(redCode, "redCode");
    URI uri = DataOrganizationSourceResource.uriSearchByRedCode(getBaseUri(), redCode);
    try {
      return accessRemote(uri).get(Organization.class);
    } catch (DataNotFoundException ex) {
      return null;
    } catch (UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public Organization getOrganizationByTicker(String ticker) {
    ArgumentChecker.notNull(ticker, "ticker");
    URI uri = DataOrganizationSourceResource.uriSearchByTicker(getBaseUri(), ticker);
    try {
      return accessRemote(uri).get(Organization.class);
    } catch (DataNotFoundException ex) {
      return null;
    } catch (UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public Organization get(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    URI uri = DataOrganizationSourceResource.uriGet(getBaseUri(), uniqueId);
    try {
      return accessRemote(uri).get(Organization.class);
    } catch (DataNotFoundException ex) {
      return null;
    } catch (UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public Organization get(ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    URI uri = DataOrganizationSourceResource.uriGet(getBaseUri(), objectId, versionCorrection);
    try {
      return accessRemote(uri).get(Organization.class);
    } catch (DataNotFoundException ex) {
      return null;
    } catch (UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  // Currently no bulk api implementation - to be added if required, for the time being
  // AbstractRemoteSource#get will be used
}
