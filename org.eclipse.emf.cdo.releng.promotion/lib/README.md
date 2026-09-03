# Library Directory

This directory contains external library JAR files required by the CDO Promotion releng project.

## Initial Setup: Installing Library JARs

When you first clone or set up this project, the library JAR files may be missing. To download and install the required libraries, you must execute the `lib-install.ant` Ant build script:

```bash
ant -f ../lib-install.ant
```

Or from the project root directory:

```bash
ant -f lib-install.ant
```

## What lib-install.ant Does

The `lib-install.ant` script automatically downloads and installs the following libraries:

- **GSON** (Google's JSON library) - version 2.10.1
  - `gson.jar` - compiled JAR
  - `gson-sources.zip` - source code

- **GitHub Core Library** (Eclipse EGit GitHub API) - version 6.1.0.202203080745-r
  - `github.jar` - compiled JAR

These libraries are downloaded from their respective repositories:
- GSON: Maven Central Repository
- GitHub Core: Eclipse Mylyn repository

## Contents

After running `lib-install.ant`, this directory will contain:

- `gson.jar` - GSON library
- `gson-sources.zip` - GSON source code
- `github.jar` - GitHub core library
- `.gitignore` - Git ignore patterns for downloaded files

## Notes

- The `.gitignore` file is configured to exclude JAR files from version control, ensuring that only the installation script is tracked.
- The `lib-install.ant` script includes cleanup logic to remove old versions before downloading new ones.
