FROM clojure:boot

RUN mkdir /app
WORKDIR /tmp/build

RUN curl -sL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get -qq install nodejs

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

CMD ["java", "-jar", "cogni.jar"]
