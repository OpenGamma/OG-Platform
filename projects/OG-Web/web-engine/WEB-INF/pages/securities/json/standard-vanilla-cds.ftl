<#escape x as x?html>
<#include "security-header.ftl"> 
<#include "standard-cds.ftl"> 
  "coupon":"${security.coupon}",
  "cashSettlementDate":{"date": "${security.cashSettlementDate.toLocalDate()}", "zone": "${security.cashSettlementDate.zone}"},
  "adjustCashSettlementDate":"${security.adjustCashSettlementDate?string}",
<#include "security-footer.ftl"> 
</#escape>
