## Spring @Transactional with JDBC and Kotlin suspend functions on different threads.

### Start database
```bash
docker run --name springtx -d -p 5432:5432 -e "POSTGRES_DB=springtx" -e "POSTGRES_USER=springtx" -e "POSTGRES_PASSWORD=springtx" postgres:16
```
### Start application
```bash
gradle bootrun -Dkotlinx.coroutines.debug
```