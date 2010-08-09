/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import java.util.Comparator;
import java.util.Set;

import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * General interface for Regions
 */
public interface Region {
  static final Comparator<Region> COMPARATOR = new Comparator<Region>() {
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
  UniqueIdentifier getUniqueIdentifier();
  IdentifierBundle getIdentifiers();
  String getName();
  RegionType getRegionType();
  FudgeFieldContainer getDataUp();
  FudgeFieldContainer getData();
  Region getSuperRegion();
  Set<Region> getSubRegions();
}
