package org.wzm.mapper;

import org.wzm.domain.AuthRoleMenu;

import java.util.List;

public interface AuthRoleMenuMapper {
    List<AuthRoleMenu> listByRoleCode(String roleCode);
}
