"""
main.py — FastAPI application for Stock Portfolio Tracker.

Exposes three endpoints:
  POST /api/login     → Trigger Angel One login using env vars + TOTP
  GET  /api/status    → Check if the current session token is active
  GET  /api/portfolio → Fetch all equity holdings with live data
"""

import os

from dotenv import load_dotenv
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from angel import login, get_holdings
from auth import get_session

# Load environment variables from .env file (if present)
load_dotenv()

app = FastAPI(
    title="Stock Portfolio Tracker API",
    description="Secure proxy between Android app and Angel One SmartAPI",
    version="1.0.0",
)

# Allow requests from the Android app (and any dev tools)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/")
async def root():
    """Health check endpoint."""
    return {"status": "ok", "service": "Stock Portfolio Tracker API"}


@app.post("/api/login")
async def api_login():
    """
    Trigger Angel One authentication.

    Reads ANGEL_CLIENT_CODE, ANGEL_PASSWORD, ANGEL_API_KEY, and ANGEL_TOTP_SECRET
    from environment variables, generates a TOTP, and calls SmartAPI's generateSession.
    Stores the session tokens in memory on success.

    Returns:
        200: {"success": true, "message": "Login successful"}
        401: {"success": false, "message": "<error details>"}
        500: {"success": false, "message": "<error details>"}
    """
    try:
        result = login()
    except ValueError as e:
        return JSONResponse(
            status_code=500,
            content={"success": False, "message": f"Configuration error: {str(e)}"},
        )
    except Exception as e:
        return JSONResponse(
            status_code=500,
            content={"success": False, "message": f"Unexpected error: {str(e)}"},
        )

    if result["success"]:
        return result
    else:
        return JSONResponse(status_code=401, content=result)


@app.get("/api/status")
async def api_status():
    """
    Check the current session status.

    Returns:
        200: {"logged_in": true/false, "message": "..."}
    """
    session = get_session()
    if session.is_active():
        return {"logged_in": True, "message": "Session is active"}
    elif session.jwt_token:
        return {"logged_in": False, "message": "Session has expired. Please login again."}
    else:
        return {"logged_in": False, "message": "Not logged in. Please call /api/login."}


@app.get("/api/portfolio")
async def api_portfolio():
    """
    Fetch all equity holdings from Angel One.

    Requires an active session (call /api/login first).

    Returns:
        200: {"success": true, "holdings": [...], "message": "..."}
        401: {"success": false, "holdings": [], "message": "..."}
        500: {"success": false, "holdings": [], "message": "..."}
    """
    try:
        result = get_holdings()
    except Exception as e:
        return JSONResponse(
            status_code=500,
            content={"success": False, "message": f"Unexpected error: {str(e)}", "holdings": []},
        )

    if result["success"]:
        return result
    else:
        # Distinguish between auth errors and API errors
        status_code = 401 if "session" in result["message"].lower() else 500
        return JSONResponse(status_code=status_code, content=result)
