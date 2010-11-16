/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region.impl;

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
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.base.Charsets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.common.Currency;
import com.opengamma.core.region.RegionClassification;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.region.ManageableRegion;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.FileUtils;

/**
 * Loads a CSV formatted region file
 * <p>
 * This populates a region master.
 */
public class RegionFileReader {

  /**
   * Path to the default regions file
   */
  //public static final String REGIONS_FILE_PATH = WORLD_DATA_DIR_PATH + File.separator + "regions" + File.separator + "regions.csv";
  private static final String REGIONS_RESOURCE = "/com/opengamma/region/regions.csv";
  /**
   * The name column header.
   */
  private static final String NAME_COLUMN = "Name";
  /**
   * The full name column header.
   */
  private static final String FORMAL_NAME_COLUMN = "Formal Name";
  /**
   * The type column header.
   */
  private static final String CLASSIFICATION_COLUMN = "Type";
  /**
   * The sovereignty column header.
   */
  private static final String SOVEREIGNITY_COLUMN = "Sovereignty";
  /**
   * The country code column header.
   */
  private static final String ISO_COUNTRY_2_COLUMN = "ISO 3166-1 2 Letter Code";
  /**
   * The currency code column header.
   */
  private static final String ISO_CURRENCY_3_COLUMN = "ISO 4217 Currency Code";
  /**
   * The sub regions column header
   */
  private static final String SUB_REGIONS_COLUMN = "Sub Regions";

  /**
   * The region master to populate.
   */
  private RegionMaster _regionMaster;

  /**
   * Creates a populated in-memory master and source.
   * <p>
   * The values can be extracted using the accessor methods.
   * 
   * @return the exchange reader, not null
   */
  public static RegionFileReader createPopulated() {
    RegionFileReader fileReader = new RegionFileReader(new InMemoryRegionMaster());
    InputStream regionsStream = fileReader.getClass().getResourceAsStream(REGIONS_RESOURCE);
    fileReader.parse(regionsStream);
    return fileReader;
  }

  /**
   * Populates a region master.
   * 
   * @param regionMaster  the region master to populate, not null
   * @return the master, not null
   */
  public static RegionMaster populate(RegionMaster regionMaster) {
    populate(regionMaster, regionMaster.getClass().getResourceAsStream(REGIONS_RESOURCE));
    return regionMaster;
  }

  /**
   * Populates a region master.
   * 
   * @param regionMaster  the region master to populate, not null
   * @param file  the CSV file to read from, not null
   * @return the master, not null
   */
  public static RegionMaster populate(RegionMaster regionMaster, File file) {
    RegionFileReader reader = new RegionFileReader(regionMaster);
    reader.parse(file);
    return regionMaster;
  }

  /**
   * Populates a region master.
   *
   * @param regionMaster  the region master to populate, not null
   * @param regionStream  the CSV stream to read from, not null
   * @return the master, not null
   */
  public static RegionMaster populate(RegionMaster regionMaster, InputStream regionStream) {
    RegionFileReader reader = new RegionFileReader(regionMaster);
    reader.parse(regionStream);
    return regionMaster;
  }

  /**
   * Creates an instance with a master to populate.
   * 
   * @param regionMaster  the region master, not null
   */
  public RegionFileReader(RegionMaster regionMaster) {
    ArgumentChecker.notNull(regionMaster, "regionMaster");
    _regionMaster = regionMaster;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the region master.
   * @return the region master, not null
   */
  public RegionMaster getRegionMaster() {
    return _regionMaster;
  }

  /**
   * Gets a {@code MasterRegionSource} for the master.
   * @return the region source, not null
   */
  public MasterRegionSource getRegionSource() {
    return new MasterRegionSource(getRegionMaster());
  }

  //-------------------------------------------------------------------------
  /**
   * Parses the specified file to populate the master.
   * 
   * @param file  the file to read, not null
   */
  public void parse(File file) {
    ArgumentChecker.notNull(file, "file");
    try {
      parse(new FileReader(file));
    } catch (FileNotFoundException ex) {
      throw new OpenGammaRuntimeException("Region file not found", ex);
    }
  }

  /**
   * Parses the specified file to populate the master.
   * 
   * @param in  the input stream to read, not null
   */
  public void parse(InputStream in) {
    InputStreamReader reader = new InputStreamReader(new BufferedInputStream(in), Charsets.UTF_8);
    try {
      parse(reader);
    } finally {
      IOUtils.closeQuietly(reader);
    }
  }

  /**
   * Parses the specified file to populate the master.
   * 
   * @param in  the input reader to read, not closed, not null
   */
  public void parse(Reader in) {
    String name = null;
    try {
      Map<String, ManageableRegion> regions = new HashMap<String, ManageableRegion>();
      Map<UniqueIdentifier, Set<String>> subRegions = new HashMap<UniqueIdentifier, Set<String>>();
      
      // open CSV file
      CSVReader reader = new CSVReader(in);
      List<String> columns = Arrays.asList(reader.readNext());
      
      // identify columns
      final int nameColumnIdx = columns.indexOf(NAME_COLUMN);
      final int formalNameColumnIdx = columns.indexOf(FORMAL_NAME_COLUMN);
      final int classificationColumnIdx = columns.indexOf(CLASSIFICATION_COLUMN);
      final int sovereignityColumnIdx = columns.indexOf(SOVEREIGNITY_COLUMN);
      final int countryColumnIdx = columns.indexOf(ISO_COUNTRY_2_COLUMN);
      final int currencyColumnIdx = columns.indexOf(ISO_CURRENCY_3_COLUMN);
      final int subRegionsColumnIdx = columns.indexOf(SUB_REGIONS_COLUMN);
      
      // parse
      String[] row = null;
      while ((row = reader.readNext()) != null) {
        name = row[nameColumnIdx].trim();  // the primary key
        String fullName = StringUtils.trimToNull(row[formalNameColumnIdx]);
        if (fullName == null) {
          fullName = name;
        }
        RegionClassification classification = RegionClassification.valueOf(row[classificationColumnIdx].trim());
        String sovereignity = StringUtils.trimToNull(row[sovereignityColumnIdx]);
        String countryISO = StringUtils.trimToNull(row[countryColumnIdx]);
        String currencyISO = StringUtils.trimToNull(row[currencyColumnIdx]);
        Set<String> rowSubRegions = new HashSet<String>(Arrays.asList(row[subRegionsColumnIdx].split(";")));
        rowSubRegions = trim(rowSubRegions);
        
        ManageableRegion region = new ManageableRegion();
        region.setClassification(classification);
        region.setName(name);
        region.setFullName(fullName);
        if (countryISO != null) {
          region.setCountryISO(countryISO);
          region.addIdentifier(RegionUtils.financialRegionId(countryISO));  // TODO: looks odd
        }
        if (currencyISO != null) {
          region.setCurrency(Currency.getInstance(currencyISO));
        }
        if (sovereignity != null) {
          ManageableRegion parent = regions.get(sovereignity);
          if (parent == null) {
            throw new OpenGammaRuntimeException("Cannot find parent '" + sovereignity + "'  for '" + name + "'");
          }
          region.getParentRegionIds().add(parent.getUniqueIdentifier());
        }
        for (Entry<UniqueIdentifier, Set<String>> entry : subRegions.entrySet()) {
          if (entry.getValue().remove(name)) {
            region.getParentRegionIds().add(entry.getKey());
          }
        }
        
        // store
        RegionDocument doc = getRegionMaster().add(new RegionDocument(region));
        if (rowSubRegions.size() > 0) {
          subRegions.put(doc.getUniqueId(), rowSubRegions);
        }
        regions.put(name, region);
      }
      for (Set<String> set : subRegions.values()) {
        if (set.size() > 0) {
          throw new OpenGammaRuntimeException("Cannot find children: " + set);
        }
      }
      
    } catch (Exception ex) {
      String detail = (name != null ? " while processing " + name : "");
      throw new OpenGammaRuntimeException("Cannot open region data file (or file I/O problem)" + detail, ex);
    }
  }

  /**
   * Trim the contents of a set.
   * @param subRegions  the set to trim, not null
   * @return the trimmed set, not null
   */
  private Set<String> trim(Set<String> subRegions) {
    Set<String> result = new HashSet<String>();
    for (String subRegion : subRegions) {
      String trimmed = subRegion.trim();
      if (trimmed.isEmpty() == false) {
        result.add(trimmed);
      }
    }
    return result;
  }

}
