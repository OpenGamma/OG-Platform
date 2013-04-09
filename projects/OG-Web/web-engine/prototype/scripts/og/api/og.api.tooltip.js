$.register_module({
    name: 'og.api.tooltip',
    dependencies: [],
    obj: function () {
        var store = {
            '1': 'Lorem ipsum dolor sit amet, consectetur adipiscing elit.'+
                 'Nullam consectetur quam a sapien egestas eget scelerisque'+
                 'lectus tempor. Duis placerat tellus at erat pellentesque nec'+
                 'ultricies erat molestie. Integer nec orci id tortor molestie'+
                 'porta. Suspendisse eu sagittis quam.'
        }
        return {
            get_tip = function (id) { return store[id]; }
        }
    }
});