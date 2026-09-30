# AGENTS.md

This is Sentry's fork of [GoCD](https://github.com/gocd/gocd), `getsentry/gocd`. It exists to
supply the GoCD server image that
[`getsentry/devinfra-deployment-service`](https://github.com/getsentry/devinfra-deployment-service)
builds and runs as Sentry's deploy service (prod: `deploy.getsentry.net`).

- Default branch: `prod`. Whatever is on `prod` is what the next server image build ships.
- Upstream baseline: release `26.1.0`, commit `55b7b460510bb739c1ae6d226ff7fb650596dca2`.
  Fork upgrades retain upstream history with a merge; compare fork patches against that tag.
  GoCD version is defined by `GO_VERSION_SEGMENTS` in `build.gradle`.
- No CI runs here (`.github/` was removed). The build that matters runs in
  devinfra-deployment-service's Cloud Build; see
  [How devinfra-deployment-service uses GoCD](#how-devinfra-deployment-service-uses-gocd).
- `README.md` is upstream's README, left unchanged. Upstream developer docs:
  https://developer.gocd.org/current/

## Local setup

Toolchains are declared in `mise.toml` (JDK 25, Node 24). Java 21 is the runtime minimum. On macOS:

```sh
brew install node@24 corepack openjdk@25 docker-buildx
```

Gradle will not download a JDK (`org.gradle.java.installations.auto-download=false` in
`gradle.properties`), so put JDK 25, Node 24, and Corepack first on `PATH`. Corepack selects
Yarn 4.17.0 from the Rails `package.json`. If a global Yarn or pnpm installation conflicts with
Homebrew linking Corepack, use its keg bin directory explicitly rather than replacing those tools.

Docker must be able to find the Homebrew buildx plugin. In `~/.docker/config.json`:

```json
{
  "cliPluginsExtraDirs": ["/opt/homebrew/lib/docker/cli-plugins"]
}
```

## Build and test

Fastest loop for frontend (TypeScript/MSX) compile errors:

```sh
cd server/src/main/webapp/WEB-INF/rails && yarn run webpack-watch
```

Build the server Docker image the same way Cloud Build does:

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home \
PATH=/opt/homebrew/opt/openjdk@25/bin:/opt/homebrew/opt/node@24/bin:/opt/homebrew/opt/corepack/bin:$PATH \
  ./gradlew -PdockerBuildLocalZip :docker:gocd-server:debian-13:docker
```

- Template: `buildSrc/src/main/resources/gocd-docker-server/Dockerfile.server.ftl`
- Rendered Dockerfile: `docker/gocd-server/target/debian-13/docker-gocd-server/Dockerfile`
- Result: local image `gocd-server:latest`, built for the host architecture (arm64 on Apple
  Silicon; Cloud Build produces amd64). Automated image verification is disabled in
  `BuildDockerImageTask.groovy`, so check the image by hand.

Tests are ordinary Gradle (JUnit) and Karma suites. Run the narrowest one that covers the change,
for example:

```sh
./gradlew :domain:test --tests 'com.thoughtworks.go.domain.materials.git.GitCommandTest'
cd server/src/main/webapp/WEB-INF/rails && yarn run jasmine-ci
```

## What this fork changes

`git diff 26.1.0..prod` shows the full set. Keep this list current when adding or dropping a
patch, and re-check each item when merging from upstream. The Git clone test uses a fresh
directory; preserve this isolation when testing retries. Add behavior tests when touching a patch.

Deploy safety:

- Git materials retry clone, fetch, and unshallow up to 3 times, 5 s apart:
  `SCMCommand.runWithRetries`, `SCMCommand.runCascadeWithRetries`, `GitCommand.MAX_RETRIES`.
- The dashboard has no one-click "Trigger" button. "Trigger with Options" refuses to run until a
  material revision is selected, because otherwise GoCD re-runs the previous run's materials instead
  of the latest: `webpack/views/dashboard/pipeline_operations_widget.js.msx`,
  `webpack/views/dashboard/trigger_with_options/modal_body.js.msx`.

Security:

- `ZipUtil` (unzip) and `ArtifactsService.saveFile` / `saveOrAppendFile` reject destinations
  outside the target directory.
- `webpack/helpers/dom.ts` accepts only function event handlers; string handlers are no longer
  compiled with `new Function`.
- Rack has a 2.2.24 minimum in the Rails `Gemfile`, with a verified gem checksum in `Gemfile.lock`.
  Java dependencies now live in `build.gradle`; upstream supersedes the former Bouncy Castle
  and jruby-rack pins. Upstream webpack 5 removes the old svgo dependency and its resolution.
  The fork retains the 4096 MB webpack heap in `rails/package.json`.
- The plugin configuration UI uses the locally vendored AngularJS core from
  `server/src/main/webapp/WEB-INF/rails/node-vendor/angular`, updated to 1.8.3. This fixes the
  AngularJS XSS advisories patched by 1.5.0-beta.1 and 1.8.0. AngularJS is end of life; CVE-2022-25869
  and the CVE-2023-26117 / CVE-2023-26118 denial-of-service advisories have no patched AngularJS
  version. CVE-2022-25869 depends on Internet Explorer's page-cache behavior; Internet Explorer is
  not in GoCD's supported browser list. The `angular-resource` module is not bundled by this fork, so
  CVE-2023-26117's `$resource` path is not present in GoCD's AngularJS integration.
- Rails' `yarn.lock` resolves js-yaml 4.x to 4.3.2 and 3.x to 3.15.2, the patched releases for the
  merge-source CPU and ordered-map CPU advisories. These remain transitive build dependencies.
- An XXE fix in `GoConfigService` was merged (#18) and then reverted (#28). The revert gives no
  reason, so find out why before re-applying it.

Build:

- The server image is Debian 13 and is tagged `gocd-server:latest` (`settings-docker.gradle`,
  `BuildDockerImageTask.getImageNameWithTag`). Upstream builds Wolfi and tags `v<version>`.
- The built image is loaded into the local Docker daemon and kept, not verified and deleted.
- Tanuki 3.6.5 now downloads from its upstream source with a verified SHA-256
  (`installers/tanuki.gradle`). The old GCS mirror contains 3.5.60, not the upgraded wrapper.

## Contracts devinfra-deployment-service relies on

Breaking any of these breaks GoCD's own deploy. Change both repos together.

- **Build entry point.** `cloudbuild.yaml` runs
  `./gradlew -PdockerBuildLocalZip :docker:gocd-server:debian-13:docker` and then builds
  `FROM gocd-server:latest`.
- **Debian base.** The devinfra image layer uses `apt-get`, `lsb_release`, the PGDG apt repo, and
  the `go` user (UID 1000).
- **Server entrypoint** (`buildSrc/src/main/resources/gocd-docker-server/docker-entrypoint.sh`).
  devinfra's stage-1 entrypoint `exec`s `/docker-entrypoint.sh` as `go`, which must, in this order:
  1. Run `install-gocd-plugins`, which downloads each `GOCD_PLUGIN_INSTALL_<name>=<url>` to
     `/godata/plugins/external/<name>.jar`.
  2. Run every executable, non-dot file in `/docker-entrypoint.d/`, in glob order. Dotfiles there
     are helpers that devinfra scripts call directly.
  3. Append `wrapper.java.additional.<100+>` lines to
     `/go-server/wrapper-config/wrapper-properties.conf`. devinfra's copy of that file uses indices
     200 and up.
- **Config XML schema.** devinfra's `gocd_autoconfig.py` hardcodes `schemaVersion="139"`, which must
  equal `GoConstants.CONFIG_SCHEMA_VERSION`. It also writes `authConfigs`, `roles`,
  `elastic/agentProfiles`, `elastic/clusterProfiles`, `config-repos` (with `rules`), `backup`,
  `secretConfigs`, and per-group `pipelines/authorization`. An upstream merge that adds a config
  migration or changes those elements needs a matching devinfra change.
- **Config reload from disk.** The role sync edits `/godata/config/cruise-config.xml` in place and
  relies on GoCD re-reading it without a restart (`cruise.config.refresh.interval`, 5 s).
- **HTTP APIs** called by devinfra scripts and by the services listed below:
  - `GET /go/api/v1/health` (readiness and load-balancer health checks).
  - `/go/api/pipelines/{name}/{pause,unpause,unlock,schedule,status,history}` and
    `/go/api/pipelines/{name}/{counter}` (v1).
  - `/go/api/stages/{pipeline}/{counter}/{stage}/run` (v2), `/go/api/stages/{stage}/cancel` (v3),
    and `/go/api/stages/{pipeline}/{stage}/history`.
  - `/go/api/users` (v3) and `/go/api/admin/encrypt` (v1).
- **Plugin API compatibility** for every plugin in [GoCD plugins in use](#gocd-plugins-in-use).
- **Agent version skew.** Elastic agents run an upstream go-agent tarball (26.1.0-22803, from
  devinfra's `gocd_agent/Dockerfile`), not a build of this repo. Bump that tarball when a server
  change needs newer agents.

## How devinfra-deployment-service uses GoCD

Paths in this section are relative to `getsentry/devinfra-deployment-service`, usually checked out
at `../devinfra-deployment-service`. Snapshot as of its commit `0faabc727d` (2026-09-16).

### Building the server image

- Cloud Build trigger `build-gocd-server` (`terraform/module/cd-project/cloud-build/build/main.tf`)
  fires on pushes to devinfra-deployment-service, not to this repo. Each GCP project watches one
  branch: `prod` for prod, `staging` for prod-staging, and a personal branch for dev projects.
- `cloudbuild.yaml` shallow-clones this repo's `prod` branch and builds it inside
  `gocd_server_src_builder/Dockerfile` (JDK 25, Node 24, Corepack/Yarn). It then layers
  `gocd_server/Dockerfile` on top and pushes
  `us-west1-docker.pkg.dev/<project>/gocd/server:<devinfra SHA>`. The image tag records the
  devinfra commit; the gocd commit is recorded only in the `gocd.git.sha` image label.
- `gocd_server/Dockerfile` adds:
  - `postgresql-client-16`, because GoCD's backup runs `pg_dump` against AlloyDB (PostgreSQL 16).
  - `python3` for the config and sync scripts, `cron`, `gosu`, and `jsonnet` 0.20.0 plus `jb`
    0.5.1 for the Jsonnet config plugin.
  - A crontab that archives pipeline artifact directories older than 30 days to GCS and deletes
    them, weekly (`gocd_server/cron/cleanup-old-artifacts.sh`), because GoCD cannot expire
    artifacts by age.
  - A crontab entry that syncs role membership every 10 minutes
    (`gocd_server/cron/sync-role-groups.sh`).
- Its entrypoint, `gocd_server/docker-entrypoint-stage1.sh`, runs as root. It starts cron, installs
  `logback-include.xml`, symlinks `.pgpass` from `/godata/config`, then
  `exec gosu go /docker-entrypoint.sh` (this repo's entrypoint).

### Running on GKE

`terraform/module/gocd-service` installs the upstream GoCD Helm chart (version 2.13.2) into
namespace `gocd`, using `helm-values.yaml.tftpl`:

- One server pod (6 CPU, 24 Gi) with an 80 Gi `/godata` volume. The JVM heap (14400 MB) and JMX for
  Datadog are set in `gocd-server-entrypoint.d/.wrapper-properties.conf`.
- A GCE ingress behind IAP. Access is granted to `role-deploy-user@sentry.io` and a handful of
  service accounts (GitHub Actions pipeline validation, eng-pipes, devinfra-metrics,
  incident-scout-bot, Babysitter).
- One static agent pinned to `gocd/gocd-agent-debian-13:v26.1.0`, meant for trivial jobs that
  need no credentials. Everything else runs on elastic agents.
- Plugins installed through `GOCD_PLUGIN_INSTALL_*` env vars (see
  [GoCD plugins in use](#gocd-plugins-in-use)).
- `shouldPreconfigure: false`. Instead, the ConfigMap `gocd-server-entrypoint.d` (built from
  `terraform/module/gocd-service/gocd-server-entrypoint.d/`) is mounted at `/docker-entrypoint.d`.
- GoCD's database is AlloyDB PostgreSQL (`terraform/alloydb`). The DB connection settings are not
  in that repo; only `.pgpass` handling is (it is kept at `/godata/config/.pgpass`).

### Boot: config is regenerated on every start

This repo's entrypoint runs the non-dot scripts in `/docker-entrypoint.d`:

1. `gocd_autoconfig` copies in `.wrapper-properties.conf`, runs `.setup_config_xml`, and installs
   gcloud. `.setup_config_xml` pipes the last known-good `/godata/db/config.git/cruise-config.xml`
   through `.gocd_autoconfig.py` (`gocd_server/gocd_autoconfig.py`) into
   `/godata/config/cruise-config.xml`.
2. `remove_removed_plugins.py` deletes any jar in `/godata/plugins/external` that has no
   `GOCD_PLUGIN_INSTALL_*` env var, so removing an entry from the Helm values uninstalls the plugin.

`gocd_autoconfig.py` builds the whole `cruise-config.xml` from scratch. From the previous config it
keeps only the `<server>` attributes (server UUIDs) and each role's `<users>`. **Config edits made
in the GoCD UI do not survive a restart.** Plugin settings are stored in the database, so they do
survive; they are applied by hand, as listed in `terraform/README.md`. The generated config
contains:

- **Auth:** a single `authConfig` using the IAP plugin, and `role-deploy-admin` as server admin.
- **Deploy targets:** from Terraform `deploy-configs` (mounted at `/opt/deploy-configs`), each one
  gets a pipeline group, an elastic agent profile, and a place in a config repo:
  - **Pipeline group:** access follows `role-deploy-{admin,user,operator}` plus
    `role-deploy-<target>-{admin,user,operator}`. `restrict-viewers` and `restrict-operators` drop
    the global roles.
  - **Elastic agent profile:** a pod spec in namespace `gocd` that runs the devinfra agent image
    under the Workload Identity service account `deploy-to-<target>`, with SSH keys at
    `/home/go/.ssh`.
  - **Config repo:** one per repo and branch (`git@github.com:getsentry/<repo>.git`), with rules
    that let it define only its targets' pipeline groups and environments. The `ops` repo may define
    any.
- **Cluster profile:** `gocd-cluster` for the Kubernetes elastic agent plugin.
- **Secret configs:** one per shared Kubernetes secret (`devinfra`, `devinfra-github`,
  `devinfra-sentryio`, `devinfra-sentryst`, `devinfra-sentrymysentry`, `devinfra-temp`).
- **Backup:** daily at 01:00. `.backup-script.sh` (the `postBackupScript`) copies each backup to
  GCS.
- **Job timeout:** 60 minutes.

### Runtime jobs

- `gocd_server/role_groups_sync.py` runs from cron every 10 minutes. The IAP plugin cannot report a
  user's roles (`canGetUserRoles=false`), so the script rewrites each `role-deploy-*` role's
  `<users>` from the Google group with the same name. It also imports members through
  `/go/api/users`. It shares a lock with `.setup_config_xml`.
- The weekly artifact cleanup and the daily backup described above.

### Pipelines, agents, and secrets

- **Pipeline definitions:** pipelines live in each service's repo as config-as-code. Almost every
  deploy target uses `jsonnet.config.plugin` with the pattern
  `gocd/**/*.jsonnet,gocd/**/jsonnetfile.json,gocd/pipelines/*.yaml`. The rest default to the
  bundled `yaml.config.plugin` with `gocd/**/*.yaml`. devinfra-deployment-service itself uses YAML
  from `gocd/production/**/*.yaml`.
- **Agent image:** `gocd_agent/Dockerfile` (Python 3.13 + upstream go-agent 26.1.0-22803 + JRE 21)
  bundles terraform, gcloud, kubectl, helm, sentry-cli, jq/yq, uv, and the `devinfra` CLIs from
  `gocd_agent/scripts` (`checks-*`, `gocd-*`, `k8s-*`, ...).
- **Tasks:** jobs are `script:` tasks, which run through the script-executor plugin.
- **Secrets:**
  - `{{SECRET:[<k8s secret>][<key>]}}` references resolve through the Kubernetes secrets plugin.
  - `secure_variables` hold `AES:` values encrypted with `/go/api/admin/encrypt`
    (`src/getsentry_deployment_service/gocd_encrypt.py`).
- **Pipeline control:** the agent CLIs call the GoCD API with a bot token and `X-GoCD-Confirm: true`
  (`gocd_agent/scripts/scripts/gocd/gocd_api.py`). They pause, cancel, unlock, re-run stages,
  trigger pipelines, and find the last good SHA.
- **Notifications:** the webhook notifier plugin posts pipeline events to eng-pipes,
  devinfra-metrics, and deploy-tools. Endpoints are set by hand in plugin settings.

### GoCD deploys itself

- Prod GoCD runs `deploy-gocd-staging`, which tracks devinfra's `staging` branch
  (`gocd/production/pipelines/deploy-staging.yaml`).
- Prod-staging GoCD runs `deploy-gocd-production`, which tracks `prod`
  (`gocd/staging/pipelines/deploy-prod.yaml`).
- Stages: `checks` (GitHub check runs, plus the Cloud Build agent and server builds for that SHA) →
  `terraform-plan` → manual approval → `terraform-apply`, wrapped in a Datadog downtime. Image tags
  are the devinfra SHA.

To ship a change from this fork: merge it to `prod` here. Then land a devinfra-deployment-service
commit on `staging` so Cloud Build rebuilds the server image, and run `deploy-gocd-staging`. Repeat
on devinfra's `prod` branch and run `deploy-gocd-production`.

## GoCD plugins in use

External plugins are installed by `GOCD_PLUGIN_INSTALL_<name>` in
`terraform/module/gocd-service/helm-values.yaml.tftpl` (devinfra-deployment-service):

| Install name | Plugin ID | Source and version | Used for |
| --- | --- | --- | --- |
| `kubernetes-elastic-agents` | `cd.go.contrib.elasticagent.kubernetes` | `gocd/kubernetes-elastic-agents` v3.8.2-320 | Cluster profile `gocd-cluster` and one agent profile per deploy target; all pipeline jobs run in these pods |
| `kubernetes-based-secrets-plugin` | `cd.go.contrib.secrets.kubernetes` | `gocd/gocd-kubernetes-based-secrets-plugin` v1.2.2-169 | `secretConfigs` for the `devinfra*` Kubernetes secrets; debug logging enabled in `.wrapper-properties.conf` |
| `getsentry-google-iap` | `net.getsentry.gocd.google-iap-authorization` | `getsentry/gocd-google-iap-authorization-plugin` v0.0.6 | The only `authConfig`: users authenticate through Google IAP; roles come from `role_groups_sync.py` |
| `jsonnet-config-plugin` | `jsonnet.config.plugin` | `getsentry/gocd-jsonnet-config-plugin` v0.2.1 | Config repos for almost all deploy targets; needs `jsonnet` and `jb` on the server image; settings applied by hand |
| `script-executor` | `script-executor` | `getsentry/script-executor-task` 1.0.3-156 | Runs `script:` tasks in pipelines |
| `getsentry-webhook-notifier` | `net.getsentry.gocd.webhook-notifier` | `getsentry/gocd-webhook-notification-plugin` v1.2.0 | Posts pipeline events to eng-pipes, devinfra-metrics, and deploy-tools; endpoints set by hand |
| `docker-registry-artifact-plugin` | `cd.go.artifact.docker.registry` | `gocd/docker-registry-artifact-plugin` v1.3.1-270 | Installed but apparently unused: `gocd_autoconfig.py` renders no `artifactStores`, and nothing in devinfra-deployment-service references it |

These plugins are bundled into the server zip by this repo (`tw-go-plugins/build.gradle`):

| Plugin | Version | Used by devinfra-deployment-service? |
| --- | --- | --- |
| `tomzo/gocd-yaml-config-plugin` (`yaml.config.plugin`) | v2.0.0-541 | Yes: the default `plugin-id` for deploy targets, including devinfra-deployment-service's own pipelines |
| `tomzo/gocd-json-config-plugin` | v2.0.0-387 | No |
| `gocd/gocd-ldap-authentication-plugin` | v4.0.0-535 | No |
| `gocd/gocd-filebased-authentication-plugin` | v3.0.0-418 | No |
| `gocd/gocd-file-based-secrets-plugin` | v2.0.0-437 | No |

## Upgrade evidence

See [GoCD 26.1 upgrade review](docs/gocd-upgrade-26.1.md) for release/API review, fork patch
disposition, plugin inventory, validation results, and staging gates. The repeatable operational
playbook lives in `../devinfra-deployment-service/AGENTS.md`.
