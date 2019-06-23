FROM clojure:boot

RUN mkdir /tmp/build /app
WORKDIR /tmp/build

RUN curl -sL https://deb.nodesource.com/setup_12.x | bash - && \
    apt-get -y install build-essential nodejs

COPY . .

RUN boot build-clj && \
    mv ./target/cogni.jar /app && \
    npm install && \
    boot build-cljs && \
    mv ./public/js/main.js /app && \
    cd / && rm -rf /tmp/build

WORKDIR /app

CMD ["java", "-jar", "cogni.jar"]
