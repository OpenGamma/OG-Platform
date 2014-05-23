/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata.faketicks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDomainIdentifierResolver;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * Decides based of security type whether to fake out subscriptions
 */
public class ByTypeFakeSubscriptionSelector implements FakeSubscriptionSelector {

  private final Set<String> _toFake;

  public ByTypeFakeSubscriptionSelector(String... toFakes) {
    this(Sets.newHashSet(toFakes));
  }

  public ByTypeFakeSubscriptionSelector(Set<String> toFake) {
    _toFake = toFake;
  }

  @Override
  public ObjectsPair<Collection<LiveDataSpecification>, Collection<LiveDataSpecification>> splitShouldFake(
      FakeSubscriptionBloombergLiveDataServer server, Collection<LiveDataSpecification> specs) {
    if (specs.isEmpty()) {
      return ObjectsPair.of((Collection<LiveDataSpecification>) new ArrayList<LiveDataSpecification>(), (Collection<LiveDataSpecification>) new ArrayList<LiveDataSpecification>());
    }
    
    Set<LiveDataSpecification> fakes = new HashSet<LiveDataSpecification>();
    Set<LiveDataSpecification> underlyings = new HashSet<LiveDataSpecification>();
    
    Map<LiveDataSpecification, DistributionSpecification> resolved = server.getDistributionSpecificationResolver().resolve(specs);
    
    Map<String, Set<LiveDataSpecification>> specsBySecurityId = new HashMap<String, Set<LiveDataSpecification>>();
    
    for (java.util.Map.Entry<LiveDataSpecification, DistributionSpecification> entry : resolved.entrySet()) {
      if (entry.getValue() == null) {
        //Subscription won't work.  Presume we want a real subscription.
        underlyings.add(entry.getKey());
        continue;
      }
      String buid = BloombergDomainIdentifierResolver.toBloombergKey(entry.getValue().getMarketDataId());
      Set<LiveDataSpecification> set = specsBySecurityId.get(buid);
      if (set == null) {
        set = new HashSet<LiveDataSpecification>();
        specsBySecurityId.put(buid, set);
      }
      set.add(entry.getKey());
    }
    
    Set<String> buids = specsBySecurityId.keySet();

    ReferenceDataProvider refDataProvider = server.getReferenceDataProvider();
    
    if (buids.size() > 0) {
      Map<String, String> values = refDataProvider.getReferenceDataValues(buids, BloombergConstants.FIELD_SECURITY_TYP);
      for (String buid : buids) {
        String type = values.get(buid);
        if (type != null && _toFake.contains(type)) {
          fakes.addAll(specsBySecurityId.get(buid));
        } else {
          underlyings.addAll(specsBySecurityId.get(buid));
        }
      }
    }

    return ObjectsPair.of((Collection<LiveDataSpecification>) underlyings, (Collection<LiveDataSpecification>) fakes);
  }

}
