package org.wzm.service;

import org.wzm.domain.AuthRole;
import org.wzm.domain.AuthRoleElementOperation;
import org.wzm.domain.AuthRoleMenu;

import java.util.List;

public interface AuthRoleService {

    AuthRole getRoleByCode(String roleCode);

    List<AuthRoleMenu> listAuthRoleMenuByRoleId(String roleCode);

    List<AuthRoleElementOperation> listAuthRoleElementOperationByRoleId(String roleCode);

    AuthRole getRoleByUserId(Long userId);
}
