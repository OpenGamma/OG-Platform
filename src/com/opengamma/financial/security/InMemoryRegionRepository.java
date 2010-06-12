/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.LocalDate;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.tuple.Pair;

/**
 * In memory implementation of a region repository.  Repository is populated from a CSV file.
 */
public class InMemoryRegionRepository implements RegionRepository {
  private static final Logger s_logger = LoggerFactory.getLogger(InMemoryRegionRepository.class);
  private static final String HIERARCHY_COLUMN = "Hierarchy";
  private static final String NAME_COLUMN = "Name";
  private static final String TYPE_COLUMN = "Type";
  private static final String SUB_REGIONS_COLUMN = "Sub Regions";
  private FudgeContext _fudgeContext;
  private Map<String, Region> _roots = new HashMap<String, Region>();
  private Map<String, Map<Pair<String, Object>, Set<Region>>> _fieldIndex = new HashMap<String, Map<Pair<String, Object>, Set<Region>>>();
    
  public InMemoryRegionRepository(File file) {
    this(FudgeContext.GLOBAL_DEFAULT, file);
  }

  public InMemoryRegionRepository(FudgeContext fudgeContext, File file) {
    _fudgeContext = fudgeContext;
    try {
      // Hierarchy Name -> (Region Name -> Region Defintion (Super/Sub name + data))
      Map<String, Map<String, RegionDefinition>> roots = new HashMap<String, Map<String, RegionDefinition>>();
      
      CSVReader reader = new CSVReader(new FileReader(file));
      List<String> columns = Arrays.asList(reader.readNext());

      final int hierarchyColumnNum = columns.indexOf(HIERARCHY_COLUMN);
      final int nameColumnNum = columns.indexOf(NAME_COLUMN);
      final int typeColumnNum = columns.indexOf(TYPE_COLUMN);
      final int subRegionsColumnNum = columns.indexOf(SUB_REGIONS_COLUMN);
      
      String[] row = null;
      while ((row = reader.readNext()) != null) {
        String hierarchy = row[hierarchyColumnNum].trim();
        String name = row[nameColumnNum].trim();
        System.err.println(name);
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
          Region root = walkTree(rootName, nameToDef);
          _roots.put(hierarchyName, root);
          indexHierarchy(hierarchyName, root);
        }
      }
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Cannot open region data file (or file I/O problem)", e);
    }
  }
  
  private void indexHierarchy(String hierarchyName, Region root) {
    s_logger.info("Indexing {} : {}", hierarchyName, root.getName());
    if (!(_fieldIndex.containsKey(hierarchyName))) {
      _fieldIndex.put(hierarchyName, new HashMap<Pair<String, Object>, Set<Region>>());
    }
    Map<Pair<String, Object>, Set<Region>> aFieldIndex = _fieldIndex.get(hierarchyName);
    FudgeFieldContainer data = root.getData();
    for (FudgeField field : data) {
      Pair<String, Object> key = Pair.of(field.getName(), field.getValue());
      if (!aFieldIndex.containsKey(key)) {
        aFieldIndex.put(key, new HashSet<Region>());
      }
      aFieldIndex.get(key).add(root);
    }   
    // now do the fields we're not sticking in the fudge container
    List<Pair<String, Object>> keys = new ArrayList<Pair<String, Object>>();
    keys.add(Pair.<String, Object>of(NAME_COLUMN, root.getName()));
    keys.add(Pair.<String, Object>of(TYPE_COLUMN, root.getRegionType()));
    for (Pair<String, Object> key : keys) {
      if (!aFieldIndex.containsKey(key)) {
        aFieldIndex.put(key, new HashSet<Region>());
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
  
  private RegionNode walkTree(String currentName, Map<String, RegionDefinition> definitions) {
    RegionDefinition regionDefinition = definitions.get(currentName);
    RegionNode regionNode = regionDefinition.getRegion();
    
    Set<Region> subRegions = new HashSet<Region>();
    for (String subRegionName : regionDefinition.getSubRegionNames()) {
      RegionNode subRegion = walkTree(subRegionName, definitions);
      subRegion.setSuperRegion(regionNode);
      subRegions.add(subRegion);
    }
    regionNode.setSubRegions(subRegions);
    return regionNode;
  }
  
  /*package*/ Object classifyValue(String value) {
    if (value.matches("^TRUE|FALSE|T|F|True|False|true|false|Y|N$")) {
      if (value.equals("Y") || value.equals("T") || value.toLowerCase().equals("true")) {
        return Boolean.TRUE;
      } else if (value.equals("N") || value.equals("F") || value.toLowerCase().equals("false")) {
        return Boolean.FALSE;
      }
      throw new OpenGammaRuntimeException("Invalid boolean");
    } else if (value.matches("^\\d+$")) {
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
  public Set<Region> getAllOfType(LocalDate asOf, final String hierarchyName, final RegionType type) {
    return getHierarchyNodes(asOf, hierarchyName, TYPE_COLUMN, type);
  }
    
  @Override
  public Region getHierarchyNode(LocalDate asOf, String hierarchyName, String nodeName) {
    Set<Region> matches = getHierarchyNodes(asOf, hierarchyName, NAME_COLUMN, nodeName);
    if (matches.size() == 1) {
      return matches.iterator().next();
    } else {
      throw new OpenGammaRuntimeException("Should not have more than one region with matching name " + nodeName + " but found " + matches);
    }
  }

  @Override
  public Set<Region> getHierarchyNodes(LocalDate asOf, String hierarchyName, String fieldName, Object value) {
    Map<Pair<String, Object>, Set<Region>> index = _fieldIndex.get(hierarchyName);
    if (index != null) {
      Set<Region> matches = index.get(Pair.<String, Object>of(fieldName, value));
      return matches;
    } else {
      throw new OpenGammaRuntimeException("No such hierarchy");
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
