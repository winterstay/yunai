export const getAppPreviewUrl = (app?: API.AppVO) => {
  if (!app?.id || !app?.codeGenType) {
    return ''
  }
  return `http://localhost:8123/api/static/${app.codeGenType}_${app.id}/`
}

export const formatRelativeTime = (time?: string) => {
  if (!time) {
    return '暂无时间'
  }
  const target = new Date(time).getTime()
  if (Number.isNaN(target)) {
    return time
  }
  const diff = Date.now() - target
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  if (diff < hour) {
    return `${Math.max(1, Math.floor(diff / minute))} 分钟前`
  }
  if (diff < day) {
    return `${Math.max(1, Math.floor(diff / hour))} 小时前`
  }
  if (diff < 7 * day) {
    return `${Math.max(1, Math.floor(diff / day))} 天前`
  }
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(new Date(target))
}

export const getAppTypeLabel = (codeGenType?: string) => {
  switch (codeGenType) {
    case 'html':
      return 'HTML'
    case 'multi_file':
      return '网站'
    default:
      return '应用'
  }
}

export const isAdmin = (user?: API.LoginUserVO) => user?.userRole === 'admin'
