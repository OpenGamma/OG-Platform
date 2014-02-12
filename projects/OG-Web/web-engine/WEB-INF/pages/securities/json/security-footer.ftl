"name": "${security.name}",
    "object_id": "${security.uniqueId.objectId}",
    <#if security.uniqueId.version?has_content>"version_id": "${security.uniqueId.version}",</#if>
    <#if timeSeriesId?has_content>"hts_id": "${timeSeriesId}",</#if>
    <#if deleted>
      "deleted": "${securityDoc.versionToInstant}",
    </#if>
    "securityType":"${security.securityType}" },
    "securityXml":"${securityXml}",
    "identifiers": {<#list security.externalIdBundle.externalIds as item> "${item.scheme.name}":"${item.scheme.name}-${item.value}"<#if item_has_next>,</#if> </#list>}
}