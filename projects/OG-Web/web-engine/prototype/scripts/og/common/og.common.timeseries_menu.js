/**
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.TimeseriesMenu',
    dependencies: ['og.api.rest'],
    obj: function () {
        var api = og.api;
        /**
         * Builds a menu of related timeseries
         * Creates arrays of timseries data to send to the timeseries component
         *
         * @param {Object} result response from a timeseries api request
         * @param {String} selector a CSS selector to render the menu in
         * @param {Array} colors an array of colors to use for the different timeseries
         */
        return function (result, selector, colors) {
            var timeseries = this, $selector = $(selector), template, data = result.data, meta = {}, state = {};
            $selector.html('<span class="og-checking-related">checking for related timeseries data...</span>');
            var generate_menu = function () {
                // build handlerbars meta object
                var handlebar_obj = {fields: [], times: []}, f, t;
                for (f in meta) handlebar_obj.fields.push(f);
                for (t in meta[state.field]) handlebar_obj.times.push(t);
                // compile
                var $form = $((Handlebars.compile(template))(handlebar_obj)), ctr = 0;
                // Set selected options
                $form.find('select').val(state.field);
                $.each(state.time, function (i, time) {
                    $form.find('label span').contents().each(function (i, node) {
                        if (time === $(node).text())
                            $(this).parent().prev().prop('checked', 'checked').parent()
                                .css({'color': '#fff', 'background-color': colors[ctr]}), ctr += 1;
                    });
                });
                // attach handlers
                $form.find('select, input').change(function (event) {
                    var id_or_object, data = [], is_select = $(event.target).is('select'), $self = $(this);
                    var checkbox_handler = function () {
                        var new_time = $self.next().text(), index_of = state.time.indexOf(new_time);
                        id_or_object = meta[state.field][new_time];
                        if (~index_of) state.time.splice(index_of, 1); else state.time.push(new_time);
                        if (typeof id_or_object === 'object') return load_handler(id_or_object);
                        api.rest.timeseries.get({id: id_or_object}).pipe(load_handler);
                    };
                    var load_handler = function (obj) {
                        var td = obj.data.template_data, field = td.data_field, time = td.observation_time,
                            cached, cached_td, t;
                        state.field = field, meta[field][time] = obj;
                        if (is_select) {
                            state.time = [time];
                            data.push({
                                data: obj.data.timeseries.data,
                                data_provider: td.data_provider,
                                data_source: td.data_source,
                                label: time,
                                object_id: td.object_id
                            });
                        }
                        else for (t in state.time) {
                            if (!meta[state.field][state.time[t]]) continue;
                            cached = meta[state.field][state.time[t]].data;
                            cached_td = cached.template_data;
                            data.push({
                                data: cached.timeseries.data,
                                data_provider: cached_td.data_provider,
                                data_source: cached_td.data_source,
                                label: state.time[t],
                                object_id: cached_td.object_id
                            });
                        }
                        timeseries.update(data);
                        timeseries.datapoints.update(data);
                        $selector.html(generate_menu());
                    };
                    var select_handler = function () {
                        id_or_object = (function (obj) {return obj[Object.keys(obj)[0]];})(meta[$(event.target).val()]);
                        if (typeof id_or_object === 'object') return load_handler(id_or_object);
                        api.rest.timeseries.get({id: id_or_object}).pipe(load_handler);
                    };
                    !!is_select ? select_handler() : checkbox_handler();
                });
                return $form;
            };
            (function () { // init meta object
                var init_field = data.template_data.data_field, init_time = data.template_data.observation_time;
                if (result.data.related) result.data.related.forEach(function (val) {
                    var df = val.data_field;
                    if (!meta[df]) meta[df] = {};
                    meta[df][val.observation_time] = val.object_id;
                });
                meta[init_field][init_time] = result;
                state.field = init_field, state.time = [init_time];
            }())
            $.when(og.api.text({module: 'og.views.gadgets.timeseries.menu_tash'})).then(function (tmpl) {
                template = tmpl;
                $selector.html(generate_menu());
            })
        };
    }
});