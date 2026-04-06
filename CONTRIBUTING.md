# Contributing to Focalboard Android

Thank you for your interest in contributing! This guide will help you get started.

## How to Contribute

### Reporting Bugs

- Check if the bug already exists in issues
- Create a new issue with clear reproduction steps
- Include your Android version and app version

### Suggesting Features

- Open an issue with a clear description
- Explain the use case and why it's valuable
- Be prepared to discuss trade-offs

### Submitting Code

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Write/update tests
5. Update documentation
6. Commit with clear messages
7. Push to your fork
8. Open a Pull Request

## Development Setup

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34 (Android 14)
- Git

### Building

```bash
./gradlew build
```

### Running Tests

```bash
./gradlew test
./gradlew connectedAndroidTest
```

### Code Style

- Follow the existing Kotlin style
- Use 4-space indentation
- Run `./gradlew ktlintFormat` before committing

## Security

- Never include API keys or secrets in code
- Report security vulnerabilities privately
- All data stays on user's self-hosted instance

## Code of Conduct

- Be respectful and inclusive
- Give constructive feedback
- Accept constructive criticism
- Focus on what's best for the community

## Questions?

Open an issue or reach out to the maintainers. We're happy to help!
