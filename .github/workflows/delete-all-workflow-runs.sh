gh api repos/mandarbk/bookstore-app/actions/workflows --paginate -q '.workflows[].id' | \
xargs -I % bash -c 'gh api repos/mandarbk/bookstore-app/actions/workflows/%/runs --paginate -q ".workflow_runs[].id" | while read run_id; do [ -z "$run_id" ] && continue; echo "Deleting run $run_id..."; gh api repos/mandarbk/bookstore-app/actions/runs/$run_id -X DELETE; done'

#299911901
#301853543


gh api repos/mandarbk/bookstore-app/actions/workflows/301853543/runs --paginate -q '.workflow_runs[].id' | \
  xargs -n1 -I {} gh api repos/mandarbk/bookstore-app/actions/runs/{} -X DELETE


# Explicitly trigger a workflow ...
gh workflow run prod-ci.yml --ref Sprint -f force-build-all=true