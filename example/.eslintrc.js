module.exports = {
  root: true,
  extends: '@react-native',
  rules: {
    'react-native/no-inline-styles': 0,
    '@typescript-eslint/no-unused-vars': 0,
    'react-hooks/exhaustive-deps': 0,
    '@typescript-eslint/no-shadow': 0,
    'handle-callback-err': 0,
    eqeqeq: 0,
    'prettier/prettier': [
      'error',
      {
        'no-inline-styles': false,
        endOfLine: 'auto',
      },
    ],
  },
};
