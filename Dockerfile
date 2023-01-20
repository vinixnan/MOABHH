FROM maven:3.6.3-jdk-11-openj9 AS build

ENV GIT_HUB_KEY=${GIT_HUB_KEY}

RUN mkdir -p /workspace
WORKDIR /workspace
COPY . /workspace/
RUN mvn -B package --file pom.xml -s settings.xml -Dserver.password=$GIT_HUB_KEY

CMD ["bash"]