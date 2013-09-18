package com.opengamma.master;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.CombinedMaster.SearchCallback;
import com.opengamma.master.CombinedMaster.SearchStrategy;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class CombinedMasterTest {

  private HolidayMaster m1;
  private HolidayMaster m2;
  
  private ObjectId o1;
  private ObjectId o2;
  private UniqueId u1;
  private UniqueId u2;
  
  private HolidayDocument d1;
  private HolidayDocument d2;
  
  private CombinedMaster<HolidayDocument, HolidayMaster> cMaster;
  
  @BeforeMethod
  public void beforeMethod() {
    m1 = mock(HolidayMaster.class);
    m2 = mock(HolidayMaster.class);
    o1 = ObjectId.of("TestScheme", "123");
    o2 = ObjectId.of("TestScheme2", "234");
    u1 = UniqueId.of(o1, "v123");
    u2 = UniqueId.of(o2, "v234");
    d1 = mock(HolidayDocument.class);
    d2 = mock(HolidayDocument.class);
    when(d1.getUniqueId()).thenReturn(u1);
    when(d1.getObjectId()).thenReturn(o1);
    cMaster = new CombinedMaster<HolidayDocument, HolidayMaster>(ImmutableList.of(m1, m2)) {};
  }
  
  @Test
  public void add() {
    when(m1.add(d1)).thenReturn(d1);
    
    cMaster.add(d1);
    
    verify(m1).add(d1);
  }

  @Test
  public void addVersionToM1() {
    //test multiple invocations here
    when(m1.replaceVersions(o1, Collections.singletonList(d1))).thenReturn(Collections.singletonList(u1));
    when(m1.replaceVersions(o1, Collections.singletonList(d1))).thenReturn(Collections.singletonList(u1));
    
    cMaster.replaceVersions(o1, Collections.singletonList(d1));
    cMaster.replaceVersions(o1, Collections.singletonList(d1));
    
    verify(m1, times(2)).replaceVersions(o1, Collections.singletonList(d1));
  }

  @Test
  public void addVersionToM2() {
    
    when(m1.replaceVersions(o1, Collections.singletonList(d1))).thenThrow(new IllegalArgumentException());
    when(m2.replaceVersions(o1, Collections.singletonList(d1))).thenReturn(Collections.singletonList(u1));

    cMaster.replaceVersions(o1, Collections.singletonList(d1));
    
    verify(m1).replaceVersions(o1, Collections.singletonList(d1));
  }
  
  @Test(expectedExceptions= {IllegalArgumentException.class})
  public void addVersionException() {
    
    when(m1.replaceVersions(o1, Collections.singletonList(d1))).thenThrow(new IllegalArgumentException());
    when(m2.replaceVersions(o1, Collections.singletonList(d1))).thenThrow(new IllegalArgumentException());

    cMaster.replaceVersions(o1, Collections.singletonList(d1));
  }

  @Test
  public void applyPaging() {
    HolidayDocument m1h1 = holidayDocWithId("m1", "1");
    HolidayDocument m1h2 = holidayDocWithId("m1", "2");
    HolidayDocument m1h3 = holidayDocWithId("m1", "3");
    HolidayDocument m1h4 = holidayDocWithId("m1", "4");
    HolidaySearchResult sr;
    sr = new HolidaySearchResult(ImmutableList.of(m1h1, m1h2, m1h3, m1h4));
    
    PagingRequest ofIndex ;
    ofIndex = PagingRequest.ofIndex(1, 3);
    
    cMaster.applyPaging(sr, ofIndex);
    
    assertEquals(Paging.of(ofIndex, 4), sr.getPaging());
    assertEquals(ImmutableList.of(m1h2, m1h3, m1h4), sr.getDocuments());

    sr = new HolidaySearchResult(ImmutableList.of(m1h1, m1h2, m1h3, m1h4));
    
    ofIndex = PagingRequest.ofIndex(100, 103);
    
    cMaster.applyPaging(sr, ofIndex);
    
    assertEquals(Paging.of(ofIndex, 4), sr.getPaging());
    assertEquals(ImmutableList.of(), sr.getDocuments());

  }

  @Test
  public void correct() {
    //test multiple invocations here
    cMaster.correct(d1);
    cMaster.correct(d1);
    
    verify(m1, times(2)).correct(d1);
  }

  @Test
  public void getUniqueId() {
    //test multiple invocations here
    when(m1.get(u1)).thenReturn(d1);
    
    cMaster.get(u1);
    cMaster.get(u1);
    
    verify(m1, times(2)).get(u1);
  }

  @Test
  public void getObjectIdentifiableVersionCorrection() {
    VersionCorrection vc = VersionCorrection.LATEST;
    //test multiple invocations here
    when(m1.get(o1, vc)).thenReturn(d1);
    
    cMaster.get(o1, vc);
    cMaster.get(o1, vc);
    
    verify(m1, times(2)).get(o1, vc);
  }

  @Test
  public void getMasterList() {
    assertEquals(ImmutableList.of(m1, m2), cMaster.getMasterList());
  }

  @Test
  public void remove() {
    //test multiple invocations here
    cMaster.remove(d1);
    cMaster.remove(d1);
    
    verify(m1, times(2)).remove(d1);
  }

  @Test
  public void removeVersion() {
    cMaster.removeVersion(u1);
    cMaster.removeVersion(u1);
    
    verify(m1, times(2)).replaceVersion(u1, Collections.<HolidayDocument>emptyList());
  }

  @Test
  public void replaceAllVersions() {
    cMaster.replaceAllVersions(o1, Lists.newArrayList(d1));
    cMaster.replaceAllVersions(o1, Lists.newArrayList(d1));
    
    verify(m1, times(2)).replaceAllVersions(o1, Lists.newArrayList(d1));
  }

  @Test
  public void replaceVersionUniqueIdListD() {
    cMaster.replaceVersion(u1, Lists.newArrayList(d1));
    cMaster.replaceVersion(u1, Lists.newArrayList(d1));
    
    verify(m1, times(2)).replaceVersion(u1, Lists.newArrayList(d1));
    
  }

  @Test
  public void replaceVersionD() {
    cMaster.replaceVersion(d1);
    cMaster.replaceVersion(d1);
    
    verify(m1, times(2)).replaceVersion(u1, Collections.singletonList(d1));
  }

  @Test
  public void replaceVersions() {
    cMaster.replaceVersions(o1, Collections.singletonList(d1));
    cMaster.replaceVersions(o1, Collections.singletonList(d1));
    
    verify(m1, times(2)).replaceVersions(o1, Collections.singletonList(d1));

  }

  @Test
  public void search() {
    
    HolidayDocument m1h1 = holidayDocWithId("m1", "1");
    HolidayDocument m1h2 = holidayDocWithId("m1", "2");
    HolidayDocument m1h3 = holidayDocWithId("m1", "3");
    HolidayDocument m1h4 = holidayDocWithId("m1", "4");

    HolidayDocument m2h3 = holidayDocWithId("m2", "3");
    HolidayDocument m2h4 = holidayDocWithId("m2", "4");
    HolidayDocument m2h5 = holidayDocWithId("m2", "5");
    HolidayDocument m2h6 = holidayDocWithId("m2", "6");
    final HolidayDocument m2h7 = holidayDocWithId("m2", "7");

    HolidaySearchResult m1Result = new HolidaySearchResult(Lists.newArrayList(m1h1, m1h2, m1h2, m1h3, m1h4, m1h4));
    HolidaySearchResult m2Result = new HolidaySearchResult(Lists.newArrayList(m2h3, m2h4, m2h5, m2h6, m2h7));
    
    final List<HolidayDocument> resultList = Lists.newArrayList();
    
    @SuppressWarnings("unchecked")
    final SearchCallback<HolidayDocument, HolidayMaster> cbDelegate = mock(SearchCallback.class);
    
    cMaster.search(Lists.newArrayList(m1Result, m2Result, null), new SearchCallback<HolidayDocument, HolidayMaster>() {

      @Override
      public int compare(HolidayDocument arg0, HolidayDocument arg1) {
        return arg0.getUniqueId().getValue().compareTo(arg1.getUniqueId().getValue());
      }

      @Override
      public boolean include(HolidayDocument document) {
        return !m2h7.equals(document);
      }

      @Override
      public void accept(HolidayDocument document, HolidayMaster master, boolean masterUnique, boolean clientUnique) {
        cbDelegate.accept(document, master, masterUnique, clientUnique);
        resultList.add(document);
      }
    });
    
    verify(cbDelegate).accept(m1h1, m1, true, true);
    verify(cbDelegate, times(2)).accept(m1h2, m1, false, true);
    verify(cbDelegate).accept(m1h3, m1, true, false);
    verify(cbDelegate).accept(m2h3, m2, true, false);
    verify(cbDelegate, times(2)).accept(m1h4, m1, false, false);
    verify(cbDelegate).accept(m2h4, m2, true, false);
    verify(cbDelegate).accept(m2h5, m2, true, true);
    verify(cbDelegate).accept(m2h6, m2, true, true);
    verifyNoMoreInteractions(cbDelegate);
    
    
    ArrayList<HolidayDocument> sortedResultList = Lists.newArrayList(resultList);
    Collections.sort(sortedResultList, cbDelegate);
    assertEquals(sortedResultList, resultList);

    
  }

  private HolidayDocument holidayDocWithId(String scheme, String id) {
    HolidayDocument holidayDocument = new HolidayDocument();
    holidayDocument.setUniqueId(UniqueId.of(scheme, id));
    return holidayDocument;
  }
  
  @Test
  public void get() {
    ArrayList<UniqueId> getList = Lists.newArrayList(u1, u2);
    
    when(m1.get(u2)).thenThrow(new IllegalArgumentException());
    
    cMaster.get(getList);
    cMaster.get(getList);
    
    verify(m1, times(2)).get(u1);
    verify(m2, times(2)).get(u2);
  }

  @Test
  public void update() {
    //test multiple invocations here
    cMaster.update(d1);
    cMaster.update(d1);
    
    verify(m1, times(2)).update(d1);

  }
  
  
  @Test
  public void pagedSearchEmpty() {
    PagingRequest pr = PagingRequest.ALL;
    List<HolidayDocument> m1Result = Lists.newArrayList();
    List<HolidayDocument> m2Result = Lists.newArrayList();
    
    List<HolidayDocument> result = runPagedSearch(pr, m1Result, 0, m2Result, 0);
    
    assertTrue(result.isEmpty());
  }

  @Test
  public void pagedSearchOneElement() {
    PagingRequest pr = PagingRequest.ALL;
    List<HolidayDocument> singleton = Lists.newArrayList(d1);
    List<HolidayDocument> empty = Lists.newArrayList();
    
    List<HolidayDocument> result = runPagedSearch(pr, singleton, 1, empty, 0);
    assertEquals(1, result.size());
  
    List<HolidayDocument> result2 = runPagedSearch(pr, empty, 0, singleton, 1);
    assertEquals(1, result2.size());

  }

  @Test
  public void pagedSearchTwoElements() {
    PagingRequest pr;
    List<HolidayDocument> singleton = Lists.newArrayList(d1);
    List<HolidayDocument> empty = Lists.newArrayList();
    
    pr = PagingRequest.ofIndex(0, 1);
    List<HolidayDocument> result2 = runPagedSearch(pr, singleton, 1, singleton, 1);
    assertEquals(1, result2.size());
    verifyNoMoreInteractions(m2);
  
    pr = PagingRequest.ALL;
    List<HolidayDocument> result = runPagedSearch(pr, singleton, 1, singleton, 1);
    assertEquals(2, result.size());
  
  
    pr = PagingRequest.ofIndex(1, 1);
    List<HolidayDocument> result3 = runPagedSearch(pr, empty, 1, singleton, 1);
    assertEquals(1, result3.size());
  }

  @Test
  public void pagedSearchMultiple() {
    PagingRequest pr = PagingRequest.ofIndex(2, 2);
    List<HolidayDocument> m1Result = Lists.newArrayList(d1);
    List<HolidayDocument> m2Result = Lists.newArrayList(d2);
    List<HolidayDocument> expected = Lists.newArrayList(d1, d2);
    
    List<HolidayDocument> result = runPagedSearch(pr, m1Result, 3, m2Result, 1);
    
    assertEquals(2, result.size());
    
    assertEquals(expected, result);
    
  }

  @Test
  public void pagedSearchOnlyFirst() {
    List<HolidayDocument> m2Result = Lists.newArrayList();
    List<HolidayDocument> result ;
    
    result = runPagedSearch(PagingRequest.ofIndex(0, 2), Lists.newArrayList(d1, d1), 3, m2Result, 0);
    assertEquals(2, result.size());
    
    result = runPagedSearch(PagingRequest.ofIndex(1, 2), Lists.newArrayList(d1, d1), 3, m2Result, 0);
    assertEquals(2, result.size());
    
    result = runPagedSearch(PagingRequest.ofIndex(0, 3), Lists.newArrayList(d1, d1, d1), 3, m2Result, 0);
    assertEquals(3, result.size());
    
    verifyNoMoreInteractions(m2);
    
  }
  
  private List<HolidayDocument> runPagedSearch(PagingRequest pr, List<HolidayDocument> m1Result, int m1Total, List<HolidayDocument> m2Result, int m2Total) {
    HolidaySearchResult result = new HolidaySearchResult();
    HolidaySearchRequest searchRequest = new HolidaySearchRequest();
    searchRequest.setPagingRequest(pr);
    HolidaySearchResult m1SearchResult = new HolidaySearchResult();
    m1SearchResult.setDocuments(m1Result);
    m1SearchResult.setPaging(Paging.of(PagingRequest.ofIndex(0, m1Result.size()), m1Total));
    HolidaySearchResult m2SearchResult = new HolidaySearchResult();
    m2SearchResult.setPaging(Paging.of(PagingRequest.ofIndex(0, m2Result.size()), m2Total));
    m2SearchResult.setDocuments(m2Result);
    
    when(m1.search(Mockito.<HolidaySearchRequest>any())).thenReturn(m1SearchResult);
    when(m2.search(Mockito.<HolidaySearchRequest>any())).thenReturn(m2SearchResult);
    
    cMaster.pagedSearch(new SearchStrategy<HolidayDocument, HolidayMaster, HolidaySearchRequest>() {

      @Override
      public HolidaySearchResult search(HolidayMaster master, HolidaySearchRequest searchRequest) {
        return master.search(searchRequest);
      }
    }, result, searchRequest);
    
    return result.getDocuments();
  }
}
