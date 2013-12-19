/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.legalentity.impl;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.AbstractRemoteSource;
import com.opengamma.core.AbstractSourceWithExternalBundle;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.legalentity.LegalEntity;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.FudgeListWrapper;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;

/**
 * Provides remote access to an {@link LegalEntitySource}.
 */
public class RemoteLegalEntitySource
    extends AbstractRemoteSource<LegalEntity>
    implements LegalEntitySource {

  /**
   * The change manager.
   */
  private final ChangeManager _changeManager;

  /**
   * Creates an instance.
   *
   * @param baseUri the base target URI for all RESTful web services, not null
   */
  public RemoteLegalEntitySource(final URI baseUri) {
    this(baseUri, new BasicChangeManager());
  }

  /**
   * Creates an instance.
   *
   * @param baseUri       the base target URI for all RESTful web services, not null
   * @param changeManager the change manager, not null
   */
  public RemoteLegalEntitySource(final URI baseUri, final ChangeManager changeManager) {
    super(baseUri);
    ArgumentChecker.notNull(changeManager, "changeManager");
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public LegalEntity get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    URI uri = DataLegalEntitySourceResource.uriGet(getBaseUri(), uniqueId);
    return accessRemote(uri).get(LegalEntity.class);
  }

  @Override
  public <T extends LegalEntity> T get(UniqueId uniqueId, Class<T> type) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(type, "type");
    LegalEntity legalEntity = get(uniqueId);
    return type.cast(legalEntity);
  }

  //-------------------------------------------------------------------------
  @Override
  public LegalEntity get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    URI uri = DataLegalEntitySourceResource.uriGet(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(LegalEntity.class);
  }

  @Override
  public <T extends LegalEntity> T get(ObjectId objectId, VersionCorrection versionCorrection, Class<T> type) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(type, "type");
    LegalEntity legalEntity = get(objectId, versionCorrection);
    return type.cast(legalEntity);
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public Collection<LegalEntity> get(final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");

    URI uri = DataLegalEntitySourceResource.uriSearchList(getBaseUri(), bundle);
    return accessRemote(uri).get(FudgeListWrapper.class).getList();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<LegalEntity> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    URI uri = DataLegalEntitySourceResource.uriSearch(getBaseUri(), versionCorrection, bundle);
    return accessRemote(uri).get(FudgeListWrapper.class).getList();
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public Map<UniqueId, LegalEntity> get(final Collection<UniqueId> uniqueIds) {
    ArgumentChecker.notNull(uniqueIds, "uniqueIds");

    URI uri = DataLegalEntitySourceResource.uriBulk(getBaseUri(), uniqueIds);
    List<LegalEntity> list = accessRemote(uri).get(FudgeListWrapper.class).getList();
    Map<UniqueId, LegalEntity> result = Maps.newHashMap();
    for (LegalEntity legalEntity : list) {
      result.put(legalEntity.getUniqueId(), legalEntity);
    }
    return result;
  }

  @Override
  public Map<ExternalIdBundle, Collection<LegalEntity>> getAll(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    // TODO: Implement this properly as a REST call
    return AbstractSourceWithExternalBundle.getAll(this, bundles, versionCorrection);
  }

  //-------------------------------------------------------------------------
  @Override
  public LegalEntity getSingle(final ExternalId externalId) {
    ArgumentChecker.notNull(externalId, "externalId");
    return doGetSingle(externalId.toBundle(), null, null);
  }

  @Override
  public <T extends LegalEntity> T getSingle(ExternalId externalId, Class<T> type) {
    ArgumentChecker.notNull(externalId, "externalId");
    return doGetSingle(externalId.toBundle(), null, type);
  }

  @Override
  public LegalEntity getSingle(final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    return doGetSingle(bundle, null, null);
  }

  @Override
  public <T extends LegalEntity> T getSingle(ExternalIdBundle bundle, Class<T> type) {
    ArgumentChecker.notNull(bundle, "bundle");
    return doGetSingle(bundle, null, type);
  }

  @Override
  public LegalEntity getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    return doGetSingle(bundle, versionCorrection, null);
  }

  @Override
  public <T extends LegalEntity> T getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection, Class<T> type) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(type, "type");
    return doGetSingle(bundle, versionCorrection, type);
  }

  @SuppressWarnings("unchecked")
  protected <T extends LegalEntity> T doGetSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection, Class<T> type) {
    try {
      URI uri = DataLegalEntitySourceResource.uriSearchSingle(getBaseUri(), bundle, versionCorrection, type);
      LegalEntity legalEntity = accessRemote(uri).get(LegalEntity.class);
      if (type != null) {
        return type.cast(legalEntity);
      } else {
        return (T) legalEntity;
      }
    } catch (DataNotFoundException ex) {
      return null;
    } catch (UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public Map<ExternalIdBundle, LegalEntity> getSingle(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    // TODO: Implement this properly as a REST call
    return AbstractSourceWithExternalBundle.getSingle(this, bundles, versionCorrection);
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}
