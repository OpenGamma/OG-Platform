/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

import org.hibernate.Session;

import com.opengamma.core.region.RegionSource;

/**
 * Context for the bean operations - i.e. access to any resources that are needed
 */
public final class OperationContext {

  private RegionSource _regionRepository;
  /**
   * The Hibernate session.
   */
  private Session _session;

  public void setRegionRepository(final RegionSource regionRepository) {
    _regionRepository = regionRepository;
  }

  public RegionSource getRegionRepository() {
    return _regionRepository;
  }
  
  public void setSession(final Session session) {
    _session = session;
  }
  
  public Session getSession() {
    return _session;
  }

}
