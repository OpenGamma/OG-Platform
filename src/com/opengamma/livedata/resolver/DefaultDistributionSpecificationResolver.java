/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import com.opengamma.id.DomainSpecificIdentifiers;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.normalization.NormalizationRuleSet;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author pietari
 */
public class DefaultDistributionSpecificationResolver implements DistributionSpecificationResolver {
  
  private final IdResolver _idResolver;
  private final NormalizationRuleResolver _normalizationRuleResolver;
  private final JmsTopicNameResolver _jmsTopicNameResolver;
  
  public DefaultDistributionSpecificationResolver(
      IdResolver idResolver,
      NormalizationRuleResolver normalizationRuleResolver,
      JmsTopicNameResolver jmsTopicNameResolver) {
    
    ArgumentChecker.checkNotNull(idResolver, "ID Resolver");
    ArgumentChecker.checkNotNull(normalizationRuleResolver, "Normalization rule resolver");
    ArgumentChecker.checkNotNull(jmsTopicNameResolver, "JMS topic name resolver");
    
    _idResolver = idResolver;
    _normalizationRuleResolver = normalizationRuleResolver;
    _jmsTopicNameResolver = jmsTopicNameResolver;
  }

  @Override
  public DistributionSpecification getDistributionSpecification(
      LiveDataSpecification spec) {
    
    ArgumentChecker.checkNotNull(spec, "Live Data specification");
    
    DomainSpecificIdentifiers identifiers = _idResolver.resolve(spec.getIdentifiers());
    if (identifiers == null) {
      throw new IllegalArgumentException("ID cannot be resolved to a valid market data line: " + spec.getIdentifiers());       
    }
    
    NormalizationRuleSet normalizationRule = _normalizationRuleResolver.resolve(spec.getNormalizationRuleSetId());
    if (normalizationRule == null) {
      throw new IllegalArgumentException("Normalization rule not found: " + spec.getNormalizationRuleSetId());
    }
    
    String jmsTopic = _jmsTopicNameResolver.resolve(identifiers, normalizationRule);
    
    DistributionSpecification distributionSpec = new DistributionSpecification(
        identifiers,
        normalizationRule,
        jmsTopic);
    return distributionSpec;
  }

}
