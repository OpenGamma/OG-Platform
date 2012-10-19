/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.Surface',
    dependencies: ['og.common.gadgets.manager', 'og.api.text'],
    obj: function () {
        var prefix = 'og_surface_gadget_', counter = 1;
        return function (config) {
            var gadget = this, surface, alive = prefix + counter++,
                surface_options = {
                    selector: config.selector,
                    options: {}
            };
            $(config.selector).addClass(alive).css({position: 'absolute', top: 0, left: 0, right: 0, bottom: 0});
            gadget.dataman = new og.analytics.Cell({source: config.source, row: config.row, col: config.col})
                .on('data', function (data) {
                    if (!data.x_values || !data.y_values) return /* TODO: load in data gadget instead */;
                    surface_options.data = {
                        vol: data.vol,
                        xs: data.x_values,
                        xs_labels: data.x_labels,
                        xs_label: 'x test',
                        zs: data.y_values,
                        zs_labels: data.y_labels,
                        zs_label: 'z test'
                    };
                    if (!surface) {
                        surface = new JSurface3D(surface_options);
                        gadget.alive = function () {
                            var live = !!$('.' + alive).length;
                            if (!live) surface.die(), gadget.dataman.kill();
                            return live;
                        };
                        gadget.resize = surface.resize;
                        if (!config.child) og.common.gadgets.manager.register(gadget);
                    } else surface.update_surface_plane(surface_options.data);
                });
        }
    }
});