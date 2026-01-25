# Publishing to Maven Central

This document describes how to set up and publish the YAMLStar Java binding to
Maven Central using the Central Publisher Portal.

**Note**: The old OSSRH system (issues.sonatype.org) was shut down on June 30,
2025.
All new registrations must use the Central Publisher Portal.

## Prerequisites

- GPG key for signing artifacts
- Central Publisher Portal account
- Domain ownership of yaml.com (for `com.yaml` groupId)

## Step 1: Create Central Portal Account

1. Go to https://central.sonatype.com and click "Sign In"
2. Create an account via Google, GitHub, or email
3. If using email credentials, verify your email address

## Step 2: Register and Verify Namespace

1. Log into the Central Portal at https://central.sonatype.com
2. Click your username (top right) → "View Namespaces"
3. Click "Add Namespace"
4. Enter `com.yaml` as the namespace
5. The Portal will provide a verification key
6. Add a DNS TXT record to yaml.com with the provided verification key:
   ```
   TXT  @  "central-namespace-verification=XXXXX"
   ```
7. Wait for DNS to propagate (usually a few minutes, check with `dig TXT
   yaml.com`)
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
central:
  token: "YOUR_BASE64_ENCODED_TOKEN"
gpg:
  key-id: "YOUR_GPG_KEY_ID"
```

The `central.token` is the base64-encoded "Username : Password" value shown
when you generate a user token at https://central.sonatype.com/usertoken.
Copy the value from the "Username : Password (base64)" field.

The release script (`util/release`) automatically:
1. Reads the base64 token from `~/.yamlstar-secrets.yaml`
2. Exports it as `CENTRAL_TOKEN` for the upload API
3. Exports `GPG_KEY_ID` for artifact signing
4. Runs `make deploy` which builds and uploads the bundle

## Publishing

### Build and Upload

```bash
make release-java
```

This command:
1. Builds the main JAR (`lein jar`)
2. Generates the POM file (`lein pom`)
3. Creates sources JAR (from `core/src` and `java/src`)
4. Creates javadoc JAR (placeholder for Clojure)
5. Signs all artifacts with GPG (`.asc` files)
6. Generates MD5 and SHA1 checksums
7. Creates a bundle ZIP with Maven repository layout
8. Uploads the bundle to Central Portal via the Publisher API

The upload uses `publishingType=AUTOMATIC`, so if validation passes, artifacts
are automatically published to Maven Central.

### Monitor Deployment

1. Log into https://central.sonatype.com
2. Click "Deployments" in the left sidebar
3. Find your deployment (com.yaml)
4. Check the status: PENDING → VALIDATING → VALIDATED → PUBLISHING → PUBLISHED
5. If validation fails, review the error messages, fix issues, and re-deploy
6. After publishing, artifacts sync to Maven Central within ~30 minutes

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
2. Copy the "Username : Password (base64)" value
3. Update `central.token` in `~/.yamlstar-secrets.yaml`

### Upload Fails with curl Error

If curl fails, check that:
- The base64 token doesn't have extra whitespace or newlines
- Your network can reach https://central.sonatype.com

### Validation Fails in Portal

Common issues:
- Missing POM elements (name, description, url, licenses, developers, scm)
- Missing signatures (.asc files)
- Missing javadoc JAR
- Missing sources JAR
- Invalid checksums

Check the deployment details in the Central Portal for specific error messages.

## References

- [Central Portal Registration](https://central.sonatype.org/register/central-portal/)
- [Namespace Verification](https://central.sonatype.org/register/namespace/)
- [Generate Portal Token](https://central.sonatype.org/publish/generate-portal-token/)
- [Publisher API Documentation](https://central.sonatype.org/publish/publish-portal-api/)
- [Bundle Upload Requirements](https://central.sonatype.org/publish/publish-portal-upload/)
- [OSSRH EOL Announcement](https://central.sonatype.org/pages/ossrh-eol/)
- [GPG Key Management](https://central.sonatype.org/publish/requirements/gpg/)
