/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.joda.beans.JodaBeanUtils;

import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * A cache decorating a {@link SecurityMasterDetailProvider} 
 */
public class EHCachingSecurityMasterDetailProvider implements SecurityMasterDetailProvider {

  /* package for testing */static final String SECURITY_CACHE = "security-detail-cache";
  
  private final SecurityMasterDetailProvider _underlying;

  /**
   * The cache manager.
   */
  private final CacheManager _manager;
  /**
   * The single security cache.
   */
  private final Cache _detailsCache;
  
  
  /**
   * @param underlying The provider to wrap
   * @param manager The cache manager
   */
  public EHCachingSecurityMasterDetailProvider(SecurityMasterDetailProvider underlying, CacheManager manager) {
    super();
    _underlying = underlying;
    _manager = manager;
    EHCacheUtils.addCache(_manager, SECURITY_CACHE);
    _detailsCache = EHCacheUtils.getCacheFromManager(_manager, SECURITY_CACHE);
  }


  @Override
  public void init(DbSecurityMaster master) {
    _underlying.init(master);
  }


  @Override
  public ManageableSecurity loadSecurityDetail(ManageableSecurity base) {
    ManageableSecurity cached;
    
    Element e = _detailsCache.get(base.getUniqueId());
    if (e != null) {
      cached = (ManageableSecurity) e.getValue();
    } else {
      cached = _underlying.loadSecurityDetail(base);
      e = new Element(base.getUniqueId(), cached);
      _detailsCache.put(e);
    }
    return JodaBeanUtils.clone(cached);
  }


  @Override
  public void storeSecurityDetail(ManageableSecurity security) {
    _underlying.storeSecurityDetail(security);
    //TODO cache?
  }

  @Override
  public void extendSearch(SecuritySearchRequest request, DbMapSqlParameterSource args) {
    _underlying.extendSearch(request, args);
  }

}
