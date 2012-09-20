/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.form',
    dependencies: [
        'og.common.util.ui.AutoCombo',
        'og.analytics.dropmenu.aggregators',
        'og.analytics.dropmenu.datasources',
        'og.views.common.layout'
    ],
    obj: function () { 
        return function (selector) {
            var emitter = new EventEmitter(), Form, ag_dropmenu = og.analytics.dropmenu.aggregators, 
                ds_dropmenu = og.analytics.dropmenu.aggregators, Status, events = {
                    focus: 'dropmenu:focus',
                    focused:'dropmenu:focused',
                    open: 'dropmenu:open',
                    opened: 'dropmenu:opened',
                    close: 'dropmenu:close',
                    closed: 'dropmenu:closed',
                    closeall: 'dropmenu:closeall'
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
            MastHead = function (template, search, aggregators, datasource) { //TODO AG: Keep things DRY as poss!
                var MastHead = this, Form, ag_dropdwn, ds_dropdwn, vd_s = '.og-view',
                    fcntrls_s = 'input, select, button', ac_s = 'input autocompletechange autocompleteselect', 
                    $form = $(selector).html(template), $ag = $('.og-aggregation', $form), 
                    $ds = $('.og-datasources', $form), $ag_fcntrls, $ds_fcntrls, $load_btn = $('.og-load', $form),
                    status, ac_menu;
                return Form = function(){
                    var form = this,
                        keydown_handler = function (event) {
                            if (event.keyCode !== 9) return;
                            var $elem = $(this), shift_key = event.shiftKey,
                                active_pos = function (elms, pos) {return $elem.is(elms[pos]())}
                            if (!shift_key && ac_menu.state === 'focused') ag_dropdwn.emitEvent(events.open);
                            if (!shift_key && active_pos($ag_fcntrls,'last')) ds_dropdwn.emitEvent(events.open);
                            if (!shift_key && active_pos($ds_fcntrls, 'last')) ds_dropdwn.emitEvent(events.close);
                            if (shift_key && $elem.is($load_btn)) ds_dropdwn.emitEvent(events.open);
                            if (shift_key && active_pos($ds_fcntrls, 'first')) ag_dropdwn.emitEvent(events.open);
                            if (shift_key && active_pos($ag_fcntrls, 'first')) ag_dropdwn.emitEvent(events.close);
                        },
                        button_handler = function (val) {
                            if (val === 'OK') {
                                if (ag_dropdwn.opened()) {
                                    ds_dropdwn.emitEvent(events.open).emitEvent(events.focus);
                                } else if (ds_dropdwn.opened()) {
                                    ds_dropdwn.emitEvent(events.close); 
                                    $load_btn.focus();
                                }
                            } else if (val === 'Cancel') {
                                emitter.emitEvent(events.closeall);
                                ac_menu.$input.select(); 
                            }
                        },
                        click_handler = function (event) {
                            var $elem = $(event.srcElement);
                            if ($elem.is('button')) button_handler($elem.text());
                        },
                        close_dropmenu = function (menu) {
                            if (menu.state() === 'open'|| menu.state() === 'focused') menu.emitEvent(events.close);
                        },
                        auto_combo_handler = function (even, ui) {
                            if ((ui && ui.item && ui.item.value || $(this).val()) !== '') {
                                $load_btn.removeClass('og-disabled').on('click', function () {status.play();});
                            } else $load_btn.addClass('og-disabled').off('click');
                        };
                    return $form.on('keydown', fcntrls_s, keydown_handler).on('click', click_handler),
                        ac_menu = new og.common.util.ui.AutoCombo(selector+' '+vd_s,'search...', search.data),
                        ac_menu.$input.on(ac_s, auto_combo_handler).select(),
                        $.when(
                            og.api.text({module: 'og.analytics.form_aggregation_tash'}),
                            og.api.text({module: 'og.analytics.form_datasources_tash'})
                        ).then(function (aggregation_markup, datasources_markup) {
                            ag_dropdwn = new ag_dropmenu({$cntr: $ag, tmpl: aggregation_markup, data: aggregators.data})
                                .addListener(events.open, function () {close_dropmenu(ds_dropdwn);}),
                            ds_dropdwn = new ds_dropmenu({$cntr: $ds, tmpl: datasources_markup, data: datasource.data})
                                .addListener(events.open, function () {close_dropmenu(ag_dropdwn);}),
                            $ag_fcntrls = $ag.find(fcntrls_s), $ds_fcntrls = $ds.find(fcntrls_s);
                        }),
                        emitter.addListener(events.closeall, function () {
                            close_dropmenu(ag_dropdwn);
                            close_dropmenu(ds_dropdwn);
                        }),
                        og.views.common.layout.main.allowOverflow('north'), 
                        status = new Status(selector + ' .og-status'),
                        form;
                }, Form.prototype = EventEmitter.prototype, MastHead = new Form();
            };
            $.when(
                og.api.text({module: 'og.analytics.form_tash'}),
                og.api.rest.viewdefinitions.get(),
                og.api.rest.aggregators.get(),
                {data: ['Live', 'Snapshot', 'Historical']}
            ).then(MastHead);
        };
    }
});