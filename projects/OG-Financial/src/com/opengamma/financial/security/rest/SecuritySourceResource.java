/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.rest;

import static com.opengamma.financial.security.rest.SecuritySourceServiceNames.SECURITYSOURCE_SECURITY;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.Sets;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.FinancialSecuritySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.ObjectsPairFudgeBuilder;
import com.opengamma.util.tuple.Pair;

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
   * RESTful method to get a security by unique identifier.
   * 
   * @param uidStr  the unique identifier from the URI, not null
   * @return the security, null if not found
   */
  @GET
  @Path("security/{uid}")
  public FudgeMsgEnvelope getSecurity(@PathParam("uid") String uidStr) {
    final UniqueId uid = UniqueId.parse(uidStr);
    final FudgeSerializer serializer = new FudgeSerializer(getFudgeContext());
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessageWithClassHeaders(msg, SECURITYSOURCE_SECURITY, null, getSecuritySource().getSecurity(uid), Security.class);
    return new FudgeMsgEnvelope(msg);
  }
  
  /**
   * RESTful method to get bulk securities by unique identifiers
   * 
   * @param uidStrs  the unique identifiers from the URI, not null
   * @return the securities, null if not found
   */
  @GET
  @Path("securities/uid")
  public FudgeMsgEnvelope getSecurities(@QueryParam("uids") List<String> uidStrs) {
    ArgumentChecker.notEmpty(uidStrs, "identifiers");
    Set<UniqueId> uniqueIds = Sets.newHashSet();
    
    for (String uidStr : uidStrs) {
      uniqueIds.add(UniqueId.parse(uidStr));
    }
    final FudgeSerializer serializer = new FudgeSerializer(getFudgeContext());
    final MutableFudgeMsg msg = serializer.newMessage();
    Map<UniqueId, Security> securityMap = getSecuritySource().getSecurity(uniqueIds);
    for (Entry<UniqueId, Security> entry : securityMap.entrySet()) {
      MutableFudgeMsg pairMsg = ObjectsPairFudgeBuilder.buildMessage(serializer, Pair.of(entry.getKey(), entry.getValue()), UniqueId.class, Security.class);
      serializer.addToMessageWithClassHeaders(msg, SECURITYSOURCE_SECURITY, null, pairMsg);
    }
    return new FudgeMsgEnvelope(msg);
  }

  /**
   * RESTful method to get a security by object identifier and version-correction.
   * 
   * @param oidStr  the object identifier from the URI, not null
   * @param versionAsOfStr  the version as of string from the URI, not null
   * @param correctedToStr  the corrected to string from the URI, not null
   * @return the security, null if not found
   */
  @GET
  @Path("security/{oid}/{versionAsOf}/{correctedTo}")
  public FudgeMsgEnvelope getSecurity(@PathParam("oid") String oidStr, @PathParam("versionAsOf") String versionAsOfStr, @PathParam("correctedTo") String correctedToStr) {
    final ObjectId oid = ObjectId.parse(oidStr);
    final VersionCorrection versionCorrection = VersionCorrection.parse(versionAsOfStr, correctedToStr);
    final FudgeSerializer serializer = new FudgeSerializer(getFudgeContext());
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessageWithClassHeaders(msg, SECURITYSOURCE_SECURITY, null, getSecuritySource().getSecurity(oid, versionCorrection));
    return new FudgeMsgEnvelope(msg);
  }

  /**
   * RESTful method to get securities by identifier bundle.
   * 
   * @param idStrs  the identifiers from the URI, not null
   * @param versionAsOfStr  the version as of string from the URI
   * @param correctedToStr  the corrected to string from the URI
   * @return the securities, null if not found
   */
  @GET
  @Path("securities")
  public FudgeMsgEnvelope getSecurities(@QueryParam("id") List<String> idStrs, @QueryParam("versionAsOf") String versionAsOfStr, @QueryParam("correctedTo") String correctedToStr) {
    ArgumentChecker.notEmpty(idStrs, "identifiers");
    ExternalIdBundle bundle = ExternalIdBundle.EMPTY;
    for (String idStr : idStrs) {
      bundle = bundle.withExternalId(ExternalId.parse(idStr));
    }
    final FudgeSerializer serializer = new FudgeSerializer(getFudgeContext());
    final MutableFudgeMsg msg = serializer.newMessage();
    final Collection<Security> securities;
    if (versionAsOfStr == null && correctedToStr == null) {
      securities = getSecuritySource().getSecurities(bundle);
    } else {
      VersionCorrection versionCorrection = VersionCorrection.parse(versionAsOfStr, correctedToStr);
      securities = getSecuritySource().getSecurities(bundle, versionCorrection);
    }
    for (Security security : securities) {
      serializer.addToMessageWithClassHeaders(msg, SECURITYSOURCE_SECURITY, null, security, Security.class);
    }
    return new FudgeMsgEnvelope(msg);
  }

  /**
   * RESTful method to get a security by identifier bundle.
   * 
   * @param idStrs  the identifiers from the URI, not null
   * @param versionAsOfStr  the version as of string from the URI
   * @param correctedToStr  the corrected to string from the URI
   * @return the security, null if not found
   */
  @GET
  @Path("securities/security")
  public FudgeMsgEnvelope getSecurity(@QueryParam("id") List<String> idStrs, @QueryParam("versionAsOf") String versionAsOfStr, @QueryParam("correctedTo") String correctedToStr) {
    ArgumentChecker.notEmpty(idStrs, "identifiers");
    ExternalIdBundle bundle = ExternalIdBundle.EMPTY;
    for (String idStr : idStrs) {
      bundle = bundle.withExternalId(ExternalId.parse(idStr));
    }
    final FudgeSerializer serializer = new FudgeSerializer(getFudgeContext());
    final MutableFudgeMsg msg = serializer.newMessage();
    final Security security;
    if (versionAsOfStr == null && correctedToStr == null) {
      security = getSecuritySource().getSecurity(bundle);
    } else {
      VersionCorrection versionCorrection = VersionCorrection.parse(versionAsOfStr, correctedToStr);
      security = getSecuritySource().getSecurity(bundle, versionCorrection);
    }
    serializer.addToMessageWithClassHeaders(msg, SECURITYSOURCE_SECURITY, null, security, Security.class);
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
    final FudgeSerializer serializer = new FudgeSerializer(getFudgeContext());
    final MutableFudgeMsg msg = serializer.newMessage();
    final Collection<Security> securities = getSecuritySource().getBondsWithIssuerName(issuerName);
    for (Security security : securities) {
      serializer.addToMessageWithClassHeaders(msg, SECURITYSOURCE_SECURITY, null, security, Security.class);
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
