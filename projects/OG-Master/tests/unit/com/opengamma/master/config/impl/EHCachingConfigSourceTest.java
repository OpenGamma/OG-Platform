/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * Test {@link EHCachingConfigSource}.
 */
@Test
public class EHCachingConfigSourceTest {

  private static final VersionCorrection VC = VersionCorrection.LATEST;
  private static final ExternalId CONFIG = ExternalId.of ("Test", "sec1");
  private static final String CONFIG_NAME = "Test";
  
  private TestConfigSource _underlyingSource;
  private EHCachingConfigSource _cachingSource;
  

  @BeforeMethod
  public void setUp() throws Exception {
    EHCacheUtils.clearAll();
    _underlyingSource = new TestConfigSource();
    CacheManager cm = EHCacheUtils.createCacheManager();
    _cachingSource = new EHCachingConfigSource(_underlyingSource, cm);
  }

  public void getConfig_uniqueId() {
    UniqueId uniqueId = _underlyingSource.addTestConfig(CONFIG);
    assertSame(_cachingSource.getConfig(ExternalId.class, uniqueId), CONFIG);
    assertSame(_cachingSource.getConfig(ExternalId.class, uniqueId), CONFIG);
    assertEquals(1, _underlyingSource.getCounter().get());
  }
  
  public void getConfig_objectId() {
    UniqueId uniqueId = _underlyingSource.addTestConfig(CONFIG);
    assertSame(_cachingSource.getConfig(ExternalId.class, uniqueId.getObjectId(), VC), CONFIG);
    assertSame(_cachingSource.getConfig(ExternalId.class, uniqueId.getObjectId(), VC), CONFIG);
    assertEquals(1, _underlyingSource.getCounter().get());
  }
  
  public void getByName() {
    final Instant versionAsOf = Instant.now();
    _underlyingSource.addTestConfig(CONFIG);
    assertSame(_cachingSource.getByName(ExternalId.class, CONFIG_NAME, versionAsOf), CONFIG);
    assertSame(_cachingSource.getByName(ExternalId.class, CONFIG_NAME, versionAsOf), CONFIG);
    assertEquals(1, _underlyingSource.getCounter().get());
  }
  
  public void getLatestByName() {
    _underlyingSource.addTestConfig(CONFIG);
    assertSame(_cachingSource.getLatestByName(ExternalId.class, CONFIG_NAME), CONFIG);
    assertSame(_cachingSource.getLatestByName(ExternalId.class, CONFIG_NAME), CONFIG);
    assertEquals(2, _underlyingSource.getCounter().get());
  }
  
  public void getConfigs() {
    final Collection<? extends ExternalId> configs = Lists.newArrayList(CONFIG, CONFIG);
    _underlyingSource.addTestConfig(CONFIG);
    _underlyingSource.addTestConfig(CONFIG);
    
    assertEquals(configs, _cachingSource.getConfigs(ExternalId.class, CONFIG_NAME, VC));
    assertEquals(configs, _cachingSource.getConfigs(ExternalId.class, CONFIG_NAME, VC));
    assertEquals(1, _underlyingSource.getCounter().get());
  }
  
  private static final class TestConfigSource implements ConfigSource {
    
    private final MasterConfigSource _delegateSource;
    private final InMemoryConfigMaster _configMaster;

    private final AtomicLong _count = new AtomicLong(0);
    
    public TestConfigSource() {
      _configMaster = new InMemoryConfigMaster();
      _delegateSource = new MasterConfigSource(_configMaster);
    }

    public AtomicLong getCounter() {
      return _count;
    }

    @Override
    public <T> T getConfig(Class<T> clazz, UniqueId uniqueId) {
      _count.getAndIncrement();
      return _delegateSource.getConfig(clazz, uniqueId);
    }

    @Override
    public <T> T getConfig(Class<T> clazz, ObjectId objectId, VersionCorrection versionCorrection) {
      _count.getAndIncrement();
      return _delegateSource.getConfig(clazz, objectId, versionCorrection);
    }

    @Override
    public <T> Collection<? extends T> getConfigs(Class<T> clazz, String configName, VersionCorrection versionCorrection) {
      _count.getAndIncrement();
      return _delegateSource.getConfigs(clazz, configName, versionCorrection);
    }

    @Override
    public <T> T getLatestByName(Class<T> clazz, String name) {
      _count.getAndIncrement();
      return _delegateSource.getLatestByName(clazz, name);
    }

    @Override
    public <T> T getByName(Class<T> clazz, String name, Instant versionAsOf) {
      _count.getAndIncrement();
      return _delegateSource.getByName(clazz, name, versionAsOf);
    }
    
    public UniqueId addTestConfig(final ExternalId id) {
      ConfigDocument<ExternalId> doc = new ConfigDocument<ExternalId>(ExternalId.class);
      doc.setName(CONFIG_NAME);
      doc.setValue(id);
      ConfigDocument<ExternalId> added = _configMaster.add(doc);
      return added.getUniqueId();
    }
    
  }

}
