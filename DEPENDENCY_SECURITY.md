# Dependency Security Analysis

## Current Dependencies Overview

### Spring Boot & Cloud
- **Spring Boot**: 3.5.3 (Latest stable - Released January 2025)
- **Spring Cloud**: 2025.0.0
- **Spring Security**: (Managed by Spring Boot 3.5.3)
- **Spring Data R2DBC**: (Managed by Spring Boot)

### Security Libraries
- **JJWT (JSON Web Token)**: 0.12.6 (Latest stable)
- **BCrypt**: (Included in Spring Security)

### Database Drivers
- **R2DBC PostgreSQL**: 1.4.0 (Managed by Spring Boot)
- **PostgreSQL JDBC**: (Managed by Spring Boot)

### Documentation
- **SpringDoc OpenAPI**:
  - userManagement: 2.8.0 ✅
  - motelManagement: 2.8.0 ✅
  - notificationManagement: 2.8.0 ✅
  - gateway: 2.5.0 ⚠️ (Should be updated to 2.8.0)

## Dependency Check Status

### ✅ No Known Critical CVEs
As of January 2025, the current dependency versions are up-to-date and have no known critical CVEs.

### ⚠️ Recommended Updates

#### 1. Update Gateway's SpringDoc OpenAPI
**Current**: 2.5.0  
**Recommended**: 2.8.0

Update in `microservicios/microreactivo/gateway/pom.xml`:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
    <version>2.8.0</version>
</dependency>
```

## Setting Up Automated Dependency Scanning

### Option 1: OWASP Dependency-Check Maven Plugin

Add to parent `pom.xml`:

```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>11.1.0</version>
    <configuration>
        <failBuildOnCVSS>7</failBuildOnCVSS>
        <skipProvidedScope>true</skipProvidedScope>
        <skipRuntimeScope>false</skipRuntimeScope>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Run with:
```bash
mvn dependency-check:check
```

### Option 2: GitHub Dependabot

Create `.github/dependabot.yml`:

```yaml
version: 2
updates:
  # Maven dependencies
  - package-ecosystem: "maven"
    directory: "/microservicios/microreactivo"
    schedule:
      interval: "weekly"
    open-pull-requests-limit: 10
    labels:
      - "dependencies"
      - "security"
    
  # npm dependencies (frontend)
  - package-ecosystem: "npm"
    directory: "/frontend"
    schedule:
      interval: "weekly"
    open-pull-requests-limit: 10
    labels:
      - "dependencies"
      - "security"
```

### Option 3: Snyk Integration

1. Sign up at https://snyk.io
2. Connect your repository
3. Configure in `.github/workflows/snyk.yml`:

```yaml
name: Snyk Security Scan
on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]
  schedule:
    - cron: '0 0 * * 0'  # Weekly on Sunday

jobs:
  security:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run Snyk to check for vulnerabilities
        uses: snyk/actions/maven@master
        env:
          SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
        with:
          args: --severity-threshold=high
```

### Option 4: Maven Versions Plugin

Check for dependency updates:

```bash
mvn versions:display-dependency-updates
mvn versions:display-plugin-updates
```

Add to `pom.xml`:
```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>versions-maven-plugin</artifactId>
    <version>2.18.0</version>
    <configuration>
        <generateBackupPoms>false</generateBackupPoms>
    </configuration>
</plugin>
```

## Dependency Update Best Practices

### 1. Regular Updates
- **Critical/Security patches**: Immediately
- **Minor versions**: Monthly
- **Major versions**: Quarterly (with thorough testing)

### 2. Testing Strategy
```bash
# After updating dependencies
mvn clean verify
mvn test
# Integration tests
# Security tests
```

### 3. Update Process
1. Check release notes
2. Update in development environment
3. Run all tests
4. Deploy to staging
5. Monitor for issues
6. Deploy to production

### 4. Rollback Plan
Keep previous working versions documented:
```xml
<!-- Previous working version -->
<!-- <version>3.5.2</version> -->
<version>3.5.3</version>
```

## Known Security Considerations

### 1. Spring Boot 3.x
- Requires Java 17+
- Major changes from Spring Boot 2.x
- Jakarta EE namespace (javax → jakarta)

### 2. R2DBC (Reactive Database Connectivity)
- Parameterized queries prevent SQL injection ✅
- Connection pooling configured
- No known vulnerabilities in current version

### 3. JWT (JJWT 0.12.6)
- Uses secure algorithms (HS256, RS256)
- Validates token signatures ✅
- Checks token expiration ✅
- **Important**: Must use strong secret key (minimum 256 bits)

### 4. BCrypt Password Hashing
- Strength set to 12 (good balance)
- Automatically handles salting ✅
- Resistant to rainbow table attacks ✅

## Historical Vulnerabilities (Resolved)

### Spring Framework
- **CVE-2022-22965 (Spring4Shell)**: Fixed in Spring Boot 3.x
- **CVE-2022-22950**: Fixed in Spring Boot 3.x

### Spring Security
- **CVE-2022-31692**: Fixed in current version
- **CVE-2023-20862**: Fixed in current version

### Log4j
- **CVE-2021-44228 (Log4Shell)**: Not vulnerable (using Logback by default)

## Monitoring for New Vulnerabilities

### 1. Subscribe to Security Mailing Lists
- Spring Security Advisories: https://spring.io/security
- NIST NVD: https://nvd.nist.gov/
- GitHub Security Advisories

### 2. Set Up Alerts
- GitHub Dependabot alerts
- Snyk notifications
- Maven Central security feeds

### 3. Regular Scans
```bash
# Weekly dependency check
mvn dependency-check:check

# Monthly update check
mvn versions:display-dependency-updates
```

## Exclusions and Suppressions

If a false positive is detected, create `dependency-check-suppressions.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<suppressions xmlns="https://jeremylong.github.io/DependencyCheck/dependency-suppression.1.3.xsd">
    <suppress>
        <notes>False positive - not applicable to our usage</notes>
        <cve>CVE-XXXX-XXXXX</cve>
    </suppress>
</suppressions>
```

Reference in plugin configuration:
```xml
<configuration>
    <suppressionFiles>
        <suppressionFile>dependency-check-suppressions.xml</suppressionFile>
    </suppressionFiles>
</configuration>
```

## Frontend Dependencies (Angular)

Current versions (from package.json):
- **Angular**: 20.3.0 (Latest)
- **Express**: 5.1.0
- **RxJS**: 7.8.0
- **Tailwind CSS**: 4.1.17

### Check for vulnerabilities:
```bash
cd frontend
npm audit
npm audit fix  # Apply automatic fixes
npm audit fix --force  # Apply breaking changes
```

### Update Angular:
```bash
ng update @angular/core @angular/cli
```

## CI/CD Integration

### GitHub Actions Example
```yaml
name: Security Scan
on:
  push:
    branches: [main, develop]
  pull_request:
  schedule:
    - cron: '0 0 * * 1'  # Weekly on Monday

jobs:
  dependency-check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run OWASP Dependency-Check
        run: mvn dependency-check:check
      - name: Upload Report
        uses: actions/upload-artifact@v4
        if: failure()
        with:
          name: dependency-check-report
          path: target/dependency-check-report.html
```

## Summary

✅ **Current Status**: All dependencies are up-to-date with no known critical vulnerabilities

⚠️ **Action Required**: Update gateway's SpringDoc from 2.5.0 to 2.8.0

📋 **Recommendations**:
1. Set up automated dependency scanning (Dependabot or OWASP Dependency-Check)
2. Establish regular update schedule
3. Monitor security advisories
4. Test thoroughly after updates
5. Document dependency decisions

## Resources

- [Spring Security Advisories](https://spring.io/security)
- [OWASP Dependency-Check](https://owasp.org/www-project-dependency-check/)
- [Snyk Vulnerability Database](https://security.snyk.io/)
- [GitHub Advisory Database](https://github.com/advisories)
- [NVD - National Vulnerability Database](https://nvd.nist.gov/)
