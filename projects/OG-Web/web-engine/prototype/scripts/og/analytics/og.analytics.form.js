/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.form',
    dependencies: ['og.common.util.ui.AutoCombo', 'og.views.common.layout'],
    obj: function () {
        return function (selector) {
            var FormCombo = function (selector, module, data) {
                var $popdiv, menu = this, $title;
                $.when(og.api.text({module: module})).then(function (template) {
                    var html = $((Handlebars.compile(template))(data));
                    $title = $(selector).html(html).find('.og-option-title').on('click', function (event) {
                        event.stopPropagation();
                        menu.state === 'open' ? menu.close() : menu.open().focus();
                    });
                    menu.state = 'closed';
                    menu.focus = function () {return $popdiv.find('select').first().focus() && menu};
                    menu.open = function () {
                        $popdiv = $(selector + ' .OG-analytics-form-menu').show().trigger('open', $title);
                        $title.addClass('og-active');
                        menu.state = 'open';
                        return menu;
                    };
                    menu.close = function () {
                        if ($popdiv) $popdiv.hide();
                        $title.removeClass('og-active');
                        menu.state = 'closed';
                    };
                });
            };
            var Status = function (selector) {
                var status = this, interval, init = false;
                $(selector + ' button').on('click', function () {
                    if (!status.status || status.status === 'paused') return status.play();
                    if (status.status === 'playing') return status.pause();
                });
                status.pause = function () {
                    $(selector + ' em').html('paused').removeClass('live').addClass('paused');
                    $(selector + ' button').removeClass('og-icon-play').addClass('og-icon-pause');
                    status.message('');
                    clearInterval(interval);
                    status.status = 'paused';
                };
                status.play = function () {
                    //if (!init) init = !!$(selector + ' button').removeClass('og-disabled');
                    $(selector + ' em').html('live').removeClass('paused').addClass('live');
                    $(selector + ' button').removeClass('og-icon-pause').addClass('og-icon-play');
                    status.message('starting...');
                    interval = setInterval(function () {
                        status.message('updated ' + (Math.random() + 1).toFixed(2) + ' seconds ago');
                    }, 1000);
                    status.status = 'playing';
                };
                status.message = function (message) {$(selector + ' .og-message').html(message);};
                status.status = null;
                return status;
            };
            $.when(
                og.api.text({module: 'og.analytics.form_tash'}),
                og.api.rest.configs.get({handler: function (r) {return r}})
            ).then(function (template, search) {
                var response = { // dummy response
                    view: search.data.data,
                    aggregation: {
                        aggregators: ['Long / Short', 'Asset Class'],
                        row: [
                            {num: 1, label: 'Aggregated by', by: 'Long / Short'},
                            {num: 2, label: 'Then by', by: 'Asset Class', last: true}
                        ]
                    },
                    datasources: {
                        type: ['Live', 'Snapsot', 'Historical', 'Data Type'],
                        live: ['Bloomberg', 'Reuters'],
                        snapshot: ['Alan', 'Alan 2'],
                        historical: ['01 June 2012', '02 June 2012', '03 June 2012'],
                        datatype: ['type 1', 'type 2'],
                        row: [
                            {num: 1, type: 'Live', value: 'Bloomberg'},
                            {num: 2, type: 'Snapshot', value: 'Alan'},
                            {num: 3, type: 'Historical', value: '02 June 2012'},
                            {num: 4, type: 'Data Type', value: 'type 2', last: true}
                        ]
                    }
                };
                $(selector).html(template)
                    /**
                     * Custom tab event, only triggers between top level menu items. e.g. a tab event will trigger
                     * when tab entering or tab leaving the evented element
                     */
                    .on('keydown', 'input, select, button', function (event) {
                        if (event.keyCode !== 9) return;
                        var $aggr = $(selector + ' .og-aggregation').find('input, select, button'),
                            $data = $(selector + ' .og-datasources').find('input, select, button'),
                            $self = $(this), shift_key = event.shiftKey,
                            trigger = function (menu) {$self.trigger('tab', menu);},
                            active_pos = function (elms, pos) {return $self.is(elms[pos]())},
                            view = $self.closest('.og-view').length,
                            aggregation = $self.closest('.og-aggregation').length,
                            datasources = $self.closest('.og-datasources').length,
                            load = $self.hasClass('og-load');
                        if (view && shift_key) return;
                        if (view && !shift_key) return trigger('aggregation');
                        if (aggregation && active_pos($aggr, 'last') && !shift_key) return trigger('datasources');
                        if (datasources && active_pos($data, 'first') && shift_key) return trigger('aggregation');
                        if (datasources && active_pos($data, 'last') && !shift_key) return trigger('load');
                        if (load && shift_key) return trigger('datasources');
                    })
                    .on('tab', function (event, type) {
                        switch (type) {
                            case 'aggregation': aggregation_menu.open(); break;
                            case 'datasources': datasources_menu.open(); break;
                            case 'load': aggregation_menu.close(); datasources_menu.close(); break;
                        }
                    })
                    /**
                     * The "open" event fires everytime a menu item is opened
                     */
                    .on('open', function (event, elm) {
                        if (!aggregation_menu.state || !datasources_menu.state) return;
                        if ($(elm).closest('.og-view').length) {datasources_menu.close(), aggregation_menu.close();}
                        if ($(elm).closest('.og-aggregation').length) {datasources_menu.close();}
                        if ($(elm).closest('.og-datasources').length) {aggregation_menu.close();}
                        $(document).on('click.analytics.form', function (event) {
                            // if not self click, close
                            if (!$(event.target).closest('.OG-analytics-form-menu').length) {
                                aggregation_menu.close(); datasources_menu.close();
                                $(document).off('click.analytics.form');
                            }
                        });
                    })
                    .on('click', '.og-menu-aggregation button', function () {
                        var val = $(this).text();
                        if (val === 'OK') $(selector).trigger('tab', 'datasources'), datasources_menu.focus();
                        if (val === 'Cancel') aggregation_menu.close(), auto_combo_menu.select();
                    })
                    .on('click', '.og-menu-datasources button', function () {
                        var val = $(this).text();
                        if (val === 'OK') $(selector).trigger('tab', 'load'), $(selector + ' .og-load').focus();
                        if (val === 'Cancel') datasources_menu.close(), auto_combo_menu.select();
                    });
                var auto_combo_menu = new og.common.util.ui.AutoCombo(
                    '.OG-analytics-form .og-view', 'search...', response.view
                );
                var aggregation_menu = new FormCombo(
                    selector + ' .og-aggregation', 'og.analytics.form_aggregation_tash', response.aggregation
                );
                var datasources_menu = new FormCombo(
                    selector + ' .og-datasources', 'og.analytics.form_datasources_tash', response.datasources
                );
                new Status(selector + ' .og-status'); // .play()
                auto_combo_menu.select();
                og.views.common.layout.main.allowOverflow('north');
            });
        }
    }
});