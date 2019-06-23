FROM clojure:boot

RUN mkdir -p /tmp/build /app/public/js
WORKDIR /tmp/build

COPY deploy/nginx.conf /etc/nginx/sites-available/app
COPY deploy/run.sh /app

RUN curl -sL https://deb.nodesource.com/setup_12.x | bash - && \
    apt-get -qq install build-essential nodejs nginx && \
    ln -s /etc/nginx/sites-available/app /etc/nginx/sites-enabled/

COPY . .

RUN boot build-clj && \
    mv ./target/cogni.jar /app && \
    npm install && \
    boot build-cljs && \
    mv ./public/js/main.js /app/public/js && \
    cd / && rm -rf /tmp/build

WORKDIR /app

CMD ["/bin/bash", "run.sh"]
