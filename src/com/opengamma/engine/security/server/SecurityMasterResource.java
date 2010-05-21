/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security.server;

import static com.opengamma.engine.security.server.SecurityMasterServiceNames.SECURITYMASTER_ALLSECURITYTYPES;
import static com.opengamma.engine.security.server.SecurityMasterServiceNames.SECURITYMASTER_SECURITIES;
import static com.opengamma.engine.security.server.SecurityMasterServiceNames.SECURITYMASTER_SECURITY;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * REST resource wrapper for a {@link SecurityMaster}.
 */
public class SecurityMasterResource {

  /**
   * The Fudge context being used.
   */
  private final FudgeContext _fudgeContext;
  /**
   * The underlying security master.
   */
  private final SecurityMaster _securityMaster;

  /**
   * Creates a resource to expose a security master over REST.
   * @param fudgeContext  the context, not null
   * @param securityMaster  the security master, not null
   */
  public SecurityMasterResource(final FudgeContext fudgeContext, final SecurityMaster securityMaster) {
    ArgumentChecker.notNull(fudgeContext, "fudge context");
    ArgumentChecker.notNull(securityMaster, "security master");
    _fudgeContext = fudgeContext;
    _securityMaster = securityMaster;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the Fudge context.
   * @return the context, not null
   */
  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Gets the security master.
   * @return the security master, not null
   */
  protected SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the serialization context derived from the main Fudge context.
   * @return the context, not null
   */
  protected FudgeSerializationContext getFudgeSerializationContext() {
    return new FudgeSerializationContext(getFudgeContext());
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("securities/security/{uid}")
  public FudgeMsgEnvelope getSecurity(@PathParam("uid") String uidStr) {
    final UniqueIdentifier uid = UniqueIdentifier.parse(uidStr);
    final FudgeSerializationContext context = getFudgeSerializationContext();
    final MutableFudgeFieldContainer msg = context.newMessage();
    context.objectToFudgeMsg(msg, SECURITYMASTER_SECURITY, null, getSecurityMaster().getSecurity(uid));
    return new FudgeMsgEnvelope(msg);
  }

  @GET
  @Path("securities/security")
  public FudgeMsgEnvelope getSecurity(@QueryParam("id") List<String> idStrs) {
    ArgumentChecker.notEmpty(idStrs, "identifiers");
    IdentifierBundle bundle = new IdentifierBundle();
    for (String idStr : idStrs) {
      bundle = bundle.withIdentifier(Identifier.parse(idStr));
    }
    final FudgeSerializationContext context = getFudgeSerializationContext();
    final MutableFudgeFieldContainer msg = context.newMessage();
    context.objectToFudgeMsg(msg, SECURITYMASTER_SECURITY, null, getSecurityMaster().getSecurity(bundle));
    return new FudgeMsgEnvelope(msg);
  }

  @GET
  @Path("securities")
  public FudgeMsgEnvelope getSecurities(@QueryParam("id") List<String> idStrs) {
    ArgumentChecker.notEmpty(idStrs, "identifiers");
    IdentifierBundle bundle = new IdentifierBundle();
    for (String idStr : idStrs) {
      bundle = bundle.withIdentifier(Identifier.parse(idStr));
    }
    final FudgeSerializationContext context = getFudgeSerializationContext();
    final MutableFudgeFieldContainer msg = context.newMessage();
    context.objectToFudgeMsg(msg, SECURITYMASTER_SECURITIES, null, getSecurityMaster().getSecurities(bundle));
    return new FudgeMsgEnvelope(msg);
  }

  @GET
  @Path("securities/types")
  public FudgeMsgEnvelope getAllSecurityTypes() {
    final FudgeSerializationContext context = getFudgeSerializationContext();
    final MutableFudgeFieldContainer msg = context.newMessage();
    context.objectToFudgeMsg(msg, SECURITYMASTER_ALLSECURITYTYPES, null, getSecurityMaster().getAllSecurityTypes());
    return new FudgeMsgEnvelope(msg);
  }

}
