package com.opengamma.financial.security.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.WritableSecurityMaster;
import com.opengamma.financial.Currency;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

public class HibernateSecurityMaster implements WritableSecurityMaster {

  private static final Logger s_logger = LoggerFactory.getLogger(HibernateSecurityMaster.class);
  private static final Set<String> SUPPORTED_SECURITY_TYPES = new HashSet<String>();
  private static final ConcurrentMap<Class<?>,BeanOperation<?,?>> BEAN_OPERATIONS_BY_SECURITY = new ConcurrentHashMap<Class<?>,BeanOperation<?,?>> ();
  private static final ConcurrentMap<Class<?>,BeanOperation<?,?>> BEAN_OPERATIONS_BY_BEAN = new ConcurrentHashMap<Class<?>,BeanOperation<?,?>> ();
  public static final String IDENTIFIER_SCHEME_DEFAULT = "HibernateSecurityMaster";
  protected static final String MODIFIED_BY = "";

  private HibernateTemplate _hibernateTemplate = null;
  private String _identifierScheme = IDENTIFIER_SCHEME_DEFAULT;

  /**
   * Creates a unique identifier.
   * @param value  the value, not null
   */
  public static UniqueIdentifier createUniqueIdentifier(String value) {
    // TODO: this static method is broken as it should use getIdentifierScheme()
    return UniqueIdentifier.of(IDENTIFIER_SCHEME_DEFAULT, value);
  }

  private static void loadBeanOperation (final BeanOperation<?,?> beanOperation) {
    SUPPORTED_SECURITY_TYPES.add (beanOperation.getSecurityType ());
    BEAN_OPERATIONS_BY_SECURITY.put (beanOperation.getSecurityClass (), beanOperation);
    BEAN_OPERATIONS_BY_BEAN.put (beanOperation.getBeanClass (), beanOperation);
  }
  
  private static BeanOperation<?,?> getBeanOperation (final ConcurrentMap<Class<?>,BeanOperation<?,?>> map, final Class<?> clazz) {
    BeanOperation<?,?> beanOperation = map.get (clazz);
    if (beanOperation != null) return beanOperation;
    if (clazz.getSuperclass () == null) return null;
    beanOperation = getBeanOperation (map, clazz.getSuperclass ());
    if (beanOperation != null) {
      map.put (clazz, beanOperation);
    }
    return beanOperation;
  }
  
  @SuppressWarnings("unchecked")
  private static <T extends Security> BeanOperation<T,SecurityBean> getBeanOperation (final T security) {
    final BeanOperation<?,?> beanOperation = getBeanOperation (BEAN_OPERATIONS_BY_SECURITY, security.getClass ());
    if (beanOperation == null) throw new OpenGammaRuntimeException ("can't find BeanOperation for " + security);
    return (BeanOperation<T,SecurityBean>)beanOperation;
  }
  
  @SuppressWarnings("unchecked")
  private static <T extends SecurityBean> BeanOperation<Security,T> getBeanOperation (final T bean) {
    final BeanOperation<?,?> beanOperation = getBeanOperation (BEAN_OPERATIONS_BY_BEAN, bean.getClass ());
    if (beanOperation == null) throw new OpenGammaRuntimeException ("can't find BeanOperation for " + bean);
    return (BeanOperation<Security,T>)beanOperation;
  }

  static {
    loadBeanOperation (BondSecurityBeanOperation.INSTANCE);
    loadBeanOperation (EquitySecurityBeanOperation.INSTANCE);
    loadBeanOperation (OptionSecurityBeanOperation.INSTANCE);
    loadBeanOperation (FutureSecurityBeanOperation.INSTANCE);
  }
  
  public void setSessionFactory(SessionFactory sessionFactory) {
    _hibernateTemplate = new HibernateTemplate(sessionFactory);
  }

  public String getIdentifierScheme () {
    return _identifierScheme;
  }
  
  public void setIdentifierScheme (final String identifierScheme) {
    _identifierScheme = identifierScheme;
  }

  // for unit testing
  /*package*/ HibernateTemplate getHibernateTemplate() {
    return _hibernateTemplate;
  }
  
  // PUBLIC API
  
  @SuppressWarnings("unchecked")
  public Security getSecurity(final Date now, final UniqueIdentifier uid, final boolean populateWithOtherIdentifiers) {
    if (uid.getScheme().equals(getIdentifierScheme()) == false) {
      s_logger.debug("rejecting invalid identity key domain '{}'", uid.getScheme());
      throw new IllegalArgumentException("Invalid identifier for HibernateSecurityMaster: " + uid);
    }
    return (Security)_hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        HibernateSecurityMasterSession secMasterSession = new HibernateSecurityMasterSession(session);
        SecurityBean security = secMasterSession.getSecurityBean(now, uid);
        // we use the DefaultSecurity interface because we need access to setIdentifiers
        if (security != null) {
          final BeanOperation beanOperation = getBeanOperation(security);
          security = beanOperation.resolve(secMasterSession, now, security);
          final DefaultSecurity result = (DefaultSecurity) beanOperation.createSecurity(uid, security);
          final List<Identifier> identifiers = new ArrayList<Identifier>();
//          if (populateWithOtherIdentifiers) {
//            Query identifierQuery = session.getNamedQuery("SecurityBean.one.byDateIdentifier");
//            identifierQuery.setParameter("security", security.getFirstVersion());
//            identifierQuery.setDate("now", now);
//            List<IdentifierAssociationBean> otherIdentifiers = identifierQuery.list();
//            for (IdentifierAssociationBean associationBean : otherIdentifiers) {
//              identifiers.add(Converters.identifierBeanToIdentifier(associationBean.getIdentifier()));
//            }
//          }
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
  public Security getSecurity(final Date now, final IdentifierBundle bundle, final boolean populateWithOtherIdentifiers) {
    return (Security)_hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        HibernateSecurityMasterSession secMasterSession = new HibernateSecurityMasterSession(session);
        SecurityBean security = secMasterSession.getSecurityBean(now, bundle);
        // we use the DefaultSecurity interface because we need access to setIdentifiers
        if (security != null) {
          final BeanOperation beanOperation = getBeanOperation(security);
          security = beanOperation.resolve(secMasterSession, now, security);
          final DefaultSecurity result = (DefaultSecurity) beanOperation.createSecurity(null, security);
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
  
  @Override
  public UniqueIdentifier putSecurity (final Date now, final Security security) {
    return (UniqueIdentifier) _hibernateTemplate.execute (new HibernateCallback () {
      @Override
      public Object doInHibernate (final Session session) throws HibernateException, SQLException {
        HibernateSecurityMasterSession secMasterSession = new HibernateSecurityMasterSession(session);
        BeanOperation<Security,SecurityBean> beanOperation = getBeanOperation (security);
        SecurityBean bean = secMasterSession.getSecurityBean(now, security.getIdentifiers());
        if (bean == null) {
          bean = secMasterSession.createSecurityBean (beanOperation, now, false, now, null, null, security);
          for (Identifier identifier : security.getIdentifiers ()) {
            secMasterSession.associateOrUpdateIdentifierWithSecurity (now, identifier, bean);
          }
        } else if (beanOperation.getBeanClass ().isAssignableFrom (bean.getClass ())) {
          bean = beanOperation.resolve (secMasterSession, now, bean);
          if (beanOperation.beanEquals (bean, security)) {
            // security is the same as the one in the database - no action
          } else {
            // create a new version of the object in the database
            secMasterSession.createSecurityBean (beanOperation, now, false, now, MODIFIED_BY, bean, security);
          }
        } else {
          throw new OpenGammaRuntimeException ("SecurityBean of unexpected type " + bean);
        }
        UniqueIdentifier uid = HibernateSecurityMaster.createUniqueIdentifier(bean.getId().toString());
        if (security instanceof DefaultSecurity) {
          ((DefaultSecurity) security).setUniqueIdentifier(uid);
        }
        return uid;
      }
    });
  }
  
  @SuppressWarnings("unchecked")
  public List<String> getExchanges() {
    return (List<String>) _hibernateTemplate.executeFind(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        HibernateSecurityMasterSession secMasterSession = new HibernateSecurityMasterSession(session);
        List<ExchangeBean> exchangeBeans = secMasterSession.getExchangeBeans();
        List<String> exchanges = new ArrayList<String>();
        if (exchangeBeans != null) {
          for (ExchangeBean exchangeBean : exchangeBeans) {
            exchanges.add(exchangeBean.getName());
          }
        }
        return exchanges;
      } 
    });
  }

  @SuppressWarnings("unchecked")
  public List<Currency> getCurrencies() {
    return (List<Currency>) _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        HibernateSecurityMasterSession secMasterSession = new HibernateSecurityMasterSession(session);
        List<CurrencyBean> currencyBeans = secMasterSession.getCurrencyBeans();
        List<Currency> currencies = new ArrayList<Currency>();
        if (currencyBeans != null) {
          for (CurrencyBean currencyBean : currencyBeans) {
            currencies.add(Converters.currencyBeanToCurrency(currencyBean));
          }
        }
        return currencies;      
      }
    });

  }

  //-------------------------------------------------------------------------
  @Override
  public Security getSecurity(UniqueIdentifier uid) {
    return getSecurity(new Date(), uid, true);
  }

  // TODO: consider if this needs to take a date
  @Override
  public Collection<Security> getSecurities(IdentifierBundle secKey) {
    Collection<Security> results = new HashSet<Security>();
    for (Identifier id : secKey) {
      Security security = getSecurity(new IdentifierBundle(id));
      if (security != null) {
        results.add(security);
      }
    }
    return results;
  }

  // TODO: consider if this needs to take a date
  @Override
  public Security getSecurity(IdentifierBundle secKey) {
    for (Identifier id : secKey) {
      Security security = getSecurity(new Date(), new IdentifierBundle(id), true);
      if (security != null) {
        return security;
      }
    }
    return null;
  }

  @Override
  public Set<String> getAllSecurityTypes() {
    return SUPPORTED_SECURITY_TYPES;
  }

}
