/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.Set;

import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * General interface for Regions
 */
public interface Region {
  UniqueIdentifier getUniqueIdentifier();
  IdentifierBundle getIdentifiers();
  String getName();
  RegionType getRegionType();
  FudgeFieldContainer getDataUp();
  FudgeFieldContainer getData();
  Region getSuperRegion();
  Set<Region> getSubRegions();
}
