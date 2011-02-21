/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.time.Instant;
import javax.time.calendar.TimeZone;

import org.junit.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.common.CurrencyUnit;
import com.opengamma.core.region.Region;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.region.ManageableRegion;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.RegionSearchRequest;
import com.opengamma.master.region.RegionSearchResult;
import com.opengamma.util.db.PagingRequest;

/**
 * Test MasterRegionSource.
 */
public class MasterRegionSourceTest {

  private static final CurrencyUnit GBP = CurrencyUnit.GBP;
  private static final UniqueIdentifier UID = UniqueIdentifier.of("A", "B");
  private static final Identifier ID = Identifier.of("C", "D");
  private static final IdentifierBundle BUNDLE = IdentifierBundle.of(ID);
  private static final Instant NOW = Instant.now();
  private static final VersionCorrection VC = VersionCorrection.of(NOW.minusSeconds(2), NOW.minusSeconds(1));

  @Test(expected = IllegalArgumentException.class)
  public void test_constructor_1arg_nullMaster() throws Exception {
    new MasterRegionSource(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_constructor_2arg_nullMaster() throws Exception {
    new MasterRegionSource(null, null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getRegion_noOverride_found() throws Exception {
    RegionMaster mock = mock(RegionMaster.class);
    
    RegionDocument doc = new RegionDocument(example());
    when(mock.get(UID)).thenReturn(doc);
    MasterRegionSource test = new MasterRegionSource(mock);
    Region testResult = test.getRegion(UID);
    verify(mock, times(1)).get(UID);
    
    assertEquals(example(), testResult);
  }

  @Test
  public void test_getRegion_found() throws Exception {
    RegionMaster mock = mock(RegionMaster.class);
    
    RegionDocument doc = new RegionDocument(example());
    when(mock.get(UID, VC)).thenReturn(doc);
    MasterRegionSource test = new MasterRegionSource(mock, VC);
    Region testResult = test.getRegion(UID);
    verify(mock, times(1)).get(UID, VC);
    
    assertEquals(example(), testResult);
  }

  @Test
  public void test_getRegion_notFound() throws Exception {
    RegionMaster mock = mock(RegionMaster.class);
    
    when(mock.get(UID, VC)).thenThrow(new DataNotFoundException(""));
    MasterRegionSource test = new MasterRegionSource(mock, VC);
    Region testResult = test.getRegion(UID);
    verify(mock, times(1)).get(UID, VC);
    
    assertEquals(null, testResult);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getRegion_Identifier_found() throws Exception {
    RegionMaster mock = mock(RegionMaster.class);
    RegionSearchRequest request = new RegionSearchRequest(ID);
    request.setPagingRequest(PagingRequest.ONE);
    request.setVersionCorrection(VC);
    
    RegionSearchResult result = new RegionSearchResult();
    result.getDocuments().add(new RegionDocument(example()));
    
    when(mock.search(request)).thenReturn(result);
    MasterRegionSource test = new MasterRegionSource(mock, VC);
    Region testResult = test.getHighestLevelRegion(ID);
    verify(mock, times(1)).search(request);
    
    assertEquals(example(), testResult);
  }

  @Test
  public void test_getRegion_Identifier_noFound() throws Exception {
    RegionMaster mock = mock(RegionMaster.class);
    RegionSearchRequest request = new RegionSearchRequest(ID);
    request.setPagingRequest(PagingRequest.ONE);
    request.setVersionCorrection(VC);
    
    RegionSearchResult result = new RegionSearchResult();
    
    when(mock.search(request)).thenReturn(result);
    MasterRegionSource test = new MasterRegionSource(mock, VC);
    Region testResult = test.getHighestLevelRegion(ID);
    verify(mock, times(1)).search(request);
    
    assertEquals(null, testResult);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getRegion_IdentifierBundle_found() throws Exception {
    RegionMaster mock = mock(RegionMaster.class);
    RegionSearchRequest request = new RegionSearchRequest(BUNDLE);
    request.setPagingRequest(PagingRequest.ONE);
    request.setVersionCorrection(VC);
    
    RegionSearchResult result = new RegionSearchResult();
    result.getDocuments().add(new RegionDocument(example()));
    
    when(mock.search(request)).thenReturn(result);
    MasterRegionSource test = new MasterRegionSource(mock, VC);
    Region testResult = test.getHighestLevelRegion(BUNDLE);
    verify(mock, times(1)).search(request);
    
    assertEquals(example(), testResult);
  }

  //-------------------------------------------------------------------------
  protected ManageableRegion example() {
    ManageableRegion region = new ManageableRegion();
    region.setUniqueId(UID);
    region.setName("United Kingdom");
    region.setCurrency(GBP);
    region.setCountryISO("GB");
    region.setTimeZone(TimeZone.of("Europe/London"));
    return region;
  }

}
