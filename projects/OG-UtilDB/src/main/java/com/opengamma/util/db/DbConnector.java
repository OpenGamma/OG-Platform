/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import static com.opengamma.util.db.HibernateDbUtils.fixSQLExceptionCause;

import java.sql.Timestamp;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.Connector;
import com.opengamma.util.ReflectionUtils;
import com.opengamma.util.time.DateUtils;

/**
 * Connector used to access SQL databases.
 * <p>
 * This class provides a simple-to-setup and simple-to-use way to access databases. It can be configured for access via JDBC, Hibernate or both. The main benefit is simpler configuration, especially
 * if that configuration is in XML.
 * <p>
 * This class is usually configured using the associated factory bean.
 */
public class DbConnector implements Connector {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbConnector.class);

  static {
    DateUtils.initTimeZone();
  }

  /**
   * The configuration name.
   */
  private final String _name;
  /**
   * The data source.
   */
  private final DataSource _dataSource;
  /**
   * The dialect.
   */
  private final DbDialect _dialect;
  /**
   * The JDBC template.
   */
  private final NamedParameterJdbcTemplate _jdbcTemplate;
  /**
   * The Hibernate template.
   */
  private final HibernateTemplate _hibernateTemplate;
  /**
   * The transaction template.
   */
  private final TransactionTemplate _transactionTemplate;
  /**
   * The clock.
   */
  private final DbClock _clock;

  /**
   * Creates an instance.
   * 
   * @param name the configuration name, not null
   * @param dialect the database dialect, not null
   * @param dataSource the data source, not null
   * @param jdbcTemplate the JDBC template, not null
   * @param hibernateTemplate the Hibernate template, may be null
   * @param transactionTemplate the transaction template, not null
   */
  public DbConnector(
      String name, DbDialect dialect, DataSource dataSource,
      NamedParameterJdbcTemplate jdbcTemplate, HibernateTemplate hibernateTemplate, TransactionTemplate transactionTemplate) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(dialect, "dialect");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(jdbcTemplate, "JDBC template");
    ArgumentChecker.notNull(transactionTemplate, "transactionTemplate");
    _name = name;
    _dataSource = dataSource;
    _dialect = dialect;
    _jdbcTemplate = jdbcTemplate;
    _hibernateTemplate = hibernateTemplate;
    _transactionTemplate = transactionTemplate;
    _clock = new DbClock(this);  // late initialization
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the display name of the connector.
   * 
   * @return a name usable for display, not null
   */
  @Override
  public final String getName() {
    return _name;
  }

  /**
   * Gets the type of the connector, which is {@code DbConnector}.
   * 
   * @return the connector type, not null
   */
  @Override
  public final Class<? extends Connector> getType() {
    return DbConnector.class;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the data source.
   * 
   * @return the data source, not null
   */
  public DataSource getDataSource() {
    return _dataSource;
  }

  /**
   * Gets the database dialect.
   * 
   * @return the database dialect, not null
   */
  public DbDialect getDialect() {
    return _dialect;
  }

  /**
   * Gets the JDBC operations.
   * <p>
   * This is used for simple calls that do no use named parameters.
   * 
   * @return the JDBC template, not null
   */
  public JdbcOperations getJdbcOperations() {
    return _jdbcTemplate.getJdbcOperations();
  }

  /**
   * Gets the JDBC template.
   * <p>
   * This is used for named parameters.
   * 
   * @return the JDBC template, not null
   */
  public NamedParameterJdbcTemplate getJdbcTemplate() {
    return _jdbcTemplate;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the Hibernate session factory.
   * <p>
   * This is shared between all users of this object and must not be further configured.
   * 
   * @return the Hibernate session factory, may be null
   */
  public SessionFactory getHibernateSessionFactory() {
    if (_hibernateTemplate == null) {
      return null;
    }
    return _hibernateTemplate.getSessionFactory();
  }

  /**
   * Gets the shared Hibernate template.
   * <p>
   * This is shared between all users of this object and must not be further configured.
   * 
   * @return the Hibernate template, null if the session factory is null
   */
  public HibernateTemplate getHibernateTemplate() {
    return _hibernateTemplate;
  }

  /**
   * Gets the simple Hibernate transaction template.
   * 
   * @return the Hibernate transaction template, not null
   */
  public HibernateTransactionTemplate getHibernateTransactionTemplate() {
    return new HibernateTransactionTemplate();
  }

  /**
   * Gets the retrying Hibernate transaction template.
   * 
   * @param retries how many maximum retires should be tried
   * @return the retrying Hibernate transaction template, not null
   */
  public HibernateTransactionTemplateRetrying getHibernateTransactionTemplateRetrying(int retries) {
    return new HibernateTransactionTemplateRetrying(retries);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the transaction manager.
   * <p>
   * This is shared between all users of this object and must not be further configured.
   * 
   * @return the transaction manager, may be null
   */
  public PlatformTransactionManager getTransactionManager() {
    return _transactionTemplate.getTransactionManager();
  }

  /**
   * Gets the transaction template.
   * <p>
   * This is shared between all users of this object and must not be further configured.
   * 
   * @return the transaction template, may be null
   */
  public TransactionTemplate getTransactionTemplate() {
    return _transactionTemplate;
  }

  /**
   * Gets the retrying transaction template.
   * <p>
   * 
   * @param retries how many maximum retires should be tried
   * @return the retrying transaction template
   */
  public TransactionTemplateRetrying getTransactionTemplateRetrying(int retries) {
    return new TransactionTemplateRetrying(retries);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the current instant based on a cached database clock.
   * 
   * @return the current instant, may be null
   */
  public Instant now() {
    return _clock.instant();
  }

  /**
   * Gets the current instant using the database clock.
   * 
   * @return the current database instant, may be null
   */
  Timestamp nowDb() {
    return getJdbcOperations().queryForObject(getDialect().sqlSelectNow(), Timestamp.class);
  }

  /**
   * Returns a time-source based on the current database clock.
   * <p>
   * This can be used to obtain the current instant by calling {@link Instant#now(Clock)}.
   * 
   * @return the database time-source, may be null
   */
  public Clock timeSource() {
    return _clock;
  }

  //-------------------------------------------------------------------------
  @Override
  public void close() {
    ReflectionUtils.close(getDataSource());
    ReflectionUtils.close(getTransactionManager());
    ReflectionUtils.close(getHibernateSessionFactory());
    getDialect().close();
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a description of this object suitable for debugging.
   * 
   * @return the description, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + _name + "]";
  }

  //-------------------------------------------------------------------------
  /**
   * A transaction template that retries the underlying Spring-based template.
   */
  public class TransactionTemplateRetrying {
    private final int _retries;
    private final TransactionTemplate _transactionTemplate;

    TransactionTemplateRetrying(int retries) {
      _retries = retries;
      _transactionTemplate = getTransactionTemplate();
    }

    /**
     * Executes the template, which will retry the code in the event of failure.
     * 
     * @param <T> the type of the result
     * @param action the underlying Spring-based template containing the action to perform, not null
     * @return the result of the underlying template
     * @throws TransactionException if an error occurs
     */
    public <T> T execute(TransactionCallback<T> action) throws TransactionException {
      // retry to handle concurrent conflicting inserts into unique content tables
      for (int retry = 0; true; retry++) {
        try {
          return _transactionTemplate.execute(action);
        } catch (DataIntegrityViolationException ex) {
          if (retry == _retries) {
            throw ex;
          }
          s_logger.warn("Execution failure on attempt " + retry + " of " + _retries, ex);
        } catch (DataAccessException ex) {
          throw fixSQLExceptionCause(ex);
        }
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * A standard transaction template wrapping the underlying Hibernate template.
   */
  public class HibernateTransactionTemplate {
    private final TransactionTemplate _transactionTemplate;
    private final HibernateTemplate _hibernateTemplate;

    HibernateTransactionTemplate() {
      _transactionTemplate = getTransactionTemplate();
      _hibernateTemplate = getHibernateTemplate();
    }

    /**
     * Executes the underlying template in a transaction.
     * 
     * @param <T> the type of the result
     * @param action the underlying Hibernate template containing the action to perform, not null
     * @return the result of the underlying template
     * @throws TransactionException if an error occurs
     */
    public <T> T execute(final HibernateCallback<T> action) throws TransactionException {
      try {
        return _transactionTemplate.execute(new TransactionCallback<T>() {
          @Override
          public T doInTransaction(TransactionStatus status) {
            return _hibernateTemplate.execute(action);
          }
        });
      } catch (DataAccessException ex) {
        throw fixSQLExceptionCause(ex);
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * A transaction template that retries the underlying Hibernate template.
   */
  public class HibernateTransactionTemplateRetrying {
    private final int _retries;
    private final TransactionTemplate _transactionTemplate;
    private final HibernateTemplate _hibernateTemplate;

    HibernateTransactionTemplateRetrying(int retries) {
      _retries = retries;
      _transactionTemplate = getTransactionTemplate();
      _hibernateTemplate = getHibernateTemplate();
      _hibernateTemplate.setAllowCreate(false);
    }

    /**
     * Executes the template, which will retry the code in the event of failure.
     * 
     * @param <T> the type of the result
     * @param action the underlying Hibernate template containing the action to perform, not null
     * @return the result of the underlying template
     * @throws TransactionException if an error occurs
     */
    public <T> T execute(final HibernateCallback<T> action) throws TransactionException {
      // retry to handle concurrent conflicting inserts into unique content tables
      for (int retry = 0; true; retry++) {
        try {
          return _transactionTemplate.execute(new TransactionCallback<T>() {
            @Override
            public T doInTransaction(TransactionStatus status) {
              return _hibernateTemplate.execute(action);
            }
          });
        } catch (DataIntegrityViolationException ex) {
          if (retry == _retries) {
            throw ex;
          }
        } catch (DataAccessException ex) {
          throw fixSQLExceptionCause(ex);
        }
      }
    }
  }

}
