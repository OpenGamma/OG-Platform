package com.opengamma.master.region.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import javax.time.Instant;
import javax.time.calendar.TimeZone;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
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

@Test
public class EhCachingRegionSourceTest {
  
  private static final ObjectId OID = ObjectId.of("A", "B");
  private static final UniqueId UID = UniqueId.of("A", "B", "V");
  private static final ExternalId ID = ExternalId.of("C", "D");
  private static final ExternalIdBundle BUNDLE = ExternalIdBundle.of(ID, ExternalId.of("E", "F"));
  private static final Instant NOW = Instant.now();
  private static final VersionCorrection VC = VersionCorrection.of(NOW.minusSeconds(2), NOW.minusSeconds(1));
    
  private EHCachingRegionSource _cachingRegionSource;
  
  private AtomicLong _count;
  
  private static final ManageableRegion  TEST_REGION = getTestRegion();

  @BeforeMethod
  public void setUp() {
    _count = new AtomicLong(0);
    _cachingRegionSource = new EHCachingRegionSource(new RegionSource() {
      
      @Override
      public Collection<? extends Region> getRegions(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
        _count.getAndIncrement();
        return Lists.newArrayList(TEST_REGION, TEST_REGION);
      }
      
      @Override
      public Region getRegion(ObjectId objectId, VersionCorrection versionCorrection) {
        _count.getAndIncrement();
        return TEST_REGION;
      }
      
      @Override
      public Region getRegion(UniqueId uniqueId) {
        _count.getAndIncrement();
        return TEST_REGION;
      }
      
      @Override
      public Region getHighestLevelRegion(ExternalIdBundle bundle) {
        _count.getAndIncrement();
        return TEST_REGION;
      }
      
      @Override
      public Region getHighestLevelRegion(ExternalId externalId) {
        _count.getAndIncrement();
        return TEST_REGION;
      }
    }, EHCacheUtils.createCacheManager());
  }
  
  
  public void test_getRegion_uniqueId() {
    assertEquals(0, _count.get());
    Region first = _cachingRegionSource.getRegion(UID);
    assertEquals(1, _count.get());
    assertEquals(TEST_REGION, first);
    
    Region second = _cachingRegionSource.getRegion(UID);
    assertEquals(1, _count.get());
    assertEquals(first, second);
  }

  public void test_getRegion_objectId_versionCorrection() {
    assertEquals(0, _count.get());
    Region first = _cachingRegionSource.getRegion(OID, VC);
    assertEquals(1, _count.get());
    assertEquals(TEST_REGION, first);
    
    Region second = _cachingRegionSource.getRegion(OID, VC);
    assertEquals(1, _count.get());
    assertEquals(first, second);
    
  }
  
  public void test_getRegions_externalIdBundle_versionCorrection() {
    assertEquals(0, _count.get());
    Collection<? extends Region> firstRegions = _cachingRegionSource.getRegions(BUNDLE, VC);
    assertEquals(1, _count.get());
    assertEquals(2, firstRegions.size());
    assertTrue(firstRegions.contains(TEST_REGION));
    
    Collection<? extends Region> secondRegions = _cachingRegionSource.getRegions(BUNDLE, VC);
    assertEquals(1, _count.get());
    assertEquals(firstRegions, secondRegions);
  }
  
  public void test_getHighestLevelRegion_externalId() {
    assertEquals(0, _count.get());
    Region first = _cachingRegionSource.getHighestLevelRegion(ID);
    assertEquals(1, _count.get());
    assertEquals(TEST_REGION, first);
    
    Region second = _cachingRegionSource.getHighestLevelRegion(ID);
    assertEquals(1, _count.get());
    assertEquals(first, second);
  }
  
  public void test_getHighestLevelRegion_externalIdBundle() {
    assertEquals(0, _count.get());
    Region first = _cachingRegionSource.getHighestLevelRegion(BUNDLE);
    assertNotNull(first);
    assertEquals(1, _count.get());
    assertEquals(TEST_REGION, first);
    
    Region second = _cachingRegionSource.getHighestLevelRegion(BUNDLE);
    assertNotNull(second);
    assertEquals(1, _count.get());
    assertEquals(first, second);
  }
  
  private static ManageableRegion getTestRegion() {
    ManageableRegion region = new ManageableRegion();
    region.setUniqueId(UID);
    region.setName("United Kingdom");
    region.setCurrency(Currency.GBP);
    region.setCountry(Country.GB);
    region.setTimeZone(TimeZone.of("Europe/London"));
    return region;
  }
  
}
