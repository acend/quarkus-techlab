FROM klakegg/hugo:0.107.0-ext-ubuntu AS builder

ARG TRAINING_HUGO_ENV=default

COPY . /src

RUN hugo --environment ${TRAINING_HUGO_ENV} --minify

FROM nginxinc/nginx-unprivileged:1.24-alpine

EXPOSE 8080

COPY --from=builder /src/public /usr/share/nginx/html
