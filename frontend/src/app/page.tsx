export default function HomePage() {

  return (
    <div className="flex items-center justify-center min-h-screen">
      <div className="bg-white p-8 rounded-2xl shadow-lg max-w-md w-full text-center">
        <div className="mb-6">
          <div className="w-16 h-16 bg-blue-600 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg
              className="w-8 h-8 text-white"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
              />
            </svg>
          </div>
          <h1 className="text-2xl font-bold text-gray-900">Welcome</h1>
          <p className="text-gray-500 mt-2">
            Sign in to access the user management dashboard
          </p>
        </div>

        <form action="/api/auth/login" method="GET" className="w-full">
          <button
            type="submit"
            className="w-full bg-blue-600 text-white font-semibold py-3 px-6 rounded-lg hover:bg-blue-700 transition-colors cursor-pointer"
          >
            Sign In with OAuth2
          </button>
        </form>

        <p className="text-xs text-gray-400 mt-4">
          PKCE flow &middot; Spring Authorization Server
        </p>
      </div>
    </div>
  );
}
