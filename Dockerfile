FROM klakegg/hugo:0.111.3-ext-ubuntu AS builder

ARG TRAINING_HUGO_ENV=default

COPY . /src

RUN hugo --environment ${TRAINING_HUGO_ENV} --minify

FROM nginxinc/nginx-unprivileged:1.25-alpine

EXPOSE 8080

COPY --from=builder /src/public /usr/share/nginx/html
