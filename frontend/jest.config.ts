import type { Config } from 'jest';

const config: Config = {
  preset: 'jest-preset-angular',
  testEnvironment: 'jsdom',

  setupFilesAfterEnv: ['<rootDir>/setup-jest.ts'],

  testMatch: ['**/*.spec.ts'],

  collectCoverage: true,
  coverageDirectory: 'coverage',
  coverageReporters: ['lcov', 'text-summary'],

  moduleFileExtensions: ['ts', 'js'],

  transform: {
    '^.+\\.(ts|mjs|js)$': 'jest-preset-angular'
  },

  moduleNameMapper: {
    '\\.(html|css|scss)$': '<rootDir>/__mocks__/fileMock.js'
  },

  transformIgnorePatterns: ['node_modules/(?!.*\\.mjs$)']
};

export default config;
