/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.client;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.engine.view.DeltaComputationResultListener;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.livedata.msg.UserPrincipal;

//NOTE jonathan 2010-09-03 -- need to verify the precise number of results received and the order of them. Mockito
//doesn't seem to offer this easily, so implemented mock listeners.

public class TestDeltaResultListener implements DeltaComputationResultListener {

  private List<ViewDeltaResultModel> _resultsReceived = new ArrayList<ViewDeltaResultModel>();
  
  @Override
  public void deltaResultAvailable(ViewDeltaResultModel deltaModel) {
    _resultsReceived.add(deltaModel);
  }

  @Override
  public UserPrincipal getUser() {
    return null;
  }

  public List<ViewDeltaResultModel> popResults() {
    List<ViewDeltaResultModel> results = _resultsReceived;
    _resultsReceived = new ArrayList<ViewDeltaResultModel>();
    return results;
  }
  
}