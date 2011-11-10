/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.position;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.Deal;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.util.ArgumentChecker;

/**
 * Trade Attributes Model to help with freemarker rendering.
 */
public class TradeAttributesModel {

  private Map<UniqueId, Map<String, Map<String, String>>> _attrMap = Maps.newHashMap();
  
  public TradeAttributesModel(final ManageablePosition position) {
    ArgumentChecker.notNull(position, "position");
    initialize(position);
  }

  private void initialize(final ManageablePosition position) {
    for (ManageableTrade trade : position.getTrades()) {
      Map<String, Map<String, String>> tradeAttr = Maps.newHashMap();
      
      Map<String, String> dealAttr = Maps.newHashMap();
      Map<String, String> userAttr = Maps.newHashMap();
      tradeAttr.put("deal", dealAttr);
      tradeAttr.put("user", userAttr);
      
      _attrMap.put(trade.getUniqueId(), tradeAttr);
      
      for (Entry<String, String> entry : trade.getAttributes().entrySet()) {
        String key = entry.getKey();
        String value = entry.getValue();
        if (key.startsWith(Deal.DEAL_PREFIX)) {
          dealAttr.put(key, value);
        } else {
          userAttr.put(key, value);
        }
      }
    }
  }

  /**
   * Gets the Deal attributes for a given trade uniqueId.
   * 
   * @param tradeId the tradeId
   * @return the deal attributes, not null
   */
  public Map<String, String> getDealAttributes(UniqueId tradeId) {
    Map<String, String> result = Maps.newHashMap();
    
    Map<String, Map<String, String>> attributes = _attrMap.get(tradeId);
    if (attributes != null) {
      result = attributes.get("deal");
    } 
    return result;
  }
  
  /**
   * Gets the User attributes for a given trade uniqueId.
   * 
   * @param tradeId the trade uniqueId
   * @return the user attributes, not null
   */
  public Map<String, String> getUserAttributes(UniqueId tradeId) {
    Map<String, String> result = Maps.newHashMap();
    Map<String, Map<String, String>> attributes = _attrMap.get(tradeId);
    if (attributes != null) {
      result = attributes.get("user");
    } 
    return result;
  }
}
