/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinitionDocument;
import com.opengamma.id.UniqueIdentifier;

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
  public YieldCurveDefinitionDocument get(UniqueIdentifier uid) {
    return _underlying.get(uid);
  }

  @Override
  public void remove(UniqueIdentifier uid) {
    _underlying.remove(uid);
    _tracker.deleted(uid);
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
