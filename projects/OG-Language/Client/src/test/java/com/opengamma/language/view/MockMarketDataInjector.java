/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import java.util.ArrayList;
import java.util.Collection;

import com.opengamma.engine.marketdata.MarketDataInjector;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalId;
import com.opengamma.lambdava.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * Mock of the MarketDataInjector for testing.
 */
/* package */class MockMarketDataInjector implements MarketDataInjector {

  private final Collection<Pair<ValueRequirement, Object>> _addByValueRequirement = new ArrayList<Pair<ValueRequirement, Object>>();
  private final Collection<Triple<ExternalId, String, Object>> _addByValueName = new ArrayList<Triple<ExternalId, String, Object>>();
  private final Collection<ValueRequirement> _removeByValueRequirement = new ArrayList<ValueRequirement>();
  private final Collection<Pair<ExternalId, String>> _removeByValueName = new ArrayList<Pair<ExternalId, String>>();

  @Override
  public void addValue(ValueRequirement valueRequirement, Object value) {
    _addByValueRequirement.add(Pair.of(valueRequirement, value));
  }

  @Override
  public void addValue(ExternalId identifier, String valueName, Object value) {
    _addByValueName.add(Triple.of(identifier, valueName, value));
  }

  @Override
  public void removeValue(ValueRequirement valueRequirement) {
    _removeByValueRequirement.add(valueRequirement);
  }

  @Override
  public void removeValue(ExternalId identifier, String valueName) {
    _removeByValueName.add(Pair.of(identifier, valueName));
  }

  public Collection<Pair<ValueRequirement, Object>> getAddByValueRequirement() {
    final Collection<Pair<ValueRequirement, Object>> value = new ArrayList<Pair<ValueRequirement, Object>>(_addByValueRequirement);
    _addByValueRequirement.clear();
    return value;
  }

  public Collection<Triple<ExternalId, String, Object>> getAddByValueName() {
    final Collection<Triple<ExternalId, String, Object>> value = new ArrayList<Triple<ExternalId, String, Object>>(_addByValueName);
    _addByValueName.clear();
    return value;
  }

  public Collection<ValueRequirement> getRemoveByValueRequirement() {
    final Collection<ValueRequirement> value = new ArrayList<ValueRequirement>(_removeByValueRequirement);
    _removeByValueRequirement.clear();
    return value;
  }

  public Collection<Pair<ExternalId, String>> getRemoveByValueName() {
    final Collection<Pair<ExternalId, String>> value = new ArrayList<Pair<ExternalId, String>>(_removeByValueName);
    _removeByValueName.clear();
    return value;
  }

}
