/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.id.UniqueId;

/**
 * Represents a view process attachment request
 */
@BeanDefinition
public class AttachToViewProcessRequest extends DirectBean {

  /**
   * The view definition name
   */
  @PropertyDefinition
  private UniqueId _viewDefinitionId;
  
  /**
   * The view execution options
   */
  @PropertyDefinition
  private ViewExecutionOptions _executionOptions;
  
  /**
   * Indicates whether the request should generate a new batch process, or whether an existing shared process may be
   * re-used.
   */
  @PropertyDefinition
  private boolean _newBatchProcess;
  
  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code AttachToViewProcessRequest}.
   * @return the meta-bean, not null
   */
  public static AttachToViewProcessRequest.Meta meta() {
    return AttachToViewProcessRequest.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(AttachToViewProcessRequest.Meta.INSTANCE);
  }

  @Override
  public AttachToViewProcessRequest.Meta metaBean() {
    return AttachToViewProcessRequest.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the view definition name
   * @return the value of the property
   */
  public UniqueId getViewDefinitionId() {
    return _viewDefinitionId;
  }

  /**
   * Sets the view definition name
   * @param viewDefinitionId  the new value of the property
   */
  public void setViewDefinitionId(UniqueId viewDefinitionId) {
    this._viewDefinitionId = viewDefinitionId;
  }

  /**
   * Gets the the {@code viewDefinitionId} property.
   * @return the property, not null
   */
  public final Property<UniqueId> viewDefinitionId() {
    return metaBean().viewDefinitionId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the view execution options
   * @return the value of the property
   */
  public ViewExecutionOptions getExecutionOptions() {
    return _executionOptions;
  }

  /**
   * Sets the view execution options
   * @param executionOptions  the new value of the property
   */
  public void setExecutionOptions(ViewExecutionOptions executionOptions) {
    this._executionOptions = executionOptions;
  }

  /**
   * Gets the the {@code executionOptions} property.
   * @return the property, not null
   */
  public final Property<ViewExecutionOptions> executionOptions() {
    return metaBean().executionOptions().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets indicates whether the request should generate a new batch process, or whether an existing shared process may be
   * re-used.
   * @return the value of the property
   */
  public boolean isNewBatchProcess() {
    return _newBatchProcess;
  }

  /**
   * Sets indicates whether the request should generate a new batch process, or whether an existing shared process may be
   * re-used.
   * @param newBatchProcess  the new value of the property
   */
  public void setNewBatchProcess(boolean newBatchProcess) {
    this._newBatchProcess = newBatchProcess;
  }

  /**
   * Gets the the {@code newBatchProcess} property.
   * re-used.
   * @return the property, not null
   */
  public final Property<Boolean> newBatchProcess() {
    return metaBean().newBatchProcess().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public AttachToViewProcessRequest clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      AttachToViewProcessRequest other = (AttachToViewProcessRequest) obj;
      return JodaBeanUtils.equal(getViewDefinitionId(), other.getViewDefinitionId()) &&
          JodaBeanUtils.equal(getExecutionOptions(), other.getExecutionOptions()) &&
          (isNewBatchProcess() == other.isNewBatchProcess());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getViewDefinitionId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getExecutionOptions());
    hash += hash * 31 + JodaBeanUtils.hashCode(isNewBatchProcess());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("AttachToViewProcessRequest{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("viewDefinitionId").append('=').append(JodaBeanUtils.toString(getViewDefinitionId())).append(',').append(' ');
    buf.append("executionOptions").append('=').append(JodaBeanUtils.toString(getExecutionOptions())).append(',').append(' ');
    buf.append("newBatchProcess").append('=').append(JodaBeanUtils.toString(isNewBatchProcess())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code AttachToViewProcessRequest}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code viewDefinitionId} property.
     */
    private final MetaProperty<UniqueId> _viewDefinitionId = DirectMetaProperty.ofReadWrite(
        this, "viewDefinitionId", AttachToViewProcessRequest.class, UniqueId.class);
    /**
     * The meta-property for the {@code executionOptions} property.
     */
    private final MetaProperty<ViewExecutionOptions> _executionOptions = DirectMetaProperty.ofReadWrite(
        this, "executionOptions", AttachToViewProcessRequest.class, ViewExecutionOptions.class);
    /**
     * The meta-property for the {@code newBatchProcess} property.
     */
    private final MetaProperty<Boolean> _newBatchProcess = DirectMetaProperty.ofReadWrite(
        this, "newBatchProcess", AttachToViewProcessRequest.class, Boolean.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "viewDefinitionId",
        "executionOptions",
        "newBatchProcess");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -545262317:  // viewDefinitionId
          return _viewDefinitionId;
        case -1448089498:  // executionOptions
          return _executionOptions;
        case -1977436267:  // newBatchProcess
          return _newBatchProcess;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends AttachToViewProcessRequest> builder() {
      return new DirectBeanBuilder<AttachToViewProcessRequest>(new AttachToViewProcessRequest());
    }

    @Override
    public Class<? extends AttachToViewProcessRequest> beanType() {
      return AttachToViewProcessRequest.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code viewDefinitionId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueId> viewDefinitionId() {
      return _viewDefinitionId;
    }

    /**
     * The meta-property for the {@code executionOptions} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ViewExecutionOptions> executionOptions() {
      return _executionOptions;
    }

    /**
     * The meta-property for the {@code newBatchProcess} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> newBatchProcess() {
      return _newBatchProcess;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -545262317:  // viewDefinitionId
          return ((AttachToViewProcessRequest) bean).getViewDefinitionId();
        case -1448089498:  // executionOptions
          return ((AttachToViewProcessRequest) bean).getExecutionOptions();
        case -1977436267:  // newBatchProcess
          return ((AttachToViewProcessRequest) bean).isNewBatchProcess();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -545262317:  // viewDefinitionId
          ((AttachToViewProcessRequest) bean).setViewDefinitionId((UniqueId) newValue);
          return;
        case -1448089498:  // executionOptions
          ((AttachToViewProcessRequest) bean).setExecutionOptions((ViewExecutionOptions) newValue);
          return;
        case -1977436267:  // newBatchProcess
          ((AttachToViewProcessRequest) bean).setNewBatchProcess((Boolean) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
