package com.opengamma.financial.security.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
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
import com.opengamma.financial.security.EquitySecurity;
import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.IdentificationDomain;

//import com.opengamma.engine.security.Security;
//import com.opengamma.engine.security.SecurityKey;
//import com.opengamma.engine.security.SecurityMaster;

public class HibernateSecurityMaster implements SecurityMaster {
  private static final IdentificationDomain DEFAULT_DOMAIN = new IdentificationDomain("BLOOMBERG");
  private static final Set<String> SUPPORTED_SECURITY_TYPES = new HashSet<String>();
  protected static final String MODIFIED_BY = "";
  static {
    SUPPORTED_SECURITY_TYPES.add("EQUITY");
  }
  private Logger s_logger = LoggerFactory.getLogger(HibernateSecurityMaster.class);
  private HibernateTemplate _hibernateTemplate = null;
  
  public void setSessionFactory(SessionFactory sessionFactory) {
    _hibernateTemplate = new HibernateTemplate(sessionFactory);
  }
  
  // for unit testing
  /*package*/ HibernateTemplate getHibernateTemplate() {
    return _hibernateTemplate;
  }
  
  // UTILITY METHODS
  
  // implicit assumption here that either the session to which the currency bean is attached is still open or it is fully initialized.
  private Currency currencyBeanToCurrency(CurrencyBean currencyBean) {
    return Currency.getInstance(currencyBean.getName());
  }
  // same again
  private DomainSpecificIdentifier domainSpecificIdentifierBeanToDomainSpecificIdentifier(DomainSpecificIdentifierBean domainSpecificIdentifierBean) {
    return new DomainSpecificIdentifier(domainSpecificIdentifierBean.getDomain(), domainSpecificIdentifierBean.getIdentifier());
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
          DefaultSecurity result = security.accept(new SecurityBeanVisitor<DefaultSecurity>() {
            @Override
            public DefaultSecurity visitEquitySecurityBean(EquitySecurityBean security) {
              EquitySecurity result = new EquitySecurity();
              result.setCompanyName(security.getCompanyName());
              result.setCurrency(currencyBeanToCurrency(security.getCurrency()));
              result.setExchange(security.getExchange().getName());
              result.setTicker(identifier.getValue());
              result.setIdentityKey(identifier.getValue());
              return result;
            }

            @Override
            public DefaultSecurity visitBondSecurityBean(
                BondSecurityBean security) {
              // TODO Auto-generated method stub
              return null;
            }

            @Override
            public DefaultSecurity visitEquityOptionSecurityBean(
                EquityOptionSecurityBean security) {
              // TODO Auto-generated method stub
              return null;
            }

            @Override
            public DefaultSecurity visitFutureSecurityBean(
                FutureSecurityBean security) {
              // TODO Auto-generated method stub
              return null;
            }
          });
          final List<DomainSpecificIdentifier> identifiers = new ArrayList<DomainSpecificIdentifier>();
          if (populateWithOtherIdentifiers) {
            System.err.println("First version security id = "+security.getFirstVersion().getId());
            Query identifierQuery = session.getNamedQuery("DomainSpecificIdentifierAssociationBean.many.byDateSecurity");
            identifierQuery.setParameter("security", security.getFirstVersion());
            identifierQuery.setDate("now", now);
            List<DomainSpecificIdentifierAssociationBean> otherIdentifiers = identifierQuery.list();
            for (DomainSpecificIdentifierAssociationBean associationBean : otherIdentifiers) {
              identifiers.add(domainSpecificIdentifierBeanToDomainSpecificIdentifier(associationBean.getDomainSpecificIdentifier()));
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
  
  public void persistEquitySecurity(final Date now, final EquitySecurity equitySecurity) {
    _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        HibernateSecurityMasterSession secMasterSession = new HibernateSecurityMasterSession(session);
        List<DomainSpecificIdentifierAssociationBean> allAssociations = secMasterSession.getAllAssociations();
        System.err.println(allAssociations);
        SecurityBean security = secMasterSession.getSecurityBean(now, equitySecurity.getIdentifiers());
        if (security == null) {
          // try and minimize the number of queries by grouping DSIDs into domain sets that are passed in as lists.
          EquitySecurityBean equity = secMasterSession.persistEquitySecurityBean(now, equitySecurity);
          Collection<DomainSpecificIdentifier> identifiers = equitySecurity.getIdentifiers();
          for (DomainSpecificIdentifier identifier : identifiers) {
            secMasterSession.associateOrUpdateDomainSpecificIdentifierWithSecurity(now, identifier, equity.getFirstVersion()); //associate all the identifiers with the first version.
          }
        } else if (security instanceof EquitySecurityBean) {
          EquitySecurityBean equity = (EquitySecurityBean) security;
          if (ObjectUtils.equals(equity.getCompanyName(), equitySecurity.getCompanyName()) &&
              ObjectUtils.equals(currencyBeanToCurrency(equity.getCurrency()), equitySecurity.getCurrency()) &&
              ObjectUtils.equals(equity.getExchange(), equitySecurity.getExchange())) {
            // they're the same, so we don't need to do anything except check the associations are up to date.
          } else {
            secMasterSession.createEquitySecurityBean(now, false, now, MODIFIED_BY, equity, 
                                                      secMasterSession.getOrCreateExchangeBean(equitySecurity.getExchange(), ""), 
                                                      equitySecurity.getCompanyName(), 
                                                      secMasterSession.getOrCreateCurrencyBean(equitySecurity.getCurrency().getISOCode()));
          }
        } else {
          throw new OpenGammaRuntimeException("SecurityBean of unexpected type:"+security);
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
            currencies.add(currencyBeanToCurrency(currencyBean));
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
 
