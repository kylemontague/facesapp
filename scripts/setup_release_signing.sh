#!/usr/bin/env bash
# ==============================================================================
# Faces — Release Keystore & Signing Setup Script
# ==============================================================================
# Generates a production release keystore (JKS), writes keystore.properties,
# extracts SHA-256 / SHA-1 certificate fingerprints, and optionally builds
# signed release App Bundles (.aab) for both Phone and Wear OS modules.
#
# Usage:
#   ./scripts/setup_release_signing.sh [optional_password] [--build]
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_ROOT"

KEYSTORE_FILE="faces-release.jks"
PROPERTIES_FILE="keystore.properties"
KEY_ALIAS="faces_upload"

# Colors for terminal output
BOLD="\033[1m"
GREEN="\033[0;32m"
CYAN="\033[0;36m"
YELLOW="\033[1;33m"
RED="\033[0;31m"
RESET="\033[0m"

echo -e "${BOLD}${CYAN}======================================================${RESET}"
echo -e "${BOLD}${CYAN}   Faces — Release Keystore & Signing Setup           ${RESET}"
echo -e "${BOLD}${CYAN}======================================================${RESET}\n"

# 1. Locate keytool binary
KEYTOOL_BIN=""
if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/keytool" ]; then
    KEYTOOL_BIN="$JAVA_HOME/bin/keytool"
elif [ -x "/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/keytool" ]; then
    KEYTOOL_BIN="/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/keytool"
elif command -v keytool >/dev/null 2>&1 && keytool -help >/dev/null 2>&1; then
    KEYTOOL_BIN="$(command -v keytool)"
fi

if [ -z "$KEYTOOL_BIN" ]; then
    echo -e "${RED}[Error] Could not find a working 'keytool' utility.${RESET}"
    echo "Please set JAVA_HOME or install Android Studio / Java 17+."
    exit 1
fi

echo -e "${GREEN}✓ Using keytool at:${RESET} $KEYTOOL_BIN"

# 2. Check for existing keystore
SHOULD_OVERWRITE=false
DO_BUILD=false

for arg in "$@"; do
    if [ "$arg" == "--force" ]; then
        SHOULD_OVERWRITE=true
    elif [ "$arg" == "--build" ]; then
        DO_BUILD=true
    fi
done

if [ -f "$KEYSTORE_FILE" ] && [ "$SHOULD_OVERWRITE" = false ]; then
    echo -e "\n${YELLOW}[Notice] Keystore file '$KEYSTORE_FILE' already exists.${RESET}"
    echo "Using existing keystore. Pass '--force' if you want to regenerate it."
else
    # 3. Determine password
    PASSWORD="${1:-}"
    if [ -z "$PASSWORD" ] || [ "$PASSWORD" == "--force" ] || [ "$PASSWORD" == "--build" ]; then
        # Auto-generate a cryptographically secure 24-character alphanumeric password
        PASSWORD="$(LC_ALL=C tr -dc 'A-Za-z0-9' </dev/urandom | head -c 24 || openssl rand -base64 18 | tr -dc 'A-Za-z0-9')"
        echo -e "${CYAN}→ Generated secure random password for keystore.${RESET}"
    fi

    echo -e "\n${BOLD}Generating production keystore '$KEYSTORE_FILE'...${RESET}"

    rm -f "$KEYSTORE_FILE"

    "$KEYTOOL_BIN" -genkeypair \
        -v \
        -keystore "$KEYSTORE_FILE" \
        -alias "$KEY_ALIAS" \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -storetype PKCS12 \
        -storepass "$PASSWORD" \
        -keypass "$PASSWORD" \
        -dname "CN=Faces App, OU=Mobile, O=FacesApp, L=London, ST=Greater London, C=GB"

    chmod 600 "$KEYSTORE_FILE"
    echo -e "${GREEN}✓ Keystore created successfully.${RESET}"

    # 4. Write keystore.properties
    echo -e "\n${BOLD}Writing '$PROPERTIES_FILE'...${RESET}"
    cat <<EOF > "$PROPERTIES_FILE"
# ==============================================================================
# Faces Release Signing Configuration (Generated: $(date))
# DO NOT COMMIT THIS FILE OR KEYSTORE TO SOURCE CONTROL.
# ==============================================================================
storeFile=$KEYSTORE_FILE
storePassword=$PASSWORD
keyAlias=$KEY_ALIAS
keyPassword=$PASSWORD
EOF

    chmod 600 "$PROPERTIES_FILE"
    echo -e "${GREEN}✓ '$PROPERTIES_FILE' written with permissions 600.${RESET}"
fi

# 5. Extract fingerprints
if [ -f "$PROPERTIES_FILE" ]; then
    STORE_PASS="$(grep '^storePassword=' "$PROPERTIES_FILE" | cut -d'=' -f2-)"
    ALIAS="$(grep '^keyAlias=' "$PROPERTIES_FILE" | cut -d'=' -f2-)"

    echo -e "\n${BOLD}${CYAN}------------------------------------------------------${RESET}"
    echo -e "${BOLD}Certificate Fingerprints (for Google Play Console):${RESET}"
    echo -e "${BOLD}${CYAN}------------------------------------------------------${RESET}"

    KEYTOOL_OUTPUT="$("$KEYTOOL_BIN" -list -v -keystore "$KEYSTORE_FILE" -alias "$ALIAS" -storepass "$STORE_PASS" 2>/dev/null || true)"

    SHA256_LINE="$(echo "$KEYTOOL_OUTPUT" | grep -i "SHA256:" | head -n 1 || true)"
    SHA1_LINE="$(echo "$KEYTOOL_OUTPUT" | grep -i "SHA1:" | head -n 1 || true)"

    echo -e "${GREEN}$SHA256_LINE${RESET}"
    echo -e "${GREEN}$SHA1_LINE${RESET}"
    echo -e "${BOLD}${CYAN}------------------------------------------------------${RESET}\n"
fi

# 6. Optional or automatic build test
if [ "$DO_BUILD" = true ]; then
    echo -e "${BOLD}Building signed release App Bundles (:mobile:bundleRelease & :wear:bundleRelease)...${RESET}"
    JAVA_HOME_DIR="$(dirname "$(dirname "$KEYTOOL_BIN")")"
    JAVA_HOME="$JAVA_HOME_DIR" ./gradlew :mobile:bundleRelease :wear:bundleRelease

    echo -e "\n${GREEN}✓ Build successful! Signed release App Bundles are ready at:${RESET}"
    echo -e "  • ${BOLD}Phone:${RESET}   mobile/build/outputs/bundle/release/mobile-release.aab"
    echo -e "  • ${BOLD}Wear OS:${RESET} wear/build/outputs/bundle/release/wear-release.aab"
else
    echo -e "${CYAN}Next step:${RESET} Run ${BOLD}./scripts/setup_release_signing.sh --build${RESET} or:"
    echo -e "  ${BOLD}./gradlew :mobile:bundleRelease :wear:bundleRelease${RESET}"
    echo -e "to compile your signed Google Play upload bundles."
fi
