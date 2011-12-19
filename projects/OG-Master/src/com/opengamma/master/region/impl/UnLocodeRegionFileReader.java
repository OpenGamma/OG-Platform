/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region.impl;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.region.RegionClassification;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.master.region.ManageableRegion;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.RegionSearchRequest;
import com.opengamma.master.region.RegionSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.i18n.Country;

/**
 * Loads a CSV formatted UN/LOCODE file based on the regions in the holiday database.
 * <p>
 * This populates a region master.
 */
class UnLocodeRegionFileReader {

  /**
   * Path to the default regions file.
   */
  private static final String REGIONS_RESOURCE = "/com/opengamma/region/UNLOCODE.csv";
  /**
   * Path to the list of locode regions to load.
   */
  private static final String LOAD_RESOURCE = "/com/opengamma/master/region/impl/UnLocode.txt";

  /**
   * The region master to populate.
   */
  private RegionMaster _regionMaster;

  /**
   * Populates a region master.
   * 
   * @param regionMaster  the region master to populate, not null
   * @return the master, not null
   */
  static RegionMaster populate(RegionMaster regionMaster) {
    InputStream stream = regionMaster.getClass().getResourceAsStream(REGIONS_RESOURCE);
    UnLocodeRegionFileReader reader = new UnLocodeRegionFileReader(regionMaster);
    reader.parse(stream);
    return regionMaster;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance with a master to populate.
   * 
   * @param regionMaster  the region master, not null
   */
  UnLocodeRegionFileReader(RegionMaster regionMaster) {
    ArgumentChecker.notNull(regionMaster, "regionMaster");
    _regionMaster = regionMaster;
  }

  //-------------------------------------------------------------------------
  private void parse(InputStream in) {
    InputStreamReader reader = new InputStreamReader(new BufferedInputStream(in), Charsets.UTF_8);
    try {
      parse(reader);
    } finally {
      IOUtils.closeQuietly(reader);
    }
  }

  private void parse(InputStreamReader reader) {
    Set<String> required = parseRequired();
    Set<ManageableRegion> regions = parseLocodes(reader, required);
    coppClark(regions);
    store(regions);
  }

  private Set<String> parseRequired() {
    InputStream stream = getClass().getResourceAsStream(LOAD_RESOURCE);
    if (stream == null) {
      throw new OpenGammaRuntimeException("Unable to find UnLocode.txt defining the UN/LOCODEs");
    }
    try {
      Set<String> lines = new HashSet<String>(IOUtils.readLines(stream, "UTF-8"));
      Set<String> required = new HashSet<String>();
      for (String line : lines) {
        line = StringUtils.trimToNull(line);
        if (line != null) {
          required.add(line);
        }
      }
      return required;
    } catch (Exception ex) {
      throw new OpenGammaRuntimeException("Unable to read UnLocode.txt defining the UN/LOCODEs");
    } finally {
      IOUtils.closeQuietly(stream);
    }
  }

  private Set<ManageableRegion> parseLocodes(Reader in, Set<String> required) {
    Set<ManageableRegion> regions = new HashSet<ManageableRegion>(1024, 0.75f);
    String name = null;
    try {
      CSVReader reader = new CSVReader(in);
      final int typeIdx = 0;
      final int countryIsoIdx = 1;
      final int unlocodePartIdx = 2;
      final int nameColumnIdx = 4;
      final int fullNameColumnIdx = 3;
      
      String[] row = null;
      while ((row = reader.readNext()) != null) {
        if (row.length < 9) {
          continue;
        }
        name = StringUtils.trimToNull(row[nameColumnIdx]);
        String type = StringUtils.trimToNull(row[typeIdx]);
        String fullName = StringUtils.trimToNull(row[fullNameColumnIdx]);
        fullName = Objects.firstNonNull(fullName, name);
        String countryISO = StringUtils.trimToNull(row[countryIsoIdx]);
        String unlocodePart = StringUtils.trimToNull(row[unlocodePartIdx]);
        String unlocode = countryISO + unlocodePart;
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(fullName) || StringUtils.isEmpty(countryISO) ||
            StringUtils.isEmpty(unlocodePart) || unlocode.length() != 5 ||
            countryISO.equals("XZ") || "=".equals(type) || required.remove(unlocode) == false) {
          continue;
        }
        
        ManageableRegion region = createRegion(name, fullName, countryISO);
        region.addExternalId(RegionUtils.unLocode20102RegionId(unlocode));
        regions.add(region);
      }
    } catch (Exception ex) {
      String detail = (name != null ? " while processing " + name : "");
      throw new OpenGammaRuntimeException("Unable to read UN/LOCODEs" + detail, ex);
    }
    if (required.size() > 0) {
      throw new OpenGammaRuntimeException("Requested UN/LOCODEs could not be found: " + required);
    }
    return regions;
  }

  private ManageableRegion createRegion(String name, String fullName, String countryISO) {
    ManageableRegion region = new ManageableRegion();
    region.setClassification(RegionClassification.MUNICIPALITY);
    region.setName(name);
    region.setFullName(fullName);
    addParent(region, countryISO);
    return region;
  }

  private void addParent(ManageableRegion region, String countryISO) {
    RegionSearchRequest request = new RegionSearchRequest();
    request.addCountry(Country.of(countryISO));
    ManageableRegion parent = _regionMaster.search(request).getFirstRegion();
    if (parent == null) {
      throw new OpenGammaRuntimeException("Cannot find parent '" + countryISO + "'  for '" + region.getName() + "'");
    }
    region.getParentRegionIds().add(parent.getUniqueId());
  }

  private void coppClark(Set<ManageableRegion> regions) {
    for (ManageableRegion region : regions) {
      String unLocode = region.getExternalIdBundle().getValue(RegionUtils.UN_LOCODE_2010_2);
      String coppClarkLocode = COPP_CLARK_ALTERATIONS.get(unLocode);
      if (coppClarkLocode != null) {
        region.addExternalId(RegionUtils.coppClarkRegionId(coppClarkLocode));
        if (coppClarkLocode.substring(0, 2).equals(unLocode.substring(0, 2)) == false) {
          addParent(region, coppClarkLocode.substring(0, 2));
        }
      } else {
        region.addExternalId(RegionUtils.coppClarkRegionId(unLocode));
      }
    }
    for (Entry<String, String> entry : COPP_CLARK_ADDITIONS.entrySet()) {
      ManageableRegion region = createRegion(entry.getValue(), entry.getValue(), entry.getKey().substring(0, 2));
      region.addExternalId(RegionUtils.coppClarkRegionId(entry.getKey()));
      regions.add(region);
    }
  }

  private void store(Set<ManageableRegion> regions) {
    for (ManageableRegion region : regions) {
      RegionDocument doc = new RegionDocument();
      doc.setRegion(region);
      RegionSearchRequest request = new RegionSearchRequest();
      request.addExternalIds(region.getExternalIdBundle());
      RegionSearchResult result = _regionMaster.search(request);
      if (result.getDocuments().size() == 0) {
        _regionMaster.add(doc);
      } else {
        RegionDocument existing = result.getFirstDocument();
        if (existing.getRegion().getName().equals(doc.getRegion().getName()) == false ||
            existing.getRegion().getFullName().equals(doc.getRegion().getFullName()) == false) {
          existing.getRegion().setName(doc.getRegion().getName());
          existing.getRegion().setFullName(doc.getRegion().getFullName());
          _regionMaster.update(existing);
        }
      }
    }
  }

  //-------------------------------------------------------------------------
  private static final Map<String, String> COPP_CLARK_ALTERATIONS = new HashMap<String, String>();
  static {
    COPP_CLARK_ALTERATIONS.put("CNCAN", "CNXSA");  // Guangzhou (China)
    COPP_CLARK_ALTERATIONS.put("GPMSB", "MFMGT");  // Marigot (Guadaloupe/St.Martin-MF)
    COPP_CLARK_ALTERATIONS.put("GPGUS", "BLSTB");  // Gustavia (Guadaloupe/St.Barts-BL)
    COPP_CLARK_ALTERATIONS.put("FIMHQ", "AXMHQ");  // Mariehamn (Finaland/Aland-AX)
    COPP_CLARK_ALTERATIONS.put("FMPNI", "FMFSM");  // Pohnpei (Micronesia)
    COPP_CLARK_ALTERATIONS.put("MSMNI", "MSMSR");  // Montserrat
  };
  private static final Map<String, String> COPP_CLARK_ADDITIONS = new HashMap<String, String>();
  static {
    COPP_CLARK_ADDITIONS.put("PSPSE", "West Bank");
    COPP_CLARK_ADDITIONS.put("LKMAT", "Matara");
    COPP_CLARK_ADDITIONS.put("ILJRU", "Jerusalem");
  };

}
