/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.engine.security.Security;
import com.opengamma.financial.RegionRepository;
import com.opengamma.financial.security.AbstractSecurityMaster;
import com.opengamma.financial.security.SecurityDocument;
import com.opengamma.financial.security.SecuritySearchHistoricRequest;
import com.opengamma.financial.security.SecuritySearchHistoricResult;
import com.opengamma.financial.security.db.bond.BondSecurityBeanOperation;
import com.opengamma.financial.security.db.cash.CashSecurityBeanOperation;
import com.opengamma.financial.security.db.equity.EquitySecurityBeanOperation;
import com.opengamma.financial.security.db.fra.FRASecurityBeanOperation;
import com.opengamma.financial.security.db.future.FutureSecurityBeanOperation;
import com.opengamma.financial.security.db.option.OptionSecurityBeanOperation;
import com.opengamma.financial.security.db.swap.SwapSecurityBeanOperation;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;

/**
 * 
 */
public class HibernateSecurityMaster extends AbstractSecurityMaster {

  private static final Logger s_logger = LoggerFactory.getLogger(HibernateSecurityMaster.class);
  private static final ConcurrentMap<Class<?>, BeanOperation<?, ?>> BEAN_OPERATIONS_BY_SECURITY = new ConcurrentHashMap<Class<?>, BeanOperation<?, ?>>();
  private static final ConcurrentMap<Class<?>, BeanOperation<?, ?>> BEAN_OPERATIONS_BY_BEAN = new ConcurrentHashMap<Class<?>, BeanOperation<?, ?>>();

  /**
   * The scheme used by the master by default.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "HibernateSecurityMaster";

  /**
   * The modified by user.
   */
  protected static final String MODIFIED_BY = "";

  private HibernateTemplate _hibernateTemplate;
  private String _identifierScheme = IDENTIFIER_SCHEME_DEFAULT;
  private final OperationContext _operationContext = new OperationContext();

  private static void loadBeanOperation(final BeanOperation<?, ?> beanOperation) {
    BEAN_OPERATIONS_BY_SECURITY.put(beanOperation.getSecurityClass(), beanOperation);
    BEAN_OPERATIONS_BY_BEAN.put(beanOperation.getBeanClass(), beanOperation);
  }

  private static BeanOperation<?, ?> getBeanOperation(final ConcurrentMap<Class<?>, BeanOperation<?, ?>> map, final Class<?> clazz) {
    BeanOperation<?, ?> beanOperation = map.get(clazz);
    if (beanOperation != null) {
      return beanOperation;
    }
    if (clazz.getSuperclass() == null) {
      return null;
    }
    beanOperation = getBeanOperation(map, clazz.getSuperclass());
    if (beanOperation != null) {
      map.put(clazz, beanOperation);
    }
    return beanOperation;
  }

  @SuppressWarnings("unchecked")
  private static <T extends Security> BeanOperation<T, SecurityBean> getBeanOperation(final T security) {
    final BeanOperation<?, ?> beanOperation = getBeanOperation(BEAN_OPERATIONS_BY_SECURITY, security.getClass());
    if (beanOperation == null) {
      throw new OpenGammaRuntimeException("can't find BeanOperation for " + security);
    }
    return (BeanOperation<T, SecurityBean>) beanOperation;
  }

  @SuppressWarnings("unchecked")
  private static <T extends SecurityBean> BeanOperation<Security, T> getBeanOperation(final T bean) {
    final BeanOperation<?, ?> beanOperation = getBeanOperation(BEAN_OPERATIONS_BY_BEAN, bean.getClass());
    if (beanOperation == null) {
      throw new OpenGammaRuntimeException("can't find BeanOperation for " + bean);
    }
    return (BeanOperation<Security, T>) beanOperation;
  }

  static {
    // TODO 2010-07-21 Should we load these from a .properties file like the other factories
    loadBeanOperation(BondSecurityBeanOperation.INSTANCE);
    loadBeanOperation(CashSecurityBeanOperation.INSTANCE);
    loadBeanOperation(EquitySecurityBeanOperation.INSTANCE);
    loadBeanOperation(FRASecurityBeanOperation.INSTANCE);
    loadBeanOperation(OptionSecurityBeanOperation.INSTANCE);
    loadBeanOperation(FutureSecurityBeanOperation.INSTANCE);
    loadBeanOperation(SwapSecurityBeanOperation.INSTANCE);
  }

  public void setSessionFactory(SessionFactory sessionFactory) {
    _hibernateTemplate = new HibernateTemplate(sessionFactory);
  }

  protected OperationContext getOperationContext() {
    return _operationContext;
  }

  public void setRegionRepository(final RegionRepository regionRepository) {
    getOperationContext().setRegionRepository(regionRepository);
  }

  public String getIdentifierScheme() {
    return _identifierScheme;
  }

  public void setIdentifierScheme(final String identifierScheme) {
    _identifierScheme = identifierScheme;
  }

  /**
   * Creates a unique identifier.
   * @param dbBean  the securityBean, not null
   * @return the created unique identifier, not null
   */
  public UniqueIdentifier createUniqueIdentifier(SecurityBean dbBean) {
    return UniqueIdentifier.of(getIdentifierScheme(), dbBean.getFirstVersion().getId().toString(), dbBean.getId().toString());
  }

  protected HibernateTemplate getHibernateTemplate() {
    return _hibernateTemplate;
  }

  protected HibernateSecurityMasterDao getHibernateSecurityMasterSession(final Session session) {
    return new HibernateSecurityMasterSession(session);
  }

  @SuppressWarnings("unchecked")
  protected Security getSecurity(final Date now, final UniqueIdentifier uid, final boolean populateWithOtherIdentifiers) {
    if (uid.getScheme().equals(getIdentifierScheme()) == false) {
      s_logger.debug("rejecting invalid identity key domain '{}'", uid.getScheme());
      throw new IllegalArgumentException("Invalid identifier for HibernateSecurityMaster: " + uid);
    }
    return (Security) getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        HibernateSecurityMasterDao secMasterSession = getHibernateSecurityMasterSession(session);
        SecurityBean security = secMasterSession.getSecurityBean(now, uid);
        // we use the DefaultSecurity interface because we need access to setIdentifiers
        if (security != null) {
          final BeanOperation beanOperation = getBeanOperation(security);
          security = beanOperation.resolve(getOperationContext(), secMasterSession, now, security);
          final DefaultSecurity result = (DefaultSecurity) beanOperation.createSecurity(getOperationContext(), security);
          result.setUniqueIdentifier(createUniqueIdentifier(security));
          final List<Identifier> identifiers = new ArrayList<Identifier>();
          if (populateWithOtherIdentifiers) {
            Query identifierQuery = session.getNamedQuery("IdentifierAssociationBean.many.byDateSecurity");
            identifierQuery.setParameter("security", security.getFirstVersion());
            identifierQuery.setDate("now", now);
            List<IdentifierAssociationBean> otherIdentifiers = identifierQuery.list();
            for (IdentifierAssociationBean associationBean : otherIdentifiers) {
              identifiers.add(Converters.identifierBeanToIdentifier(associationBean.getIdentifier()));
            }
          }
          result.setIdentifiers(new IdentifierBundle(identifiers));
          result.setName(StringUtils.defaultString(security.getDisplayName()));
          return result;
        }
        return null;
      }
    });
  }

  @SuppressWarnings("unchecked")
  protected Security getSecurity(final Date now, final IdentifierBundle bundle, final boolean populateWithOtherIdentifiers) {
    return (Security) getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        HibernateSecurityMasterDao secMasterSession = getHibernateSecurityMasterSession(session);
        SecurityBean security = secMasterSession.getSecurityBean(now, bundle);
        // we use the DefaultSecurity interface because we need access to setIdentifiers
        if (security != null) {
          final BeanOperation beanOperation = getBeanOperation(security);
          security = beanOperation.resolve(getOperationContext(), secMasterSession, now, security);
          final DefaultSecurity result = (DefaultSecurity) beanOperation.createSecurity(getOperationContext(), security);
          result.setUniqueIdentifier(createUniqueIdentifier(security));
          final List<Identifier> identifiers = new ArrayList<Identifier>();
          if (populateWithOtherIdentifiers) {
            Query identifierQuery = session.getNamedQuery("IdentifierAssociationBean.many.byDateSecurity");
            identifierQuery.setParameter("security", security.getFirstVersion());
            identifierQuery.setDate("now", now);
            List<IdentifierAssociationBean> otherIdentifiers = identifierQuery.list();
            for (IdentifierAssociationBean associationBean : otherIdentifiers) {
              identifiers.add(Converters.identifierBeanToIdentifier(associationBean.getIdentifier()));
            }
          }
          result.setIdentifiers(new IdentifierBundle(identifiers));
          result.setName(StringUtils.defaultString(security.getDisplayName()));
          return result;
        }
        return null;
      }
    });
  }

  /**
   * Puts a security into the master.
   * @param now  the instant to add at
   * @param security  the security to add
   * @return the created unique identifier
   */
  /*
   * public UniqueIdentifier putSecurity(final Date now, final Security security) {
   * return (UniqueIdentifier) _hibernateTemplate.execute(new HibernateCallback() {
   * 
   * @Override
   * public Object doInHibernate(final Session session) throws HibernateException, SQLException {
   * HibernateSecurityMasterSession secMasterSession = new HibernateSecurityMasterSession(session);
   * BeanOperation<Security, SecurityBean> beanOperation = getBeanOperation(security);
   * SecurityBean updatedDbBean;
   * if (security.getUniqueIdentifier() == null ||
   * security.getUniqueIdentifier().getScheme().equals(getIdentifierScheme()) == false) {
   * // add security
   * updatedDbBean = secMasterSession.createSecurityBean(beanOperation, now, false, now, null, null, security);
   * for (Identifier identifier : security.getIdentifiers()) {
   * secMasterSession.associateOrUpdateIdentifierWithSecurity(now, identifier, updatedDbBean);
   * }
   * } else {
   * // update security
   * SecurityBean origDbBean = secMasterSession.getSecurityBean(security.getUniqueIdentifier());
   * if (beanOperation.getBeanClass().isAssignableFrom(origDbBean.getClass()) == false) {
   * throw new OpenGammaRuntimeException("Security has changed type: " + security + "/" + origDbBean);
   * }
   * origDbBean = beanOperation.resolve(secMasterSession, now, origDbBean);
   * if (beanOperation.beanEquals(origDbBean, security)) {
   * // security is the same as the one in the database - no action
   * return security.getUniqueIdentifier();
   * }
   * updatedDbBean = secMasterSession.createSecurityBean(beanOperation, now, false, now, MODIFIED_BY, origDbBean.getFirstVersion(), security);
   * }
   * UniqueIdentifier uid = createUniqueIdentifier(updatedDbBean);
   * if (security instanceof DefaultSecurity) {
   * ((DefaultSecurity) security).setUniqueIdentifier(uid);
   * }
   * return uid;
   * }
   * });
   * }
   */

  /*
   * @SuppressWarnings("unchecked")
   * public List<String> getExchanges() {
   * return (List<String>) _hibernateTemplate.executeFind(new HibernateCallback() {
   * 
   * @Override
   * public Object doInHibernate(Session session) throws HibernateException,
   * SQLException {
   * HibernateSecurityMasterDao secMasterSession = new HibernateSecurityMasterSession(session);
   * List<ExchangeBean> exchangeBeans = secMasterSession.getExchangeBeans();
   * List<String> exchanges = new ArrayList<String>();
   * if (exchangeBeans != null) {
   * for (ExchangeBean exchangeBean : exchangeBeans) {
   * exchanges.add(exchangeBean.getName());
   * }
   * }
   * return exchanges;
   * }
   * });
   * }
   */

  /*
   * @SuppressWarnings("unchecked")
   * public List<Currency> getCurrencies() {
   * return (List<Currency>) _hibernateTemplate.execute(new HibernateCallback() {
   * 
   * @Override
   * public Object doInHibernate(Session session) throws HibernateException,
   * SQLException {
   * HibernateSecurityMasterDao secMasterSession = new HibernateSecurityMasterSession(session);
   * List<CurrencyBean> currencyBeans = secMasterSession.getCurrencyBeans();
   * List<Currency> currencies = new ArrayList<Currency>();
   * if (currencyBeans != null) {
   * for (CurrencyBean currencyBean : currencyBeans) {
   * currencies.add(Converters.currencyBeanToCurrency(currencyBean));
   * }
   * }
   * return currencies;
   * }
   * });
   * 
   * }
   */

  // -------------------------------------------------------------------------
  @Override
  @SuppressWarnings("unchecked")
  public Security getSecurity(final UniqueIdentifier uid) {
    if (uid.isLatest()) {
      return getSecurity(new Date(), uid, true);
    }
    if (uid.getScheme().equals(getIdentifierScheme()) == false) {
      s_logger.debug("rejecting invalid identity key domain '{}'", uid.getScheme());
      throw new IllegalArgumentException("Invalid identifier for HibernateSecurityMaster: " + uid);
    }
    return (Security) getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        HibernateSecurityMasterDao secMasterSession = getHibernateSecurityMasterSession(session);
        SecurityBean security = secMasterSession.getSecurityBean(uid);
        // we use the DefaultSecurity interface because we need access to setIdentifiers
        if (security == null) {
          return null;
        }
        final BeanOperation beanOperation = getBeanOperation(security);
        security = beanOperation.resolve(getOperationContext(), secMasterSession, null, security);
        final DefaultSecurity result = (DefaultSecurity) beanOperation.createSecurity(getOperationContext(), security);
        result.setUniqueIdentifier(createUniqueIdentifier(security));
        final List<Identifier> identifiers = new ArrayList<Identifier>();
        Query identifierQuery = session.getNamedQuery("IdentifierAssociationBean.many.byDateSecurity");
        identifierQuery.setParameter("security", security.getFirstVersion());
        identifierQuery.setDate("now", security.getEffectiveDateTime());
        List<IdentifierAssociationBean> otherIdentifiers = identifierQuery.list();
        for (IdentifierAssociationBean associationBean : otherIdentifiers) {
          identifiers.add(Converters.identifierBeanToIdentifier(associationBean.getIdentifier()));
        }
        result.setIdentifiers(new IdentifierBundle(identifiers));
        result.setName(StringUtils.defaultString(security.getDisplayName()));
        return result;
      }
    });
  }

  @Override
  public Security getSecurity(IdentifierBundle secKey) {
    return getSecurity(new Date(), secKey, true);
  }

  // SecurityMaster

  @Override
  public SecurityDocument add(final SecurityDocument document) {
    return (SecurityDocument) getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(final Session session) throws HibernateException, SQLException {
        final HibernateSecurityMasterDao secMasterSession = getHibernateSecurityMasterSession(session);
        final Security security = document.getSecurity();
        final BeanOperation<Security, SecurityBean> beanOperation = getBeanOperation(security);
        final Date now = new Date();
        final SecurityBean bean = secMasterSession.createSecurityBean(getOperationContext(), beanOperation, now, false, now, null, null, security);
        for (Identifier identifier : security.getIdentifiers()) {
          secMasterSession.associateOrUpdateIdentifierWithSecurity(now, identifier, bean);
        }
        final UniqueIdentifier uniqueIdentifier = createUniqueIdentifier(bean);
        document.setUniqueIdentifier(uniqueIdentifier);
        if (security instanceof MutableUniqueIdentifiable) {
          ((MutableUniqueIdentifiable) security).setUniqueIdentifier(uniqueIdentifier);
        }
        return document;
      }
    });
  }

  @Override
  public SecurityDocument correct(final SecurityDocument document) {
    // TODO
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public void remove(final UniqueIdentifier uid) {
    getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(final Session session) throws HibernateException, SQLException {
        final HibernateSecurityMasterDao secMasterSession = getHibernateSecurityMasterSession(session);
        SecurityBean bean = secMasterSession.getSecurityBean(uid);
        if (bean == null) {
          throw new DataNotFoundException("UniqueIdentifier = " + uid);
        }
        final BeanOperation<Security, SecurityBean> beanOperation = getBeanOperation(bean);
        final Date now = new Date();
        bean = beanOperation.resolve(getOperationContext(), secMasterSession, now, bean);
        // Write a bean to the master with the same data and its DELETE flag set
        secMasterSession.createSecurityBean(getOperationContext(), beanOperation, now, true, now, MODIFIED_BY, bean.getFirstVersion(), beanOperation.createSecurity(getOperationContext(), bean));
        return null;
      }
    });
  }

  @Override
  public SecuritySearchHistoricResult searchHistoric(SecuritySearchHistoricRequest request) {
    // TODO
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public SecurityDocument update(final SecurityDocument document) {
    return (SecurityDocument) getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(final Session session) throws HibernateException, SQLException {
        final HibernateSecurityMasterDao secMasterSession = getHibernateSecurityMasterSession(session);
        final Security security = document.getSecurity();
        final BeanOperation<Security, SecurityBean> beanOperation = getBeanOperation(security);
        final Date now = new Date();
        SecurityBean origDbBean = secMasterSession.getSecurityBean(security.getUniqueIdentifier());
        if (origDbBean == null) {
          throw new DataNotFoundException("Unique identifier " + security.getUniqueIdentifier() + " not found in security master");
        }
        if (beanOperation.getBeanClass().isAssignableFrom(origDbBean.getClass()) == false) {
          throw new IllegalArgumentException("Security has changed type: " + security + "/" + origDbBean);
        }
        origDbBean = beanOperation.resolve(getOperationContext(), secMasterSession, now, origDbBean);
        if (beanOperation.beanEquals(getOperationContext(), origDbBean, security)) {
          // security is the same as the one in the database - no action
          return document;
        }
        final SecurityBean updatedDbBean = secMasterSession.createSecurityBean(getOperationContext(), beanOperation, now, false, now, MODIFIED_BY, origDbBean.getFirstVersion(), security);
        final UniqueIdentifier uniqueIdentifier = createUniqueIdentifier(updatedDbBean);
        document.setUniqueIdentifier(uniqueIdentifier);
        if (security instanceof MutableUniqueIdentifiable) {
          ((MutableUniqueIdentifiable) security).setUniqueIdentifier(uniqueIdentifier);
        }
        return document;
      }
    });
  }

}
