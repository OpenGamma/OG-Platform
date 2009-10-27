/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.IdentificationDomain;

/**
 * 
 *
 * @author yomi
 */
public class EHCachingSecurityMasterTest {
  
  private SecurityMaster _underlyingSecMaster = null;
  private EHCachingSecurityMaster _cachingSecMaster = null;
  
  DomainSpecificIdentifier _secId1 = new DomainSpecificIdentifier(new IdentificationDomain("d1"), "v1");
  DomainSpecificIdentifier _secId2 = new DomainSpecificIdentifier(new IdentificationDomain("d1"), "v2");

  @Before
  public void setUp() throws Exception {    
    _underlyingSecMaster = new InMemorySecurityMaster();
    _cachingSecMaster = new EHCachingSecurityMaster(_underlyingSecMaster);
  }

  @After
  public void tearDown() throws Exception {
    _underlyingSecMaster = null;
    if (_cachingSecMaster != null) {
      CacheManager cacheManager = _cachingSecMaster.getCacheManager();
      if (cacheManager != null) {
        cacheManager.shutdown();
      }
    }
    _cachingSecMaster = null;
  }
  
  @Test
  public void emptyCache() {
    _underlyingSecMaster = new InMemorySecurityMaster();
     
    SecurityKey secKey = new SecurityKeyImpl(_secId1);
    Security cachedSec = _cachingSecMaster.getSecurity(secKey);
    assertNull(cachedSec);
    
    CacheManager cacheManager = _cachingSecMaster.getCacheManager();
    Cache singleSecCache = cacheManager.getCache(EHCachingSecurityMaster.SINGLE_SECURITY_CACHE);
    assertEquals(0, singleSecCache.getSize());
    Element element = singleSecCache.get(secKey);
    assertNull(element);
    
    Collection<Security> cachedSecurities = _cachingSecMaster.getSecurities(secKey);
    assertNotNull(cachedSecurities);
    assertTrue(cachedSecurities.isEmpty());
    
    assertEquals(0, singleSecCache.getSize());
    element = singleSecCache.get(secKey);
    assertNull(element);
    Cache multiSecCache = cacheManager.getCache(EHCachingSecurityMaster.MULTI_SECURITIES_CACHE);
    assertEquals(0, multiSecCache.getSize());
    element = multiSecCache.get(secKey);
    assertNull(element);
  }

  @Test
  public void getSecurities() {
    addSecuritiesToMemorySecurityMaster();
    
    SecurityKey secKey1 = new SecurityKeyImpl(_secId1);
    SecurityKey secKey2 = new SecurityKeyImpl(_secId2);
    SecurityKey secKey3 = new SecurityKeyImpl(_secId1, _secId2);
    
    Collection<Security> underlyingSecurities = _underlyingSecMaster.getSecurities(secKey1);
    Collection<Security> cachedSecurities = _cachingSecMaster.getSecurities(secKey1);
    assertNotNull(cachedSecurities);
    assertEquals(underlyingSecurities, cachedSecurities);
    
    CacheManager cacheManager = _cachingSecMaster.getCacheManager();
    Cache singleSecCache = cacheManager.getCache(EHCachingSecurityMaster.SINGLE_SECURITY_CACHE);
    assertEquals(1, singleSecCache.getSize());
    Cache multiSecCache = cacheManager.getCache(EHCachingSecurityMaster.MULTI_SECURITIES_CACHE);
    assertEquals(1, multiSecCache.getSize());
    
    Element multiElement = multiSecCache.getQuiet(secKey1);
    Element sec1Element = singleSecCache.getQuiet(secKey1);
    assertNotNull(sec1Element);
    assertNotNull(multiElement);
    for (int i = 1; i < 10; i++) {
      cachedSecurities = _cachingSecMaster.getSecurities(secKey1);
      assertNotNull(cachedSecurities);
      assertEquals(i, multiElement.getHitCount());
      assertEquals(i, sec1Element.getHitCount());
    }
    
    underlyingSecurities = _underlyingSecMaster.getSecurities(secKey3);
    cachedSecurities = _cachingSecMaster.getSecurities(secKey3);
    assertNotNull(cachedSecurities);
    
    assertEquals(underlyingSecurities, cachedSecurities);
    assertEquals(2, singleSecCache.getSize());
    assertEquals(2, multiSecCache.getSize());
    
    multiElement = multiSecCache.getQuiet(secKey3);
    sec1Element = singleSecCache.getQuiet(secKey1);
    Element sec2Element = singleSecCache.getQuiet(secKey2);
    assertNotNull(sec1Element);
    assertNotNull(sec2Element);
    assertNotNull(multiElement);
    int lastHit = (int)sec1Element.getHitCount();
    for (int i = 1; i < 10; i++) {
      cachedSecurities = _cachingSecMaster.getSecurities(secKey3);
      assertNotNull(cachedSecurities);
      assertEquals(i, multiElement.getHitCount());
      assertEquals(lastHit + i, sec1Element.getHitCount());
      assertEquals(i, sec2Element.getHitCount());
    }
        
  }

  @Test
  public void getSecurity() {
    addSecuritiesToMemorySecurityMaster();
    
    SecurityKey secKey1 = new SecurityKeyImpl(_secId1);
    SecurityKey secKey2 = new SecurityKeyImpl(_secId2);
    SecurityKey secKey3 = new SecurityKeyImpl(_secId1, _secId2);
    
    Security underlyingSec = _underlyingSecMaster.getSecurity(secKey1);
    Security cachedSec = _cachingSecMaster.getSecurity(secKey1);
    assertNotNull(cachedSec);
    assertSame(underlyingSec, cachedSec);
    
    CacheManager cacheManager = _cachingSecMaster.getCacheManager();
    Cache singleSecCache = cacheManager.getCache(EHCachingSecurityMaster.SINGLE_SECURITY_CACHE);
    assertEquals(1, singleSecCache.getSize());
    Element element = singleSecCache.getQuiet(secKey1);
    assertNotNull(element);
    for (int i = 1; i < 10; i++) {
      cachedSec = _cachingSecMaster.getSecurity(secKey1);
      assertNotNull(cachedSec);
      assertEquals(i, element.getHitCount());
    }
    
    underlyingSec = _underlyingSecMaster.getSecurity(secKey2);
    cachedSec = _cachingSecMaster.getSecurity(secKey2);
    assertNotNull(underlyingSec);
    assertNotNull(cachedSec);
    assertSame(underlyingSec, cachedSec);
    assertEquals(2, singleSecCache.getSize());
    element = singleSecCache.getQuiet(secKey2);
    assertNotNull(element);
    for (int i = 1; i < 10; i++) {
      cachedSec = _cachingSecMaster.getSecurity(secKey2);
      assertNotNull(cachedSec);
      assertEquals(i, element.getHitCount());
    }
    
    underlyingSec = _underlyingSecMaster.getSecurity(secKey3);
    cachedSec = _cachingSecMaster.getSecurity(secKey3);
    assertNotNull(cachedSec);
    assertSame(underlyingSec, cachedSec);
    assertEquals(3, singleSecCache.getSize());
    element = singleSecCache.getQuiet(secKey3);
    assertNotNull(element);
    for (int i = 1; i < 10; i++) {
      cachedSec = _cachingSecMaster.getSecurity(secKey3);
      assertNotNull(cachedSec);
      assertEquals(i, element.getHitCount());
    }
    
    Cache multiSecCache = cacheManager.getCache(EHCachingSecurityMaster.MULTI_SECURITIES_CACHE);
    assertEquals(0, multiSecCache.getSize());
    
  }

  @Test
  public void refresh() {
    addSecuritiesToMemorySecurityMaster();
    
    SecurityKey secKey1 = new SecurityKeyImpl(_secId1);
    SecurityKey secKey2 = new SecurityKeyImpl(_secId2);
    SecurityKey secKey3 = new SecurityKeyImpl(_secId1, _secId2);
    
    _cachingSecMaster.getSecurity(secKey1);
    
    Cache singleSecCache = _cachingSecMaster.getCacheManager().getCache(EHCachingSecurityMaster.SINGLE_SECURITY_CACHE);
    assertEquals(1, singleSecCache.getSize());
    Element sec1Element = singleSecCache.getQuiet(secKey1);
    assertNotNull(sec1Element);
    for (int i = 1; i < 10; i++) {
      _cachingSecMaster.getSecurity(secKey1);
      assertEquals(i, sec1Element.getHitCount());
    }
    
    //remove security1 from cache
    _cachingSecMaster.refresh(secKey1);
    assertEquals(0, singleSecCache.getSize());
    
    _cachingSecMaster.getSecurity(secKey1);
    assertEquals(1, singleSecCache.getSize());
    sec1Element = singleSecCache.getQuiet(secKey1);
    assertNotNull(sec1Element);
    for (int i = 1; i < 10; i++) {
      _cachingSecMaster.getSecurity(secKey1);
      assertEquals(i, sec1Element.getHitCount());
    }
    
    _cachingSecMaster.getSecurities(secKey3);
    Cache multiSecCache = _cachingSecMaster.getCacheManager().getCache(EHCachingSecurityMaster.MULTI_SECURITIES_CACHE);
    assertEquals(2, singleSecCache.getSize());
    assertEquals(1, multiSecCache.getSize());
    
    sec1Element = singleSecCache.getQuiet(secKey1);
    Element multiElement = multiSecCache.getQuiet(secKey3);
    Element sec2Element = singleSecCache.getQuiet(secKey2);
    assertNotNull(sec1Element);
    assertNotNull(sec2Element);
    assertNotNull(multiElement);
    int lastHit = (int)sec1Element.getHitCount();
    for (int i = 1; i < 10; i++) {
      _cachingSecMaster.getSecurities(secKey3);
      assertEquals(lastHit + i, sec1Element.getHitCount());
      assertEquals(i, sec2Element.getHitCount());
      assertEquals(i, multiElement.getHitCount());
    }
    
    //remove security3 from cache
    _cachingSecMaster.refresh(secKey3);
    assertEquals(0, multiSecCache.getSize());
    assertEquals(0, singleSecCache.getSize());
    
    _cachingSecMaster.getSecurities(secKey3);
    sec1Element = singleSecCache.getQuiet(secKey1);
    sec2Element = singleSecCache.getQuiet(secKey2);
    multiElement = multiSecCache.getQuiet(secKey3);
    assertNotNull(sec1Element);
    assertNotNull(sec2Element);
    assertNotNull(multiElement);
    for (int i = 1; i < 10; i++) {
      _cachingSecMaster.getSecurities(secKey3);
      assertEquals(i, multiElement.getHitCount());
      assertEquals(i, sec1Element.getHitCount());
      assertEquals(i, sec2Element.getHitCount());
    }
     
  }
  
  /**
   * @return
   */
  private void addSecuritiesToMemorySecurityMaster() {
    InMemorySecurityMaster secMaster = (InMemorySecurityMaster)_underlyingSecMaster;
    DefaultSecurity sec1 = new DefaultSecurity();
    sec1.setIdentifiers(Collections.singleton(_secId1));
    secMaster.add(sec1);
    
    DefaultSecurity sec2 = new DefaultSecurity();
    sec2.setIdentifiers(Collections.singleton(_secId2));
    secMaster.add(sec2);
  }

}
