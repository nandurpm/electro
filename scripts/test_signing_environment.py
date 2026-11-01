"""A named but unprotected Environment is not a signing-security boundary."""
import unittest
from verify_signing_environment import validate


class SigningEnvironmentTests(unittest.TestCase):
    def test_unprotected_or_unrestricted_environments_are_rejected(self):
        for data in ({}, {'protection_rules': [{'type': 'required_reviewers', 'reviewers': [{}]}]},
                     {'deployment_branch_policy': {'custom_branch_policies': True}}):
            with self.assertRaises(ValueError):
                validate(data)

    def test_review_and_ref_policy_are_both_required(self):
        validate({'protection_rules': [{'type': 'required_reviewers', 'reviewers': [{'type': 'User'}]}],
                  'deployment_branch_policy': {'custom_branch_policies': True}})
