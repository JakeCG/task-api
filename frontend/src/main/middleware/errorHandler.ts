import { Request, Response, NextFunction } from 'express';

// eslint-disable-next-line @typescript-eslint/no-unused-vars, @typescript-eslint/no-explicit-any
export function errorHandler(err: any, _req: Request, res: Response, _next: NextFunction): void {
  console.error('Error:', err);

  if (!err) {
    res.status(500).render('error', {
      title: 'Something went wrong',
      message: 'An unexpected error occurred. Please try again.',
    });
    return;
  }

  if (err.isAxiosError) {
    if (err.response) {
      const status = err.response.status;
      const message =
        err.response.data?.detail || err.response.data?.message || 'An error occurred';

      if (status === 404) {
        res.status(404).render('error', {
          title: 'Task not found',
          message: 'The task you are looking for could not be found.',
        });
        return;
      }

      res.status(status).render('error', {
        title: 'Error',
        message: message,
      });
      return;
    } else if (err.request) {
      res.status(503).render('error', {
        title: 'Service unavailable',
        message: 'Unable to connect to the task service. Please try again later.',
      });
      return;
    }
  }

  res.status(500).render('error', {
    title: 'Something went wrong',
    message: 'An unexpected error occurred. Please try again.',
  });
}
