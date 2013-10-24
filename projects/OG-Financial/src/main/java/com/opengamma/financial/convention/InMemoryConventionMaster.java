/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.threeten.bp.Instant;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.percurrency.EUConventions;
import com.opengamma.financial.convention.percurrency.JPConventions;
import com.opengamma.financial.convention.percurrency.USConventions;
import com.opengamma.financial.convention.percurrency.KRConventions;
import com.opengamma.financial.convention.percurrency.ZAConventions;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * In-memory convention master.
 *
 * Versioning is *NOT* supported.
 * Attempting a version correction will throw an exception.
 * Only VersionCorrection.LATEST is supported for retrieval.
 */
public class InMemoryConventionMaster implements ConventionMaster {
  /** In-memory scheme */
  public static final ExternalScheme IN_MEMORY_UNIQUE_SCHEME = ExternalScheme.of("In-memory");
  /** Maps from external ids to unique ids */
  private final ExternalIdBundleMapper<Convention> _mapper = new ExternalIdBundleMapper<>(IN_MEMORY_UNIQUE_SCHEME.getName());
  /** Change manager */
  private final ChangeManager _changeManager = new BasicChangeManager();

  /**
   * Initializes the conventions.
   */
  public InMemoryConventionMaster() {
    init();
  }

  /**
   * Initializes the convention master.
   */
  protected void init() {
    addFXConventions();
    EUConventions.addFixedIncomeInstrumentConventions(this);
    JPConventions.addFixedIncomeInstrumentConventions(this);
    USConventions.addFixedIncomeInstrumentConventions(this);
    ZAConventions.addFixedIncomeInstrumentConventions(this);
    KRConventions.addFixedIncomeInstrumentConventions(this);
  }

  private void addFXConventions() {
    final ExternalId us = ExternalSchemes.financialRegionId("US");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final FXSpotConvention usdCadSpot = new FXSpotConvention("USD/CAD FX Spot", ExternalIdBundle.of(ExternalId.of("CONVENTION", "USD/CAD FX Spot")), 1, us);
    final FXForwardAndSwapConvention usdCadForward = new FXForwardAndSwapConvention("USD/CAD FX Forward", ExternalIdBundle.of(ExternalId.of("CONVENTION", "USD/CAD FX Forward")),
        ExternalId.of("CONVENTION", "USD/CAD FX Spot"), following, false, us);
    final FXSpotConvention fxSpot = new FXSpotConvention("FX Spot", ExternalIdBundle.of(ExternalId.of("CONVENTION", "FX Spot")), 2, us);
    // TODO: Holiday should not be US only.
    final FXForwardAndSwapConvention fxForward = new FXForwardAndSwapConvention("FX Forward", ExternalIdBundle.of(ExternalId.of("CONVENTION", "FX Forward")),
        ExternalId.of("CONVENTION", "FX Spot"), following, false, us);
    add(usdCadSpot);
    add(usdCadForward);
    add(fxSpot);
    add(fxForward);
  }

  @Override
  public ConventionSearchResult searchConvention(final ConventionSearchRequest searchRequest) {
    final Collection<Convention> collection = _mapper.get(searchRequest.getIdentifiers());
    return new ConventionSearchResult(wrapConventionsWithDocuments(collection));
  }

  @Override
  public ConventionSearchResult searchHistoricalConvention(final ConventionSearchHistoricRequest searchRequest) {
    final Collection<Convention> collection = _mapper.get(searchRequest.getIdentifiers());
    return new ConventionSearchResult(wrapConventionsWithDocuments(collection));
  }

  @Override
  public ConventionDocument getConvention(final UniqueId uniqueId) {
    return new ConventionDocument(_mapper.get(uniqueId));
  }

  @Override
  public UniqueId add(final Convention convention) {
    final UniqueId uid = _mapper.add(convention.getExternalIdBundle(), convention);
    convention.setUniqueId(uid);
    _changeManager.entityChanged(ChangeType.ADDED, convention.getUniqueId().getObjectId(), Instant.now(), null, Instant.now());
    return uid;
  }

  private static Collection<ConventionDocument> wrapConventionsWithDocuments(final Collection<Convention> collection) {
    final Collection<ConventionDocument> results = new ArrayList<>(collection.size());
    for (final Convention convention : collection) {
      results.add(new ConventionDocument(convention));
    }
    return results;
  }

  @Override
  public ConventionDocument get(final UniqueId uniqueId) {
    return getConvention(uniqueId);
  }

  @Override
  public ConventionDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    if (!VersionCorrection.LATEST.equals(versionCorrection)) {
      throw new UnsupportedOperationException("InMemoryConventionMaster only supports VersionCorrection.LATEST");
    }
    return getConvention(UniqueId.of(objectId.getObjectId(), null));
  }

  @Override
  public Map<UniqueId, ConventionDocument> get(final Collection<UniqueId> uniqueIds) {
    final Map<UniqueId, ConventionDocument> map = new HashMap<>();
    for (final UniqueId id : uniqueIds) {
      final ConventionDocument doc = getConvention(id);
      map.put(id, doc);
    }
    return map;
  }

  @Override
  public ConventionDocument add(final ConventionDocument document) {
    add(document.getConvention());
    return new ConventionDocument(document.getConvention());
  }

  @Override
  public ConventionDocument update(final ConventionDocument document) {
    add(document.getConvention());
    return new ConventionDocument(document.getConvention());
  }

  @Override
  public void remove(final ObjectIdentifiable oid) {
    throw new UnsupportedOperationException("InMemoryConventionMaster does not support deletion.");
  }

  @Override
  public ConventionDocument correct(final ConventionDocument document) {
    throw new UnsupportedOperationException("InMemoryConventionMaster does not support versioning.");
  }

  @Override
  public List<UniqueId> replaceVersion(final UniqueId uniqueId, final List<ConventionDocument> replacementDocuments) {
    throw new UnsupportedOperationException("InMemoryConventionMaster does not support versioning.");
  }

  @Override
  public List<UniqueId> replaceAllVersions(final ObjectIdentifiable objectId, final List<ConventionDocument> replacementDocuments) {
    throw new UnsupportedOperationException("InMemoryConventionMaster does not support versioning.");
  }

  @Override
  public List<UniqueId> replaceVersions(final ObjectIdentifiable objectId, final List<ConventionDocument> replacementDocuments) {
    throw new UnsupportedOperationException("InMemoryConventionMaster does not support versioning.");
  }

  @Override
  public UniqueId replaceVersion(final ConventionDocument replacementDocument) {
    throw new UnsupportedOperationException("InMemoryConventionMaster does not support versioning.");
  }

  @Override
  public void removeVersion(final UniqueId uniqueId) {
    throw new UnsupportedOperationException("InMemoryConventionMaster does not support versioning.");
  }

  @Override
  public UniqueId addVersion(final ObjectIdentifiable objectId, final ConventionDocument documentToAdd) {
    throw new UnsupportedOperationException("InMemoryConventionMaster does not support versioning.");
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }
}
