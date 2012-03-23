/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.region.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

import javax.time.Instant;
import javax.time.calendar.TimeZone;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.region.ManageableRegion;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 * Test EHCachingRegionSource
 */
public class EhCachingRegionSourceTest {
  
  private static final ObjectId OID = ObjectId.of("A", "B");
  private static final UniqueId UID = UniqueId.of("A", "B", "V");
  private static final ExternalId ID = ExternalId.of("C", "D");
  private static final ExternalIdBundle BUNDLE = ExternalIdBundle.of(ID);
  private static final Instant NOW = Instant.now();
  private static final VersionCorrection VC = VersionCorrection.of(NOW.minusSeconds(2), NOW.minusSeconds(1));
    
  private EHCachingRegionSource _cachingRegionSource;
  private TestRegionSource _underlying;
    
  private static final ManageableRegion  TEST_REGION = getTestRegion();

  @BeforeMethod
  public void setUp() {
    _underlying = new TestRegionSource(TEST_REGION);
    _cachingRegionSource = new EHCachingRegionSource(_underlying, EHCacheUtils.createCacheManager());
  }
  
  @AfterMethod
  public void cleanUp() throws Exception {
    _cachingRegionSource.shutdown();
  }
  
  @Test
  public void test_getRegion_uniqueId() {
    assertEquals(0, _underlying.getCount().get());
    Region first = _cachingRegionSource.getRegion(UID);
    assertEquals(1, _underlying.getCount().get());
    assertEquals(TEST_REGION, first);
    
    Region second = _cachingRegionSource.getRegion(UID);
    assertEquals(1, _underlying.getCount().get());
    assertEquals(first, second);
  }
  
  @Test
  public void test_getRegion_uniqueId_notAvailable() {
    UniqueId uid = UniqueId.of("X", "Y", "Z");
    assertEquals(0, _underlying.getCount().get());
    Region first = _cachingRegionSource.getRegion(uid);
    assertEquals(1, _underlying.getCount().get());
    assertNull(first);
    
    Region second = _cachingRegionSource.getRegion(uid);
    assertEquals(1, _underlying.getCount().get());
    assertNull(second);
  }

  @Test
  public void test_getRegion_objectId_versionCorrection() {
    assertEquals(0, _underlying.getCount().get());
    Region first = _cachingRegionSource.getRegion(OID, VersionCorrection.LATEST);
    assertEquals(1, _underlying.getCount().get());
    assertEquals(TEST_REGION, first);
    
    Region second = _cachingRegionSource.getRegion(OID, VersionCorrection.LATEST);
    assertEquals(1, _underlying.getCount().get());
    assertEquals(first, second);
  }
  
  @Test
  public void test_getRegion_objectId_versionCorrection_notAvailable() {
    assertEquals(0, _underlying.getCount().get());
    Region first = _cachingRegionSource.getRegion(OID, VC);
    assertEquals(1, _underlying.getCount().get());
    assertNull(first);
    
    Region second = _cachingRegionSource.getRegion(OID, VC);
    assertEquals(1, _underlying.getCount().get());
    assertNull(second);
  }
  
  @Test
  public void test_getRegions_externalIdBundle_versionCorrection() {
    assertEquals(0, _underlying.getCount().get());
    Collection<? extends Region> firstRegions = _cachingRegionSource.getRegions(BUNDLE, VersionCorrection.LATEST);
    assertEquals(1, _underlying.getCount().get());
    assertEquals(1, firstRegions.size());
    assertTrue(firstRegions.contains(TEST_REGION));
    
    Collection<? extends Region> secondRegions = _cachingRegionSource.getRegions(BUNDLE, VersionCorrection.LATEST);
    assertEquals(1, _underlying.getCount().get());
    assertEquals(firstRegions, secondRegions);
  }
  
  @Test
  public void test_getRegions_externalIdBundle_versionCorrection_notAvailable() {
    assertEquals(0, _underlying.getCount().get());
    Collection<? extends Region> firstRegions = _cachingRegionSource.getRegions(BUNDLE, VC);
    assertEquals(1, _underlying.getCount().get());
    assertTrue(firstRegions.isEmpty());
    
    Collection<? extends Region> secondRegions = _cachingRegionSource.getRegions(BUNDLE, VC);
    assertEquals(1, _underlying.getCount().get());
    assertTrue(secondRegions.isEmpty());
  }
  
  @Test
  public void test_getHighestLevelRegion_externalId() {
    assertEquals(0, _underlying.getCount().get());
    Region first = _cachingRegionSource.getHighestLevelRegion(ID);
    assertEquals(1, _underlying.getCount().get());
    assertEquals(TEST_REGION, first);
    
    Region second = _cachingRegionSource.getHighestLevelRegion(ID);
    assertEquals(1, _underlying.getCount().get());
    assertEquals(first, second);
  }
  
  @Test
  public void test_getHighestLevelRegion_externalId_notAvailable() {
    final ExternalId id = ExternalId.of("G", "H");
    assertEquals(0, _underlying.getCount().get());
    Region first = _cachingRegionSource.getHighestLevelRegion(id);
    assertEquals(1, _underlying.getCount().get());
    assertNull(first);
    
    Region second = _cachingRegionSource.getHighestLevelRegion(id);
    assertEquals(1, _underlying.getCount().get());
    assertNull(second);
  }
  
  @Test
  public void test_getHighestLevelRegion_externalIdBundle() {
    assertEquals(0, _underlying.getCount().get());
    Region first = _cachingRegionSource.getHighestLevelRegion(BUNDLE);
    assertNotNull(first);
    assertEquals(1, _underlying.getCount().get());
    assertEquals(TEST_REGION, first);
    
    Region second = _cachingRegionSource.getHighestLevelRegion(BUNDLE);
    assertNotNull(second);
    assertEquals(1, _underlying.getCount().get());
    assertEquals(first, second);
  }
  
  @Test
  public void test_getHighestLevelRegion_externalIdBundle_notAvailable() {
    final ExternalIdBundle bundle = ExternalIdBundle.of(ExternalId.of("J", "K"));
    assertEquals(0, _underlying.getCount().get());
    Region first = _cachingRegionSource.getHighestLevelRegion(bundle);
    assertEquals(1, _underlying.getCount().get());
    assertNull(first);
    
    Region second = _cachingRegionSource.getHighestLevelRegion(bundle);
    assertEquals(1, _underlying.getCount().get());
    assertNull(second);
  }
  
  private static ManageableRegion getTestRegion() {
    ManageableRegion region = new ManageableRegion();
    region.setUniqueId(UID);
    region.setName("United Kingdom");
    region.setCurrency(Currency.GBP);
    region.setCountry(Country.GB);
    region.setTimeZone(TimeZone.of("Europe/London"));
    region.setExternalIdBundle(BUNDLE);
    return region;
  }
  
  private class TestRegionSource implements RegionSource {
    
    private final AtomicLong _count = new AtomicLong(0);
    private final Region _testRegion;

    private TestRegionSource(Region testRegion) {
      _testRegion = testRegion;
    }

    @Override
    public Collection<? extends Region> getRegions(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
      _count.getAndIncrement();
      Collection<? extends Region> result = Collections.emptyList();
      if (_testRegion.getExternalIdBundle().equals(bundle) && versionCorrection.equals(VersionCorrection.LATEST)) {
        result = Collections.singleton(TEST_REGION);
      }
      return result;
    }
    
    @Override
    public Region getRegion(ObjectId objectId, VersionCorrection versionCorrection) {
      _count.getAndIncrement();
      Region result = null;
      if (_testRegion.getUniqueId().getObjectId().equals(objectId) && versionCorrection.equals(VersionCorrection.LATEST)) {
        result = _testRegion;
      }
      return result;
    }
    
    @Override
    public Region getRegion(UniqueId uniqueId) {
      _count.getAndIncrement();
      Region result = null;
      if (_testRegion.getUniqueId().equals(uniqueId)) {
        result = _testRegion;
      }
      return result;
    }
    
    @Override
    public Region getHighestLevelRegion(ExternalIdBundle bundle) {
      _count.getAndIncrement();
      Region result = null;
      if (_testRegion.getExternalIdBundle().equals(bundle)) {
        result = _testRegion;
      }
      return result;
    }
    
    @Override
    public Region getHighestLevelRegion(ExternalId externalId) {
      _count.getAndIncrement();
      Region result = null;
      if (_testRegion.getExternalIdBundle().contains(externalId)) {
        result = _testRegion;
      }
      return result;
    }

    /**
     * Gets the count.
     * @return the count
     */
    public AtomicLong getCount() {
      return _count;
    }
    
  }
  
}
