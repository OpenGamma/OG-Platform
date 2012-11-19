/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.Curve',
    dependencies: ['og.common.gadgets.manager'],
    obj: function () {
        var prefix = 'og_curve_gadget_', counter = 1, module = this, loading_template;
        return function (config) {
            var gadget = this, $curve, alive = prefix + counter++;
            gadget.dataman = new og.analytics
                .Cell({source: config.source, row: config.row, col: config.col, format: 'EXPANDED'}, 'curve')
                .on('data', function (data) {
                    data = data.v || data; 
                    if (!$.isArray(data)) return og.dev.warn(module.name + ': data should be an Array', data);
                    gadget.data = [{curve: data}];
                    $curve.update ? $curve.update(gadget.data) : gadget.resize();
                });
            gadget.alive = function () {
                var live = !!$('.' + alive).length;
                if (!live) gadget.dataman.kill();
                return live;
            };
            gadget.resize = function () {
                if (!$(config.selector).length) return;
                $curve = $(config.selector)
                    .css({position: 'absolute', top: 0, left: 0, right: 0, bottom: 0})
                    .ogcurve(gadget.data);
            };
            $curve = $(config.selector).addClass(alive);
            if (loading_template) $curve.html(loading_template({text: 'loading...'}));
            else og.api.text({module: 'og.analytics.loading_tash'}).pipe(function (template) {
                loading_template = Handlebars.compile(template);
                $curve.html(loading_template({text: 'loading...'}));
            });
            if (!config.child) og.common.gadgets.manager.register(gadget);
        };
    }
});