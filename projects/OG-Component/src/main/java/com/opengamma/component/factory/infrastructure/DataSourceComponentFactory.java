/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.component.factory.infrastructure;

import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.sql.DataSource;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jolbox.bonecp.BoneCPDataSource;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractAliasedComponentFactory;
import com.opengamma.util.db.management.jmx.DatabaseMBean;

/**
 * Component factory for a data source. This has some default values designed for the masters.
 * <p>
 * This class is designed to allow protected methods to be overridden.
 */
@BeanDefinition
public class DataSourceComponentFactory extends AbstractAliasedComponentFactory {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DataSourceComponentFactory.class);

  /**
   * The fully-qualified class name of the database driver.
   */
  @PropertyDefinition(validate = "notNull")
  private String _driverClass;
  /**
   * The JDBC connection URL.
   */
  @PropertyDefinition(validate = "notNull")
  private String _jdbcUrl;
  /**
   * The database user name.
   */
  @PropertyDefinition(validate = "notNull")
  private String _username;
  /**
   * The database password.
   */
  @PropertyDefinition(validate = "notNull")
  private String _password;
  /**
   * The connection pool name, defaults to the classifier name.
   */
  @PropertyDefinition
  private String _poolName;
  /**
   * The number of partitions, defaults to 2.
   */
  @PropertyDefinition
  private int _partitionCount = 2;
  /**
   * The number of connections to acquire at once, defaults to 1.
   */
  @PropertyDefinition
  private int _acquireIncrement = 1;
  /**
   * The minimum number of connections per partition, defaults to 1.
   */
  @PropertyDefinition
  private int _minConnectionsPerPartition = 1;
  /**
   * The maximum number of connections per partition, defaults to 10.
   */
  @PropertyDefinition
  private int _maxConnectionsPerPartition = 10;

  //-------------------------------------------------------------------------
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) throws Exception {
    if (getPoolName() == null) {
      setPoolName(getClassifier());
    }
    initDataSource(repo);
  }

  /**
   * Creates and registers the data source.
   * 
   * @param repo  the component repository, not null
   * @return the cache manager, not null
   */
  protected DataSource initDataSource(ComponentRepository repo) {
    DataSource dataSource = createDataSource(repo);
    registerComponentAndAliases(repo, DataSource.class, dataSource);
    registerMBean(repo, dataSource);
    return dataSource;
  }

  /**
   * Creates the data source without registering it.
   * 
   * @param repo  the component repository, only used to register secondary items like lifecycle, not null
   * @return the data source, not null
   */
  protected DataSource createDataSource(ComponentRepository repo) {
    BoneCPDataSource dataSource = new BoneCPDataSource();
    dataSource.setDriverClass(getDriverClass());
    dataSource.setJdbcUrl(getJdbcUrl());
    dataSource.setUsername(getUsername());
    dataSource.setPassword(getPassword());
    dataSource.setPoolName(getPoolName());
    dataSource.setPartitionCount(getPartitionCount());
    dataSource.setAcquireIncrement(getAcquireIncrement());
    dataSource.setMinConnectionsPerPartition(getMinConnectionsPerPartition());
    dataSource.setMaxConnectionsPerPartition(getMaxConnectionsPerPartition());
    return dataSource;
  }

  /**
   * Registers the JMX MBean for the data source.
   * 
   * @param repo  the component repository, not null
   * @param dataSource  the data source, not null
   */
  protected void registerMBean(ComponentRepository repo, DataSource dataSource) {
    DatabaseMBean.Local mbeanLocal = new DatabaseMBean.Local(getDriverClass(), dataSource);
    mbeanLocal.setLocalJdbc(getJdbcUrl());
    mbeanLocal.setUsername(getUsername());
    Hashtable<String, String> mbeanName = new Hashtable<String, String>();
    mbeanName.put("type", "DataSourceComponent");
    mbeanName.put("name", getClassifier());
    try {
      repo.registerMBean(mbeanLocal.mbean(), new ObjectName("com.opengamma", mbeanName));
    } catch (MalformedObjectNameException ex) {
      s_logger.error("Couldn't register MBEAN for {}: {}", this, ex.getMessage());
      s_logger.warn("Caught exception", ex);
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code DataSourceComponentFactory}.
   * @return the meta-bean, not null
   */
  public static DataSourceComponentFactory.Meta meta() {
    return DataSourceComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(DataSourceComponentFactory.Meta.INSTANCE);
  }

  @Override
  public DataSourceComponentFactory.Meta metaBean() {
    return DataSourceComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the fully-qualified class name of the database driver.
   * @return the value of the property, not null
   */
  public String getDriverClass() {
    return _driverClass;
  }

  /**
   * Sets the fully-qualified class name of the database driver.
   * @param driverClass  the new value of the property, not null
   */
  public void setDriverClass(String driverClass) {
    JodaBeanUtils.notNull(driverClass, "driverClass");
    this._driverClass = driverClass;
  }

  /**
   * Gets the the {@code driverClass} property.
   * @return the property, not null
   */
  public final Property<String> driverClass() {
    return metaBean().driverClass().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the JDBC connection URL.
   * @return the value of the property, not null
   */
  public String getJdbcUrl() {
    return _jdbcUrl;
  }

  /**
   * Sets the JDBC connection URL.
   * @param jdbcUrl  the new value of the property, not null
   */
  public void setJdbcUrl(String jdbcUrl) {
    JodaBeanUtils.notNull(jdbcUrl, "jdbcUrl");
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
   * Gets the database user name.
   * @return the value of the property, not null
   */
  public String getUsername() {
    return _username;
  }

  /**
   * Sets the database user name.
   * @param username  the new value of the property, not null
   */
  public void setUsername(String username) {
    JodaBeanUtils.notNull(username, "username");
    this._username = username;
  }

  /**
   * Gets the the {@code username} property.
   * @return the property, not null
   */
  public final Property<String> username() {
    return metaBean().username().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the database password.
   * @return the value of the property, not null
   */
  public String getPassword() {
    return _password;
  }

  /**
   * Sets the database password.
   * @param password  the new value of the property, not null
   */
  public void setPassword(String password) {
    JodaBeanUtils.notNull(password, "password");
    this._password = password;
  }

  /**
   * Gets the the {@code password} property.
   * @return the property, not null
   */
  public final Property<String> password() {
    return metaBean().password().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the connection pool name, defaults to the classifier name.
   * @return the value of the property
   */
  public String getPoolName() {
    return _poolName;
  }

  /**
   * Sets the connection pool name, defaults to the classifier name.
   * @param poolName  the new value of the property
   */
  public void setPoolName(String poolName) {
    this._poolName = poolName;
  }

  /**
   * Gets the the {@code poolName} property.
   * @return the property, not null
   */
  public final Property<String> poolName() {
    return metaBean().poolName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the number of partitions, defaults to 2.
   * @return the value of the property
   */
  public int getPartitionCount() {
    return _partitionCount;
  }

  /**
   * Sets the number of partitions, defaults to 2.
   * @param partitionCount  the new value of the property
   */
  public void setPartitionCount(int partitionCount) {
    this._partitionCount = partitionCount;
  }

  /**
   * Gets the the {@code partitionCount} property.
   * @return the property, not null
   */
  public final Property<Integer> partitionCount() {
    return metaBean().partitionCount().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the number of connections to acquire at once, defaults to 1.
   * @return the value of the property
   */
  public int getAcquireIncrement() {
    return _acquireIncrement;
  }

  /**
   * Sets the number of connections to acquire at once, defaults to 1.
   * @param acquireIncrement  the new value of the property
   */
  public void setAcquireIncrement(int acquireIncrement) {
    this._acquireIncrement = acquireIncrement;
  }

  /**
   * Gets the the {@code acquireIncrement} property.
   * @return the property, not null
   */
  public final Property<Integer> acquireIncrement() {
    return metaBean().acquireIncrement().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the minimum number of connections per partition, defaults to 1.
   * @return the value of the property
   */
  public int getMinConnectionsPerPartition() {
    return _minConnectionsPerPartition;
  }

  /**
   * Sets the minimum number of connections per partition, defaults to 1.
   * @param minConnectionsPerPartition  the new value of the property
   */
  public void setMinConnectionsPerPartition(int minConnectionsPerPartition) {
    this._minConnectionsPerPartition = minConnectionsPerPartition;
  }

  /**
   * Gets the the {@code minConnectionsPerPartition} property.
   * @return the property, not null
   */
  public final Property<Integer> minConnectionsPerPartition() {
    return metaBean().minConnectionsPerPartition().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the maximum number of connections per partition, defaults to 10.
   * @return the value of the property
   */
  public int getMaxConnectionsPerPartition() {
    return _maxConnectionsPerPartition;
  }

  /**
   * Sets the maximum number of connections per partition, defaults to 10.
   * @param maxConnectionsPerPartition  the new value of the property
   */
  public void setMaxConnectionsPerPartition(int maxConnectionsPerPartition) {
    this._maxConnectionsPerPartition = maxConnectionsPerPartition;
  }

  /**
   * Gets the the {@code maxConnectionsPerPartition} property.
   * @return the property, not null
   */
  public final Property<Integer> maxConnectionsPerPartition() {
    return metaBean().maxConnectionsPerPartition().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public DataSourceComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      DataSourceComponentFactory other = (DataSourceComponentFactory) obj;
      return JodaBeanUtils.equal(getDriverClass(), other.getDriverClass()) &&
          JodaBeanUtils.equal(getJdbcUrl(), other.getJdbcUrl()) &&
          JodaBeanUtils.equal(getUsername(), other.getUsername()) &&
          JodaBeanUtils.equal(getPassword(), other.getPassword()) &&
          JodaBeanUtils.equal(getPoolName(), other.getPoolName()) &&
          (getPartitionCount() == other.getPartitionCount()) &&
          (getAcquireIncrement() == other.getAcquireIncrement()) &&
          (getMinConnectionsPerPartition() == other.getMinConnectionsPerPartition()) &&
          (getMaxConnectionsPerPartition() == other.getMaxConnectionsPerPartition()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getDriverClass());
    hash += hash * 31 + JodaBeanUtils.hashCode(getJdbcUrl());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUsername());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPassword());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPoolName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPartitionCount());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAcquireIncrement());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMinConnectionsPerPartition());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMaxConnectionsPerPartition());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(320);
    buf.append("DataSourceComponentFactory{");
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
    buf.append("driverClass").append('=').append(JodaBeanUtils.toString(getDriverClass())).append(',').append(' ');
    buf.append("jdbcUrl").append('=').append(JodaBeanUtils.toString(getJdbcUrl())).append(',').append(' ');
    buf.append("username").append('=').append(JodaBeanUtils.toString(getUsername())).append(',').append(' ');
    buf.append("password").append('=').append(JodaBeanUtils.toString(getPassword())).append(',').append(' ');
    buf.append("poolName").append('=').append(JodaBeanUtils.toString(getPoolName())).append(',').append(' ');
    buf.append("partitionCount").append('=').append(JodaBeanUtils.toString(getPartitionCount())).append(',').append(' ');
    buf.append("acquireIncrement").append('=').append(JodaBeanUtils.toString(getAcquireIncrement())).append(',').append(' ');
    buf.append("minConnectionsPerPartition").append('=').append(JodaBeanUtils.toString(getMinConnectionsPerPartition())).append(',').append(' ');
    buf.append("maxConnectionsPerPartition").append('=').append(JodaBeanUtils.toString(getMaxConnectionsPerPartition())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code DataSourceComponentFactory}.
   */
  public static class Meta extends AbstractAliasedComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code driverClass} property.
     */
    private final MetaProperty<String> _driverClass = DirectMetaProperty.ofReadWrite(
        this, "driverClass", DataSourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code jdbcUrl} property.
     */
    private final MetaProperty<String> _jdbcUrl = DirectMetaProperty.ofReadWrite(
        this, "jdbcUrl", DataSourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code username} property.
     */
    private final MetaProperty<String> _username = DirectMetaProperty.ofReadWrite(
        this, "username", DataSourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code password} property.
     */
    private final MetaProperty<String> _password = DirectMetaProperty.ofReadWrite(
        this, "password", DataSourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code poolName} property.
     */
    private final MetaProperty<String> _poolName = DirectMetaProperty.ofReadWrite(
        this, "poolName", DataSourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code partitionCount} property.
     */
    private final MetaProperty<Integer> _partitionCount = DirectMetaProperty.ofReadWrite(
        this, "partitionCount", DataSourceComponentFactory.class, Integer.TYPE);
    /**
     * The meta-property for the {@code acquireIncrement} property.
     */
    private final MetaProperty<Integer> _acquireIncrement = DirectMetaProperty.ofReadWrite(
        this, "acquireIncrement", DataSourceComponentFactory.class, Integer.TYPE);
    /**
     * The meta-property for the {@code minConnectionsPerPartition} property.
     */
    private final MetaProperty<Integer> _minConnectionsPerPartition = DirectMetaProperty.ofReadWrite(
        this, "minConnectionsPerPartition", DataSourceComponentFactory.class, Integer.TYPE);
    /**
     * The meta-property for the {@code maxConnectionsPerPartition} property.
     */
    private final MetaProperty<Integer> _maxConnectionsPerPartition = DirectMetaProperty.ofReadWrite(
        this, "maxConnectionsPerPartition", DataSourceComponentFactory.class, Integer.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "driverClass",
        "jdbcUrl",
        "username",
        "password",
        "poolName",
        "partitionCount",
        "acquireIncrement",
        "minConnectionsPerPartition",
        "maxConnectionsPerPartition");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1227291184:  // driverClass
          return _driverClass;
        case -1752402828:  // jdbcUrl
          return _jdbcUrl;
        case -265713450:  // username
          return _username;
        case 1216985755:  // password
          return _password;
        case 634919111:  // poolName
          return _poolName;
        case -1051422651:  // partitionCount
          return _partitionCount;
        case -349316295:  // acquireIncrement
          return _acquireIncrement;
        case 1469514128:  // minConnectionsPerPartition
          return _minConnectionsPerPartition;
        case 237252158:  // maxConnectionsPerPartition
          return _maxConnectionsPerPartition;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends DataSourceComponentFactory> builder() {
      return new DirectBeanBuilder<DataSourceComponentFactory>(new DataSourceComponentFactory());
    }

    @Override
    public Class<? extends DataSourceComponentFactory> beanType() {
      return DataSourceComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code driverClass} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> driverClass() {
      return _driverClass;
    }

    /**
     * The meta-property for the {@code jdbcUrl} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> jdbcUrl() {
      return _jdbcUrl;
    }

    /**
     * The meta-property for the {@code username} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> username() {
      return _username;
    }

    /**
     * The meta-property for the {@code password} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> password() {
      return _password;
    }

    /**
     * The meta-property for the {@code poolName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> poolName() {
      return _poolName;
    }

    /**
     * The meta-property for the {@code partitionCount} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> partitionCount() {
      return _partitionCount;
    }

    /**
     * The meta-property for the {@code acquireIncrement} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> acquireIncrement() {
      return _acquireIncrement;
    }

    /**
     * The meta-property for the {@code minConnectionsPerPartition} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> minConnectionsPerPartition() {
      return _minConnectionsPerPartition;
    }

    /**
     * The meta-property for the {@code maxConnectionsPerPartition} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> maxConnectionsPerPartition() {
      return _maxConnectionsPerPartition;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1227291184:  // driverClass
          return ((DataSourceComponentFactory) bean).getDriverClass();
        case -1752402828:  // jdbcUrl
          return ((DataSourceComponentFactory) bean).getJdbcUrl();
        case -265713450:  // username
          return ((DataSourceComponentFactory) bean).getUsername();
        case 1216985755:  // password
          return ((DataSourceComponentFactory) bean).getPassword();
        case 634919111:  // poolName
          return ((DataSourceComponentFactory) bean).getPoolName();
        case -1051422651:  // partitionCount
          return ((DataSourceComponentFactory) bean).getPartitionCount();
        case -349316295:  // acquireIncrement
          return ((DataSourceComponentFactory) bean).getAcquireIncrement();
        case 1469514128:  // minConnectionsPerPartition
          return ((DataSourceComponentFactory) bean).getMinConnectionsPerPartition();
        case 237252158:  // maxConnectionsPerPartition
          return ((DataSourceComponentFactory) bean).getMaxConnectionsPerPartition();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1227291184:  // driverClass
          ((DataSourceComponentFactory) bean).setDriverClass((String) newValue);
          return;
        case -1752402828:  // jdbcUrl
          ((DataSourceComponentFactory) bean).setJdbcUrl((String) newValue);
          return;
        case -265713450:  // username
          ((DataSourceComponentFactory) bean).setUsername((String) newValue);
          return;
        case 1216985755:  // password
          ((DataSourceComponentFactory) bean).setPassword((String) newValue);
          return;
        case 634919111:  // poolName
          ((DataSourceComponentFactory) bean).setPoolName((String) newValue);
          return;
        case -1051422651:  // partitionCount
          ((DataSourceComponentFactory) bean).setPartitionCount((Integer) newValue);
          return;
        case -349316295:  // acquireIncrement
          ((DataSourceComponentFactory) bean).setAcquireIncrement((Integer) newValue);
          return;
        case 1469514128:  // minConnectionsPerPartition
          ((DataSourceComponentFactory) bean).setMinConnectionsPerPartition((Integer) newValue);
          return;
        case 237252158:  // maxConnectionsPerPartition
          ((DataSourceComponentFactory) bean).setMaxConnectionsPerPartition((Integer) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((DataSourceComponentFactory) bean)._driverClass, "driverClass");
      JodaBeanUtils.notNull(((DataSourceComponentFactory) bean)._jdbcUrl, "jdbcUrl");
      JodaBeanUtils.notNull(((DataSourceComponentFactory) bean)._username, "username");
      JodaBeanUtils.notNull(((DataSourceComponentFactory) bean)._password, "password");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
