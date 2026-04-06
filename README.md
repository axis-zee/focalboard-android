# Focalboard Android

A native Android client for self-hosted Focalboard instances.

## What is this?

Focalboard is an open-source, self-hosted alternative to Trello, Notion, and Asana. This is a native Android app that connects to your self-hosted Focalboard server.

## Features (MVP)

- [ ] Connect to self-hosted Focalboard instance
- [ ] View boards and cards
- [ ] Create/edit cards
- [ ] Drag-and-drop cards between columns
- [ ] Offline support (local caching)

## Planned Features

- [ ] Push notifications
- [ ] Multiple server support
- [ ] Card attachments
- [ ] Comments and activity feed
- [ ] Dark mode

## Building

```bash
./gradlew assembleRelease
```

The APK will be in `app/build/outputs/apk/release/`

## Security

- No data is sent to external servers
- All data stays on your self-hosted instance
- Open source and auditable
- No tracking or analytics

## Contributing

Contributions welcome! Please read our [CONTRIBUTING.md](CONTRIBUTING.md) before submitting PRs.

## License

MIT License - see [LICENSE](LICENSE) for details.

## Credits

Built with ❤️ by the community for the community.

Focalboard is maintained by [Mattermost](https://github.com/mattermost/focalboard).
