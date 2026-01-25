# Publishing to Maven Central

This document describes how to set up and publish the YAMLStar Java binding to
Maven Central using the Central Publisher Portal.

**Note**: The old OSSRH system (issues.sonatype.org) was shut down on June 30,
2025.
All new registrations must use the Central Publisher Portal.

## Prerequisites

- GPG key for signing artifacts
- Central Publisher Portal account
- Domain ownership of yamlstar.org (for `org.yamlstar` groupId)

## Step 1: Create Central Portal Account

1. Go to https://central.sonatype.com and click "Sign In"
2. Create an account via Google, GitHub, or email
3. If using email credentials, verify your email address

## Step 2: Register and Verify Namespace

1. Log into the Central Portal at https://central.sonatype.com
2. Click your username (top right) → "View Namespaces"
3. Click "Add Namespace"
4. Enter `org.yamlstar` as the namespace
5. The Portal will provide a verification key
6. Add a DNS TXT record to yamlstar.org with the provided verification key:
   ```
   TXT  @  "central-namespace-verification=XXXXX"
   ```
7. Wait for DNS to propagate (usually a few minutes, check with `dig TXT
   yamlstar.org`)
8. Click "Verify" in the Portal

The verification is usually instant once DNS propagates.

## Step 3: GPG Key Setup

Generate a new GPG key:

```bash
gpg --full-generate-key
# Select: (1) RSA and RSA
# Key size: 4096
# Expiration: 0 (does not expire) or set your preference
# Enter your name and email
```

List your keys to find the key ID:

```bash
gpg --list-secret-keys --keyid-format=long
# Output will show something like:
# sec   rsa4096/ABCD1234EFGH5678 2025-01-25 [SC]
# The key ID is "ABCD1234EFGH5678"
```

Upload your public key to keyservers (Maven Central checks these):

```bash
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID
```

## Step 4: Generate Central Portal User Token

1. Log into https://central.sonatype.com
2. Go to https://central.sonatype.com/usertoken
3. Click "Generate User Token"
4. Set a display name and expiration date
5. **Save the username and password immediately** - they cannot be retrieved
   later

## Step 5: Configure Credentials

YAMLStar uses a centralized secrets file for all language bindings, including
Java.
When you run `make release-java`, the release script reads credentials from
this file and exports them as environment variables.

Add your Maven Central credentials to `~/.yamlstar-secrets.yaml`:

```yaml
ossrh:
  user: "YOUR_PORTAL_TOKEN_USERNAME"
  token: "YOUR_PORTAL_TOKEN_PASSWORD"
gpg:
  key-id: "YOUR_GPG_KEY_ID"
```

The release script (`util/release`) automatically:
1. Reads credentials from `~/.yamlstar-secrets.yaml`
2. Exports them as `OSSRH_USERNAME` and `OSSRH_PASSWORD`
3. Runs `lein deploy` which reads from these environment variables

**Note**: You do NOT need to create `~/.lein/credentials.clj.gpg`.
The environment variable approach is used for consistency with other YAMLStar
language bindings.

## Publishing

### Deploy to Staging

```bash
make release-java
```

This uploads the artifacts to the staging repository using the OSSRH
compatibility API.

### Complete Release in Central Portal

1. Log into https://central.sonatype.com
2. Click "Deployments" in the left sidebar
3. Find your deployment (org.yamlstar)
4. The Portal automatically runs validation checks
5. If validation passes, click "Publish" to release to Maven Central
6. If validation fails, review the error messages, fix issues, and re-deploy
7. After publishing, artifacts sync to Maven Central within ~30 minutes

**Note**: Unlike the old Nexus UI with separate "Close" and "Release" steps,
the Central Portal combines validation and publishing into a streamlined
workflow.

## Troubleshooting

### GPG Signing Fails

If signing fails with "No secret key":

```bash
# Verify your key is available
gpg --list-secret-keys

# If using gpg-agent, ensure it's running
gpg-agent --daemon
```

### Upload Fails with 401

Your credentials may be incorrect or expired:

1. Regenerate user token in Central Portal at
   https://central.sonatype.com/usertoken
2. Update `~/.lein/credentials.clj`
3. Re-encrypt the file

### Validation Fails in Portal

Common issues:
- Missing POM elements (name, description, url, licenses, developers, scm)
- Missing signatures (.asc files)
- Missing javadoc JAR
- Missing sources JAR

Check the deployment details in the Central Portal for specific error messages.

## References

- [Central Portal Registration](https://central.sonatype.org/register/central-portal/)
- [Namespace Verification](https://central.sonatype.org/register/namespace/)
- [Generate Portal Token](https://central.sonatype.org/publish/generate-portal-token/)
- [Publishing Guide](https://central.sonatype.org/publish/publish-portal-guide/)
- [OSSRH EOL Announcement](https://central.sonatype.org/pages/ossrh-eol/)
- [Leiningen Deploy Documentation](https://github.com/technomancy/leiningen/blob/master/doc/DEPLOY.md)
- [GPG Key Management](https://central.sonatype.org/publish/requirements/gpg/)
