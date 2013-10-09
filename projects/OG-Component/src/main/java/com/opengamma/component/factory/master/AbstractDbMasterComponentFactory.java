/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.component.factory.master;

import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.jolbox.bonecp.BoneCPDataSource;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.masterdb.AbstractDbMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.management.DbManagement;
import com.opengamma.util.db.management.DbManagementUtils;
import com.opengamma.util.db.script.DbScriptUtils;
import com.opengamma.util.db.tool.DbCreateOperation;
import com.opengamma.util.db.tool.DbToolContext;
import com.opengamma.util.db.tool.DbUpgradeOperation;

/**
 * Base component factory for all {@link AbstractDbMaster} implementations.
 */
@BeanDefinition
public abstract class AbstractDbMasterComponentFactory extends AbstractComponentFactory {
  
  private static final Logger s_logger = LoggerFactory.getLogger(DbSecurityMasterComponentFactory.class);
  
  /**
   * The database connector.
   */
  @PropertyDefinition(validate = "notNull")
  private DbConnector _dbConnector;
  /**
   * The flag determining whether to enforce the schema version, preventing the server from starting if the version
   * does not match the expected version.
   */
  @PropertyDefinition
  private boolean _enforceSchemaVersion = true;
  /**
   * The flag determining whether to manage the database objects automatically.
   * <p>
   * The database objects will be created if they do not exist, and will be upgraded if their version is older than the
   * server expects. Database objects will never be deleted and the server will fail to start if the database is found
   * in an unexpected state.
   * <p>
   * This flag is intended for use with temporary user databases. 
   */
  @PropertyDefinition
  private boolean _autoSchemaManagement;
  
  //-------------------------------------------------------------------------
  protected void checkSchema(Integer actualSchemaVersion, String schemaName) {
    if (isAutoSchemaManagement()) {
      manageSchema(actualSchemaVersion, schemaName);
    } else {
      checkSchemaVersion(actualSchemaVersion, schemaName);
    }
  }
  
  @SuppressWarnings("resource")
  private void manageSchema(Integer actualSchemaVersion, String schemaName) {
    ArgumentChecker.notNull(schemaName, "schemaName");
    
    // REVIEW jonathan 2013-05-14 -- don't look at this :-)
    if (!(getDbConnector().getDataSource() instanceof BoneCPDataSource)) {
      s_logger.warn("Unable to obtain database management instance. Database objects cannot be inspected or modified, and may be missing or out-of-date.");
      return; 
    }
    BoneCPDataSource dataSource = (BoneCPDataSource) getDbConnector().getDataSource();
    String jdbcUrl = dataSource.getJdbcUrl();
    if (jdbcUrl == null) {
      throw new OpenGammaRuntimeException("No JDBC URL specified");
    }
    DbManagement dbManagement = DbManagementUtils.getDbManagement(jdbcUrl);
    int lastSlashIdx = jdbcUrl.lastIndexOf("/");
    if (lastSlashIdx == -1) {
      throw new OpenGammaRuntimeException("JDBC URL must contain '/' before the database name");
    }

    // REVIEW jonathan 2013-05-14 -- should not be doing this (PLAT-2745)
    int lastSlash = jdbcUrl.lastIndexOf('/');
    if (lastSlash == -1 || lastSlash == jdbcUrl.length() - 1) {
      throw new OpenGammaRuntimeException("JDBC URL must contain a slash separating the server host and the database name");
    }
    String dbServerHost = jdbcUrl.substring(0, lastSlash);
    String catalog = jdbcUrl.substring(lastSlashIdx + 1);
    String user = dataSource.getUsername();
    String password = dataSource.getPassword();
    dbManagement.initialise(dbServerHost, user, password);
    
    Integer expectedSchemaVersion = DbScriptUtils.getCurrentVersion(schemaName);
    if (expectedSchemaVersion == null) {
      throw new OpenGammaRuntimeException("Unable to find schema version information for " + schemaName + ". Database objects cannot be managed.");
    }
    // DbToolContext should not be closed as DbConnector needs to remain started
    DbToolContext dbToolContext = new DbToolContext();
    dbToolContext.setDbConnector(getDbConnector());
    dbToolContext.setDbManagement(dbManagement);
    dbToolContext.setCatalog(catalog);
    dbToolContext.setSchemaNames(ImmutableSet.of(schemaName));
    if (actualSchemaVersion == null) {
      // Assume empty database, so attempt to create tables
      DbCreateOperation createOperation = new DbCreateOperation(dbToolContext, true, null, false);
      createOperation.execute();
    } else if (actualSchemaVersion < expectedSchemaVersion) {
      // Upgrade from expected to actual
      DbUpgradeOperation upgradeOperation = new DbUpgradeOperation(dbToolContext, true, null);
      upgradeOperation.execute();
    } else if (expectedSchemaVersion > actualSchemaVersion) {
      throw new OpenGammaRuntimeException(schemaName + " schema too new. This build of the OpenGamma Platform works with version " +
          expectedSchemaVersion + " of the " + schemaName + " schema, but the database contains version " + actualSchemaVersion +
          ". Unable to downgrade an existing database.");
    }
  }

  private void checkSchemaVersion(Integer actualSchemaVersion, String schemaName) {
    ArgumentChecker.notNull(schemaName, "schemaName");
    if (actualSchemaVersion == null) {
      throw new OpenGammaRuntimeException("Unable to find current " + schemaName + " schema version in database");
    }
    Integer expectedSchemaVersion = DbScriptUtils.getCurrentVersion(schemaName);
    if (expectedSchemaVersion == null) {
      s_logger.info("Unable to find schema version information for {}. The database schema may differ from the required version.", schemaName);
      return;
    }
    if (expectedSchemaVersion.intValue() == actualSchemaVersion) {
      s_logger.debug("Verified " + schemaName + " schema version " + actualSchemaVersion);
      return;
    }
    String relativeDbAge = expectedSchemaVersion.intValue() < actualSchemaVersion ? "new" : "old";
    String message = schemaName + " schema too " + relativeDbAge + ". This build of the OpenGamma Platform works with version " +
        expectedSchemaVersion + " of the " + schemaName + " schema, but the database contains version " + actualSchemaVersion + ".";
    if (isEnforceSchemaVersion()) {
      throw new OpenGammaRuntimeException(message);
    } else {
      s_logger.warn(message);
    }
  }
  
  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code AbstractDbMasterComponentFactory}.
   * @return the meta-bean, not null
   */
  public static AbstractDbMasterComponentFactory.Meta meta() {
    return AbstractDbMasterComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(AbstractDbMasterComponentFactory.Meta.INSTANCE);
  }

  @Override
  public AbstractDbMasterComponentFactory.Meta metaBean() {
    return AbstractDbMasterComponentFactory.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 39794031:  // dbConnector
        return getDbConnector();
      case 2128193333:  // enforceSchemaVersion
        return isEnforceSchemaVersion();
      case 1236703379:  // autoSchemaManagement
        return isAutoSchemaManagement();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 39794031:  // dbConnector
        setDbConnector((DbConnector) newValue);
        return;
      case 2128193333:  // enforceSchemaVersion
        setEnforceSchemaVersion((Boolean) newValue);
        return;
      case 1236703379:  // autoSchemaManagement
        setAutoSchemaManagement((Boolean) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_dbConnector, "dbConnector");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      AbstractDbMasterComponentFactory other = (AbstractDbMasterComponentFactory) obj;
      return JodaBeanUtils.equal(getDbConnector(), other.getDbConnector()) &&
          JodaBeanUtils.equal(isEnforceSchemaVersion(), other.isEnforceSchemaVersion()) &&
          JodaBeanUtils.equal(isAutoSchemaManagement(), other.isAutoSchemaManagement()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getDbConnector());
    hash += hash * 31 + JodaBeanUtils.hashCode(isEnforceSchemaVersion());
    hash += hash * 31 + JodaBeanUtils.hashCode(isAutoSchemaManagement());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the database connector.
   * @return the value of the property, not null
   */
  public DbConnector getDbConnector() {
    return _dbConnector;
  }

  /**
   * Sets the database connector.
   * @param dbConnector  the new value of the property, not null
   */
  public void setDbConnector(DbConnector dbConnector) {
    JodaBeanUtils.notNull(dbConnector, "dbConnector");
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
   * Gets the flag determining whether to enforce the schema version, preventing the server from starting if the version
   * does not match the expected version.
   * @return the value of the property
   */
  public boolean isEnforceSchemaVersion() {
    return _enforceSchemaVersion;
  }

  /**
   * Sets the flag determining whether to enforce the schema version, preventing the server from starting if the version
   * does not match the expected version.
   * @param enforceSchemaVersion  the new value of the property
   */
  public void setEnforceSchemaVersion(boolean enforceSchemaVersion) {
    this._enforceSchemaVersion = enforceSchemaVersion;
  }

  /**
   * Gets the the {@code enforceSchemaVersion} property.
   * does not match the expected version.
   * @return the property, not null
   */
  public final Property<Boolean> enforceSchemaVersion() {
    return metaBean().enforceSchemaVersion().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the flag determining whether to manage the database objects automatically.
   * <p>
   * The database objects will be created if they do not exist, and will be upgraded if their version is older than the
   * server expects. Database objects will never be deleted and the server will fail to start if the database is found
   * in an unexpected state.
   * <p>
   * This flag is intended for use with temporary user databases.
   * @return the value of the property
   */
  public boolean isAutoSchemaManagement() {
    return _autoSchemaManagement;
  }

  /**
   * Sets the flag determining whether to manage the database objects automatically.
   * <p>
   * The database objects will be created if they do not exist, and will be upgraded if their version is older than the
   * server expects. Database objects will never be deleted and the server will fail to start if the database is found
   * in an unexpected state.
   * <p>
   * This flag is intended for use with temporary user databases.
   * @param autoSchemaManagement  the new value of the property
   */
  public void setAutoSchemaManagement(boolean autoSchemaManagement) {
    this._autoSchemaManagement = autoSchemaManagement;
  }

  /**
   * Gets the the {@code autoSchemaManagement} property.
   * <p>
   * The database objects will be created if they do not exist, and will be upgraded if their version is older than the
   * server expects. Database objects will never be deleted and the server will fail to start if the database is found
   * in an unexpected state.
   * <p>
   * This flag is intended for use with temporary user databases.
   * @return the property, not null
   */
  public final Property<Boolean> autoSchemaManagement() {
    return metaBean().autoSchemaManagement().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code AbstractDbMasterComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code dbConnector} property.
     */
    private final MetaProperty<DbConnector> _dbConnector = DirectMetaProperty.ofReadWrite(
        this, "dbConnector", AbstractDbMasterComponentFactory.class, DbConnector.class);
    /**
     * The meta-property for the {@code enforceSchemaVersion} property.
     */
    private final MetaProperty<Boolean> _enforceSchemaVersion = DirectMetaProperty.ofReadWrite(
        this, "enforceSchemaVersion", AbstractDbMasterComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code autoSchemaManagement} property.
     */
    private final MetaProperty<Boolean> _autoSchemaManagement = DirectMetaProperty.ofReadWrite(
        this, "autoSchemaManagement", AbstractDbMasterComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "dbConnector",
        "enforceSchemaVersion",
        "autoSchemaManagement");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 39794031:  // dbConnector
          return _dbConnector;
        case 2128193333:  // enforceSchemaVersion
          return _enforceSchemaVersion;
        case 1236703379:  // autoSchemaManagement
          return _autoSchemaManagement;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends AbstractDbMasterComponentFactory> builder() {
      throw new UnsupportedOperationException("AbstractDbMasterComponentFactory is an abstract class");
    }

    @Override
    public Class<? extends AbstractDbMasterComponentFactory> beanType() {
      return AbstractDbMasterComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code dbConnector} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<DbConnector> dbConnector() {
      return _dbConnector;
    }

    /**
     * The meta-property for the {@code enforceSchemaVersion} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> enforceSchemaVersion() {
      return _enforceSchemaVersion;
    }

    /**
     * The meta-property for the {@code autoSchemaManagement} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> autoSchemaManagement() {
      return _autoSchemaManagement;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
