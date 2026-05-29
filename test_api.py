import requests
import psycopg2

conn = psycopg2.connect(dbname='mediflow', user='postgres', password='123', host='localhost')
cur = conn.cursor()

# Signup
signup_data = {
    'username': 'admin98',
    'email': 'admin98@example.com',
    'password': 'password123',
    'fullName': 'Admin User',
    'role': ['admin']
}
res = requests.post('http://localhost:8080/api/auth/signup', json=signup_data)
print('Signup:', res.status_code, res.text)

# Get OTP
cur.execute("SELECT otp FROM users WHERE username='admin98'")
otp = cur.fetchone()[0]

# Verify
res = requests.post('http://localhost:8080/api/auth/verify-otp', json={'username': 'admin98', 'otp': otp})
print('Verify:', res.status_code, res.text)

# Login
res = requests.post('http://localhost:8080/api/auth/signin', json={'username': 'admin98', 'password': 'password123'})
print('Login:', res.status_code, res.text)
token = res.json().get('token')

# Get Patients
res = requests.get('http://localhost:8080/api/patients', headers={'Authorization': 'Bearer ' + token})
print('Patients:', res.status_code, res.text)
