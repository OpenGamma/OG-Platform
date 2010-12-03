/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
import com.opengamma.core.common.Currency;
import com.opengamma.core.region.Region;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.region.ManageableRegion;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.RegionSearchRequest;
import com.opengamma.master.region.RegionSearchResult;
import com.opengamma.master.region.impl.MasterRegionSource;
import com.opengamma.util.db.PagingRequest;

/**
 * Test MasterRegionSource.
 */
public class MasterRegionSourceTest {

  private static final Currency GBP = Currency.getInstance("GBP");
  private static final UniqueIdentifier UID = UniqueIdentifier.of("A", "B");
  private static final Identifier ID = Identifier.of("C", "D");
  private static final IdentifierBundle BUNDLE = IdentifierBundle.of(ID);

  @Test(expected = IllegalArgumentException.class)
  public void test_constructor_1arg_nullMaster() throws Exception {
    new MasterRegionSource(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_constructor_2arg_nullMaster() throws Exception {
    new MasterRegionSource(null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_constructor3arg_nullMaster() throws Exception {
    new MasterRegionSource(null, null, null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getRegion_found() throws Exception {
    Instant now = Instant.now();
    RegionMaster mock = mock(RegionMaster.class);
    
    RegionDocument doc = new RegionDocument(uk());
    when(mock.get(UID)).thenReturn(doc);
    MasterRegionSource test = new MasterRegionSource(mock, now.minusSeconds(2), now.minusSeconds(1));
    Region testResult = test.getRegion(UID);
    verify(mock, times(1)).get(UID);
    
    assertEquals(uk(), testResult);
  }

  @Test
  public void test_getRegion_notFound() throws Exception {
    Instant now = Instant.now();
    RegionMaster mock = mock(RegionMaster.class);
    
    when(mock.get(UID)).thenThrow(new DataNotFoundException(""));
    MasterRegionSource test = new MasterRegionSource(mock, now.minusSeconds(2), now.minusSeconds(1));
    Region testResult = test.getRegion(UID);
    verify(mock, times(1)).get(UID);
    
    assertEquals(null, testResult);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getRegion_Identifier_found() throws Exception {
    Instant now = Instant.now();
    RegionMaster mock = mock(RegionMaster.class);
    RegionSearchRequest request = new RegionSearchRequest(ID);
    request.setPagingRequest(PagingRequest.ONE);
    request.setVersionAsOfInstant(now.minusSeconds(2));
    request.setCorrectedToInstant(now.minusSeconds(1));
    
    RegionSearchResult result = new RegionSearchResult();
    result.getDocuments().add(new RegionDocument(uk()));
    
    when(mock.search(request)).thenReturn(result);
    MasterRegionSource test = new MasterRegionSource(mock, now.minusSeconds(2), now.minusSeconds(1));
    Region testResult = test.getHighestLevelRegion(ID);
    verify(mock, times(1)).search(request);
    
    assertEquals(uk(), testResult);
  }

  @Test
  public void test_getRegion_Identifier_noFound() throws Exception {
    Instant now = Instant.now();
    RegionMaster mock = mock(RegionMaster.class);
    RegionSearchRequest request = new RegionSearchRequest(ID);
    request.setPagingRequest(PagingRequest.ONE);
    request.setVersionAsOfInstant(now.minusSeconds(2));
    request.setCorrectedToInstant(now.minusSeconds(1));
    
    RegionSearchResult result = new RegionSearchResult();
    
    when(mock.search(request)).thenReturn(result);
    MasterRegionSource test = new MasterRegionSource(mock, now.minusSeconds(2), now.minusSeconds(1));
    Region testResult = test.getHighestLevelRegion(ID);
    verify(mock, times(1)).search(request);
    
    assertEquals(null, testResult);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getRegion_IdentifierBundle_found() throws Exception {
    Instant now = Instant.now();
    RegionMaster mock = mock(RegionMaster.class);
    RegionSearchRequest request = new RegionSearchRequest(BUNDLE);
    request.setPagingRequest(PagingRequest.ONE);
    request.setVersionAsOfInstant(now.minusSeconds(2));
    request.setCorrectedToInstant(now.minusSeconds(1));
    
    RegionSearchResult result = new RegionSearchResult();
    result.getDocuments().add(new RegionDocument(uk()));
    
    when(mock.search(request)).thenReturn(result);
    MasterRegionSource test = new MasterRegionSource(mock, now.minusSeconds(2), now.minusSeconds(1));
    Region testResult = test.getHighestLevelRegion(BUNDLE);
    verify(mock, times(1)).search(request);
    
    assertEquals(uk(), testResult);
  }

  //-------------------------------------------------------------------------
  protected ManageableRegion uk() {
    ManageableRegion region = new ManageableRegion();
    region.setUniqueIdentifier(UID);
    region.setName("United Kingdom");
    region.setCurrency(GBP);
    region.setCountryISO("GB");
    region.setTimeZone(TimeZone.of("Europe/London"));
    return region;
  }

}
