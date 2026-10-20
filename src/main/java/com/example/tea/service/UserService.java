package com.example.tea.service;

import com.example.tea.entity.dto.User.LoginInfo;
import com.example.tea.entity.dto.User.RegisterInfo;
import com.example.tea.entity.dto.User.UserQueryDTO;
import com.example.tea.entity.pojo.PageResult;
import com.example.tea.entity.pojo.Result;
import com.example.tea.entity.vo.User.LoginVO;
import com.example.tea.entity.vo.User.UserVO;

public interface UserService {
    LoginVO login(LoginInfo loginInfo);
    Result register(RegisterInfo registerInfo);

    UserVO show() throws Exception;

    /**
     * 退出登录：把当前 token 加入黑名单（幂等，未携带 token 也视为成功）。
     */
    Result logout(String token);

    LoginVO loginAdmin(LoginInfo loginInfo);

    PageResult showUserAdmin(UserQueryDTO dto);

    Result banUser(Long userId);

    Result switchBanUser(Long userId);
}
