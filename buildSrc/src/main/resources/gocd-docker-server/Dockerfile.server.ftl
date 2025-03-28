# Copyright Thoughtworks, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

###############################################################################################
# This file is autogenerated by the repository at https://github.com/gocd/gocd.
# Please file any issues or PRs at https://github.com/gocd/gocd
###############################################################################################

FROM cgr.dev/chainguard/bash:latest AS gocd-server-unzip
ARG TARGETARCH
ARG UID=1000
<#if useFromArtifact >
COPY go-server-${fullVersion}.zip /tmp/go-server-${fullVersion}.zip
RUN \
<#else>
RUN curl --fail --location --silent --show-error "https://download.gocd.org/binaries/${fullVersion}/generic/go-server-${fullVersion}.zip" > /tmp/go-server-${fullVersion}.zip && \
</#if>
    unzip -q /tmp/go-server-${fullVersion}.zip -d / && \
    mkdir -p /go-server/wrapper /go-server/bin && \
    mv -v /go-server-${goVersion}/LICENSE /go-server/LICENSE && \
    mv -v /go-server-${goVersion}/bin/go-server /go-server/bin/go-server && \
    mv -v /go-server-${goVersion}/lib /go-server/lib && \
    mv -v /go-server-${goVersion}/logs /go-server/logs && \
    mv -v /go-server-${goVersion}/run /go-server/run && \
    mv -v /go-server-${goVersion}/wrapper-config /go-server/wrapper-config && \
    WRAPPERARCH=${dockerAliasToWrapperArchAsShell} && \
    mv -v /go-server-${goVersion}/wrapper/wrapper-linux-$WRAPPERARCH* /go-server/wrapper/ && \
    mv -v /go-server-${goVersion}/wrapper/libwrapper-linux-$WRAPPERARCH* /go-server/wrapper/ && \
    mv -v /go-server-${goVersion}/wrapper/wrapper.jar /go-server/wrapper/ && \
    chown -R ${r"${UID}"}:0 /go-server && chmod -R g=u /go-server

FROM ${distro.getBaseImageLocation(distroVersion)}
ARG TARGETARCH

LABEL gocd.version="${goVersion}" \
  description="GoCD server based on ${distro.getBaseImageLocation(distroVersion)}" \
  maintainer="GoCD Team <go-cd-dev@googlegroups.com>" \
  url="https://www.gocd.org" \
  gocd.full.version="${fullVersion}" \
  gocd.git.sha="${gitRevision}"

# the ports that GoCD server runs on
EXPOSE 8153

<#list additionalFiles as filePath, fileDescriptor>
ADD ${fileDescriptor.url} ${filePath}
</#list>

# force encoding
ENV LANG=en_US.UTF-8 LANGUAGE=en_US:en LC_ALL=en_US.UTF-8
<#list distro.getEnvironmentVariables(distroVersion) as key, value>
ENV ${key}="${value}"
</#list>

ARG UID=1000

RUN \
<#if additionalFiles?size != 0>
# add mode and permissions for files we added above
  <#list additionalFiles as filePath, fileDescriptor>
  chmod ${fileDescriptor.mode} ${filePath} && \
  chown ${fileDescriptor.owner}:${fileDescriptor.group} ${filePath} && \
  </#list>
</#if>
<#list distro.getBaseImageUpdateCommands(distroVersion) as command>
  ${command} && \
</#list>
# add our user and group first to make sure their IDs get assigned consistently, regardless of whatever dependencies get added
<#list distro.createUserAndGroupCommands as command>
  ${command} && \
</#list>
<#list distroVersion.installPrerequisitesCommands as command>
  ${command} && \
</#list>
<#list distro.getInstallPrerequisitesCommands(distroVersion) as command>
  ${command} && \
</#list>
<#list distro.getInstallJavaCommands(project) as command>
  ${command} && \
</#list>
  mkdir -p /go-server /docker-entrypoint.d /go-working-dir /godata

ADD docker-entrypoint.sh /

COPY --from=gocd-server-unzip /go-server /go-server
# ensure that logs are printed to console output
COPY --chown=go:root logback-include.xml /go-server/config/logback-include.xml
COPY --chown=go:root install-gocd-plugins git-clone-config /usr/local/sbin/

RUN chown -R go:root /docker-entrypoint.d /go-working-dir /godata /docker-entrypoint.sh && \
    chmod -R g=u /docker-entrypoint.d /go-working-dir /godata /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]

USER go
