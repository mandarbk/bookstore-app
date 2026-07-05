#!/bin/bash

# Configuration
OWNER="mandarbk"
REPO="bookstore-app"

gh api --method POST -H "Accept: application/vnd.github+json" "/repos/$OWNER/$REPO/rulesets" --input ruleset-dev.json
echo "Protection applied successfully."

echo "All Rulesets "
gh api repos/$OWNER/$REPO/rulesets
## Define the protection rules in JSON format
## Adjust these settings based on your security requirements
#JSON_PAYLOAD='{
#  "required_status_checks": {
#      "strict": true,
#      "contexts": ["build-and-test", "build-and-scan-images"]
#  },
#  "enforce_admins": false,
#  "required_pull_request_reviews": {
#    "dismiss_stale_reviews": true,
#    "require_code_owner_reviews": false,
#    "required_approving_review_count": 1
#  },
#  "required_linear_history": true,
#  "restrictions": null,
#  "allow_force_pushes": false,
#  "allow_deletions": false
#}'
#
## Use GitHub CLI to PUT the configuration to the API
#echo "Applying branch protection to $REPO:$BRANCH..."
#echo "$JSON_PAYLOAD" | gh api --method PUT "repos/$REPO/branches/$BRANCH/protection" --input -
#


# First, find the ID of the existing ruleset if you already created one
# Or just POST a new one:
# gh api --method POST -H "Accept: application/vnd.github+json" "/repos/$OWNER/$REPO/rulesets" --input ruleset-main.json
