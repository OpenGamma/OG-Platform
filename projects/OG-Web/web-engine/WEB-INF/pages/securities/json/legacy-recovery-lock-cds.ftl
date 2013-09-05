<#escape x as x?html>
<#include "security-header.ftl"> 
<#include "legacy-cds.ftl"> 
  "parSpread":"${security.parSpread}",
  "recoveryRate":"${security.recoveryRate}"
<#include "security-footer.ftl"> 
</#escape>
