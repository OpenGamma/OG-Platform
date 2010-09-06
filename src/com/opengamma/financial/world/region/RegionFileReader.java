/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.region;

import static com.opengamma.financial.world.region.InMemoryRegionMaster.HIERARCHY_COLUMN;
import static com.opengamma.financial.world.region.InMemoryRegionMaster.NAME_COLUMN;
import static com.opengamma.financial.world.region.InMemoryRegionMaster.SUB_REGIONS_COLUMN;
import static com.opengamma.financial.world.region.InMemoryRegionMaster.TYPE_COLUMN;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeFieldContainer;

import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.FileUtils;

/**
 * Loads a region file an populated a region master with it. 
 */
public class RegionFileReader {
  private static final String REGIONS_RESOURCE = "/com/opengamma/region/countrylist_test.csv";
  
  private RegionMaster _regionMaster;
  private FudgeContext _fudgeContext;

  public static RegionSource createPopulatedRegionSource() {
    return new DefaultRegionSource(createPopulatedRegionMaster());
  }
  
  public static RegionMaster createPopulatedRegionMaster() {
    RegionMaster regionMaster = new InMemoryRegionMaster();
    RegionFileReader reader = new RegionFileReader(regionMaster);
    InputStream stream = reader.getClass().getResourceAsStream(REGIONS_RESOURCE);
    reader.populate(stream);
    return regionMaster;
  }
  
  public RegionFileReader(RegionMaster regionMaster) {
    _regionMaster = regionMaster;
    _fudgeContext = FudgeContext.GLOBAL_DEFAULT;
  }
  
  public RegionFileReader(RegionMaster regionMaster, FudgeContext fudgeContext) {
    _regionMaster = regionMaster;
    _fudgeContext = FudgeContext.GLOBAL_DEFAULT;
  }
  
  public void populate(File file) {
    try {
      populate(new FileReader(file));
    } catch (FileNotFoundException ex) {
      throw new OpenGammaRuntimeException("Region file not found", ex);
    }
  }
  
  public void populate(InputStream is) {
    populate(new InputStreamReader(new BufferedInputStream(is)));
  }
  
  public void populate(Reader aReader) {
    try {
      // Hierarchy Name -> (Region Name -> Region Definition (Super/Sub name + data))
      Map<String, Map<String, RegionDefinition>> roots = new HashMap<String, Map<String, RegionDefinition>>();
      
      // Open CSV file
      CSVReader reader = new CSVReader(aReader);
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
        _regionMaster.addRegionTree(hierarchyName, roots.get(hierarchyName));
      }
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Cannot open region data file (or file I/O problem)", e);
    }
  }
  
  private static Set<String> trim(Set<String> subRegions) {
    Set<String> result = new HashSet<String>();
    for (String subRegion : subRegions) {
      String trimmed = subRegion.trim();
      if (!trimmed.isEmpty()) {
        result.add(trimmed);
      }
    }
    return result;
  }
  
  /*package*/ static Object classifyValue(String value) {
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
  
  /**
   * Static convenience method to save separately constructing the object and calling populate
   * @param regionMaster the RegionMaster to populate
   * @param file the CSV file to read from
   */
  public static void populateMaster(RegionMaster regionMaster, File file) {
    RegionFileReader reader = new RegionFileReader(regionMaster);
    reader.populate(file);
  }

  /**
   * Path to world data generally (covering exchanges, regions and holidays).
   */
  public static final String WORLD_DATA_DIR_PATH = FileUtils.getSharedDrivePrefix() + File.separator + "world-data";
  
  /**
   * Path to the default regions file
   */
  public static final String REGIONS_FILE_PATH = WORLD_DATA_DIR_PATH + File.separator + "regions" + File.separator + "countrylist_test.csv";

}
