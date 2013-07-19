/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.cache;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractChangeProvidingMaster;

class CacheTestMaster implements AbstractChangeProvidingMaster<CacheTestDocument> {

  @Override
  public CacheTestDocument get(UniqueId uniqueId) {
    return null;
  }

  @Override
  public CacheTestDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    return null;
  }

  @Override
  public Map<UniqueId, CacheTestDocument> get(Collection<UniqueId> uniqueIds) {
    return null;
  }

  @Override
  public CacheTestDocument add(CacheTestDocument document) {
    return null;
  }

  @Override
  public CacheTestDocument update(CacheTestDocument document) {
    return null;
  }

  @Override
  public void remove(ObjectIdentifiable oid) {
  }

  @Override
  public CacheTestDocument correct(CacheTestDocument document) {
    return null;
  }

  @Override
  public List<UniqueId> replaceVersion(UniqueId uniqueId, List<CacheTestDocument> replacementDocuments) {
    return null;
  }

  @Override
  public List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<CacheTestDocument> replacementDocuments) {
    return null;
  }

  @Override
  public List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<CacheTestDocument> replacementDocuments) {
    return null;
  }

  @Override
  public UniqueId replaceVersion(CacheTestDocument replacementDocument) {
    return null;
  }

  @Override
  public void removeVersion(UniqueId uniqueId) {
  }

  @Override
  public UniqueId addVersion(ObjectIdentifiable objectId, CacheTestDocument documentToAdd) {
    return null;
  }

  @Override
  public ChangeManager changeManager() {
    return null;
  }

}
