![2](https://github.com/user-attachments/assets/b0e871b7-93be-428e-a6ce-74a18477ef84)
## Reduce.me 🔗 - URL shortener service

- System Design based on: (https://medium.com/double-pointer/system-design-interview-url-shortener-c45819b252cd)

------------------------------------------------------------------------------------------

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
> | `200`         | `text/html; charset=UTF-8`        | <html></html>                                                       |

##### Example cURL

> ```javascript
>  curl -X GET http://localhost:8080/{{keyHash}}
> ```

</details>

