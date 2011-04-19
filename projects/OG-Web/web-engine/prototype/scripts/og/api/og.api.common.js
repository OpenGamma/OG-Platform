$.register_module({
    name: 'og.api.common',
    dependencies: [],
    obj: function () {
        return {
            end_loading: function () {/*global ajax loading events end here*/},
            start_loading: function (loading_method) {
                if (loading_method) loading_method();
                /*global ajax loading events start here*/
            }
        };
    }
});