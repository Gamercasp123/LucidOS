# LucidOS Build Guide

## Prerequisites

### System Requirements
- **OS**: Linux (Ubuntu 20.04+ recommended), macOS, or Windows with WSL2
- **RAM**: 8GB minimum, 16GB+ recommended
- **Storage**: 50GB+ free space
- **Java**: JDK 11 or later

### Required Tools
- Git
- Python 3.6+
- Gradle 7.0+
- Android SDK (API 33+)

## Installation

### 1. Install JDK

```bash
# Ubuntu/Debian
sudo apt-get install openjdk-11-jdk

# macOS
brew install openjdk@11
```

### 2. Install Android SDK

```bash
# Download Android SDK Command Line Tools
# https://developer.android.com/studio/command-line
```

### 3. Clone Repository

```bash
git clone https://github.com/yourusername/LucidOS.git
cd LucidOS
```

### 4. Configure Build

Create `local.properties`:
```
sdk.dir=/path/to/android/sdk
ndk.dir=/path/to/android/ndk
```

## Building Components

### Build Full ROM

```bash
./build.sh
```

### Build Individual Module

```bash
gradle -p packages/apps/:moduleName build
```

### Build System Image

```bash
./build_rom.sh
```

## Troubleshooting

### Out of Memory

Increase Gradle heap size in `gradle.properties`:
```
org.gradle.jvmargs=-Xmx4g
```

### Build Fails

1. Clean build directory: `gradle clean`
2. Check Java version: `java -version`
3. Verify SDK configuration

## Development Workflow

See DEVELOPMENT.md for development guidelines and tools.
