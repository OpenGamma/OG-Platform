/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertSame;

import javax.time.Instant;

import net.sf.ehcache.CacheManager;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * Test {@link EHCachingConfigSource}.
 */
@Test
public class EHCachingConfigSourceTest {

  private static final VersionCorrection VC = VersionCorrection.LATEST;
  private static final ExternalId CONFIG = ExternalId.of ("Test", "sec1");
  private static final String CONFIG_NAME = "Test";
  private static final UniqueId UID = UniqueId.of("A", "B");
  private static final ObjectId OID = ObjectId.of("A", "B");
  
  private ConfigSource _underlyingSource;
  private EHCachingConfigSource _cachingSource;
  

  @BeforeMethod
  public void setUp() throws Exception {
    EHCacheUtils.clearAll();
    _underlyingSource = mock(ConfigSource.class);
    CacheManager cm = EHCacheUtils.createCacheManager();
    _cachingSource = new EHCachingConfigSource(_underlyingSource, cm);
  }

  public void getConfig_uniqueId() {
    when(_underlyingSource.getConfig(ExternalId.class, UID)).thenReturn(CONFIG);
    assertSame(_cachingSource.getConfig(ExternalId.class, UID), CONFIG);
    assertSame(_cachingSource.getConfig(ExternalId.class, UID), CONFIG);
    verify(_underlyingSource, times(1)).getConfig(ExternalId.class, UID);
  }
  
  public void getConfig_objectId() {
    when(_underlyingSource.getConfig(ExternalId.class, OID, VC)).thenReturn(CONFIG);
    assertSame(_cachingSource.getConfig(ExternalId.class, OID, VC), CONFIG);
    assertSame(_cachingSource.getConfig(ExternalId.class, OID, VC), CONFIG);
    verify(_underlyingSource, times(1)).getConfig(ExternalId.class, OID, VC);
  }
  
  public void getByName() {
    final Instant versionAsOf = Instant.now();
    when(_underlyingSource.getByName(ExternalId.class, CONFIG_NAME, versionAsOf)).thenReturn(CONFIG);
    assertSame(_cachingSource.getByName(ExternalId.class, CONFIG_NAME, versionAsOf), CONFIG);
    assertSame(_cachingSource.getByName(ExternalId.class, CONFIG_NAME, versionAsOf), CONFIG);
    verify(_underlyingSource, times(1)).getByName(ExternalId.class, CONFIG_NAME, versionAsOf);
  }
  
  public void getLatestByName() {
    when(_underlyingSource.getLatestByName(ExternalId.class, CONFIG_NAME)).thenReturn(CONFIG);
    assertSame(_cachingSource.getLatestByName(ExternalId.class, CONFIG_NAME), CONFIG);
    assertSame(_cachingSource.getLatestByName(ExternalId.class, CONFIG_NAME), CONFIG);
    verify(_underlyingSource, times(2)).getLatestByName(ExternalId.class, CONFIG_NAME);
  }
  
}
