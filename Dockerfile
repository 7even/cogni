FROM clojure:boot

RUN mkdir /app
WORKDIR /tmp/build

COPY deploy/nginx.conf /etc/nginx/sites-available/app
COPY deploy/run.sh /app

RUN curl -sL https://deb.nodesource.com/setup_12.x | bash - && \
    apt-get -qq install build-essential nodejs nginx && \
    ln -s /etc/nginx/sites-available/app /etc/nginx/sites-enabled/

COPY . .

RUN npm install && \
    boot build-cljs && \
    cd resources/public/js && \
    find . -type f ! -name 'main.js' -delete && \
    cd ../../.. && \
    boot build-clj && \
    mv ./target/cogni.jar /app && \
    rm -rf /tmp/build

WORKDIR /app

CMD ["/bin/bash", "run.sh"]
