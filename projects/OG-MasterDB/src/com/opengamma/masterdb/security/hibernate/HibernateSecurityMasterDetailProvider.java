/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

import java.sql.SQLException;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.google.common.base.Objects;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.bond.BondSecuritySearchRequest;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.masterdb.security.DbSecurityMaster;
import com.opengamma.masterdb.security.SecurityMasterDetailProvider;
import com.opengamma.masterdb.security.hibernate.bond.BondSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.capfloor.CapFloorCMSSpreadSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.capfloor.CapFloorSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.cash.CashSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.equity.EquitySecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.equity.EquityVarianceSwapSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.fra.FRASecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.future.FutureSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.fx.FXForwardSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.fx.FXSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.fx.NonDeliverableFXForwardSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.option.EquityBarrierOptionSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.option.EquityIndexOptionSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.option.EquityOptionSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.option.FxBarrierOptionSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.option.FxOptionSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.option.IRFutureOptionSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.option.NonDeliverableFxOptionSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.option.SwaptionSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.swap.SwapSecurityBeanOperation;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDialect;
import com.opengamma.util.db.DbMapSqlParameterSource;

/**
 * Provides access to persist the full bean structure of the security.
 * This supports the default {@link DbSecurityMaster} implementations.
 */
public class HibernateSecurityMasterDetailProvider implements SecurityMasterDetailProvider {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(HibernateSecurityMasterDetailProvider.class);
  private static final ConcurrentMap<Class<?>, SecurityBeanOperation<?, ?>> BEAN_OPERATIONS_BY_SECURITY = new ConcurrentHashMap<Class<?>, SecurityBeanOperation<?, ?>>();
  private static final ConcurrentMap<Class<?>, SecurityBeanOperation<?, ?>> BEAN_OPERATIONS_BY_BEAN = new ConcurrentHashMap<Class<?>, SecurityBeanOperation<?, ?>>();
  private static final ConcurrentMap<String, SecurityBeanOperation<?, ?>> BEAN_OPERATIONS_BY_TYPE = new ConcurrentHashMap<String, SecurityBeanOperation<?, ?>>();

  /**
   * The database connector.
   */
  private DbConnector _dbConnector;
  /**
   * The operation context for management additional resources.
   */
  private final OperationContext _operationContext = new OperationContext();

  //-------------------------------------------------------------------------
  private static void loadBeanOperation(final SecurityBeanOperation<?, ?> beanOperation) {
    BEAN_OPERATIONS_BY_SECURITY.put(beanOperation.getSecurityClass(), beanOperation);
    BEAN_OPERATIONS_BY_BEAN.put(beanOperation.getBeanClass(), beanOperation);
    BEAN_OPERATIONS_BY_TYPE.put(beanOperation.getSecurityType(), beanOperation);
  }

  private static SecurityBeanOperation<?, ?> getBeanOperation(final ConcurrentMap<Class<?>, SecurityBeanOperation<?, ?>> map, final Class<?> clazz) {
    SecurityBeanOperation<?, ?> beanOperation = map.get(clazz);
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
  private static <T extends Security> SecurityBeanOperation<T, SecurityBean> getBeanOperation(final T security) {
    final SecurityBeanOperation<?, ?> beanOperation = getBeanOperation(BEAN_OPERATIONS_BY_SECURITY, security.getClass());
    if (beanOperation == null) {
      throw new OpenGammaRuntimeException("can't find BeanOperation for " + security);
    }
    return (SecurityBeanOperation<T, SecurityBean>) beanOperation;
  }

//  @SuppressWarnings("unchecked")
//  private static <T extends SecurityBean> SecurityBeanOperation<Security, T> getBeanOperation(final T bean) {
//    final SecurityBeanOperation<?, ?> beanOperation = getBeanOperation(BEAN_OPERATIONS_BY_BEAN, bean.getClass());
//    if (beanOperation == null) {
//      throw new OpenGammaRuntimeException("can't find BeanOperation for " + bean);
//    }
//    return (SecurityBeanOperation<Security, T>) beanOperation;
//  }

  @SuppressWarnings("unchecked")
  private static <T extends SecurityBean> SecurityBeanOperation<Security, T> getBeanOperation(final String type) {
    SecurityBeanOperation<?, ?> beanOperation = BEAN_OPERATIONS_BY_TYPE.get(type.toUpperCase(Locale.ENGLISH));  // upper case handles "Cash"
    if (beanOperation == null) {
      if (type.contains("_")) {
        beanOperation = BEAN_OPERATIONS_BY_TYPE.get(type.substring(type.indexOf('_') + 1));
      }
      if (type.equals("SWAPTION")) { // SWAPTION used to be SWAP_OPTION, in which case the above code handled it.
        beanOperation = BEAN_OPERATIONS_BY_TYPE.get("OPTION");
      }
      if (beanOperation == null) {
        throw new OpenGammaRuntimeException("can't find BeanOperation for " + type);
      }
    }
    return (SecurityBeanOperation<Security, T>) beanOperation;
  }

  static {
    // TODO 2010-07-21 Should we load these from a .properties file like the other factories
    loadBeanOperation(BondSecurityBeanOperation.INSTANCE);
    loadBeanOperation(CashSecurityBeanOperation.INSTANCE);
    loadBeanOperation(EquitySecurityBeanOperation.INSTANCE);
    loadBeanOperation(FRASecurityBeanOperation.INSTANCE);
    loadBeanOperation(FutureSecurityBeanOperation.INSTANCE);
    loadBeanOperation(SwapSecurityBeanOperation.INSTANCE);
    loadBeanOperation(EquityIndexOptionSecurityBeanOperation.INSTANCE);
    loadBeanOperation(EquityOptionSecurityBeanOperation.INSTANCE);
    loadBeanOperation(EquityBarrierOptionSecurityBeanOperation.INSTANCE);
    loadBeanOperation(FxOptionSecurityBeanOperation.INSTANCE);
    loadBeanOperation(NonDeliverableFxOptionSecurityBeanOperation.INSTANCE);
    loadBeanOperation(SwaptionSecurityBeanOperation.INSTANCE);
    loadBeanOperation(IRFutureOptionSecurityBeanOperation.INSTANCE);
    loadBeanOperation(FxBarrierOptionSecurityBeanOperation.INSTANCE);
    loadBeanOperation(FXSecurityBeanOperation.INSTANCE);
    loadBeanOperation(FXForwardSecurityBeanOperation.INSTANCE);
    loadBeanOperation(NonDeliverableFXForwardSecurityBeanOperation.INSTANCE);
    loadBeanOperation(CapFloorSecurityBeanOperation.INSTANCE);
    loadBeanOperation(CapFloorCMSSpreadSecurityBeanOperation.INSTANCE);
    loadBeanOperation(EquityVarianceSwapSecurityBeanOperation.INSTANCE);
  }

  //-------------------------------------------------------------------------
  @Override
  public void init(DbSecurityMaster master) {
    _dbConnector = master.getDbConnector();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the context for additional resources.
   * @return the context
   */
  protected OperationContext getOperationContext() {
    return _operationContext;
  }

  /**
   * Gets the Hibernate Spring template.
   * @return the template
   */
  protected HibernateTemplate getHibernateTemplate() {
    return _dbConnector.getHibernateTemplate();
  }

  /**
   * Gets the database dialect.
   * @return the dialect
   */
  protected DbDialect getDialect() {
    return _dbConnector.getDialect();
  }

  /**
   * Gets the session DAO.
   * @param session  the session
   * @return the DAO
   */
  protected HibernateSecurityMasterDao getHibernateSecurityMasterSession(final Session session) {
    return new HibernateSecurityMasterSession(session);
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableSecurity loadSecurityDetail(final ManageableSecurity base) {
    s_logger.debug("loading detail for security {}", base.getUniqueId());
    return getHibernateTemplate().execute(new HibernateCallback<ManageableSecurity>() {
      @SuppressWarnings({"unchecked", "rawtypes" })
      @Override
      public ManageableSecurity doInHibernate(Session session) throws HibernateException, SQLException {
        final SecurityBeanOperation beanOperation = getBeanOperation(base.getSecurityType());
        HibernateSecurityMasterDao secMasterSession = getHibernateSecurityMasterSession(session);
        SecurityBean security = secMasterSession.getSecurityBean(base, beanOperation);
        if (security == null) {
          s_logger.debug("no detail found for security {}", base.getUniqueId());
          return base;
        }
        security = beanOperation.resolve(getOperationContext(), secMasterSession, null, security);
        final ManageableSecurity result = (ManageableSecurity) beanOperation.createSecurity(getOperationContext(), security);
        if (result == null) {
          throw new IllegalStateException("Unable to convert security from database: " + base.getUniqueId() + " " + base.getSecurityType());
        }
        if (Objects.equal(base.getSecurityType(), result.getSecurityType()) == false) {
          throw new IllegalStateException("Security type returned by Hibernate load does not match");
        }
        result.setUniqueId(base.getUniqueId());
        result.setName(base.getName());
        result.setExternalIdBundle(base.getExternalIdBundle());
        result.setAttributes(base.getAttributes());
        return result;
      }
    });
  }

  @Override
  public void storeSecurityDetail(final ManageableSecurity security) {
    s_logger.debug("storing detail for security {}", security.getUniqueId());
    if (security.getClass() == ManageableSecurity.class) {
      return;  // no detail to store
    }
    getHibernateTemplate().execute(new HibernateCallback<Object>() {
      @SuppressWarnings({"unchecked", "rawtypes" })
      @Override
      public Object doInHibernate(final Session session) throws HibernateException, SQLException {
        final HibernateSecurityMasterDao secMasterSession = getHibernateSecurityMasterSession(session);
        final SecurityBeanOperation beanOperation = getBeanOperation(security);
        final Date now = new Date();
        secMasterSession.createSecurityBean(getOperationContext(), beanOperation, now, security);
        return null;
      }
    });
  }

  @Override
  public void extendSearch(SecuritySearchRequest request, DbMapSqlParameterSource args) {
    if (request instanceof BondSecuritySearchRequest) {
      BondSecuritySearchRequest bondRequest = (BondSecuritySearchRequest) request;
      if (bondRequest.getIssuerName() != null || bondRequest.getIssuerType() != null) {
        args.addValue("sql_search_bond_join", Boolean.TRUE);
      }
      if (bondRequest.getIssuerName() != null) {
        args.addValue("bond_issuer_name", getDialect().sqlWildcardAdjustValue(bondRequest.getIssuerName()));
      }
      if (bondRequest.getIssuerType() != null) {
        args.addValue("bond_issuer_type", getDialect().sqlWildcardAdjustValue(bondRequest.getIssuerName()));
      }
    }
  }

}
