/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.engine.cache.msg;

/**
 * Visitor to the {@link CacheMessage} messages.
 */
public abstract class CacheMessageVisitor {

  protected abstract <T extends CacheMessage> T visitUnexpectedMessage(CacheMessage message);

  protected <T extends CacheMessage> T visitBinaryDataStoreMessage(final CacheMessage message) {
    return this.<T>visitUnexpectedMessage(message);
  }

  protected <T extends CacheMessage> T visitIdentifierMapMessage(final CacheMessage message) {
    return this.<T>visitUnexpectedMessage(message);
  }

  protected CacheMessage visitDeleteRequest(final DeleteRequest message) {
    return visitBinaryDataStoreMessage(message);
  }
  
  protected CacheMessage visitFindMessage(final FindMessage message) {
    return visitBinaryDataStoreMessage(message);
  }

  protected GetResponse visitGetRequest(final GetRequest message) {
    return visitBinaryDataStoreMessage(message);
  }

  protected CacheMessage visitGetResponse(final GetResponse message) {
    return visitBinaryDataStoreMessage(message);
  }

  protected IdentifierLookupResponse visitIdentifierLookupRequest(final IdentifierLookupRequest message) {
    return visitIdentifierMapMessage(message);
  }

  protected CacheMessage visitIdentifierLookupResponse(final IdentifierLookupResponse message) {
    return visitIdentifierMapMessage(message);
  }

  protected CacheMessage visitPutRequest(final PutRequest message) {
    return visitBinaryDataStoreMessage(message);
  }

  protected CacheMessage visitReleaseCacheMessage(final ReleaseCacheMessage message) {
    return visitBinaryDataStoreMessage(message);
  }

  protected CacheMessage visitSlaveChannelMessage(final SlaveChannelMessage message) {
    return visitBinaryDataStoreMessage(message);
  }

  protected SpecificationLookupResponse visitSpecificationLookupRequest(final SpecificationLookupRequest message) {
    return visitIdentifierMapMessage(message);
  }

  protected CacheMessage visitSpecificationLookupResponse(final SpecificationLookupResponse message) {
    return visitIdentifierMapMessage(message);
  }

}
