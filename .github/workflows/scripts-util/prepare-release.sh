#The "Production-Stable" Flow
#  MAKE SURE YOU EXECUTE THE SCRIPT ON DEV BRANCH's checked out code.

#     Development: Developers work in feature branches (v1.0.0-SNAPSHOT), merging into Sprint.
#         Upon passing all the successful code quality and build/test metrics requirements, the code from feature branches is then merged
#         into the 'dev' Branch (via PRs a.k.a. Pull Requests).

#     Release Preparation: When 'dev'' is stable, the Tech Lead prepares the release.

#     Merge & Tag: The Tech Lead merges "dev" into "main" by executing the following script.
#          $ ./prepare-release.sh 1.0.0 1.0.1-SNAPSHOT
#     Tagging: The Tech Lead tags 'main' at the release commit (1.0.0).
#     Now, 'main' is officially 1.0.0.
#     Back-to-Work: The Tech Lead then bumps the version on the 'dev' branch to 1.1.0-SNAPSHOT so the team can continue working.

#   'dev' Branch: This is your "Integration" branch. This is where the team works, runs tests, and integrates features.
#   'main' Branch: This is your "Release" branch. Only the Tech Lead merges to main.

#!/bin/bash
# Usage: ./prepare-release.sh <RELEASE_VERSION> <SOURCE_VERSION_NEW>
# Usage: ./prepare-release.sh 1.0.0 1.0.1-SNAPSHOT --
# This will release Version 1.0.0 from "main" branch and then bump the "dev" branch's version to the specified new-source-version.

set -e

# Run this from the Sprint branch
RELEASE_VERSION=$1
NEXT_VERSION=$2
DEVELOPMENT_BRANCH=dev
RELEASE_BRANCH=main

if [ -z "$RELEASE_VERSION" ] || [ -z "$NEXT_VERSION" ]; then
    echo "Usage: ./prepare-release.sh <release_version> <next_snapshot_version>"
    exit 1
fi

CURRENT_BRANCH=$(git branch --show-current)
if [ "$CURRENT_BRANCH" != "$DEVELOPMENT_BRANCH" ]; then
    echo "Error: You must be on the 'Dev' branch to perform a release."
    exit 1
fi

echo "--- Starting Release Train: $RELEASE_VERSION ---"

# 1. Update 'dev' to 1.0.0 (The Release)
mvn versions:set -DnewVersion=$RELEASE_VERSION -DgenerateBackupPoms=false
git add .
git commit -m "Release $RELEASE_VERSION"

# 2. Merge 'dev' into Main
git checkout $RELEASE_BRANCH
git merge $DEVELOPMENT_BRANCH
git tag "api-gateway/$RELEASE_VERSION"
git tag "auth-server/$RELEASE_VERSION"
git tag "book-api/$RELEASE_VERSION"
git tag "bookstore-eureka-server/$RELEASE_VERSION"
git tag "config-server/$RELEASE_VERSION"
git tag "order-api/$RELEASE_VERSION"
git push origin $RELEASE_BRANCH --tags

# 3. Switch back to 'dev' and bump to Snapshot
git checkout $DEVELOPMENT_BRANCH
mvn versions:set -DnewVersion=$NEXT_VERSION -DgenerateBackupPoms=false
git add .
git commit -m "Bump to $NEXT_VERSION"
git push origin $DEVELOPMENT_BRANCH

echo "--- Release Complete: Main is at $RELEASE_VERSION, Sprint is at $NEXT_VERSION ---"