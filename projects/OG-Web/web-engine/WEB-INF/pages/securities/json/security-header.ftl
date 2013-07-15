<#setting number_format="0.#####">
{
    "template_data": {
     <#if securityAttributes?? && securityAttributes?has_content>
      "attributes": {
      <#list securityAttributes?keys as key>
        <#assign value = securityAttributes[key]> "${key}" : "${value}"<#if key_has_next>,</#if>
      </#list>
      },
    </#if>