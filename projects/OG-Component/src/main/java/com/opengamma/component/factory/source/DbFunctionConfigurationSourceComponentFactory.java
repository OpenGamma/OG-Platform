/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.component.factory.ComponentInfoAttributes;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationBundle;
import com.opengamma.engine.function.config.FunctionConfigurationDefinitionAggregator;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.function.config.StaticFunctionConfiguration;
import com.opengamma.financial.function.rest.DataRepositoryConfigurationSourceResource;
import com.opengamma.financial.function.rest.RemoteFunctionConfigurationSource;

/**
 * Component factory providing the {@code FunctionConfigurationSource} read from a {@code ConfigMaster}.
 */
@BeanDefinition
public class DbFunctionConfigurationSourceComponentFactory extends AbstractComponentFactory {

  /**
   * The classifier that the factory should publish under.
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;
  /**
   * The flag determining whether the component should be published by REST (default true).
   */
  @PropertyDefinition
  private boolean _publishRest = true;
  /**
   * The config source.
   */
  @PropertyDefinition(validate = "notNull")
  private ConfigSource _configSource;
  /**
   * The function configuration definition name.
   */
  @PropertyDefinition(validate = "notNull")
  private String _functionDefinitionName;

  //-------------------------------------------------------------------------
  @Override
  public void init(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) {
    
    final FunctionConfigurationDefinitionAggregator definitionAggregator = new FunctionConfigurationDefinitionAggregator(_configSource);
    final FunctionConfigurationSource source = definitionAggregator.aggregate(_functionDefinitionName);
    
    final ComponentInfo info = new ComponentInfo(FunctionConfigurationSource.class, getClassifier());
    info.addAttribute(ComponentInfoAttributes.LEVEL, 1);
    info.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteFunctionConfigurationSource.class);
    repo.registerComponent(info, source);

    if (isPublishRest()) {
      repo.getRestComponents().publish(info, new DataRepositoryConfigurationSourceResource(source));
    }
  }

  /**
   * Debug utility to sort a repository. This allows two to be compared more easily.
   *
   * @param source the raw repository configuration source
   * @return a source that return a sorted list of functions
   */
  protected FunctionConfigurationSource sorted(final FunctionConfigurationSource source) {
    return new FunctionConfigurationSource() {

      @Override
      public FunctionConfigurationBundle getFunctionConfiguration() {
        final List<FunctionConfiguration> functions = new ArrayList<FunctionConfiguration>(source.getFunctionConfiguration().getFunctions());
        Collections.sort(functions, new Comparator<FunctionConfiguration>() {

          @Override
          public int compare(final FunctionConfiguration o1, final FunctionConfiguration o2) {
            if (o1 instanceof ParameterizedFunctionConfiguration) {
              if (o2 instanceof ParameterizedFunctionConfiguration) {
                final ParameterizedFunctionConfiguration p1 = (ParameterizedFunctionConfiguration) o1;
                final ParameterizedFunctionConfiguration p2 = (ParameterizedFunctionConfiguration) o2;
                // Order by class name
                int c = p1.getDefinitionClassName().compareTo(p2.getDefinitionClassName());
                if (c != 0) {
                  return c;
                }
                // Order by parameter lengths
                c = p1.getParameter().size() - p2.getParameter().size();
                if (c != 0) {
                  return c;
                }
                // Order by parameters
                for (int i = 0; i < p1.getParameter().size(); i++) {
                  c = p1.getParameter().get(i).compareTo(p2.getParameter().get(i));
                  if (c != 0) {
                    return c;
                  }
                }
                // Equal? Put a breakpoint here; we don't really want this to be happening.
                //assert false;
                return 0;
              } else if (o2 instanceof StaticFunctionConfiguration) {
                // Static goes first
                return 1;
              }
            } else if (o1 instanceof StaticFunctionConfiguration) {
              if (o2 instanceof ParameterizedFunctionConfiguration) {
                // Static goes first
                return -1;
              } else if (o2 instanceof StaticFunctionConfiguration) {
                // Sort by class name
                return ((StaticFunctionConfiguration) o1).getDefinitionClassName().compareTo(((StaticFunctionConfiguration) o2).getDefinitionClassName());
              }
            }
            throw new UnsupportedOperationException("Can't compare " + o1.getClass() + " and " + o2.getClass());
          }

        });
        return new FunctionConfigurationBundle(functions);
      }

    };
  }


  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code DbFunctionConfigurationSourceComponentFactory}.
   * @return the meta-bean, not null
   */
  public static DbFunctionConfigurationSourceComponentFactory.Meta meta() {
    return DbFunctionConfigurationSourceComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(DbFunctionConfigurationSourceComponentFactory.Meta.INSTANCE);
  }

  @Override
  public DbFunctionConfigurationSourceComponentFactory.Meta metaBean() {
    return DbFunctionConfigurationSourceComponentFactory.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        return getClassifier();
      case -614707837:  // publishRest
        return isPublishRest();
      case 195157501:  // configSource
        return getConfigSource();
      case -1999640458:  // functionDefinitionName
        return getFunctionDefinitionName();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        setClassifier((String) newValue);
        return;
      case -614707837:  // publishRest
        setPublishRest((Boolean) newValue);
        return;
      case 195157501:  // configSource
        setConfigSource((ConfigSource) newValue);
        return;
      case -1999640458:  // functionDefinitionName
        setFunctionDefinitionName((String) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_classifier, "classifier");
    JodaBeanUtils.notNull(_configSource, "configSource");
    JodaBeanUtils.notNull(_functionDefinitionName, "functionDefinitionName");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      DbFunctionConfigurationSourceComponentFactory other = (DbFunctionConfigurationSourceComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          JodaBeanUtils.equal(isPublishRest(), other.isPublishRest()) &&
          JodaBeanUtils.equal(getConfigSource(), other.getConfigSource()) &&
          JodaBeanUtils.equal(getFunctionDefinitionName(), other.getFunctionDefinitionName()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash += hash * 31 + JodaBeanUtils.hashCode(isPublishRest());
    hash += hash * 31 + JodaBeanUtils.hashCode(getConfigSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFunctionDefinitionName());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier that the factory should publish under.
   * @return the value of the property, not null
   */
  public String getClassifier() {
    return _classifier;
  }

  /**
   * Sets the classifier that the factory should publish under.
   * @param classifier  the new value of the property, not null
   */
  public void setClassifier(String classifier) {
    JodaBeanUtils.notNull(classifier, "classifier");
    this._classifier = classifier;
  }

  /**
   * Gets the the {@code classifier} property.
   * @return the property, not null
   */
  public final Property<String> classifier() {
    return metaBean().classifier().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the flag determining whether the component should be published by REST (default true).
   * @return the value of the property
   */
  public boolean isPublishRest() {
    return _publishRest;
  }

  /**
   * Sets the flag determining whether the component should be published by REST (default true).
   * @param publishRest  the new value of the property
   */
  public void setPublishRest(boolean publishRest) {
    this._publishRest = publishRest;
  }

  /**
   * Gets the the {@code publishRest} property.
   * @return the property, not null
   */
  public final Property<Boolean> publishRest() {
    return metaBean().publishRest().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the config source.
   * @return the value of the property, not null
   */
  public ConfigSource getConfigSource() {
    return _configSource;
  }

  /**
   * Sets the config source.
   * @param configSource  the new value of the property, not null
   */
  public void setConfigSource(ConfigSource configSource) {
    JodaBeanUtils.notNull(configSource, "configSource");
    this._configSource = configSource;
  }

  /**
   * Gets the the {@code configSource} property.
   * @return the property, not null
   */
  public final Property<ConfigSource> configSource() {
    return metaBean().configSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the function configuration definition name.
   * @return the value of the property, not null
   */
  public String getFunctionDefinitionName() {
    return _functionDefinitionName;
  }

  /**
   * Sets the function configuration definition name.
   * @param functionDefinitionName  the new value of the property, not null
   */
  public void setFunctionDefinitionName(String functionDefinitionName) {
    JodaBeanUtils.notNull(functionDefinitionName, "functionDefinitionName");
    this._functionDefinitionName = functionDefinitionName;
  }

  /**
   * Gets the the {@code functionDefinitionName} property.
   * @return the property, not null
   */
  public final Property<String> functionDefinitionName() {
    return metaBean().functionDefinitionName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code DbFunctionConfigurationSourceComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code classifier} property.
     */
    private final MetaProperty<String> _classifier = DirectMetaProperty.ofReadWrite(
        this, "classifier", DbFunctionConfigurationSourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code publishRest} property.
     */
    private final MetaProperty<Boolean> _publishRest = DirectMetaProperty.ofReadWrite(
        this, "publishRest", DbFunctionConfigurationSourceComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code configSource} property.
     */
    private final MetaProperty<ConfigSource> _configSource = DirectMetaProperty.ofReadWrite(
        this, "configSource", DbFunctionConfigurationSourceComponentFactory.class, ConfigSource.class);
    /**
     * The meta-property for the {@code functionDefinitionName} property.
     */
    private final MetaProperty<String> _functionDefinitionName = DirectMetaProperty.ofReadWrite(
        this, "functionDefinitionName", DbFunctionConfigurationSourceComponentFactory.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "publishRest",
        "configSource",
        "functionDefinitionName");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return _classifier;
        case -614707837:  // publishRest
          return _publishRest;
        case 195157501:  // configSource
          return _configSource;
        case -1999640458:  // functionDefinitionName
          return _functionDefinitionName;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends DbFunctionConfigurationSourceComponentFactory> builder() {
      return new DirectBeanBuilder<DbFunctionConfigurationSourceComponentFactory>(new DbFunctionConfigurationSourceComponentFactory());
    }

    @Override
    public Class<? extends DbFunctionConfigurationSourceComponentFactory> beanType() {
      return DbFunctionConfigurationSourceComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code classifier} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> classifier() {
      return _classifier;
    }

    /**
     * The meta-property for the {@code publishRest} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> publishRest() {
      return _publishRest;
    }

    /**
     * The meta-property for the {@code configSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConfigSource> configSource() {
      return _configSource;
    }

    /**
     * The meta-property for the {@code functionDefinitionName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> functionDefinitionName() {
      return _functionDefinitionName;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
