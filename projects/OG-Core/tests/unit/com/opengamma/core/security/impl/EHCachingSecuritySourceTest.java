/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.security.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Collection;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.test.MockSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * Test EHCachingSecuritySource.
 */
public class EHCachingSecuritySourceTest {

  private MockSecuritySource _underlyingSecuritySource = null;
  private EHCachingSecuritySource _cachingSecuritySource = null;
  private Identifier _secId1 = Identifier.of("d1", "v1");
  private Identifier _secId2 = Identifier.of("d1", "v2");
  private MockSecurity _security1 = new MockSecurity("");
  private MockSecurity _security2 = new MockSecurity("");

  @Before
  public void setUp() throws Exception {    
    _underlyingSecuritySource = new MockSecuritySource();
    _cachingSecuritySource = new EHCachingSecuritySource(_underlyingSecuritySource, EHCacheUtils.createCacheManager ());
    
    _security1.addIdentifier(_secId1);
    _security2.addIdentifier(_secId2);
  }

  @After
  public void tearDown() throws Exception {
    _underlyingSecuritySource = null;
    if (_cachingSecuritySource != null) {
      _cachingSecuritySource.shutdown();
    }
    _cachingSecuritySource = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void getSecurity_UniqueIdentifier() {
    addSecuritiesToMock(_security1, _security2);
    
    UniqueIdentifier uid1 = _security1.getUniqueId();
    Security underlyingSec = _underlyingSecuritySource.getSecurity(uid1);
    Security cachedSec = _cachingSecuritySource.getSecurity(uid1);
    assertNotNull(underlyingSec);
    assertNotNull(cachedSec);
    assertSame(underlyingSec, cachedSec);
    
    CacheManager cacheManager = _cachingSecuritySource.getCacheManager();
    Cache singleSecCache = cacheManager.getCache(EHCachingSecuritySource.SINGLE_SECURITY_CACHE);
    assertEquals(1, singleSecCache.getSize());
    Element element = singleSecCache.getQuiet(uid1);
    assertNotNull(element);
    for (int i = 1; i < 10; i++) {
      cachedSec = _cachingSecuritySource.getSecurity(uid1);
      assertNotNull(cachedSec);
      assertEquals(i, element.getHitCount());
    }
    Cache multiSecCache = cacheManager.getCache(EHCachingSecuritySource.MULTI_SECURITIES_CACHE);
    assertEquals(0, multiSecCache.getSize());
  }

  @Test
  public void getSecurity_UniqueIdentifier_empty() {
    CacheManager cacheManager = _cachingSecuritySource.getCacheManager();
    Cache singleSecCache = cacheManager.getCache(EHCachingSecuritySource.SINGLE_SECURITY_CACHE);
    Cache multiSecCache = cacheManager.getCache(EHCachingSecuritySource.MULTI_SECURITIES_CACHE);
    
    UniqueIdentifier uid = UniqueIdentifier.of("Mock", "99");
    Security cachedSec = _cachingSecuritySource.getSecurity(uid);
    assertNull(cachedSec);
    assertEquals(0, singleSecCache.getSize());
    assertEquals(0, multiSecCache.getSize());
    Element element = singleSecCache.get(uid);
    assertNull(element);
  }

  //-------------------------------------------------------------------------
  @Test
  public void getSecurities_IdentifierBundle() {
    addSecuritiesToMock(_security1, _security2);
    IdentifierBundle secKey = IdentifierBundle.of(_secId1, _secId2);
    
    Collection<Security> underlyingSecurities = _underlyingSecuritySource.getSecurities(secKey);
    assertNotNull(underlyingSecurities);
    Collection<Security> cachedSecurities = _cachingSecuritySource.getSecurities(secKey);
    assertNotNull(cachedSecurities);
    assertEquals(underlyingSecurities, cachedSecurities);
    
    CacheManager cacheManager = _cachingSecuritySource.getCacheManager();
    assertNotNull(cacheManager);
    Cache singleSecCache = cacheManager.getCache(EHCachingSecuritySource.SINGLE_SECURITY_CACHE);
    assertNotNull(singleSecCache);
    assertEquals(2, singleSecCache.getSize());
    Cache multiSecCache = cacheManager.getCache(EHCachingSecuritySource.MULTI_SECURITIES_CACHE);
    assertNotNull(multiSecCache);
    assertEquals(1, multiSecCache.getSize());
    
    Element multiElement = multiSecCache.getQuiet(secKey);
    Element sec1Element = singleSecCache.getQuiet(_security1.getUniqueId());
    Element sec2Element = singleSecCache.getQuiet(_security2.getUniqueId());
    assertNotNull(multiElement);
    assertNotNull(sec1Element);
    assertNotNull(sec2Element);
    for (int i = 1; i < 10; i++) {
      _cachingSecuritySource.getSecurities(secKey);
      assertEquals(i, multiElement.getHitCount());
      assertEquals(0, sec1Element.getHitCount());
      assertEquals(0, sec2Element.getHitCount());
    }
  }

  @Test
  public void getSecurities_IdentifierBundle_empty() {
    IdentifierBundle secKey = IdentifierBundle.of(_secId1);
    CacheManager cacheManager = _cachingSecuritySource.getCacheManager();
    Cache singleSecCache = cacheManager.getCache(EHCachingSecuritySource.SINGLE_SECURITY_CACHE);
    Cache multiSecCache = cacheManager.getCache(EHCachingSecuritySource.MULTI_SECURITIES_CACHE);
    
    Security cachedSec = _cachingSecuritySource.getSecurity(secKey);
    assertNull(cachedSec);
    assertEquals(0, singleSecCache.getSize());
    assertEquals(1, multiSecCache.getSize());
    Element element = multiSecCache.get(secKey);
    assertEquals(0, ((Collection<?>) element.getValue()).size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void getSecurity_IdentifierBundle() {
    addSecuritiesToMock(_security1, _security2);
    
    IdentifierBundle secKey1 = IdentifierBundle.of(_secId1);
    Security underlyingSec = _underlyingSecuritySource.getSecurity(secKey1);
    Security cachedSec = _cachingSecuritySource.getSecurity(secKey1);
    assertNotNull(underlyingSec);
    assertNotNull(cachedSec);
    assertSame(underlyingSec, cachedSec);
    
    CacheManager cacheManager = _cachingSecuritySource.getCacheManager();
    assertNotNull(cacheManager);
    Cache singleSecCache = cacheManager.getCache(EHCachingSecuritySource.SINGLE_SECURITY_CACHE);
    assertNotNull(singleSecCache);
    assertEquals(1, singleSecCache.getSize());
    Cache multiSecCache = cacheManager.getCache(EHCachingSecuritySource.MULTI_SECURITIES_CACHE);
    assertNotNull(multiSecCache);
    assertEquals(1, multiSecCache.getSize());
    
    Element multiElement = multiSecCache.getQuiet(secKey1);
    Element sec1Element = singleSecCache.getQuiet(_security1.getUniqueId());
    assertNotNull(multiElement);
    assertNotNull(sec1Element);
    for (int i = 1; i < 10; i++) {
      _cachingSecuritySource.getSecurities(secKey1);
      assertEquals(i, multiElement.getHitCount());
      assertEquals(0, sec1Element.getHitCount());
    }
  }

  @Test
  public void getSecurity_IdentifierBundle_empty() {
    IdentifierBundle secKey = IdentifierBundle.of(_secId1);
    CacheManager cacheManager = _cachingSecuritySource.getCacheManager();
    Cache singleSecCache = cacheManager.getCache(EHCachingSecuritySource.SINGLE_SECURITY_CACHE);
    Cache multiSecCache = cacheManager.getCache(EHCachingSecuritySource.MULTI_SECURITIES_CACHE);
    
    Security cachedSec = _cachingSecuritySource.getSecurity(secKey);
    assertNull(cachedSec);
    assertEquals(0, singleSecCache.getSize());
    assertEquals(1, multiSecCache.getSize());
    Element element = multiSecCache.get(secKey);
    assertEquals(0, ((Collection<?>) element.getValue()).size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void refreshGetSecurity_UniqueIdentity() {
    addSecuritiesToMock(_security1, _security2);
    UniqueIdentifier uid1 = _security1.getUniqueId();
    _cachingSecuritySource.getSecurity(uid1);
    Cache singleSecCache = _cachingSecuritySource.getCacheManager().getCache(EHCachingSecuritySource.SINGLE_SECURITY_CACHE);
    assertEquals(1, singleSecCache.getSize());
    Element sec1Element = singleSecCache.getQuiet(uid1);
    assertNotNull(sec1Element);
    for (int i = 1; i < 10; i++) {
      _cachingSecuritySource.getSecurity(uid1);
      assertEquals(i, sec1Element.getHitCount());
    }
    _cachingSecuritySource.refresh(uid1);
    assertEquals(0, singleSecCache.getSize());
    sec1Element = singleSecCache.getQuiet(uid1);
    assertNull(sec1Element);
    _cachingSecuritySource.getSecurity(uid1);
    assertEquals(1, singleSecCache.getSize());
    sec1Element = singleSecCache.getQuiet(uid1);
    assertNotNull(sec1Element);
    for (int i = 1; i < 10; i++) {
      _cachingSecuritySource.getSecurity(uid1);
      assertEquals(i, sec1Element.getHitCount());
    }
  }
   
  @Test
  public void refreshGetSecurities_IdentifierBundle() {
    addSecuritiesToMock(_security1, _security2);
    IdentifierBundle secKey = IdentifierBundle.of(_secId1, _secId2);
    _cachingSecuritySource.getSecurities(secKey);
    Cache singleSecCache = _cachingSecuritySource.getCacheManager().getCache(EHCachingSecuritySource.SINGLE_SECURITY_CACHE);
    Cache multiSecCache = _cachingSecuritySource.getCacheManager().getCache(EHCachingSecuritySource.MULTI_SECURITIES_CACHE);
    assertEquals(2, singleSecCache.getSize());
    assertEquals(1, multiSecCache.getSize());
    
    Element sec1Element = singleSecCache.getQuiet(_security1.getUniqueId());
    Element sec2Element = singleSecCache.getQuiet(_security2.getUniqueId());
    Element multiElement = multiSecCache.getQuiet(secKey);
    assertNotNull(sec1Element);
    assertNotNull(sec2Element);
    assertNotNull(multiElement);
    for (int i = 1; i < 10; i++) {
      _cachingSecuritySource.getSecurities(secKey);
      assertEquals(0, sec1Element.getHitCount());
      assertEquals(0, sec2Element.getHitCount());
      assertEquals(i, multiElement.getHitCount());
    }
    
    _cachingSecuritySource.refresh(secKey);
    assertEquals(0, multiSecCache.getSize());
    assertEquals(0, singleSecCache.getSize());
    sec1Element = singleSecCache.getQuiet(_security1.getUniqueId());
    sec2Element = singleSecCache.getQuiet(_security2.getUniqueId());
    multiElement = multiSecCache.getQuiet(secKey);
    assertNull(sec1Element);
    assertNull(sec2Element);
    assertNull(multiElement);
    
    _cachingSecuritySource.getSecurities(secKey);
    sec1Element = singleSecCache.getQuiet(_security1.getUniqueId());
    sec2Element = singleSecCache.getQuiet(_security2.getUniqueId());
    multiElement = multiSecCache.getQuiet(secKey);
    assertNotNull(sec1Element);
    assertNotNull(sec2Element);
    assertNotNull(multiElement);
    for (int i = 1; i < 10; i++) {
      _cachingSecuritySource.getSecurities(secKey);
      assertEquals(i, multiElement.getHitCount());
      assertEquals(0, sec1Element.getHitCount());
      assertEquals(0, sec2Element.getHitCount());
    }
  }
    
  @Test
  public void refreshGetSecurity_IdentifierBundle() {
    addSecuritiesToMock(_security1, _security2);
    
    IdentifierBundle secKey1 = IdentifierBundle.of(_secId1);
    _cachingSecuritySource.getSecurity(secKey1);
    Cache multiSecCache = _cachingSecuritySource.getCacheManager().getCache(EHCachingSecuritySource.MULTI_SECURITIES_CACHE);
    assertEquals(1, multiSecCache.getSize());
    Element multiElement = multiSecCache.getQuiet(secKey1);
    assertNotNull(multiElement);
    for (int i = 1; i < 10; i++) {
      _cachingSecuritySource.getSecurity(secKey1);
      assertEquals(i, multiElement.getHitCount());
    }
    _cachingSecuritySource.refresh(secKey1);
    assertEquals(0, multiSecCache.getSize());
    multiElement = multiSecCache.getQuiet(secKey1);
    assertNull(multiElement);
    _cachingSecuritySource.getSecurity(secKey1);
    assertEquals(1, multiSecCache.getSize());
    multiElement = multiSecCache.getQuiet(secKey1);
    assertNotNull(multiElement);
    for (int i = 1; i < 10; i++) {
      _cachingSecuritySource.getSecurity(secKey1);
      assertEquals(i, multiElement.getHitCount());
    }
  }

  private void addSecuritiesToMock(Security ... securities) {
    for (Security security : securities) {
      _underlyingSecuritySource.addSecurity(security);
    }
  }

}
