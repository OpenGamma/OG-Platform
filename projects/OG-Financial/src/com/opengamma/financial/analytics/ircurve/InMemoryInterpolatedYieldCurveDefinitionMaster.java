/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.time.InstantProvider;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.common.Currency;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * An in-memory master for yield curve definitions, backed by a hash-map.
 */
public class InMemoryInterpolatedYieldCurveDefinitionMaster implements InterpolatedYieldCurveDefinitionMaster, InterpolatedYieldCurveDefinitionSource {
  
  /**
   * Default scheme used for identifiers created.
   */
  public static final String DEFAULT_SCHEME = "InMemoryInterpolatedYieldCurveDefinition";

  private final ConcurrentMap<Pair<Currency, String>, YieldCurveDefinition> _definitions = new ConcurrentHashMap<Pair<Currency, String>, YieldCurveDefinition>();

  private String _identifierScheme;

  public InMemoryInterpolatedYieldCurveDefinitionMaster() {
    setIdentifierScheme(DEFAULT_SCHEME);
  }

  public void setIdentifierScheme(final String identifierScheme) {
    ArgumentChecker.notNull(identifierScheme, "identifierScheme");
    _identifierScheme = identifierScheme;
  }

  public String getIdentifierScheme() {
    return _identifierScheme;
  }

  // InterpolatedYieldCurveDefinitionSource

  /**
   * Gets a yield curve definition for a currency and name.
   * @param currency  the currency, not null
   * @param name  the name, not null
   * @return the definition, null if not found
   */
  @Override
  public YieldCurveDefinition getDefinition(Currency currency, String name) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(name, "name");
    return _definitions.get(Pair.of(currency, name));
  }

  /**
   * Gets a yield curve definition for a currency, name and version.
   * @param currency  the currency, not null
   * @param name  the name, not null
   * @param version  the version instant, not null
   * @return the definition, null if not found
   */
  @Override
  public YieldCurveDefinition getDefinition(Currency currency, String name, InstantProvider version) {
    throw new UnsupportedOperationException();
  }

  // InterpolatedYieldCurveDefinitionMaster

  @Override
  public YieldCurveDefinitionDocument add(YieldCurveDefinitionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getYieldCurveDefinition(), "document.yieldCurveDefinition");
    final Currency currency = document.getYieldCurveDefinition().getCurrency();
    final String name = document.getYieldCurveDefinition().getName();
    if (_definitions.putIfAbsent(Pair.of(currency, name), document.getYieldCurveDefinition()) != null) {
      throw new IllegalArgumentException("Duplicate definition");
    }
    document.setUniqueId(UniqueIdentifier.of(getIdentifierScheme(), currency.getISOCode() + "_" + name));
    return document;
  }

  @Override
  public YieldCurveDefinitionDocument addOrUpdate(YieldCurveDefinitionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getYieldCurveDefinition(), "document.yieldCurveDefinition");
    final Currency currency = document.getYieldCurveDefinition().getCurrency();
    final String name = document.getYieldCurveDefinition().getName();
    _definitions.put(Pair.of(currency, name), document.getYieldCurveDefinition());
    document.setUniqueId(UniqueIdentifier.of(getIdentifierScheme(), currency.getISOCode() + "_" + name));
    return document;
  }

  @Override
  public YieldCurveDefinitionDocument correct(YieldCurveDefinitionDocument document) {
    throw new UnsupportedOperationException();
  }

  @Override
  public YieldCurveDefinitionDocument get(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    if (!uid.isLatest()) {
      throw new IllegalArgumentException("Only latest version supported by '" + getIdentifierScheme() + "'");
    }
    if (!getIdentifierScheme().equals(uid.getScheme())) {
      throw new DataNotFoundException("Scheme '" + uid.getScheme() + "' not valid for '" + getIdentifierScheme() + "'");
    }
    final int i = uid.getValue().indexOf('_');
    if (i <= 0) {
      throw new DataNotFoundException("Identifier '" + uid.getValue() + "' not valid for '" + getIdentifierScheme() + "'");
    }
    final String iso = uid.getValue().substring(0, i);
    final String name = uid.getValue().substring(i + 1);
    final Currency currency;
    try {
      currency = Currency.getInstance(iso);
    } catch (IllegalArgumentException e) {
      throw new DataNotFoundException("Identifier '" + uid.getValue() + "' not valid for '" + getIdentifierScheme() + "'", e);
    }
    final YieldCurveDefinition definition = getDefinition(currency, name);
    if (definition == null) {
      throw new DataNotFoundException("Curve definition not found");
    }
    return new YieldCurveDefinitionDocument(uid, definition);
  }

  @Override
  public void remove(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    if (!uid.isLatest()) {
      throw new IllegalArgumentException("Only latest version supported by '" + getIdentifierScheme() + "'");
    }
    if (!getIdentifierScheme().equals(uid.getScheme())) {
      throw new DataNotFoundException("Scheme '" + uid.getScheme() + "' not valid for '" + getIdentifierScheme() + "'");
    }
    final int i = uid.getValue().indexOf('_');
    if (i <= 0) {
      throw new DataNotFoundException("Identifier '" + uid.getValue() + "' not valid for '" + getIdentifierScheme() + "'");
    }
    final String iso = uid.getValue().substring(0, i);
    final String name = uid.getValue().substring(i + 1);
    final Currency currency;
    try {
      currency = Currency.getInstance(iso);
    } catch (IllegalArgumentException e) {
      throw new DataNotFoundException("Identifier '" + uid.getValue() + "' not valid for '" + getIdentifierScheme() + "'", e);
    }
    final YieldCurveDefinition definition = _definitions.remove(Pair.of(currency, name));
    if (definition == null) {
      throw new DataNotFoundException("Curve definition not found");
    }
  }

  @Override
  public YieldCurveDefinitionDocument update(YieldCurveDefinitionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getYieldCurveDefinition(), "document.yieldCurveDefinition");
    final Currency currency = document.getYieldCurveDefinition().getCurrency();
    final String name = document.getYieldCurveDefinition().getName();
    final UniqueIdentifier uid = UniqueIdentifier.of(getIdentifierScheme(), currency.getISOCode() + "_" + name);
    if (!uid.equals(document.getUniqueId())) {
      throw new IllegalArgumentException("Invalid unique identifier");
    }
    final Pair<Currency, String> key = Pair.of(currency, name);
    if (!_definitions.containsKey(key)) {
      throw new DataNotFoundException("UID '" + uid + "' not found");
    }
    _definitions.put(key, document.getYieldCurveDefinition());
    document.setUniqueId(uid);
    return document;
  }

}
