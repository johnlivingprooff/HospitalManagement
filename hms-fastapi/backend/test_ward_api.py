#!/usr/bin/env python3
"""
Test script for Ward API endpoints
"""

import requests
import json
from datetime import datetime

BASE_URL = "http://localhost:8001"

def test_ward_api():
    """Test the Ward API endpoints"""
    
    # First, login to get a token (assuming admin user exists)
    login_data = {
        "email": "admin@hospital.com",
        "password": "admin123"
    }
    
    try:
        response = requests.post(f"{BASE_URL}/api/auth/login", json=login_data)
        if response.status_code != 200:
            print("Failed to login. Make sure admin user exists.")
            return
        
        token = response.json()["access_token"]
        headers = {"Authorization": f"Bearer {token}"}
        
        print("🔑 Successfully logged in!")
        
        # Test 1: Get all wards
        print("\n📋 Testing GET /api/wards")
        response = requests.get(f"{BASE_URL}/api/wards", headers=headers)
        print(f"Status: {response.status_code}")
        if response.status_code == 200:
            wards = response.json()
            print(f"Found {len(wards)} wards")
            for ward in wards[:3]:  # Show first 3 wards
                print(f"  - {ward['name']} ({ward['type']}) - {ward['current_occupancy']}/{ward['capacity']} occupied")
        
        # Test 2: Create a new ward
        print("\n➕ Testing POST /api/wards")
        new_ward = {
            "name": "Test Ward API",
            "type": "general",
            "capacity": 25,
            "floor": 4,
            "description": "Test ward created via API"
        }
        
        response = requests.post(f"{BASE_URL}/api/wards", json=new_ward, headers=headers)
        print(f"Status: {response.status_code}")
        if response.status_code == 200:
            created_ward = response.json()
            ward_id = created_ward["id"]
            print(f"Created ward: {created_ward['name']} (ID: {ward_id})")
            
            # Test 3: Get specific ward
            print(f"\n🔍 Testing GET /api/wards/{ward_id}")
            response = requests.get(f"{BASE_URL}/api/wards/{ward_id}", headers=headers)
            print(f"Status: {response.status_code}")
            if response.status_code == 200:
                ward = response.json()
                print(f"Ward details: {ward['name']} - Floor {ward['floor']}")
            
            # Test 4: Update ward
            print(f"\n✏️ Testing PUT /api/wards/{ward_id}")
            update_data = {
                "description": "Updated test ward description",
                "capacity": 30
            }
            response = requests.put(f"{BASE_URL}/api/wards/{ward_id}", json=update_data, headers=headers)
            print(f"Status: {response.status_code}")
            if response.status_code == 200:
                updated_ward = response.json()
                print(f"Updated ward capacity: {updated_ward['capacity']}")
            
            # Test 5: Get patients (should be empty for new ward)
            print(f"\n👥 Testing ward patients for ward {ward_id}")
            response = requests.get(f"{BASE_URL}/api/wards/{ward_id}", headers=headers)
            if response.status_code == 200:
                ward = response.json()
                print(f"Current patients in ward: {len(ward.get('patients', []))}")
            
            # Test 6: Delete ward
            print(f"\n🗑️ Testing DELETE /api/wards/{ward_id}")
            response = requests.delete(f"{BASE_URL}/api/wards/{ward_id}", headers=headers)
            print(f"Status: {response.status_code}")
            if response.status_code == 200:
                print("Ward deleted successfully")
        
        # Test 7: Test search and filters
        print("\n🔍 Testing ward search and filters")
        response = requests.get(f"{BASE_URL}/api/wards?search=general", headers=headers)
        print(f"Search 'general' - Status: {response.status_code}")
        if response.status_code == 200:
            wards = response.json()
            print(f"Found {len(wards)} wards matching 'general'")
        
        response = requests.get(f"{BASE_URL}/api/wards?ward_type=icu", headers=headers)
        print(f"Filter by type 'icu' - Status: {response.status_code}")
        if response.status_code == 200:
            wards = response.json()
            print(f"Found {len(wards)} ICU wards")
        
        print("\n✅ All tests completed!")
        
    except requests.exceptions.ConnectionError:
        print("❌ Could not connect to the API. Make sure the backend server is running on port 8000.")
    except Exception as e:
        print(f"❌ Error during testing: {e}")

if __name__ == "__main__":
    print("🏥 Testing HMS Ward API endpoints")
    print("=" * 50)
    test_ward_api()
