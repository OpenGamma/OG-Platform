/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.holiday.master.db;

import com.opengamma.financial.master.db.AbstractDbMasterWorker;
import com.opengamma.financial.world.holiday.master.HolidayDocument;
import com.opengamma.financial.world.holiday.master.HolidayHistoryRequest;
import com.opengamma.financial.world.holiday.master.HolidayHistoryResult;
import com.opengamma.financial.world.holiday.master.HolidayMaster;
import com.opengamma.financial.world.holiday.master.HolidaySearchRequest;
import com.opengamma.financial.world.holiday.master.HolidaySearchResult;
import com.opengamma.id.UniqueIdentifier;

/**
 * Base worker class for the holiday master.
 * <p>
 * This is designed to allow each holiday master method to be implemented by a
 * different class and easily replaced by an alternative.
 * Implementations are registered using {@link DbHolidayMasterWorkers}.
 * <p>
 * The API of this class follows {@link HolidayMaster}.
 * Each of the methods should be implemented as per the documentation on the master.
 * The parameters to the methods will be pre-checked for nulls before the worker is called,
 * including any internal required values in request or document objects.
 * <p>
 * This base implementation throws {@code UnsupportedOperationException} from each method.
 * As a result, subclasses only need to implement those methods they want to.
 */
public class DbHolidayMasterWorker extends AbstractDbMasterWorker<DbHolidayMaster> {

  /**
   * Creates an instance.
   */
  protected DbHolidayMasterWorker() {
  }

  /**
   * Initializes the instance.
   * @param master  the holiday master, not null
   */
  protected void init(final DbHolidayMaster master) {
    super.init(master);
  }

  //-------------------------------------------------------------------------
  protected HolidaySearchResult search(HolidaySearchRequest request) {
    throw new UnsupportedOperationException();
  }

  protected HolidayDocument get(final UniqueIdentifier uid) {
    throw new UnsupportedOperationException();
  }

  protected HolidayDocument add(HolidayDocument document) {
    throw new UnsupportedOperationException();
  }

  protected HolidayDocument update(HolidayDocument document) {
    throw new UnsupportedOperationException();
  }

  protected void remove(UniqueIdentifier uid) {
    throw new UnsupportedOperationException();
  }

  protected HolidayHistoryResult history(HolidayHistoryRequest request) {
    throw new UnsupportedOperationException();
  }

  protected HolidayDocument correct(HolidayDocument document) {
    throw new UnsupportedOperationException();
  }

}
