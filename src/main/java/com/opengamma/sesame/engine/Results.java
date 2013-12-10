/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import java.util.ArrayList;
import java.util.List;
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

import com.google.common.collect.ImmutableList;
import com.opengamma.util.ArgumentChecker;

// TODO is it worth including a lookup by ID instead of row index?
// TODO is it worth including a lookup by column name as well as column index?
// TODO Iterable<Row>?
// TODO column types
@BeanDefinition
public final class Results implements ImmutableBean {

  @PropertyDefinition(validate = "notNull")
  private final List<String> _columnNames;

  @PropertyDefinition(validate = "notNull")
  private final List<ResultRow> _rows;

  @ImmutableConstructor
  /* package */ Results(List<String> columnNames, List<ResultRow> rows) {
    _rows = ImmutableList.copyOf(ArgumentChecker.notNull(rows, "rows"));
    _columnNames = ImmutableList.copyOf(ArgumentChecker.notNull(columnNames, "columnNames"));
  }

  public ResultRow get(int rowIndex) {
    checkRowIndex(rowIndex);
    return _rows.get(rowIndex);
  }

  private void checkRowIndex(int rowIndex) {
    if (rowIndex < 0 || rowIndex >= _rows.size()) {
      throw new IndexOutOfBoundsException("Index " + rowIndex + " is out of bounds. row count = " + _rows.size());
    }
  }

  public ResultItem get(int rowIndex, int columnIndex) {
    checkRowIndex(rowIndex);
    if (columnIndex < 0 || columnIndex >= _columnNames.size()) {
      throw new IndexOutOfBoundsException("Index " + columnIndex + " is out of bounds. column count = " + _columnNames.size());
    }
    return _rows.get(rowIndex).get(columnIndex);
  }

  @Override
  public String toString() {
    return "Results [_columnNames=" + _columnNames + ", _rows=" + _rows + "]";
  }

  /* package */ static ResultBuilder builder(List<?> inputs, List<String> columnNames) {
    return new ResultBuilder(inputs, columnNames);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code Results}.
   * @return the meta-bean, not null
   */
  public static Results.Meta meta() {
    return Results.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(Results.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static Results.Builder builder() {
    return new Results.Builder();
  }

  @Override
  public Results.Meta metaBean() {
    return Results.Meta.INSTANCE;
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
  /**
   * Gets the columnNames.
   * @return the value of the property, not null
   */
  public List<String> getColumnNames() {
    return _columnNames;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the rows.
   * @return the value of the property, not null
   */
  public List<ResultRow> getRows() {
    return _rows;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public Results clone() {
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      Results other = (Results) obj;
      return JodaBeanUtils.equal(getColumnNames(), other.getColumnNames()) &&
          JodaBeanUtils.equal(getRows(), other.getRows());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getColumnNames());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRows());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code Results}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code columnNames} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<String>> _columnNames = DirectMetaProperty.ofImmutable(
        this, "columnNames", Results.class, (Class) List.class);
    /**
     * The meta-property for the {@code rows} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ResultRow>> _rows = DirectMetaProperty.ofImmutable(
        this, "rows", Results.class, (Class) List.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "columnNames",
        "rows");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -851002990:  // columnNames
          return _columnNames;
        case 3506649:  // rows
          return _rows;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public Results.Builder builder() {
      return new Results.Builder();
    }

    @Override
    public Class<? extends Results> beanType() {
      return Results.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code columnNames} property.
     * @return the meta-property, not null
     */
    public MetaProperty<List<String>> columnNames() {
      return _columnNames;
    }

    /**
     * The meta-property for the {@code rows} property.
     * @return the meta-property, not null
     */
    public MetaProperty<List<ResultRow>> rows() {
      return _rows;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -851002990:  // columnNames
          return ((Results) bean).getColumnNames();
        case 3506649:  // rows
          return ((Results) bean).getRows();
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
   * The bean-builder for {@code Results}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<Results> {

    private List<String> _columnNames = new ArrayList<String>();
    private List<ResultRow> _rows = new ArrayList<ResultRow>();

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(Results beanToCopy) {
      this._columnNames = new ArrayList<String>(beanToCopy.getColumnNames());
      this._rows = new ArrayList<ResultRow>(beanToCopy.getRows());
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -851002990:  // columnNames
          this._columnNames = (List<String>) newValue;
          break;
        case 3506649:  // rows
          this._rows = (List<ResultRow>) newValue;
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
    public Results build() {
      return new Results(
          _columnNames,
          _rows);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code columnNames} property in the builder.
     * @param columnNames  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder columnNames(List<String> columnNames) {
      JodaBeanUtils.notNull(columnNames, "columnNames");
      this._columnNames = columnNames;
      return this;
    }

    /**
     * Sets the {@code rows} property in the builder.
     * @param rows  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder rows(List<ResultRow> rows) {
      JodaBeanUtils.notNull(rows, "rows");
      this._rows = rows;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("Results.Builder{");
      buf.append("columnNames").append('=').append(JodaBeanUtils.toString(_columnNames)).append(',').append(' ');
      buf.append("rows").append('=').append(JodaBeanUtils.toString(_rows));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
