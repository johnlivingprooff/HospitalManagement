import React from 'react'

// Simple class name utility function
function cn(...classes: (string | undefined | null | false)[]): string {
  return classes.filter(Boolean).join(' ')
}

interface SkeletonProps extends React.HTMLAttributes<HTMLDivElement> {
  className?: string
  variant?: 'default' | 'card' | 'circle' | 'text' | 'button' | 'input' | 'avatar' | 'badge'
  animation?: 'pulse' | 'wave' | 'shine' | 'fade' | 'none'
  width?: string | number
  height?: string | number
  dark?: boolean
  repeat?: number
  gap?: number
}

export function Skeleton({ 
  className, 
  variant = 'default',
  animation = 'pulse',
  width,
  height,
  dark = false,
  repeat = 1,
  gap = 4,
  style,
  ...props 
}: SkeletonProps) {
  const variants = {
    default: "rounded-md",
    card: "rounded-lg shadow-sm border border-gray-200/50",
    circle: "rounded-full",
    text: "rounded",
    button: "rounded-md h-10",
    input: "rounded-md h-9 border border-gray-200/50",
    avatar: "rounded-full border-2 border-gray-200/50",
    badge: "rounded-full h-6 w-max px-2"
  }

  const animations = {
    pulse: "animate-pulse",
    wave: "relative overflow-hidden animate-pulse",
    shine: "relative overflow-hidden animate-pulse",
    fade: "animate-pulse",
    none: ""
  }

  const baseStyles = "bg-gray-200/15 dark:bg-gray-700/15"
  
  if (repeat > 1) {
    return (
      <div className={cn("space-y-" + gap)}>
        {Array.from({ length: repeat }, (_, i) => (
          <div
            key={i}
            className={cn(
              baseStyles,
              variants[variant],
              animations[animation],
              "relative overflow-hidden",
              className
            )}
            style={{
              width: typeof width === 'number' ? `${width}px` : width,
              height: typeof height === 'number' ? `${height}px` : height,
              ...style
            }}
            {...props}
          />
        ))}
      </div>
    )
  }

  return (
    <div
      className={cn(
        baseStyles,
        variants[variant],
        animations[animation],
        "relative overflow-hidden",
        className
      )}
      style={{
        width: typeof width === 'number' ? `${width}px` : width,
        height: typeof height === 'number' ? `${height}px` : height,
        ...style
      }}
      {...props}
    />
  )
}
