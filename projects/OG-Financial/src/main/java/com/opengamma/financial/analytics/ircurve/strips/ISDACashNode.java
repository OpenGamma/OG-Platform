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
import com.opengamma.financial.convention.ISDACashNodeConvention;

/**
 * 
 */
@BeanDefinition
public class ISDACashNode extends ISDAYieldCurveNode {

  private static final long serialVersionUID = 1L;
  
  @PropertyDefinition(validate = "notNull")
  private ConventionLink<ISDACashNodeConvention> _conventionLink;
  
  
  @Override
  public <T> T accept(CurveNodeVisitor<T> visitor) {
    return visitor.visitISDACashNode(this);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ISDACashNode}.
   * @return the meta-bean, not null
   */
  public static ISDACashNode.Meta meta() {
    return ISDACashNode.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ISDACashNode.Meta.INSTANCE);
  }

  @Override
  public ISDACashNode.Meta metaBean() {
    return ISDACashNode.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the conventionLink.
   * @return the value of the property, not null
   */
  public ConventionLink<ISDACashNodeConvention> getConventionLink() {
    return _conventionLink;
  }

  /**
   * Sets the conventionLink.
   * @param conventionLink  the new value of the property, not null
   */
  public void setConventionLink(ConventionLink<ISDACashNodeConvention> conventionLink) {
    JodaBeanUtils.notNull(conventionLink, "conventionLink");
    this._conventionLink = conventionLink;
  }

  /**
   * Gets the the {@code conventionLink} property.
   * @return the property, not null
   */
  public final Property<ConventionLink<ISDACashNodeConvention>> conventionLink() {
    return metaBean().conventionLink().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ISDACashNode clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ISDACashNode other = (ISDACashNode) obj;
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
    buf.append("ISDACashNode{");
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
   * The meta-bean for {@code ISDACashNode}.
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
    private final MetaProperty<ConventionLink<ISDACashNodeConvention>> _conventionLink = DirectMetaProperty.ofReadWrite(
        this, "conventionLink", ISDACashNode.class, (Class) ConventionLink.class);
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
    public BeanBuilder<? extends ISDACashNode> builder() {
      return new DirectBeanBuilder<ISDACashNode>(new ISDACashNode());
    }

    @Override
    public Class<? extends ISDACashNode> beanType() {
      return ISDACashNode.class;
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
    public final MetaProperty<ConventionLink<ISDACashNodeConvention>> conventionLink() {
      return _conventionLink;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1372086027:  // conventionLink
          return ((ISDACashNode) bean).getConventionLink();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1372086027:  // conventionLink
          ((ISDACashNode) bean).setConventionLink((ConventionLink<ISDACashNodeConvention>) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((ISDACashNode) bean)._conventionLink, "conventionLink");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
