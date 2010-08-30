/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.time.Instant;
import javax.time.calendar.LocalDate;

import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.tuple.Pair;

/**
 * Interface for retrieving Region hierarchies.
 */
public interface RegionMaster {
  /**
   * Name of the hierarchy used for basic political geographic regions (countries, dependencies, etc)
   */
  public static final String POLITICAL_HIERARCHY_NAME = "Political";
  /**
   * Get the region information using the unique id assigned to a region/hierarchy combination.
   * @param uniqueId an unique Identifier for the desired region
   * @return the matching region or null if none is found
   */
  Region getRegion(final UniqueIdentifier uniqueId);
  
  /**
   * Perform a search
   * @param searchRequest an object containing the request parameters
   * @return seachResult the result of the search
   */
  RegionSearchResult searchRegions(RegionSearchRequest searchRequest);
  
  /**
   * Perform a historical search
   * @param searchHistoricRequest an object containing the historical request parameters
   * @return seachResult the result of the search
   */
  RegionSearchResult searchHistoricRegions(RegionSearchHistoricRequest searchRequest);
    
  void addRegionTree(String hierarchyName, Map<String, RegionDefinition> nameToDef);
}
