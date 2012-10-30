/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.resolver.DistributionSpecificationResolver;
import com.opengamma.livedata.resolver.NaiveDistributionSpecificationResolver;

/**
 * Will return null for IDs not in the domain, value otherwise
 */
public class MockDistributionSpecificationResolver implements DistributionSpecificationResolver {

  private final ExternalScheme _domain;
  private final DistributionSpecificationResolver _distributionSpecificationResolver;

  public MockDistributionSpecificationResolver(ExternalScheme domain) {
    this(domain, new NaiveDistributionSpecificationResolver());
  }
  public MockDistributionSpecificationResolver(ExternalScheme domain,
      DistributionSpecificationResolver distributionSpecificationResolver) {
    _domain = domain;
    _distributionSpecificationResolver = distributionSpecificationResolver;
  }

  @Override
  public DistributionSpecification resolve(LiveDataSpecification liveDataSpecificationFromClient) {
    String id = liveDataSpecificationFromClient.getIdentifier(_domain);
    if (id == null) {
      return null;
    } else {
      LiveDataSpecification inner = new LiveDataSpecification(
          liveDataSpecificationFromClient.getNormalizationRuleSetId(), Collections.singleton(ExternalId.of(_domain,
              id)));
      return _distributionSpecificationResolver.resolve(inner);
    }
  }

  @Override
  public Map<LiveDataSpecification, DistributionSpecification> resolve(
      Collection<LiveDataSpecification> liveDataSpecifications) {
    HashMap<LiveDataSpecification, DistributionSpecification> ret = new HashMap<LiveDataSpecification, DistributionSpecification>();
    for (LiveDataSpecification liveDataSpecification : liveDataSpecifications) {
      ret.put(liveDataSpecification, resolve(liveDataSpecification));
    }
    return ret;
  }
  
}
