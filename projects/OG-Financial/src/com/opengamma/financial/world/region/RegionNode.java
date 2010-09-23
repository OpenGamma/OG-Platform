/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.region;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.TimeZone;
import javax.time.i18n.Territory;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

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
  private UniqueIdentifier _uniqueIdentifier;
  private IdentifierBundle _identifiers;
  
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
  
  // REVIEW 2010-07-26 Andrew -- this is not a good solution to the equality problem below.
  private static final ThreadLocal<Integer> s_stackDirection = new ThreadLocal<Integer>() {
    @Override
    protected Integer initialValue() {
      return 0;
    }
  };

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
    } else {
      final int stack = s_stackDirection.get();
      if (stack >= 0) {
        s_stackDirection.set(stack + 1);
        final boolean cmp = _subRegions.equals(other._subRegions);
        s_stackDirection.set(stack);
        if (!cmp) {
          return false;
        }
      }
    }
    if (_superRegion == null) {
      if (other._superRegion != null) {
        return false;
      }
    } else {
      final int stack = s_stackDirection.get();
      if (stack <= 0) {
        s_stackDirection.set(stack + 1);
        final boolean cmp = _superRegion.equals(other._superRegion);
        s_stackDirection.set(stack);
        if (!cmp) {
          return false;
        }
      }
    }
    return true;
  }
  
  /*package*/ void setIdentifiers(IdentifierBundle identifiers) {
    _identifiers = identifiers;
  }

  @Override
  public IdentifierBundle getIdentifiers() {
    return _identifiers;
  }
  
  /*package*/ void setUniqueIdentifier(UniqueIdentifier uniqueIdentifier) {
    _uniqueIdentifier = uniqueIdentifier;
  }

  @Override
  public UniqueIdentifier getUniqueIdentifier() {
    return _uniqueIdentifier;
  }
  
  public String toString() {
    // Don't use ToStringBuilder or we will get the full object graph
    final StringBuilder sb = new StringBuilder();
    sb.append("RegionNode[");
    sb.append("_name=").append(_name);
    sb.append(",_regionType=").append(_regionType);
    sb.append(",_subRegion={");
    for (Region region : _subRegions) {
      sb.append(region.getUniqueIdentifier());
    }
    sb.append("},_superRegion=").append((_superRegion != null) ? _superRegion.getUniqueIdentifier() : "<null>");
    sb.append(",_uniqueIdentifier=").append(_uniqueIdentifier);
    sb.append(",_data=").append(_data);
    sb.append(",_uniqueIdentifier=").append(_uniqueIdentifier);
    sb.append(",_identifiers=").append(_identifiers);
    return sb.append(']').toString();
  }

  @Override
  public String getCountryISO2() {
    if (getData().hasField(InMemoryRegionMaster.ISO_COUNTRY_2)) {
      return getData().getString(InMemoryRegionMaster.ISO_COUNTRY_2);
    } else {
      return null;
    }
  }
  
  @Override
  public String getCurrencyISO3() {
    if (getData().hasField(InMemoryRegionMaster.ISO_CURRENCY_3)) {
      return getData().getString(InMemoryRegionMaster.ISO_CURRENCY_3);
    } else {
      return null;
    }
  }

  @Override
  public TimeZone getTimeZone() {
    String countryISO2 = getCountryISO2();
    if (countryISO2 != null) {
      return TimeZone.UTC;
      // TODO: fix this!!!!
      //Territory territory = Territory.forID(countryISO2);
      //return territory.getZone();
    }  else {
      return null;
    }
  }
}
