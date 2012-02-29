/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb;

import com.opengamma.extsql.ExtSqlBundle;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDialect;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import javax.time.Instant;
import javax.time.TimeSource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * An abstract master for rapid implementation of a database backed master.
 * <p>
 * This combines the various configuration elements and convenience methods
 * needed for most database masters.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 */
public abstract class HibernateTemplateProvider {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(HibernateTemplateProvider.class);
  
  /**
   * The Hibernate template.
   */
  private HibernateTemplate _hibernateTemplate;

  /**
   * Creates an instance.
   * 
   * @param hibernateTemplate  the hibernateTemplate, not null
   */
  public HibernateTemplateProvider(HibernateTemplate hibernateTemplate) {
    ArgumentChecker.notNull(hibernateTemplate, "hibernateTemplate");
    _hibernateTemplate = hibernateTemplate;
    _hibernateTemplate.setAllowCreate(false);
  }

  //-------------------------------------------------------------------------
  
  /**
   * Gets the local Hibernate template.
   *
   * @return the template, not null
   */
  public HibernateTemplate getHibernateTemplate() {
    return _hibernateTemplate;
  }

}
