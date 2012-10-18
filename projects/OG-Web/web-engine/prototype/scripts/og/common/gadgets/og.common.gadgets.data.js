/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.Data',
    dependencies: ['og.common.gadgets.manager'],
    obj: function () {
        var module = this, prefix = 'og_data_gadget_', counter = 1;
        return function (config) {
            var gadget = this, instantiated = false, alive = prefix + counter++,
                css_position = {position: 'absolute', top: '0', left: 0, right: 0, bottom: 0}, $data_grid;
            gadget.alive = function () {return $(config.selector).length ? true : (gadget.die(), false);};
            gadget.load = function () {
                $(config.selector).addClass(alive).css(css_position);
                gadget.dataman = new og.analytics.Cell({source: config.source, col: config.col, row: config.row})
                    .on('data', function (data) {
                        if (data) {
                            if (!instantiated)
                                $data_grid = (instantiated = true) && $(config.selector).ogdata({data: data});
                            else gadget.update({data: data});
                        } else {
                            $(config.selector).html('loading...');
                            og.dev.warn(module.name + ': bad data, ', data);
                        }
                    });
            };
            gadget.die = function () {
                try {$data_grid.die();} catch (error) {}
                try {gadget.dataman.kill();} catch (error) {}
            };
            gadget.update = function (input) {$data_grid.update(input);};
            gadget.resize = function () {try {$data_grid.resize();} catch (error) {}};
            gadget.load();
            if (!config.child) og.common.gadgets.manager.register(gadget);
        };
    }
});