import path from 'node:path';

import nunjucks from 'nunjucks';
import { Application } from 'express';

export function configureNunjucks(app: Application): void {
  const viewsPath = path.join(__dirname, '../views');

  // eslint-disable-next-line import/no-named-as-default-member
  const env = nunjucks.configure(viewsPath, {
    autoescape: true,
    express: app,
    noCache: process.env.NODE_ENV !== 'production',
    watch: process.env.NODE_ENV === 'development',
  });

  env.addFilter('formatDate', (date: string) => {
    if (!date) return '';
    const formattedDate = new Date(date);
    return formattedDate.toLocaleDateString('en-GB', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  });

  env.addFilter('statusBadge', (status: string) => {
    const statusBadges: Record<string, string> = {
      TODO: 'govuk-tag--grey',
      IN_PROGRESS: 'govuk-tag--blue',
      COMPLETED: 'govuk-tag--green',
      CANCELLED: 'govuk-tag--red',
    };
    return statusBadges[status] || '';
  });

  app.set('view engine', 'njk');
  app.set('views', viewsPath);
}
