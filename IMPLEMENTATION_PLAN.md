# Focalboard Android - Implementation Plan

## Phase 1: Core API & Auth (Current Focus)
- [x] Project scaffolding
- [ ] Implement Focalboard API client (Retrofit)
  - [ ] Auth endpoints (login, token refresh)
  - [ ] Board listing endpoint
  - [ ] Board detail endpoint
  - [ ] Card/kanban endpoints
- [ ] Authentication repository
  - [ ] Login flow
  - [ ] Token storage (DataStore)
  - [ ] Auto-refresh logic
- [ ] API testing with localhost:8081

## Phase 2: Basic UI Screens
- [ ] Login screen
- [ ] Board list screen
- [ ] Board detail screen (Kanban view)
- [ ] Navigation setup

## Phase 3: Data & Persistence
- [ ] Room database setup
- [ ] Offline caching strategy
- [ ] Sync logic

## Phase 4: Polish
- [ ] Card CRUD operations
- [ ] Drag-and-drop
- [ ] Error handling
- [ ] Loading states
- [ ] Build & test APK

---
*Quality over speed. Test each component before moving on.*
