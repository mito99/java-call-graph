ARG ORACLE_VER=12.2.0.1.0
ARG ORACLE_SHORT_VER=12_2

FROM node:20-alpine AS build

WORKDIR /app

COPY . .

RUN npm install -g pnpm
RUN pnpm install
RUN pnpm build

FROM node:20-alpine AS runtime

WORKDIR /app

RUN npm install -g pnpm

COPY --from=build /app/package*.json ./
COPY --from=build /app/node_modules ./node_modules
COPY --from=build /app/.next ./.next

EXPOSE 3000

CMD ["pnpm", "start"]
