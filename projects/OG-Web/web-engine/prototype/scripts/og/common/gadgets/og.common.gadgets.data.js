/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.Data',
    dependencies: ['og.common.gadgets.manager'],
    obj: function () {
        var module = this, loading_template;
        return function (config) {
            var gadget = this, instantiated = false, alive = og.common.id('gadget_data'),
                css_position = {position: 'absolute', top: '0', left: 0, right: 0, bottom: 0}, $data_grid;
            gadget.alive = function () {return $(config.selector).length ? true : (gadget.die(), false);};
            gadget.load = function () {
                $(config.selector).addClass(alive).css(css_position).html(loading_template({text: 'loading...'}));
                gadget.dataman = new og.analytics
                    .Cell({source: config.source, col: config.col, row: config.row, format: 'EXPANDED'}, 'data')
                    .on('data', function (cell) {
                        if (!cell.error && cell.v) {
                            if (cell.v.label) cell.v.labels = [cell.v.label]; // if there is only one label
                            try {
                                if (!instantiated)
                                    $data_grid = (instantiated = true) && $(config.selector).ogdata(cell.v);
                                else gadget.update(cell.v);
                            } catch (error) {
                                gadget.die();
                                $(config.selector).html('Error: Cannot render this cell as a data gadget');
                            }
                        } else {
                            og.dev.warn(module.name + ': bad data, ', cell.v);
                            if (!cell.error) return;
                            if ($data_grid) $data_grid = instantiated = $data_grid.die() && null;
                            $(config.selector).html('Error: ' + cell.v);
                        }
                    });
            };
            gadget.die = function () {
                try {$data_grid.die();} catch (error) {}
                try {gadget.dataman.kill();} catch (error) {}
            };
            gadget.update = function (input) {$data_grid.update(input);};
            gadget.resize = function () {try {$data_grid.resize();} catch (error) {}};
            if (loading_template) gadget.load(); else og.api.text({module: 'og.analytics.loading_tash'})
                .pipe(function (template) {loading_template = Handlebars.compile(template); gadget.load();});
            if (!config.child) og.common.gadgets.manager.register(gadget);
        };
    }
});