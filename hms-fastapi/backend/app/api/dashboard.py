from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import func
from datetime import datetime, date, timedelta
from app.core.database import get_db
from app.models import Patient, Appointment, MedicalRecord, Bill, LabTest, Prescription, User
from app.schemas import BaseSchema
from app.api.auth import get_current_user_dependency
from typing import List, Dict, Any

router = APIRouter()

@router.get("/health")
def dashboard_health():
    """Simple health check for dashboard API"""
    return {"status": "healthy", "service": "dashboard"}

class DashboardStats(BaseSchema):
    total_patients: int
    total_active_patients: int
    todays_appointments: int
    total_appointments: int
    total_medical_records: int
    pending_lab_tests: int
    pending_prescriptions: int
    total_bills: int
    unpaid_bills: int
    revenue_this_month: int

class RecentAppointment(BaseSchema):
    id: int
    patient_name: str
    appointment_date: str
    appointment_type: str
    status: str

class DashboardData(BaseSchema):
    stats: DashboardStats
    recent_appointments: List[RecentAppointment]
    monthly_revenue: List[Dict[str, Any]]
    appointment_status_distribution: Dict[str, int]

@router.get("/", response_model=DashboardData)
@router.get("", response_model=DashboardData)  # Handle both /api/dashboard and /api/dashboard/
def get_dashboard_data(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Get dashboard analytics data"""
    
    # Calculate date ranges
    today = date.today()
    start_of_month = today.replace(day=1)
    start_of_year = today.replace(month=1, day=1)
    
    # Get basic counts
    total_patients = db.query(Patient).count()
    active_patients = db.query(Patient).filter(Patient.is_active == True).count()
    
    # Today's appointments
    todays_appointments = db.query(Appointment).filter(
        func.date(Appointment.appointment_date) == today
    ).count()
    
    total_appointments = db.query(Appointment).count()
    total_medical_records = db.query(MedicalRecord).count()
    
    # Lab tests and prescriptions
    pending_lab_tests = db.query(LabTest).filter(LabTest.status == "pending").count()
    pending_prescriptions = db.query(Prescription).filter(Prescription.status == "pending").count()
    
    # Bills and revenue
    total_bills = db.query(Bill).count()
    unpaid_bills = db.query(Bill).filter(Bill.status.in_(["pending", "overdue"])).count()
    
    # Revenue this month (in cents, convert to dollars for display)
    revenue_result = db.query(func.sum(Bill.paid_amount)).filter(
        Bill.created_at >= start_of_month
    ).scalar()
    revenue_this_month = revenue_result or 0
    
    # Recent appointments (last 10)
    recent_appointments_query = db.query(Appointment).join(Patient).filter(
        Appointment.appointment_date >= datetime.now() - timedelta(days=7)
    ).order_by(Appointment.appointment_date.desc()).limit(10)
    
    recent_appointments = []
    for apt in recent_appointments_query:
        recent_appointments.append(RecentAppointment(
            id=apt.id,
            patient_name=f"{apt.patient.first_name} {apt.patient.last_name}",
            appointment_date=apt.appointment_date.strftime("%Y-%m-%d %H:%M"),
            appointment_type=apt.appointment_type,
            status=apt.status
        ))
    
    # Monthly revenue for the last 6 months
    monthly_revenue = []
    for i in range(6):
        month_start = (today.replace(day=1) - timedelta(days=i*30)).replace(day=1)
        # Calculate next month correctly, handling December
        if month_start.month == 12:
            month_end = (month_start.replace(year=month_start.year + 1, month=1, day=1) - timedelta(days=1))
        else:
            month_end = (month_start.replace(month=month_start.month + 1, day=1) - timedelta(days=1))
        
        month_revenue = db.query(func.sum(Bill.paid_amount)).filter(
            Bill.created_at >= month_start,
            Bill.created_at <= month_end
        ).scalar() or 0
        
        monthly_revenue.append({
            "month": month_start.strftime("%b %Y"),
            "revenue": month_revenue / 100 if month_revenue else 0  # Convert cents to dollars, handle None
        })
    
    monthly_revenue.reverse()  # Show oldest to newest
    
    # Appointment status distribution
    status_distribution = {}
    statuses = db.query(Appointment.status, func.count(Appointment.id)).group_by(Appointment.status).all()
    for status, count in statuses:
        status_distribution[status] = count
    
    stats = DashboardStats(
        total_patients=total_patients,
        total_active_patients=active_patients,
        todays_appointments=todays_appointments,
        total_appointments=total_appointments,
        total_medical_records=total_medical_records,
        pending_lab_tests=pending_lab_tests,
        pending_prescriptions=pending_prescriptions,
        total_bills=total_bills,
        unpaid_bills=unpaid_bills,
        revenue_this_month=revenue_this_month // 100  # Convert to dollars
    )
    
    return DashboardData(
        stats=stats,
        recent_appointments=recent_appointments,
        monthly_revenue=monthly_revenue,
        appointment_status_distribution=status_distribution
    )
