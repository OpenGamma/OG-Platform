<#escape x as x?html>
{
  <#if schemaVersion?has_content>"schemaVersion": "${schemaVersion}",</#if> 
  "types" : [<#list securityTypes as key>"${key}"<#if key_has_next>,</#if></#list>]
  
}
</#escape>