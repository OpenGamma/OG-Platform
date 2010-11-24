/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import org.fudgemsg.FudgeContext;

import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.view.ManageableViewDefinitionRepository;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityMaster;

/**
 * Context/configuration for the objects to pass around.
 */
public class UsersResourceContext {

  private FudgeContext _fudgeContext;
  private PositionMaster _userPositionMaster;
  private SecurityMaster _userSecurityMaster;
  private ManageableViewDefinitionRepository _userViewDefinitionRepository;
  private InterpolatedYieldCurveDefinitionMaster _userInterpolatedYieldCurveDefinitionMaster;

  public UsersResourceContext() {
  }
  
  public void setUserPositionMaster(PositionMaster positionMaster) {
    _userPositionMaster = positionMaster;
  }
  
  public PositionMaster getPositionMaster() {
    return _userPositionMaster;
  }

  public void setUserInterpolatedYieldCurveDefinitionMaster(final InterpolatedYieldCurveDefinitionMaster userInterpolatedYieldCurveDefinitionMaster) {
    _userInterpolatedYieldCurveDefinitionMaster = userInterpolatedYieldCurveDefinitionMaster;
  }

  public InterpolatedYieldCurveDefinitionMaster getInterpolatedYieldCurveDefinitionMaster() {
    return _userInterpolatedYieldCurveDefinitionMaster;
  }

  public void setUserSecurityMaster(SecurityMaster securityMaster) {
    _userSecurityMaster = securityMaster;
  }

  public SecurityMaster getSecurityMaster() {
    return _userSecurityMaster;
  }

  public void setUserViewDefinitionRepository(ManageableViewDefinitionRepository viewDefinitionRepository) {
    _userViewDefinitionRepository = viewDefinitionRepository;
  }

  public ManageableViewDefinitionRepository getViewDefinitionRepository() {
    return _userViewDefinitionRepository;
  }

  public void setFudgeContext(final FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

}
