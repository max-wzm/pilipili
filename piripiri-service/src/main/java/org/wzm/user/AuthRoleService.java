package org.wzm.user;

import org.wzm.domain.AuthRole;

public interface AuthRoleService {

    AuthRole getRoleByCode(String roleCode);
}
