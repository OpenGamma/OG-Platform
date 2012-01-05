package com.opengamma.web.server.push.rest;

import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * REST resource that produces a JSON list of market data snapshots and their IDs.  This isn't a full REST
 * interface for market data snapshots, it's intended for populating data in the web client.
 */
@Path("marketdatasnapshots")
public class MarketDataSnapshotListResource {

  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataSnapshotListResource.class);
  private static final Pattern s_guidPattern =
      Pattern.compile("(\\{?([0-9a-fA-F]){8}-([0-9a-fA-F]){4}-([0-9a-fA-F]){4}-([0-9a-fA-F]){4}-([0-9a-fA-F]){12}\\}?)");

  private final MarketDataSnapshotMaster _snapshotMaster;

  public MarketDataSnapshotListResource(MarketDataSnapshotMaster snapshotMaster) {
    _snapshotMaster = snapshotMaster;
  }

  /**
   * @return JSON: {@code {<basisViewName>: {<unique ID>: <name>}}}
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getMarketDataSnapshotList() {
    MarketDataSnapshotSearchRequest snapshotSearchRequest = new MarketDataSnapshotSearchRequest();
    snapshotSearchRequest.setIncludeData(false);
    MarketDataSnapshotSearchResult snapshotSearchResult = _snapshotMaster.search(snapshotSearchRequest);
    List<ManageableMarketDataSnapshot> snapshots = snapshotSearchResult.getMarketDataSnapshots();

    Map<String, Map<String, String>> snapshotsByBasisView = new HashMap<String, Map<String, String>>();
    for (ManageableMarketDataSnapshot snapshot : snapshots) {
      if (snapshot.getUniqueId() == null) {
        s_logger.warn("Ignoring snapshot with null unique identifier {}", snapshot.getName());
        continue;
      }
      if (StringUtils.isBlank(snapshot.getName())) {
        s_logger.warn("Ignoring snapshot {} with no name", snapshot.getUniqueId());
        continue;
      }
      if (s_guidPattern.matcher(snapshot.getName()).find()) {
        s_logger.debug("Ignoring snapshot which appears to have an auto-generated name: {}", snapshot.getName());
        continue;
      }
      String basisViewName = snapshot.getBasisViewName() != null ? snapshot.getBasisViewName() : "unknown";
      Map<String, String> snapshotsForBasisView = snapshotsByBasisView.get(basisViewName);
      if (snapshotsForBasisView == null) {
        snapshotsForBasisView = new HashMap<String, String>();
        snapshotsByBasisView.put(basisViewName, snapshotsForBasisView);
      }
      snapshotsForBasisView.put(snapshot.getUniqueId().toString(), snapshot.getName());
    }
    return new JSONObject(snapshotsByBasisView).toString();
  }
}
