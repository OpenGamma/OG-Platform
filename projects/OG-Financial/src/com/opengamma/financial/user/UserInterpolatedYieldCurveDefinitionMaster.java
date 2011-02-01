/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinitionDocument;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.VersionCorrection;

/**
 * Wraps a curve definition master to trap calls to record user based information to allow clean up and
 * hooks for access control logics if needed.
 */
public class UserInterpolatedYieldCurveDefinitionMaster implements InterpolatedYieldCurveDefinitionMaster {

  private final UserDataTrackerWrapper _tracker;
  private final InterpolatedYieldCurveDefinitionMaster _underlying;

  public UserInterpolatedYieldCurveDefinitionMaster(final String userName, final String clientName, final UserDataTracker tracker, final InterpolatedYieldCurveDefinitionMaster underlying) {
    _tracker = new UserDataTrackerWrapper(tracker, userName, clientName, UserDataType.INTERPOLATED_YIELD_CURVE_DEFINITION);
    _underlying = underlying;
  }

  @Override
  public YieldCurveDefinitionDocument add(YieldCurveDefinitionDocument document) {
    document = _underlying.add(document);
    if (document.getUniqueId() != null) {
      _tracker.created(document.getUniqueId());
    }
    return document;
  }

  @Override
  public YieldCurveDefinitionDocument correct(YieldCurveDefinitionDocument document) {
    return _underlying.correct(document);
  }

  @Override
  public YieldCurveDefinitionDocument get(UniqueIdentifier uniqueId) {
    return _underlying.get(uniqueId);
  }

  @Override
  public YieldCurveDefinitionDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    return _underlying.get(objectId, versionCorrection);
  }

  @Override
  public void remove(UniqueIdentifier uniqueId) {
    _underlying.remove(uniqueId);
    _tracker.deleted(uniqueId);
  }

  @Override
  public YieldCurveDefinitionDocument update(YieldCurveDefinitionDocument document) {
    return _underlying.update(document);
  }

  @Override
  public YieldCurveDefinitionDocument addOrUpdate(YieldCurveDefinitionDocument document) {
    document = _underlying.addOrUpdate(document);
    if (document.getUniqueId() != null) {
      _tracker.created(document.getUniqueId());
    }
    return document;
  }

}
