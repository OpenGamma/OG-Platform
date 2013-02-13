package com.opengamma.integration.tool.portfolio.xml.v1_0;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class TradeRefAdapter extends XmlAdapter<TradeRef, Trade> {

  @Override
  public Trade unmarshal(TradeRef tradeRef) throws Exception {
    return tradeRef.getTrade();
  }

  @Override
  public TradeRef marshal(Trade trade) throws Exception {
    return new TradeRef(trade);
  }
}
