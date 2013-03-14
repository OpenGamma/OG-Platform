<#escape x as x?html>
<#include "security-header.ftl"> 
      "shortName":"${security.shortName}",
      "exchange":"${security.exchange}",
      "companyName":"${security.companyName}",
      "currency":"${security.currency}",
      "exchangeCode":"${security.exchangeCode}",
      "gicsCode":"${security.gicsCode}",
<#include "security-footer.ftl"> 
</#escape>
