{
    "templateData": {
      "name":"${configDoc.name}",
      "objectId":"${configDoc.uniqueId.objectId}",
      "versionId":"${configDoc.uniqueId.version}",
<#-- deprecated -->
      "uniqueId":{"Value":"${configDoc.uniqueId.value}","Scheme":"${configDoc.uniqueId.scheme}","Version":"${configDoc.uniqueId.version}"},
<#if deleted>
      "deleted":"${configDoc.versionToInstant}",
</#if>
      <#assign clazz = configDoc.value.class/>
      "type":"${clazz.simpleName}",
      <#if clazz.canonicalName = "com.opengamma.engine.view.ViewDefinition">
        "configJSON" : ${configJSON}
      <#else>
        "configXml":"${configXml}"
      </#if>
      
   	}
}