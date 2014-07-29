/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.paging;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ImmutableConstructor;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.util.ArgumentChecker;

/**
 * Simple immutable description of a range of results.
 * <p>
 * This class is the result of using {@link PagingRequest} to obtain an indexed subset of results.
 * This may represent traditional fixed paging or arbitrary paging starting from an index.
 * <p>
 * This class is immutable and thread-safe.
 */
@BeanDefinition(builderScope = "private")
public final class Paging implements ImmutableBean {

  /**
   * The request.
   */
  @PropertyDefinition(get = "manual", validate = "notNull")
  private final PagingRequest _request;
  /**
   * The total number of items.
   */
  @PropertyDefinition(get = "manual")
  private final int _totalItems;

  //-------------------------------------------------------------------------
  /**
   * Creates an instance from a paging request and total number of items.
   * 
   * @param pagingRequest  the paging request to base the result on, not null
   * @param totalItems  the total number of items
   * @return the created paging, not null
   */
  public static Paging of(final PagingRequest pagingRequest, final int totalItems) {
    return new Paging(pagingRequest, totalItems);
  }

  /**
   * Creates an instance based on the specified collection setting the total count of items.
   * <p>
   * This combines {@link PagingRequest#ALL} with the collection size.
   * 
   * @param coll  the collection to base the paging on, not null
   * @return the created paging, not null
   */
  public static Paging ofAll(final Collection<?> coll) {
    ArgumentChecker.notNull(coll, "coll");
    return new Paging(PagingRequest.ALL, coll.size());
  }

  /**
   * Creates an instance based on the specified collection setting the total count of items.
   * <p>
   * This combines the specified paging request with the collection size.
   * 
   * @param pagingRequest  the paging request to base the result on, not null
   * @param coll  the collection to base the paging on, not null
   * @return the created paging, not null
   */
  public static Paging of(PagingRequest pagingRequest, Collection<?> coll) {
    ArgumentChecker.notNull(pagingRequest, "pagingRequest");
    ArgumentChecker.notNull(coll, "coll");
    if (pagingRequest.getFirstItem() >= coll.size()) {
      return new Paging(PagingRequest.ofIndex(coll.size(), pagingRequest.getPagingSize()), coll.size());
    }
    return new Paging(pagingRequest, coll.size());
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   * 
   * @param pagingRequest  the request, not null
   * @param totalItems  the total number of items, zero or greater
   */
  @ImmutableConstructor
  private Paging(final PagingRequest pagingRequest, final int totalItems) {
    ArgumentChecker.notNull(pagingRequest, "pagingRequest");
    ArgumentChecker.notNegative(totalItems, "totalItems");
    _request = pagingRequest;
    _totalItems = totalItems;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the request that represents the results.
   * <p>
   * This request represents that request that matches the results.
   * This is not necessarily the same as the request actually used.
   * 
   * @return the request, not null
   */
  public PagingRequest getRequest() {
    return _request;
  }

  /**
   * Gets the total number of items in the complete result set.
   * <p>
   * This is the number of results that would be returned if  {@link PagingRequest#ALL} was used.
   * 
   * @return the number of items, zero or greater
   */
  public int getTotalItems() {
    return _totalItems;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the first item, using a zero-based index.
   * 
   * @return the first item index, zero-based
   */
  public int getFirstItem() {
    return getRequest().getFirstItem();
  }

  /**
   * Gets the first item, using a one-based index.
   * 
   * @return the first item number, one-based
   */
  public int getFirstItemOneBased() {
    return getRequest().getFirstItemOneBased();
  }

  /**
   * Gets the last item exclusive, using a zero-based index.
   * 
   * @return the last item index, exclusive, zero-based
   */
  public int getLastItem() {
    return Math.min(getRequest().getLastItem(), getTotalItems());
  }

  /**
   * Gets the last item inclusive, using a one-based index.
   * 
   * @return the last item number, inclusive, one-based
   */
  public int getLastItemOneBased() {
    return getLastItem();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the current page number, one-based, when viewed as traditional paging.
   * <p>
   * If the request was for index-based paging rather than traditional paging
   * then the result of this method will be the effective page of the first item.
   * 
   * @return the current page, one or greater
   */
  public int getPageNumber() {
    return (getFirstItem() / getPagingSize()) + 1;
  }

  /**
   * Gets the page size, which is the number of items requested.
   * <p>
   * This is zero if no data was requested.
   * 
   * @return the number of items in the page, zero or greater
   */
  public int getPagingSize() {
    return getRequest().getPagingSize();
  }

  /**
   * Gets the total number of pages, one-based, when viewed as traditional paging.
   * 
   * @return the number of pages, one or greater
   * @throws ArithmeticException if a paging request of NONE was used
   */
  public int getTotalPages() {
    return (getTotalItems() - 1) / getPagingSize() + 1;
  }

  /**
   * Checks whether a paging request of NONE was used, returning only the
   * total item count.
   * 
   * @return true if unable to use paging
   */
  public boolean isSizeOnly() {
    return getPagingSize() == 0;
  }

  /**
   * Checks whether there is a next page available.
   * This is the opposite of {@link #isLastPage()}.
   * 
   * @return true if there is another page
   * @throws IllegalStateException if insufficient information was requested - PagingRequest.NONE
   */
  public boolean isNextPage() {
    checkPaging();
    return getPageNumber() < getTotalPages();
  }

  /**
   * Checks whether this is the last page.
   * This is the opposite of {@link #isNextPage()}.
   * 
   * @return true if this is the last page
   * @throws IllegalStateException if insufficient information was requested - PagingRequest.NONE
   */
  public boolean isLastPage() {
    checkPaging();
    return getPageNumber() == getTotalPages();
  }

  /**
   * Checks whether there is a previous page available.
   * This is the opposite of {@link #isFirstPage()}.
   * 
   * @return true if there is a previous page
   * @throws IllegalStateException if insufficient information was requested - PagingRequest.NONE
   */
  public boolean isPreviousPage() {
    checkPaging();
    return getPageNumber() > 1;
  }

  /**
   * Checks whether this is the first page.
   * This is the opposite of {@link #isPreviousPage()}.
   * 
   * @return true if this is the last page
   * @throws IllegalStateException if insufficient information was requested - PagingRequest.NONE
   */
  public boolean isFirstPage() {
    checkPaging();
    return getPageNumber() == 1;
  }

  //-------------------------------------------------------------------------
  /**
   * Converts this object to a {@code PagingRequest} for the same page.
   * <p>
   * This can convert an index-based original request into a page-based one.
   * 
   * @return the request for the same page, not null
   */
  public PagingRequest toPagingRequest() {
    if (isSizeOnly()) {
      return PagingRequest.NONE;
    }
    return PagingRequest.ofPage(getPageNumber(), getPagingSize());
  }

  /**
   * Gets the {@code PagingRequest} for the next page.
   * 
   * @return the request for the next page, not null
   * @throws IllegalStateException if insufficient information was requested - PagingRequest.NONE
   * @throws NoSuchElementException if there are no more pages
   */
  public PagingRequest nextPagingRequest() {
    checkPaging();
    if (isLastPage()) {
      throw new NoSuchElementException("Unable to return next page as this is the last page");
    }
    return PagingRequest.ofPage(getPageNumber() + 1, getPagingSize());
  }

  /**
   * Gets the {@code PagingRequest} for the next page.
   * 
   * @return the request for the previous page, not null
   * @throws IllegalStateException if insufficient information was requested - PagingRequest.NONE
   * @throws NoSuchElementException if there are no more pages
   */
  public PagingRequest previousPagingRequest() {
    checkPaging();
    if (isFirstPage()) {
      throw new NoSuchElementException("Unable to return previous page as this is the first page");
    }
    return PagingRequest.ofPage(getPageNumber() - 1, getPagingSize());
  }

  /**
   * Checks if this represents a valid paging request for paging.
   * 
   * @throws IllegalStateException if insufficient information was requested - PagingRequest.NONE
   */
  private void checkPaging() {
    if (isSizeOnly()) {
      throw new IllegalStateException("Paging base on PagingRequest.NONE");
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[first=" + getFirstItem() + ", size=" + getPagingSize() + ", totalItems=" + _totalItems + "]";
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code Paging}.
   * @return the meta-bean, not null
   */
  public static Paging.Meta meta() {
    return Paging.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(Paging.Meta.INSTANCE);
  }

  @Override
  public Paging.Meta metaBean() {
    return Paging.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      Paging other = (Paging) obj;
      return JodaBeanUtils.equal(getRequest(), other.getRequest()) &&
          (getTotalItems() == other.getTotalItems());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getRequest());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTotalItems());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code Paging}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code request} property.
     */
    private final MetaProperty<PagingRequest> _request = DirectMetaProperty.ofImmutable(
        this, "request", Paging.class, PagingRequest.class);
    /**
     * The meta-property for the {@code totalItems} property.
     */
    private final MetaProperty<Integer> _totalItems = DirectMetaProperty.ofImmutable(
        this, "totalItems", Paging.class, Integer.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "request",
        "totalItems");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1095692943:  // request
          return _request;
        case -725711140:  // totalItems
          return _totalItems;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public Paging.Builder builder() {
      return new Paging.Builder();
    }

    @Override
    public Class<? extends Paging> beanType() {
      return Paging.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code request} property.
     * @return the meta-property, not null
     */
    public MetaProperty<PagingRequest> request() {
      return _request;
    }

    /**
     * The meta-property for the {@code totalItems} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Integer> totalItems() {
      return _totalItems;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1095692943:  // request
          return ((Paging) bean).getRequest();
        case -725711140:  // totalItems
          return ((Paging) bean).getTotalItems();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code Paging}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<Paging> {

    private PagingRequest _request;
    private int _totalItems;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1095692943:  // request
          return _request;
        case -725711140:  // totalItems
          return _totalItems;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 1095692943:  // request
          this._request = (PagingRequest) newValue;
          break;
        case -725711140:  // totalItems
          this._totalItems = (Integer) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public Paging build() {
      return new Paging(
          _request,
          _totalItems);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("Paging.Builder{");
      buf.append("request").append('=').append(JodaBeanUtils.toString(_request)).append(',').append(' ');
      buf.append("totalItems").append('=').append(JodaBeanUtils.toString(_totalItems));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
