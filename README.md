# Charm mPHR — Android Application

Patient-facing mobile app for the Charm Health EHR platform.

- **Package**: `com.charm.phr.mphr`
- **Min SDK**: 23 (Android 6.0) · **Target SDK**: 35 · **Compile SDK**: 36
- **Stack**: Kotlin 2.2.20, Android Views (XML) + Jetpack Compose, Dagger 2, MVVM
- **Flavors**: `common` (production), `dev` (testing), `thalassemia` (white-label), `neuroConnect` (white-label)
- **Device support**: Phones only; supports Android 6.0+

## Documentation

The three docs below are the full handoff package. Open them in order if you are new to the project.

| Doc | What it covers |
|---|---|
| [docs/setup.md](./docs/setup.md) | Setup, build, run on a device, test, release |
| [docs/architecture.md](./docs/architecture.md) | Layers, MVVM flow, repositories, DI, navigation, technical stack |
| [docs/workflow.md](./docs/workflow.md) | Screen-by-screen user journey for patients and caregivers |

## Quick start

```bash
git clone --recurse-submodules <repository_url> charm-phr-android
cd charm-phr-android
cp secure.properties.example secure.properties   # then fill in the CharmAuth settings
./gradlew installCommonDebug
```

A handful of config files (notably `secure.properties`, `google-services.json`, `apptics-config.json`, `local.properties`) must be in place before the first build — see [docs/setup.md](./docs/setup.md).

> Sign-in uses the CharmHealth auth SDK, a git submodule at `charm-mobile-auth-sdk` (branch `ch1575-chis-mode`). If you cloned without `--recurse-submodules`, run `git submodule update --init --recursive`. Apptics resolves from the public `https://maven.zohodl.com/` repo, so no Zoho Maven credentials are needed.

## Overview

Charm mPHR is an Android application that empowers patients to manage their healthcare information. Patients use it to view medical records, schedule appointments, communicate with providers, manage prescriptions, pay bills, track vitals, complete questionnaires, and participate in video consultations (Zoom/OpenTok). Caregivers can manage records for dependents. The app supports white-label deployments for specialized practices.

## Git & contribution note

Automated assistants (e.g., GitHub Copilot, Claude Code) used for repository work should **not** perform any git operations (commits, pushes, or branch changes). The repository owner and maintainers are solely responsible for all git commits and pushes.

