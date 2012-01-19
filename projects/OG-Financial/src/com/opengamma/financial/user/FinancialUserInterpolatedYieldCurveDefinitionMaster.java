/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinitionDocument;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * Wraps a curve definition master to trap calls to record user based information to allow clean up and
 * hooks for access control logics if needed.
 */
public class FinancialUserInterpolatedYieldCurveDefinitionMaster extends AbstractFinancialUserService implements InterpolatedYieldCurveDefinitionMaster {

  /**
   * The underlying master.
   */
  private final InterpolatedYieldCurveDefinitionMaster _underlying;

  /**
   * Creates an instance.
   * 
   * @param client  the client, not null
   * @param underlying  the underlying master, not null
   */
  public FinancialUserInterpolatedYieldCurveDefinitionMaster(FinancialClient client, InterpolatedYieldCurveDefinitionMaster underlying) {
    super(client, FinancialUserDataType.INTERPOLATED_YIELD_CURVE_DEFINITION);
    _underlying = underlying;
  }

  //-------------------------------------------------------------------------
  @Override
  public YieldCurveDefinitionDocument add(YieldCurveDefinitionDocument document) {
    document = _underlying.add(document);
    if (document.getUniqueId() != null) {
      created(document.getUniqueId());
    }
    return document;
  }

  @Override
  public YieldCurveDefinitionDocument correct(YieldCurveDefinitionDocument document) {
    return _underlying.correct(document);
  }

  @Override
  public YieldCurveDefinitionDocument get(UniqueId uniqueId) {
    return _underlying.get(uniqueId);
  }

  @Override
  public YieldCurveDefinitionDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    return _underlying.get(objectId, versionCorrection);
  }

  @Override
  public void remove(UniqueId uniqueId) {
    _underlying.remove(uniqueId);
    deleted(uniqueId);
  }

  @Override
  public YieldCurveDefinitionDocument update(YieldCurveDefinitionDocument document) {
    return _underlying.update(document);
  }

  @Override
  public YieldCurveDefinitionDocument addOrUpdate(YieldCurveDefinitionDocument document) {
    document = _underlying.addOrUpdate(document);
    if (document.getUniqueId() != null) {
      created(document.getUniqueId());
    }
    return document;
  }

}
