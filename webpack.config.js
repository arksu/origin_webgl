var path = require('path');


module.exports = {
    entry: './src/main.js',
    output: {
        pathinfo  : true,
        path      : path.resolve(__dirname, 'dist'),
        publicPath: './dist/',
        filename  : 'bundle.js'
    }
};