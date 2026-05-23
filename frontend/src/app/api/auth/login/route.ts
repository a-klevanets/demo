import { NextResponse } from "next/server";
import {
  generateCodeVerifier,
  generateCodeChallenge,
  generateState,
} from "@/lib/pkce";

const COOKIE_OPTIONS = {
  httpOnly: true,
  secure: false,
  sameSite: "lax" as const,
  path: "/",
};

export async function GET() {
  const codeVerifier = generateCodeVerifier();
  const codeChallenge = generateCodeChallenge(codeVerifier);
  const state = generateState();

  const authUrl = process.env.NEXT_PUBLIC_AUTH_URL || "http://localhost:8080";
  const redirectUri = `${process.env.NEXT_PUBLIC_APP_URL || "http://localhost:3000"}/api/auth/callback`;

  const params = new URLSearchParams({
    response_type: "code",
    client_id: "nextjs-client",
    scope: "openid profile users.read",
    redirect_uri: redirectUri,
    code_challenge: codeChallenge,
    code_challenge_method: "S256",
    state: state,
  });

  const response = NextResponse.redirect(`${authUrl}/oauth2/authorize?${params.toString()}`);

  response.cookies.set("code_verifier", codeVerifier, { ...COOKIE_OPTIONS, maxAge: 300 });
  response.cookies.set("oauth_state", state, { ...COOKIE_OPTIONS, maxAge: 300 });
  response.cookies.set("code_challenge", codeChallenge, { ...COOKIE_OPTIONS, maxAge: 300 });

  return response;
}
