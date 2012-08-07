/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import javax.time.Instant;

import net.sf.ehcache.CacheManager;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * Test {@link EHCachingMasterConfigSource}.
 */
@Test
public class EHCachingMasterConfigSourceTest {
  
  private static final VersionCorrection VC = VersionCorrection.LATEST;
  private static final ExternalId CONFIG = ExternalId.of ("Test", "sec1");
  private static final String CONFIG_NAME = "Test";
  
  private UnitTestConfigMaster _underlyingConfigMaster;
  private EHCachingMasterConfigSource _cachingSource;
  
  private static final ConfigDocument<ExternalId> DOC;
  static {
    ConfigDocument<ExternalId> doc = new ConfigDocument<ExternalId>(ExternalId.class);
    doc.setName(CONFIG_NAME);
    doc.setValue(CONFIG);
    DOC = doc;
  }
 
  @BeforeMethod
  public void setUp() throws Exception {
    EHCacheUtils.clearAll();
    _underlyingConfigMaster = new UnitTestConfigMaster();
    CacheManager cm = EHCacheUtils.createCacheManager();
    _cachingSource = new EHCachingMasterConfigSource(_underlyingConfigMaster, cm);
  }

  public void getConfig_uniqueId() {
    UniqueId uniqueId = _underlyingConfigMaster.add(DOC).getUniqueId();
    assertSame(_cachingSource.getConfig(ExternalId.class, uniqueId), CONFIG);
    assertSame(_cachingSource.getConfig(ExternalId.class, uniqueId), CONFIG);
    assertEquals(1, _underlyingConfigMaster.getCounter().get());
  }
  
  public void getConfig_objectId() {
    UniqueId uniqueId = _underlyingConfigMaster.add(DOC).getUniqueId();
    assertSame(_cachingSource.getConfig(ExternalId.class, uniqueId.getObjectId(), VC), CONFIG);
    assertSame(_cachingSource.getConfig(ExternalId.class, uniqueId.getObjectId(), VC), CONFIG);
    assertEquals(2, _underlyingConfigMaster.getCounter().get());
  }
  
  public void getByName() {
    final Instant versionAsOf = Instant.now();
    _underlyingConfigMaster.add(DOC);
    assertSame(_cachingSource.getByName(ExternalId.class, CONFIG_NAME, versionAsOf), CONFIG);
    assertSame(_cachingSource.getByName(ExternalId.class, CONFIG_NAME, versionAsOf), CONFIG);
    assertEquals(1, _underlyingConfigMaster.getCounter().get());
  }
  
  public void getLatestByName() {
    _underlyingConfigMaster.add(DOC);
    assertSame(_cachingSource.getLatestByName(ExternalId.class, CONFIG_NAME), CONFIG);
    assertSame(_cachingSource.getLatestByName(ExternalId.class, CONFIG_NAME), CONFIG);
    assertEquals(1, _underlyingConfigMaster.getCounter().get());
  }

  public void getConfigs() {
    final Collection<ExternalId> configs = Lists.newArrayList(CONFIG, CONFIG);
    _underlyingConfigMaster.add(DOC);
    _underlyingConfigMaster.add(DOC);
    
    assertEquals(configs, _cachingSource.getConfigs(ExternalId.class, CONFIG_NAME, VC));
    assertEquals(configs, _cachingSource.getConfigs(ExternalId.class, CONFIG_NAME, VC));
    assertEquals(1, _underlyingConfigMaster.getCounter().get());
  }
  
  public void getLatestByNameAfterUpdate() {
    ConfigDocument<ExternalId> addedDoc = _underlyingConfigMaster.add(DOC);
    assertSame(_cachingSource.getLatestByName(ExternalId.class, CONFIG_NAME), CONFIG);
    assertSame(_cachingSource.getLatestByName(ExternalId.class, CONFIG_NAME), CONFIG);
    assertEquals(1, _underlyingConfigMaster.getCounter().get());
    
    final ExternalId lastestConfig = ExternalId.of ("Test", "sec1");
    addedDoc.setValue(lastestConfig);
    _underlyingConfigMaster.update(addedDoc);
    assertSame(_cachingSource.getLatestByName(ExternalId.class, CONFIG_NAME), lastestConfig);
    assertSame(_cachingSource.getLatestByName(ExternalId.class, CONFIG_NAME), lastestConfig);
    assertEquals(2, _underlyingConfigMaster.getCounter().get());
  }
  
  private static class UnitTestConfigMaster extends InMemoryConfigMaster {
    
    private AtomicLong _counter = new AtomicLong(0);

    public AtomicLong getCounter() {
      return _counter;
    }

    @Override
    public ConfigDocument<?> get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
      _counter.getAndIncrement();
      return super.get(objectId, versionCorrection);
    }

    @Override
    public <T> ConfigDocument<T> get(ObjectIdentifiable objectId, VersionCorrection versionCorrection, Class<T> clazz) {
      _counter.getAndIncrement();
      return super.get(objectId, versionCorrection, clazz);
    }

    @Override
    public <T> ConfigSearchResult<T> search(ConfigSearchRequest<T> request) {
      _counter.getAndIncrement();
      return super.search(request);
    }   
  }
  
}
