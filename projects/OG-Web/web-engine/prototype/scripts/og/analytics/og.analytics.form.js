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
                            return menu.$el.show().blurkill(menu.close), emitter.emitEvent('menu:open', {
                                elem: title.$el, state: (menu.state = 'open'), scope: menu}),
                                title.$el.addClass('og-active'), menu;
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
            MastHead = function (template, search, aggregators) { // TODO AG: Keep things as DRY as poss!
                var MastHead = this, Form, ag_menu, ds_menu, vd_s = '.og-view', ag_s = '.og-aggregation',
                    ds_s = '.og-datasources', load_s = '.og-load', fcntrls_s = 'input, select, button',
                    $form = $(selector).html(template), $ag = $(ag_s, $form), $ds = $(ds_s, $form),
                    $ag_fcntrls, $ds_fcntrls, $load_btn = $(load_s, $form), status, auto_combo_menu;
                return Form = function(){
                    var form = this,
                        keydown_handler = function (event) {
                            if (event.keyCode !== 9) return;
                            var $elem = $(this), shift_key = event.shiftKey,
                                trigger = function (menu) {$elem.trigger('tab', menu);},
                                active_pos = function (elms, pos) {return $elem.is(elms[pos]())},
                                view = $elem.closest('.og-view').length,
                                aggregation = $elem.closest(ag_s).length,
                                datasources = $elem.closest(ds_s).length,
                                load = $elem.hasClass('og-load');
                            if (view && shift_key)
                                return; //view.find('ui-autocomplete-input').focus();
                            if (view && !shift_key)
                                return trigger('aggregation');
                            if (aggregation && active_pos($ag_fcntrls, 'last') && !shift_key) 
                                return trigger('datasources');
                            if (aggregation && active_pos($ag_fcntrls, 'first') && shift_key) 
                                return trigger('view');
                            if (datasources && active_pos($ds_fcntrls, 'first') && shift_key) 
                                return trigger('aggregation');
                            if (datasources && active_pos($ds_fcntrls, 'last') && !shift_key)
                                return trigger('load');
                            if (load && shift_key)
                                return trigger('datasources');
                        },
                        tab_handler = function (event, type) {
                            switch (type) {
                                case 'aggregation': ag_menu.open(); ds_menu.close(); break;
                                case 'datasources': ds_menu.open(); ag_menu.close(); break;
                                case 'load':  ag_menu.close(); ds_menu.close(); break;
                                case 'view': ag_menu.close(); ds_menu.close(); break;

                            }
                        },
                        click_handler = function (event) {
                           var $elem = $(event.srcElement), txt_val = $elem.text(), type;
                           if ($elem.is('button')) {
                                type = $elem.parent('.og-menu-datasources') ? 'datasources' :
                                    ($elem.parent('.og-menu-datasources') ? 'load' : '');    
                                $(selector).trigger('tab', type);
                                if (txt_val === 'OK') {
                                    if (type === 'datasources') ds_menu.focus();
                                    if (type === 'load') $load_btn.focus();
                                } else if (txt_val === 'Cancel') { 
                                    if (type === 'datasources') ds_menu.close(); 
                                    if (type === 'load') ag_menu.close();
                                }
                                auto_combo_menu.select();
                            }
                            event.preventDefault();
                            return false;
                        };
                    return $form.on('keydown', fcntrls_s, keydown_handler)
                            .on('tab', tab_handler)
                            .on('click', click_handler),
                        auto_combo_menu = new og.common.util.ui.AutoCombo('.OG-analytics-form .og-view',
                            'search...', search.data).on('input autocompletechange autocompleteselect',
                                function (event, ui) {
                                    if ((ui && ui.item && ui.item.value || $(this).val()) !== '') {
                                        $load_btn.removeClass('og-disabled').on('click', function () {status.play();});
                                    } else $load_btn.addClass('og-disabled').off('click');
                                }
                        ),
                        $.when(
                            og.api.text({module: 'og.analytics.form_aggregation_tash'}),
                            og.api.text({module: 'og.analytics.form_datasources_tash'})
                        ).then(function (aggregation_markup, datasources_markup) {
                            $ag.html($((Handlebars.compile(aggregation_markup))(aggregators.data))),
                            $ds.html($((Handlebars.compile(datasources_markup))(ds_response))),
                            $ag_fcntrls = $ag.find(fcntrls_s), $ds_fcntrls = $ds.find(fcntrls_s),
                            ag_menu = new FormCombo(ag_s), ds_menu = new FormCombo(ds_s);
                        }),
                        emitter.addListener('menu:open', function (elm) {
                            if (!ag_menu || !ds_menu) return;
                            var elem = $(elm);
                            if (elem.closest('.og-view').length) ds_menu.close(), ag_menu.close();
                            if (elem.closest(ag_s).length) ds_menu.close();
                            if (elem.closest(ds_s).length) ag_menu.close();
                        }),
                        og.views.common.layout.main.allowOverflow('north'), 
                        status = new Status(selector + ' .og-status'), form;
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