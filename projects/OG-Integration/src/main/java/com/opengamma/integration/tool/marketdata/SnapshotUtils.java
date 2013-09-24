/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.marketdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;

/**
 * Utility class to provide services to snapshot command line tools (and potentially UI tools too).
 */
public final class SnapshotUtils {
  private static final Logger s_logger = LoggerFactory.getLogger(SnapshotUtils.class);
  
  private MarketDataSnapshotMaster _snapshotMaster;

  private SnapshotUtils(MarketDataSnapshotMaster snapshotMaster) {
    _snapshotMaster = snapshotMaster;
  }
  
  public static SnapshotUtils of(MarketDataSnapshotMaster snapshotMaster) {
    return new SnapshotUtils(snapshotMaster);
  }

  private static String getSnapshotNameId(MarketDataSnapshotDocument doc) {
    return doc.getUniqueId() + " - " + doc.getName();
  }

  /**
   * Get a list of all available snapshots
   * @return the list of all available snapshot ids and names or an empty list if no snapshots found
   */
  public List<String> allSnapshots() {
    MarketDataSnapshotSearchRequest searchRequest = new MarketDataSnapshotSearchRequest();
    searchRequest.setIncludeData(false);
    MarketDataSnapshotSearchResult searchResult = _snapshotMaster.search(searchRequest);
    List<String> results = new ArrayList<>();
    for (MarketDataSnapshotDocument doc : searchResult.getDocuments()) {
      results.add(getSnapshotNameId(doc));
    }
    return results;
  }
  
  /**
   * Get a list of snapshot according to a glob query string
   * @param query the query string, which can contain wildcards
   * @return the list of resulting snapshot ids and names or an empty list if no matches
   */
  public List<String> snapshotByGlob(String query) {
    MarketDataSnapshotSearchRequest searchRequest = new MarketDataSnapshotSearchRequest();
    searchRequest.setName(query);
    searchRequest.setIncludeData(false);
    MarketDataSnapshotSearchResult searchResult = _snapshotMaster.search(searchRequest);
    List<String> results = new ArrayList<>();
    for (MarketDataSnapshotDocument doc : searchResult.getDocuments()) {
      results.add(getSnapshotNameId(doc));
    }
    return results;
  }
  
  /**
   * Get the latest snapshot by name
   * @param name exact name of the snapshot, not null
   * @return the UniqueId of the matched snapshot, or null if no match found
   * @throws OpenGammaRuntimeException if multiple matches are found
   */
  public UniqueId latestSnapshotByName(String name) {
    MarketDataSnapshotSearchRequest searchRequest = new MarketDataSnapshotSearchRequest();
    searchRequest.setName(name);
    searchRequest.setIncludeData(false);
    MarketDataSnapshotSearchResult searchResult = _snapshotMaster.search(searchRequest);
    if (searchResult.getDocuments().size() > 1) {
      throw new OpenGammaRuntimeException("More than one snapshot matches supplied name");
    }
    if (searchResult.getDocuments().size() == 0) {
      return null;
    }
    return searchResult.getFirstDocument().getUniqueId();   
  }
  
  /**
   * Get the latest snapshot by name
   * @param name exact name of the snapshot, not null
   * @param dateTime the date/time of the version of the snapshot to fetch 
   * @return the UniqueId of the matched snapshot, or null if no match found
   * @throws OpenGammaRuntimeException if multiple matches are found
   */
  public UniqueId latestSnapshotByNameAndDate(String name, ZonedDateTime dateTime) {
    MarketDataSnapshotSearchRequest searchRequest = new MarketDataSnapshotSearchRequest();
    searchRequest.setName(name);
    searchRequest.setIncludeData(false);
    MarketDataSnapshotSearchResult searchResult = _snapshotMaster.search(searchRequest);
    searchRequest.setVersionCorrection(VersionCorrection.ofVersionAsOf(dateTime.toInstant()));
    if (searchResult.getDocuments().size() > 1) {
      throw new OpenGammaRuntimeException("More than one snapshot matches supplied name");
    }
    if (searchResult.getDocuments().size() == 0) {
      return null;
    }
    return searchResult.getFirstDocument().getUniqueId();   
  }

  /**
   * Get meta data about available versions of a snapshot by it's name
   * @param name exact name of the snapshot, not null
   * @return a list of VersionInfo meta data objects containing version correction ranges and unique ids
   * @throws OpenGammaRuntimeException if multiple name matches are found
   */
  public List<VersionInfo> snapshotVersionsByName(String name) {
    MarketDataSnapshotSearchRequest searchRequest = new MarketDataSnapshotSearchRequest();
    searchRequest.setName(name);
    searchRequest.setIncludeData(false);
    MarketDataSnapshotSearchResult searchResult = _snapshotMaster.search(searchRequest);
    if (searchResult.getDocuments().size() > 1) {
      s_logger.warn("More than one snapshot matches supplied name, using first");
    }
    if (searchResult.getDocuments().size() == 0) {
      return Collections.emptyList();
    }
    ObjectId objectId = searchResult.getFirstDocument().getObjectId();
    MarketDataSnapshotHistoryResult historyResult = _snapshotMaster.history(new MarketDataSnapshotHistoryRequest(objectId));
    List<VersionInfo> results = new ArrayList<>();
    for (MarketDataSnapshotDocument doc : historyResult.getDocuments()) {
      results.add(new VersionInfo(doc.getVersionFromInstant(), doc.getCorrectionFromInstant(), doc.getVersionToInstant(), doc.getCorrectionToInstant(), doc.getUniqueId()));
    }
    return results;
  }
  
  /**
   * Class representing the version range information for a snapshot, including the UniqueId.
   */
  public class VersionInfo {
    private Instant _versionFrom;
    private Instant _versionTo;
    private Instant _correctionFrom;
    private Instant _correctionTo;
    private UniqueId _uniqueId;

    public VersionInfo(Instant versionFrom, Instant versionTo, Instant correctionFrom, Instant correctionTo, UniqueId uniqueId) {
      _versionFrom = versionFrom;
      _versionTo = versionTo;
      _correctionFrom = correctionFrom;
      _correctionTo = correctionTo;
      _uniqueId = uniqueId;
    }

    public Instant getVersionFrom() {
      return _versionFrom;
    }

    public Instant getVersionTo() {
      return _versionTo;
    }

    public Instant getCorrectionFrom() {
      return _correctionFrom;
    }

    public Instant getCorrectionTo() {
      return _correctionTo;
    }
    
    public UniqueId getUniqueId() {
      return _uniqueId;
    }
    
  }
  
}
