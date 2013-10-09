/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master;

import static com.google.common.collect.Maps.newHashMap;
import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;

/**
 * A 'master' which simply delegates to other wrapped masters.
 *
 * @param <D> master document type
 * @param <M> master type
 */
public abstract class CombinedMaster<D extends AbstractDocument, M extends AbstractMaster<D>> implements AbstractMaster<D> {

  private static final Logger s_logger = LoggerFactory.getLogger(CombinedMaster.class);

  private final List<M> _masterList;
  private final ConcurrentMap<String, M> _schemeToMaster = Maps.newConcurrentMap();

  protected CombinedMaster(List<M> masterList) {
    ArgumentChecker.notNull(masterList, "masterList");
    ArgumentChecker.notEmpty(masterList, "masterList");
    _masterList = masterList;
  }

  protected List<M> getMasterList() {
    return _masterList;
  }
  
  protected M getMasterByScheme(final String scheme) {
    return _schemeToMaster.get(scheme);
  }

  protected void setMasterScheme(String scheme, M master) {
    _schemeToMaster.putIfAbsent(scheme, master);
  }

  /**
   * A try-catch template which handles any {@link IllegalArgumentException} thrown
   * from {@link #tryMaster(AbstractMaster)}. Only if no {@link #tryMaster(AbstractMaster)}
   * calls return is an {@link IllegalArgumentException} thrown.
   * @param <T> the type returned by {@link #tryMaster(AbstractMaster)}
   */
  protected abstract class Try<T> {

    public abstract T tryMaster(M master);

    public T each(final String scheme) {
      T result;
      for (M master : _masterList) {
        try {
          result = tryMaster(master);
          setMasterScheme(scheme, master);
          return result;
        } catch (IllegalArgumentException e) {
          s_logger.debug("Illegal argument exception from master", e);
        }
      }
      throw new IllegalArgumentException("No masters accepted request on scheme " + scheme);
    }

  }

  @Override
  public D get(final UniqueId uniqueId) {
    final M master = getMasterByScheme(uniqueId.getScheme());
    if (master != null) {
      return master.get(uniqueId);
    }
    return (new Try<D>() {
      @Override
      public D tryMaster(final M master) {
        return master.get(uniqueId);
      }
    }).each(uniqueId.getScheme());
  }

  @Override
  public D get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    final M master = getMasterByScheme(objectId.getObjectId().getScheme());
    if (master != null) {
      return master.get(objectId, versionCorrection);
    }
    return (new Try<D>() {
      @Override
      public D tryMaster(final M master) {
        return master.get(objectId, versionCorrection);
      }
    }).each(objectId.getObjectId().getScheme());
  }

  @Override
  public D add(final D document) {
    //no unique id on a new record so no scheme as yet. have to just add to first master.
    return _masterList.get(0).add(document);
  }

  @Override
  public D update(final D document) {
    final M master = getMasterByScheme(document.getUniqueId().getScheme());
    if (master != null) {
      return master.update(document);
    }
    return (new Try<D>() {
      @Override
      public D tryMaster(final M master) {
        return master.update(document);
      }
    }).each(document.getUniqueId().getScheme());
  }

  @Override
  public void remove(final ObjectIdentifiable objectIdentifiable) {
    final M master = getMasterByScheme(objectIdentifiable.getObjectId().getScheme());
    if (master != null) {
      master.remove(objectIdentifiable);
      return;
    }
    (new Try<Void>() {
      @Override
      public Void tryMaster(final M master) {
        master.remove(objectIdentifiable);
        return null;
      }
    }).each(objectIdentifiable.getObjectId().getScheme());
  }

  @Override
  public D correct(final D document) {
    final M master = getMasterByScheme(document.getUniqueId().getScheme());
    if (master != null) {
      return master.correct(document);
    }
    return (new Try<D>() {
      @Override
      public D tryMaster(final M master) {
        return master.correct(document);
      }
    }).each(document.getUniqueId().getScheme());
  }

  
  /**
   * Implements the logic of querying one master after another. The paging information
   * passed on the searchRequst will be modified so that it is appropriate to each
   * master delegated to. Original paging information will be restored to this instance
   * once the method completes (successfully).
   * 
   * Note that any documents returned by the searchStrategy will be added to the 
   * documentsResult object by this method. However, no other result fields are
   * managed here. If other fields are need to be salvaged from the DocumentsResult,
   * it should be done within the strategy itself.
   * 
   * @param searchStrategy the search strategy to execute
   * @param documentsResult the documents result to append documents to
   * @param searchRequest the search request to execute
   * @param <S> the search request type
   */
  protected <S extends PagedRequest> void pagedSearch(SearchStrategy<D, M, S> searchStrategy, AbstractDocumentsResult<D> documentsResult, S searchRequest) {
    //first, hive off the original paging request. this is
    //because we're about to mutate the searchRequest so 
    //that it is appropriate for downstream masters.
    PagingRequest originalPagingRequest = searchRequest.getPagingRequest();
    //first page to request will be identical to passed
    //page request.
    PagingRequest nextPage = originalPagingRequest;
    //totalResults used to keep track of whether we
    //have accumulated enough records to satisfy the
    //page size.
    int totalResults = 0;
    //iterate over the master list, stopping when we
    //either run out of masters or the totalResults
    //exceeds the range of records requested.
    for (int i = 0; i < getMasterList().size(); i++) {
      M master = getMasterList().get(i);
      //delegate the query to the master via the strategy
      AbstractDocumentsResult<D> masterResult = searchStrategy.search(master, searchRequest);
      //totalItems() reflects the total number of records
      //available from the queried master.
      totalResults += masterResult.getPaging().getTotalItems();
      documentsResult.getDocuments().addAll(masterResult.getDocuments());
      if (i == getMasterList().size() - 1) {
        //all masters are exhausted so stop here.
        //totalResults will reflect the total number of
        //records available across masters to no need
        //to modify it (see next condition).
        break;
      }
      if (totalResults >= nextPage.getLastItem()) {
        //we have maxed out the requested window and more masters remain.
        //add one to ensure that the gui makes further requests which will
        //run into the next master's range.
        totalResults += 1;
        break;
      }
      //more masters remain, so adjust the page size to
      //be appropriate for them. this is done by taking
      //into account the totalResults received so far.
      nextPage = getNextPage(totalResults, nextPage);
      searchRequest.setPagingRequest(nextPage);
    }
    searchRequest.setPagingRequest(originalPagingRequest); //put the original paging back
    documentsResult.setPaging(Paging.of(originalPagingRequest, totalResults));
  }
  
  /**
   * Calculates the next page to ask for in the sequence of queries to masters.
   * @param itemsFound items found in the last query
   * @param lastRequest the last paging request
   * @return the next paging request
   */
  private PagingRequest getNextPage(int itemsFound, PagingRequest lastRequest) {
    int nextFirstItem = lastRequest.getFirstItem() - itemsFound;
    int nextWindowSize = lastRequest.getPagingSize();
    if (nextFirstItem < 0) {
      //need to adjust window size
      nextWindowSize += nextFirstItem;
      nextFirstItem = 0;
      
      if (nextWindowSize <= 0) {
        throw new IllegalStateException(format("Failed to compute next window size. Last page: %s, items found: %d", lastRequest, itemsFound));
      }
      
    }
    return PagingRequest.ofIndex(nextFirstItem, nextWindowSize);
  }
  
  /**
   * Used in {@link CombinedMaster#pagedSearch(SearchStrategy, AbstractDocumentsResult, PagedRequest)} for indicating
   * how the master should be queried. Lots of nasty generics here, but there's not really much choice since
   * the masters all have their own query methods.
   * @param <D> document type
   * @param <M> master type
   * @param <S> search request type
   */
  public interface SearchStrategy<D extends AbstractDocument, M extends AbstractMaster<D>, S extends PagedRequest> {
    
    /**
     * @param master the master to query
     * @param searchRequest the search request, with appropriate paging set
     * @return the list of documents returned
     */
    AbstractDocumentsResult<D> search(M master, S searchRequest);
    
  }
  
  
  /**
   * A callback object which describes a document order, a filtering policy,
   * and a callback method for handling the document.
   * @param <D> The document type.
   */
  public interface SearchCallback<D, M> extends Comparator<D> {

    /**
     * Returns true to include the document in the callback results, false
     * to suppress it.
     *
     * @param document document to consider, not null
     * @return whether to include the document
     */
    boolean include(D document);

    /**
     * Passes a document to the callback. Only documents that were accepted by
     * {@link #include} should be passed.
     *
     * @param document document to consider, not null
     * @param master the master that sourced the document, not null
     * @param masterUnique true if this comparator did not make the document
     *        equal to any other document passed from this master
     * @param clientUnique true if this comparator did not make the document
     *        equal to any other document from the masters on this client
     */
    void accept(D document, M master, boolean masterUnique, boolean clientUnique);

  }

  private List<D> next(final ListIterator<D> itr, final SearchCallback<D, M> callback) {
    List<D> results = Lists.newArrayList();
    D lastIncluded = null;
    while (itr.hasNext()) {
      final D element = itr.next();
      if (lastIncluded != null) {
        //check if the next is the same as last included, otherwise break
        if (callback.compare(lastIncluded, element) != 0) {
          itr.previous();
          break;
        }
      }
      if (callback.include(element)) {
        lastIncluded = element;
        results.add(element);
      }
    }
    return results;
  }

  private List<D> getNonUnique(final Set<D> nonUnique, final List<D> documents, final SearchCallback<D, M> callback) {
    Collections.sort(documents, callback);
    D previous = null;
    for (D document : documents) {
      if (callback.include(document)) {
        if (previous != null) {
          if (callback.compare(previous, document) == 0) {
            nonUnique.add(previous);
            nonUnique.add(document);
          }
        }
        previous = document;
      }
    }
    return documents;
  }

  /**
   * Utility method to scan a set of results from the configured masters. The passed callback defines the
   * ordering (compare), filtering (include) and, ultimately, what to do with the record (accept). Sorted
   * results from the masters are passed to the callback object along with metadata regarding whether
   * duplicates (as defined by the compare implementation) exist. Duplicates are either indicated as across
   * masters or within a master.
   * @param results the list of results. The order should correspond with the order of the configured masters.
   * @param callback the object to define ordering, filtering and what to do with each record.
   */
  protected void search(List<? extends AbstractSearchResult<D>> results, final SearchCallback<D, M> callback) {
    final Set<D> nonUnique = new HashSet<D>();
    List<ListIterator<D>> iterators = Lists.newArrayList();
    Set<Integer> nextIteratorsToUse = Sets.newHashSet();
    for (AbstractSearchResult<D> searchResult : results) {
      final List<D> documents = (searchResult != null) ? getNonUnique(nonUnique, searchResult.getDocuments(), callback) : Collections.<D>emptyList();
      iterators.add(documents.listIterator());
      nextIteratorsToUse.add(iterators.size() - 1);
    }
    int remainingIterators = iterators.size();
    
    List<IndexedElement> elements = Lists.newArrayList();
    
    do {
      //refill elements
      remainingIterators = refillElements(elements, callback, iterators, nextIteratorsToUse, remainingIterators);
      //check we're not in a silly state here
      assert remainingIterators >= 0;
      
      if (remainingIterators < 0) {
        s_logger.error("Illegal state - number of remaining iterators is negative.");
        //allow to continue in the interest of providing a result...
      }
      if (remainingIterators <= 0) {
        break;
      }
      
      //sort the elements for iteration.
      //reverse ordering used so we're not popping from the start of the array list.
      Collections.sort(elements, Ordering.natural().reverse());
      
      ListIterator<IndexedElement> it = elements.listIterator(elements.size());
      List<IndexedElement> elementResults = Lists.newLinkedList();
      while (true) {
        IndexedElement indexedElement = it.previous();
        it.remove();
        elementResults.add(indexedElement);
        int index = indexedElement._index;
        nextIteratorsToUse.add(index);
        if (!it.hasPrevious()) {
          break;
        }
        D document = indexedElement._element;
        IndexedElement peekedElement = elements.get(elements.size() - 1);
        if (callback.compare(document, peekedElement._element) != 0) {
          break;
        }
      };
      
      if (elementResults.size() == 1) {
        //this document was unique
        IndexedElement indexedElement = elementResults.get(0);
        //booleans are trivially true since list is a singleton
        callback.accept(indexedElement._element, _masterList.get(indexedElement._index), true, true);
      } else {
        //multiple documents across master, so only unique across masters if records picked from a single iterator
        boolean uniqueAcrossMasters = nextIteratorsToUse.size() == 1;
        for (IndexedElement element : elementResults) {
          boolean uniqueWithinMaster = !nonUnique.contains(element._element);
          callback.accept(element._element, _masterList.get(element._index), uniqueWithinMaster, uniqueAcrossMasters);
        }
      }
      
    } while (true);
  }

  private int refillElements(List<IndexedElement> elements, SearchCallback<D, M> callback, List<ListIterator<D>> iterators, Set<Integer> nextIteratorsToUse, int remainingIterators) {
    for (Iterator<Integer> it = nextIteratorsToUse.iterator(); it.hasNext();) {
      Integer i = it.next();
      it.remove();
      List<D> next = next(iterators.get(i), callback);
      if (!next.isEmpty()) {
        for (D nextElement : next) {
          elements.add(new IndexedElement(i, nextElement, callback));
        }
      } else {
        //iterator is done. set to null
        iterators.set(i, null);
        remainingIterators--;
      }
      
    }
    return remainingIterators;
  }
  
  
  /**
   * Internal class for the simple purpose of retaining the iterator an
   * element came from after the list of next elements is sorted. See 
   * {@link CombinedMaster#search(List, SearchCallback)} method for 
   * usage.
   */
  private class IndexedElement implements Comparable<IndexedElement> {
    private final Comparator<D> _comparator;
    /**
     * Refers to the index of the iterator that this element originally came from
     */
    private final int _index;
    private final D _element;
    
    public IndexedElement(int index, D element, Comparator<D> comparator) {
      _index = index;
      _element = element;
      this._comparator = comparator;
    }

    @Override
    public int compareTo(IndexedElement o) {
      return _comparator.compare(_element, o._element);
    }
    
  }

  @Override
  public final List<UniqueId> replaceVersion(final UniqueId uniqueId, final List<D> replacementDocuments) {
    final String scheme = uniqueId.getScheme();
    final M master = getMasterByScheme(scheme);
    if (master != null) {
      return master.replaceVersion(uniqueId, replacementDocuments);
    }
    return (new Try<List<UniqueId>>() {
      @Override
      public List<UniqueId> tryMaster(final M master) {
        return master.replaceVersion(uniqueId, replacementDocuments);
      }
    }).each(scheme);
  }

  @Override
  public final List<UniqueId> replaceAllVersions(final ObjectIdentifiable objectId, final List<D> replacementDocuments) {
    final String scheme = objectId.getObjectId().getScheme();
    final M master = getMasterByScheme(scheme);
    if (master != null) {
      return master.replaceAllVersions(objectId, replacementDocuments);
    }
    return (new Try<List<UniqueId>>() {
      @Override
      public List<UniqueId> tryMaster(final M master) {
        return master.replaceAllVersions(objectId, replacementDocuments);
      }
    }).each(scheme);
  }

  @Override
  public List<UniqueId> replaceVersions(final ObjectIdentifiable objectId, final List<D> replacementDocuments) {
    final String scheme = objectId.getObjectId().getScheme();
    final M master = getMasterByScheme(scheme);
    if (master != null) {
      return master.replaceVersions(objectId, replacementDocuments);
    }
    return (new Try<List<UniqueId>>() {
      @Override
      public List<UniqueId> tryMaster(final M master) {
        return master.replaceVersions(objectId, replacementDocuments);
      }
    }).each(scheme);
  }

  @Override
  public final void removeVersion(final UniqueId uniqueId) {
    replaceVersion(uniqueId, Collections.<D>emptyList());
  }

  @Override
  public final UniqueId replaceVersion(D replacementDocument) {
    List<UniqueId> result = replaceVersion(replacementDocument.getUniqueId(), Collections.singletonList(replacementDocument));
    if (result.isEmpty()) {
      return null;
    } else {
      return result.get(0);
    }
  }

  @Override
  public final UniqueId addVersion(ObjectIdentifiable objectId, D documentToAdd) {
    List<UniqueId> result = replaceVersions(objectId, Collections.singletonList(documentToAdd));
    if (result.isEmpty()) {
      return null;
    } else {
      return result.get(0);
    }
  }
  
  @Override
  public Map<UniqueId, D> get(Collection<UniqueId> uniqueIds) {
    Map<UniqueId, D> map = newHashMap();
    for (UniqueId uniqueId : uniqueIds) {
      map.put(uniqueId, get(uniqueId));
    }
    return map;
  }

  /**
   * Resets the paging on a search request back to ALL and returns
   * the configured {@link PagingRequest}. This is useful for 
   * implementing search methods which require support for paging
   * and can be used with {@link #applyPaging(AbstractSearchResult, PagingRequest)}
   * to restrict the resultset back to what was originally requested.
   * Obviously, querying everything then filtering out the subset
   * required for the page is not the most efficient approach but
   * avoids complexities such as dealing with live updates.
   * @param request the search request to broaden
   * @return the {@link PagingRequest}
   */
  protected PagingRequest clearPaging(AbstractSearchRequest request) {
    PagingRequest pagingRequest = request.getPagingRequest();
    request.setPagingRequest(PagingRequest.ALL);
    return pagingRequest;
  }
  
  /**
   * Same as for {@link AbstractSearchRequest}.
   * @param request the search request
   * @return the paging request
   */
  protected PagingRequest clearPaging(AbstractHistoryRequest request) {
    PagingRequest pagingRequest = request.getPagingRequest();
    request.setPagingRequest(PagingRequest.ALL);
    return pagingRequest;
  }
  
  /**
   * Applies paging to a comprehensive search result. Only the subset of
   * records contained in this page are retained in the result.
   * Typically used after a call to {@link #clearPaging(AbstractSearchRequest)}.
   * @param result the search result
   * @param originalRequest the original request
   */
  protected void applyPaging(AbstractDocumentsResult<D> result, PagingRequest originalRequest) {
    result.setPaging(Paging.of(originalRequest, result.getDocuments().size()));
    int docSize = result.getDocuments().size();
    int firstItem = originalRequest.getFirstItem() > docSize ? docSize : originalRequest.getFirstItem();
    int lastItem = originalRequest.getLastItem() > docSize ? docSize : originalRequest.getLastItem();
    ArrayList<D> resultDocuments = Lists.newArrayList(result.getDocuments().subList(firstItem, lastItem));
    result.setDocuments(resultDocuments);
  }
  

}
