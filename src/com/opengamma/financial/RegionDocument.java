/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import java.util.Comparator;

import com.opengamma.engine.world.Region;
import com.opengamma.engine.world.RegionType;

/**
 * Document containing a Region + metadata
 */
public class RegionDocument {
  /**
   * Comparator that sorts documents using the same ordering as the underlying Region object's default comparator.
   */
  public static final Comparator<RegionDocument> COMPARATOR = new Comparator<RegionDocument>() {
    @Override
    public int compare(RegionDocument o1, RegionDocument o2) {
      return Region.COMPARATOR.compare(o1.getValue(), o2.getValue());
    }
  };
  private String _hierarchy;
  private String _name;
  private RegionType _type;
  private Region _value;
  public RegionDocument(String hierarchy, Region region) {
    _hierarchy = hierarchy;
    _name = region.getName();
    _type = region.getRegionType();
    _value = region;
  }
  
  public String getHierarchy() {
    return _hierarchy;
  }
  
  public String getName() {
    return _name;
  }
  
  public RegionType getRegionType() {
    return _type;
  }
  
  public Region getValue() {
    return _value;
  }
}
