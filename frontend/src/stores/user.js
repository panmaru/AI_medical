import { defineStore } from 'pinia'
import { ref, nextTick } from 'vue'
import { login as loginApi, getUserInfo, getMenu, getUserPermissions } from '@/api/auth'
import router from '@/router'
import { addDynamicRoutes } from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  const menus = ref([])
  const permissions = ref([])
  const menusLoaded = ref(false)

  // 登录
  const login = async (loginForm) => {
    const res = await loginApi(loginForm)
    token.value = res.data
    localStorage.setItem('token', res.data)
  }

  // 获取用户信息
  const getInfo = async () => {
    const res = await getUserInfo()
    userInfo.value = res.data
    localStorage.setItem('userInfo', JSON.stringify(res.data))
  }

  // 获取用户菜单和权限
  const fetchMenus = async () => {
    // 同时获取菜单和权限
    const [menuRes, permRes] = await Promise.all([
      getMenu(),
      getUserPermissions()
    ])

    menus.value = menuRes.data || []
    permissions.value = permRes.data || []

    localStorage.setItem('menus', JSON.stringify(menus.value))
    localStorage.setItem('permissions', JSON.stringify(permissions.value))

    // 添加动态路由
    addDynamicRoutes(menus.value)

    // 等待 Vue Router 完全注册路由
    await nextTick()

    // 验证路由是否真的添加成功
    const routes = router.getRoutes()
    const layoutRoute = routes.find(r => r.name === 'Layout')
    console.log('Layout 路由检查:', layoutRoute ? {
      name: layoutRoute.name,
      path: layoutRoute.path,
      childrenCount: layoutRoute.children?.length || 0
    } : '未找到')

    console.log('用户权限:', permissions.value)

    // 路由添加完成后再标记为已加载
    menusLoaded.value = true
  }

  // 设置菜单
  const setMenus = (menuList) => {
    menus.value = menuList
    menusLoaded.value = true
    localStorage.setItem('menus', JSON.stringify(menus.value))
  }

  // 退出登录
  const logout = () => {
    token.value = ''
    userInfo.value = {}
    menus.value = []
    permissions.value = []
    menusLoaded.value = false
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('menus')
    localStorage.removeItem('permissions')
  }

  return {
    token,
    userInfo,
    menus,
    permissions,
    menusLoaded,
    login,
    getInfo,
    fetchMenus,
    setMenus,
    logout
  }
})
