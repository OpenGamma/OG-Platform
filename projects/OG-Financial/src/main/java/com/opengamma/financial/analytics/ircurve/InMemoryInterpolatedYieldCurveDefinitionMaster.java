/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.MasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * An in-memory master for yield curve definitions, backed by a hash-map.
 */
public class InMemoryInterpolatedYieldCurveDefinitionMaster implements InterpolatedYieldCurveDefinitionMaster, InterpolatedYieldCurveDefinitionSource, ChangeProvider {

  /**
   * Default scheme used for identifiers created.
   */
  public static final String DEFAULT_SCHEME = "InMemoryInterpolatedYieldCurveDefinition";

  /**
   * The in-memory definitions.
   */
  private final Map<Pair<Currency, String>, TreeMap<Instant, YieldCurveDefinition>> _definitions = new HashMap<Pair<Currency, String>, TreeMap<Instant, YieldCurveDefinition>>();
  /**
   * The change manager.
   */
  private final ChangeManager _changeManager = new BasicChangeManager(); // TODO make possible to pass the change manager in constructor
  /**
   * The unique id scheme
   */
  private String _uniqueIdScheme;

  /**
   * Creates an instance.
   */
  public InMemoryInterpolatedYieldCurveDefinitionMaster() {
    setUniqueIdScheme(DEFAULT_SCHEME);
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the scheme in use for unique identifier.
   * 
   * @return the scheme, not null
   */
  public String getUniqueIdScheme() {
    return _uniqueIdScheme;
  }

  /**
   * Sets the scheme in use for unique identifier.
   * 
   * @param scheme the scheme for unique identifier, not null
   */
  public void setUniqueIdScheme(final String scheme) {
    ArgumentChecker.notNull(scheme, "scheme");
    _uniqueIdScheme = scheme;
  }

  //-------------------------------------------------------------------------
  @Override
  public synchronized YieldCurveDefinition getDefinition(Currency currency, String name) {
    return getDefinition(currency, name, VersionCorrection.LATEST);
  }

  @Override
  public YieldCurveDefinition getDefinition(final Currency currency, final String name, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(name, "name");
    final TreeMap<Instant, YieldCurveDefinition> definitions = _definitions.get(Pairs.of(currency, name));
    if (definitions == null) {
      return null;
    }
    final Map.Entry<Instant, YieldCurveDefinition> entry = (versionCorrection.getVersionAsOf() == null) ? definitions.lastEntry() : definitions
        .floorEntry(versionCorrection.getVersionAsOf());
    if (entry == null) {
      return null;
    }
    return entry.getValue();
  }

  //-------------------------------------------------------------------------
  @Override
  public synchronized YieldCurveDefinitionDocument add(YieldCurveDefinitionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getYieldCurveDefinition(), "document.yieldCurveDefinition");
    final Currency currency = document.getYieldCurveDefinition().getCurrency();
    final String name = document.getYieldCurveDefinition().getName();
    final Pair<Currency, String> key = Pairs.of(currency, name);
    if (_definitions.containsKey(key)) {
      throw new IllegalArgumentException("Duplicate definition");
    }
    final TreeMap<Instant, YieldCurveDefinition> value = new TreeMap<Instant, YieldCurveDefinition>();
    Instant now = Instant.now();
    value.put(now, document.getYieldCurveDefinition());
    _definitions.put(key, value);
    final UniqueId uid = UniqueId.of(getUniqueIdScheme(), name + "_" + currency.getCode());
    document.setUniqueId(uid);
    changeManager().entityChanged(ChangeType.ADDED, document.getObjectId(), document.getVersionFromInstant(), document.getVersionToInstant(), now);
    return document;
  }

  @Override
  public synchronized YieldCurveDefinitionDocument addOrUpdate(YieldCurveDefinitionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getYieldCurveDefinition(), "document.yieldCurveDefinition");
    final Currency currency = document.getYieldCurveDefinition().getCurrency();
    final String name = document.getYieldCurveDefinition().getName();
    final Pair<Currency, String> key = Pairs.of(currency, name);
    TreeMap<Instant, YieldCurveDefinition> value = _definitions.get(key);
    final UniqueId uid = UniqueId.of(getUniqueIdScheme(), name + "_" + currency.getCode());
    Instant now = Instant.now();
    if (value != null) {
      // TODO: Need to housekeep the map to release memory from old entries; this was previously done based on the latch version, but we've taken that out
      value.put(now, document.getYieldCurveDefinition());
      changeManager().entityChanged(ChangeType.CHANGED, uid.getObjectId(), null, null, now);
    } else {
      value = new TreeMap<Instant, YieldCurveDefinition>();
      value.put(now, document.getYieldCurveDefinition());
      _definitions.put(key, value);
      changeManager().entityChanged(ChangeType.ADDED, uid.getObjectId(), document.getVersionFromInstant(), document.getVersionToInstant(), now);
    }
    document.setUniqueId(uid);
    return document;
  }

  @Override
  public YieldCurveDefinitionDocument correct(YieldCurveDefinitionDocument document) {
    throw new UnsupportedOperationException();
  }

  @Override
  public synchronized YieldCurveDefinitionDocument get(UniqueId uid) {
    ArgumentChecker.notNull(uid, "objectIdentifiable");
    if (!uid.isLatest()) {
      throw new IllegalArgumentException("Only latest version supported by '" + getUniqueIdScheme() + "'");
    }
    if (!getUniqueIdScheme().equals(uid.getScheme())) {
      throw new DataNotFoundException("Scheme '" + uid.getScheme() + "' not valid for '" + getUniqueIdScheme() + "'");
    }
    final int i = uid.getValue().indexOf('_');
    if (i <= 0) {
      throw new DataNotFoundException("Identifier '" + uid.getValue() + "' not valid for '" + getUniqueIdScheme() + "'");
    }
    final String name = uid.getValue().substring(0, i);
    final String iso = uid.getValue().substring(i + 1);
    final Currency currency;
    try {
      currency = Currency.of(iso);
    } catch (IllegalArgumentException e) {
      throw new DataNotFoundException("Identifier '" + uid.getValue() + "' not valid for '" + getUniqueIdScheme() + "'", e);
    }
    final TreeMap<Instant, YieldCurveDefinition> definitions = _definitions.get(Pairs.of(currency, name));
    if (definitions == null) {
      throw new DataNotFoundException("Curve definition not found");
    }
    final YieldCurveDefinition definition = definitions.lastEntry().getValue();
    if (definition == null) {
      throw new DataNotFoundException("Curve definition not found");
    }
    return new YieldCurveDefinitionDocument(uid, definition);
  }

  @Override
  public synchronized YieldCurveDefinitionDocument get(ObjectIdentifiable objectIdable, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectIdable, "objectIdable");
    ObjectId objectId = objectIdable.getObjectId();
    if (!getUniqueIdScheme().equals(objectId.getScheme())) {
      throw new DataNotFoundException("Scheme '" + objectId.getScheme() + "' not valid for '" + getUniqueIdScheme() + "'");
    }
    final int i = objectId.getValue().indexOf('_');
    if (i <= 0) {
      throw new DataNotFoundException("Identifier '" + objectId.getValue() + "' not valid for '" + getUniqueIdScheme() + "'");
    }
    final String name = objectId.getValue().substring(0, i);
    final String iso = objectId.getValue().substring(i + 1);
    final Currency currency;
    try {
      currency = Currency.of(iso);
    } catch (IllegalArgumentException e) {
      throw new DataNotFoundException("Identifier '" + objectId.getValue() + "' not valid for '" + getUniqueIdScheme() + "'", e);
    }
    final TreeMap<Instant, YieldCurveDefinition> definitions = _definitions.get(Pairs.of(currency, name));
    if (definitions == null) {
      throw new DataNotFoundException("Curve definition not found");
    }
    final YieldCurveDefinition definition = definitions.lastEntry().getValue();
    if (definition == null) {
      throw new DataNotFoundException("Curve definition not found");
    }
    return new YieldCurveDefinitionDocument(objectId.atLatestVersion(), definition);
  }

  @Override
  public synchronized void remove(ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");
    if (!getUniqueIdScheme().equals(objectIdentifiable.getObjectId().getScheme())) {
      throw new DataNotFoundException("Scheme '" + objectIdentifiable.getObjectId().getScheme() + "' not valid for '" + getUniqueIdScheme() + "'");
    }
    final int i = objectIdentifiable.getObjectId().getValue().indexOf('_');
    if (i <= 0) {
      throw new DataNotFoundException("Identifier '" + objectIdentifiable.getObjectId().getValue() + "' not valid for '" + getUniqueIdScheme() + "'");
    }
    final String name = objectIdentifiable.getObjectId().getValue().substring(0, i);
    final String iso = objectIdentifiable.getObjectId().getValue().substring(i + 1);
    final Currency currency;
    try {
      currency = Currency.of(iso);
    } catch (IllegalArgumentException e) {
      throw new DataNotFoundException("Identifier '" + objectIdentifiable.getObjectId().getValue() + "' not valid for '" + getUniqueIdScheme() + "'", e);
    }
    final Pair<Currency, String> key = Pairs.of(currency, name);
    // TODO: Need to housekeep the map to release memory from old entries; this was previously done based on the latch version, but we've taken that out
    final TreeMap<Instant, YieldCurveDefinition> value = _definitions.get(key);
    if (value == null) {
      throw new DataNotFoundException("Curve definition not found");
    }
    // Store a null to indicate the delete
    final Instant now = Instant.now();
    value.put(now, null);
    changeManager().entityChanged(ChangeType.REMOVED, objectIdentifiable.getObjectId(), null, null, now);
  }

  @Override
  public synchronized YieldCurveDefinitionDocument update(YieldCurveDefinitionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getYieldCurveDefinition(), "document.yieldCurveDefinition");
    final Currency currency = document.getYieldCurveDefinition().getCurrency();
    final String name = document.getYieldCurveDefinition().getName();
    final UniqueId uid = UniqueId.of(getUniqueIdScheme(), name + "_" + currency.getCode());
    if (!uid.equals(document.getUniqueId())) {
      throw new IllegalArgumentException("Invalid unique identifier");
    }
    final Pair<Currency, String> key = Pairs.of(currency, name);
    final TreeMap<Instant, YieldCurveDefinition> value = _definitions.get(key);
    if (value == null) {
      throw new DataNotFoundException("UID '" + uid + "' not found");
    }
    // TODO: Need to housekeep the map to release memory from old entries; this was previously done based on the latch version, but we've taken that out
    Instant now = Instant.now();
    value.put(now, document.getYieldCurveDefinition());
    document.setUniqueId(uid);
    changeManager().entityChanged(ChangeType.CHANGED, uid.getObjectId(), null, null, now);
    return document;
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  @Override
  public List<UniqueId> replaceVersions(ObjectIdentifiable objectIdentifiable, List<YieldCurveDefinitionDocument> replacementDocuments) {
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");

    final Instant now = Instant.now();

    for (YieldCurveDefinitionDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "document");
      ArgumentChecker.notNull(replacementDocument.getYieldCurveDefinition(), "document.yieldCurveDefinition");
      final Currency currency = replacementDocument.getYieldCurveDefinition().getCurrency();
      final String name = replacementDocument.getYieldCurveDefinition().getName();
      final UniqueId id = UniqueId.of(getUniqueIdScheme(), name + "_" + currency.getCode());
      ArgumentChecker.isTrue(id.equals(objectIdentifiable), "Invalid object identifier");
    }

    YieldCurveDefinitionDocument storedDocument = get(objectIdentifiable, null);
    if (storedDocument == null) {
      throw new DataNotFoundException("Document not found: " + objectIdentifiable);
    }
    final Currency currency = storedDocument.getYieldCurveDefinition().getCurrency();
    final String name = storedDocument.getYieldCurveDefinition().getName();
    Pair<Currency, String> key = Pairs.of(currency, name);

    final TreeMap<Instant, YieldCurveDefinition> value = _definitions.get(key);
    if (value == null) {
      throw new DataNotFoundException("OID '" + objectIdentifiable + "' not found");
    }
    // TODO: Need to housekeep the map to release memory from old entries; this was previously done based on the latch version, but we've taken that out

    Instant lowestCurrentVersionFrom = value.firstKey();

    List<YieldCurveDefinitionDocument> orderedReplacementDocuments = MasterUtils.adjustVersionInstants(now, lowestCurrentVersionFrom, null, replacementDocuments);

    final Instant lowestVersionFrom = orderedReplacementDocuments.get(0).getVersionFromInstant();
    final Instant highestVersionTo = orderedReplacementDocuments.get(orderedReplacementDocuments.size() - 1).getVersionToInstant();

    if (orderedReplacementDocuments.size() > 0) {
      value.subMap(lowestVersionFrom, true, highestVersionTo, false).clear();
    }

    for (YieldCurveDefinitionDocument replacementDocument : orderedReplacementDocuments) {
      value.put(replacementDocument.getVersionFromInstant(), replacementDocument.getYieldCurveDefinition());
      changeManager().entityChanged(ChangeType.CHANGED, replacementDocument.getObjectId(), null, null, now);
    }
    return MasterUtils.mapToUniqueIDs(orderedReplacementDocuments);

  }

  @Override
  public List<UniqueId> replaceAllVersions(ObjectIdentifiable objectIdentifiable, List<YieldCurveDefinitionDocument> replacementDocuments) {
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");

    final Instant now = Instant.now();

    for (YieldCurveDefinitionDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "document");
      ArgumentChecker.notNull(replacementDocument.getYieldCurveDefinition(), "document.yieldCurveDefinition");
      final Currency currency = replacementDocument.getYieldCurveDefinition().getCurrency();
      final String name = replacementDocument.getYieldCurveDefinition().getName();
      final UniqueId id = UniqueId.of(getUniqueIdScheme(), name + "_" + currency.getCode());
      ArgumentChecker.isTrue(id.equals(objectIdentifiable), "Invalid object identifier");
    }

    YieldCurveDefinitionDocument storedDocument = get(objectIdentifiable, null);
    if (storedDocument == null) {
      throw new DataNotFoundException("Document not found: " + objectIdentifiable);
    }
    final Currency currency = storedDocument.getYieldCurveDefinition().getCurrency();
    final String name = storedDocument.getYieldCurveDefinition().getName();
    Pair<Currency, String> key = Pairs.of(currency, name);

    final TreeMap<Instant, YieldCurveDefinition> value = _definitions.get(key);
    if (value == null) {
      throw new DataNotFoundException("OID '" + objectIdentifiable + "' not found");
    }
    value.clear();

    List<YieldCurveDefinitionDocument> orderedReplacementDocuments = MasterUtils.adjustVersionInstants(now, null, null, replacementDocuments);

    final Instant lowestVersionFrom = orderedReplacementDocuments.get(0).getVersionFromInstant();

    ArgumentChecker.notNull(lowestVersionFrom, "You must define version from of the first document");

    for (YieldCurveDefinitionDocument replacementDocument : orderedReplacementDocuments) {
      value.put(replacementDocument.getVersionFromInstant(), replacementDocument.getYieldCurveDefinition());
      changeManager().entityChanged(ChangeType.CHANGED, replacementDocument.getObjectId(), null, null, now);
    }
    return MasterUtils.mapToUniqueIDs(orderedReplacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersion(UniqueId uniqueId, List<YieldCurveDefinitionDocument> replacementDocuments) {
    return replaceVersions(uniqueId.getObjectId(), replacementDocuments);
  }

  @Override
  public void removeVersion(final UniqueId uniqueId) {
    replaceVersion(uniqueId, Collections.<YieldCurveDefinitionDocument>emptyList());
  }

  @Override
  public UniqueId replaceVersion(YieldCurveDefinitionDocument replacementDocument) {
    List<UniqueId> result = replaceVersion(replacementDocument.getUniqueId(), Collections.singletonList(replacementDocument));
    if (result.isEmpty()) {
      return null;
    } else {
      return result.get(0);
    }
  }

  @Override
  public UniqueId addVersion(ObjectIdentifiable objectId, YieldCurveDefinitionDocument documentToAdd) {
    List<UniqueId> result = replaceVersions(objectId, Collections.singletonList(documentToAdd));
    if (result.isEmpty()) {
      return null;
    } else {
      return result.get(0);
    }
  }

  @Override
  public Map<UniqueId, YieldCurveDefinitionDocument> get(Collection<UniqueId> uniqueIds) {
    Map<UniqueId, YieldCurveDefinitionDocument> map = newHashMap();
    for (UniqueId uniqueId : uniqueIds) {
      map.put(uniqueId, get(uniqueId));
    }
    return map;
  }
}
