# What is PreSigned Url?
- allow PUT/POST (update) and GET (view) file
- directly from cloud storage within limited time period
- skip middleman (server side) from normal flow :: [client side -> server side -> cloud storage]

# Why use PreSigned Url?
1. when skip middleman (server side), then is Fast
- in order to be fast, we access directly to cloud storage
- SAFETY NOTE (access to cloud but can't put the master cloud login credential in client side is dangerous)
    - hence, use this presigned url, which with a active guess token expired after a duration

2. With Expired Time
- consist of token active in limited time period 
- SAFETY NOTE (who with link will have access)
    - so the access control need to be done before link generated
    - allow the link for single use only [AWS S3 not supported]
    - access granted restricted by the requested user home IP address [AWS S3 not supported]

# Normal Flow before presigned url
1. Server side Receives the file from client side: 
- The server accepts the uploaded file into its own temporary memory (RAM).

2. Processes/Validates the file: 
- It checks if the file is safe, checks the file size, or checks user permissions.

3. Re-uploads the file: 
- The server opens a new connection to the Cloud Storage (like AWS S3) 
- and sends the file over to its final home.

- But with presigned url we skip the server side directly