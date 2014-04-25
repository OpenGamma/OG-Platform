/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.strips;

import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.core.link.ConventionLink;
import com.opengamma.financial.convention.ISDASwapNodeConvention;

/**
 * ISDA swap node.
 */
@BeanDefinition
public class ISDASwapNode extends ISDAYieldCurveNode {

  private static final long serialVersionUID = 1L;
  
  /**
   * Convention for this swap node.
   */
  @PropertyDefinition(validate = "notNull")
  private ConventionLink<ISDASwapNodeConvention> _conventionLink;
  
  @Override
  public <T> T accept(CurveNodeVisitor<T> visitor) {
    return visitor.visitISDASwapNode(this);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ISDASwapNode}.
   * @return the meta-bean, not null
   */
  public static ISDASwapNode.Meta meta() {
    return ISDASwapNode.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ISDASwapNode.Meta.INSTANCE);
  }

  @Override
  public ISDASwapNode.Meta metaBean() {
    return ISDASwapNode.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets convention for this swap node.
   * @return the value of the property, not null
   */
  public ConventionLink<ISDASwapNodeConvention> getConventionLink() {
    return _conventionLink;
  }

  /**
   * Sets convention for this swap node.
   * @param conventionLink  the new value of the property, not null
   */
  public void setConventionLink(ConventionLink<ISDASwapNodeConvention> conventionLink) {
    JodaBeanUtils.notNull(conventionLink, "conventionLink");
    this._conventionLink = conventionLink;
  }

  /**
   * Gets the the {@code conventionLink} property.
   * @return the property, not null
   */
  public final Property<ConventionLink<ISDASwapNodeConvention>> conventionLink() {
    return metaBean().conventionLink().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ISDASwapNode clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ISDASwapNode other = (ISDASwapNode) obj;
      return JodaBeanUtils.equal(getConventionLink(), other.getConventionLink()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getConventionLink());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("ISDASwapNode{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  @Override
  protected void toString(StringBuilder buf) {
    super.toString(buf);
    buf.append("conventionLink").append('=').append(JodaBeanUtils.toString(getConventionLink())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ISDASwapNode}.
   */
  public static class Meta extends ISDAYieldCurveNode.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code conventionLink} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<ConventionLink<ISDASwapNodeConvention>> _conventionLink = DirectMetaProperty.ofReadWrite(
        this, "conventionLink", ISDASwapNode.class, (Class) ConventionLink.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "conventionLink");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1372086027:  // conventionLink
          return _conventionLink;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ISDASwapNode> builder() {
      return new DirectBeanBuilder<ISDASwapNode>(new ISDASwapNode());
    }

    @Override
    public Class<? extends ISDASwapNode> beanType() {
      return ISDASwapNode.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code conventionLink} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConventionLink<ISDASwapNodeConvention>> conventionLink() {
      return _conventionLink;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1372086027:  // conventionLink
          return ((ISDASwapNode) bean).getConventionLink();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1372086027:  // conventionLink
          ((ISDASwapNode) bean).setConventionLink((ConventionLink<ISDASwapNodeConvention>) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((ISDASwapNode) bean)._conventionLink, "conventionLink");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
