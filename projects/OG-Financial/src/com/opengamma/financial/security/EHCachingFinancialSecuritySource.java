/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.time.Instant;
import javax.time.calendar.Period;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.security.AbstractSecuritySource;
import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.tuple.Triple;

/**
 * A cache decorating a {@code FinancialSecuritySource}.
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public class EHCachingFinancialSecuritySource extends AbstractSecuritySource implements FinancialSecuritySource {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(EHCachingFinancialSecuritySource.class);
  /** The single security cache key. */
  /* package for testing */static final String SINGLE_SECURITY_CACHE = "single-security-cache";
  /** The multiple security cache key. */
  /* package for testing */static final String MULTI_SECURITIES_CACHE = "multi-securities-cache";
  /** The mulitple bonds cache key */
  /* package for testing */static final String MULTI_BONDS_CACHE = "multi-bonds-cache";
  /** The Bundle hint cache key. */
  /* package for testing */static final String BUNDLE_HINT_SECURITIES_CACHE = "multi-securities-hint-cache";

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
   * The multiple security cache.
   */
  private final Cache _bundleCache;
  /**
   * The bond cache.
   */
  private final Cache _bondCache;
  /**
   * The bundle hint cache.
   */
  private final Cache _bundleHintCache;
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
    EHCacheUtils.addCache(cacheManager, MULTI_SECURITIES_CACHE);
    EHCacheUtils.addCache(cacheManager, MULTI_BONDS_CACHE);
    EHCacheUtils.addCache(cacheManager, BUNDLE_HINT_SECURITIES_CACHE);
    _uidCache = EHCacheUtils.getCacheFromManager(cacheManager, SINGLE_SECURITY_CACHE);
    _bundleCache = EHCacheUtils.getCacheFromManager(cacheManager, MULTI_SECURITIES_CACHE);
    _bundleHintCache = EHCacheUtils.getCacheFromManager(cacheManager, BUNDLE_HINT_SECURITIES_CACHE);
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
      _uidCache.put(new Element(uid, result));
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private Map<VersionCorrection, Security> getObjectIdCacheEntry(final ObjectId objectId) {
    final Element e = _uidCache.get(objectId);
    if (e != null) {
      Serializable value = e.getValue();
      if (value instanceof Map<?, ?>) {
        return (Map<VersionCorrection, Security>) value;
      }
    }
    return null;
  }

  @Override
  public Security getSecurity(ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    Map<VersionCorrection, Security> securities = getObjectIdCacheEntry(objectId);
    Security result;
    if (securities != null) {
      result = securities.get(versionCorrection);
    } else {
      result = null;
    }
    if (result == null) {
      result = getUnderlying().getSecurity(objectId, versionCorrection);
      if (result != null) {
        _uidCache.put(new Element(result.getUniqueId(), result));
      }
      final Map<VersionCorrection, Security> newSecurities = new HashMap<VersionCorrection, Security>();
      synchronized (this) {
        securities = getObjectIdCacheEntry(objectId);
        if (securities != null) {
          newSecurities.putAll(securities);
        }
        newSecurities.put(versionCorrection, result);
        _uidCache.put(new Element(objectId, newSecurities));
      }
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Security> getSecurities(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    Element e = _bundleCache.get(bundle);
    Collection<Security> result = new HashSet<Security>();
    if (e != null) {
      Serializable value = e.getValue();
      if (value instanceof Collection<?>) {
        result.addAll((Collection<Security>) value);
      } else {
        s_logger.warn("returned object {} from cache is not a Collection<Security>", value);
      }
    } else {
      result = getUnderlying().getSecurities(bundle);
      if (result != null) {
        _bundleCache.put(new Element(bundle, result));
        cacheSecurities(result);
      }
    }
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

  static final Period s_versionCorrectionBucketSize = Period.ofDays(1); //This is an attempt to avoid thrashing if we get conflicting queries
  static final BigInteger s_versionCorrectionBucketSizeInNanos = BigInteger.valueOf(s_versionCorrectionBucketSize.totalNanosWith24HourDays());
  static final int s_versionCorrectionBucketSizeInSeconds = (int) Period.ofDays(1).totalSecondsWith24HourDays();
  static final BigInteger s_latestBucket = BigInteger.valueOf(-1);

  @Override
  public Security getSecurity(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    Instant correctedTo = versionCorrection.getCorrectedTo();
    Instant versionAsOf = versionCorrection.getVersionAsOf();
    BigInteger correctedToBucket = correctedTo == null ? s_latestBucket : versionCorrection.getCorrectedTo().toEpochNanos().divide(s_versionCorrectionBucketSizeInNanos);
    BigInteger versionAsOfBucket = versionAsOf == null ? s_latestBucket : versionCorrection.getVersionAsOf().toEpochNanos().divide(s_versionCorrectionBucketSizeInNanos);
    Triple<ExternalIdBundle, BigInteger, BigInteger> key = Triple.of(bundle, correctedToBucket, versionAsOfBucket);

    Element hintUid = _bundleHintCache.get(key);
    if (hintUid != null) {
      ObjectId hint = (ObjectId) hintUid.getValue();
      try {
        //Caching is based on the idea that this query is significantly faster
        Security candidate = getSecurity(hint, versionCorrection);
        if (candidate.getExternalIdBundle().containsAny(bundle)) {
          //This is a good enough result with the current resolution logic,
          // h'ver as soon as we have rules about which of multiple matches to use this caching must be rewritten
          return candidate;
        }
      } catch (DataNotFoundException dnfe) {
        s_logger.debug("Hinted security {} has dissapeared", hint);
      }
    }

    Collection<Security> matched = getSecurities(bundle, versionCorrection);
    if (matched.isEmpty()) {
      return null;
    }
    Security ret = matched.iterator().next();
    Element element = new Element(key, ret.getUniqueId().getObjectId());
    element.setTimeToLive(s_versionCorrectionBucketSizeInSeconds);
    _bundleHintCache.put(element);
    return ret;
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
   * @param securityKey  the security key, not null
   */
  @SuppressWarnings("unchecked")
  public void refresh(Object securityKey) {
    ArgumentChecker.notNull(securityKey, "securityKey");
    Element element = _bundleCache.get(securityKey);
    if (element != null) {
      Serializable value = element.getValue();
      if (value instanceof Collection<?>) {
        Collection<Security> securities = (Collection<Security>) value;
        for (Security sec : securities) {
          _uidCache.remove(sec.getUniqueId());
        }
      }
      _bundleCache.remove(securityKey);
    } else {
      _uidCache.remove(securityKey);
    }
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
    _manager.removeCache(MULTI_SECURITIES_CACHE);
    _manager.removeCache(MULTI_BONDS_CACHE);
    _manager.removeCache(BUNDLE_HINT_SECURITIES_CACHE);
  }

  //-------------------------------------------------------------------------
  private void cleanCaches(UniqueId id) {
    // Only care where the unversioned ID has been cached since it now represents something else
    UniqueId latestId = id.toLatest();
    _uidCache.remove(latestId);
    // Destroy all version/correction cached values for the object
    _uidCache.remove(id.getObjectId());
  }

  private void cacheSecurities(Collection<Security> securities) {
    for (Security security : securities) {
      _uidCache.put(new Element(security.getUniqueId(), security));
    }
  }

}
