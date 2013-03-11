<#escape x as x?html>
{
  "types" : [<#list securityTypes as key>"${key}"<#if key_has_next>,</#if></#list>]
}
</#escape>