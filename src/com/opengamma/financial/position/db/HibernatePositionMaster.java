/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.db;

import java.util.Collection;

import javax.time.InstantProvider;
import javax.time.TimeSource;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionMaster;
import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A Hibernate database backed implementation of a PositionMaster. 
 * 
 * @author Andrew Griffin
 */
public class HibernatePositionMaster implements PositionMaster, InitializingBean {
  
  private HibernateTemplate _hibernateTemplate;
  
  public HibernatePositionMaster () {
  }
  
  public void setSessionFactory (final SessionFactory sessionFactory) {
    ArgumentChecker.checkNotNull(sessionFactory, "sessionFactory");
    _hibernateTemplate = new HibernateTemplate (sessionFactory);
  }
  
  protected HibernateTemplate getHibernateTemplate () {
    return _hibernateTemplate;
  }
  
  @Override
  public void afterPropertiesSet () throws Exception {
    if (getHibernateTemplate () == null) {
      throw new IllegalStateException ("sessionFactory not set");
    }
  }
  
  public PortfolioNode getPortfolioNode (final InstantProvider now, final DomainSpecificIdentifier identityKey) {
    // TODO
    return null;
  }

  @Override
  public PortfolioNode getPortfolioNode(final DomainSpecificIdentifier identityKey) {
    return getPortfolioNode (TimeSource.system ().instant (), identityKey);
  }
  
  public Position getPosition (final InstantProvider now, final DomainSpecificIdentifier identityKey) {
    // TODO
    return null;
  }

  @Override
  public Position getPosition(final DomainSpecificIdentifier identityKey) {
    return getPosition (TimeSource.system ().instant (), identityKey);
  }
  
  public Portfolio getRootPortfolio(final InstantProvider now, final String portfolioName) {
    // TODO
    return null;
  }

  @Override
  public Portfolio getRootPortfolio(String portfolioName) {
    return getRootPortfolio (TimeSource.system ().instant (), portfolioName);
  }
  
  public Collection<String> getRootPortfolioNames (final InstantProvider now) {
    // TODO
    return null;
  }
  
  @Override
  public Collection<String> getRootPortfolioNames() {
    return getRootPortfolioNames (TimeSource.system ().instant ());
  }
  
  
  
}