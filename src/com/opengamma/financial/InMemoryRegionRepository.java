/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.time.calendar.LocalDate;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierBundleMapper;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.FileUtils;
import com.opengamma.util.tuple.Pair;

/**
 * In memory implementation of a region repository.  Repository is populated from a CSV file.  
 * THERE IS CURRENTLY NO SUPPORT FOR VERSIONING, THE DATES ARE IGNORED
 */
public class InMemoryRegionRepository implements RegionRepository {
  // TODO: jim 2-Jul-2010 -- Make this cope with versioning...
  public static final String WORLD_DATA_DIR_PATH = FileUtils.getSharedDrivePrefix() + File.separator + "world-data";
  public static final String REGIONS_FILE_PATH = WORLD_DATA_DIR_PATH + File.separator + "regions" + File.separator + "countrylist_test.csv";

  private static final Comparator<Region> s_defaultRegionComparator = new Comparator<Region>() {
    @Override
    public int compare(Region r1, Region r2) {
      int type = r1.getRegionType().compareTo(r2.getRegionType());
      if (type != 0) { return type; }
      int name = r1.getName().compareTo(r2.getName());
      if (name != 0) { return name; }
      if (r1.equals(r2)) {
        return 0;
      } else {
        throw new OpenGammaRuntimeException("Non-unique name/region type present");
      }
    }
  };
  
  private static final Logger s_logger = LoggerFactory.getLogger(InMemoryRegionRepository.class);

  private static final String HIERARCHY_COLUMN = "Hierarchy";
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
  
  /**
   * Name of the hierarchy used for basic political geographic regions (countries, dependencies, etc)
   */
  public static final String POLITICAL_HIERARCHY_NAME = "Political";
  
  private static final String SUB_REGIONS_COLUMN = "Sub Regions";
  public static final IdentificationScheme REGION_FILE_SCHEME = new IdentificationScheme("REGION_FILE_SCHEME");
  public static final IdentificationScheme REGION_FILE_SCHEME_ISO2 = new IdentificationScheme("REGION_FILE_SCHEME_ISO2");

  private static final String REGION_SCHEME_PREFIX = "REGION_SCHEME_";
  
  private FudgeContext _fudgeContext;
  private Map<String, Region> _roots = new HashMap<String, Region>();
  private Map<String, IdentifierBundleMapper<Region>> _uniqueIdFactories = new HashMap<String, IdentifierBundleMapper<Region>>();
  private Map<String, Map<Pair<String, Object>, SortedSet<Region>>> _fieldIndex = new HashMap<String, Map<Pair<String, Object>, SortedSet<Region>>>();
    
  public InMemoryRegionRepository(File file) {
    this(FudgeContext.GLOBAL_DEFAULT, file);
  }

  public InMemoryRegionRepository(FudgeContext fudgeContext, File file) {
    _fudgeContext = fudgeContext;
    try {
      // Hierarchy Name -> (Region Name -> Region Definition (Super/Sub name + data))
      Map<String, Map<String, RegionDefinition>> roots = new HashMap<String, Map<String, RegionDefinition>>();
      
      // Open CSV file
      CSVReader reader = new CSVReader(new FileReader(file));
      List<String> columns = Arrays.asList(reader.readNext());

      // Special columns that don't go in fudge field container.
      final int hierarchyColumnNum = columns.indexOf(HIERARCHY_COLUMN);
      final int nameColumnNum = columns.indexOf(NAME_COLUMN);
      final int typeColumnNum = columns.indexOf(TYPE_COLUMN);
      final int subRegionsColumnNum = columns.indexOf(SUB_REGIONS_COLUMN);
      
      // build up map of name->definition, avoiding the step of building the tree so ordering is not needed.
      String[] row = null;
      while ((row = reader.readNext()) != null) {
        String hierarchy = row[hierarchyColumnNum].trim();
        String name = row[nameColumnNum].trim();
        //System.err.println(name);
        RegionType type = RegionType.valueOf(row[typeColumnNum].trim());
        // split semicolon separated list (somewhat stripped of whitespace) into array, convert to list and then to set. 
        Set<String> subRegions = new HashSet<String>(Arrays.asList(row[subRegionsColumnNum].split(";")));
        subRegions = trim(subRegions);
        MutableFudgeFieldContainer data = _fudgeContext.newMessage();
        for (int i = 0; i < columns.size(); i++) {
          if (i != hierarchyColumnNum &&
              i != nameColumnNum &&
              i != typeColumnNum && 
              i != subRegionsColumnNum) {
            String columnName = columns.get(i).trim();
            String strValue = row[i].trim();
            Object value = classifyValue(strValue);
            data.add(columnName, value);
          }
        }
        if (!roots.containsKey(hierarchy)) {
          roots.put(hierarchy, new HashMap<String, RegionDefinition>());
        }
        RegionNode incompleteRegion = new RegionNode(_fudgeContext, name, type, data);
        roots.get(hierarchy).put(name, new RegionDefinition(incompleteRegion, subRegions));
      }
      // now turn the definitions into proper tree reference links.
      for (String hierarchyName : roots.keySet()) {
        Map<String, RegionDefinition> nameToDef = roots.get(hierarchyName);
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
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Cannot open region data file (or file I/O problem)", e);
    }
  }
  
  private RegionNode walkTree(String currentName, Map<String, RegionDefinition> definitions, IdentifierBundleMapper<Region> bundleMapper) {
    RegionDefinition regionDefinition = definitions.get(currentName);
    if (regionDefinition == null) {
      throw new OpenGammaRuntimeException("Can't find region [" + currentName + "]");
    }
    RegionNode regionNode = regionDefinition.getRegion();
    SortedSet<Region> subRegions = Sets.newTreeSet(s_defaultRegionComparator);
    for (String subRegionName : regionDefinition.getSubRegionNames()) {
      RegionNode subRegion = walkTree(subRegionName, definitions, bundleMapper);
      subRegion.setSuperRegion(regionNode);
      subRegions.add(subRegion);
    }
    regionNode.setSubRegions(subRegions);
    regionNode.setIdentifiers(new IdentifierBundle(new Identifier(REGION_FILE_SCHEME, currentName)));
    UniqueIdentifier uniqueId = bundleMapper.add(regionNode.getIdentifiers(), regionNode);
    regionNode.setUniqueIdentifier(uniqueId);
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
        aFieldIndex.put(key, new TreeSet<Region>(s_defaultRegionComparator));
      }
      aFieldIndex.get(key).add(root);
    }   
    // now do the fields we're not sticking in the fudge container
    List<Pair<String, Object>> keys = new ArrayList<Pair<String, Object>>();
    keys.add(Pair.<String, Object>of(NAME_COLUMN, root.getName()));
    keys.add(Pair.<String, Object>of(TYPE_COLUMN, root.getRegionType()));
    for (Pair<String, Object> key : keys) {
      if (!aFieldIndex.containsKey(key)) {
        aFieldIndex.put(key, new TreeSet<Region>(s_defaultRegionComparator));
      }
      aFieldIndex.get(key).add(root);
    }
    // index the sub-regions.
    for (Region subRegion : root.getSubRegions()) {
      indexHierarchy(hierarchyName, subRegion);
    }
  }
  
  private Set<String> trim(Set<String> subRegions) {
    Set<String> result = new HashSet<String>();
    for (String subRegion : subRegions) {
      String trimmed = subRegion.trim();
      if (!trimmed.isEmpty()) {
        result.add(trimmed);
      }
    }
    return result;
  }
  
  /*package*/ Object classifyValue(String value) {
    if (value.matches("^TRUE|FALSE|T|F|True|False|true|false|Y|N$")) {
      if (value.equals("Y") || value.equals("T") || value.toLowerCase().equals("true")) {
        return Boolean.TRUE;
      } else if (value.equals("N") || value.equals("F") || value.toLowerCase().equals("false")) {
        return Boolean.FALSE;
      }
      throw new OpenGammaRuntimeException("Invalid boolean");
    } else if (value.matches("^[+-]?\\d+$")) {
      return Integer.parseInt(value);
    } else {
      try {
        return Double.parseDouble(value);
      } catch (NumberFormatException nfe) {
        return value;
      }
    }
  }
  
  @Override
  public SortedSet<Region> getAllOfType(LocalDate asOf, final String hierarchyName, final RegionType type) {
    return getHierarchyNodes(asOf, hierarchyName, TYPE_COLUMN, type);
  }
    
  @Override
  public Region getHierarchyNode(LocalDate asOf, String hierarchyName, String nodeName) {
    SortedSet<Region> matches = getHierarchyNodes(asOf, hierarchyName, NAME_COLUMN, nodeName);
    if (matches.size() == 1) {
      return matches.iterator().next();
    } else {
      throw new OpenGammaRuntimeException("Should not have more than one region with matching name " + nodeName + " but found " + matches);
    }
  }

  @Override
  public SortedSet<Region> getHierarchyNodes(LocalDate asOf, String hierarchyName, String fieldName, Object value) {
    Map<Pair<String, Object>, SortedSet<Region>> index = _fieldIndex.get(hierarchyName);
    if (index != null) {
      SortedSet<Region> matches = index.get(Pair.<String, Object>of(fieldName, value));
      return matches;
    } else {
      throw new OpenGammaRuntimeException("No such hierarchy");
    }
  }
  
  public SortedSet<Region> getHierarchyNodes(LocalDate asOf, String hierarchyName, Pair<String, Object>... fieldNameValuePairs) {
    SortedSet<Region> result = new TreeSet<Region>(s_defaultRegionComparator);
    for (Pair<?, ?> keyValuePair : fieldNameValuePairs) {
      String fieldName = (String) keyValuePair.getKey();
      Object value = keyValuePair.getValue();
      if (result.isEmpty()) {
        result.addAll(getHierarchyNodes(asOf, hierarchyName, fieldName, value));
      } else {
        result.retainAll(getHierarchyNodes(asOf, hierarchyName, fieldName, value));
      }
    } 
    return result;
  }

  
  @Override
  public Region getHierarchyNode(LocalDate asOf, String hierarchyName, Identifier nodeId) {
    return _uniqueIdFactories.get(hierarchyName).get(nodeId);
  }

  @Override
  public Region getHierarchyNode(LocalDate asOf, UniqueIdentifier uniqueId) {
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
  
  @Override
  public Region getHierarchyRoot(LocalDate asOf, String hierarchyName) {
    return _roots.get(hierarchyName);
  }
  
  
  /**
   * This method updates the internal indices, and should be called if the Region graph is modified in any way.
   * @param hierarchyName the name of the hierarchy to update the index of
   */
  public void updateIndex(String hierarchyName) {
    indexHierarchy(hierarchyName, _roots.get(hierarchyName));
  }
}
