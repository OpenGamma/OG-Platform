/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.web;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
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
import org.springframework.web.context.ServletContextAware;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.web.FreemarkerOutputter;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.cache.WebappTemplateLoader;
import freemarker.template.Configuration;

/**
 * Component factory for initializing Freemarker.
 */
@BeanDefinition
public class FreemarkerConfigurationComponentFactory extends AbstractComponentFactory {

  /**
   * Prefix used for a file relative to the servlet context.
   */
  public static final String SERVLET_CONTEXT = "servlet-context";
  /**
   * Prefix used for a simple file location.
   */
  public static final String FILE = "file";

  /**
   * The locations of the templates.
   */
  @PropertyDefinition(validate = "notEmpty")
  private String _templateLocations;

  @Override
  public void init(ComponentRepository repo, final LinkedHashMap<String, String> configuration) throws Exception {
    String[] locations = _templateLocations.split(",");
    repo.registerServletContextAware(new FreemarkerInitializer(locations));
  }

  //-------------------------------------------------------------------------
  static final class FreemarkerInitializer implements ServletContextAware {
    private final String[] _locations;
    FreemarkerInitializer(String[] locations) {
      _locations = locations;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
      Configuration cfg = FreemarkerOutputter.createConfiguration();
      cfg.setTemplateLoader(new MultiTemplateLoader(createLoaders(_locations, servletContext)));
      FreemarkerOutputter.init(servletContext, cfg);
    }
  }

  static TemplateLoader[] createLoaders(String[] locations, ServletContext servletContext) {
    Collection<TemplateLoader> templateLoaders = new ArrayList<TemplateLoader>();
    for (String location : locations) {
      String[] prefixAndBase = StringUtils.split(location, ":", 2);
      if (prefixAndBase.length != 2) {
        throw new OpenGammaRuntimeException("Invalid Freemarker template location: " + location);
      }
      String prefix = prefixAndBase[0].trim();
      String base = prefixAndBase[1].trim();
      if (SERVLET_CONTEXT.equals(prefix)) {
        templateLoaders.add(new WebappTemplateLoader(servletContext, base));
      } else if (FILE.equals(prefix)) {
        try {
          templateLoaders.add(new FileTemplateLoader(new File(base)));
        } catch (IOException e) {
          throw new OpenGammaRuntimeException("Unable to load Freemarker templates from " + base, e);
        }
      } else {
        throw new OpenGammaRuntimeException("Invalid Freemarker template location: " + location);
      }
    }
    return templateLoaders.toArray(new TemplateLoader[templateLoaders.size()]);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FreemarkerConfigurationComponentFactory}.
   * @return the meta-bean, not null
   */
  public static FreemarkerConfigurationComponentFactory.Meta meta() {
    return FreemarkerConfigurationComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FreemarkerConfigurationComponentFactory.Meta.INSTANCE);
  }

  @Override
  public FreemarkerConfigurationComponentFactory.Meta metaBean() {
    return FreemarkerConfigurationComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the locations of the templates.
   * @return the value of the property, not empty
   */
  public String getTemplateLocations() {
    return _templateLocations;
  }

  /**
   * Sets the locations of the templates.
   * @param templateLocations  the new value of the property, not empty
   */
  public void setTemplateLocations(String templateLocations) {
    JodaBeanUtils.notEmpty(templateLocations, "templateLocations");
    this._templateLocations = templateLocations;
  }

  /**
   * Gets the the {@code templateLocations} property.
   * @return the property, not null
   */
  public final Property<String> templateLocations() {
    return metaBean().templateLocations().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public FreemarkerConfigurationComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      FreemarkerConfigurationComponentFactory other = (FreemarkerConfigurationComponentFactory) obj;
      return JodaBeanUtils.equal(getTemplateLocations(), other.getTemplateLocations()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getTemplateLocations());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("FreemarkerConfigurationComponentFactory{");
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
    buf.append("templateLocations").append('=').append(JodaBeanUtils.toString(getTemplateLocations())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FreemarkerConfigurationComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code templateLocations} property.
     */
    private final MetaProperty<String> _templateLocations = DirectMetaProperty.ofReadWrite(
        this, "templateLocations", FreemarkerConfigurationComponentFactory.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "templateLocations");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 826348548:  // templateLocations
          return _templateLocations;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends FreemarkerConfigurationComponentFactory> builder() {
      return new DirectBeanBuilder<FreemarkerConfigurationComponentFactory>(new FreemarkerConfigurationComponentFactory());
    }

    @Override
    public Class<? extends FreemarkerConfigurationComponentFactory> beanType() {
      return FreemarkerConfigurationComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code templateLocations} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> templateLocations() {
      return _templateLocations;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 826348548:  // templateLocations
          return ((FreemarkerConfigurationComponentFactory) bean).getTemplateLocations();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 826348548:  // templateLocations
          ((FreemarkerConfigurationComponentFactory) bean).setTemplateLocations((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notEmpty(((FreemarkerConfigurationComponentFactory) bean)._templateLocations, "templateLocations");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
