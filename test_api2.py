import json
import urllib.request
import subprocess
import urllib.error

# 1. Register admin97
req = urllib.request.Request('http://localhost:8080/api/auth/signup', 
    data=json.dumps({'username': 'admin97', 'email': 'admin97@example.com', 'password': 'password123', 'fullName': 'Admin', 'role': ['admin']}).encode('utf-8'),
    headers={'Content-Type': 'application/json'})
try:
    urllib.request.urlopen(req)
except Exception as e:
    pass

# 2. Get OTP
otp = subprocess.check_output(['psql', '-U', 'postgres', '-h', 'localhost', '-d', 'mediflow', '-t', '-c', "SELECT otp FROM users WHERE username='admin97';"], env={'PGPASSWORD':'123'}).decode('utf-8').strip()

# 3. Verify OTP
req = urllib.request.Request('http://localhost:8080/api/auth/verify-otp', 
    data=json.dumps({'username': 'admin97', 'otp': otp}).encode('utf-8'),
    headers={'Content-Type': 'application/json'})
try:
    urllib.request.urlopen(req)
except Exception as e:
    pass

# 4. Login
req = urllib.request.Request('http://localhost:8080/api/auth/signin', 
    data=json.dumps({'username': 'admin97', 'password': 'password123'}).encode('utf-8'),
    headers={'Content-Type': 'application/json'})
try:
    resp = urllib.request.urlopen(req)
    token = json.loads(resp.read())['token']
except Exception as e:
    print('Login failed', e)
    exit(1)

# 5. Get Patients
req = urllib.request.Request('http://localhost:8080/api/patients', headers={'Authorization': 'Bearer ' + token})
try:
    resp = urllib.request.urlopen(req)
    print('Patients:', resp.read())
except urllib.error.HTTPError as e:
    print('HTTP Error:', e.code)
    print('Response:', e.read().decode('utf-8'))
