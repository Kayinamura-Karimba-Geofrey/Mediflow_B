import jwt
import time
import requests

secret = '======================MediflowSecretKeyForJWTAuthentication======================'

payload = {
    'sub': 'admin93',
    'iat': int(time.time()),
    'exp': int(time.time()) + 86400
}

token = jwt.encode(payload, secret, algorithm='HS256')

res = requests.get('http://localhost:8080/api/patients', headers={'Authorization': 'Bearer ' + token})
print(res.status_code)
print(res.text)
