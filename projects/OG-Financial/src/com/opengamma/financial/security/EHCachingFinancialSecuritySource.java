/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * A cache decorating a {@code FinancialSecuritySource}.
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public class EHCachingFinancialSecuritySource implements FinancialSecuritySource {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(EHCachingFinancialSecuritySource.class);
  /** The single security cache key. */
  /* package for testing */ static final String SINGLE_SECURITY_CACHE = "single-security-cache";
  /** The mulitple bonds cache key */
  /* package for testing */ static final String MULTI_BONDS_CACHE = "multi-bonds-cache";

  /**
   * The underlying cache.
   */
  private final FinancialSecuritySource _underlying;
  /**
   * The cache manager.
   */
  private final CacheManager _manager;
  /**
   * The single security cache.
   */
  private final Cache _uidCache;
  /**
   * The bond cache.
   */
  private final Cache _bondCache;
  /**
   * Listens for changes in the underlying security source.
   */
  private final ChangeListener _changeListener;
  /**
   * The local change manager.
   */
  private final ChangeManager _changeManager;

  /**
   * Creates an instance over an underlying source specifying the cache manager.
   * 
   * @param underlying  the underlying security source, not null
   * @param cacheManager  the cache manager, not null
   */
  public EHCachingFinancialSecuritySource(final FinancialSecuritySource underlying, final CacheManager cacheManager) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _underlying = underlying;
    EHCacheUtils.addCache(cacheManager, SINGLE_SECURITY_CACHE);
    EHCacheUtils.addCache(cacheManager, MULTI_BONDS_CACHE);
    _uidCache = EHCacheUtils.getCacheFromManager(cacheManager, SINGLE_SECURITY_CACHE);
    _bondCache = EHCacheUtils.getCacheFromManager(cacheManager, MULTI_BONDS_CACHE);
    _manager = cacheManager;
    _changeManager = new BasicChangeManager();
    _changeListener = new ChangeListener() {
      
      @Override
      public void entityChanged(ChangeEvent event) {
        if (event.getBeforeId() != null) {
          cleanCaches(event.getBeforeId());
        }
        if (event.getAfterId() != null) {
          cleanCaches(event.getAfterId());
        }
        changeManager().entityChanged(event.getType(), event.getBeforeId(), event.getAfterId(), event.getVersionInstant());
      }
      
    };
    underlying.changeManager().addChangeListener(_changeListener);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying source of securities.
   * 
   * @return the underlying source of securities, not null
   */
  protected FinancialSecuritySource getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the cache manager.
   * 
   * @return the cache manager, not null
   */
  protected CacheManager getCacheManager() {
    return _manager;
  }

  //-------------------------------------------------------------------------
  @Override
  public Security getSecurity(UniqueId uid) {
    ArgumentChecker.notNull(uid, "uid");
    Element e = _uidCache.get(uid);
    Security result = null;
    if (e != null) {
      Serializable value = e.getValue();
      if (value instanceof Security) {
        result = (Security) value;
        s_logger.debug("retrieved security: {} from single-security-cache", result);
      } else {
        s_logger.warn("returned object {} from single-security-cache not a Security", value);
      }
    } else {
      result = getUnderlying().getSecurity(uid);
      if (result != null) {
        _uidCache.put(new Element(uid, result));
      }
    }
    return result;
  }
  
  @Override
  public Security getSecurity(ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    Security result = getUnderlying().getSecurity(objectId, versionCorrection);
    if (result != null) {
      _uidCache.put(new Element(result.getUniqueId(), result));
    }
    return result;
  }

  @Override
  public Collection<Security> getSecurities(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    Collection<Security> result = getUnderlying().getSecurities(bundle);
    if (result == null) {
      return Collections.emptySet();
    }
    cacheSecurities(result);
    return result;
  }
  
  @Override
  public Collection<Security> getSecurities(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    Collection<Security> result = getUnderlying().getSecurities(bundle, versionCorrection);
    if (result == null) {
      return Collections.emptySet();
    }
    cacheSecurities(result);
    return result;
  }

  @Override
  public Security getSecurity(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    Collection<Security> matched = getSecurities(bundle);
    if (matched.isEmpty()) {
      return null;
    }
    return matched.iterator().next();
  }

  @Override
  public Security getSecurity(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    Collection<Security> matched = getSecurities(bundle, versionCorrection);
    if (matched.isEmpty()) {
      return null;
    }
    return matched.iterator().next();
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Collection<Security> getBondsWithIssuerName(String issuerType) {
    ArgumentChecker.notNull(issuerType, "issuerType");
    Element e = _bondCache.get(issuerType);
    Collection<Security> result = new HashSet<Security>();
    if (e != null) {
      Serializable value = e.getValue();
      if (value instanceof Collection<?>) {
        result.addAll((Collection<Security>) value);
      } else {
        s_logger.warn("returned object {} from bond cache is not a Collection<Security>", value);
      }
    } else {
      result = getUnderlying().getBondsWithIssuerName(issuerType);
      if (result != null) {
        _bondCache.put(new Element(issuerType, result));
        cacheSecurities(result);
      }
    }
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Refreshes the value for the specified security key.
   * 
   * @param securityId  the security identifier, not null
   */
  public void refresh(UniqueId securityId) {
    _uidCache.remove(securityId);
  }
  
  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  /**
   * Call this at the end of a unit test run to clear the state of EHCache.
   * It should not be part of a generic lifecycle method.
   */
  protected void shutdown() {
    _underlying.changeManager().removeChangeListener(_changeListener);
    _manager.removeCache(SINGLE_SECURITY_CACHE);
    _manager.shutdown();
  }
  
  //-------------------------------------------------------------------------
  private void cleanCaches(UniqueId id) {
    // Only care where the unversioned ID has been cached since it now represents something else
    UniqueId latestId = id.toLatest();
    _uidCache.remove(latestId);
  }
  
  private void cacheSecurities(Collection<Security> securities) {
    for (Security security : securities) {
      _uidCache.put(new Element(security.getUniqueId(), security));
    }
  }

}
