<#include "modules/common/og.common.header.ftl">
<title>OpenGamma</title>
${ogStyle.print('og_all.css', 'all',false)}
<style type="text/css">
.temp-ul li{padding-left:6px;}
.temp-ul {padding:3px;}
</style>
</head>
<body>
<div class="OG-layout-blotter-container">
  <div class="ui-layout-north">
      <#include "modules/common/og.common.masthead.ftl">
      <div class="og-form"></div>
  </div>
  <div class="ui-layout-center">
    <div class="new_trade" style="border-radius: 8px 8px 8px 8px;border-style: solid;
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.6);color: black;cursor: pointer;padding: 10px 13px;
        margin: 5px;">NEW TRADE</div><br/>
        <strong>Over The Counter Trades</strong><br/>
        <ul class="temp-ul">
          <li class="capfloorcmsspread">Cap Floor CMS Spread - DbPos~164152</li>
          <li class="capfloor">Cap Floor - DbPos~164182</li>
          <li class="equityvar">Equity Varience Swap - DbPos~164208</li>
          <li class="fra">Forward Rate Agreement - DbPos~164245</li> 
          <li class="fxbarrier">FX Barrier Option - DbPos~164255</li> 
          <li class="fxforward">FX Forward - DbPos~164134</li>
          <li class="fxoption">FX Option - DbPos~164257</li>
          <li class="nondelfxoption">Non Deliverable FX Option - DbPos~164259</li>
          <li class="swap">Swap - DbPos~164306</li>
          <li class="swaption">Swaption - DbPos~164328</li>
        </ul>
        <strong>Fungible Trades</strong><br/>
        <ul class="temp-ul">
          <li class="fungibleb">Bond - DbPos~164540</li>
          <li class="fungiblebf">Bond Future - DbPos~164562</li>
          <li class="fungibleet">Exchange Traded - DbPos~164554</li>
        </ul>
  </div>
</div>
<!--[if IE]>${ogScript.print('ie.js',false)}<![endif]-->
${ogScript.print('og_common.js',false)}
${ogScript.print('og_blotter.js',false)}
</body>
</html>