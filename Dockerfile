FROM clojure:boot

RUN mkdir /tmp/build /app
WORKDIR /tmp/build

COPY . .

RUN boot build && \
    mv ./target/cogni.jar /app && \
    cd / && rm -rf /tmp/build

WORKDIR /app

CMD ["java", "-jar", "cogni.jar"]
