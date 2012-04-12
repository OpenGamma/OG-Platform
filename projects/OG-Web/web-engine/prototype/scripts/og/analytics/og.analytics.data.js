/*
 * @copyright 2012 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.analytics.Data',
    dependencies: ['og.api.rest'],
    obj: function () {
        return function (config) {
            var data = this, data_handler, initialize, events = {init: null, data: null};
            data_handler = function (result) {
                if (!events.data) return;
                var matrix = [], rows = 60;
                while (rows--) matrix.push(function (cols, row) {
                    while (cols--) row.push(Math.random() * 1000);
                    return row;
                }(19, []));
                events.data(matrix);
                setTimeout(data_handler, 5000);
            };
            initialize = function () {
                events.init({
                    'fixed': [
                        {name: 'Fixed 1', width: 250},
                        {name: 'Fixed 2', width: 100}
                    ],
                    'scroll': [
                        {name: 'Column 3', width: 100},
                        {name: 'Column 4', width: 100},
                        {name: 'Column 5', width: 100},
                        {name: 'Column 6', width: 100},
                        {name: 'Column 7', width: 100},
                        {name: 'Column 8', width: 100},
                        {name: 'Column 9', width: 100},
                        {name: 'Column 10', width: 100},
                        {name: 'Column 11', width: 100},
                        {name: 'Column 12', width: 100},
                        {name: 'Column 13', width: 100},
                        {name: 'Column 14', width: 100},
                        {name: 'Column 15', width: 100},
                        {name: 'Column 16', width: 100},
                        {name: 'Column 17', width: 100},
                        {name: 'Column 18', width: 100},
                        {name: 'Column 19', width: 100}
                    ]
                });
                data_handler();
            };
            data.on = function (name, handler) {
                if (name in events) events[name] = handler;
                if (name === 'init') initialize();
            };
        };
    }
});