"""
angel.py — All Angel One SmartAPI interactions.

Handles:
  - Logging in via SmartConnect.generateSession()
  - Fetching equity holdings via SmartConnect.holding()
  - Wrapping responses into clean Python dicts for the FastAPI layer.
"""

import os
import logging

from SmartApi import SmartConnect

from auth import generate_totp, save_session, get_session

# Configure logging
logger = logging.getLogger("angel")
logger.setLevel(logging.INFO)
if not logger.handlers:
    ch = logging.StreamHandler()
    ch.setLevel(logging.INFO)
    formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
    ch.setFormatter(formatter)
    logger.addHandler(ch)

# Store the authenticated client globally to reuse the same instance
_global_client = None


def _get_client() -> SmartConnect:
    """
    Create or reuse a SmartConnect client instance.
    Sets all required tokens if an active session exists.
    """
    global _global_client
    
    current_session = get_session()
    client_code = os.getenv("ANGEL_CLIENT_CODE", "")
    
    if _global_client is not None:
        logger.info("Reusing existing SmartConnect instance.")
        if current_session.is_active():
            _global_client.setAccessToken(current_session.jwt_token)
            _global_client.setRefreshToken(current_session.refresh_token)
            if hasattr(_global_client, 'setFeedToken'):
                _global_client.setFeedToken(current_session.feed_token)
            else:
                _global_client.feedToken = current_session.feed_token
            _global_client.setUserId(client_code)
        return _global_client

    api_key = os.getenv("ANGEL_API_KEY", "")
    if not api_key:
        raise ValueError("ANGEL_API_KEY environment variable is not set")

    logger.info("Creating new SmartConnect instance.")
    _global_client = SmartConnect(api_key=api_key)

    if current_session.is_active():
        logger.info("Applying stored tokens to new SmartConnect instance.")
        _global_client.setAccessToken(current_session.jwt_token)
        _global_client.setRefreshToken(current_session.refresh_token)
        if hasattr(_global_client, 'setFeedToken'):
            _global_client.setFeedToken(current_session.feed_token)
        else:
            _global_client.feedToken = current_session.feed_token
        _global_client.setUserId(client_code)

    return _global_client


def _refresh_session() -> bool:
    """Attempt to refresh the session using the refresh token."""
    client = _get_client()
    current_session = get_session()
    
    if not current_session.refresh_token:
        logger.error("No refresh token available to refresh session.")
        return False
        
    logger.info("Attempting to refresh token...")
    try:
        response = client.generateToken(current_session.refresh_token)
        if response and response.get("status"):
            session_data = response.get("data", {})
            jwt_token = session_data.get("jwtToken")
            refresh_token = session_data.get("refreshToken")
            feed_token = session_data.get("feedToken", current_session.feed_token)
            
            if jwt_token and refresh_token:
                save_session(jwt_token, refresh_token, feed_token)
                client.setAccessToken(jwt_token)
                client.setRefreshToken(refresh_token)
                logger.info("Token refreshed successfully.")
                return True
        logger.error(f"Failed to refresh token: {response}")
        return False
    except Exception as e:
        logger.error(f"Exception during token refresh: {e}")
        return False


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

    logger.info("Calling SmartAPI generateSession...")
    try:
        data = client.generateSession(client_code, password, totp)
    except Exception as e:
        logger.error(f"SmartAPI login error: {e}")
        return {"success": False, "message": f"SmartAPI login error: {str(e)}"}

    if not data or not data.get("status"):
        error_msg = data.get("message", "Unknown error") if data else "No response from Angel One"
        logger.error(f"Login failed: {error_msg}")
        return {"success": False, "message": f"Login failed: {error_msg}"}

    logger.info("Login successful. Storing tokens.")
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
          - holdings (list of dicts)
    """
    current_session = get_session()
    if not current_session.is_active():
        logger.warning("No active session during get_holdings call.")
        return {
            "success": False,
            "message": "No active session. Please call /api/login first.",
            "holdings": [],
        }

    client = _get_client()

    try:
        logger.info("Fetching holdings from SmartAPI...")
        response = client.holding()
    except Exception as e:
        logger.error(f"Error fetching holdings: {e}")
        return {
            "success": False,
            "message": f"Error fetching holdings: {str(e)}",
            "holdings": [],
        }

    # Handle token expiry/Invalid Token
    if response and not response.get("status") and response.get("message", "") == "Invalid Token":
        logger.warning("Invalid Token detected. Attempting to refresh session...")
        if _refresh_session():
            logger.info("Token refreshed, retrying holding fetch...")
            try:
                response = client.holding()
            except Exception as e:
                logger.error(f"Error fetching holdings after refresh: {e}")
                return {
                    "success": False,
                    "message": f"Error fetching holdings after refresh: {str(e)}",
                    "holdings": [],
                }
        else:
            logger.error("Token refresh failed. Returning invalid token error.")

    if not response or not response.get("status"):
        error_msg = (
            response.get("message", "Unknown error") if response else "No response from Angel One"
        )
        logger.error(f"Failed to fetch holdings: {error_msg}")
        return {"success": False, "message": error_msg, "holdings": []}

    logger.info("Holdings fetched successfully.")
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
