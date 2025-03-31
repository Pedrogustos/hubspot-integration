HubSpot Integration (Meetime Test)
=====================================

## Technologies
### Backend
- **Java 21** – Language used
- **Spring Boot** – Backend framework
- **Spring Web** – RESTful services
- **Spring Security** – Basic redirect handling
- **RestTemplate** – HTTP client
- **Lombok** – Boilerplate reduction
- **Maven** – Dependency manager

### Authentication
- **OAuth 2.0** – HubSpot authentication (Authorization Code Flow)
- **Bearer Token** – Secure API access

### Other
- **Postman** – Manual API testing
- **Ngrok** – Tunnel local server for webhooks

---

## Project Logic Summary
1. **Authorization Flow**: Redirects the user to HubSpot’s auth page. Upon authorization, a code is returned to our callback endpoint.
2. **Token Exchange**: The code is exchanged for an access token.
3. **Contact Operations**: Authenticated requests are sent to HubSpot to create and fetch contacts using the access token.
4. **Webhook Listener**: A `/webhook/contact` endpoint listens for HubSpot contact events.

---

## Step-by-Step Setup Instructions

### 1. Create HubSpot Account and App
- Go to [HubSpot Developer Portal](https://developers.hubspot.com).
- Create an app: **Apps > Create App**
- Under **Auth** tab:
   - **Redirect URL**: `http://localhost:8080/oauth/callback`
   - **Scopes**: `crm.objects.contacts.read crm.objects.contacts.write oauth`
- Save and copy your **Client ID** and **Client Secret**.

### 2. Clone the Project
```bash
git clone https://github.com/your-username/hubspot-integration.git
cd hubspot-integration
```

### 3. Configure `application.yml`
Create the file `src/main/resources/application.yml`:
```yaml
hubspot:
  oauth:
    client:
      id: YOUR_CLIENT_ID
      secret: YOUR_CLIENT_SECRET
    redirect:
      uri: http://localhost:8080/oauth/callback
    scope: crm.objects.contacts.read crm.objects.contacts.write oauth
```

### 4. Run the Project
```bash
./mvnw spring-boot:run
```

### 5. Authenticate via Browser
- Open: [http://localhost:8080/oauth/authorize](http://localhost:8080/oauth/authorize)
- Authorize the app on HubSpot.
- You’ll be redirected to: `http://localhost:8080/oauth/callback?code=XYZ123...`

---

## Using Postman to Test Endpoints

### 1. Exchange Authorization Code for Token
- **Method**: `GET`
- **URL**: `http://localhost:8080/oauth/callback?code=PASTE_YOUR_CODE_HERE`
- **Body**: none
- **Returns**: JSON with `access_token`

### 2. Create Contact
- **Method**: `POST`
- **URL**: `http://localhost:8080/oauth/create`
- **Headers**:
   - `Authorization: Bearer YOUR_ACCESS_TOKEN`
   - `Content-Type: application/json`
- **Body**:
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com"
}
```

### 3. List Contacts
- **Method**: `GET`
- **URL**: `http://localhost:8080/oauth/contacts`
- **Headers**:
   - `Authorization: Bearer YOUR_ACCESS_TOKEN`

---

## Setting Up Ngrok for Webhook Testing

### 1. Create Ngrok Account
- Go to [https://ngrok.com](https://ngrok.com) and sign up.

### 2. Install and Authenticate Ngrok
```bash
git install ngrok
ngrok config add-authtoken YOUR_AUTHTOKEN
```

### 3. Start Ngrok Tunnel
```bash
ngrok http 8080
```
- Copy the HTTPS URL (e.g., `https://abc123.ngrok.io`)

### 4. Register Webhook in HubSpot
- Go to **App > Webhooks** in your HubSpot developer dashboard
- Add the URL: `https://abc123.ngrok.io/webhook/contact`
- Subscribe to **contact creation** and/or **update events**

---

## API Endpoints Summary
| Method | URL                           | Description                                   |
|--------|-------------------------------|-----------------------------------------------|
| GET    | `/oauth/authorize`           | Redirects to HubSpot auth page                |
| GET    | `/oauth/callback?code=...`   | Exchanges code for access token               |
| POST   | `/oauth/create`              | Creates a contact in HubSpot                  |
| GET    | `/oauth/contacts`            | Lists HubSpot contacts                        |
| POST   | `/webhook/contact`           | Endpoint for receiving HubSpot webhook events |

---

## Final Notes
- Credentials should **never** be committed. Use environment variables or secrets manager.
- Logs are printed to the console for easier debugging.
- This project is modular and can be extended with database storage or a frontend.

---

**Made with ❤️ by Pedro Augusto for the Meetime Challenge.**

