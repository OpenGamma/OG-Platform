/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.normalization.NormalizationRuleSet;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * This class produces a {@code DistributionSpecification}
 * using an underlying {@code IdResolver}, {@code NormalizationRuleResolver}, and
 * {@code JmsTopicNameResolver}.   
 */
public class DefaultDistributionSpecificationResolver 
  extends AbstractResolver<LiveDataSpecification, DistributionSpecification> 
  implements DistributionSpecificationResolver {
  
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
  public Map<LiveDataSpecification, DistributionSpecification> resolve(Collection<LiveDataSpecification> liveDataSpecifications) {
    
    ArgumentChecker.notNull(liveDataSpecifications, "Live Data specification");
    
    Collection<IdentifierBundle> identifierBundles = new ArrayList<IdentifierBundle>();
    Map<IdentifierBundle, Collection<LiveDataSpecification>> identifierBundle2LiveDataSpec = new HashMap<IdentifierBundle, Collection<LiveDataSpecification>>();
    Map<LiveDataSpecification, Identifier> liveDataSec2Identifier = new HashMap<LiveDataSpecification, Identifier>();
    Map<LiveDataSpecification, NormalizationRuleSet> liveDataSec2NormalizationRule = new HashMap<LiveDataSpecification, NormalizationRuleSet>();
    Collection<JmsTopicNameResolveRequest> jmsTopicNameRequests = new ArrayList<JmsTopicNameResolveRequest>();
    Map<JmsTopicNameResolveRequest, Collection<LiveDataSpecification>> jmsTopicNameRequest2LiveDataSec = new HashMap<JmsTopicNameResolveRequest, Collection<LiveDataSpecification>>();
    Map<LiveDataSpecification, String> liveDataSec2JmsTopicName = new HashMap<LiveDataSpecification, String>();
    
    for (LiveDataSpecification liveDataSpec : liveDataSpecifications) {
      identifierBundles.add(liveDataSpec.getIdentifiers());
      Collection<LiveDataSpecification> liveDataSpecs = identifierBundle2LiveDataSpec.get(liveDataSpec.getIdentifiers());
      if (liveDataSpecs == null) {
        liveDataSpecs = new ArrayList<LiveDataSpecification>();
        identifierBundle2LiveDataSpec.put(liveDataSpec.getIdentifiers(), liveDataSpecs);
      }
      liveDataSpecs.add(liveDataSpec);
      
      NormalizationRuleSet normalizationRule = _normalizationRuleResolver.resolve(liveDataSpec.getNormalizationRuleSetId());
      liveDataSec2NormalizationRule.put(liveDataSpec, normalizationRule);
    }
    
    Map<IdentifierBundle, Identifier> bundle2Identifier = _idResolver.resolve(identifierBundles);
    for (Map.Entry<IdentifierBundle, Identifier> entry : bundle2Identifier.entrySet()) {
      Collection<LiveDataSpecification> liveDataSpecsForBundle = identifierBundle2LiveDataSpec.get(entry.getKey());
      for (LiveDataSpecification liveDataSpecForBundle : liveDataSpecsForBundle) {
        liveDataSec2Identifier.put(liveDataSpecForBundle, entry.getValue());
      }
    }
      
    for (LiveDataSpecification liveDataSpec : liveDataSpecifications) {
      Identifier identifier = liveDataSec2Identifier.get(liveDataSpec);
      NormalizationRuleSet normalizationRule = liveDataSec2NormalizationRule.get(liveDataSpec);
      if (identifier == null || normalizationRule == null) {
        liveDataSec2JmsTopicName.put(liveDataSpec, null);       
      } else {
        JmsTopicNameResolveRequest jmsTopicNameRequest = new JmsTopicNameResolveRequest(identifier, normalizationRule);
        jmsTopicNameRequests.add(jmsTopicNameRequest);
        
        Collection<LiveDataSpecification> liveDataSpecs = jmsTopicNameRequest2LiveDataSec.get(jmsTopicNameRequest);
        if (liveDataSpecs == null) {
          liveDataSpecs = new ArrayList<LiveDataSpecification>();
          jmsTopicNameRequest2LiveDataSec.put(jmsTopicNameRequest, liveDataSpecs);
        }
        liveDataSpecs.add(liveDataSpec);
      }
    }
    
    Map<JmsTopicNameResolveRequest, String> jmsTopicNames = _jmsTopicNameResolver.resolve(jmsTopicNameRequests);
    for (Map.Entry<JmsTopicNameResolveRequest, String> entry : jmsTopicNames.entrySet()) {
      Collection<LiveDataSpecification> liveDataSpecsForRequest = jmsTopicNameRequest2LiveDataSec.get(entry.getKey());
      for (LiveDataSpecification liveDataSpecForRequest : liveDataSpecsForRequest) {
        liveDataSec2JmsTopicName.put(liveDataSpecForRequest, entry.getValue());
      }
    }
    
    Map<LiveDataSpecification, DistributionSpecification> returnValue = new HashMap<LiveDataSpecification, DistributionSpecification>();

    for (LiveDataSpecification liveDataSpec : liveDataSpecifications) {
      Identifier identifier = liveDataSec2Identifier.get(liveDataSpec);
      NormalizationRuleSet normalizationRule = liveDataSec2NormalizationRule.get(liveDataSpec);
      String jmsTopicName = liveDataSec2JmsTopicName.get(liveDataSpec);
      if (identifier == null || normalizationRule == null || jmsTopicName == null) {
        returnValue.put(liveDataSpec, null);
      } else {
        DistributionSpecification distributionSpec = new DistributionSpecification(
            identifier,
            normalizationRule,
            jmsTopicName);
        returnValue.put(liveDataSpec, distributionSpec);
      }
    }
    
    return returnValue;
  }

}
