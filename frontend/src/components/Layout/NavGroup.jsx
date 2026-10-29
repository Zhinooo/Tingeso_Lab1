import List from '@mui/material/List'
import ListSubheader from '@mui/material/ListSubheader'
import NavItem from './NavItem'

function NavGroup({ title, items }) {
  return (
    <List
      dense
      subheader={
        <ListSubheader
          disableSticky
          sx={{
            bgcolor: 'transparent',
            color: 'sidebar.subheader',
            fontSize: 11,
            fontWeight: 700,
            textTransform: 'uppercase',
            letterSpacing: '0.08em',
            lineHeight: '32px',
            px: 1.5,
          }}
        >
          {title}
        </ListSubheader>
      }
    >
      {items.map((item) => (
        <NavItem key={item.to} to={item.to} label={item.label} icon={item.icon} />
      ))}
    </List>
  )
}

export default NavGroup