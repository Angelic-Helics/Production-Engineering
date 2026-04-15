# Local Lab 2 Setup

This repository contains the Lab 2 starter project and can be used fully from local VS Code on Windows.

## Prerequisites

- Java 21
- Docker Desktop
- VS Code
- Optional: the VS Code `REST Client` extension for `requests.http`

## PowerShell Workflow

Start MongoDB:

```powershell
.\start_mongo_only.ps1
```

Build the project:

```powershell
.\build_local.ps1
```

Run the Spring Boot service:

```powershell
.\run_local.ps1
```

Stop Docker services:

```powershell
.\stop_mongo_only.ps1
```

## Local URLs

- API: `http://localhost:8080/api/users`
- Mongo Express: `http://localhost:8090`
- Mongo Express credentials: username `unibuc`, password `adobe`

## Notes

- The PowerShell scripts set `JAVA_HOME` and `GRADLE_USER_HOME` for the current session.
- `GRADLE_USER_HOME` is stored in the repo as `.gradle-user` so Gradle works cleanly from this workspace.
- If Java 21 is installed somewhere else, update the path at the top of `build_local.ps1` and `run_local.ps1`.
