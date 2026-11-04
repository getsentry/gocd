# Builds the GoCD server image (`gocd-server:latest`) from source.
#
# getsentry/devinfra-deployment-service's Cloud Build runs this with a registry layer cache, so:
# - unchanged source (most devinfra pushes) reuses every layer and skips the Gradle build;
# - changed source reuses the toolchain, Yarn, and gem layers, and the runtime base layer.
#
# Base images come from getsentry/image-mirror (ghcr.io) instead of Docker Hub.
#
# The runtime stage mirrors buildSrc/src/main/resources/gocd-docker-server/Dockerfile.server.ftl
# for Debian 12. Change both together.
#
# Local build: docker build -t gocd-server:latest \
#   --build-arg GOCD_GIT_REVISION=$(git rev-parse HEAD) .

ARG BUILDER_IMAGE=ghcr.io/getsentry/image-mirror-library-eclipse-temurin:21.0.6_7-jdk-noble
ARG BASE_IMAGE=ghcr.io/getsentry/image-mirror-library-debian:12.15-slim

FROM ${BUILDER_IMAGE} AS toolchain
ARG TARGETARCH

RUN apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
        build-essential ca-certificates curl git unzip xz-utils \
    && rm -rf /var/lib/apt/lists/*

ARG NODE_VERSION=v22.23.4
ARG NODE_SHA256_AMD64=1805bcd35e6815559bd2eb97db438d3fdb0eb3a5e6a977b7c1badca76337e9ce
ARG NODE_SHA256_ARM64=6287aa46b6c375d816e63ae66d024fbbfd70404825aabdfdf898a970871d2f4b
ARG YARN_VERSION=1.22.22
ENV PATH=/node/bin:$PATH
# nodejs.org is not reliable enough, so use devinfra's GCS mirror.
RUN case "${TARGETARCH}" in \
        amd64) arch=x64; sha="${NODE_SHA256_AMD64}" ;; \
        arm64) arch=arm64; sha="${NODE_SHA256_ARM64}" ;; \
        *) echo "unsupported arch ${TARGETARCH}"; exit 1 ;; \
    esac \
    && curl -fsSL -o /tmp/node.tar.xz \
        "https://storage.googleapis.com/sentry-dev-infra-assets/node/node-${NODE_VERSION}-linux-${arch}.tar.xz" \
    && echo "${sha}  /tmp/node.tar.xz" | sha256sum -c - \
    && mkdir /node \
    && tar -xf /tmp/node.tar.xz -C /node --strip 1 \
    && rm /tmp/node.tar.xz \
    && npm install -g "yarn@${YARN_VERSION}"

WORKDIR /src
COPY gradlew ./
COPY gradle/wrapper gradle/wrapper
RUN ./gradlew --no-daemon --version

# Warm Yarn's global cache, so the Gradle yarnInstall task only links packages.
FROM toolchain AS yarn-deps
ENV RAILS_DIR=server/src/main/webapp/WEB-INF/rails
WORKDIR /src/${RAILS_DIR}
COPY ${RAILS_DIR}/package.json ${RAILS_DIR}/yarn.lock ${RAILS_DIR}/.yarnrc.yml ./
COPY ${RAILS_DIR}/.yarn/patches .yarn/patches
COPY ${RAILS_DIR}/.yarn/plugins .yarn/plugins
COPY ${RAILS_DIR}/.yarn/releases .yarn/releases
COPY ${RAILS_DIR}/node-vendor node-vendor
COPY ${RAILS_DIR}/vendor/assets/fonts/opensans vendor/assets/fonts/opensans
RUN yarn install --immutable

# Install the bundled gems, so the Gradle initializeRailsGems task finds them already installed.
FROM yarn-deps AS gem-deps
COPY dependencies.gradle /tmp/dependencies.gradle
COPY ${RAILS_DIR}/Gemfile ${RAILS_DIR}/Gemfile.lock ./
COPY ${RAILS_DIR}/.bundle .bundle
RUN jruby_version="$(sed -n "s/.*'org\.jruby:jruby-complete:\([^']*\)'.*/\1/p" /tmp/dependencies.gradle)" \
    && test -n "${jruby_version}" \
    && curl -fsSL -o /tmp/jruby-complete.jar \
        "https://repo1.maven.org/maven2/org/jruby/jruby-complete/${jruby_version}/jruby-complete-${jruby_version}.jar" \
    && curl -fsSL "https://repo1.maven.org/maven2/org/jruby/jruby-complete/${jruby_version}/jruby-complete-${jruby_version}.jar.sha1" \
        | sed 's/$/  \/tmp\/jruby-complete.jar/' | sha1sum -c - \
    && java -Xmx1g -jar /tmp/jruby-complete.jar -S bundle install \
    && rm /tmp/jruby-complete.jar

FROM gem-deps AS build
ARG TARGETARCH
# .git is excluded from the build context so that identical source hits the layer cache, so
# build.gradle reads the revision from these instead.
ARG GOCD_GIT_REVISION
# A depth-1 clone, as Cloud Build makes, counts 1.
ARG GOCD_DIST_VERSION=1
WORKDIR /src
COPY . .
# One RUN, so that the layer the registry cache stores holds only /go-server, not the source tree,
# build outputs, and Gradle caches. The unzip mirrors the gocd-server-unzip stage of
# Dockerfile.server.ftl.
RUN ./gradlew --no-daemon :installers:serverGenericZip \
    && zip="$(ls installers/target/distributions/zip/go-server-*.zip)" \
    && unzip -q "${zip}" -d /tmp/unzip \
    && src="$(ls -d /tmp/unzip/go-server-*)" \
    && case "${TARGETARCH}" in \
        amd64) wrapperarch=x86-64 ;; \
        arm64) wrapperarch=arm-64 ;; \
        *) echo "unsupported arch ${TARGETARCH}"; exit 1 ;; \
    esac \
    && mkdir -p /go-server/wrapper /go-server/bin \
    && mv "${src}/LICENSE" /go-server/LICENSE \
    && mv "${src}/bin/go-server" /go-server/bin/go-server \
    && mv "${src}/lib" /go-server/lib \
    && mv "${src}/logs" /go-server/logs \
    && mv "${src}/run" /go-server/run \
    && mv "${src}/wrapper-config" /go-server/wrapper-config \
    && mv "${src}/wrapper/wrapper-linux-${wrapperarch}"* /go-server/wrapper/ \
    && mv "${src}/wrapper/libwrapper-linux-${wrapperarch}"* /go-server/wrapper/ \
    && mv "${src}/wrapper/wrapper.jar" /go-server/wrapper/ \
    && chown -R 1000:0 /go-server && chmod -R g=u /go-server \
    && cd / && rm -rf /src /tmp/unzip /root/.gradle

FROM toolchain AS downloads
ARG TARGETARCH
ARG TINI_VERSION=v0.19.0
ARG TINI_SHA256_AMD64=c5b0666b4cb676901f90dfcb37106783c5fe2077b04590973b885950611b30ee
ARG TINI_SHA256_ARM64=eae1d3aa50c48fb23b8cbdf4e369d0910dfc538566bfd09df89a774aa84a48b9
ARG JRE_URL=https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.6%2B7
ARG JRE_VERSION=21.0.6_7
ARG JRE_SHA256_AMD64=7fc9d6837da5fa1f12e0f41901fd70a73154914b8c8ecbbcad2d44176a989937
ARG JRE_SHA256_ARM64=f1b78f2bd6d505d5e0539261737740ad11ade3233376b4ca52e6c72fbefd2bf6
RUN case "${TARGETARCH}" in \
        amd64) jrearch=x64; tinisha="${TINI_SHA256_AMD64}"; jresha="${JRE_SHA256_AMD64}" ;; \
        arm64) jrearch=aarch64; tinisha="${TINI_SHA256_ARM64}"; jresha="${JRE_SHA256_ARM64}" ;; \
        *) echo "unsupported arch ${TARGETARCH}"; exit 1 ;; \
    esac \
    && curl -fsSL -o /tini \
        "https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini-static-${TARGETARCH}" \
    && echo "${tinisha}  /tini" | sha256sum -c - \
    && chmod 0755 /tini \
    && curl -fsSL -o /tmp/jre.tar.gz \
        "${JRE_URL}/OpenJDK21U-jre_${jrearch}_linux_hotspot_${JRE_VERSION}.tar.gz" \
    && echo "${jresha}  /tmp/jre.tar.gz" | sha256sum -c - \
    && mkdir /gocd-jre \
    && tar -xf /tmp/jre.tar.gz -C /gocd-jre --strip 1 \
    && rm /tmp/jre.tar.gz

FROM ${BASE_IMAGE}
ARG TARGETARCH
ARG UID=1000

# the ports that GoCD server runs on
EXPOSE 8153

# force encoding
ENV LANG=en_US.UTF-8 LANGUAGE=en_US:en LC_ALL=en_US.UTF-8
ENV GO_JAVA_HOME="/gocd-jre"

# Layers are cached, so `apt-get upgrade` reruns only when this changes. Cloud Build passes the
# ISO week, so the image picks up Debian updates at least weekly.
ARG APT_REFRESH=
RUN echo "apt refresh: ${APT_REFRESH}" \
    && DEBIAN_FRONTEND=noninteractive apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get upgrade -y \
    # add our user and group first to make sure their IDs get assigned consistently, regardless of whatever dependencies get added
    && useradd -l -u ${UID} -g root -d /home/go -m go \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y git-core openssh-client bash unzip curl ca-certificates locales procps coreutils \
    && DEBIAN_FRONTEND=noninteractive apt-get clean all \
    && rm -rf /var/lib/apt/lists/* \
    && echo 'en_US.UTF-8 UTF-8' > /etc/locale.gen && /usr/sbin/locale-gen \
    && mkdir -p /go-server /docker-entrypoint.d /go-working-dir /godata

COPY --from=downloads /tini /usr/local/sbin/tini
COPY --from=downloads /gocd-jre /gocd-jre
COPY buildSrc/src/main/resources/gocd-docker-server/docker-entrypoint.sh /
COPY --chown=go:root buildSrc/src/main/resources/gocd-docker-server/install-gocd-plugins buildSrc/src/main/resources/gocd-docker-server/git-clone-config /usr/local/sbin/

RUN chown -R go:root /docker-entrypoint.d /go-working-dir /godata /docker-entrypoint.sh \
    && chmod -R g=u /docker-entrypoint.d /go-working-dir /godata /docker-entrypoint.sh

COPY --from=build /go-server /go-server
# ensure that logs are printed to console output
COPY --chown=go:root buildSrc/src/main/resources/gocd-docker-server/logback-include.xml /go-server/config/logback-include.xml

ARG GOCD_GIT_REVISION
LABEL description="GoCD server based on debian:12-slim" \
  url="https://github.com/getsentry/gocd" \
  gocd.git.sha="${GOCD_GIT_REVISION}"

ENTRYPOINT ["/docker-entrypoint.sh"]

USER go
