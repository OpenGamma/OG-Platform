/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.marketdata.MarketDataInjector;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Mock of the MarketDataInjector for testing.
 */
/* package */class MockMarketDataInjector implements MarketDataInjector {

  private final Map<ValueSpecification, Object> _addByValueSpecification = new HashMap<ValueSpecification, Object>();
  private final Map<ValueRequirement, Object> _addByValueRequirement = new HashMap<ValueRequirement, Object>();
  private final Set<ValueSpecification> _removeByValueSpecification = new HashSet<ValueSpecification>();
  private final Set<ValueRequirement> _removeByValueRequirement = new HashSet<ValueRequirement>();

  @Override
  public void addValue(final ValueSpecification valueSpecification, final Object value) {
    _addByValueSpecification.put(valueSpecification, value);
  }

  @Override
  public void addValue(final ValueRequirement valueRequirement, final Object value) {
    _addByValueRequirement.put(valueRequirement, value);
  }

  @Override
  public void removeValue(final ValueSpecification valueSpecification) {
    _removeByValueSpecification.add(valueSpecification);
  }

  @Override
  public void removeValue(final ValueRequirement valueRequirement) {
    _removeByValueRequirement.add(valueRequirement);
  }

  public Map<ValueSpecification, Object> getAddByValueSpecification() {
    return _addByValueSpecification;
  }

  public Map<ValueRequirement, Object> getAddByValueRequirement() {
    return _addByValueRequirement;
  }

  public Set<ValueSpecification> getRemoveByValueSpecification() {
    return _removeByValueSpecification;
  }

  public Set<ValueRequirement> getRemoveByValueRequirement() {
    return _removeByValueRequirement;
  }

}
