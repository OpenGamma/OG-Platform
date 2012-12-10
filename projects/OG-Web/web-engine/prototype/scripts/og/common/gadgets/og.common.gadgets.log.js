/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.Log',
    dependencies: ['og.common.gadgets.manager'],
    obj: function () {
        var loading_template;
        return function (config) {
            var gadget = this, alive = og.common.id('gadget_log'), $selector = $(config.selector),
                logger, instantiated, template,
                cell_options = {source: config.source, col: config.col, row: config.row, format: 'EXPANDED', log: true},
                css_position = {position: 'absolute', top: '0', left: 0, right: 0, bottom: 0};
            gadget.alive = function () {return $selector.length ? true : (gadget.die(), false);};
            gadget.load = function () {
                $selector.addClass(alive).css(css_position).html(loading_template({text: 'loading...'}));
                gadget.dataman = new og.analytics.Cell(cell_options, 'data').on('data', function (cell) {
                    gadget.data = cell.logOutput;
                    if (gadget.data) {
                        if (!instantiated) $.when(og.api.text({module: 'og.analytics.logger'})).then(function (tmpl) {
                            template = Handlebars.compile(tmpl);
                            instantiated = true;
                            gadget.update();
                        });
                        else gadget.update();
                    } else {
                        instantiated = gadget.die() && null;
                        $selector.html('No log information available');
                    }
                });
            };
            gadget.die = function () {try {gadget.dataman.kill();} catch (error) {}};
            gadget.update = function () {if (template) $selector.html(template({obj: gadget.data}));};
            gadget.resize = function () {gadget.update();};
            if (loading_template) gadget.load(); else og.api.text({module: 'og.analytics.loading_tash'})
                .pipe(function (template) {loading_template = Handlebars.compile(template); gadget.load();});
            if (!config.child) og.common.gadgets.manager.register(gadget);
        };
    }
});