/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.client;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.engine.view.ComputationResultListener;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.livedata.msg.UserPrincipal;

// NOTE jonathan 2010-09-03 -- need to verify the precise number of results received and the order of them. Mockito
// doesn't seem to offer this easily, so implemented mock listeners.

public class TestComputationResultListener implements ComputationResultListener {
  
  private List<ViewComputationResultModel> _resultsReceived = new ArrayList<ViewComputationResultModel>();

  @Override
  public void computationResultAvailable(ViewComputationResultModel resultModel) {
    _resultsReceived.add(resultModel);
  }

  @Override
  public UserPrincipal getUser() {
    return null;
  }
  
  public List<ViewComputationResultModel> popResults() {
    List<ViewComputationResultModel> results = _resultsReceived;
    _resultsReceived = new ArrayList<ViewComputationResultModel>();
    return results;
  }
  
}
