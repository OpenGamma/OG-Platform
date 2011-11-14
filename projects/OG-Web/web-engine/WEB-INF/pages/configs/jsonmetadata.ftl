<#escape x as x?html>
{
  "types" : [<#list typeMap?keys as key>"${key}"<#if key_has_next>,</#if></#list>],
  "curveSpecificationBuilderConfiguration" : [<#list curveSpecs as curveSpec>"${curveSpec}"<#if curveSpec_has_next>,</#if></#list>]
}
</#escape>