/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.analyticservice;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.resolver.AbstractResolver;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Resolves 
 */
public class DefaultJmsTopicNameResolver extends AbstractResolver<JmsTopicNameResolveRequest, String> implements JmsTopicNameResolver {

  /** Logger **/
  private static final Logger s_logger = LoggerFactory.getLogger(DefaultJmsTopicNameResolver.class);
  
  private static final String PREFIX = "OGAnalytics";
  
  private final PositionMaster _positionMaster;
  
  public DefaultJmsTopicNameResolver(final PositionMaster positionMaster) {
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    _positionMaster = positionMaster;
  }

  @Override
  public String resolve(JmsTopicNameResolveRequest request) {
    ValueSpecification valueSpecification = request.getValueSpecification();
    ExternalId providerId = _positionMaster.get(getPositionId(valueSpecification.getTargetSpecification().getUniqueId())).getPosition().getProviderId();
    String result = PREFIX + SEPARATOR + providerId.getValue() + SEPARATOR + request.getCalcConfig() + SEPARATOR + request.getValueSpecification().getValueName();
    if (request.getValueSpecification().getProperties() != null) {
      result += SEPARATOR + request.getValueSpecification().getProperties().toSimpleString();
    }
    s_logger.debug("{} resolved to {}", request, result);
    return result;
    
  }
  
  private UniqueId getPositionId(final UniqueId uniqueId) {
    String[] schemes = StringUtils.split(uniqueId.getScheme(), '-');
    String[] values = StringUtils.split(uniqueId.getValue(), '-');
    String[] versions = Objects.firstNonNull(StringUtils.split(uniqueId.getVersion(), '-'), new String[] {null, null});
    if (schemes.length != 2 || values.length != 2 || versions.length != 2) {
      throw new IllegalArgumentException("Invalid position identifier for MasterPositionSource: " + uniqueId);
    }
    return UniqueId.of(schemes[1], values[1], versions[1]);
  }
  
  
}
