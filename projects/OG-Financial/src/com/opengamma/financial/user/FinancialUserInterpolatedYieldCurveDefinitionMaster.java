/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinitionDocument;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractChangeProvidingMaster;
import com.opengamma.master.ChangeProvidingDecorator;

/**
 * Wraps a curve definition master to trap calls to record user based information to allow clean up and
 * hooks for access control logics if needed.
 */
public class FinancialUserInterpolatedYieldCurveDefinitionMaster
    extends AbstractFinancialUserMaster<YieldCurveDefinitionDocument>
    implements InterpolatedYieldCurveDefinitionMaster {

  /**
   * The underlying master.
   */
  private final InterpolatedYieldCurveDefinitionMaster _underlying;
  /**
   * The change providing master.
   */
  private final AbstractChangeProvidingMaster<YieldCurveDefinitionDocument> _changeProvidingMaster;

  /**
   * Creates an instance.
   *
   * @param client  the client, not null
   * @param underlying  the underlying master, not null
   */
  public FinancialUserInterpolatedYieldCurveDefinitionMaster(FinancialClient client, InterpolatedYieldCurveDefinitionMaster underlying) {
    super(client, FinancialUserDataType.INTERPOLATED_YIELD_CURVE_DEFINITION);
    _underlying = underlying;
    _changeProvidingMaster = ChangeProvidingDecorator.wrap(underlying);
  }

  @Override
  public YieldCurveDefinitionDocument add(YieldCurveDefinitionDocument document) {
    return _changeProvidingMaster.add(document);
  }

  @Override
  public UniqueId addVersion(ObjectIdentifiable objectId, YieldCurveDefinitionDocument documentToAdd) {
    return _changeProvidingMaster.addVersion(objectId, documentToAdd);
  }

  @Override
  public YieldCurveDefinitionDocument correct(YieldCurveDefinitionDocument document) {
    return _changeProvidingMaster.correct(document);
  }

  @Override
  public YieldCurveDefinitionDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    return _changeProvidingMaster.get(objectId, versionCorrection);
  }

  @Override
  public YieldCurveDefinitionDocument get(UniqueId uniqueId) {
    return _changeProvidingMaster.get(uniqueId);
  }

  @Override
  public Map<UniqueId, YieldCurveDefinitionDocument> get(Collection<UniqueId> uniqueIds) {
    return _changeProvidingMaster.get(uniqueIds);
  }

  @Override
  public void remove(ObjectIdentifiable objectIdentifiable) {
    _changeProvidingMaster.remove(objectIdentifiable);
  }

  @Override
  public void removeVersion(UniqueId uniqueId) {
    _changeProvidingMaster.removeVersion(uniqueId);
  }

  @Override
  public List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<YieldCurveDefinitionDocument> replacementDocuments) {
    return _changeProvidingMaster.replaceAllVersions(objectId, replacementDocuments);
  }

  @Override
  public UniqueId replaceVersion(YieldCurveDefinitionDocument replacementDocument) {
    return _changeProvidingMaster.replaceVersion(replacementDocument);
  }

  @Override
  public List<UniqueId> replaceVersion(UniqueId uniqueId, List<YieldCurveDefinitionDocument> replacementDocuments) {
    return _changeProvidingMaster.replaceVersion(uniqueId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<YieldCurveDefinitionDocument> replacementDocuments) {
    return _changeProvidingMaster.replaceVersions(objectId, replacementDocuments);
  }

  @Override
  public YieldCurveDefinitionDocument update(YieldCurveDefinitionDocument document) {
    return _changeProvidingMaster.update(document);
  }

  @Override
  public ChangeManager changeManager() {
    return _changeProvidingMaster.changeManager();
  }

  @Override
  public YieldCurveDefinitionDocument addOrUpdate(YieldCurveDefinitionDocument document) {
    return _underlying.addOrUpdate(document);
  }
}
