#!/usr/bin/env python3
"""Print the latest versionCode published for the app on Google Play.

Reads PACKAGE_NAME and GOOGLE_PLAY_SERVICE_ACCOUNT_JSON from the environment.
The service account JSON may be given inline (as a secret) or as a file path.
"""

import json
import os
import sys


def load_service_account(raw):
    raw = (raw or "").strip()
    if not raw:
        raise SystemExit("GOOGLE_PLAY_SERVICE_ACCOUNT_JSON is not set")
    if raw.startswith("{"):
        return json.loads(raw)
    with open(raw, "r", encoding="utf-8") as fh:
        return json.load(fh)


def main():
    package = os.environ.get("PACKAGE_NAME", "education.mahmoud.quranyapp")
    info = load_service_account(os.environ.get("GOOGLE_PLAY_SERVICE_ACCOUNT_JSON", ""))

    try:
        from google.oauth2 import service_account
        from googleapiclient.discovery import build
    except ImportError:
        raise SystemExit(
            "Missing dependencies: pip install google-auth google-api-python-client"
        )

    creds = service_account.Credentials.from_service_account_info(
        info, scopes=["https://www.googleapis.com/auth/androidpublisher"]
    )
    service = build("androidpublisher", "v3", credentials=creds, cache_discovery=False)

    edit = service.edits().insert(packageName=package, body={}).execute()
    edit_id = edit["id"]
    latest = 0
    try:
        result = service.edits().tracks().list(
            packageName=package, editId=edit_id
        ).execute()
        for track in result.get("tracks", []) or []:
            for release in track.get("releases", []) or []:
                for code in release.get("versionCodes", []) or []:
                    try:
                        latest = max(latest, int(code))
                    except (TypeError, ValueError):
                        continue
    finally:
        try:
            service.edits().delete(packageName=package, editId=edit_id).execute()
        except Exception:
            pass

    print(latest)


if __name__ == "__main__":
    main()
