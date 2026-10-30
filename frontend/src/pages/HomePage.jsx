import useAuth from '../auth/useAuth'
import { NAV, ROLE_LABEL } from '../components/Layout/navConfig'
import QuickLinkGroup from '../components/home/QuickLinkGroup'
import WelcomeBanner from '../components/home/WelcomeBanner'

function HomePage() {
  const { me, hasRole } = useAuth()
  const groups = NAV.filter((entry) => hasRole(entry.role)).flatMap((entry) => entry.groups)
  const roleLabel = me.roles.map((role) => ROLE_LABEL[role] || role).join(', ')

  return (
    <>
      <WelcomeBanner fullName={me.fullName} username={me.username} roleLabel={roleLabel} />
      {groups.map((group) => (
        <QuickLinkGroup key={group.title} title={group.title} items={group.items} />
      ))}
    </>
  )
}

export default HomePage
