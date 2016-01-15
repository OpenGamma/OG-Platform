/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.paging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
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
import com.opengamma.util.PublicAPI;

/**
 * Simple immutable request for a page of results.
 * <p>
 * This class is follows the design of SQL OFFSET and FETCH/LIMIT, exposed as a first-item/size data model.
 * This can be used to implement traditional fixed paging or arbitrary paging starting from an index.
 * <p>
 * This class is immutable and thread-safe.
 */
@PublicAPI
@BeanDefinition(builderScope = "private")
public final class PagingRequest implements ImmutableBean, Serializable {

  /**
   * A default size for paging.
   */
  public static final int DEFAULT_PAGING_SIZE = 20;
  /**
   * Singleton constant to request all items (no paging).
   */
  public static final PagingRequest ALL = new PagingRequest(0, Integer.MAX_VALUE);
  /**
   * Singleton constant to request the first page of 20 items.
   */
  public static final PagingRequest FIRST_PAGE = new PagingRequest(0, DEFAULT_PAGING_SIZE);
  /**
   * Singleton constant to request the first matching item.
   */
  public static final PagingRequest ONE = new PagingRequest(0, 1);
  /**
   * Singleton constant to request no data, just the total count.
   */
  public static final PagingRequest NONE = new PagingRequest(0, 0);

  /**
   * The requested first item.
   */
  @PropertyDefinition(get = "manual")
  private final int _firstItem;
  /**
   * The requested number of items.
   */
  @PropertyDefinition(get = "manual")
  private final int _pagingSize;

  //-------------------------------------------------------------------------
  /**
   * Obtains an instance based on a zero-based index and requested size.
   * <p>
   * This factory represents the internal state directly.
   * The index is the first item in the list of results that is required (SQL OFFSET).
   * The size is the requested number of items (SQL FETCH/LIMIT).
   * 
   * @param index  the zero-based start index, zero or greater
   * @param size  the number of items to request, zero or greater
   * @return the paging request, not null
   * @throws IllegalArgumentException if either input is invalid
   */
  public static PagingRequest ofIndex(int index, int size) {
    return new PagingRequest(index, size);
  }

  /**
   * Obtains an instance based on a page and paging size.
   * <p>
   * This implements paging on top of the basic first-item/size data model.
   * 
   * @param page  the page number, one or greater
   * @param pagingSize  the paging size, zero or greater
   * @return the paging request, not null
   * @throws IllegalArgumentException if either input is invalid
   */
  public static PagingRequest ofPage(int page, int pagingSize) {
    ArgumentChecker.notNegativeOrZero(page, "page");
    ArgumentChecker.notNegative(pagingSize, "pagingSize");
    int index = ((page - 1) * pagingSize);
    return new PagingRequest(index, pagingSize);
  }

  /**
   * Obtains an instance based on a page and paging size, applying default values.
   * <p>
   * This implements paging on top of the basic first-item/size data model.
   * The page will default to 1 if the input is 0.
   * The paging size will default to 20 if the input is 0.
   * 
   * @param page  the page number, page one chosen if zero, not negative
   * @param pagingSize  the paging size, size twenty chosen if zero, not negative
   * @return the paging request, not null
   * @throws IllegalArgumentException if either input is negative
   */
  public static PagingRequest ofPageDefaulted(int page, int pagingSize) {
    page = (page == 0 ? 1 : page);
    pagingSize = (pagingSize == 0 ? DEFAULT_PAGING_SIZE : pagingSize);
    return PagingRequest.ofPage(page, pagingSize);
  }

  /**
   * Creates an instance without using defaults.
   * <p>
   * A paging size of zero will only return the count of items and will
   * always have a first item index of zero.
   * 
   * @param index  the zero-based start index, zero or greater
   * @param size  the number of items to request, zero or greater
   * @throws IllegalArgumentException if either input is invalid
   */
  @ImmutableConstructor
  private PagingRequest(final int index, final int size) {
    ArgumentChecker.notNegative(index, "index");
    ArgumentChecker.notNegative(size, "size");
    _firstItem = (size != 0 ? index : 0);
    _pagingSize = size;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the first item, using a zero-based index.
   * <p>
   * In SQL this corresponds to OFFSET.
   * 
   * @return the first item index, zero-based
   */
  public int getFirstItem() {
    return _firstItem;
  }

  /**
   * Gets the requested number of items.
   * <p>
   * In SQL this corresponds to FETCH/LIMIT.
   * 
   * @return the number of requested items, zero or greater
   */
  public int getPagingSize() {
    return _pagingSize;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the first item, using a one-based index.
   * 
   * @return the first item number, one-based
   */
  public int getFirstItemOneBased() {
    return _firstItem + 1;
  }

  /**
   * Gets the last item exclusive, using a zero-based index.
   * 
   * @return the last item index, exclusive, zero-based
   */
  public int getLastItem() {
    return _firstItem + _pagingSize;
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
   * Selects the elements from the list matching this request.
   * <p>
   * This will return a new list consisting of the selected elements from the supplied list.
   * The elements are selected based on {@link #getFirstItem()} and {@link #getLastItem()}.
   * 
   * @param <T> the list type
   * @param list  the collection to select from, not null
   * @return the selected list, not linked to the original, not null
   */
  public <T> List<T> select(List<T> list) {
    int firstIndex = getFirstItem();
    int lastIndex = getLastItem();
    if (firstIndex > list.size()) {
      firstIndex = list.size();
    }
    if (lastIndex > list.size()) {
      lastIndex = list.size();
    }
    return new ArrayList<T>(list.subList(firstIndex, lastIndex));
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[first=" + _firstItem + ", size=" + _pagingSize + "]";
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code PagingRequest}.
   * @return the meta-bean, not null
   */
  public static PagingRequest.Meta meta() {
    return PagingRequest.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(PagingRequest.Meta.INSTANCE);
  }

  @Override
  public PagingRequest.Meta metaBean() {
    return PagingRequest.Meta.INSTANCE;
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
      PagingRequest other = (PagingRequest) obj;
      return (getFirstItem() == other.getFirstItem()) &&
          (getPagingSize() == other.getPagingSize());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getFirstItem());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPagingSize());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code PagingRequest}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code firstItem} property.
     */
    private final MetaProperty<Integer> _firstItem = DirectMetaProperty.ofImmutable(
        this, "firstItem", PagingRequest.class, Integer.TYPE);
    /**
     * The meta-property for the {@code pagingSize} property.
     */
    private final MetaProperty<Integer> _pagingSize = DirectMetaProperty.ofImmutable(
        this, "pagingSize", PagingRequest.class, Integer.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "firstItem",
        "pagingSize");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 132704739:  // firstItem
          return _firstItem;
        case 1302250925:  // pagingSize
          return _pagingSize;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends PagingRequest> builder() {
      return new PagingRequest.Builder();
    }

    @Override
    public Class<? extends PagingRequest> beanType() {
      return PagingRequest.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code firstItem} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Integer> firstItem() {
      return _firstItem;
    }

    /**
     * The meta-property for the {@code pagingSize} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Integer> pagingSize() {
      return _pagingSize;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 132704739:  // firstItem
          return ((PagingRequest) bean).getFirstItem();
        case 1302250925:  // pagingSize
          return ((PagingRequest) bean).getPagingSize();
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
   * The bean-builder for {@code PagingRequest}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<PagingRequest> {

    private int _firstItem;
    private int _pagingSize;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 132704739:  // firstItem
          return _firstItem;
        case 1302250925:  // pagingSize
          return _pagingSize;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 132704739:  // firstItem
          this._firstItem = (Integer) newValue;
          break;
        case 1302250925:  // pagingSize
          this._pagingSize = (Integer) newValue;
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
      super.setString(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public PagingRequest build() {
      return new PagingRequest(
          _firstItem,
          _pagingSize);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("PagingRequest.Builder{");
      buf.append("firstItem").append('=').append(JodaBeanUtils.toString(_firstItem)).append(',').append(' ');
      buf.append("pagingSize").append('=').append(JodaBeanUtils.toString(_pagingSize));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
