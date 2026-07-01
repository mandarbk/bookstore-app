#The "Production-Stable" Flow

#     Development: Developers work in feature branches (v1.0.0-SNAPSHOT), merging into Sprint.
#         Upon passing all the successful code quality and build/test metrics requirements, the code from feature branches is then merged
#         into the 'Sprint' Branch (via PRs a.k.a. Pull Requests).

#     Release Preparation: When Sprint is stable, the Tech Lead prepares the release.

#     Merge & Tag: The Tech Lead merges Sprint into main by executing the following script.
#     Tagging: The Tech Lead tags main at the release commit (1.0.0).
#     Now, main is officially 1.0.0.
#     Back-to-Work: The Tech Lead then bumps the version on the Sprint branch to 1.1.0-SNAPSHOT so the team can continue working.

#   Sprint Branch: This is your "Integration" branch. This is where the team works, runs tests, and integrates features.
#   Main Branch: This is your "Release" branch. Only the Tech Lead merges to main.

#!/bin/bash
# Usage: ./prepare-release.sh 1.0.0 1.0.1-SNAPSHOT
set -e

# Run this from the Sprint branch
RELEASE_VERSION=$1
NEXT_VERSION=$2

if [ -z "$RELEASE_VERSION" ] || [ -z "$NEXT_VERSION" ]; then
    echo "Usage: ./prepare-release.sh <release_version> <next_snapshot_version>"
    exit 1
fi

CURRENT_BRANCH=$(git branch --show-current)
if [ "$CURRENT_BRANCH" != "Sprint" ]; then
    echo "Error: You must be on the 'Sprint' branch to perform a release."
    exit 1
fi

echo "--- Starting Release Train: $RELEASE_VERSION ---"

# 1. Update Sprint to 1.0.0 (The Release)
mvn versions:set -DnewVersion=$RELEASE_VERSION -DgenerateBackupPoms=false
git add .
git commit -m "Release $RELEASE_VERSION"

# 2. Merge Sprint into Main
git checkout main
git merge Sprint
git tag "api-gateway/$RELEASE_VERSION"
git tag "auth-server/$RELEASE_VERSION"
git tag "book-api/$RELEASE_VERSION"
git tag "bookstore-eureka-server/$RELEASE_VERSION"
git tag "cart-management-service/$RELEASE_VERSION"
git tag "config-server/$RELEASE_VERSION"
git tag "order-api/$RELEASE_VERSION"
git push origin main --tags

# 3. Switch back to Sprint and bump to Snapshot
git checkout Sprint
mvn versions:set -DnewVersion=$NEXT_VERSION -DgenerateBackupPoms=false
git add .
git commit -m "Bump to $NEXT_VERSION"
git push origin Sprint

echo "--- Release Complete: Main is at $RELEASE_VERSION, Sprint is at $NEXT_VERSION ---"