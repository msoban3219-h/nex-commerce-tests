FROM markhobson/maven-chrome

WORKDIR /tests

COPY . .

CMD ["mvn", "test"]