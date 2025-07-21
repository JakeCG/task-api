import path from 'path';

import express, { Application, Request, Response } from 'express';
import bodyParser from 'body-parser';
import cookieParser from 'cookie-parser';

import { configureNunjucks } from '@/main/config/nunjucks';
import { taskRoutes } from '@/main/routes/tasks';
import { errorHandler } from '@/main/middleware/errorHandler';

const app: Application = express();
const port = process.env.PORT ?? 3000;

// Configure Nunjucks
configureNunjucks(app);

// Middleware
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(cookieParser());

app.use('/assets', express.static(path.join(__dirname, '../../dist/assets')));
app.use(
  '/assets',
  express.static(path.join(__dirname, '../../node_modules/govuk-frontend/dist/govuk/assets'))
);
app.use(
  '/govuk',
  express.static(path.join(__dirname, '../../node_modules/govuk-frontend/dist/govuk'))
);

app.use('/tasks', taskRoutes);

app.get('/', (_req: Request, res: Response) => {
  res.redirect('/tasks');
});

// Health check
app.get('/health', (_req: Request, res: Response) => {
  res.json({ status: 'UP' });
});

// Error handling
app.use((_req: Request, res: Response) => {
  res.status(404).render('error', {
    title: 'Page not found',
    message: 'The page you are looking for could not be found.',
  });
});

app.use(errorHandler);

// Start server
app.listen(port, () => {
  console.log(`Server is running on http://localhost:${port}`);
});
