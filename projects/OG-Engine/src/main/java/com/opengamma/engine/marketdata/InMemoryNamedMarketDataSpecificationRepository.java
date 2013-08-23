/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * A repository of fixed, existing market data providers for given specifications.
 * 
 * @deprecated  This is only required for the legacy analytics UI.
 */
@Deprecated
public class InMemoryNamedMarketDataSpecificationRepository implements NamedMarketDataSpecificationRepository {
  
  private final Map<String, MarketDataSpecification> _nameToSpec = new LinkedHashMap<String, MarketDataSpecification>();
  
  /**
   * Constructs an empty instance.
   */
  public InMemoryNamedMarketDataSpecificationRepository() {
  }
  
  /**
   * Constructs an instance containing a collection of named providers.
   * 
   * @param specifications  the fixed collection of named specifications, not null
   */
  public InMemoryNamedMarketDataSpecificationRepository(Collection<Pair<String, MarketDataSpecification>> specifications) {
    ArgumentChecker.notNull(specifications, "specifications");
    for (Pair<String, MarketDataSpecification> specification : specifications) {
      addSpecification(specification.getFirst(), specification.getSecond());
    }
  }

  public void addSpecification(String name, MarketDataSpecification specification) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(specification, "specification");
    _nameToSpec.put(name, specification);
  }
  
  //-------------------------------------------------------------------------
  @Override
  public List<String> getNames() {
    return new ArrayList<String>(_nameToSpec.keySet());
  }
  
  @Override
  public MarketDataSpecification getSpecification(String providerName) {
    MarketDataSpecification spec = _nameToSpec.get(providerName);
    if (spec == null) {
      throw new IllegalArgumentException("No provider with name '" + providerName + "' is registered with the repository");
    }
    return spec;
  }
  
}
