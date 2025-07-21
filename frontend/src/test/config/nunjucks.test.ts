const mockEnv = {
  addFilter: jest.fn(),
  getFilter: jest.fn(),
};

jest.mock('nunjucks', () => ({
  configure: jest.fn(() => mockEnv),
}));

import express from 'express';

import { configureNunjucks } from '@/main/config/nunjucks';

type FormatDateFilter = (date: string | null | undefined) => string;
type StatusBadgeFilter = (status: string | null | undefined) => string;
type FilterFunction = FormatDateFilter | StatusBadgeFilter;

describe('Nunjucks Configuration', () => {
  let app: express.Application;
  const filters: Record<string, FilterFunction> = {};

  beforeEach(() => {
    app = express();
    jest.clearAllMocks();

    mockEnv.addFilter.mockImplementation((name: string, fn: FilterFunction) => {
      filters[name] = fn;
    });

    mockEnv.getFilter.mockImplementation((name: string) => filters[name]);

    configureNunjucks(app);
  });

  describe('Express configuration', () => {
    it('should set the correct view engine', () => {
      expect(app.get('view engine')).toBe('njk');
    });

    it('should set the views directory', () => {
      const viewsPath = app.get('views');
      expect(typeof viewsPath).toBe('string');
      expect(viewsPath).toContain('views');
    });
  });

  describe('formatDate filter', () => {
    let formatDate: FormatDateFilter;

    beforeEach(() => {
      const addFilterCalls = mockEnv.addFilter.mock.calls;
      const formatDateCall = addFilterCalls.find(call => call[0] === 'formatDate');
      formatDate = formatDateCall[1] as FormatDateFilter;
    });

    it('should format a valid date correctly', () => {
      const result = formatDate('2024-12-25T14:30:00Z');

      expect(typeof result).toBe('string');
      expect(result).toContain('25');
      expect(result).toContain('Dec');
      expect(result).toContain('2024');
    });

    it('should return empty string for null date', () => {
      expect(formatDate(null)).toBe('');
    });

    it('should return empty string for empty string', () => {
      expect(formatDate('')).toBe('');
    });

    it('should handle invalid date strings', () => {
      const result = formatDate('invalid-date');
      expect(result).toContain('Invalid Date');
    });
  });

  describe('statusBadge filter', () => {
    let statusBadge: StatusBadgeFilter;

    beforeEach(() => {
      const addFilterCalls = mockEnv.addFilter.mock.calls;
      const statusBadgeCall = addFilterCalls.find(call => call[0] === 'statusBadge');
      statusBadge = statusBadgeCall[1] as StatusBadgeFilter;
    });

    test.each([
      ['TODO', 'govuk-tag--grey'],
      ['IN_PROGRESS', 'govuk-tag--blue'],
      ['COMPLETED', 'govuk-tag--green'],
      ['CANCELLED', 'govuk-tag--red'],
      ['UNKNOWN', ''],
      ['', ''],
      [null, ''],
      [undefined, ''],
    ])('statusBadge(%s) should return %s', (input, expected) => {
      expect(statusBadge(input)).toBe(expected);
    });

    it('should be case sensitive', () => {
      expect(statusBadge('todo')).toBe('');
    });
  });
});
