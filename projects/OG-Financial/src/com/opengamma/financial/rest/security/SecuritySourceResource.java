/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.rest.security;

import static com.opengamma.financial.rest.security.SecuritySourceServiceNames.SECURITYSOURCE_SECURITY;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.core.security.Security;
import com.opengamma.financial.security.FinancialSecuritySource;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * REST resource wrapper for a {@link FinancialSecuritySource}.
 */
public class SecuritySourceResource {

  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;
  /**
   * The underlying security source.
   */
  private final FinancialSecuritySource _securitySource;

  /**
   * Creates a resource to expose a security source over REST.
   * 
   * @param fudgeContext  the context, not null
   * @param securitySource  the security source, not null
   */
  public SecuritySourceResource(final FudgeContext fudgeContext, final FinancialSecuritySource securitySource) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(securitySource, "securitySource");
    _fudgeContext = fudgeContext;
    _securitySource = securitySource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the Fudge context.
   * 
   * @return the context, not null
   */
  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Gets the security source.
   * 
   * @return the security source, not null
   */
  protected FinancialSecuritySource getSecuritySource() {
    return _securitySource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the serialization context derived from the main Fudge context.
   * 
   * @return the context, not null
   */
  protected FudgeSerializationContext getFudgeSerializationContext() {
    return new FudgeSerializationContext(getFudgeContext());
  }

  //-------------------------------------------------------------------------
  /**
   * RESTful method to get a security by unique identifier.
   * 
   * @param uidStr  the unique identifier from the URI, not null
   * @return the security, null if not found
   */
  @GET
  @Path("security/{uid}")
  public FudgeMsgEnvelope getSecurity(@PathParam("uid") String uidStr) {
    final UniqueIdentifier uid = UniqueIdentifier.parse(uidStr);
    final FudgeSerializationContext context = getFudgeSerializationContext();
    final MutableFudgeMsg msg = context.newMessage();
    context.objectToFudgeMsgWithClassHeaders(msg, SECURITYSOURCE_SECURITY, null, getSecuritySource().getSecurity(uid), Security.class);
    return new FudgeMsgEnvelope(msg);
  }

  /**
   * RESTful method to get securities by identifier bundle.
   * 
   * @param idStrs  the identifiers from the URI, not null
   * @return the securities, null if not found
   */
  @GET
  @Path("securities")
  public FudgeMsgEnvelope getSecurities(@QueryParam("id") List<String> idStrs) {
    ArgumentChecker.notEmpty(idStrs, "identifiers");
    IdentifierBundle bundle = IdentifierBundle.EMPTY;
    for (String idStr : idStrs) {
      bundle = bundle.withIdentifier(Identifier.parse(idStr));
    }
    final FudgeSerializationContext context = getFudgeSerializationContext();
    final MutableFudgeMsg msg = context.newMessage();
    final Collection<Security> securities = getSecuritySource().getSecurities(bundle);
    for (Security security : securities) {
      context.objectToFudgeMsgWithClassHeaders(msg, SECURITYSOURCE_SECURITY, null, security, Security.class);
    }
    return new FudgeMsgEnvelope(msg);
  }

  /**
   * RESTful method to get a security by identifier bundle.
   * 
   * @param idStrs  the identifiers from the URI, not null
   * @return the security, null if not found
   */
  @GET
  @Path("securities/security")
  public FudgeMsgEnvelope getSecurity(@QueryParam("id") List<String> idStrs) {
    ArgumentChecker.notEmpty(idStrs, "identifiers");
    IdentifierBundle bundle = IdentifierBundle.EMPTY;
    for (String idStr : idStrs) {
      bundle = bundle.withIdentifier(Identifier.parse(idStr));
    }
    final FudgeSerializationContext context = getFudgeSerializationContext();
    final MutableFudgeMsg msg = context.newMessage();
    context.objectToFudgeMsgWithClassHeaders(msg, SECURITYSOURCE_SECURITY, null, getSecuritySource().getSecurity(bundle), Security.class);
    return new FudgeMsgEnvelope(msg);
  }

  /**
   * RESTful method to get all bonds of a specific issuer type.
   * 
   * @param issuerName  the issuer type
   * @return the securities, null if not found
   */
  @GET
  @Path("bonds")
  public FudgeMsgEnvelope getBondsWithIssuerName(@QueryParam("issuerName") String issuerName) {
    ArgumentChecker.notEmpty(issuerName, "issuerName");
    final FudgeSerializationContext context = getFudgeSerializationContext();
    final MutableFudgeMsg msg = context.newMessage();
    final Collection<Security> securities = getSecuritySource().getBondsWithIssuerName(issuerName);
    for (Security security : securities) {
      context.objectToFudgeMsgWithClassHeaders(msg, SECURITYSOURCE_SECURITY, null, security, Security.class);
    }
    return new FudgeMsgEnvelope(msg);
  }

  //-------------------------------------------------------------------------
  /**
   * For debugging purposes only.
   * 
   * @return some debug information about the state of this resource object; e.g. which underlying objects is it connected to.
   */
  @GET
  @Path("debugInfo")
  public FudgeMsgEnvelope getDebugInfo() {
    final MutableFudgeMsg message = getFudgeContext().newMessage();
    message.add("fudgeContext", getFudgeContext().toString());
    message.add("securitySource", getSecuritySource().toString());
    return new FudgeMsgEnvelope(message);
  }

}
