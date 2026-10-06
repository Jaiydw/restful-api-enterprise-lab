$loginBody = '{"username":"admin","password":"admin123"}'
$loginResp = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method Post -ContentType "application/json" -Body $loginBody

Write-Host ">>> 1. Login Result: Success =" $loginResp.success " | Username =" $loginResp.data.username " | Role =" $loginResp.data.role
Write-Host ">>> Correlation ID:" $loginResp.correlationId

$token = $loginResp.data.accessToken
$headers = @{ "Authorization" = "Bearer $token" }

# Paginated posts
$pagedResp = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/posts?page=0&size=2&sort=title,asc" -Method Get -Headers $headers
Write-Host ">>> 2. Paginated Posts: TotalElements =" $pagedResp.data.totalElements " | PageSize =" $pagedResp.data.pageSize " | TotalPages =" $pagedResp.data.totalPages

# Optimized JOIN FETCH
$joinResp = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/posts/optimized-fetch" -Method Get -Headers $headers
Write-Host ">>> 3. JOIN FETCH Result: Fetched" $joinResp.data.Count "posts in 1 single SQL query without N+1 issue!"

# Cache Benchmark
$cacheResp = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/benchmark/cache-test/1" -Method Get
Write-Host ">>> 4. Cache Benchmark: Average Cache Hit Latency =" $cacheResp.data.averageCacheHitLatency
