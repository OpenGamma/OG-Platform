/**
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.slickgrid.formatters.timeseries',
    dependencies: [],
    obj: function () {
        return function (row, cell, value) {
            if (!value) return '<span class=og-no-content>-</span>';
            var date_info = /(?::[SE]:[0-9]{4}-[0-9]{2}-[0-9]{2}){1,2}/.exec(value) + '',
                end_date = /E:[0-9]{4}-[0-9]{2}-[0-9]{2}/.exec(value),
                start_date = /:S:[0-9]{4}-[0-9]{2}-[0-9]{2}/.exec(value),
                non_date_info = value.replace(date_info, ''),
                stopped = 'OG-icon og-clock-stopped', active = 'OG-icon og-clock-active', css_class, return_data;
            if (start_date) {
                if (end_date)
                    (css_class = stopped), date_info = date_info.replace(':S:', 'Start: ').replace(':E:', ' - End: ');
                else
                    (css_class = active), date_info = date_info.replace(':S:', 'Start: ');
            } else {
                if (end_date) (css_class = stopped), date_info = date_info.replace(':E:', 'Start: ? - End: ');
            }
            if (css_class) return_data = '<span class="' + css_class + '" title="' + date_info + '">' +
                '<span>' + non_date_info + '</span></span>';
            return return_data || '<span class="og-no-clock"><span>' + value + '</span></span>';
        };
    }
});