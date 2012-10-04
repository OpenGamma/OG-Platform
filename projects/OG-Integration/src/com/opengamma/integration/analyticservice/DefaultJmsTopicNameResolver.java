/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.analyticservice;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.livedata.resolver.AbstractResolver;
import com.opengamma.util.ArgumentChecker;

/**
 * Resolves 
 */
public class DefaultJmsTopicNameResolver extends AbstractResolver<JmsTopicNameResolveRequest, String> implements JmsTopicNameResolver {
  
  /** Logger **/
  private static final Logger s_logger = LoggerFactory.getLogger(DefaultJmsTopicNameResolver.class);

  private static final String PROVIDER_ID_FIELD = "providerId";
  private static final String PREFIX = "OGAnalytics";
  private static final String MISSING_PROVIDER_ID = "UNKNOWN_ID";
  
  private final PositionSource _positionSource;
  
  public DefaultJmsTopicNameResolver(final PositionSource positionSource) {
    ArgumentChecker.notNull(positionSource, "positionSource");
    _positionSource = positionSource;
  }

  @Override
  public String resolve(JmsTopicNameResolveRequest request) {
    ValueSpecification valueSpecification = request.getValueSpecification();
    Position position = _positionSource.getPosition(valueSpecification.getTargetSpecification().getUniqueId());
    String providerId = getProviderId(position); 
    String result = PREFIX + SEPARATOR + providerId + SEPARATOR + request.getCalcConfig() + SEPARATOR + request.getValueSpecification().getValueName();
//    if (request.getValueSpecification().getProperties() != null) {
//      result += SEPARATOR + request.getValueSpecification().getProperties().toSimpleString();
//    }
    s_logger.debug("{} resolved to {}", request, result);
    return result;
    
  }

  private String getProviderId(final Position position) {
    String result = null;
    Map<String, String> positionAttrs = position.getAttributes();
    if (positionAttrs != null) {
      result = positionAttrs.get(PROVIDER_ID_FIELD);
      if (result == null) {
        Collection<Trade> trades = position.getTrades();
        if (trades != null) {
          for (Trade trade : trades) {
            if (trade != null) {
              Map<String, String> tradeAttrs = trade.getAttributes();
              result = tradeAttrs.get(PROVIDER_ID_FIELD);
              if (result != null) {
                break;
              }
            }
          }
        }
      }
    }
    if (result == null) {
      result = MISSING_PROVIDER_ID;
    } else {
      result = ExternalId.parse(result).getValue();
    }
    return result;
  }
  
}
