"""Fail closed when release signing lacks a configured protected Environment."""
import json
import os
import urllib.request


def validate(data):
    """Require a reviewer and an explicit deployment-ref policy; never inspect secret values."""
    reviewers = [rule for rule in data.get('protection_rules', [])
                 if rule.get('type') == 'required_reviewers' and rule.get('reviewers')]
    policy = data.get('deployment_branch_policy') or {}
    if not reviewers or not (policy.get('custom_branch_policies') or policy.get('protected_branches')):
        raise ValueError('Configure required reviewers and deployment-ref restrictions on the signing Environment')


if __name__ == '__main__':
    repository = os.environ['GITHUB_REPOSITORY']
    environment = os.environ['SIGNING_ENVIRONMENT']
    if environment not in ('release-signing', 'release-review'):
        raise SystemExit('Unexpected signing Environment')
    request = urllib.request.Request(
        f'https://api.github.com/repos/{repository}/environments/{environment}',
        headers={'Authorization': 'Bearer ' + os.environ['GH_TOKEN'],
                 'Accept': 'application/vnd.github+json'})
    with urllib.request.urlopen(request, timeout=20) as response:
        validate(json.load(response))
    print('Protected signing Environment configured. Secret-scope migration remains an administrator responsibility.')
