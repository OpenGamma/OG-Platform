<#escape x as x?html>
{
    "templateData": {
      "name": "${position.name}",
      "objectId":"${position.uniqueId.objectId}",
      "versionId":"${position.uniqueId.version}",
<#if deleted>
      "deleted":"${positionDoc.versionToInstant}",
</#if>
      "quantity": "${position.quantity}"
    },
    "security": {
    	"name":"${security.name}",
    	"uniqueId":"${security.uniqueId.objectId}",
      "security_type":"${security.securityType}"
    },
    "securities": [
    <#list position.securityKey.identifiers as item>
    	{"scheme":"${item.scheme.name}", "value":"${item.value}"}<#if item_has_next>,</#if>
	</#list>
    ],
    "trades": [
    <#list position.trades as item>
    	{"id":"${item.uniqueId.objectId}", "quantity":"${item.quantity}", "counterParty":"${item.counterpartyKey}", "date":"${item.tradeDate}"}<#if item_has_next>,</#if>
	</#list>
    ]
}
</#escape>