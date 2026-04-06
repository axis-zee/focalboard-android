#!/bin/bash
# Focalboard Android Progress Report Script
# Run this every 30 minutes to backup work and send progress report

set -e

REPO_DIR="/home/zaytun/.openclaw/workspace/focalboard-android"
cd "$REPO_DIR"

# Generate report
REPORT=$(cat <<EOF
## 📊 Focalboard Android - Progress Report
**Generated:** $(date '+%Y-%m-%d %H:%M:%S')

### 📁 Recent Commits
$(git log --oneline -5 2>/dev/null || echo "No commits")

### 📊 Repository Stats
- **Total commits:** $(git rev-list --count HEAD 2>/dev/null || echo "0")
- **Total files:** $(find . -type f -not -path './.git/*' | wc -l)
- **Total lines of code:** $(find . -name "*.kt" -o -name "*.kts" -o -name "*.xml" | xargs wc -l 2>/dev/null | tail -1 | awk '{print $1}' || echo "0")

### 📦 Uncommitted Changes
$(git status --short 2>/dev/null || echo "No changes")

### 🔄 Last Push
$(git log -1 --format="%cr" 2>/dev/null || echo "Never")

### 📝 Next Tasks
- [ ] Implement Focalboard API client
- [ ] Add actual login authentication
- [ ] Fetch and display boards
- [ ] Add board detail screen with Kanban view
- [ ] Implement card CRUD operations
- [ ] Add drag-and-drop for cards
- [ ] Add offline caching with Room
EOF
)

# Commit if there are uncommitted changes
if ! git diff --quiet 2>/dev/null; then
    git add -A
    git commit -m "chore: auto-save progress $(date '+%Y-%m-%d %H:%M')" || true
fi

# Push to remote
git push origin main 2>/dev/null || true

# Output report
echo "$REPORT"
