/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import com.opengamma.engine.world.Region;

/**
 * 
 */
public class RegionSearchResult {
  private SortedSet<RegionDocument> _docs = new TreeSet<RegionDocument>(RegionDocument.COMPARATOR);
  
  public RegionSearchResult() {
  }
  
  public RegionSearchResult(Collection<RegionDocument> regionDocs) {
    addRegionDocuments(regionDocs);
  }
  
  public RegionSearchResult(RegionDocument regionDoc) {
    addRegionDocument(regionDoc); 
  }
  
  public void addRegionDocument(RegionDocument regionDoc) {
    _docs.add(regionDoc);
  }
  
  public void addRegionDocuments(Collection<RegionDocument> regionDocs) {
    _docs.addAll(regionDocs);
  }
  
  public SortedSet<RegionDocument> getResultDocuments() {
    return _docs;
  }
  
  public SortedSet<Region> getResults() {
    SortedSet<Region> results = new TreeSet<Region>(Region.COMPARATOR);
    for (RegionDocument regionDoc : _docs) {
      results.add(regionDoc.getValue());
    }
    return results;
  }
  
  public Region getBestResult() {
    RegionDocument regionDoc = getBestResultDocument();
    if (regionDoc == null) {
      return null;
    } else {
      return regionDoc.getValue();
    }
  }
  
  public RegionDocument getBestResultDocument() {
    if (_docs.size() > 0) {
      return _docs.first();
    } else {
      return null;
    } 
  }
}
