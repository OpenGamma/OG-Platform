<#escape x as x?html>
<#include "security-header.ftl"> 
<#include "standard-cds.ftl"> 
  "coupon":"${security.coupon}",
  "cashSettlementDate":"${security.cashSettlementDate.toLocalDate()}",
  "adjustCashSettlementDate":"${security.adjustCashSettlementDate?string}",
<#include "security-footer.ftl"> 
</#escape>
