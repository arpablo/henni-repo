const path = require('path'),
    webpack = require('webpack'),
    HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
    entry: {
        app: './src/main/reactapp/app/App.tsx',
        vendor: ['react', 'react-dom']
    },
    output: {
        path: path.resolve(__dirname, 'target/www'),
        filename: 'js/[name].bundle.js'
    },
    resolve: {
        extensions: ['.js', '.jsx', '.json', '.ts', '.tsx']
    },
    module: {
        rules: [
            {
                test: /\.(ts|tsx)$/,
                loader: 'ts-loader'
            },
            { enforce: "pre", test: /\.js$/, loader: "source-map-loader" }
        ]
    },
    plugins: [
        new HtmlWebpackPlugin({ template: path.resolve(__dirname, 'src/main/reactapp/index.html') }),
        new webpack.HotModuleReplacementPlugin()
    ],
    performance: {
        maxEntrypointSize: 512000,
        maxAssetSize: 512000
      }
}