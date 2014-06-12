/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.portfolio.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.Permission;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.auth.AuthUtils;

/**
 * A decorator for a portfolio master that applies permissions.
 * <p>
 * Two kinds of permissions are applied by this class.
 * <p>
 * The class applies master-based permissions.
 * These are provided as static constants on this class and cover
 * the basic view, add, update and remove operations.
 */
public class PermissionedPortfolioMaster implements PortfolioMaster {

  /**
   * The permission object for viewing data.
   */
  public static final Permission PERMISSION_VIEW = AuthUtils.getPermissionResolver().resolvePermission("PortfolioMaster:view");
  /**
   * The permission object for adding data.
   */
  public static final Permission PERMISSION_ADD = AuthUtils.getPermissionResolver().resolvePermission("PortfolioMaster:edit:add");
  /**
   * The permission object for updating data.
   */
  public static final Permission PERMISSION_UPDATE = AuthUtils.getPermissionResolver().resolvePermission("PortfolioMaster:edit:update");
  /**
   * The permission object for removing data.
   */
  public static final Permission PERMISSION_REMOVE = AuthUtils.getPermissionResolver().resolvePermission("PortfolioMaster:edit:remove");
  /**
   * The permission object for correcting data.
   */
  public static final Permission PERMISSION_CORRECT = AuthUtils.getPermissionResolver().resolvePermission("PortfolioMaster:edit:correct");

  /**
   * The underlying portfolio master.
   */
  private final PortfolioMaster _underlying;

  //-------------------------------------------------------------------------
  /**
   * Wraps an underlying master if appropriate.
   * <p>
   * No wrapping occurs if permissions are not in use.
   * 
   * @param underlying  the underlying master, not null
   * @return the master, not null
   */
  public static PortfolioMaster wrap(PortfolioMaster underlying) {
    if (AuthUtils.isPermissive()) {
      return underlying;
    }
    return new PermissionedPortfolioMaster(underlying);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   * 
   * @param underlying  the underlying portfolio master, not null
   */
  public PermissionedPortfolioMaster(PortfolioMaster underlying) {
    _underlying = ArgumentChecker.notNull(underlying, "underlying");
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying portfolio master.
   * 
   * @return the underlying master, not null
   */
  protected PortfolioMaster getUnderlying() {
    return _underlying;
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioDocument get(UniqueId uniqueId) {
    AuthUtils.getSubject().checkPermission(PERMISSION_VIEW);
    return getUnderlying().get(uniqueId);
  }

  @Override
  public PortfolioDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    AuthUtils.getSubject().checkPermission(PERMISSION_VIEW);
    return getUnderlying().get(objectId, versionCorrection);
  }

  @Override
  public ManageablePortfolioNode getNode(UniqueId nodeId) {
    AuthUtils.getSubject().checkPermission(PERMISSION_VIEW);
    return getUnderlying().getNode(nodeId);
  }

  @Override
  public Map<UniqueId, PortfolioDocument> get(Collection<UniqueId> uniqueIds) {
    AuthUtils.getSubject().checkPermission(PERMISSION_VIEW);
    return getUnderlying().get(uniqueIds);
  }

  @Override
  public PortfolioSearchResult search(PortfolioSearchRequest request) {
    AuthUtils.getSubject().checkPermission(PERMISSION_VIEW);
    return getUnderlying().search(request);
  }

  @Override
  public PortfolioHistoryResult history(PortfolioHistoryRequest request) {
    AuthUtils.getSubject().checkPermission(PERMISSION_VIEW);
    return getUnderlying().history(request);
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return getUnderlying().changeManager();
  }

  @Override
  public PortfolioDocument add(PortfolioDocument document) {
    AuthUtils.getSubject().checkPermission(PERMISSION_ADD);
    return getUnderlying().add(document);
  }

  @Override
  public PortfolioDocument update(PortfolioDocument document) {
    AuthUtils.getSubject().checkPermission(PERMISSION_UPDATE);
    return getUnderlying().update(document);
  }

  @Override
  public void remove(ObjectIdentifiable oid) {
    AuthUtils.getSubject().checkPermission(PERMISSION_REMOVE);
    getUnderlying().remove(oid);
  }

  @Override
  public PortfolioDocument correct(PortfolioDocument document) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    return getUnderlying().correct(document);
  }

  @Override
  public List<UniqueId> replaceVersion(UniqueId uniqueId, List<PortfolioDocument> replacementDocuments) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    return getUnderlying().replaceVersion(uniqueId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<PortfolioDocument> replacementDocuments) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    return getUnderlying().replaceAllVersions(objectId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<PortfolioDocument> replacementDocuments) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    return getUnderlying().replaceVersions(objectId, replacementDocuments);
  }

  @Override
  public UniqueId replaceVersion(PortfolioDocument replacementDocument) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    return getUnderlying().replaceVersion(replacementDocument);
  }

  @Override
  public void removeVersion(UniqueId uniqueId) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    getUnderlying().removeVersion(uniqueId);
  }

  @Override
  public UniqueId addVersion(ObjectIdentifiable objectId, PortfolioDocument documentToAdd) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    return getUnderlying().addVersion(objectId, documentToAdd);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return String.format("%s[%s]", getClass().getSimpleName(), getUnderlying());
  }

}
