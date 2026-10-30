export const ROLE_LABEL = { ADMIN: 'Administrador', TEACHER: 'Docente', STUDENT: 'Estudiante' }

export const formatRoles = (roles) => roles.map((role) => ROLE_LABEL[role] || role).join(', ')
