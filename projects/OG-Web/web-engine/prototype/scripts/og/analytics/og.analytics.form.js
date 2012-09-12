/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.form',
    dependencies: ['og.common.util.ui.AutoCombo', 'og.views.common.layout'],
    obj: function () { 
        return function (selector) {
            var emitter = new EventEmitter(), Form, FormCombo, Status, handle_error, ds_response = { // dummy
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
            };
            FormCombo = function (selector) {
                var FormCombo = this, Title, Menu;
                return Title = function (){
                    var title = this;
                    return title.$el = $('.og-option-title', selector).on('click', function (event) {
                            event.stopPropagation();
                            FormCombo.state === 'open' ? FormCombo.close() : FormCombo.open().focus();
                        }), title;
                },
                Menu = function () {
                    var menu = this, title = new Title();
                    return menu.state = 'closed', menu.$el = $(selector + ' .OG-analytics-form-menu'),
                        menu.focus = function () {
                            return menu.$el.find('select').first().focus(), menu; 
                        },
                        menu.open = function () {
                            return menu.$el.show().blurkill(menu.close), emitter.emitEvent('ee:open', title.$el),
                                title.$el.addClass('og-active'), menu.state = 'open', menu;
                        },
                        menu.close = function () { 
                            return (menu.$el ? menu.$el.hide() : null), title.$el.removeClass('og-active'),
                                menu.state = 'closed', menu;
                        }, menu;
                }, Menu.prototype = EventEmitter.prototype, FormCombo = new Menu();
            };
            Status = function (selector) {
                var status = this, interval, ini
                t = false;
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
                    if (!init) init = !!$(selector + ' button').removeClass('og-disabled');
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
            MastHead = function (template, search, aggregators) {
                var MastHead = this, Form, ag_selector = '.og-aggregation', ds_selector = '.og-datasources', ag_menu,
                    ds_menu, $form = $(selector).html(template), $ag = $(ag_selector, selector),
                    $ds = $(ds_selector, selector), status, auto_combo_menu;
                
                return Form = function(){
                    var form = this;
                    /**
                     * Custom tab event, only triggers between top level menu items. e.g. a tab event will trigger
                     * when tab entering or tab leaving the evented element
                     */
                    return $form.on('keydown', 'input, select, button', function (event) {
                        if (event.keyCode !== 9) return;
                        var $self = $(this), shift_key = event.shiftKey,
                            $aggr = $ag.find('input, select, button'),
                            $data = $ds.find('input, select, button'),
                            trigger = function (menu) {$self.trigger('tab', menu);},
                            active_pos = function (elms, pos) {return $self.is(elms[pos]())},
                            view = $self.closest('.og-view').length,
                            aggregation = $self.closest(ag_selector).length,
                            datasources = $self.closest(ds_selector).length,
                            load = $self.hasClass('og-load');
                        if (view && shift_key) return;
                        if (view && !shift_key) return trigger('aggregation');
                        if (aggregators && active_pos($aggr, 'last') && !shift_key) return trigger('datasources');
                        if (datasources && active_pos($data, 'first') && shift_key) return trigger('aggregation');
                        if (datasources && active_pos($data, 'last') && !shift_key) return trigger('load');
                        if (load && shift_key) return trigger('datasources');
                    }).on('tab', function (event, type) {
                        switch (type) {
                            case 'aggregation': ag_menu.open(); break;
                            case 'datasources': ds_menu.open(); break;
                            case 'load': ag_menu.close(); ds_menu.close(); break;
                        }
                    }).on('click', '.og-menu-aggregation button', function () {
                        var val = $(this).text();
                        if (val === 'OK') $(selector).trigger('tab', 'datasources'), ds_menu.focus();
                        if (val === 'Cancel') ag_menu.close(), auto_combo_menu.select();
                    }).on('click', '.og-menu-datasources button', function () {
                        var val = $(this).text();
                        if (val === 'OK') $(selector).trigger('tab', 'load'), $(selector + ' .og-load').focus();
                        if (val === 'Cancel') ds_menu.close(), auto_combo_menu.select();
                    }),
                    auto_combo_menu = new og.common.util.ui.AutoCombo('.OG-analytics-form .og-view',
                        'search...', search.data).on('input autocompletechange autocompleteselect',
                        function (event, ui) {
                            var $load = $(selector + ' .og-load');
                            if ((ui && ui.item && ui.item.value || $(this).val()) !== '') {
                                $load.removeClass('og-disabled').on('click', function () {
                                    status.play();
                             });
                            } else $load.addClass('og-disabled').off('click');
                        }
                    ),
                    $.when(
                        og.api.text({module: 'og.analytics.form_aggregation_tash'}),
                        og.api.text({module: 'og.analytics.form_datasources_tash'})
                    ).then(function (aggregation_markup, datasources_markup) {
                        $ag.html($((Handlebars.compile(aggregation_markup))(aggregators.data)));
                        $ds.html($((Handlebars.compile(datasources_markup))(ds_response)));
                        ag_menu = new FormCombo(ag_selector);
                        ds_menu = new FormCombo(ds_selector);
                    }),
                    emitter.addListener('ee:open', function (elm) {
                        if (!ag_menu || !ds_menu) return;
                        var elem = $(elm);
                        if (elem.closest('.og-view').length) ds_menu.close(), ag_menu.close();
                        if (elem.closest(ag_selector).length) ds_menu.close();
                        if (elem.closest(ds_selector).length) ag_menu.close();
                    }),
                    og.views.common.layout.main.allowOverflow('north'), status = new Status(selector + ' .og-status'),
                    form;
                }, Form.prototype = EventEmitter.prototype, MastHead = new Form();
            };
            $.when(
                og.api.text({module: 'og.analytics.form_tash'}),
                og.api.rest.viewdefinitions.get(),
                og.api.rest.aggregators.get()
            ).then(MastHead);
        };
    }
});