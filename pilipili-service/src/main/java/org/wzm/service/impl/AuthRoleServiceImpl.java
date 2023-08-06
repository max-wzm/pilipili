package org.wzm.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.wzm.domain.AuthRole;
import org.wzm.domain.AuthRoleElementOperation;
import org.wzm.domain.AuthRoleMenu;
import org.wzm.mapper.AuthRoleElementOperationMapper;
import org.wzm.mapper.AuthRoleMapper;
import org.wzm.mapper.AuthRoleMenuMapper;
import org.wzm.service.AuthRoleService;

import java.util.List;

public class AuthRoleServiceImpl implements AuthRoleService {
    @Autowired
    private AuthRoleMenuMapper             authRoleMenuMapper;
    @Autowired
    private AuthRoleElementOperationMapper authRoleElementOperationMapper;
    @Autowired
    private AuthRoleMapper                 authRoleMapper;

    @Override
    public AuthRole getRoleByCode(String roleCode) {
        return authRoleMapper.getByCode(roleCode);
    }

    @Override
    public List<AuthRoleMenu> listAuthRoleMenuByRoleId(String roleCode) {
        return authRoleMenuMapper.listByRoleCode(roleCode);
    }

    @Override
    public List<AuthRoleElementOperation> listAuthRoleElementOperationByRoleId(String roleCode) {
        return authRoleElementOperationMapper.listByRoleCode(roleCode);
    }

    @Override
    public AuthRole getRoleByUserId(Long userId) {
        return null;
    }
}
