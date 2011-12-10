/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;

import org.joda.beans.JodaBeanUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Supplier;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.UniqueId;
import com.opengamma.master.holiday.impl.InMemoryHolidayMaster;
import com.opengamma.master.position.impl.InMemoryPositionMaster;

/**
 * Test {@link InMemoryHolidayMaster}.
 */
@Test
public class InMemoryPositionMasterTest {
  
  private static final ExternalId SEC1 = ExternalId.of ("Test", "sec1");
  private static final ExternalId SEC2 = ExternalId.of ("Test", "sec2");
  private static final ExternalId SEC3 = ExternalId.of ("Test", "sec3");
  private static final ExternalId COUNTER_PARTY = ExternalId.of ("Test", "counterParty");
  
  private static final ManageableTrade TRADE1 = new ManageableTrade(BigDecimal.ONE, SEC1, LocalDate.now(), OffsetTime.now(), COUNTER_PARTY);
  private static final ManageableTrade TRADE2 = new ManageableTrade(BigDecimal.ONE, SEC2, LocalDate.now(), OffsetTime.now(), COUNTER_PARTY);
  private static final ManageableTrade TRADE3 = new ManageableTrade(BigDecimal.ONE, SEC3, LocalDate.now(), OffsetTime.now(), COUNTER_PARTY);
  
  private InMemoryPositionMaster _populatedMaster;
  private InMemoryPositionMaster _emptyMaster;
  private PositionDocument _pos1;
  private PositionDocument _pos2;
  private PositionDocument _pos3;

  @BeforeMethod
  public void setUp() {
    _emptyMaster = new InMemoryPositionMaster();
    _populatedMaster = new InMemoryPositionMaster();
    _pos1 = new PositionDocument(new ManageablePosition(BigDecimal.ONE, SEC1));
    _pos1.getPosition().addTrade(JodaBeanUtils.clone(TRADE1));
    _pos1 = _populatedMaster.add(_pos1);
    
    _pos2 = new PositionDocument(new ManageablePosition(BigDecimal.valueOf(2), SEC2));
    _pos2.getPosition().addTrade(JodaBeanUtils.clone(TRADE1));
    _pos2.getPosition().addTrade(JodaBeanUtils.clone(TRADE2));
    _pos2 = _populatedMaster.add(_pos2);
    
    _pos3 = new PositionDocument(new ManageablePosition(BigDecimal.valueOf(3), SEC3));
    _pos3.getPosition().addTrade(JodaBeanUtils.clone(TRADE1));
    _pos3.getPosition().addTrade(JodaBeanUtils.clone(TRADE2));
    _pos3.getPosition().addTrade(JodaBeanUtils.clone(TRADE3));
    _pos3 = _populatedMaster.add(_pos3);
  }
  
  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_nullSupplier() {
    new InMemoryPositionMaster((Supplier<ObjectId>) null);
  }

  public void test_defaultSupplier() {
    InMemoryPositionMaster master = new InMemoryPositionMaster();
    PositionDocument added = master.add(new PositionDocument(_pos1.getPosition()));
    assertEquals("MemPos", added.getUniqueId().getScheme());
  }
  
  //-------------------------------------------------------------------------
  public void test_alternateSupplier() {
    InMemoryPositionMaster master = new InMemoryPositionMaster(new ObjectIdSupplier("Hello"));
    PositionDocument added = master.add(new PositionDocument(_pos1.getPosition()));
    assertEquals("Hello", added.getUniqueId().getScheme());
  }
  
  //-------------------------------------------------------------------------
  public void test_add_emptyMaster() {
    ManageablePosition pos = new ManageablePosition(_pos1.getPosition());
    pos.setUniqueId(null);
    pos.getTrades().clear();
    pos.addTrade(JodaBeanUtils.clone(TRADE1));
    
    PositionDocument addedDoc = _emptyMaster.add(new PositionDocument(pos));
    assertNotNull(addedDoc.getVersionFromInstant());
    assertEquals("MemPos", addedDoc.getUniqueId().getScheme());
    assertEquals("1", addedDoc.getUniqueId().getValue());
    ManageablePosition addedPosition = addedDoc.getPosition();
    assertNotNull(addedPosition);
    assertEquals(addedDoc.getUniqueId(), addedPosition.getUniqueId());
    List<ManageableTrade> addedTrades = addedPosition.getTrades();
    assertNotNull(addedTrades);
    assertEquals(1, addedTrades.size());
    ManageableTrade addedTrade = addedTrades.get(0);
    assertNotNull(addedTrade);
    assertEquals("MemPos", addedTrade.getUniqueId().getScheme());
    assertEquals("2", addedTrade.getUniqueId().getValue());
    
    addedTrade.setUniqueId(null);
    addedTrade.setParentPositionId(null);
    
    assertEquals(TRADE1, addedTrade);
    addedPosition.setUniqueId(null);
    assertEquals(pos, addedPosition);
  }
  
  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_emptyMaster() {
    assertNull(_emptyMaster.get(UniqueId.of("MemPos", "A")));
  }
  
  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_remove_emptyMaster() {
    _emptyMaster.remove(UniqueId.of("MemPos", "A"));
  }
  
  //-------------------------------------------------------------------------
  public void test_search_emptyMaster() {
    PositionSearchRequest request = new PositionSearchRequest();
    PositionSearchResult result = _emptyMaster.search(request);
    assertEquals(0, result.getPaging().getTotalItems());
    assertEquals(0, result.getDocuments().size());
  }
  
  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_update_emptyMaster() {
    PositionDocument doc = new PositionDocument(_pos1.getPosition());
    doc.setUniqueId(UniqueId.of("MemPos", "A"));
    _emptyMaster.update(doc);
  }
  
  public void test_update_populatedMaster() {
    PositionDocument doc = new PositionDocument(new ManageablePosition(BigDecimal.valueOf(100), SEC3));
    doc.getPosition().addTrade(new ManageableTrade(BigDecimal.ONE, SEC3, LocalDate.now(), OffsetTime.now(), COUNTER_PARTY));
    doc.setUniqueId(_pos1.getUniqueId());
    PositionDocument updated = _populatedMaster.update(doc);
    assertEquals(_pos1.getUniqueId(), updated.getUniqueId());
    assertNotNull(_pos1.getVersionFromInstant());
    assertNotNull(updated.getVersionFromInstant());
  }
  
  public void test_get_populatedMaster() {
    PositionDocument addedPos1 = _populatedMaster.get(_pos1.getUniqueId());
    assertNotSame(_pos1, addedPos1);
    assertEquals(_pos1, addedPos1);
    PositionDocument addedPos2 = _populatedMaster.get(_pos2.getUniqueId());
    assertNotSame(_pos2, addedPos2);
    assertEquals(_pos2, addedPos2);
    PositionDocument addedPos3 = _populatedMaster.get(_pos3.getUniqueId());
    assertNotSame(_pos3, addedPos3);
    assertEquals(_pos3, addedPos3);
    
    assertNotSame(_populatedMaster.get(_pos1.getUniqueId()), 
        _populatedMaster.get(_pos1.getUniqueId()));
  }
  
  public void test_getIsClone_populatedMaster() {
    assertNotSame(_populatedMaster.get(_pos1.getUniqueId()), 
        _populatedMaster.get(_pos1.getUniqueId()));
    assertEquals(_populatedMaster.get(_pos1.getUniqueId()), 
        _populatedMaster.get(_pos1.getUniqueId()));
  }
  
  public void test_remove_populatedMaster() {
    _populatedMaster.remove(_pos1.getUniqueId());
    PositionSearchRequest request = new PositionSearchRequest();
    PositionSearchResult result = _populatedMaster.search(request);
    assertEquals(2, result.getPaging().getTotalItems());
    List<PositionDocument> docs = result.getDocuments();
    assertEquals(2, docs.size());
    assertEquals(true, docs.contains(_pos2));
    assertEquals(true, docs.contains(_pos3));
  }
  
  public void test_search_populatedMaster_all() {
    PositionSearchRequest request = new PositionSearchRequest();
    PositionSearchResult result = _populatedMaster.search(request);
    assertEquals(3, result.getPaging().getTotalItems());
    List<PositionDocument> docs = result.getDocuments();
    assertEquals(3, docs.size());
    assertTrue(docs.contains(_pos1));
    assertTrue(docs.contains(_pos2));
    assertTrue(docs.contains(_pos3));
  }
  
  //-------------------------------------------------------------------------
  public void test_search_filterByPositionId() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.addPositionObjectId(_pos1.getObjectId());
    PositionSearchResult result = _populatedMaster.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_pos1, result.getFirstDocument());
  }
  
  //-------------------------------------------------------------------------
  public void test_search_filterByPositionId_noMatch() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.addPositionObjectId(ObjectId.of("A", "UNREAL"));
    PositionSearchResult result = _populatedMaster.search(request);
    assertEquals(0, result.getDocuments().size());
  }
  
  //-------------------------------------------------------------------------
  public void test_search_filterByOneSecurityId() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.addSecurityExternalId(SEC2);
    PositionSearchResult result = _populatedMaster.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_pos2, result.getFirstDocument());
  }
  
  //-------------------------------------------------------------------------
  public void test_search_filterByMultiSecurityId() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.addSecurityExternalIds(SEC2, SEC1);
    PositionSearchResult result = _populatedMaster.search(request);
    assertEquals(2, result.getDocuments().size());
    List<PositionDocument> docs = result.getDocuments();
    assertTrue(docs.contains(_pos1));
    assertTrue(docs.contains(_pos2));
  }
  
  //-------------------------------------------------------------------------
  public void test_search_filterBySecurityId_noMatch() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.addSecurityExternalId(ExternalId.of("A", "UNREAL"));
    PositionSearchResult result = _populatedMaster.search(request);
    assertEquals(0, result.getDocuments().size());
  }
   
}
