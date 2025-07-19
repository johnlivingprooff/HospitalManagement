import { ReactNode } from 'react'
import { X } from 'lucide-react'

interface ModalProps {
  isOpen: boolean
  onClose: () => void
  title: string
  children: ReactNode
  size?: string
}

const Modal = ({ isOpen, onClose, title, children, size = 'max-w-md' }: ModalProps) => {
  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* Backdrop */}
      <div 
        className="fixed inset-0 bg-black bg-opacity-50"
        onClick={onClose}
      />
      
      {/* Modal */}
      <div className={`relative z-10 w-full ${size} p-6 mx-4 bg-white rounded-lg shadow-xl max-h-[90vh] overflow-y-auto`}>
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-lg font-semibold text-gray-900">{title}</h3>
          <button
            onClick={onClose}
            className="p-1 text-gray-400 hover:text-gray-600"
          >
            <X className="w-5 h-5" />
          </button>
        </div>
        
        {children}
      </div>
    </div>
  )
}

export default Modal
