/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.rest;

import static com.opengamma.financial.security.rest.SecurityMasterServiceNames.SECURITYMASTER_HISTORIC;
import static com.opengamma.financial.security.rest.SecurityMasterServiceNames.SECURITYMASTER_METADATA;
import static com.opengamma.financial.security.rest.SecurityMasterServiceNames.SECURITYMASTER_SEARCH;
import static com.opengamma.financial.security.rest.SecurityMasterServiceNames.SECURITYMASTER_SECURITY;

import javax.time.Instant;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMetaDataRequest;
import com.opengamma.master.security.SecurityMetaDataResult;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource publishing details from a {@link SecurityMaster}.
 */
public class SecurityMasterResource {

  private SecurityMaster _securityMaster;

  private FudgeContext _fudgeContext;

  public SecurityMasterResource(final SecurityMaster securityMaster, final FudgeContext fudgeContext) {
    setSecurityMaster(securityMaster);
    setFudgeContext(fudgeContext);
  }

  public void setSecurityMaster(final SecurityMaster securityMaster) {
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    _securityMaster = securityMaster;
  }

  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  public void setFudgeContext(final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeContext = fudgeContext;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  public FudgeSerializationContext getFudgeSerializationContext() {
    return new FudgeSerializationContext(getFudgeContext());
  }

  public FudgeDeserializationContext getFudgeDeserializationContext() {
    return new FudgeDeserializationContext(getFudgeContext());
  }

  /**
   * 
   */
  public class IdentifiedSecurityResource {

    private final UniqueIdentifier _uniqueId;

    public IdentifiedSecurityResource(final UniqueIdentifier uid) {
      _uniqueId = uid;
    }

    @GET
    public FudgeMsgEnvelope get(@QueryParam("versionAsOf") String versionAsOf, @QueryParam("correctedTo") String correctedTo) {
      try {
        if (_uniqueId.isVersioned()) {
          final SecurityDocument document = getSecurityMaster().get(_uniqueId);
          return new FudgeMsgEnvelope(getFudgeSerializationContext().objectToFudgeMsg(document));
        } else {
          Instant v = (versionAsOf != null ? Instant.parse(versionAsOf) : null);
          Instant c = (correctedTo != null ? Instant.parse(correctedTo) : null);
          final SecurityDocument document = getSecurityMaster().get(_uniqueId, VersionCorrection.of(v, c));
          return new FudgeMsgEnvelope(getFudgeSerializationContext().objectToFudgeMsg(document));
        }
      } catch (DataNotFoundException e) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
    }

    @PUT
    public FudgeMsgEnvelope correct(final FudgeMsgEnvelope payload) {
      SecurityDocument document = getFudgeDeserializationContext().fudgeMsgToObject(SecurityDocument.class, payload.getMessage());
      document = getSecurityMaster().correct(document);
      final UniqueIdentifier uid = document.getUniqueId();
      if (uid == null) {
        return FudgeContext.EMPTY_MESSAGE_ENVELOPE;
      } else {
        return new FudgeMsgEnvelope(uid.toFudgeMsg(getFudgeContext()));
      }
    }

    @POST
    public FudgeMsgEnvelope update(final FudgeMsgEnvelope payload) {
      SecurityDocument document = getFudgeDeserializationContext().fudgeMsgToObject(SecurityDocument.class, payload.getMessage());
      document = getSecurityMaster().update(document);
      final UniqueIdentifier uid = document.getUniqueId();
      if (uid == null) {
        return FudgeContext.EMPTY_MESSAGE_ENVELOPE;
      } else {
        return new FudgeMsgEnvelope(uid.toFudgeMsg(getFudgeContext()));
      }
    }

    @DELETE
    public void remove() {
      getSecurityMaster().remove(_uniqueId);
    }

  }

  /**
   * 
   */
  public class SecurityResource {

    @POST
    public FudgeMsgEnvelope add(final FudgeMsgEnvelope payload) {
      SecurityDocument document = getFudgeDeserializationContext().fudgeMsgToObject(SecurityDocument.class, payload.getMessage());
      document = getSecurityMaster().add(document);
      return new FudgeMsgEnvelope(document.getUniqueId().toFudgeMsg(getFudgeContext()));
    }

    @Path("{uid}")
    public Object resource(@PathParam("uid") String uid) {
      final UniqueIdentifier uniqueId = UniqueIdentifier.parse(uid);
      return new IdentifiedSecurityResource(uniqueId);
    }

  }

  @Path(SECURITYMASTER_SECURITY)
  public SecurityResource securityResource() {
    return new SecurityResource();
  }

  @POST
  @Path(SECURITYMASTER_METADATA)
  public FudgeMsgEnvelope metaData(final FudgeMsgEnvelope payload) {
    final SecurityMetaDataRequest request = getFudgeDeserializationContext().fudgeMsgToObject(SecurityMetaDataRequest.class, payload.getMessage());
    final SecurityMetaDataResult result = getSecurityMaster().metaData(request);
    return new FudgeMsgEnvelope(getFudgeSerializationContext().objectToFudgeMsg(result));
  }

  @POST
  @Path(SECURITYMASTER_SEARCH)
  public FudgeMsgEnvelope search(final FudgeMsgEnvelope payload) {
    final SecuritySearchRequest request = getFudgeDeserializationContext().fudgeMsgToObject(SecuritySearchRequest.class, payload.getMessage());
    final SecuritySearchResult result = getSecurityMaster().search(request);
    return new FudgeMsgEnvelope(getFudgeSerializationContext().objectToFudgeMsg(result));
  }

  @POST
  @Path(SECURITYMASTER_HISTORIC)
  public FudgeMsgEnvelope history(final FudgeMsgEnvelope payload) {
    final SecurityHistoryRequest request = getFudgeDeserializationContext().fudgeMsgToObject(SecurityHistoryRequest.class, payload.getMessage());
    final SecurityHistoryResult result = getSecurityMaster().history(request);
    return new FudgeMsgEnvelope(getFudgeSerializationContext().objectToFudgeMsg(result));
  }

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
    message.add("securityMaster", getSecurityMaster().toString());
    return new FudgeMsgEnvelope(message);
  }

}
