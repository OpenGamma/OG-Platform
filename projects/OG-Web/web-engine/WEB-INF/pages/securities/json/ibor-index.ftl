<#escape x as x?html>
<#include "security-header.ftl"> 
      "tenor":"${security.tenor.toFormattedString()}",
      "conventionId":"${security.conventionId}",
      "indexFamilyId":"${security.indexFamilyId}",
<#include "security-footer.ftl"> 
</#escape>
