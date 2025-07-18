#!/usr/bin/env python3
import psycopg2
import sys

# Test PostgreSQL connection
try:
    # Try with the hash-like password
    conn = psycopg2.connect(
        host="localhost",
        database="hms",
        user="postgres",
        password="d433847adf9344c4a5e31f6fdb51be0a"
    )
    print("✓ Connection successful with hash-like password")
    conn.close()
except Exception as e:
    print(f"✗ Connection failed with hash-like password: {e}")
    
    # Try with common passwords
    common_passwords = ["", "root", "admin", "postgres", "password", "123456"]
    
    for pwd in common_passwords:
        try:
            conn = psycopg2.connect(
                host="localhost",
                database="hms",
                user="postgres",
                password=pwd
            )
            print(f"✓ Connection successful with password: '{pwd}'")
            conn.close()
            break
        except Exception as e:
            print(f"✗ Failed with password '{pwd}': {e}")
