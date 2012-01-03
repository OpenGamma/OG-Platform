<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
  <head>
    <title>OpenGamma Analytics</title>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />

    <script type="text/javascript" src="/js/jquery/jquery-1.4.2.min.js"></script>
    <script type="text/javascript" src="/js/jquery/jquery.event.drag-2.0.min.js"></script>
    <script type="text/javascript" src="/js/jquery/jquery.json-2.2.js"></script>
    <script type="text/javascript" src="/js/jquery/jquery.sparkline.js"></script>
    <script type="text/javascript" src="/js/jquery/jquery.cookie.js"></script>
    <script type="text/javascript" src="/js/jquery/jquery-ui-1.8.5.custom.min.js"></script>
    <script type="text/javascript" src="/js/jquery/jquery.ui.core.min.js"></script>
    <script type="text/javascript" src="/js/jquery/jquery.ui.tabs.min.js"></script>
    <script type="text/javascript" src="/js/jquery/jquery.layout-1.2.0.js"></script>
    <link rel="stylesheet" href="/css/jquery/smoothness/jquery-ui-1.8.5.custom.css" type="text/css" charset="utf-8"  media="screen, print" />

    <!--[if IE]><script language="javascript" type="text/javascript" src="/js/excanvas/excanvas.min.js"></script><![endif]-->

    <script type="text/javascript" src="/js/cometd/cometd.js"></script>
    <script type="text/javascript" src="/js/jquery/jquery.cometd.js"></script>

    <script type="text/javascript" src="/js/jquery/jquery.transform-0.6.2.min.js"></script>
    <script type="text/javascript" src="/js/jquery/jquery.jdCrazyDots.js"></script>
    <script type="text/javascript" src="/js/jquery/jquery.scrollTo.js"></script>

    <script type="text/javascript" src="/js/slickgrid/slick.editors.js"></script>
    <script type="text/javascript" src="/js/slickgrid/slick.grid.js"></script>
    <script type="text/javascript" src="/js/slickgrid/slick.model.js"></script>
    <link rel="stylesheet" href="/css/slickgrid/slick.grid.css" type="text/css" charset="utf-8" media="screen, print" />

    <script type="text/javascript" src="/js/flot/jquery.flot.js"></script>
    <script type="text/javascript" src="/js/flot/jquery.flot.selection.js"></script>
    <script type="text/javascript" src="/js/flot/jquery.flot.navigate.js"></script>
    <script type="text/javascript" src="/js/flot/jquery.flot.crosshair.js"></script>

    <script type="text/javascript" src="js/common.js"></script>
    <script type="text/javascript" src="js/userConfig.js"></script>
    <script type="text/javascript" src="js/liveResultsClient.js"></script>
    <script type="text/javascript" src="js/formatting/columnFormatter.js"></script>
    <script type="text/javascript" src="js/formatting/primitiveFormatter.js"></script>
    <script type="text/javascript" src="js/formatting/doubleFormatter.js"></script>
    <script type="text/javascript" src="js/formatting/matrixFormatter.js"></script>
    <script type="text/javascript" src="js/formatting/interpolatedYieldCurveFormatter.js"></script>
    <script type="text/javascript" src="js/formatting/interpolatedYieldCurveDetail.js"></script>
    <script type="text/javascript" src="js/formatting/interpolatedYieldCurveChart.js"></script>
    <script type="text/javascript" src="js/formatting/interpolatedYieldCurveData.js"></script>
    <script type="text/javascript" src="js/formatting/volatilitySurfaceDataFormatter.js"></script>
    <script type="text/javascript" src="js/formatting/volatilitySurfaceDataDetail.js"></script>
    <script type="text/javascript" src="js/formatting/labelledMatrix1DFormatter.js"></script>
    <script type="text/javascript" src="js/formatting/labelledMatrix1DDetail.js"></script>
    <script type="text/javascript" src="js/formatting/labelledMatrix2DFormatter.js"></script>
    <script type="text/javascript" src="js/formatting/labelledMatrix2DDetail.js"></script>
    <script type="text/javascript" src="js/formatting/unknownTypeFormatter.js"></script>
    <script type="text/javascript" src="js/slickGridHelper.js"></script>
    <script type="text/javascript" src="js/popupManager.js"></script>
    <script type="text/javascript" src="js/portfolioViewer.js"></script>
    <script type="text/javascript" src="js/primitivesViewer.js"></script>
    <script type="text/javascript" src="js/depGraphViewer.js"></script>
    <script type="text/javascript" src="js/tabbedViewResultsViewer.js"></script>
    <script type="text/javascript" src="js/home.js"></script>

    <script type="text/javascript">
      var config = {
        contextPath: '${pageContext.request.contextPath}'
      };
    </script>

    <link rel="stylesheet" href="css/analytics-base.css" type="text/css" charset="utf-8" media="screen, print" />
    <link rel="stylesheet" href="css/popup-column.css" type="text/css" charset="utf-8" media="screen, print" />
    <link rel="stylesheet" href="css/ui-layout.css" type="text/css" charset="utf-8" media="screen, print" />
    <link rel="stylesheet" href="css/analytics-print.css" type="text/css" media="print" charset="utf-8" />

    <style type="text/css" media="screen, print">

      .cell-title {
        font-weight: bold;
        margin: 0 4px;
      }
      
      .right {
        text-align: right;
      }

      .cell-effort-driven {
        text-align: center;
      }

      .toggle {
        height: 9px;
        width: 9px;
        margin: 0 0 0 4px;
        display: inline-block;
      }

      .toggle.expand {
        background: url(/images/expand.gif) no-repeat center center;
      }

      .toggle.collapse {
        background: url(/images/collapse.gif) no-repeat center center;
      }

      .tickindicator {
        height: 16px;
        width: 10px;
        display: inline-block;
        vertical-align: middle;
        padding: 0;
        position: absolute;
        right: 2px;
        top: 50%;
        margin: -7px 0 0 0;
      }

      .tickindicator.up {
        background: url(/images/tick-up.png) no-repeat;
      }

      .tickindicator.down {
        background: url(/images/tick-down.png) no-repeat;
      }

      .tickindicator.same {
        background: url(/images/tick-same.png) no-repeat;
      }

      .slick-cell.explain {
        background-color: #cbead7;
      }

      .slick-cell.explain.explain-hover {
        background-color: #ffff95;
      }

      .cell-contents {
        width: 100%;
        height: 100%;
        vertical-align: middle;
        position: relative;
        background-color: inherit;
      }

      .cell-value {
        text-overflow: ellipsis;
        margin: 0;
        padding: 0 4px;
        background-color: inherit;
      }

      .cell-value.right {
        position: absolute;
        right: 12px;
        background-color: inherit;
      }

      .negative {
        color: red;
      }

      .primitive-history-sparkline {
        display: inline-block;
        vertical-align: middle;
        margin: 0 0 0 2px;
        padding: 0;
        height: 14px;
        width: 50px;
      }

      .interpolated-yield-curve, .volatility-surface {
        display: inline-block;
        align: center;
        margin: 5px;
      }

      .interpolated-yield-curve-detail-tabs {
        width: 100%;
        height: 100%;
      }

      .interpolated-yield-curve-detail-tabs .ui-tabs-nav li a {
        font-size: 90%;
      }

      .interpolated-yield-curve-detail-tabs .ui-tabs-nav span {
        font-size: 100%;
      }

      .interpolated-yield-curve-detail-tabs .data, .interpolated-yield-curve-detail-tabs .curve {
        position: absolute;
        top: 2.8em;
        bottom: 0;
        left: 0;
        right: 0;
        padding: 0;
      }

      .interpolated-yield-curve-detail-tabs .data .grid {
        position: absolute;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;
      }

      .interpolated-yield-curve-detail-tabs .curve .chart {
        position: absolute;
        top: 8px;
        bottom: 8px;
        left: 8px;
        right: 8px;
      }
      
      .labelled-matrix-header {
        font-weight: bold;
      }

      .detail-popup {
        position: absolute;
      }
      
      .detail-popup.tabs {
        padding: 8px;
      }

      .detail-popup .detail-content {
        width: 100%;
        height: 100%;
      }

      .header {
        width: 289px;
        height: 51px;
        display: inline-block;
        background: url(/images/opengamma.png) no-repeat center center;
        margin-left: 1em;
        margin-bottom: 1em;
        margin-top: 1em;
      }
      
      #loadingviews {
        margin-top: 1em;
      }
      
      #loadingviews p {
        margin: 0;
        padding: 0;
        font-size: 1.2em;
      }

      #viewcontrols {
        display: none;
        text-align: right;
        float: right;
        margin-top: 1em;
        margin-right: 1em;
      }
      
      #viewselector {
        position: absolute;
        left: 5px;
        top: 10px;
      }

      #currentviewcontrols {
        margin-right: 3px;
        margin-top: 5px;
      }

      .imgbutton {
        cursor: pointer;
        vertical-align: middle;
        width: 14px;
        height: 16px;
        margin: 0 1px;
        display: inline-block;
      }

      .imgbutton.resume.on {
        background: url(/images/resume-on.png) no-repeat center center;
      }

      .imgbutton.resume.off {
        background: url(/images/resume-off.png) no-repeat center center;
        cursor: auto;
      }

      .imgbutton.pause.on {
        background: url(/images/pause-on.png) no-repeat center center;
      }

      .imgbutton.pause.off {
        background: url(/images/pause-off.png) no-repeat center center;
        cursor: auto;
      }

      .imgbutton.sparklines {
        width: 16px;
      }

      .imgbutton.sparklines.off {
        background: url(/images/sparklines-off.png) no-repeat center center;
      }

      .imgbutton.sparklines.on {
        background: url(/images/sparklines-on.png) no-repeat center center;
      }

      .imgbutton.revealmore {
        width: 14px;
        height: 14px;
      }

      #resultsViewer #tabs #viewstatus {
        float: right;
        margin: 0.4em 4em 0 0;
      }

      #resultsViewer #tabs #viewstatus p {
        font-weight: normal;
        margin: 0;
        padding: 0;
      }

      .statustitle {
        font-weight: bold;
      }

      #resultsViewer {
        position: absolute;
        top: 40px;
        left: 0;
        right: 0;
        bottom: 0;
        background: #fff;
      }

      #resultsViewer #tabs {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
      }
      
      #resultsViewer #tabs .export-button {
        float: right;
        border: 0;
        margin: -3.4em -1em 0 0;
      }
      
      #resultsViewer #tabs .export-button a {
        margin: 0;
        padding: 0;
      }
      
      #resultsViewer #tabs .export-button .ui-button-text {
        font-size: 0.8em;
        padding: 0.4em 0.5em;
      }

      #loading {
        position: absolute;
        margin: 5em auto;
        width: 0;
      }

      #resultsViewer #loading .dot {
        position: absolute;
        opacity: 0.0;
        filter: alpha(opacity=0);
        background: -moz-linear-gradient(top left, #033c5a, #477187);
        background: -webkit-gradient(linear, left top, left bottom, from(#033c5a), to(#477187));
        -moz-border-radius: 50%;
        -webkit-border-radius: 15px;
      }

      #resultsViewer #tabs #portfolio, #resultsViewer #tabs #portfolio #portfolioGrid,
      #resultsViewer #tabs #primitives, #resultsViewer #tabs #primitives #primitivesGrid {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
      }

      #resultsViewer #tabs #portfolio, #resultsViewer #tabs #primitives {
        top: 3em;
      }

      #views {
        display: inline-block;
      }
      
      .viewlabel {
        margin: 0 0.5em;
      }
      
      .standard-entry {
        font-style: italic;
      }
      
      .autocomplete-divider {
        border-width: 0 0 1px 0;
        border-style: inherit;
        border-color: inherit;
      }
      
      /* JQuery UI theme overrides */

      .ui-tabs {
        padding: 0;
        border-bottom-left-radius: 0;
        border-bottom-right-radius: 0;
      }

      .ui-tabs-nav {
        border-top: none;
        border-left: none;
        border-right: none;
        border-bottom-left-radius: 0;
        border-bottom-right-radius: 0;
      }

      .ui-tabs-nav li a {
        font-weight: bold;
      }

      .ui-button .ui-button-text {
        display: inline;
      }
      
      .ui-autocomplete {
        max-height: 200px;
        overflow-y: scroll;
        overflow-x: hidden;
      }

      .ui-autocomplete-input {
        font-size: 1em;
        height: 16px;
        margin: 0;
        padding: 0 0.2em;
      }

   </style>
  </head>
  <body>
    <div id="loadingviews"><p>Loading views...</p></div> 
    <div id="viewcontrols">
      <div id="viewselector">
        <div id="views"></div>
        <div id="changeView"></div>
        <div id="sparklines" class="imgbutton sparklines"></div>
      </div>
      <div id="currentviewcontrols" style="position: absolute; right: 5px; top: 7px;">
      <div></div>
        <div id="resume" class="imgbutton resume"></div>
        <div id="pause" class="imgbutton pause"></div>
      </div>
    </div>

    <div id="resultsViewer"></div>
  </body>
</html>
