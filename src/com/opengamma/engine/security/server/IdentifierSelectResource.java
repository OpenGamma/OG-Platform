/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fudgemsg.FudgeMsgEnvelope;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

public class IdentifierSelectResource {
  
  public static abstract class Callback {

    protected FudgeMsgEnvelope getByIdentifier (final UniqueIdentifier identifier) {
      throw new UnsupportedOperationException ("not supported");
    }
    
    protected FudgeMsgEnvelope getByBundle (final IdentifierBundle bundle) {
      throw new UnsupportedOperationException ("not supported");
    }
    
  }
  
  private final Callback _callback;
  private final List<Identifier> _identifiers;
  
  public IdentifierSelectResource (final Callback callback) {
    _callback = callback;
    _identifiers = Collections.emptyList ();
  }
  
  private IdentifierSelectResource (final Callback callback, final List<Identifier> identifiers, final Identifier identifier) {
    _callback = callback;
    _identifiers = new ArrayList<Identifier> (identifiers.size () + 1);
    _identifiers.addAll (identifiers);
    _identifiers.add (identifier);
  }
  
  private Callback getCallback () {
    return _callback;
  }
  
  private List<Identifier> getIdentifiers () {
    return _identifiers;
  }
  
  private Identifier getIdentifier () {
    return getIdentifiers ().get (0);
  }
  
  private IdentifierBundle getIdentifierBundle () {
    return new IdentifierBundle (getIdentifiers ());
  }
  
  @Path ("I/{scheme}/{identifier}")
  public IdentifierSelectResource addIdentifier (@PathParam("scheme") String scheme, @PathParam("identifier") String identifier) {
    return new IdentifierSelectResource (getCallback (), getIdentifiers (), new Identifier (scheme, identifier));
  }
  
  @GET
  @Path ("V")
  public FudgeMsgEnvelope get () {
    if (getIdentifiers ().size () == 1) {
      // TODO: separate UID from ID
      UniqueIdentifier uid = UniqueIdentifier.of(getIdentifier().getScheme().getName(), getIdentifier().getValue());
      return getCallback().getByIdentifier(uid);
    } else {
      return getCallback ().getByBundle (getIdentifierBundle ());
    }
  }
  
}