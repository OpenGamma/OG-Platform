<#escape x as x?html>
{
  "securityTypes" : [<#list securityTypes as key>"${key}"<#if key_has_next>,</#if></#list>]
}
</#escape>