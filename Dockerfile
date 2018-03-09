FROM registry.fedoraproject.org/fedora:27
RUN dnf install -y maven git

COPY . /src
WORKDIR /src

RUN mvn install -DskipTests
CMD ["bash"]
