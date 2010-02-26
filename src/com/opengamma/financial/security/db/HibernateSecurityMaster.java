package com.opengamma.financial.security.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZoneOffset;
import javax.time.calendar.ZonedDateTime;

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
import com.opengamma.financial.GICSCode;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.FrequencyFactory;
import com.opengamma.financial.security.AmericanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.BondFutureSecurity;
import com.opengamma.financial.security.BondSecurity;
import com.opengamma.financial.security.CorporateBondSecurity;
import com.opengamma.financial.security.EquityOptionSecurity;
import com.opengamma.financial.security.EquitySecurity;
import com.opengamma.financial.security.EuropeanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.FXFutureSecurity;
import com.opengamma.financial.security.GovernmentBondSecurity;
import com.opengamma.financial.security.MunicipalBondSecurity;
import com.opengamma.financial.security.VanillaFutureSecurity;
import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.IdentificationDomain;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

//import com.opengamma.engine.security.Security;
//import com.opengamma.engine.security.SecurityKey;
//import com.opengamma.engine.security.SecurityMaster;

public class HibernateSecurityMaster implements SecurityMaster {
  private static final IdentificationDomain DEFAULT_DOMAIN = new IdentificationDomain("BLOOMBERG");
  private static final Set<String> SUPPORTED_SECURITY_TYPES = new HashSet<String>();
  protected static final String MODIFIED_BY = "";
  static {
    SUPPORTED_SECURITY_TYPES.add("BOND");
    SUPPORTED_SECURITY_TYPES.add("EQUITY");
    SUPPORTED_SECURITY_TYPES.add("EQUITYOPTION");
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
  
  private Expiry dateToExpiry(Date date) {
    final Calendar c = Calendar.getInstance ();
    c.setTime (date);
    return new Expiry (ZonedDateTime.fromInstant (OffsetDateTime.dateMidnight (c.get (Calendar.YEAR), c.get (Calendar.MONTH) + 1, c.get (Calendar.DAY_OF_MONTH), ZoneOffset.UTC), TimeZone.UTC));
  }
  
  private Date expiryToDate (Expiry expiry) {
    // we're storing just as a date, so assert that the value we're storing isn't a vague month or year
    if (expiry.getAccuracy () != null) {
      if (expiry.getAccuracy () != ExpiryAccuracy.DAY_MONTH_YEAR) throw new OpenGammaRuntimeException ("Expiry is not to DAY_MONTH_YEAR precision");
    }
    return new Date (expiry.toInstant ().toEpochMillis ());
  }
  
  private Frequency frequencyBeanToFrequency (final FrequencyBean frequencyBean) {
    final Frequency f = FrequencyFactory.INSTANCE.getFrequency (frequencyBean.getName ());
    if (f == null) throw new OpenGammaRuntimeException ("Bad value for frequencyBean (" + frequencyBean.getName () + ")");
    return f;
  }
  
  private DayCount dayCountBeanToDayCount (final DayCountBean dayCountBean) {
    final DayCount dc = DayCountFactory.INSTANCE.getDayCount (dayCountBean.getName ());
    if (dc == null) throw new OpenGammaRuntimeException ("Bad value for dayCountBean (" + dayCountBean.getName () + ")");
    return dc;
  }
  
  private BusinessDayConvention businessDayConventionBeanToBusinessDayConvention (final BusinessDayConventionBean businessDayConventionBean) {
    final BusinessDayConvention bdc = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention (businessDayConventionBean.getName ());
    if (bdc == null) throw new OpenGammaRuntimeException ("Bad value for businessDayConventionBean (" + businessDayConventionBean.getName () + ")");
    return bdc;
  }
  
  private GICSCode gicsCodeBeanToGICSCode (final GICSCodeBean gicsCodeBean) {
    return GICSCode.getInstance (gicsCodeBean.getName ());
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
              result.setGICSCode(gicsCodeBeanToGICSCode (security.getGICSCode ()));
              return result;
            }

            @Override
            public DefaultSecurity visitBondSecurityBean(
                BondSecurityBean security) {
              switch (security.getBondType ()) {
              case CORPORATE :
                return new CorporateBondSecurity (
                      dateToExpiry (security.getMaturity ()),
                      security.getCoupon (),
                      frequencyBeanToFrequency (security.getFrequency ()),
                      security.getCountry (),
                      security.getCreditRating (),
                      currencyBeanToCurrency (security.getCurrency ()),
                      security.getIssuer (),
                      dayCountBeanToDayCount (security.getDayCountConvention ()),
                      businessDayConventionBeanToBusinessDayConvention (security.getBusinessDayConvention ())
                    );
              case MUNICIPAL :
                return new MunicipalBondSecurity (
                    dateToExpiry (security.getMaturity ()),
                    security.getCoupon (),
                    frequencyBeanToFrequency (security.getFrequency ()),
                    security.getCountry (),
                    security.getCreditRating (),
                    currencyBeanToCurrency (security.getCurrency ()),
                    security.getIssuer (),
                    dayCountBeanToDayCount (security.getDayCountConvention ()),
                    businessDayConventionBeanToBusinessDayConvention (security.getBusinessDayConvention ())
                  );
              case GOVERNMENT :
                return new GovernmentBondSecurity (
                    dateToExpiry (security.getMaturity ()),
                    security.getCoupon (),
                    frequencyBeanToFrequency (security.getFrequency ()),
                    security.getCountry (),
                    security.getCreditRating (),
                    currencyBeanToCurrency (security.getCurrency ()),
                    security.getIssuer (),
                    dayCountBeanToDayCount (security.getDayCountConvention ()),
                    businessDayConventionBeanToBusinessDayConvention (security.getBusinessDayConvention ())
                  );
              default :
                throw new OpenGammaRuntimeException ("Bad value for bondSecurityType (" + security.getBondType () + ")");
              }
            }

            @Override
            public DefaultSecurity visitEquityOptionSecurityBean(
                EquityOptionSecurityBean security) {
              switch (security.getEquityOptionType ()) {
              case AMERICAN :
                return new AmericanVanillaEquityOptionSecurity (
                    security.getOptionType (),
                    security.getStrike (),
                    dateToExpiry (security.getExpiry ()),
                    security.getUnderlyingIdentityKey (),
                    currencyBeanToCurrency (security.getCurrency ()),
                    security.getExchange ().getName ()
                    );
              case EUROPEAN :
                return new EuropeanVanillaEquityOptionSecurity (
                    security.getOptionType (),
                    security.getStrike (),
                    dateToExpiry (security.getExpiry ()),
                    security.getUnderlyingIdentityKey (),
                    currencyBeanToCurrency (security.getCurrency ()),
                    security.getExchange ().getName ()
                    );
              default :
                throw new OpenGammaRuntimeException ("Bad value for equityOptionType (" + security.getEquityOptionType () + ")");
              }
            }

            @Override
            public DefaultSecurity visitFutureSecurityBean(
                FutureSecurityBean security) {
              switch (security.getFutureType ()) {
              case BOND :
                return new BondFutureSecurity (
                    dateToExpiry (security.getExpiry ()),
                    security.getMonth (),
                    security.getYear (),
                    security.getTradingExchange ().getName (),
                    security.getSettlementExchange ().getName ()
                    );
              case FX :
                return new FXFutureSecurity (
                    dateToExpiry (security.getExpiry ()),
                    security.getMonth (),
                    security.getYear (),
                    security.getTradingExchange ().getName (),
                    security.getSettlementExchange ().getName ()
                    );
              case VANILLA :
                return new VanillaFutureSecurity (
                    dateToExpiry (security.getExpiry ()),
                    security.getMonth (),
                    security.getYear (),
                    security.getTradingExchange ().getName (),
                    security.getSettlementExchange ().getName ()
                    );
              default :
                throw new OpenGammaRuntimeException ("Bad value for futureSecurityType (" + security.getFutureType () + ")");
              }
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
        SecurityBean security = secMasterSession.getSecurityBean(now, equitySecurity.getIdentifiers());
        if (security == null) {
          // try and minimize the number of queries by grouping DSIDs into domain sets that are passed in as lists.
          EquitySecurityBean equity = secMasterSession.persistEquitySecurityBean(now, equitySecurity);
          Collection<DomainSpecificIdentifier> identifiers = equitySecurity.getIdentifiers();
          for (DomainSpecificIdentifier identifier : identifiers) {
            secMasterSession.associateOrUpdateDomainSpecificIdentifierWithSecurity(now, identifier, equity);
          }
        } else if (security instanceof EquitySecurityBean) {
          EquitySecurityBean equity = (EquitySecurityBean) security;
          if (ObjectUtils.equals(equity.getCompanyName(), equitySecurity.getCompanyName()) &&
              ObjectUtils.equals(currencyBeanToCurrency(equity.getCurrency()), equitySecurity.getCurrency()) &&
              ObjectUtils.equals(equity.getExchange().getName (), equitySecurity.getExchange()) &&
              ObjectUtils.equals(gicsCodeBeanToGICSCode (equity.getGICSCode ()), equitySecurity.getGICSCode ())) {
            // they're the same, so we don't need to do anything except check the associations are up to date.
          } else {
            secMasterSession.createEquitySecurityBean(now, false, now, MODIFIED_BY, equity, 
                                                      secMasterSession.getOrCreateExchangeBean(equitySecurity.getExchange(), ""), 
                                                      equitySecurity.getCompanyName(), 
                                                      secMasterSession.getOrCreateCurrencyBean(equitySecurity.getCurrency().getISOCode()),
                                                      secMasterSession.getOrCreateGICSCodeBean(equitySecurity.getGICSCode ().toString (), ""));
          }
        } else {
          throw new OpenGammaRuntimeException("SecurityBean of unexpected type:"+security);
        }
        return null;
      }
    });
  }
  
  public void persistEquityOptionSecurity (final Date now, final EquityOptionSecurity equityOptionSecurity) {
    _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        HibernateSecurityMasterSession secMasterSession = new HibernateSecurityMasterSession(session);
        SecurityBean security = secMasterSession.getSecurityBean(now, equityOptionSecurity.getIdentifiers());
        if (security == null) {
          // try and minimize the number of queries by grouping DSIDs into domain sets that are passed in as lists.
          EquityOptionSecurityBean equity = secMasterSession.persistEquityOptionSecurityBean(now, equityOptionSecurity);
          Collection<DomainSpecificIdentifier> identifiers = equityOptionSecurity.getIdentifiers();
          for (DomainSpecificIdentifier identifier : identifiers) {
            secMasterSession.associateOrUpdateDomainSpecificIdentifierWithSecurity(now, identifier, equity);
          }
        } else if (security instanceof EquityOptionSecurityBean) {
          EquityOptionSecurityBean equity = (EquityOptionSecurityBean) security;
          if (ObjectUtils.equals(equity.getEquityOptionType (), EquityOptionType.identify (equityOptionSecurity)) &&
              ObjectUtils.equals(equity.getOptionType (), equityOptionSecurity.getOptionType ()) &&
              ObjectUtils.equals(equity.getStrike (), equityOptionSecurity.getStrike ()) &&
              ObjectUtils.equals(dateToExpiry (equity.getExpiry ()), equityOptionSecurity.getExpiry ()) &&
              ObjectUtils.equals(equity.getUnderlyingIdentityKey (), equityOptionSecurity.getUnderlyingIdentityKey ()) &&
              ObjectUtils.equals(currencyBeanToCurrency (equity.getCurrency ()), equityOptionSecurity.getCurrency ()) &&
              ObjectUtils.equals(equity.getExchange ().getName (), equityOptionSecurity.getExchange ())) {
            // they're the same, so we don't need to do anything except check the associations are up to date.
          } else {
            secMasterSession.createEquityOptionSecurityBean(now, false, now, MODIFIED_BY, equity,
                EquityOptionType.identify (equityOptionSecurity),
                equityOptionSecurity.getOptionType (),
                equityOptionSecurity.getStrike (),
                expiryToDate (equityOptionSecurity.getExpiry ()),
                equityOptionSecurity.getUnderlyingIdentityKey (),
                secMasterSession.getOrCreateCurrencyBean (equityOptionSecurity.getCurrency ().getISOCode ()),
                secMasterSession.getOrCreateExchangeBean (equityOptionSecurity.getExchange (), ""));
          }
        } else {
          throw new OpenGammaRuntimeException("SecurityBean of unexpected type:"+security);
        }
        return null;
      }
    });
  }
  
  public void persistBondSecurity (final Date now, final BondSecurity bondSecurity) {
    _hibernateTemplate.execute (new HibernateCallback () {
      @Override
      public Object doInHibernate (final Session session) throws HibernateException, SQLException {
        HibernateSecurityMasterSession secMasterSession = new HibernateSecurityMasterSession(session);
        SecurityBean security = secMasterSession.getSecurityBean(now, bondSecurity.getIdentifiers());
        if (security == null) {
          // try and minimize the number of queries by grouping DSIDs into domain sets that are passed in as lists.
          BondSecurityBean bond = secMasterSession.persistBondSecurityBean(now, bondSecurity);
          Collection<DomainSpecificIdentifier> identifiers = bondSecurity.getIdentifiers();
          for (DomainSpecificIdentifier identifier : identifiers) {
            secMasterSession.associateOrUpdateDomainSpecificIdentifierWithSecurity(now, identifier, bond);
          }
        } else if (security instanceof BondSecurityBean) {
          BondSecurityBean bond = (BondSecurityBean) security;
          if (ObjectUtils.equals (bond.getBondType (), BondType.identify (bondSecurity)) &&
              ObjectUtils.equals (dateToExpiry (bond.getMaturity ()), bondSecurity.getMaturity ()) &&
              ObjectUtils.equals (bond.getCoupon (), bondSecurity.getCoupon ()) &&
              ObjectUtils.equals (frequencyBeanToFrequency (bond.getFrequency ()), bondSecurity.getFrequency ()) &&
              ObjectUtils.equals (bond.getCountry (), bondSecurity.getCountry ()) &&
              ObjectUtils.equals (bond.getCreditRating (), bondSecurity.getCreditRating ()) &&
              ObjectUtils.equals (currencyBeanToCurrency (bond.getCurrency ()), bondSecurity.getCurrency ()) &&
              ObjectUtils.equals (bond.getIssuer (), bondSecurity.getIssuer ()) &&
              ObjectUtils.equals (dayCountBeanToDayCount (bond.getDayCountConvention ()), bondSecurity.getDayCountConvention ()) &&
              ObjectUtils.equals (businessDayConventionBeanToBusinessDayConvention (bond.getBusinessDayConvention ()), bondSecurity.getBusinessDayConvention ())) {
            // they're the same, so we don't need to do anything except check the associations are up to date.
          } else {
            secMasterSession.createBondSecurityBean(now, false, now, MODIFIED_BY, bond,
                BondType.identify (bondSecurity),
                expiryToDate (bondSecurity.getMaturity ()),
                bondSecurity.getCoupon (),
                secMasterSession.getOrCreateFrequencyBean (bondSecurity.getFrequency ().getConventionName ()),
                bondSecurity.getCountry (),
                bondSecurity.getCreditRating (),
                secMasterSession.getOrCreateCurrencyBean (bondSecurity.getCurrency ().getISOCode ()),
                bondSecurity.getIssuer (),
                secMasterSession.getOrCreateDayCountBean (bondSecurity.getDayCountConvention ().getConventionName ()),
                secMasterSession.getOrCreateBusinessDayConventionBean (bondSecurity.getBusinessDayConvention ().getConventionName ()));
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
 
