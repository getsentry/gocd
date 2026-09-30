# GoCD 26.1 upgrade review

This upgrades Sentry's production fork from its pre-release 25.2 baseline
(`647a5201b5`) to upstream's stable 26.1.0 tag
(`55b7b460510bb739c1ae6d226ff7fb650596dca2`). The merge retains upstream ancestry
and the production fork's changes. The matching deployment-service changes must
land before building or deploying the upgraded server in that service.

## Release and interface review

Reviewed the release notes for 25.2, 25.3, 25.4 and 26.1 and the corresponding
HTTP API and plugin API changelogs:

- [Release notes](https://www.gocd.org/releases/)
- [HTTP API changelog](https://api.gocd.org/26.1.0/#api-changelog)
- [Plugin API changelog](https://plugin-api.gocd.org/26.1.0/#changes-in-gocd-26-1-0)
- [Upstream source comparison](https://github.com/gocd/gocd/compare/25.2.0...26.1.0)

26.1 includes security fixes and requires Java 21 or later. The build uses JDK 25
and Node 24; bundled server Java is 25.0.3+9. The deployment builder must match
this build toolchain. Older agent bootstrappers running Java 21 can download the
matching agent implementation from the server. Pin the elastic bootstrapper and
static image to 26.1 anyway to keep future upgrades predictable.

The historical configuration XML HTTP API was removed and Feeds v1 deprecated.
Searches found no callers of these APIs in deployment-service. Health, pipeline
operations/history, stage operations, users v3 and encryption v1 remain available.
Plugin logging gains an additive overload; no extension API versions were removed
in this upgrade range. `cruise-config.xsd` still fixes `schemaVersion` at 139.
Config generation and disk-based role refresh must still be exercised in staging.

Upstream's image build rejects Debian 12 after its configured support window.
Use Debian 13 and `:docker:gocd-server:debian-13:docker` in both repositories.
The deployment layer still explicitly installs PostgreSQL client 16 for AlloyDB.
Upstream container agents changed their working directory to `/go-working-dir`;
the deployment-service elastic image has its own entrypoint which explicitly sets
`wrapper.working.dir=/go`. That custom directory contract can remain in place.
The static agent follows the newer upstream image layout.

LDAP validation became stricter in 25.3 and the bundled LDAP plugin advances to
4.0. Sentry uses the IAP authorization plugin rather than LDAP; no LDAP trust
store migration is needed for the generated deployment configuration.

## Fork patch disposition

| Fork behavior | Disposition |
| --- | --- |
| Git clone/fetch/unshallow retries | Retained against upstream's updated Git command implementation. Clone test now uses an empty directory rather than attempting a second clone into the fixture. |
| Revision required for manual trigger; no one-click trigger | Retained in dashboard widget and modal. Staging must verify the UI behavior. |
| Canonical ZIP destination and artifact containment checks | Retained while adopting upstream filesystem helpers. |
| DOM handler accepts functions only | Retained; no compilation of string event handlers. |
| Sentry logback reporting | Moved existing dependency into upstream's consolidated `build.gradle`; retained jar verification and logback configuration. |
| Debian image loaded as `gocd-server:latest` | Retained using upstream's new Gradle property types; native image is loaded for the deployment layer. |
| Former Bouncy Castle/jruby-rack pins | Superseded by upstream 1.84 / 1.3.0.11. |
| Rack gem | Minimum 2.2.24, locked to 2.2.24; downloaded gem SHA-256 checked against lockfile. |
| svgo resolution | Obsolete: upstream webpack 5 dependency graph no longer contains svgo. |
| Tanuki mirror | Upstream 3.6.5-st replaces 3.5.60-st; new mirror URL returns 404, so use upstream's checksum-verified download. |
| Reverted GoConfigService XXE patch | No manual reapplication of the reverted patch. Adopt upstream code and configuration tests. |

## Plugin inventory

All seven pinned external release jars were downloaded successfully. Their
`plugin.xml` IDs match deployment configuration; declared minimum GoCD versions
are older than 26.1 and maximum base class versions are Java 8 or 11, compatible
with Java 21/25. These checks alone do not establish runtime compatibility.

| Plugin ID | Retained external version | Minimum GoCD |
| --- | --- | --- |
| `cd.go.contrib.elasticagent.kubernetes` | 3.8.2-320 | 20.9.0 |
| `cd.go.contrib.secrets.kubernetes` | 1.2.2-169 | 20.9.0 |
| `net.getsentry.gocd.google-iap-authorization` | v0.0.6 | 20.9.0 |
| `jsonnet.config.plugin` | v0.2.1 | 20.4.0 |
| `script-executor` | 1.0.3-156 | 20.1.0 |
| `net.getsentry.gocd.webhook-notifier` | 1.2.0 | 16.2.1 |
| `cd.go.artifact.docker.registry` | 1.3.1-270 | 20.9.0 |

Upstream updates bundled YAML config to 2.0.0-541, JSON config to 2.0.0-387,
LDAP auth to 4.0.0-535, file auth to 3.0.0-418 and file secrets to 2.0.0-437.
YAML is needed for GoCD's own deployment pipelines; validate an actual YAML
pipeline through the upgraded plugin before promotion. Jsonnet additionally needs
`jsonnet` and `jb`, installed in the deployment layer. External pins remain stable
unless a demonstrated incompatibility requires changing one.

## Validation and rollout

The operational upgrade playbook belongs in deployment-service's `AGENTS.md`.
Local build/tests and runtime results are recorded below once checks complete.
Live IAP identity, Kubernetes credentials/Workload Identity, secret retrieval,
webhook deliveries, AlloyDB backup/restore, group sync and self-deploy pipelines
require a development or staging environment; local plugin loading cannot prove
those integrations. Keep this PR draft until those environment gates pass.

Completed local checks:

- Built `:installers:serverGenericZip`, including immutable Yarn installation,
  Rails gems with deployment/checksum enforcement, production webpack and launcher
  jar verification. Built the Debian 13 OCI image for amd64 and arm64 and loaded
  the native arm64 image as `gocd-server:latest`.
- Ran `ZipUtilTest`, `GitCommandTest` and the complete plugin-access suite.
  Git: 19 passing; plugin-access: 504 passing.
- Ran agents v7, pipeline operations v1, stage operations v2, users v3 and
  encryption v1 suites: 256 passing tests in total.
- Ran `ArtifactsServiceTest` and `GoConfigServiceTest`: 72 passing, one existing
  disabled test. The artifact retry-limit fixture now supplies its artifact root.
- Started the built server with fresh local H2 storage and all seven external
  release jars. Health returned `OK`; Plugin Info v7 reported all 12 external and
  bundled plugins `active`, with their expected extension metadata.
- Startup logged warnings for plugins without global plugin-settings handlers;
  these plugins expose their extension-specific metadata and remain active. First
  initialization also logged a server-ID config race, followed by successful
  readiness. Staging must use the persistent server ID preserved by autoconfig.

No formatters, linters or pre-commit hooks were run. For the local image build,
webpack's ESLint/Stylelint plugins were temporarily removed and then restored;
production lint configuration remains intact. Browser interaction and live
cloud integrations are still staging gates, not established by these checks.
