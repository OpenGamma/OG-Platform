/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.util.Map;

import com.opengamma.core.position.Trade;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;

/**
 * 
 */
public class RegressionIdPreProcessor {
  
  private final PositionMaster _positionMaster;

  public RegressionIdPreProcessor(PositionMaster positionMaster) {
    _positionMaster = positionMaster;
  }
  
  public void execute() {
    
    PositionSearchRequest request = new PositionSearchRequest();
    PositionSearchResult searchResult = _positionMaster.search(request);
    //TODO worry about older versions?
    
    for (PositionDocument doc : searchResult.getDocuments()) {
      ManageablePosition position = doc.getPosition();
      Map<String, String> positionAttributes = position.getAttributes();
      if (!positionAttributes.containsKey(DatabaseRestore.REGRESSION_ID)) {
        String regressionId = createRegressionId(position.getUniqueId());
        positionAttributes.put(DatabaseRestore.REGRESSION_ID, regressionId);
      }
      
      for (Trade trade : position.getTrades()) {
        Map<String, String> tradeAttributes = trade.getAttributes();
        if (!tradeAttributes.containsKey(DatabaseRestore.REGRESSION_ID)) {
          String regressionId2 = createRegressionId(trade.getUniqueId());
          tradeAttributes.put(DatabaseRestore.REGRESSION_ID, regressionId2);
        }
      }
      _positionMaster.update(doc);
    }
    
    
  }
  
  
  private String createRegressionId(UniqueId uniqueId) {
    return uniqueId.getObjectId().toString();
  }
  
}
