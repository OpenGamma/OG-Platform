/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Collection;
import java.util.Collections;

import javax.time.Instant;

import net.sf.ehcache.CacheManager;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.paging.PagingRequest;

/**
 * Test {@link EHCachingMasterConfigSource}.
 */
@Test
public class EHCachingMasterConfigSourceTest {
  
  private static final VersionCorrection VC = VersionCorrection.LATEST;
  private static final ExternalId CONFIG = ExternalId.of ("Test", "sec1");
  private static final String CONFIG_NAME = "Test";
  private static final UniqueId UID = UniqueId.of("A", "B");
  private static final ObjectId OID = ObjectId.of("A", "B");
  
  private ConfigMaster _underlyingConfigMaster;
  private EHCachingMasterConfigSource _cachingSource;
  
  private static final ConfigDocument<ExternalId> DOC;
  static {
    ConfigDocument<ExternalId> doc = new ConfigDocument<ExternalId>(ExternalId.class);
    doc.setName("Test");
    doc.setValue(CONFIG);
    DOC = doc;
  }
 
  @BeforeMethod
  public void setUp() throws Exception {
    EHCacheUtils.clearAll();
    _underlyingConfigMaster = mock(ConfigMaster.class);
    CacheManager cm = EHCacheUtils.createCacheManager();
    _cachingSource = new EHCachingMasterConfigSource(_underlyingConfigMaster, cm);
  }

  public void getConfig_uniqueId() {
    when(_underlyingConfigMaster.get(UID, ExternalId.class)).thenReturn(DOC);
    assertSame(_cachingSource.getConfig(ExternalId.class, UID), CONFIG);
    assertSame(_cachingSource.getConfig(ExternalId.class, UID), CONFIG);
    verify(_underlyingConfigMaster, times(1)).get(UID, ExternalId.class);
  }
  
  public void getConfig_objectId() {
    when(_underlyingConfigMaster.get(OID, VC, ExternalId.class)).thenReturn(DOC);
    assertSame(_cachingSource.getConfig(ExternalId.class, OID, VC), CONFIG);
    assertSame(_cachingSource.getConfig(ExternalId.class, OID, VC), CONFIG);
    verify(_underlyingConfigMaster, times(1)).get(OID, VC, ExternalId.class);
  }
  
  public void getByName() {
    final Instant versionAsOf = Instant.now();
    
    ConfigSearchRequest<ExternalId> request = new ConfigSearchRequest<ExternalId>();
    request.setPagingRequest(PagingRequest.ONE);
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(versionAsOf));
    request.setName(CONFIG_NAME);
    request.setType(ExternalId.class);
     
    ConfigSearchResult<ExternalId> searchResult = new ConfigSearchResult<ExternalId>(Collections.singletonList(DOC));
    
    when(_underlyingConfigMaster.search(request)).thenReturn(searchResult);
    assertSame(_cachingSource.getByName(ExternalId.class, CONFIG_NAME, versionAsOf), CONFIG);
    assertSame(_cachingSource.getByName(ExternalId.class, CONFIG_NAME, versionAsOf), CONFIG);
    verify(_underlyingConfigMaster, times(1)).search(request);
  }
  
  public void getLatestByName() {
    
    ConfigSearchRequest<ExternalId> request = new ConfigSearchRequest<ExternalId>();
    request.setPagingRequest(PagingRequest.ONE);
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(null));
    request.setName(CONFIG_NAME);
    request.setType(ExternalId.class);
        
    ConfigSearchResult<ExternalId> searchResult = new ConfigSearchResult<ExternalId>(Collections.singletonList(DOC));
    
    when(_underlyingConfigMaster.search(request)).thenReturn(searchResult);
    assertSame(_cachingSource.getLatestByName(ExternalId.class, CONFIG_NAME), CONFIG);
    assertSame(_cachingSource.getLatestByName(ExternalId.class, CONFIG_NAME), CONFIG);
    verify(_underlyingConfigMaster, times(1)).search(request);
  }
  
  @SuppressWarnings("unchecked")
  public void getConfigs() {
    
    final Collection<ExternalId> configs = Lists.newArrayList(CONFIG, CONFIG);
    
    ConfigSearchRequest<ExternalId> request = new ConfigSearchRequest<ExternalId>();
    request.setVersionCorrection(VC);
    request.setName(CONFIG_NAME);
    request.setType(ExternalId.class);
    
    ConfigSearchResult<ExternalId> searchResult = new ConfigSearchResult<ExternalId>();
    searchResult.setDocuments(Lists.newArrayList(DOC, DOC));
    
    when(_underlyingConfigMaster.search(request)).thenReturn(searchResult);
    assertEquals(configs, _cachingSource.getConfigs(ExternalId.class, CONFIG_NAME, VC));
    assertEquals(configs, _cachingSource.getConfigs(ExternalId.class, CONFIG_NAME, VC));
    verify(_underlyingConfigMaster, times(1)).search(request);
  }
  
}
