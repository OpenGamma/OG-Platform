/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch;

import com.opengamma.batch.domain.MarketData;
import com.opengamma.batch.domain.MarketDataValue;
import com.opengamma.batch.domain.RiskRun;
import com.opengamma.batch.rest.BatchRunSearchRequest;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.id.ObjectId;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.tuple.Pair;

import java.util.List;

/**
 * Interface combining BatchMaster and BatchRunWriter
 */
public interface BatchMasterWriter extends BatchMaster, BatchRunWriter{}
