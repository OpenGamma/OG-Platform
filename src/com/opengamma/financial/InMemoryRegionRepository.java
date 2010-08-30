/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.time.Instant;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierBundleMapper;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.tuple.Pair;

/**
 * In memory implementation of a region repository.  Repository is populated from a CSV file.  
 * THERE IS CURRENTLY NO SUPPORT FOR VERSIONING, THE DATES ARE IGNORED
 */
public class InMemoryRegionRepository implements RegionMaster {
  // TODO: jim 2-Jul-2010 -- Make this cope with versioning...
  private static final Logger s_logger = LoggerFactory.getLogger(InMemoryRegionRepository.class);

  static final String HIERARCHY_COLUMN = "Hierarchy";
  /**
   * Name of the pseudo-field used to hold the name of the region so it can be passed into getHierarchyNodes 
   */
  public static final String NAME_COLUMN = "Name";
  /**
   * Name of the pseudo-field used to hold the type (RegionType) of the region so it can be passed into getHierarchyNodes 
   */
  public static final String TYPE_COLUMN = "Type";
  /**
   * Name of the field used to hold the 2 letter ISO 3166-1 Country Code
   */
  public static final String ISO_COUNTRY_2 = "ISO 3166-1 2 Letter Code";
  /**
   * Name of the field used to hold the 3 letter ISO 3166-1 Country Code
   */
  public static final String ISO_CURRENCY_3 = "ISO 4217 Currency Code";
  
  static final String SUB_REGIONS_COLUMN = "Sub Regions";
  static final IdentificationScheme REGION_FILE_SCHEME = new IdentificationScheme("REGION_FILE_SCHEME");
  static final IdentificationScheme REGION_FILE_SCHEME_ISO2 = new IdentificationScheme("REGION_FILE_SCHEME_ISO2");

  static final String REGION_SCHEME_PREFIX = "REGION_SCHEME_";
  
  private Map<String, Region> _roots = new HashMap<String, Region>();
  private Map<String, IdentifierBundleMapper<Region>> _uniqueIdFactories = new HashMap<String, IdentifierBundleMapper<Region>>();
  private Map<String, Map<Pair<String, Object>, SortedSet<Region>>> _fieldIndex = new HashMap<String, Map<Pair<String, Object>, SortedSet<Region>>>();

  public InMemoryRegionRepository() {
  }
  
  public void addRegionTree(String hierarchyName, Map<String, RegionDefinition> nameToDef) {
    Set<String> regionDefinitions = new HashSet<String>(nameToDef.keySet());
    for (Map.Entry<String, RegionDefinition> entry : nameToDef.entrySet()) {
      RegionDefinition regionDefinition = entry.getValue();
      regionDefinitions.removeAll(regionDefinition.getSubRegionNames());
    }
    if (regionDefinitions.isEmpty()) {
      throw new OpenGammaRuntimeException("Cannot find a root node in " + hierarchyName);
    } else if (regionDefinitions.size() > 1) {
      throw new OpenGammaRuntimeException("Too many root nodes (nodes not subnodes elsewhere) left over in " +
                                          hierarchyName + ": " + regionDefinitions.toString());
    } else {
      String rootName = regionDefinitions.iterator().next();
      IdentifierBundleMapper<Region> bundleMapper = new IdentifierBundleMapper<Region>(REGION_SCHEME_PREFIX + hierarchyName);
      // Walk down, converting the name references in the definition into object references.  
      // At the same time, populating the identifiers and unique identifier fields from the bundleMapper.
      Region root = walkTree(rootName, nameToDef, bundleMapper);
      _roots.put(hierarchyName, root);
      _uniqueIdFactories.put(hierarchyName, bundleMapper);
      indexHierarchy(hierarchyName, root);
    }
  }
  
  private RegionNode walkTree(String currentName, Map<String, RegionDefinition> definitions, IdentifierBundleMapper<Region> bundleMapper) {
    RegionDefinition regionDefinition = definitions.get(currentName);
    if (regionDefinition == null) {
      throw new OpenGammaRuntimeException("Can't find region [" + currentName + "]");
    }
    RegionNode regionNode = regionDefinition.getRegion();
    SortedSet<Region> subRegions = Sets.newTreeSet(Region.COMPARATOR);
    for (String subRegionName : regionDefinition.getSubRegionNames()) {
      RegionNode subRegion = walkTree(subRegionName, definitions, bundleMapper);
      subRegion.setSuperRegion(regionNode);
      subRegions.add(subRegion);
    }
    regionNode.setSubRegions(subRegions);
    List<Identifier> identifiers = new ArrayList<Identifier>();
    identifiers.add(Identifier.of(REGION_FILE_SCHEME, currentName));
    
    if (regionNode.getCountryISO2() != null && !regionNode.getCountryISO2().isEmpty()) {
      
      identifiers.add(Identifier.of(ISO_COUNTRY_2, regionNode.getCountryISO2()));
    }
    if (regionNode.getCurrencyISO3() != null && !regionNode.getCurrencyISO3().isEmpty()) {
      identifiers.add(Identifier.of(ISO_CURRENCY_3, regionNode.getCurrencyISO3()));
    }
    regionNode.setIdentifiers(new IdentifierBundle(identifiers.toArray(new Identifier[] {})));
    s_logger.info("ids = " + regionNode.getIdentifiers());
    UniqueIdentifier uniqueId = bundleMapper.add(regionNode.getIdentifiers(), regionNode);
    regionNode.setUniqueIdentifier(uniqueId);
    s_logger.info("ids = " + regionNode.getIdentifiers());
    return regionNode;
  }
  
  private void indexHierarchy(String hierarchyName, Region root) {
    // s_logger.info("Indexing {} : {}", hierarchyName, root.getName());
    if (!(_fieldIndex.containsKey(hierarchyName))) {
      _fieldIndex.put(hierarchyName, new HashMap<Pair<String, Object>, SortedSet<Region>>());
    }
    Map<Pair<String, Object>, SortedSet<Region>> aFieldIndex = _fieldIndex.get(hierarchyName);
    FudgeFieldContainer data = root.getData();
    for (FudgeField field : data) {
      Pair<String, Object> key = Pair.of(field.getName(), field.getValue());
      if (!aFieldIndex.containsKey(key)) {
        aFieldIndex.put(key, new TreeSet<Region>(Region.COMPARATOR));
      }
      aFieldIndex.get(key).add(root);
    }   
    // now do the fields we're not sticking in the fudge container
    List<Pair<String, Object>> keys = new ArrayList<Pair<String, Object>>();
    keys.add(Pair.<String, Object>of(NAME_COLUMN, root.getName()));
    keys.add(Pair.<String, Object>of(TYPE_COLUMN, root.getRegionType()));
    for (Pair<String, Object> key : keys) {
      if (!aFieldIndex.containsKey(key)) {
        aFieldIndex.put(key, new TreeSet<Region>(Region.COMPARATOR));
      }
      aFieldIndex.get(key).add(root);
    }
    // index the sub-regions.
    for (Region subRegion : root.getSubRegions()) {
      indexHierarchy(hierarchyName, subRegion);
    }
  }
  
  public RegionSearchResult searchRegions(RegionSearchRequest searchRequest) {
    SortedSet<Region> resultNodes;
    if (searchRequest.isRootRequest()) {
      Region region = _roots.get(searchRequest.getHierarchy());
      SortedSet<Region> sortedResult = new ImmutableSortedSet.Builder<Region>(RegionNode.COMPARATOR).add(region).build();
      return new RegionSearchResult(wrapWithDocuments(searchRequest.getHierarchy(), sortedResult, searchRequest.isGraphIncluded()));
    } else if (searchRequest.getPredicates().isEmpty()) {
      if (_uniqueIdFactories.containsKey(searchRequest.getHierarchy())) {
        resultNodes = new TreeSet<Region>(RegionNode.COMPARATOR);
        resultNodes.addAll(_uniqueIdFactories.get(searchRequest.getHierarchy()).get(searchRequest.getIdentifierBundle()));
      } else {
        throw new OpenGammaRuntimeException("Cannot field hierarchy " + searchRequest.getHierarchy());
      }
    } else {
      if (searchRequest.getIdentifierBundle() != null) {
        throw new OpenGammaRuntimeException("You can either search with identifiers or predicates, but not both");
      }
      resultNodes = getHierarchyNodes(null, null, searchRequest.getHierarchy(), searchRequest.getPredicates());
    }
    return new RegionSearchResult(wrapWithDocuments(searchRequest.getHierarchy(), resultNodes, searchRequest.isGraphIncluded()));
  }
  
  // TODO: make this consistent with the normal search.T
  public RegionSearchResult searchHistoricRegions(RegionSearchHistoricRequest searchRequest) {    
    SortedSet<Region> results;
    IdentifierBundle identifiers = searchRequest.getIdentifierBundle();
    if (searchRequest.isRootRequest()) {
      Region region = _roots.get(searchRequest.getHierarchy());
      SortedSet<Region> sortedResult = new ImmutableSortedSet.Builder<Region>(RegionNode.COMPARATOR).add(region).build();
      return new RegionSearchResult(wrapWithDocuments(searchRequest.getHierarchy(), sortedResult, searchRequest.isGraphIncluded()));
    } else if (identifiers != null) {
      if (identifiers.size() == 1) {
        results = getHistoricRegion(searchRequest.getVersion(), searchRequest.getCorrection(), 
                                    searchRequest.getHierarchy(), identifiers.getIdentifiers().iterator().next());
      } else {
        results = new TreeSet<Region>(Region.COMPARATOR);
        for (Identifier identifier : identifiers) {
          results.addAll(getHistoricRegion(searchRequest.getVersion(), searchRequest.getCorrection(), 
                                           searchRequest.getHierarchy(), identifier));
        }
      }
    } else {
      results = getHierarchyNodes(searchRequest.getVersion(), searchRequest.getCorrection(), 
                                  searchRequest.getHierarchy(), searchRequest.getPredicates());
    }
    return new RegionSearchResult(wrapWithDocuments(searchRequest.getHierarchy(), results, searchRequest.isGraphIncluded()));
  }
   
  private SortedSet<RegionDocument> wrapWithDocuments(String hierarchyName, SortedSet<Region> regions, boolean isGraphIncluded) {
    SortedSet<RegionDocument> documents = new TreeSet<RegionDocument>(RegionDocument.COMPARATOR);
    for (Region region : regions) {
      if (isGraphIncluded) {
        documents.add(new RegionDocument(hierarchyName, region));
      } else {
        RegionNode detachedRegion = new RegionNode(FudgeContext.GLOBAL_DEFAULT, region.getName(), region.getRegionType(), FudgeContext.GLOBAL_DEFAULT.newMessage(region.getData()));
        detachedRegion.setIdentifiers(region.getIdentifiers());
        detachedRegion.setUniqueIdentifier(region.getUniqueIdentifier());
        documents.add(new RegionDocument(hierarchyName, detachedRegion));
      }
    }
    return documents;
  }
    
  private SortedSet<Region> getHierarchyNodes(Instant version, Instant correction, String hierarchyName, String fieldName, Object value) {
    Map<Pair<String, Object>, SortedSet<Region>> index = _fieldIndex.get(hierarchyName);
    if (index != null) {
      SortedSet<Region> matches = index.get(Pair.<String, Object>of(fieldName, value));
      return matches;
    } else {
      throw new OpenGammaRuntimeException("No such hierarchy");
    }
  }
  
  private SortedSet<Region> getHierarchyNodes(Instant version, Instant correction, String hierarchyName, Map<String, Object> fieldNameValuePairs) {
    SortedSet<Region> result = new TreeSet<Region>(Region.COMPARATOR);
    for (Map.Entry<?, ?> keyValuePair : fieldNameValuePairs.entrySet()) {
      String fieldName = (String) keyValuePair.getKey();
      Object value = keyValuePair.getValue();
      if (result.isEmpty()) {
        result.addAll(getHierarchyNodes(version, correction, hierarchyName, fieldName, value));
      } else {
        result.retainAll(getHierarchyNodes(version, correction, hierarchyName, fieldName, value));
      }
    } 
    return result;
  }

  private SortedSet<Region> getHistoricRegion(Instant version, Instant correction, String hierarchyName, Identifier nodeId) {
    SortedSet<Region> results = new TreeSet<Region>(Region.COMPARATOR);
    results.addAll(_uniqueIdFactories.get(hierarchyName).get(nodeId));
    return results;
  }

  private Region getHistoricRegion(Instant version, Instant correction, UniqueIdentifier uniqueId) {
    // scheme encodes which hierarchy to look in.
    if (uniqueId.getScheme().startsWith(REGION_SCHEME_PREFIX)) {
      String hierarchyName = uniqueId.getScheme().substring(REGION_SCHEME_PREFIX.length());
      if (_uniqueIdFactories.containsKey(hierarchyName)) {
        return _uniqueIdFactories.get(hierarchyName).get(uniqueId);
      } else {
        return null;
      }
    } else {
      throw new OpenGammaRuntimeException("Scheme of supplied uniqueId not provided by this system: " + uniqueId);
    }
  } 
  
  public Region getRegion(UniqueIdentifier uniqueId) {
    return getHistoricRegion(null, null, uniqueId);
  }
  
  /**
   * This method updates the internal indices, and should be called if the Region graph is modified in any way.
   * @param hierarchyName the name of the hierarchy to update the index of
   */
  public void updateIndex(String hierarchyName) {
    indexHierarchy(hierarchyName, _roots.get(hierarchyName));
  }
}
