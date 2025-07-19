# Ward Management API Documentation

This document describes the Ward Management API endpoints for the Hospital Management System.

## Overview

The Ward Management API provides comprehensive functionality for managing hospital wards and patient admissions, including:

- Ward CRUD operations
- Patient admission and discharge
- Ward occupancy tracking
- Bed management
- Patient status updates

## Models

### Ward
```json
{
  "id": 1,
  "name": "General Ward A",
  "type": "general",
  "capacity": 20,
  "current_occupancy": 15,
  "floor": 1,
  "description": "General patient ward with standard facilities",
  "is_active": true,
  "created_at": "2024-01-01T00:00:00Z",
  "updated_at": "2024-01-01T00:00:00Z",
  "patients": []
}
```

### WardPatient
```json
{
  "id": 1,
  "patient_id": 1,
  "ward_id": 1,
  "doctor_id": 1,
  "bed_number": "A01",
  "admission_date": "2024-01-01T00:00:00Z",
  "discharge_date": null,
  "status": "stable",
  "notes": "Patient recovering well",
  "created_at": "2024-01-01T00:00:00Z",
  "updated_at": "2024-01-01T00:00:00Z",
  "patient": {
    "id": 1,
    "first_name": "John",
    "last_name": "Doe",
    "email": "john.doe@email.com"
  },
  "doctor": {
    "id": 1,
    "first_name": "Dr. Smith",
    "last_name": "Johnson"
  }
}
```

## API Endpoints

### Ward Management

#### GET /api/wards
Get all wards with optional filters.

**Query Parameters:**
- `skip` (int): Number of records to skip (default: 0)
- `limit` (int): Maximum records to return (default: 100)
- `search` (str): Search in ward name, type, or description
- `ward_type` (str): Filter by ward type
- `is_active` (bool): Filter by active status
- `floor` (int): Filter by floor number

**Response:** List of Ward objects

#### GET /api/wards/{ward_id}
Get a specific ward by ID with current patients.

**Response:** Ward object with patients array

#### POST /api/wards
Create a new ward.

**Request Body:**
```json
{
  "name": "New Ward",
  "type": "general",
  "capacity": 20,
  "floor": 1,
  "description": "Ward description"
}
```

**Required Permissions:** Admin or Doctor

**Response:** Created Ward object

#### PUT /api/wards/{ward_id}
Update an existing ward.

**Request Body:** Partial Ward object (only fields to update)

**Required Permissions:** Admin or Doctor

**Response:** Updated Ward object

#### DELETE /api/wards/{ward_id}
Delete a ward (only if no current patients).

**Required Permissions:** Admin only

**Response:** Success message

### Patient Admission Management

#### POST /api/wards/{ward_id}/patients
Admit a patient to a ward.

**Request Body:**
```json
{
  "patient_id": 1,
  "doctor_id": 1,
  "bed_number": "A01",
  "status": "admitted",
  "notes": "Admission notes"
}
```

**Required Permissions:** Admin, Doctor, or Nurse

**Validation:**
- Ward must exist and be active
- Ward must have available capacity
- Patient must not be already admitted elsewhere
- Bed number must be available in the ward
- Doctor must exist (if provided)

**Response:** WardPatient object

#### PUT /api/wards/patients/{ward_patient_id}
Update ward patient information.

**Request Body:** Partial WardPatient object

**Required Permissions:** Admin, Doctor, or Nurse

**Response:** Updated WardPatient object

#### PUT /api/wards/patients/{ward_patient_id}/discharge
Discharge a patient from ward.

**Request Body:**
```json
{
  "discharge_date": "2024-01-01T00:00:00Z",
  "notes": "Discharge summary and instructions"
}
```

**Required Permissions:** Admin, Doctor, or Nurse

**Response:** Updated WardPatient object with discharge information

#### GET /api/wards/patients/{ward_patient_id}
Get specific ward patient record.

**Response:** WardPatient object

## Ward Types

The following ward types are supported:
- `general`: General patient ward
- `icu`: Intensive Care Unit
- `emergency`: Emergency treatment ward
- `surgery`: Post-operative and surgical care
- `maternity`: Maternity and obstetric care
- `pediatric`: Pediatric and children's care

## Patient Status

The following patient statuses are supported:
- `admitted`: Newly admitted patient
- `stable`: Patient in stable condition
- `critical`: Patient in critical condition
- `recovering`: Patient recovering
- `discharged`: Patient has been discharged

## Error Responses

### 400 Bad Request
- Ward name already exists
- Ward at full capacity
- Patient already admitted elsewhere
- Bed number already occupied
- Cannot delete ward with current patients

### 403 Forbidden
- Insufficient permissions for the operation

### 404 Not Found
- Ward not found
- Patient not found
- Doctor not found
- Ward patient record not found

## Business Rules

### Ward Management
1. Ward names must be unique
2. Ward capacity must be greater than 0
3. Floor numbers must be positive
4. Only admins can delete wards
5. Wards can only be deleted if they have no current patients

### Patient Admission
1. Patients can only be admitted to one ward at a time
2. Bed numbers must be unique within a ward
3. Ward occupancy is automatically updated on admission/discharge
4. Only active wards can accept new patients
5. Ward capacity cannot be exceeded

### Permissions
- **Admin**: Full access to all operations
- **Doctor**: Can manage wards and patients (except deletion)
- **Nurse**: Can manage patients, view wards
- **Receptionist**: Read-only access to ward information

## Usage Examples

### Bash/cURL

```bash
# Get all wards
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8000/api/wards

# Create a new ward
curl -X POST -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Ward","type":"general","capacity":20,"floor":1}' \
  http://localhost:8000/api/wards

# Admit a patient
curl -X POST -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"patient_id":1,"doctor_id":1,"bed_number":"A01","status":"admitted"}' \
  http://localhost:8000/api/wards/1/patients

# Discharge a patient
curl -X PUT -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"discharge_date":"2024-01-01T12:00:00Z","notes":"Discharged in good health"}' \
  http://localhost:8000/api/wards/patients/1/discharge
```

### Python/Requests

```python
import requests

headers = {"Authorization": f"Bearer {token}"}

# Get wards with search
response = requests.get(
    "http://localhost:8000/api/wards?search=general&limit=10",
    headers=headers
)
wards = response.json()

# Create ward
new_ward = {
    "name": "New ICU",
    "type": "icu",
    "capacity": 10,
    "floor": 2,
    "description": "New ICU ward"
}
response = requests.post(
    "http://localhost:8000/api/wards",
    json=new_ward,
    headers=headers
)
ward = response.json()

# Admit patient
admission = {
    "patient_id": 1,
    "doctor_id": 2,
    "bed_number": "ICU01",
    "status": "critical",
    "notes": "Emergency admission"
}
response = requests.post(
    f"http://localhost:8000/api/wards/{ward['id']}/patients",
    json=admission,
    headers=headers
)
```

## Testing

Run the provided test script to verify the API:

```bash
cd backend
python test_ward_api.py
```

## Database Setup

To add the ward tables to an existing database:

```bash
cd backend
python add_ward_tables.py
python seed_data.py  # Add sample data
```

To recreate all tables (WARNING: This will delete existing data):

```bash
cd backend
python recreate_tables.py
python seed_data.py
```
