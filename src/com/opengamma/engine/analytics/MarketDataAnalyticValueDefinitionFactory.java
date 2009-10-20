/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.Map;
import java.util.TreeMap;

import org.fudgemsg.FudgeMsg;

import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityKey;
import com.opengamma.engine.security.SecurityKeyImpl;
import com.opengamma.id.DomainSpecificIdentifier;

/**
 * A factory for analytic value definitions when live data is required
 * from an underlying market data source.
 * It will construct a definition which consists of the underlying keys to the market data sourcing
 * system combined with the {@link #MARKET_DATA_TYPE_NAME} field set to
 * {@link #HEADER_TYPE_VALUE}.
 *
 * @author kirk
 */
public final class MarketDataAnalyticValueDefinitionFactory {
  public static final String MARKET_DATA_TYPE_NAME = "Type";
  public static final String HEADER_TYPE_VALUE = "MarketDataHeader";
  
  private MarketDataAnalyticValueDefinitionFactory() {
  }
  
  public static AnalyticValueDefinition<FudgeMsg> constructHeaderDefinition(
      String key, Object value) {
    Map<String, Object> predicates = new TreeMap<String, Object>();
    predicates.put(MARKET_DATA_TYPE_NAME, HEADER_TYPE_VALUE);
    predicates.put(key, value);
    return new AnalyticValueDefinitionImpl<FudgeMsg>(predicates);
  }
  
  public static AnalyticValueDefinition<FudgeMsg> constructHeaderDefinition(
      Security security) {
    AnalyticValueDefinition<FudgeMsg> securityMarketDataDefinition = security.getMarketDataDefinition();
    if(securityMarketDataDefinition != null) {
      return securityMarketDataDefinition;
    }
    return constructHeaderDefinition(new SecurityKeyImpl(security.getIdentifiers()));
  }
  
  public static AnalyticValueDefinition<FudgeMsg> constructHeaderDefinition(
      SecurityKey secKey) {
    Map<String, Object> predicates = new TreeMap<String, Object>();
    predicates.put(MARKET_DATA_TYPE_NAME, HEADER_TYPE_VALUE);
    for(DomainSpecificIdentifier identifier : secKey.getIdentifiers()) {
      predicates.put(identifier.getDomain().getDomainName(), identifier.getValue());
    }
    return new AnalyticValueDefinitionImpl<FudgeMsg>(predicates);
  }
  
}
