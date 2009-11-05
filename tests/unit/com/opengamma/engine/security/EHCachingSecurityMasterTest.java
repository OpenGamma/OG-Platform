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
  
  private DomainSpecificIdentifier _secId1 = new DomainSpecificIdentifier(new IdentificationDomain("d1"), "v1");
  private DomainSpecificIdentifier _secId2 = new DomainSpecificIdentifier(new IdentificationDomain("d1"), "v2");
  
  private DefaultSecurity _security1 = new DefaultSecurity();
  private DefaultSecurity _security2 = new DefaultSecurity();

  @Before
  public void setUp() throws Exception {    
    _underlyingSecMaster = new InMemorySecurityMaster();
    _cachingSecMaster = new EHCachingSecurityMaster(_underlyingSecMaster);
    
    _security1.setIdentifiers(Collections.singleton(_secId1));
    _security2.setIdentifiers(Collections.singleton(_secId2));
    
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
    
    String identityKey = _security1.getIdentityKey();
    cachedSec = _cachingSecMaster.getSecurity(identityKey);
    assertEquals(0, singleSecCache.getSize());
    element = singleSecCache.get(identityKey);
    assertNull(element);
  }

  @Test
  public void getSecurities() {
    addSecuritiesToMemorySecurityMaster(_security1, _security2);
    SecurityKey secKey = new SecurityKeyImpl(_secId1, _secId2);
    
    Collection<Security> underlyingSecurities = _underlyingSecMaster.getSecurities(secKey);
    assertNotNull(underlyingSecurities);
    Collection<Security> cachedSecurities = _cachingSecMaster.getSecurities(secKey);
    assertNotNull(cachedSecurities);
    assertEquals(underlyingSecurities, cachedSecurities);
    
    CacheManager cacheManager = _cachingSecMaster.getCacheManager();
    assertNotNull(cacheManager);
    Cache singleSecCache = cacheManager.getCache(EHCachingSecurityMaster.SINGLE_SECURITY_CACHE);
    assertNotNull(singleSecCache);
    assertEquals(2, singleSecCache.getSize());
    Cache multiSecCache = cacheManager.getCache(EHCachingSecurityMaster.MULTI_SECURITIES_CACHE);
    assertNotNull(multiSecCache);
    assertEquals(1, multiSecCache.getSize());
    
    Element multiElement = multiSecCache.getQuiet(secKey);
    Element sec1Element = singleSecCache.getQuiet(_security1.getIdentityKey());
    Element sec2Element = singleSecCache.getQuiet(_security2.getIdentityKey());
    assertNotNull(multiElement);
    assertNotNull(sec1Element);
    assertNotNull(sec2Element);
    for (int i = 1; i < 10; i++) {
      _cachingSecMaster.getSecurities(secKey);
      assertEquals(i, multiElement.getHitCount());
      assertEquals(i, sec1Element.getHitCount());
      assertEquals(i, sec2Element.getHitCount());
    }
  }

  @Test
  public void getSecurityBySecurityKey() {
    addSecuritiesToMemorySecurityMaster(_security1, _security2);
    
    SecurityKey secKey1 = new SecurityKeyImpl(_secId1);
    Security underlyingSec = _underlyingSecMaster.getSecurity(secKey1);
    Security cachedSec = _cachingSecMaster.getSecurity(secKey1);
    assertNotNull(underlyingSec);
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
    Cache multiSecCache = cacheManager.getCache(EHCachingSecurityMaster.MULTI_SECURITIES_CACHE);
    assertEquals(0, multiSecCache.getSize());
  }
  
  @Test
  public void getSecurityByIdentityKey() {
    addSecuritiesToMemorySecurityMaster(_security1, _security2);
    
    String identityKey1 = _security1.getIdentityKey();
    Security underlyingSec = _underlyingSecMaster.getSecurity(identityKey1);
    Security cachedSec = _cachingSecMaster.getSecurity(identityKey1);
    assertNotNull(underlyingSec);
    assertNotNull(cachedSec);
    assertSame(underlyingSec, cachedSec);
    
    CacheManager cacheManager = _cachingSecMaster.getCacheManager();
    Cache singleSecCache = cacheManager.getCache(EHCachingSecurityMaster.SINGLE_SECURITY_CACHE);
    assertEquals(1, singleSecCache.getSize());
    Element element = singleSecCache.getQuiet(identityKey1);
    assertNotNull(element);
    for (int i = 1; i < 10; i++) {
      cachedSec = _cachingSecMaster.getSecurity(identityKey1);
      assertNotNull(cachedSec);
      assertEquals(i, element.getHitCount());
    }
    Cache multiSecCache = cacheManager.getCache(EHCachingSecurityMaster.MULTI_SECURITIES_CACHE);
    assertEquals(0, multiSecCache.getSize());
  }

  @Test
  public void refreshGetSecurityBySecurityKey() {
    addSecuritiesToMemorySecurityMaster(_security1, _security2);
    
    SecurityKey secKey1 = new SecurityKeyImpl(_secId1);
    _cachingSecMaster.getSecurity(secKey1);
    Cache singleSecCache = _cachingSecMaster.getCacheManager().getCache(EHCachingSecurityMaster.SINGLE_SECURITY_CACHE);
    assertEquals(1, singleSecCache.getSize());
    Element sec1Element = singleSecCache.getQuiet(secKey1);
    assertNotNull(sec1Element);
    for (int i = 1; i < 10; i++) {
      _cachingSecMaster.getSecurity(secKey1);
      assertEquals(i, sec1Element.getHitCount());
    }
    _cachingSecMaster.refresh(secKey1);
    assertEquals(0, singleSecCache.getSize());
    sec1Element = singleSecCache.getQuiet(secKey1);
    assertNull(sec1Element);
    _cachingSecMaster.getSecurity(secKey1);
    assertEquals(1, singleSecCache.getSize());
    sec1Element = singleSecCache.getQuiet(secKey1);
    assertNotNull(sec1Element);
    for (int i = 1; i < 10; i++) {
      _cachingSecMaster.getSecurity(secKey1);
      assertEquals(i, sec1Element.getHitCount());
    }
  }
  
  @Test
  public void refreshGetSecurityByIdentityKey() {
    addSecuritiesToMemorySecurityMaster(_security1, _security2);
    String identityKey1 = _security1.getIdentityKey();
    _cachingSecMaster.getSecurity(identityKey1);
    Cache singleSecCache = _cachingSecMaster.getCacheManager().getCache(EHCachingSecurityMaster.SINGLE_SECURITY_CACHE);
    assertEquals(1, singleSecCache.getSize());
    Element sec1Element = singleSecCache.getQuiet(identityKey1);
    assertNotNull(sec1Element);
    for (int i = 1; i < 10; i++) {
      _cachingSecMaster.getSecurity(identityKey1);
      assertEquals(i, sec1Element.getHitCount());
    }
    _cachingSecMaster.refresh(identityKey1);
    assertEquals(0, singleSecCache.getSize());
    sec1Element = singleSecCache.getQuiet(identityKey1);
    assertNull(sec1Element);
    _cachingSecMaster.getSecurity(identityKey1);
    assertEquals(1, singleSecCache.getSize());
    sec1Element = singleSecCache.getQuiet(identityKey1);
    assertNotNull(sec1Element);
    for (int i = 1; i < 10; i++) {
      _cachingSecMaster.getSecurity(identityKey1);
      assertEquals(i, sec1Element.getHitCount());
    }
  }
   
  @Test
  public void refreshGetSecuritiesBySecurityKey() {
    addSecuritiesToMemorySecurityMaster(_security1, _security2);
    SecurityKey secKey = new SecurityKeyImpl(_secId1, _secId2);
    _cachingSecMaster.getSecurities(secKey);
    Cache singleSecCache = _cachingSecMaster.getCacheManager().getCache(EHCachingSecurityMaster.SINGLE_SECURITY_CACHE);
    Cache multiSecCache = _cachingSecMaster.getCacheManager().getCache(EHCachingSecurityMaster.MULTI_SECURITIES_CACHE);
    assertEquals(2, singleSecCache.getSize());
    assertEquals(1, multiSecCache.getSize());
    
    Element sec1Element = singleSecCache.getQuiet(_security1.getIdentityKey());
    Element sec2Element = singleSecCache.getQuiet(_security2.getIdentityKey());
    Element multiElement = multiSecCache.getQuiet(secKey);
    assertNotNull(sec1Element);
    assertNotNull(sec2Element);
    assertNotNull(multiElement);
    for (int i = 1; i < 10; i++) {
      _cachingSecMaster.getSecurities(secKey);
      assertEquals(i, sec1Element.getHitCount());
      assertEquals(i, sec2Element.getHitCount());
      assertEquals(i, multiElement.getHitCount());
    }
    
    _cachingSecMaster.refresh(secKey);
    assertEquals(0, multiSecCache.getSize());
    assertEquals(0, singleSecCache.getSize());
    sec1Element = singleSecCache.getQuiet(_security1.getIdentityKey());
    sec2Element = singleSecCache.getQuiet(_security2.getIdentityKey());
    multiElement = multiSecCache.getQuiet(secKey);
    assertNull(sec1Element);
    assertNull(sec2Element);
    assertNull(multiElement);
    
    
    _cachingSecMaster.getSecurities(secKey);
    sec1Element = singleSecCache.getQuiet(_security1.getIdentityKey());
    sec2Element = singleSecCache.getQuiet(_security2.getIdentityKey());
    multiElement = multiSecCache.getQuiet(secKey);
    assertNotNull(sec1Element);
    assertNotNull(sec2Element);
    assertNotNull(multiElement);
    for (int i = 1; i < 10; i++) {
      _cachingSecMaster.getSecurities(secKey);
      assertEquals(i, multiElement.getHitCount());
      assertEquals(i, sec1Element.getHitCount());
      assertEquals(i, sec2Element.getHitCount());
    }
  }
    
  private void addSecuritiesToMemorySecurityMaster(DefaultSecurity ... securities) {
    InMemorySecurityMaster secMaster = (InMemorySecurityMaster)_underlyingSecMaster;
    for (DefaultSecurity security : securities) {
      secMaster.add(security);
    }
  }

}
