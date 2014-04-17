{
    "template_data": {
<#if configJSON??>
      "configJSON":${configJSON},
</#if>
<#if configXML??>
      "configXML":"${configXML}",
</#if>
<#if deleted>
      "deleted":"${configDoc.versionToInstant}",
</#if>
      "name":"${configDoc.name}",
      "type":"${configDescription}",
      "object_id":"${configDoc.uniqueId.objectId}",
      "version_id":"${configDoc.uniqueId.version}",
      "type":"${type}"
    }
}
