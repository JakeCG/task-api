import { Request, Response, NextFunction } from 'express';

import { errorHandler } from '@/main/middleware/errorHandler';

describe('errorHandler middleware', () => {
  let mockRequest: Partial<Request>;
  let mockResponse: Partial<Response>;
  let mockNext: NextFunction;

  beforeEach(() => {
    mockRequest = {};
    mockResponse = {
      status: jest.fn().mockReturnThis(),
      render: jest.fn(),
    };
    mockNext = jest.fn();

    jest.spyOn(console, 'error').mockImplementation(() => {
    });
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  describe('Axios errors with response', () => {
    it('should handle 404 errors with custom message', () => {
      const error = {
        isAxiosError: true,
        response: {
          status: 404,
          data: { message: 'Task with ID 123 not found' },
        },
      };

      errorHandler(error, mockRequest as Request, mockResponse as Response, mockNext);

      expect(mockResponse.status).toHaveBeenCalledWith(404);
      expect(mockResponse.render).toHaveBeenCalledWith('error', {
        title: 'Task not found',
        message: 'The task you are looking for could not be found.',
      });
      expect(console.error).toHaveBeenCalledWith('Error:', error);
    });

    it('should handle 400 bad request with detail message', () => {
      const error = {
        isAxiosError: true,
        response: {
          status: 400,
          data: { detail: 'Invalid task data provided' },
        },
      };

      errorHandler(error, mockRequest as Request, mockResponse as Response, mockNext);

      expect(mockResponse.status).toHaveBeenCalledWith(400);
      expect(mockResponse.render).toHaveBeenCalledWith('error', {
        title: 'Error',
        message: 'Invalid task data provided',
      });
    });

    it('should handle 401 unauthorized errors', () => {
      const error = {
        isAxiosError: true,
        response: {
          status: 401,
          data: { message: 'Authentication required' },
        },
      };

      errorHandler(error, mockRequest as Request, mockResponse as Response, mockNext);

      expect(mockResponse.status).toHaveBeenCalledWith(401);
      expect(mockResponse.render).toHaveBeenCalledWith('error', {
        title: 'Error',
        message: 'Authentication required',
      });
    });

    it('should handle 500 server errors', () => {
      const error = {
        isAxiosError: true,
        response: {
          status: 500,
          data: { message: 'Internal server error' },
        },
      };

      errorHandler(error, mockRequest as Request, mockResponse as Response, mockNext);

      expect(mockResponse.status).toHaveBeenCalledWith(500);
      expect(mockResponse.render).toHaveBeenCalledWith('error', {
        title: 'Error',
        message: 'Internal server error',
      });
    });

    it('should use default message when no detail or message provided', () => {
      const error = {
        isAxiosError: true,
        response: {
          status: 400,
          data: {},
        },
      };

      errorHandler(error, mockRequest as Request, mockResponse as Response, mockNext);

      expect(mockResponse.status).toHaveBeenCalledWith(400);
      expect(mockResponse.render).toHaveBeenCalledWith('error', {
        title: 'Error',
        message: 'An error occurred',
      });
    });

    it('should prioritize detail over message field', () => {
      const error = {
        isAxiosError: true,
        response: {
          status: 400,
          data: {
            detail: 'Detailed error message',
            message: 'Generic error message',
          },
        },
      };

      errorHandler(error, mockRequest as Request, mockResponse as Response, mockNext);

      expect(mockResponse.render).toHaveBeenCalledWith('error', {
        title: 'Error',
        message: 'Detailed error message',
      });
    });

    it('should handle response data as null', () => {
      const error = {
        isAxiosError: true,
        response: {
          status: 400,
          data: null,
        },
      };

      errorHandler(error, mockRequest as Request, mockResponse as Response, mockNext);

      expect(mockResponse.render).toHaveBeenCalledWith('error', {
        title: 'Error',
        message: 'An error occurred',
      });
    });
  });

  describe('Axios errors without response (network errors)', () => {
    it('should handle network/connection errors', () => {
      const error = {
        isAxiosError: true,
        request: {},
        message: 'Network Error',
      };

      errorHandler(error, mockRequest as Request, mockResponse as Response, mockNext);

      expect(mockResponse.status).toHaveBeenCalledWith(503);
      expect(mockResponse.render).toHaveBeenCalledWith('error', {
        title: 'Service unavailable',
        message: 'Unable to connect to the task service. Please try again later.',
      });
    });

    it('should handle timeout errors', () => {
      const error = {
        isAxiosError: true,
        request: {},
        code: 'ECONNABORTED',
        message: 'timeout of 1000ms exceeded',
      };

      errorHandler(error, mockRequest as Request, mockResponse as Response, mockNext);

      expect(mockResponse.status).toHaveBeenCalledWith(503);
      expect(mockResponse.render).toHaveBeenCalledWith('error', {
        title: 'Service unavailable',
        message: 'Unable to connect to the task service. Please try again later.',
      });
    });
  });

  describe('Non-Axios errors', () => {
    it('should handle generic JavaScript errors', () => {
      const error = new Error('Something went wrong');

      errorHandler(error, mockRequest as Request, mockResponse as Response, mockNext);

      expect(mockResponse.status).toHaveBeenCalledWith(500);
      expect(mockResponse.render).toHaveBeenCalledWith('error', {
        title: 'Something went wrong',
        message: 'An unexpected error occurred. Please try again.',
      });
      expect(console.error).toHaveBeenCalledWith('Error:', error);
    });

    it('should handle TypeError', () => {
      const error = new TypeError('Cannot read property of undefined');

      errorHandler(error, mockRequest as Request, mockResponse as Response, mockNext);

      expect(mockResponse.status).toHaveBeenCalledWith(500);
      expect(mockResponse.render).toHaveBeenCalledWith('error', {
        title: 'Something went wrong',
        message: 'An unexpected error occurred. Please try again.',
      });
    });

    it('should handle null errors', () => {
      const error = null;

      errorHandler(error, mockRequest as Request, mockResponse as Response, mockNext);

      expect(mockResponse.status).toHaveBeenCalledWith(500);
      expect(mockResponse.render).toHaveBeenCalledWith('error', {
        title: 'Something went wrong',
        message: 'An unexpected error occurred. Please try again.',
      });
    });

    it('should handle string errors', () => {
      const error = 'String error message';

      errorHandler(error, mockRequest as Request, mockResponse as Response, mockNext);

      expect(mockResponse.status).toHaveBeenCalledWith(500);
      expect(mockResponse.render).toHaveBeenCalledWith('error', {
        title: 'Something went wrong',
        message: 'An unexpected error occurred. Please try again.',
      });
    });
  });

  describe('Edge cases', () => {
    it('should handle Axios error with neither response nor request', () => {
      const error = {
        isAxiosError: true,
        message: 'Axios configuration error',
      };

      errorHandler(error, mockRequest as Request, mockResponse as Response, mockNext);

      expect(mockResponse.status).toHaveBeenCalledWith(500);
      expect(mockResponse.render).toHaveBeenCalledWith('error', {
        title: 'Something went wrong',
        message: 'An unexpected error occurred. Please try again.',
      });
    });

    it('should handle errors with circular references', () => {
      const error: any = { isAxiosError: false };
      error.circular = error;

      expect(() => {
        errorHandler(error, mockRequest as Request, mockResponse as Response, mockNext);
      }).not.toThrow();

      expect(mockResponse.status).toHaveBeenCalledWith(500);
    });

    it('should never call next function', () => {
      const error = new Error('Test error');

      errorHandler(error, mockRequest as Request, mockResponse as Response, mockNext);

      expect(mockNext).not.toHaveBeenCalled();
    });
  });

  describe('Response behavior', () => {
    it('should always call status before render', () => {
      const error = new Error('Test');

      errorHandler(error, mockRequest as Request, mockResponse as Response, mockNext);

      const statusOrder = (mockResponse.status as jest.Mock).mock.invocationCallOrder[0];
      const renderOrder = (mockResponse.render as jest.Mock).mock.invocationCallOrder[0];

      expect(statusOrder).toBeLessThan(renderOrder);
    });

    it('should return response for chaining', () => {
      const error = new Error('Test');

      errorHandler(error, mockRequest as Request, mockResponse as Response, mockNext);

      expect(mockResponse.status).toHaveReturnedWith(mockResponse);
    });
  });
});
