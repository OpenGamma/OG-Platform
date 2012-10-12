/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.component.factory.tool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.management.DbManagement;
import com.opengamma.util.db.script.DbScriptDirectory;
import com.opengamma.util.db.script.DbScriptReader;
import com.opengamma.util.db.script.FileDbScriptDirectory;
import com.opengamma.util.db.script.ZipFileDbScriptDirectory;
import com.opengamma.util.db.tool.DbToolContext;

/**
 * Component factory for setting up a database tool context.
 */
@BeanDefinition
public class DbToolContextComponentFactory extends AbstractComponentFactory {

  /**
   * The classifier under which to publish.
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;
  /**
   * The database connector.
   */
  @PropertyDefinition
  private DbConnector _dbConnector;
  /**
   * The JDBC URL.
   */
  @PropertyDefinition
  private String _jdbcUrl;
  /**
   * The database management instance.
   */
  @PropertyDefinition
  private DbManagement _dbManagement;
  /**
   * A comma-separated list of database object groups on which to operate.
   */
  @PropertyDefinition
  private String _groupsList;
  /**
   * A resource pointing to the root of the database installation scripts.
   */
  @PropertyDefinition
  private Resource _scriptsResource;
  
  //-------------------------------------------------------------------------
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    DbToolContext dbToolContext = new DbToolContext();
    Map<String, MetaProperty<?>> mapTarget = new HashMap<String, MetaProperty<?>>(dbToolContext.metaBean().metaPropertyMap());
    mapTarget.keySet().retainAll(this.metaBean().metaPropertyMap().keySet());
    for (MetaProperty<?> mp : mapTarget.values()) {
      mp.set(dbToolContext, property(mp.name()).get());
    }
    if (getJdbcUrl() != null) {
      // REVIEW jonathan 2012-10-12 -- workaround for PLAT-2745
      int lastSlashIdx = getJdbcUrl().lastIndexOf("/");
      if (lastSlashIdx == -1) {
        throw new OpenGammaRuntimeException("JDBC URL must contain '/' before the database name");
      }
      dbToolContext.setCatalog(getJdbcUrl().substring(lastSlashIdx + 1));
    }
    if (getScriptsResource() != null) {
      URL scriptsResourceUrl;
      try {
        scriptsResourceUrl = getScriptsResource().getURL();
      } catch (IOException e) {
        throw new OpenGammaRuntimeException("Unable to get scripts resource as URL", e);
      }
      DbScriptDirectory dbScriptDirectory;
      if (ResourceUtils.isFileURL(scriptsResourceUrl)) {
        try {
          dbScriptDirectory = new FileDbScriptDirectory(ResourceUtils.getFile(scriptsResourceUrl));
        } catch (FileNotFoundException e) {
          throw new OpenGammaRuntimeException("Error resolving scripts resource to file from URL " + scriptsResourceUrl);
        }
      } else if (ResourceUtils.isJarURL(scriptsResourceUrl)) {
        URL jarFileUrl;
        try {
          jarFileUrl = ResourceUtils.extractJarFileURL(scriptsResourceUrl);
        } catch (MalformedURLException e) {
          throw new OpenGammaRuntimeException("Error resolving JAR file from URL " + scriptsResourceUrl);
        }
        int jarPathSeparatorIdx = scriptsResourceUrl.getFile().indexOf("!/");
        if (jarPathSeparatorIdx == -1) {
          throw new OpenGammaRuntimeException("Could not find resource path in JAR URL " + scriptsResourceUrl);
        }
        String jarPath = scriptsResourceUrl.getFile().substring(jarPathSeparatorIdx + 2);
        dbScriptDirectory = new ZipFileDbScriptDirectory(new File(jarFileUrl.getFile()), jarPath);
      } else {
        throw new OpenGammaRuntimeException("Unsupported scripts resource URL: " + scriptsResourceUrl);
      }
      dbToolContext.setScriptReader(new DbScriptReader(dbScriptDirectory));
    }
    if (getGroupsList() != null) {
      Set<String> schemaGroups = new HashSet<String>();
      for (String schemaGroup : getGroupsList().split(",")) {
        schemaGroups.add(schemaGroup.toLowerCase().trim());
      }
      dbToolContext.setGroups(schemaGroups);
    }
    repo.registerInfrastructure(DbToolContext.class, getClassifier(), dbToolContext);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code DbToolContextComponentFactory}.
   * @return the meta-bean, not null
   */
  public static DbToolContextComponentFactory.Meta meta() {
    return DbToolContextComponentFactory.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(DbToolContextComponentFactory.Meta.INSTANCE);
  }

  @Override
  public DbToolContextComponentFactory.Meta metaBean() {
    return DbToolContextComponentFactory.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        return getClassifier();
      case 39794031:  // dbConnector
        return getDbConnector();
      case -1752402828:  // jdbcUrl
        return getJdbcUrl();
      case 209279841:  // dbManagement
        return getDbManagement();
      case 1299772562:  // groupsList
        return getGroupsList();
      case 1948576054:  // scriptsResource
        return getScriptsResource();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        setClassifier((String) newValue);
        return;
      case 39794031:  // dbConnector
        setDbConnector((DbConnector) newValue);
        return;
      case -1752402828:  // jdbcUrl
        setJdbcUrl((String) newValue);
        return;
      case 209279841:  // dbManagement
        setDbManagement((DbManagement) newValue);
        return;
      case 1299772562:  // groupsList
        setGroupsList((String) newValue);
        return;
      case 1948576054:  // scriptsResource
        setScriptsResource((Resource) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_classifier, "classifier");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      DbToolContextComponentFactory other = (DbToolContextComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          JodaBeanUtils.equal(getDbConnector(), other.getDbConnector()) &&
          JodaBeanUtils.equal(getJdbcUrl(), other.getJdbcUrl()) &&
          JodaBeanUtils.equal(getDbManagement(), other.getDbManagement()) &&
          JodaBeanUtils.equal(getGroupsList(), other.getGroupsList()) &&
          JodaBeanUtils.equal(getScriptsResource(), other.getScriptsResource()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDbConnector());
    hash += hash * 31 + JodaBeanUtils.hashCode(getJdbcUrl());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDbManagement());
    hash += hash * 31 + JodaBeanUtils.hashCode(getGroupsList());
    hash += hash * 31 + JodaBeanUtils.hashCode(getScriptsResource());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier under which to publish.
   * @return the value of the property, not null
   */
  public String getClassifier() {
    return _classifier;
  }

  /**
   * Sets the classifier under which to publish.
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
   * Gets the database connector.
   * @return the value of the property
   */
  public DbConnector getDbConnector() {
    return _dbConnector;
  }

  /**
   * Sets the database connector.
   * @param dbConnector  the new value of the property
   */
  public void setDbConnector(DbConnector dbConnector) {
    this._dbConnector = dbConnector;
  }

  /**
   * Gets the the {@code dbConnector} property.
   * @return the property, not null
   */
  public final Property<DbConnector> dbConnector() {
    return metaBean().dbConnector().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the JDBC URL.
   * @return the value of the property
   */
  public String getJdbcUrl() {
    return _jdbcUrl;
  }

  /**
   * Sets the JDBC URL.
   * @param jdbcUrl  the new value of the property
   */
  public void setJdbcUrl(String jdbcUrl) {
    this._jdbcUrl = jdbcUrl;
  }

  /**
   * Gets the the {@code jdbcUrl} property.
   * @return the property, not null
   */
  public final Property<String> jdbcUrl() {
    return metaBean().jdbcUrl().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the database management instance.
   * @return the value of the property
   */
  public DbManagement getDbManagement() {
    return _dbManagement;
  }

  /**
   * Sets the database management instance.
   * @param dbManagement  the new value of the property
   */
  public void setDbManagement(DbManagement dbManagement) {
    this._dbManagement = dbManagement;
  }

  /**
   * Gets the the {@code dbManagement} property.
   * @return the property, not null
   */
  public final Property<DbManagement> dbManagement() {
    return metaBean().dbManagement().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets a comma-separated list of database object groups on which to operate.
   * @return the value of the property
   */
  public String getGroupsList() {
    return _groupsList;
  }

  /**
   * Sets a comma-separated list of database object groups on which to operate.
   * @param groupsList  the new value of the property
   */
  public void setGroupsList(String groupsList) {
    this._groupsList = groupsList;
  }

  /**
   * Gets the the {@code groupsList} property.
   * @return the property, not null
   */
  public final Property<String> groupsList() {
    return metaBean().groupsList().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets a resource pointing to the root of the database installation scripts.
   * @return the value of the property
   */
  public Resource getScriptsResource() {
    return _scriptsResource;
  }

  /**
   * Sets a resource pointing to the root of the database installation scripts.
   * @param scriptsResource  the new value of the property
   */
  public void setScriptsResource(Resource scriptsResource) {
    this._scriptsResource = scriptsResource;
  }

  /**
   * Gets the the {@code scriptsResource} property.
   * @return the property, not null
   */
  public final Property<Resource> scriptsResource() {
    return metaBean().scriptsResource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code DbToolContextComponentFactory}.
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
        this, "classifier", DbToolContextComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code dbConnector} property.
     */
    private final MetaProperty<DbConnector> _dbConnector = DirectMetaProperty.ofReadWrite(
        this, "dbConnector", DbToolContextComponentFactory.class, DbConnector.class);
    /**
     * The meta-property for the {@code jdbcUrl} property.
     */
    private final MetaProperty<String> _jdbcUrl = DirectMetaProperty.ofReadWrite(
        this, "jdbcUrl", DbToolContextComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code dbManagement} property.
     */
    private final MetaProperty<DbManagement> _dbManagement = DirectMetaProperty.ofReadWrite(
        this, "dbManagement", DbToolContextComponentFactory.class, DbManagement.class);
    /**
     * The meta-property for the {@code groupsList} property.
     */
    private final MetaProperty<String> _groupsList = DirectMetaProperty.ofReadWrite(
        this, "groupsList", DbToolContextComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code scriptsResource} property.
     */
    private final MetaProperty<Resource> _scriptsResource = DirectMetaProperty.ofReadWrite(
        this, "scriptsResource", DbToolContextComponentFactory.class, Resource.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "dbConnector",
        "jdbcUrl",
        "dbManagement",
        "groupsList",
        "scriptsResource");

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
        case 39794031:  // dbConnector
          return _dbConnector;
        case -1752402828:  // jdbcUrl
          return _jdbcUrl;
        case 209279841:  // dbManagement
          return _dbManagement;
        case 1299772562:  // groupsList
          return _groupsList;
        case 1948576054:  // scriptsResource
          return _scriptsResource;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends DbToolContextComponentFactory> builder() {
      return new DirectBeanBuilder<DbToolContextComponentFactory>(new DbToolContextComponentFactory());
    }

    @Override
    public Class<? extends DbToolContextComponentFactory> beanType() {
      return DbToolContextComponentFactory.class;
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
     * The meta-property for the {@code dbConnector} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<DbConnector> dbConnector() {
      return _dbConnector;
    }

    /**
     * The meta-property for the {@code jdbcUrl} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> jdbcUrl() {
      return _jdbcUrl;
    }

    /**
     * The meta-property for the {@code dbManagement} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<DbManagement> dbManagement() {
      return _dbManagement;
    }

    /**
     * The meta-property for the {@code groupsList} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> groupsList() {
      return _groupsList;
    }

    /**
     * The meta-property for the {@code scriptsResource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Resource> scriptsResource() {
      return _scriptsResource;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
