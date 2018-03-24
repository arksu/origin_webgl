const path = require('path');

module.exports = {
    mode: 'production',

    entry: {
        main: './src/main.js',
        vendor: ['three']
    },

    module: {
        rules: [
            {
                test: /\.js$/,
                exclude: /node_modules/,
                include: path.join(__dirname, 'src'),
                use: {
                    loader: "babel-loader"
                }
            }
        ]
    },

    optimization: {
        splitChunks: {
            cacheGroups: {
                commons: {
                    test: /[\\/]node_modules[\\/]/,
                    name: 'vendor',
                    chunks: 'all'
                }
            }
        }
    },

    output: {
        filename: '[name].bundle.js',
        path: path.resolve(__dirname, 'dist')
    }
};
