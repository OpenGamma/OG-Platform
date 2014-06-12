/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.Permission;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.auth.AuthUtils;

/**
 * A decorator for a position master that applies permissions.
 * <p>
 * The class applies master-based permissions.
 * These are provided as static constants on this class and cover
 * the basic view, add, update and remove operations.
 */
public class PermissionedPositionMaster implements PositionMaster {

  /**
   * The permission object for viewing data.
   */
  public static final Permission PERMISSION_VIEW = AuthUtils.getPermissionResolver().resolvePermission("PositionMaster:view");
  /**
   * The permission object for adding data.
   */
  public static final Permission PERMISSION_ADD = AuthUtils.getPermissionResolver().resolvePermission("PositionMaster:edit:add");
  /**
   * The permission object for updating data.
   */
  public static final Permission PERMISSION_UPDATE = AuthUtils.getPermissionResolver().resolvePermission("PositionMaster:edit:update");
  /**
   * The permission object for removing data.
   */
  public static final Permission PERMISSION_REMOVE = AuthUtils.getPermissionResolver().resolvePermission("PositionMaster:edit:remove");
  /**
   * The permission object for correcting data.
   */
  public static final Permission PERMISSION_CORRECT = AuthUtils.getPermissionResolver().resolvePermission("PositionMaster:edit:correct");

  /**
   * The underlying position master.
   */
  private final PositionMaster _underlying;

  //-------------------------------------------------------------------------
  /**
   * Wraps an underlying master if appropriate.
   * <p>
   * No wrapping occurs if permissions are not in use.
   * 
   * @param underlying  the underlying master, not null
   * @return the master, not null
   */
  public static PositionMaster wrap(PositionMaster underlying) {
    if (AuthUtils.isPermissive()) {
      return underlying;
    }
    return new PermissionedPositionMaster(underlying);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   * 
   * @param underlying  the underlying position master, not null
   */
  public PermissionedPositionMaster(PositionMaster underlying) {
    _underlying = ArgumentChecker.notNull(underlying, "underlying");
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying position master.
   * 
   * @return the underlying master, not null
   */
  protected PositionMaster getUnderlying() {
    return _underlying;
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument get(UniqueId uniqueId) {
    AuthUtils.getSubject().checkPermission(PERMISSION_VIEW);
    return getUnderlying().get(uniqueId);
  }

  @Override
  public PositionDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    AuthUtils.getSubject().checkPermission(PERMISSION_VIEW);
    return getUnderlying().get(objectId, versionCorrection);
  }

  @Override
  public ManageableTrade getTrade(UniqueId tradeId) {
    AuthUtils.getSubject().checkPermission(PERMISSION_VIEW);
    return getUnderlying().getTrade(tradeId);
  }

  @Override
  public Map<UniqueId, PositionDocument> get(Collection<UniqueId> uniqueIds) {
    AuthUtils.getSubject().checkPermission(PERMISSION_VIEW);
    return getUnderlying().get(uniqueIds);
  }

  @Override
  public PositionSearchResult search(PositionSearchRequest request) {
    AuthUtils.getSubject().checkPermission(PERMISSION_VIEW);
    return getUnderlying().search(request);
  }

  @Override
  public PositionHistoryResult history(PositionHistoryRequest request) {
    AuthUtils.getSubject().checkPermission(PERMISSION_VIEW);
    return getUnderlying().history(request);
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return getUnderlying().changeManager();
  }

  @Override
  public PositionDocument add(PositionDocument document) {
    AuthUtils.getSubject().checkPermission(PERMISSION_ADD);
    return getUnderlying().add(document);
  }

  @Override
  public PositionDocument update(PositionDocument document) {
    AuthUtils.getSubject().checkPermission(PERMISSION_UPDATE);
    return getUnderlying().update(document);
  }

  @Override
  public void remove(ObjectIdentifiable oid) {
    AuthUtils.getSubject().checkPermission(PERMISSION_REMOVE);
    getUnderlying().remove(oid);
  }

  @Override
  public PositionDocument correct(PositionDocument document) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    return getUnderlying().correct(document);
  }

  @Override
  public List<UniqueId> replaceVersion(UniqueId uniqueId, List<PositionDocument> replacementDocuments) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    return getUnderlying().replaceVersion(uniqueId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<PositionDocument> replacementDocuments) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    return getUnderlying().replaceAllVersions(objectId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<PositionDocument> replacementDocuments) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    return getUnderlying().replaceVersions(objectId, replacementDocuments);
  }

  @Override
  public UniqueId replaceVersion(PositionDocument replacementDocument) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    return getUnderlying().replaceVersion(replacementDocument);
  }

  @Override
  public void removeVersion(UniqueId uniqueId) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    getUnderlying().removeVersion(uniqueId);
  }

  @Override
  public UniqueId addVersion(ObjectIdentifiable objectId, PositionDocument documentToAdd) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    return getUnderlying().addVersion(objectId, documentToAdd);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return String.format("%s[%s]", getClass().getSimpleName(), getUnderlying());
  }

}
