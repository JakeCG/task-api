FROM node:18-alpine

WORKDIR /app

COPY package.json yarn.lock .yarnrc.yml ./

RUN corepack enable && \
    corepack prepare yarn@3.8.2 --activate && \
    yarn install --frozen-lockfile

COPY . .

RUN yarn build:prod

USER node:node

EXPOSE 3000

CMD ["yarn", "start"]
