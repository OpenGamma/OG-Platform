/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.Surface',
    dependencies: ['og.common.gadgets.manager'],
    obj: function () {
        var loading_template;
        return function (config) {
            var gadget = this, surface, alive = og.common.id('gadget_surface'), $selector = $(config.selector),
                surface_options = {selector: config.selector, options: {webgl: og.app.WEBGL}};
            $(config.selector).addClass(alive).css({position: 'absolute', top: 0, left: 0, right: 0, bottom: 0});
            gadget.alive = function () {
                var live = !!$('.' + alive).length;
                if (live) return true;
                try {if (surface) surface.die();} catch (error) {}
                try {gadget.dataman.kill();} catch (error) {}
                return false;
            };
            gadget.resize = function () {try {surface.resize();} catch (error) {}};
            gadget.load = function () {
                $(config.selector).html(loading_template({text: 'loading...'}));
                gadget.dataman = new og.analytics
                    .Cells({
                        source: config.source, single: {row: config.row, col: config.col}, format: 'EXPANDED'
                    }, 'surface')
                    .on('data', function (data) {
                        var error = data.error;
                        data = data.v || data;
                        if (error) return !surface && $selector.html(data);
                        if (!data.xValues || !data.yValues)
                            return !surface && $selector.html('bad data: ' + JSON.stringify(data));
                        surface_options.data = {
                            vol: data.vol,
                            xs: data.xValues, xs_labels: data.xLabels, xs_label: data.xTitle || 'X Axis',
                            zs: data.yValues, zs_labels: data.yLabels, zs_label: data.yTitle || 'Y Axis'
                        };
                        if (!surface) surface = new JSurface3D(surface_options);
                        else surface.update('surface', surface_options.data);
                    })
                    .on('fatal', function (message) {$selector.html(message)});
            };
            if (loading_template) gadget.load(); else og.api.text({module: 'og.views.gadgets.loading_tash'})
                .pipe(function (template) {loading_template = Handlebars.compile(template); gadget.load();});
            if (!config.child) og.common.gadgets.manager.register(gadget);
        }
    }
});