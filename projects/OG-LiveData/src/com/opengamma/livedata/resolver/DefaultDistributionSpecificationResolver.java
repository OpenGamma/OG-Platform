/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import com.opengamma.id.Identifier;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.normalization.NormalizationRuleSet;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * This class produces a {@code DistributionSpecification}
 * using an underlying {@code IdResolver}, {@code NormalizationRuleResolver}, and
 * {@code JmsTopicNameResolver}.   
 */
public class DefaultDistributionSpecificationResolver implements DistributionSpecificationResolver {
  
  private final IdResolver _idResolver;
  private final NormalizationRuleResolver _normalizationRuleResolver;
  private final JmsTopicNameResolver _jmsTopicNameResolver;
  
  public DefaultDistributionSpecificationResolver(
      IdResolver idResolver,
      NormalizationRuleResolver normalizationRuleResolver,
      JmsTopicNameResolver jmsTopicNameResolver) {
    
    ArgumentChecker.notNull(idResolver, "ID Resolver");
    ArgumentChecker.notNull(normalizationRuleResolver, "Normalization rule resolver");
    ArgumentChecker.notNull(jmsTopicNameResolver, "JMS topic name resolver");
    
    _idResolver = idResolver;
    _normalizationRuleResolver = normalizationRuleResolver;
    _jmsTopicNameResolver = jmsTopicNameResolver;
  }

  @Override
  public DistributionSpecification getDistributionSpecification(
      LiveDataSpecification spec) {
    
    ArgumentChecker.notNull(spec, "Live Data specification");
    
    Identifier identifier = _idResolver.resolve(spec.getIdentifiers());
    if (identifier == null) {
      throw new IllegalArgumentException("ID cannot be resolved to a valid market data line: " + spec.getIdentifiers());       
    }
    
    NormalizationRuleSet normalizationRule = _normalizationRuleResolver.resolve(spec.getNormalizationRuleSetId());
    if (normalizationRule == null) {
      throw new IllegalArgumentException("Normalization rule not found: " + spec.getNormalizationRuleSetId());
    }
    
    String jmsTopic = _jmsTopicNameResolver.resolve(identifier, normalizationRule);
    
    DistributionSpecification distributionSpec = new DistributionSpecification(
        identifier,
        normalizationRule,
        jmsTopic);
    return distributionSpec;
  }

}
