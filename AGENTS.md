# AGENTS.md

This is Sentry's fork of [GoCD](https://github.com/gocd/gocd), `getsentry/gocd`. It exists to
supply the GoCD server image that
[`getsentry/devinfra-deployment-service`](https://github.com/getsentry/devinfra-deployment-service)
builds and runs as Sentry's deploy service (prod: `deploy.getsentry.net`).

- Default branch: `prod`. Whatever is on `prod` is what the next server image build ships.
- Forked from upstream at `647a5201b5` (2025-03-24). GoCD version 25.2.0 (`GO_VERSION_SEGMENTS`
  in `build.gradle`).
- No CI runs here (`.github/` was removed). The build that matters runs in
  devinfra-deployment-service's Cloud Build; see
  [How devinfra-deployment-service uses GoCD](#how-devinfra-deployment-service-uses-gocd).
- `README.md` is upstream's README, left unchanged. Upstream developer docs:
  https://developer.gocd.org/current/

## Local setup

Toolchain versions are pinned in `.tool-versions` (Temurin JDK 21, Node 22). On macOS:

```sh
brew install node@22 yarn openjdk@21 docker-buildx
```

Gradle will not download a JDK (`org.gradle.java.installations.auto-download=false` in
`gradle.properties`), so put JDK 21 and Node 22 first on `PATH`, as in the build command below.

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
PATH=/opt/homebrew/opt/openjdk@21/bin:/opt/homebrew/opt/node@22/bin:$PATH \
  ./gradlew -PdockerBuildLocalZip :docker:gocd-server:debian-12:docker
```

- Template: `buildSrc/src/main/resources/gocd-docker-server/Dockerfile.server.ftl`
- Rendered Dockerfile: `docker/gocd-server/target/debian-12/docker-gocd-server/Dockerfile`
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

`git diff 647a5201b5..prod` shows the full set. Keep this list current when adding or dropping a
patch, and re-check each item when merging from upstream. None of these patches came with tests;
add tests when you touch one.

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
- Dependency bumps: `bcprov-jdk18on` 1.80.2 and `jruby-rack` 1.2.4.1 (`dependencies.gradle`), and
  an `svgo` resolution (`rails/package.json`, which also raises the webpack heap to 4096 MB).
- An XXE fix in `GoConfigService` was merged (#18) and then reverted (#28). The revert gives no
  reason, so find out why before re-applying it.

Build:

- The server image is Debian 12 and is tagged `gocd-server:latest` (`settings-docker.gradle`,
  `BuildDockerImageTask.getImageNameWithTag`). Upstream builds Wolfi and tags `v<version>`.
- The built image is loaded into the local Docker daemon and kept, not verified and deleted.
- The Tanuki wrapper delta pack is fetched from the `sentry-dev-infra-build-assets` GCS mirror
  (`installers/tanuki.gradle`).

## Contracts devinfra-deployment-service relies on

Breaking any of these breaks GoCD's own deploy. Change both repos together.

- **Build entry point.** `cloudbuild.yaml` runs
  `./gradlew -PdockerBuildLocalZip :docker:gocd-server:debian-12:docker` and then builds
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
- **Agent version skew.** Elastic agents run an upstream go-agent tarball (25.1.0-20129, from
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
  `gocd_server_src_builder/Dockerfile` (JDK 21, Node 22, Yarn). It then layers
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
- One static agent from the chart's default image, meant for trivial jobs that need no
  credentials. Everything else runs on elastic agents.
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
- **Agent image:** `gocd_agent/Dockerfile` (Python 3.13 + upstream go-agent 25.1.0-20129 + JRE 21)
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
| `tomzo/gocd-yaml-config-plugin` (`yaml.config.plugin`) | v1.0.0-423 | Yes: the default `plugin-id` for deploy targets, including devinfra-deployment-service's own pipelines |
| `tomzo/gocd-json-config-plugin` | v1.0.0-273 | No |
| `gocd/gocd-ldap-authentication-plugin` | v2.3.0-386 | No |
| `gocd/gocd-filebased-authentication-plugin` | v2.2.0-300 | No |
| `gocd/gocd-file-based-secrets-plugin` | v1.2.0-310 | No |
