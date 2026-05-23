import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

const COOKIE_OPTIONS = {
  httpOnly: true,
  secure: false,
  sameSite: "lax" as const,
  path: "/",
};

export async function GET(request: NextRequest) {
  const { searchParams } = new URL(request.url);
  const code = searchParams.get("code");
  const state = searchParams.get("state");
  const error = searchParams.get("error");

  if (error) {
    return NextResponse.redirect(
      new URL(`/?error=${encodeURIComponent(error)}`, request.url)
    );
  }

  if (!code || !state) {
    return NextResponse.redirect(new URL("/?error=missing_params", request.url));
  }

  // Verify state from cookie
  const savedState = request.cookies.get("oauth_state")?.value;
  if (state !== savedState) {
    return NextResponse.redirect(new URL("/?error=invalid_state", request.url));
  }

  // Get code_verifier from cookie
  const codeVerifier = request.cookies.get("code_verifier")?.value;
  if (!codeVerifier) {
    return NextResponse.redirect(new URL("/?error=missing_verifier", request.url));
  }

  // Exchange code for tokens
  const authUrl = process.env.AUTH_INTERNAL_URL || process.env.NEXT_PUBLIC_AUTH_URL || "http://localhost:8080";
  const redirectUri = `${process.env.NEXT_PUBLIC_APP_URL || "http://localhost:3000"}/api/auth/callback`;

  const tokenResponse = await fetch(`${authUrl}/oauth2/token`, {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body: new URLSearchParams({
      grant_type: "authorization_code",
      code: code,
      redirect_uri: redirectUri,
      client_id: "nextjs-client",
      code_verifier: codeVerifier,
    }),
  });

  if (!tokenResponse.ok) {
    const errBody = await tokenResponse.text();
    console.error("Token exchange failed:", tokenResponse.status, errBody);
    return NextResponse.redirect(new URL("/?error=token_exchange_failed", request.url));
  }

  const tokens = await tokenResponse.json();

  // Build redirect response with cookies
  const response = NextResponse.redirect(new URL("/dashboard", request.url));

  response.cookies.set("access_token", tokens.access_token, { ...COOKIE_OPTIONS, maxAge: 3600 });
  response.cookies.delete("code_verifier");
  response.cookies.delete("oauth_state");
  response.cookies.delete("code_challenge");

  return response;
}
