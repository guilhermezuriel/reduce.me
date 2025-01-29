![2](https://github.com/user-attachments/assets/b0e871b7-93be-428e-a6ce-74a18477ef84)
## Reduce.me 🔗 - URL shortener service

- System Design based on: (https://medium.com/double-pointer/system-design-interview-url-shortener-c45819b252cd)

------------------------------------------------------------------------------------------

### Techs:

<img src="https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white"></img>
<img src="https://img.shields.io/badge/cassandra-%231287B1.svg?style=for-the-badge&logo=apache-cassandra&logoColor=white"></img>
<img src="https://img.shields.io/badge/Thymeleaf-%23005C0F.svg?style=for-the-badge&logo=Thymeleaf&logoColor=white"></img>
<img src="https://img.shields.io/badge/tailwindcss-%2338B2AC.svg?style=for-the-badge&logo=tailwind-css&logoColor=white"></img>
<img src="https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white"></img>


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

