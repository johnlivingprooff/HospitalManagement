import { useState } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { Settings, User, Bell, Shield, Database, Palette } from 'lucide-react'

const SettingsPage = () => {
  const { user } = useAuth()
  const [activeTab, setActiveTab] = useState('profile')
  const [settings, setSettings] = useState({
    notifications: {
      email: true,
      sms: false,
      push: true,
      appointments: true,
      bills: true,
      reminders: true
    },
    profile: {
      firstName: user?.first_name || '',
      lastName: user?.last_name || '',
      email: user?.email || '',
      phone: '',
      department: '',
      specialization: ''
    },
    system: {
      language: 'english',
      timezone: 'UTC',
      dateFormat: 'MM/DD/YYYY',
      theme: 'light'
    }
  })

  const tabs = [
    { id: 'profile', name: 'Profile', icon: User },
    { id: 'notifications', name: 'Notifications', icon: Bell },
    { id: 'security', name: 'Security', icon: Shield },
    { id: 'system', name: 'System', icon: Database },
    { id: 'appearance', name: 'Appearance', icon: Palette }
  ]

  const handleProfileChange = (field: string, value: string) => {
    setSettings(prev => ({
      ...prev,
      profile: { ...prev.profile, [field]: value }
    }))
  }

  const handleNotificationChange = (field: string, value: boolean) => {
    setSettings(prev => ({
      ...prev,
      notifications: { ...prev.notifications, [field]: value }
    }))
  }

  const handleSystemChange = (field: string, value: string) => {
    setSettings(prev => ({
      ...prev,
      system: { ...prev.system, [field]: value }
    }))
  }

  const handleSave = () => {
    console.log('Settings saved:', settings)
    // Here you would typically make an API call to save settings
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Settings</h1>
        <button 
          onClick={handleSave} 
          className="bg-orange-600 hover:bg-orange-700 text-white font-semibold py-2.5 px-6 rounded-lg 
                   shadow-lg hover:shadow-xl transition-all duration-200 transform hover:scale-105 
                   focus:outline-none focus:ring-2 focus:ring-orange-500 focus:ring-offset-2
                   flex items-center space-x-2 group"
        >
          <svg 
            className="h-5 w-5 group-hover:rotate-12 transition-transform duration-200" 
            fill="none" 
            stroke="currentColor" 
            viewBox="0 0 24 24"
          >
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} 
                  d="M5 13l4 4L19 7" />
          </svg>
          <span>Save Changes</span>
        </button>
      </div>

      <div className="bg-white rounded-lg shadow">
        <div className="border-b border-gray-200">
          <nav className="flex px-6 space-x-8" aria-label="Tabs">
            {tabs.map((tab) => {
              const Icon = tab.icon
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`py-4 px-1 border-b-2 font-medium text-sm flex items-center space-x-2 ${
                    activeTab === tab.id
                      ? 'border-orange-500 text-orange-600'
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  <span>{tab.name}</span>
                </button>
              )
            })}
          </nav>
        </div>

        <div className="p-6">
          {/* Profile Tab */}
          {activeTab === 'profile' && (
            <div className="space-y-6">
              <h3 className="text-lg font-medium text-gray-900">Profile Information</h3>
              <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
                <div>
                  <label className="block text-sm font-medium text-gray-700">First Name</label>
                  <input
                    type="text"
                    className="mt-1 input"
                    value={settings.profile.firstName}
                    onChange={(e) => handleProfileChange('firstName', e.target.value)}
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Last Name</label>
                  <input
                    type="text"
                    className="mt-1 input"
                    value={settings.profile.lastName}
                    onChange={(e) => handleProfileChange('lastName', e.target.value)}
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Email</label>
                  <input
                    type="email"
                    className="mt-1 input"
                    value={settings.profile.email}
                    onChange={(e) => handleProfileChange('email', e.target.value)}
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Phone</label>
                  <input
                    type="tel"
                    className="mt-1 input"
                    value={settings.profile.phone}
                    onChange={(e) => handleProfileChange('phone', e.target.value)}
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Department</label>
                  <select
                    className="mt-1 input"
                    value={settings.profile.department}
                    onChange={(e) => handleProfileChange('department', e.target.value)}
                  >
                    <option value="">Select Department</option>
                    <option value="emergency">Emergency</option>
                    <option value="cardiology">Cardiology</option>
                    <option value="neurology">Neurology</option>
                    <option value="orthopedics">Orthopedics</option>
                    <option value="pediatrics">Pediatrics</option>
                    <option value="administration">Administration</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Specialization</label>
                  <input
                    type="text"
                    className="mt-1 input"
                    value={settings.profile.specialization}
                    onChange={(e) => handleProfileChange('specialization', e.target.value)}
                  />
                </div>
              </div>
            </div>
          )}

          {/* Notifications Tab */}
          {activeTab === 'notifications' && (
            <div className="space-y-6">
              <h3 className="text-lg font-medium text-gray-900">Notification Preferences</h3>
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <div>
                    <h4 className="text-sm font-medium text-gray-900">Email Notifications</h4>
                    <p className="text-sm text-gray-500">Receive notifications via email</p>
                  </div>
                  <input
                    type="checkbox"
                    className="w-4 h-4 text-orange-600"
                    checked={settings.notifications.email}
                    onChange={(e) => handleNotificationChange('email', e.target.checked)}
                  />
                </div>
                <div className="flex items-center justify-between">
                  <div>
                    <h4 className="text-sm font-medium text-gray-900">SMS Notifications</h4>
                    <p className="text-sm text-gray-500">Receive notifications via SMS</p>
                  </div>
                  <input
                    type="checkbox"
                    className="w-4 h-4 text-orange-600"
                    checked={settings.notifications.sms}
                    onChange={(e) => handleNotificationChange('sms', e.target.checked)}
                  />
                </div>
                <div className="flex items-center justify-between">
                  <div>
                    <h4 className="text-sm font-medium text-gray-900">Appointment Reminders</h4>
                    <p className="text-sm text-gray-500">Get reminded about upcoming appointments</p>
                  </div>
                  <input
                    type="checkbox"
                    className="w-4 h-4 text-orange-600"
                    checked={settings.notifications.appointments}
                    onChange={(e) => handleNotificationChange('appointments', e.target.checked)}
                  />
                </div>
                <div className="flex items-center justify-between">
                  <div>
                    <h4 className="text-sm font-medium text-gray-900">Bill Notifications</h4>
                    <p className="text-sm text-gray-500">Get notified about billing updates</p>
                  </div>
                  <input
                    type="checkbox"
                    className="w-4 h-4 text-orange-600"
                    checked={settings.notifications.bills}
                    onChange={(e) => handleNotificationChange('bills', e.target.checked)}
                  />
                </div>
              </div>
            </div>
          )}

          {/* Security Tab */}
          {activeTab === 'security' && (
            <div className="space-y-6">
              <h3 className="text-lg font-medium text-gray-900">Security Settings</h3>
              <div className="space-y-4">
                <div>
                  <h4 className="mb-2 text-sm font-medium text-gray-900">Change Password</h4>
                  <div className="space-y-3">
                    <input
                      type="password"
                      placeholder="Current Password"
                      className="input"
                    />
                    <input
                      type="password"
                      placeholder="New Password"
                      className="input"
                    />
                    <input
                      type="password"
                      placeholder="Confirm New Password"
                      className="input"
                    />
                    <button className="bg-orange-100 hover:bg-orange-200 text-orange-700 font-medium py-2 px-4 rounded-lg border border-orange-200 transition-colors duration-200">
                      Update Password
                    </button>
                  </div>
                </div>
                <div className="pt-4 border-t">
                  <h4 className="mb-2 text-sm font-medium text-gray-900">Two-Factor Authentication</h4>
                  <p className="mb-3 text-sm text-gray-500">
                    Add an extra layer of security to your account
                  </p>
                  <button className="bg-orange-100 hover:bg-orange-200 text-orange-700 font-medium py-2 px-4 rounded-lg border border-orange-200 transition-colors duration-200">
                    Enable 2FA
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* System Tab */}
          {activeTab === 'system' && (
            <div className="space-y-6">
              <h3 className="text-lg font-medium text-gray-900">System Preferences</h3>
              <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
                <div>
                  <label className="block text-sm font-medium text-gray-700">Language</label>
                  <select
                    className="mt-1 input"
                    value={settings.system.language}
                    onChange={(e) => handleSystemChange('language', e.target.value)}
                  >
                    <option value="english">English</option>
                    <option value="spanish">Spanish</option>
                    <option value="french">French</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Timezone</label>
                  <select
                    className="mt-1 input"
                    value={settings.system.timezone}
                    onChange={(e) => handleSystemChange('timezone', e.target.value)}
                  >
                    <option value="UTC">UTC</option>
                    <option value="EST">Eastern Time</option>
                    <option value="PST">Pacific Time</option>
                    <option value="CST">Central Time</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Date Format</label>
                  <select
                    className="mt-1 input"
                    value={settings.system.dateFormat}
                    onChange={(e) => handleSystemChange('dateFormat', e.target.value)}
                  >
                    <option value="MM/DD/YYYY">MM/DD/YYYY</option>
                    <option value="DD/MM/YYYY">DD/MM/YYYY</option>
                    <option value="YYYY-MM-DD">YYYY-MM-DD</option>
                  </select>
                </div>
              </div>
            </div>
          )}

          {/* Appearance Tab */}
          {activeTab === 'appearance' && (
            <div className="space-y-6">
              <h3 className="text-lg font-medium text-gray-900">Appearance</h3>
              <div>
                <label className="block mb-3 text-sm font-medium text-gray-700">Theme</label>
                <div className="space-y-2">
                  <label className="flex items-center">
                    <input
                      type="radio"
                      name="theme"
                      value="light"
                      checked={settings.system.theme === 'light'}
                      onChange={(e) => handleSystemChange('theme', e.target.value)}
                      className="w-4 h-4 text-orange-600"
                    />
                    <span className="ml-3 text-sm text-gray-700">Light</span>
                  </label>
                  <label className="flex items-center">
                    <input
                      type="radio"
                      name="theme"
                      value="dark"
                      checked={settings.system.theme === 'dark'}
                      onChange={(e) => handleSystemChange('theme', e.target.value)}
                      className="w-4 h-4 text-orange-600"
                    />
                    <span className="ml-3 text-sm text-gray-700">Dark</span>
                  </label>
                  <label className="flex items-center">
                    <input
                      type="radio"
                      name="theme"
                      value="auto"
                      checked={settings.system.theme === 'auto'}
                      onChange={(e) => handleSystemChange('theme', e.target.value)}
                      className="w-4 h-4 text-primary-600"
                    />
                    <span className="ml-3 text-sm text-gray-700">Auto (System)</span>
                  </label>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default SettingsPage
