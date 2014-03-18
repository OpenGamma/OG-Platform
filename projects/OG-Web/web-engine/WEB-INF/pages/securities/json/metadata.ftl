{
  <#if description2type??>
    "description2type" : {
    <#list description2type?keys as key>
      "${key}":"${description2type[key]}"<#if key_has_next>,</#if>
    </#list>
    },
  </#if>
  <#if schemaVersion?has_content>"schemaVersion": "${schemaVersion}",</#if> 
  "types" : [<#list securityTypes as key>"${key}"<#if key_has_next>,</#if></#list>]
}