/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb;

import org.springframework.orm.hibernate3.HibernateTemplate;

import com.opengamma.util.ArgumentChecker;

/**
 * An abstract master for rapid implementation of a database backed master.
 * <p>
 * This combines the various configuration elements and convenience methods
 * needed for most database masters.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 */
public abstract class HibernateTemplateProvider {

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
