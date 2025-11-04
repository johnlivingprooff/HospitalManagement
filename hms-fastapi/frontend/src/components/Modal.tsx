import { ReactNode } from 'react'
import { X } from 'lucide-react'

interface ModalProps {
  isOpen: boolean
  onClose: () => void
  title: string
  children: ReactNode
  size?: string
}

const Modal = ({ isOpen, onClose, title, children, size = 'max-w-2xl' }: ModalProps) => {
  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div 
        className="fixed inset-0 bg-black bg-opacity-50 transition-opacity"
        onClick={onClose}
      />
      
      {/* Modal */}
      <div className={`relative z-10 w-full ${size} bg-gradient-to-br from-white to-primary-50/30 rounded-2xl shadow-2xl max-h-[90vh] overflow-hidden border-2 border-primary-200`}>
        {/* Header */}
        <div className="flex items-center justify-between px-8 py-6 border-b-2 border-primary-200 bg-gradient-to-r from-primary-50 to-white">
          <h3 className="text-xl font-bold text-gray-900">{title}</h3>
          <button
            onClick={onClose}
            className="p-2 text-gray-400 transition-all duration-200 rounded-lg hover:text-gray-600 hover:bg-white/50"
          >
            <X className="w-6 h-6" />
          </button>
        </div>
        
        {/* Content */}
        <div className="px-8 py-6 overflow-y-auto max-h-[calc(90vh-120px)]">
          {children}
        </div>
      </div>
    </div>
  )
}

export default Modal
