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
                instantiated, tash, $msg, sbar_size = og.common.util.scrollbar_size + 'px',
                cell_options = {
                    source: config.source, single: {row: config.row, col: config.col}, format: 'EXPANDED', log: true
                },
                css_position = {position: 'absolute', top: '0', left: 0, right: 0, bottom: 0},
                nolog = '<div class="OG-logger"><div class="og-nolog">No log information available</div></div>';
            gadget.alive = function () {return $(config.selector).length ? true : (gadget.die(), false);};
            gadget.load = function () {
                $selector.addClass(alive).css(css_position).html(loading_template({text: 'loading...'}));
                gadget.dataman = new og.analytics.Cells(cell_options, 'log').on('data', function (cell) {
                    if (gadget.data = cell.logOutput) {
                        if (!instantiated) $.when(og.api.text({module: 'og.analytics.logger'})).then(function (tmpl) {
                            tash = Handlebars.compile(tmpl);
                            instantiated = true;
                            gadget.update();
                        });
                        else if (!$msg.is(':visible')) {
                            $selector.find('table').css('marginTop', '25px');
                            $msg.slideDown().on('click', '.og-link', function () {$msg.slideUp(null, gadget.update);});
                        }
                    } else $selector.html(nolog), instantiated = null;
                });
            };
            gadget.die = function () {try {gadget.dataman.kill();} catch (error) {}};
            gadget.update = function () {
                if (tash) $msg = $selector.html(tash({obj: gadget.data})).find('.og-message').css('right', sbar_size);
            };
            gadget.resize = function () {gadget.update();};
            if (loading_template) gadget.load(); else og.api.text({module: 'og.views.gadgets.loading_tash'})
                .pipe(function (template) {loading_template = Handlebars.compile(template); gadget.load();});
            if (!config.child) og.common.gadgets.manager.register(gadget);
        };
    }
});