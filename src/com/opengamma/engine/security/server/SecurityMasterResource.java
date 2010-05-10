/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security.server;

import static com.opengamma.engine.security.server.SecurityMasterServiceNames.SECURITYMASTER_ALLSECURITYTYPES;
import static com.opengamma.engine.security.server.SecurityMasterServiceNames.SECURITYMASTER_SECURITIES;
import static com.opengamma.engine.security.server.SecurityMasterServiceNames.SECURITYMASTER_SECURITY;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;

/**
 * REST resource wrapper for a {@link SecurityMaster}.
 */
public class SecurityMasterResource {
  
  private final FudgeContext _fudgeContext;
  private final SecurityMaster _securityMaster;
  
  public SecurityMasterResource (final FudgeContext fudgeContext, final SecurityMaster securityMaster) {
    _fudgeContext = fudgeContext;
    _securityMaster = securityMaster;
  }
  
  protected FudgeContext getFudgeContext () {
    return _fudgeContext;
  }
  
  protected SecurityMaster getSecurityMaster () {
    return _securityMaster;
  }
  
  protected FudgeSerializationContext getFudgeSerializationContext () {
    return new FudgeSerializationContext (getFudgeContext ());
  }
  
  @GET
  @Path ("allSecurityTypes")
  public FudgeMsgEnvelope getAllSecurityTypes () {
    final FudgeSerializationContext context = getFudgeSerializationContext ();
    final MutableFudgeFieldContainer msg = context.newMessage ();
    context.objectToFudgeMsg (msg, SECURITYMASTER_ALLSECURITYTYPES, null, getSecurityMaster ().getAllSecurityTypes ());
    return new FudgeMsgEnvelope (msg);
  }
  
  @Path ("security")
  public IdentifierSelectResource security () {
    return new IdentifierSelectResource (new IdentifierSelectResource.Callback () {
      
      private FudgeMsgEnvelope returnSecurity (final Security security) {
        if (security != null) {
          final FudgeSerializationContext context = getFudgeSerializationContext ();
          final MutableFudgeFieldContainer msg = context.newMessage ();
          context.objectToFudgeMsg (msg, SECURITYMASTER_SECURITY, null, security);
          return new FudgeMsgEnvelope (msg);
        } else {
          return null;
        }
      }
      
      @Override
      protected FudgeMsgEnvelope getByIdentifier (final Identifier identifier) {
        System.out.println ("security.getByIdentifier: " + identifier);
        return returnSecurity (getSecurityMaster ().getSecurity (identifier));
      }
      
      @Override
      protected FudgeMsgEnvelope getByBundle (final IdentifierBundle bundle) {
        System.out.println ("security.getByBundle: " + bundle);
        return returnSecurity (getSecurityMaster ().getSecurity (bundle));
      }
      
    });
  }
  
  @Path ("securities")
  public IdentifierSelectResource securities () {
    return new IdentifierSelectResource (new IdentifierSelectResource.Callback () {
      
      @Override
      protected FudgeMsgEnvelope getByBundle (final IdentifierBundle bundle) {
        System.out.println ("securities.getByBundle: " + bundle);
        final FudgeSerializationContext context = getFudgeSerializationContext ();
        final MutableFudgeFieldContainer msg = context.newMessage ();
        context.objectToFudgeMsg (msg, SECURITYMASTER_SECURITIES, null, getSecurityMaster ().getSecurities (bundle));
        return new FudgeMsgEnvelope (msg);
      }
      
    });
  }
  
}