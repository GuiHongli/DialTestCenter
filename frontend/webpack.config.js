const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
  entry: './src/main.jsx',
  output: {
    path: path.resolve(__dirname, '../dialingtest-service/src/main/resources/static'),
    filename: 'bundle.js',
    clean: true,
    publicPath: '/',
  },
  module: {
    rules: [
      {
        test: /\.(js|jsx)$/,
        use: {
          loader: 'babel-loader',
          options: {
            presets: ['@babel/preset-env', '@babel/preset-react']
          }
        },
        exclude: /node_modules/,
      },
      {
        test: /\.css$/i,
        use: ['style-loader', 'css-loader'],
      },
    ],
  },
  resolve: {
    extensions: ['.jsx', '.js'],
  },
  plugins: [
    new HtmlWebpackPlugin({
      template: './index.html',
      filename: 'index.html',
      inject: true,
    }),
  ],
  devServer: {
    static: {
      directory: path.join(__dirname, 'public'),
    },
    port: 4396,
    hot: true,
    historyApiFallback: true,
    client: {
      overlay: {
        errors: true,
        warnings: false,
        runtimeErrors: (error) => {
          const errorMessage = error.message || '';
          if (errorMessage.includes('ResizeObserver loop completed with undelivered notifications')) {
            return false;
          }
          return true;
        },
      },
    },
    proxy: {
      '/dialingtest/api': {
        target: 'https://localhost:8087',
        changeOrigin: true,
        secure: false,
      },
    },
  },
};