/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
(function () {
    if (!window.JSurface3D) throw new Error('JSurface3D.Hud requires JSurface3D');
    /**
     * Create the html overlay with the log option and volatility display
     * @param {Object} js3d A JSurface3D instance
     * @param {String} $selector A Jquery selector to the surfaces container
     * @param {String} stylesheet Selector to the surfaces shared stylesheet location
     * @name JSurface3D.Hud
     * @namespace JSurface3D.Hud
     * @private
     * @constructor
     */
    window.JSurface3D.Hud = function (js3d, $selector, stylesheet) {
        var hud = this, settings = js3d.settings;
        /**
         * Loads 2D overlay display with form
         * @name JSurface3D.Hud.load
         * @function
         * @private
         */
        hud.load = function () {
            if (!settings.hud) return;
            $selector.find('.js3d').remove();
            (function () {
                if ($(stylesheet).length) return;
                var css = '\
                    .js3d {bottom: 10px; left: 10px; top: 0; position: absolute;}\
                    .js3d .j-o {position: absolute; top: 10px; white-space: nowrap;}\
                    .js3d .j-o input {vertical-align: top;}\
                    .js3d .j-v {padding-top: 9px; padding-bottom: 3px; position: absolute; bottom: 0;}\
                    .js3d .j-v canvas {border: 1px solid #ccc;}\
                    .js3d .j-v span {position: absolute; left: 20px;}\
                    .js3d .j-v .j-max {top: 0;}\
                    .js3d .j-v .j-min {bottom: 0;}\
                    .js3d .j-v .j-vol {display: none; background: #eee; border: 1px solid #ddd; padding: 0 5px;}\
                    .js3d .j-v .j-vol:after {content: ""; display: block; position: absolute; \
                        width: 0; height: 0; left: -10px; top: 50%; margin-top: -4px; \
                        border-top: 5px solid transparent; border-right: 10px solid #ddd; \
                        border-bottom: 5px solid transparent;}',
                    head = document.querySelector('head'), style = document.createElement('style');
                style.setAttribute('data-og', 'surface');
                if (style.styleSheet) style.styleSheet.cssText = css; // IE
                else style.appendChild(document.createTextNode(css));
                head.appendChild(stylesheet = style);
            })();
            var tmpl = '\
                <div class="js3d">\
                  <div class="j-o"><label>Log<input type="checkbox" checked="checked" /></label></div>\
                  <div class="j-v">\
                    <span class="j-max">{{max}}</span><span class="j-min">{{min}}</span><span class="j-vol"></span>\
                    <canvas>canvas</canvas>\
                  </div>\
                </div>',
                min = js3d.vol_min.toFixed(settings.precision_hud), max = js3d.vol_max.toFixed(settings.precision_hud),
                html = tmpl.replace(/\{\{(?:max|min)\}\}/g, function (m) {return m === '{{min}}' ? min : max;}),
                $html = $(html).appendTo($selector);
            hud.vol_canvas_height = js3d.height / 2;
            if (js3d.webgl) hud.volatility($html.find('canvas')[0]);
            else $html.find('.j-v').hide();
            hud.form();
        };
        /**
         * Attach change handler to log option
         * @function
         * @private
         * @ignore
         */
        hud.form = function () {
            $selector.find('.j-o input').prop('checked', js3d.settings.log).on('change', function () {
                js3d.settings.log = $(this).is(':checked');
                js3d.update('world');
            });
        };
        /**
         * Set a value in the 2D volatility display
         * @param {Number} value The value to set. If value is not a number the indicator is hidden
         * @function
         * @private
         * @ignore
         */
        hud.set_volatility = function (value) {
            if (!settings.hud) return;
            var top = (Four.scale([js3d.vol_min, value, js3d.vol_max], hud.vol_canvas_height, 0))[1],
                css = {top: top + 'px', color: settings.interactive_hud_color},
                $vol = $selector.find('.j-vol');
            typeof value === 'number'
                ? $vol.html(value.toFixed(settings.precision_hud)).css(css).show()
                : $vol.empty().hide();
        };
        hud.vol_canvas_height = null;
        /**
         * Volatility gradient canvas
         * @param {Object} canvas html canvas element
         * @function
         * @private
         * @ignore
         */
        hud.volatility = function (canvas) {
            var ctx = canvas.getContext('2d'), gradient,
                min_hue = settings.vertex_shading_hue_min, max_hue = settings.vertex_shading_hue_max,
                steps = Math.abs(max_hue - min_hue) / 60, stop;
            gradient = ctx.createLinearGradient(0, 0, 0, hud.vol_canvas_height);
            canvas.width = 10;
            canvas.height = hud.vol_canvas_height;
            gradient.addColorStop(0, 'hsl(' + max_hue + ', 100%, 50%)');
            while (steps--) { // 60: the number of hue increaments before one color starts fading in and another out
                stop = (steps * 60 / (min_hue / 100)) / 100;
                gradient.addColorStop(stop, 'hsl(' + steps * 60 + ', 100%, 50%)');
                gradient.addColorStop(stop, 'hsl(' + steps * 60 + ', 100%, 50%)');
            }
            gradient.addColorStop(1, 'hsl(' + min_hue + ', 100%, 50%)');
            ctx.fillStyle = gradient;
            ctx.fillRect(0, 0, 10, hud.vol_canvas_height);
        };
    };
})();