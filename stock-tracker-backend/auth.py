"""
auth.py — TOTP generation and in-memory session token management.

Handles:
  - Generating 6-digit TOTP codes from the ANGEL_TOTP_SECRET env var.
  - Storing the current Angel One session (JWT + refresh token) in memory.
  - Checking whether the current session is still valid (based on login timestamp).
"""

import os
import time
from dataclasses import dataclass, field

import pyotp


@dataclass
class SessionState:
    """Holds the current Angel One session tokens and metadata."""
    jwt_token: str = ""
    refresh_token: str = ""
    feed_token: str = ""
    logged_in_at: float = 0.0  # Unix timestamp of last successful login

    def is_active(self) -> bool:
        """
        Returns True if a session exists and was created less than 8 hours ago.
        Angel One JWT tokens are valid for ~24 hours, but we use a conservative
        8-hour window to avoid edge-case expiry during active use.
        """
        if not self.jwt_token:
            return False
        hours_elapsed = (time.time() - self.logged_in_at) / 3600
        return hours_elapsed < 8

    def clear(self) -> None:
        """Wipe all stored session data."""
        self.jwt_token = ""
        self.refresh_token = ""
        self.feed_token = ""
        self.logged_in_at = 0.0


# Single in-memory session instance shared across the app
session = SessionState()


def generate_totp() -> str:
    """
    Generate a 6-digit TOTP code from the ANGEL_TOTP_SECRET environment variable.
    Raises ValueError if the env var is missing or empty.
    """
    totp_secret = os.getenv("ANGEL_TOTP_SECRET", "")
    if not totp_secret:
        raise ValueError("ANGEL_TOTP_SECRET environment variable is not set")
    return pyotp.TOTP(totp_secret).now()


def save_session(jwt_token: str, refresh_token: str, feed_token: str) -> None:
    """Store a successful login's tokens in the in-memory session."""
    session.jwt_token = jwt_token
    session.refresh_token = refresh_token
    session.feed_token = feed_token
    session.logged_in_at = time.time()


def get_session() -> SessionState:
    """Return the current session state."""
    return session
