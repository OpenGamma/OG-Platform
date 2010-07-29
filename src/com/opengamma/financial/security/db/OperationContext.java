/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.security.db;

import com.opengamma.financial.RegionRepository;

/**
 * Context for the bean operations - i.e. access to any resources that are needed
 */
public final class OperationContext {

  private RegionRepository _regionRepository;

  public void setRegionRepository(final RegionRepository regionRepository) {
    _regionRepository = regionRepository;
  }

  public RegionRepository getRegionRepository() {
    return _regionRepository;
  }

}
