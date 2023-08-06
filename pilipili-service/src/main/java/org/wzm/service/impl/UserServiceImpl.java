package org.wzm.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.wzm.constant.AuthRoleConstant;
import org.wzm.constant.RedisConstant;
import org.wzm.constant.UserConstant;
import org.wzm.domain.*;
import org.wzm.mapper.UserMapper;
import org.wzm.service.*;
import org.wzm.user.*;
import org.wzm.utils.DateUtil;
import org.wzm.utils.MD5Util;
import org.wzm.utils.RSAUtil;
import org.wzm.utils.TokenUtil;

import java.util.List;
import java.util.Objects;

/**
 * @author wangzhiming
 */
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper                    userMapper;
    @Autowired
    private UserInfoService               userInfoService;
    @Autowired
    private UserCoinService               userCoinService;
    @Autowired
    private RefreshTokenService           refreshTokenService;
    @Autowired
    private AuthRoleService               authRoleService;
    @Autowired
    private RedisTemplate<String, String> stringRedisTemplate;

    @Override
    public User getUser(Long userId) {
        User user = userMapper.getUserById(userId);
        UserInfo userInfo = userInfoService.getUserInfoByUserId(userId);
        user.setUserInfo(userInfo);
        return user;
    }

    @Override
    public Token login(User user) {
        String phone = user.getPhone();
        if (StringUtils.isBlank(phone)) {
            throw new BizException("Invalid phone!");
        }
        User dbUser = userMapper.getUserByPhone(phone);
        if (Objects.isNull(dbUser)) {
            throw new BizException("No existing user with the given phone!");
        }
        String encPwd = user.getUserPassword();
        String rawPwd;
        try {
            rawPwd = RSAUtil.decrypt(encPwd);
        } catch (Exception e) {
            throw new BizException("Error decrypting data!");
        }
        // rawPwd -enc-> webPwd
        // rawPwd -MD5-> dbPwd
        String md5Pwd = MD5Util.sign(rawPwd, dbUser.getSalt(), UserConstant.DEFAULT_CHARSET);
        if (!dbUser.getUserPassword().equals(md5Pwd)) {
            throw new BizException("Wrong password!");
        }
        String accessToken;
        String refreshToken;
        try {
            accessToken = TokenUtil.generateToken(dbUser.getId());
            refreshToken = TokenUtil.generateRefreshToken(dbUser.getId());
        } catch (Exception e) {
            throw new BizException("Error generating token!");
        }
        refreshTokenService.removeByUserId(user.getId());
        RefreshToken dbRefreshToken = new RefreshToken();
        dbRefreshToken.setUserId(user.getId());
        dbRefreshToken.setRefreshToken(refreshToken);
        refreshTokenService.save(dbRefreshToken);

        Token token = new Token();
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        return token;
    }

    @Override
    public void register(User user) {
        String phone = user.getPhone();
        if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(phone)) {
            throw new BizException("手机号不能为空！");
        }
        User dbUser = userMapper.getUserByPhone(phone);
        if (dbUser != null) {
            throw new BizException("该手机号已经注册！");
        }
        String userPassword = user.getUserPassword();
        String rawPassword;
        try {
            rawPassword = RSAUtil.decrypt(userPassword);
        } catch (Exception e) {
            throw new BizException("数据解析异常！");
        }

        String salt = String.valueOf(DateUtil.getCurrentTimestamp());
        String md5Password = MD5Util.sign(rawPassword, salt, UserConstant.DEFAULT_CHARSET);
        user.setSalt(salt);
        user.setUserPassword(md5Password);
        userMapper.save(user);

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setNick(UserConstant.DEFAULT_NICK);
        userInfo.setBirth(UserConstant.DEFAULT_BIRTH);
        userInfo.setGender(UserConstant.GENDER_UNKNOW);
        userInfo.setRoleCode(AuthRoleConstant.ROLE_LV0);
        userInfoService.save(userInfo);

        UserCoin userCoin = new UserCoin();
        userCoin.setUserId(user.getId());
        userCoin.setAmount(0L);
        userCoinService.save(userCoin);
    }

    @Override
    public int updateUser(User user) {
        user.setSalt(String.valueOf(DateUtil.getCurrentTimestamp()));
        return userMapper.update(user);
    }

    @Override
    public void logout(String refreshToken, Long userId) {
        refreshTokenService.removeByToken(refreshToken);
    }

    @Override
    public String refreshAccessToken(String refreshToken, Long userId) throws Exception {
        RefreshToken dbToken = refreshTokenService.getByToken(refreshToken);
        if (Objects.isNull(dbToken) || TokenUtil.verifyRefreshToken(dbToken.getRefreshToken())
                .before(DateUtil.getCurrentTime())) {
            throw new BizException("expired refresh token!");
        }
        return TokenUtil.generateToken(dbToken.getUserId());
    }

    @Override
    public UserAuthInfo getUserAuthorities(Long userId) {
        UserInfo userInfo = userInfoService.getUserInfoByUserId(userId);
        String roleCode = userInfo.getRoleCode();
        List<AuthRoleElementOperation> authRoleElementOperations = authRoleService.listAuthRoleElementOperationByRoleId(
                roleCode);
        List<AuthRoleMenu> authRoleMenus = authRoleService.listAuthRoleMenuByRoleId(roleCode);
        UserAuthInfo userAuthInfo = new UserAuthInfo();
        userAuthInfo.setRoleMenuList(authRoleMenus);
        userAuthInfo.setRoleElementOperationList(authRoleElementOperations);
        return userAuthInfo;
    }

    @Override
    public boolean isActive(Long userId) {
        int count = 0;
        String key = RedisConstant.USER_SIGN + userId + ":" + DateUtil.formatDate(DateUtil.getCurrentTime(), "yyyyMM");
        int dayOfMonth = DateUtil.getDayOfMonth(DateUtil.getCurrentTime());
        List<Long> result = stringRedisTemplate.opsForValue()
                .bitField(key, BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth))
                        .valueAt(0));
        //没有任何签到
        if (result == null || result.isEmpty()) {
            return false;
        }
        Long num = result.get(0);
        if (num == null || num == 0) {
            return false;
        }
        while ((num & 1) != 0) {
            count++;
            //数字右移，继续下一个bit位
            num >>>= 1;
        }
        return count >= 7;
    }

    @Override
    public void sign(Long userId) {
        String key = RedisConstant.USER_SIGN + userId + ":" + DateUtil.formatDate(DateUtil.getCurrentTime(), "yyyyMM");
        int dayOfMonth = DateUtil.getDayOfMonth(DateUtil.getCurrentTime());
        stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
    }

}
