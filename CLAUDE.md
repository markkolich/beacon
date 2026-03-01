# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Beacon is a Java 11 dynamic DNS updater that syncs a UniFi UDM's WAN uplink IP with AWS Route 53 DNS records, and optionally updates NextDNS linked IP. It runs as a self-contained Jetty web server with Quartz-scheduled jobs.

## Build & Run

```bash
# Build (compiles, runs tests, checkstyle, PMD, and produces fat JAR in dist/)
mvn clean package

# Run
java -Dconfig.file=~/beacon-dev.conf -jar dist/beacon-0.1-runnable.jar

# Run tests only
mvn test

# Run a single test class
mvn test -Dtest=ClassName

# Check for dependency updates
mvn versions:display-dependency-updates
mvn versions:display-plugin-updates
```

## Architecture

- **Framework**: [Curacao](https://github.com/markkolich/curacao) (servlet-based MVC) on embedded Jetty 11 (Jakarta Servlet)
- **DI**: Curacao's `@Injectable` annotation for constructor injection; `@Controller` for request handlers
- **Config**: Typesafe Config (HOCON) — base config in `src/main/resources/application.conf`, overridden at runtime via `-Dconfig.file`; boot package is `com.kolich.beacon`
- **Scheduling**: Quartz scheduler runs `BeaconJob` on a cron schedule to poll UDM and update Route 53/NextDNS
- **Templating**: FreeMarker templates in `src/main/webapp/templates/`
- **Entry point**: `com.kolich.beacon.Application` — configures Jetty server, context path `/beacon`

### Key Component Packages

- `components/unifi/` — UDM controller API client (auth + uplink IP discovery)
- `components/aws/` — AWS credentials/config and Route 53 client
- `components/nextdns/` — NextDNS linked IP update client
- `components/quartz/` — Quartz scheduler setup and `BeaconJob` (core sync logic)
- `components/freemarker/` — FreeMarker template configuration
- `controllers/` — Curacao HTTP controllers
- `mappers/` — Curacao response mappers

## Code Style Conventions

The build enforces strict Checkstyle and PMD rules that **fail the build** on violation:

- **All non-exception classes must be `final`** — `public final class Foo`
- **Member fields use trailing underscore**: `private final String name_;`
- **Method/constructor parameters must each start on a new line** (no inline args)
- **All parameters and local variables must be `final`**
- **4-space indentation**, 8-space line wrapping continuation; max line length 120
- **No tabs**, no trailing whitespace, newline at end of file
- **No consecutive blank lines** (except after package/import)
- **All source files require the MIT license header** (see `build/checkstyle/LICENSE.txt`)
- **Banned imports**: `java.util.logging.*` (use SLF4J), `commons-lang` without `3`, `commons-collections` without `4`, `com.google.common.base.Charsets/Optional`
- Tests run in `America/Los_Angeles` timezone
