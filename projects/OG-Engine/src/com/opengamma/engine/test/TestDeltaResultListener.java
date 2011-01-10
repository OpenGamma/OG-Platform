/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import com.opengamma.engine.view.DeltaComputationResultListener;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.livedata.UserPrincipal;

public class TestDeltaResultListener extends AbstractTestResultListener<ViewDeltaResultModel>
    implements DeltaComputationResultListener {

  @Override
  public void deltaResultAvailable(ViewDeltaResultModel deltaModel) {
    resultReceived(deltaModel);
  }

  @Override
  public UserPrincipal getUser() {
    return UserPrincipal.getLocalUser();
  }

}