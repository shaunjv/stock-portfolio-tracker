"""
angel.py — All Angel One SmartAPI interactions.

Handles:
  - Logging in via SmartConnect.generateSession()
  - Fetching equity holdings via SmartConnect.holding()
  - Wrapping responses into clean Python dicts for the FastAPI layer.
"""

import os

from SmartApi import SmartConnect

from auth import generate_totp, save_session, get_session


def _get_client() -> SmartConnect:
    """
    Create and return a SmartConnect client instance.
    If an active session exists, set its access token so the client
    can make authenticated API calls.
    """
    api_key = os.getenv("ANGEL_API_KEY", "")
    if not api_key:
        raise ValueError("ANGEL_API_KEY environment variable is not set")

    client = SmartConnect(api_key=api_key)

    current_session = get_session()
    if current_session.is_active():
        client.setAccessToken(current_session.jwt_token)

    return client


def login() -> dict:
    """
    Authenticate with Angel One using environment variables.

    Steps:
      1. Create a SmartConnect client with the API key.
      2. Generate a TOTP code from the stored secret.
      3. Call generateSession with client code, password, and TOTP.
      4. Store the returned tokens in the in-memory session.

    Returns:
        dict with keys: success (bool), message (str)
    """
    client_code = os.getenv("ANGEL_CLIENT_CODE", "")
    password = os.getenv("ANGEL_PASSWORD", "")

    if not client_code:
        raise ValueError("ANGEL_CLIENT_CODE environment variable is not set")
    if not password:
        raise ValueError("ANGEL_PASSWORD environment variable is not set")

    client = _get_client()
    totp = generate_totp()

    try:
        data = client.generateSession(client_code, password, totp)
    except Exception as e:
        return {"success": False, "message": f"SmartAPI login error: {str(e)}"}

    if not data or not data.get("status"):
        error_msg = data.get("message", "Unknown error") if data else "No response from Angel One"
        return {"success": False, "message": f"Login failed: {error_msg}"}

    # Extract tokens from the successful response
    session_data = data["data"]
    save_session(
        jwt_token=session_data["jwtToken"],
        refresh_token=session_data["refreshToken"],
        feed_token=session_data.get("feedToken", ""),
    )

    return {"success": True, "message": "Login successful"}


def get_holdings() -> dict:
    """
    Fetch all equity holdings from Angel One.

    Returns:
        dict with keys:
          - success (bool)
          - message (str)
          - holdings (list of dicts) — each dict contains:
              tradingsymbol, exchange, quantity, averageprice,
              ltp, profitandloss, pnlpercentage
    """
    current_session = get_session()
    if not current_session.is_active():
        return {
            "success": False,
            "message": "No active session. Please call /api/login first.",
            "holdings": [],
        }

    client = _get_client()

    try:
        response = client.holding()
    except Exception as e:
        return {
            "success": False,
            "message": f"Error fetching holdings: {str(e)}",
            "holdings": [],
        }

    if not response or not response.get("status"):
        error_msg = (
            response.get("message", "Unknown error") if response else "No response from Angel One"
        )
        return {"success": False, "message": error_msg, "holdings": []}

    raw_holdings = response.get("data", []) or []

    # Map each holding to a clean, consistent dict for the Android app
    holdings = []
    for h in raw_holdings:
        holdings.append(
            {
                "tradingsymbol": h.get("tradingsymbol", ""),
                "exchange": h.get("exchange", ""),
                "quantity": h.get("quantity", 0),
                "averageprice": h.get("averageprice", 0.0),
                "ltp": h.get("ltp", 0.0),
                "profitandloss": h.get("profitandloss", 0.0),
                "pnlpercentage": h.get("pnlpercentage", 0.0),
            }
        )

    return {"success": True, "message": "Holdings fetched successfully", "holdings": holdings}
