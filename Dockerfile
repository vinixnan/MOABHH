FROM maven:3.6.3-jdk-11-openj9 AS build
RUN mkdir -p /workspace
WORKDIR /workspace
COPY . /workspace/
RUN mvn -B package --file pom.xml -s settings.xml

CMD ["bash"]