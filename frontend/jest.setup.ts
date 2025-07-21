import 'jest-extended';

process.env.NODE_ENV = 'test';

declare global {
  namespace jest {
    interface Matchers<R> {
      toBeString(): R;

      toBeEmpty(): R;

      toBeTrue(): R;

      toBeFalse(): R;

      toStartWith(prefix: string): R;
    }
  }
}
