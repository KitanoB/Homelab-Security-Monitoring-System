#!/bin/bash

OWNER="@me"
PROJECT_TITLE="Sprint Board"
SPRINT_COUNT=4
SPRINT_DURATION=14
START_DATE=$(date +%Y-%m-%d)

# Check for tools
if ! command -v gdate &> /dev/null; then
  echo "⚠️  'gdate' is missing. Run: brew install coreutils"
  exit 1
fi

# Create project and capture the URL
echo "🛠 Creating project..."
PROJECT_URL=$(gh project create --title "$PROJECT_TITLE" --owner "$OWNER" --format json | jq -r ".url")
echo "✅ Project created: $PROJECT_URL"

# Extract number from the URL (e.g., .../projects/3 → 3)
PROJECT_NUMBER=$(echo "$PROJECT_URL" | grep -oE "projects/[0-9]+" | cut -d'/' -f2)

if [[ -z "$PROJECT_NUMBER" ]]; then
  echo "❌ Failed to extract project number from URL: $PROJECT_URL"
  exit 1
fi

# Add custom fields
echo "📦 Creating custom fields..."

gh project field-create "$PROJECT_NUMBER" --owner "$OWNER" \
  --name "Status" --data-type SINGLE_SELECT --single-select-options "To Do,In Progress,Done"

gh project field-create "$PROJECT_NUMBER" --owner "$OWNER" \
  --name "Priority" --data-type SINGLE_SELECT --single-select-options "High,Medium,Low"

gh project field-create "$PROJECT_NUMBER" --owner "$OWNER" \
  --name "Type" --data-type SINGLE_SELECT --single-select-options "Bug,Feature,Task,Docs"

echo "✅ Fields created."

# Iteration reminder
echo
echo "🚧 Iterations (Sprints) can't be created from CLI yet."
echo "👉 Open your project and add them manually under ⚙️ Settings → Fields → Iteration"
echo "🔗 $PROJECT_URL"
echo
echo "🎉 Setup complete!"