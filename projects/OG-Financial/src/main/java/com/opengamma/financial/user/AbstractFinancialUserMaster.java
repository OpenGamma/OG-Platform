/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.ObjectId;
import com.opengamma.master.AbstractChangeProvidingMaster;
import com.opengamma.master.AbstractDocument;

/**
 * Base class for wrapping masters to trap calls to record user based information,
 * allowing clean up and hooks for access control logics if needed.
 * 
 * @param <D>  the type of the document
 */
public abstract class AbstractFinancialUserMaster<D extends AbstractDocument> implements AbstractChangeProvidingMaster<D> {

  /**
   * The user name.
   */
  private final String _userName;
  /**
   * The client name.
   */
  private final String _clientName;
  /**
   * The tracker.
   */
  private final FinancialUserDataTracker _tracker;
  /**
   * The data type.
   */
  private final FinancialUserDataType _type;


  private void setupChangeListener() {
    changeManager().addChangeListener(new ChangeListener() {
      @Override
      public void entityChanged(ChangeEvent event) {
        if (event.getType().equals(ChangeType.REMOVED)) {
          _tracker.deleted(_userName, _clientName, _type, event.getObjectId());
        } else if (event.getType().equals(ChangeType.ADDED)) {
          _tracker.created(_userName, _clientName, _type, event.getObjectId());
        }
      }
    });
  }

  /**
   * Creates an instance.
   *
   * @param userName  the user name, not null
   * @param clientName  the client name, not null
   * @param tracker  the tracker, not null
   * @param type  the data type, not null
   */
  public AbstractFinancialUserMaster(String userName, String clientName, FinancialUserDataTracker tracker, FinancialUserDataType type) {
    _userName = userName;
    _clientName = clientName;
    _tracker = tracker;
    _type = type;
  }
  
  protected void init() {
    setupChangeListener();
  }

  /**
   * Creates an instance.
   *
   * @param client  the client, not null
   * @param type  the data type, not null
   */
  public AbstractFinancialUserMaster(FinancialClient client, FinancialUserDataType type) {
    _userName = client.getUserName();
    _clientName = client.getClientName();
    _tracker = client.getUserDataTracker();
    _type = type;
  }

  //-------------------------------------------------------------------------
  protected void created(ObjectId oid) {
    _tracker.created(_userName, _clientName, _type, oid);
  }

  protected void deleted(ObjectId oid) {
    _tracker.deleted(_userName, _clientName, _type, oid);
  }

}
