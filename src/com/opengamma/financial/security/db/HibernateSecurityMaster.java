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
import com.opengamma.engine.security.SecurityKey;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.financial.Currency;
import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.IdentificationDomain;

//import com.opengamma.engine.security.Security;
//import com.opengamma.engine.security.SecurityKey;
//import com.opengamma.engine.security.SecurityMaster;

public class HibernateSecurityMaster implements SecurityMaster {
  private static final IdentificationDomain DEFAULT_DOMAIN = new IdentificationDomain("BLOOMBERG");
  private static final Set<String> SUPPORTED_SECURITY_TYPES = new HashSet<String>();
  private static final ConcurrentMap<Class<?>,BeanOperation<?,?>> BEAN_OPERATIONS_BY_SECURITY = new ConcurrentHashMap<Class<?>,BeanOperation<?,?>> ();
  private static final ConcurrentMap<Class<?>,BeanOperation<?,?>> BEAN_OPERATIONS_BY_BEAN = new ConcurrentHashMap<Class<?>,BeanOperation<?,?>> ();
  protected static final String MODIFIED_BY = "";
  private Logger s_logger = LoggerFactory.getLogger(HibernateSecurityMaster.class);
  private HibernateTemplate _hibernateTemplate = null;
  
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
    loadBeanOperation (EquityOptionSecurityBeanOperation.INSTANCE);
  }
  
  public void setSessionFactory(SessionFactory sessionFactory) {
    _hibernateTemplate = new HibernateTemplate(sessionFactory);
  }
  
  // for unit testing
  /*package*/ HibernateTemplate getHibernateTemplate() {
    return _hibernateTemplate;
  }
  
  // PUBLIC API
  
  @SuppressWarnings("unchecked")
  public Security getSecurity(final Date now, final DomainSpecificIdentifier identifier, final boolean populateWithOtherIdentifiers) {
    return (Security)_hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        HibernateSecurityMasterSession secMasterSession = new HibernateSecurityMasterSession(session);
        SecurityBean security = secMasterSession.getSecurityBean(now, identifier);
        // we use the DefaultSecurity interface because we need access to setIdentifiers
        if (security != null) {
          final DefaultSecurity result = (DefaultSecurity)getBeanOperation (security).createSecurity (identifier, security);
          final List<DomainSpecificIdentifier> identifiers = new ArrayList<DomainSpecificIdentifier>();
          if (populateWithOtherIdentifiers) {
            System.err.println("First version security id = "+security.getFirstVersion().getId());
            Query identifierQuery = session.getNamedQuery("DomainSpecificIdentifierAssociationBean.many.byDateSecurity");
            identifierQuery.setParameter("security", security.getFirstVersion());
            identifierQuery.setDate("now", now);
            List<DomainSpecificIdentifierAssociationBean> otherIdentifiers = identifierQuery.list();
            for (DomainSpecificIdentifierAssociationBean associationBean : otherIdentifiers) {
              identifiers.add(Converters.domainSpecificIdentifierBeanToDomainSpecificIdentifier(associationBean.getDomainSpecificIdentifier()));
            }
          } else {
            identifiers.add(identifier);
          }
          result.setIdentifiers(identifiers);
          return result;
        }
        return null;
      }
    });
  }
  
  public void persistSecurity (final Date now, final Security security) {
    _hibernateTemplate.execute (new HibernateCallback () {
      @Override
      public Object doInHibernate (final Session session) throws HibernateException, SQLException {
        HibernateSecurityMasterSession secMasterSession = new HibernateSecurityMasterSession(session);
        BeanOperation<Security,SecurityBean> beanOperation = getBeanOperation (security);
        SecurityBean bean = secMasterSession.getSecurityBean(now, security.getIdentifiers());
        if (bean == null) {
          bean = secMasterSession.createSecurityBean (beanOperation, now, false, now, null, null, security);
          for (DomainSpecificIdentifier identifier : security.getIdentifiers ()) {
            secMasterSession.associateOrUpdateDomainSpecificIdentifierWithSecurity (now, identifier, bean);
          }
        } else if (beanOperation.getBeanClass ().isAssignableFrom (bean.getClass ())) {
          if (beanOperation.beanEquals (bean, security)) {
            // security is the same as the one in the database - no action
          } else {
            // create a new version of the object in the database
            secMasterSession.createSecurityBean (beanOperation, now, false, now, MODIFIED_BY, bean, security);
          }
        } else {
          throw new OpenGammaRuntimeException ("SecurityBean of unexpected type " + bean);
        }
        return null;
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
  
  @Override
  public Set<String> getAllSecurityTypes() {
    return SUPPORTED_SECURITY_TYPES;
  }

  // TODO: consider if this needs to take a date
  @Override
  public Collection<Security> getSecurities(SecurityKey secKey) {
    Collection<DomainSpecificIdentifier> identifiers = secKey.getIdentifiers();
    Collection<Security> results = new HashSet<Security>();
    for (DomainSpecificIdentifier dsi : identifiers) {
      Security security = getSecurity(new Date(), dsi, true);
      if (security != null) {
        results.add(security);
      }
    }
    return results;
  }

  // TODO: consider if this needs to take a date
  @Override
  public Security getSecurity(SecurityKey secKey) {
    Collection<DomainSpecificIdentifier> identifiers = secKey.getIdentifiers();
    for (DomainSpecificIdentifier dsi : identifiers) {
      Security security = getSecurity(new Date(), dsi, true);
      if (security != null) {
        return security;
      }
    }
    return null;
  }
  
  // TODO: remove this once we've got rid of the string Bloomberg only identities floating around.
  @Override
  public Security getSecurity(String identityKey) {
    return getSecurity(new Date(), new DomainSpecificIdentifier(DEFAULT_DOMAIN, identityKey), true);
  }

}
 
