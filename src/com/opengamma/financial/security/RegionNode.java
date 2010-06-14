/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;

/**
 * Implementation of a Region.  This version is specifically mutable.
 */
public class RegionNode implements Region {

  private FudgeContext _fudgeContext;
  private String _name;
  private RegionType _regionType;
  private Set<Region> _subRegions;
  private Region _superRegion;
  private FudgeFieldContainer _data;

  
  public RegionNode(FudgeContext fudgeContext, String name, RegionType regionType, Region superRegion, Set<Region> subRegions, FudgeFieldContainer data) {
    _fudgeContext = fudgeContext;
    _name = name;
    _regionType = regionType;
    _subRegions = new HashSet<Region>(subRegions); // in case we passed an unmodifiable set or something.
    _superRegion = superRegion;
    _data = data;
  }
  
  public RegionNode(FudgeContext fudgeContext, String name, RegionType regionType, FudgeFieldContainer data) {
    this(fudgeContext, name, regionType, null, Collections.<Region>emptySet(), data);
  }
  
  @Override
  public FudgeFieldContainer getData() {
    return _data;
  }
  
  public void setData(FudgeFieldContainer data) {
    _data = data;
  }

  /**
   * Walk up the Region tree, adding fields that don't already exist from each level (the effect
   * being that nodes lower down the heirarchy override those above) to give a 'union of fields'
   * effect.
   * @return fudge field container with union of fields in data contains in nodes above it in the region heirarchy
   */
  @Override
  public FudgeFieldContainer getDataUp() {
    MutableFudgeFieldContainer result = _fudgeContext.newMessage();
    Region current = this;
    while (current != null) {
      FudgeFieldContainer data = current.getData();
      for (FudgeField field : data) {
        if (result.getByName(field.getName()) == null) {
          result.add(field);
        }
      }
      current = current.getSuperRegion();
    }
    return result;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public RegionType getRegionType() {
    return _regionType;
  }
  
  public void setRegionType(RegionType regionType) {
    _regionType = regionType;
  }

  @Override
  public Set<Region> getSubRegions() {
    return _subRegions;
  }
  
  public void addSubRegion(Region subRegion) {
    _subRegions.add(subRegion);
  }

  @Override
  public Region getSuperRegion() {
    return _superRegion;
  }
  
  /**
   * @param subRegions the subRegions to set
   */
  public void setSubRegions(Set<Region> subRegions) {
    _subRegions = subRegions;
  }

  /**
   * @param superRegion the superRegion to set
   */
  public void setSuperRegion(Region superRegion) {
    _superRegion = superRegion;
  }

  @Override
  public int hashCode() {
    return _name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof RegionNode)) {
      return false;
    }
    RegionNode other = (RegionNode) obj;

    if (_name == null) {
      if (other._name != null) {
        return false;
      }
    } else if (!_name.equals(other._name)) {
      return false;
    }
    if (_data == null) {
      if (other._data != null) {
        return false;
      }
    } else if (!_data.equals(other._data)) {
      return false;
    }
    if (_regionType == null) {
      if (other._regionType != null) {
        return false;
      }
    } else if (!_regionType.equals(other._regionType)) {
      return false;
    }
    if (_subRegions == null) {
      if (other._subRegions != null) {
        return false;
      }
    } else if (!_subRegions.equals(other._subRegions)) {
      return false;
    }
    if (_superRegion == null) {
      if (other._superRegion != null) {
        return false;
      }
    } else if (!_superRegion.equals(other._superRegion)) {
      return false;
    }
    return true;
  }

  

}
