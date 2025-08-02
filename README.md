![2](https://github.com/user-attachments/assets/b0e871b7-93be-428e-a6ce-74a18477ef84)
## Reduce.me 🔗 - URL shortener service

- System Design based on: (https://medium.com/double-pointer/system-design-interview-url-shortener-c45819b252cd)
- Deployed shortener: (www.reduceme.site)

------------------------------------------------------------------------------------------

### Techs:

<img src="https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white"></img>
<img src="https://img.shields.io/badge/cassandra-%231287B1.svg?style=for-the-badge&logo=apache-cassandra&logoColor=white"></img>
<img src="https://img.shields.io/badge/Thymeleaf-%23005C0F.svg?style=for-the-badge&logo=Thymeleaf&logoColor=white"></img>
<img src="https://img.shields.io/badge/tailwindcss-%2338B2AC.svg?style=for-the-badge&logo=tailwind-css&logoColor=white"></img>
<img src="https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white"></img>

### Code Highlights

#### Migrations — [View Code](https://github.com/guilhermezuriel/reduce.me/blob/master/src/main/java/com/guilhermezuriel/reduceme/application/config/infra/database/connection/local/RunCassandraMigrations.java)

The project uses a **versioned migration system** to manage the Cassandra schema for local environment setup.

- **Scripts location**: Stored in the `migrations` folder, following the naming convention `V{n}__description.cql`.  
- **Automatic execution**: The migration process runs automatically when the backend starts.  
- **Version control table**: A special table named `schema_version` is created in Cassandra (if it does not exist) to track applied scripts.  
- **Idempotency**: Only pending migrations are executed, preventing unintended recreation of tables or indexes.  
- **Consistency**: Ensures the database schema is always synchronized with the current application version.  
- **Inspiration**: Follows the principles of Flyway, adapted for Cassandra.  

#### Production Strategy

For production, a different approach was required to ensure scalability and proper connection handling:  

- **Spring Profiles**: A dedicated `prod` profile is used to separate local and production configurations.  
- **Dependency Injection**: A `@Bean` method provides the Cassandra connection using **Spring’s dependency injection** mechanism.  
- **Interface Abstraction**: An interface defines the contract for Cassandra connections, allowing multiple implementations.  
- **CqlSession in Production**: The production implementation uses `CqlSession` for robust and efficient Cassandra connections.  

This separation ensures that **local migrations run automatically in development**, while **production relies on a clean, profile-based connection strategy**.

---

#### Cleanup Service — [View Code](https://github.com/guilhermezuriel/reduce.me/blob/master/src/main/java/com/guilhermezuriel/reduceme/application/services/cleanup/CleanupService.java)

The project includes a **scheduled cleanup service** to automatically remove expired shortened URLs.  

- **Startup execution**: The service starts automatically when the application launches (`CommandLineRunner`).  
- **Scheduled task**: Uses `ScheduledExecutorService` to check for expired keys every **10 minutes**.  
- **Repository integration**: Expired keys are retrieved via `KeyRepository.findAllWithMoreThanExpirationDate()` and deleted in batch.  
- **Error handling**: Logs any runtime errors without interrupting the cleanup cycle.  
- **Graceful shutdown**: Implements a shutdown hook to cleanly terminate the executor when the application stops.  

This mechanism ensures that the system remains **optimized and free of expired records** over time.

---

#### Sessions & Cookies for Private Shortened URLs — [View Code](https://github.com/guilhermezuriel/reduce.me/blob/master/src/main/java/com/guilhermezuriel/reduceme/application/services/sessions/SessionService.java)

The application supports **private shortened URLs** by associating them with user sessions stored in cookies.  

- **Session-based tracking**: A unique session ID is generated for each browser session.  
- **Cookie persistence**: The session ID is stored in a secure cookie, enabling the server to identify the user’s private shortened URLs across requests.  
- **Data isolation**: Each session has access only to its own shortened URLs, ensuring privacy.  
- **Automatic handling**: Spring manages session creation, expiration, and retrieval transparently.  
- **Security considerations**: Cookies are configured with appropriate flags (`HttpOnly`, `Secure`) to reduce exposure to XSS or interception.  

This approach allows users to manage their own private shortened URLs **without requiring authentication or accounts**, while keeping data scoped to their session.

---

#### Deployment — [View Code](https://github.com/guilhermezuriel/reduce.me/blob/master/.github/workflows/deploy.yml)

The deployment pipeline is fully automated using **AWS EC2** and **GitHub Actions**.  

- **Infrastructure**: The application is hosted on an AWS EC2 instance, providing a scalable and reliable environment.  
- **CI/CD automation**: A GitHub Actions workflow triggers on pushes to the `master` branch.  
- **Dockerized deployment**: The application is built as a Docker image and deployed to the EC2 instance.  
- **Zero-downtime strategy**: The deployment process ensures that the application remains available while new versions are released.  
- **Security & secrets management**: AWS credentials and other sensitive information are stored as encrypted GitHub Secrets.  

This setup ensures a **fast, consistent, and secure delivery process** from code commit to production.

## Running

```sh
docker compose up -d
```
## Acessing by browser
 - You can test the API by browser, the page was made with Thymeleaf and Tailwind
   `http://localhost:8080/` 
  
## Routes

#### Creating a new shortened url
<details>
 <summary><code>POST</code> <code><b>/</b></code> <code>key/create</code></summary>


##### Parameters

> | name      |  type     | data type               | description                                                           |
> |-----------|-----------|-------------------------|-----------------------------------------------------------------------|
> | form      |  required | object (JSON)           | Form containing only the field "url"                                  |


##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `201`         | `text/plain;charset=UTF-8`        | `Configuration created successfully`                                |
> | `400`         | `application/json`                | `{"code":"400","message":"Bad Request"}`                            |

##### Example cURL

> ```javascript
>  curl -X POST -H "Content-Type: application/json" --data @post.json http://localhost:8080/key/create
> ```

</details>

------------------------------------------------------------------------------------------

#### Acessign the original url

<details>
 <summary><code>GET</code> <code><b>/</b></code> <code>{keyHash}</code></summary>

##### Parameters

> | name      |  type     | data type               | description                                                           |
> |-----------|-----------|-------------------------|-----------------------------------------------------------------------|
> | keyHash   |  required | String                  | The reduced url string (keyHash)                                      |

##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `text/html; charset=UTF-8`        | `<html></html>`                                                      |
> | `404`         | `application/json`                | `{"code":"404","message":"Not Found"}`                              |

##### Example cURL

> ```javascript
>  curl -X GET http://localhost:8080/{{keyHash}}
> ```

</details>

