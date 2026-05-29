import jwt
import time
import requests

secret = '======================MediflowSecretKeyForJWTAuthentication======================'

payload = {
    'sub': 'adminDept',
    'iat': int(time.time()),
    'exp': int(time.time()) + 86400
}

import base64
decoded_secret = base64.b64decode(secret)

token = jwt.encode(payload, decoded_secret, algorithm='HS256')

deptBody = {'name': 'Radiology', 'description': 'X-Ray and Scans'}
res = requests.post('http://localhost:8080/api/departments', json=deptBody, headers={'Authorization': 'Bearer ' + token})
print(res.status_code)
print(res.text)
