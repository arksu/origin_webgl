const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
    mode: 'development',
    entry: './src/main.ts',
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                use: 'ts-loader',
                exclude: '/node_modules/'
            },
        ],
    },
    plugins: [
        new HtmlWebpackPlugin({
            hash: true,
            title: 'Origin',
            template: "./src/index.html",
            filename: '../dist/index.html'
        })
    ],
    output: {
        filename: '[name].bundle.js',
        path: path.resolve(path.join(__dirname, "..", "dist")),
    },
    devServer: {
        compress: true,
        overlay: true
    },
    target: "web"
};