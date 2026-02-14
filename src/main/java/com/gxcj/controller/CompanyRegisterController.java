package com.gxcj.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxcj.constant.SysConstant;
import com.gxcj.entity.CompanyEntity;
import com.gxcj.entity.HrEntity;
import com.gxcj.entity.UserEntity;
import com.gxcj.entity.dto.CompanyRegisterDto;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.CompanyMapper;
import com.gxcj.mapper.HrMapper;
import com.gxcj.mapper.UserMapper;
import com.gxcj.result.Result;
import com.gxcj.utils.EntityHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/company")
public class CompanyRegisterController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private HrMapper hrMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> register(@RequestBody CompanyRegisterDto dto) {
        // 1. 验证邮箱格式
        if (!StringUtils.hasText(dto.getAdminAccount())) {
            throw new BusinessException("邮箱不能为空");
        }
        if (!dto.getAdminAccount().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new BusinessException("邮箱格式不正确");
        }

        // 2. 验证邮箱验证码
        String redisKey = SysConstant.REDIS_EMAIL_CODE + dto.getAdminAccount();
        String cachedCode = stringRedisTemplate.opsForValue().get(redisKey);

        if (cachedCode == null) {
            throw new BusinessException("验证码已过期，请重新获取");
        }

        if (!cachedCode.equals(dto.getEmailCode())) {
            throw new BusinessException("验证码错误");
        }

        // 3. 检查邮箱是否已注册
        LambdaQueryWrapper<UserEntity> emailWrapper = new LambdaQueryWrapper<>();
        emailWrapper.eq(UserEntity::getEmail, dto.getAdminAccount());
        Long emailCount = userMapper.selectCount(emailWrapper);
        if (emailCount > 0) {
            throw new BusinessException("该邮箱已被注册");
        }

        // 4. 检查信用代码是否重复
        LambdaQueryWrapper<CompanyEntity> codeWrapper = new LambdaQueryWrapper<>();
        codeWrapper.eq(CompanyEntity::getCode, dto.getCode());
        Long codeCount = companyMapper.selectCount(codeWrapper);
        if (codeCount > 0) {
            throw new BusinessException("该信用代码已被注册");
        }

        // 5. 创建企业记录
        String companyId = EntityHelper.uuid();
        CompanyEntity company = new CompanyEntity();
        company.setId(companyId);
        company.setName(dto.getName());
        company.setCode(dto.getCode());
        company.setIndustry(dto.getIndustry());
        company.setScale(dto.getScale());
        company.setLicenseUrl(dto.getLicenseUrl());
        company.setContactPerson(dto.getContactPerson());
        company.setContactPhone(dto.getContactPhone());
        company.setEmail(dto.getAdminAccount());
        company.setStatus(0); // 待审核
        company.setIsDeleted(0);
        company.setCreateTime(EntityHelper.now());
        company.setUpdateTime(EntityHelper.now());
        company.setDefaultAccountId(""); // 暂时为空，后面会更新
        companyMapper.insert(company);

        // 6. 创建HR账号
        String userId = EntityHelper.uuid();
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setLoginIdentity(dto.getAdminAccount()); // 使用邮箱作为登录账号
        user.setEmail(dto.getAdminAccount());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getContactPerson());
        user.setRealName(dto.getContactPerson());
        user.setRoleKey("ROLE_COMPANY");
        user.setOwnerId(companyId);
        user.setStatus(1); // 正常
        user.setIsDeleted(0);
        user.setCreateTime(EntityHelper.now());
        user.setUpdateTime(EntityHelper.now());
        userMapper.insert(user);

        // 7. 更新企业的默认账号ID
        company.setDefaultAccountId(userId);
        companyMapper.updateById(company);

        // 8. 创建HR信息
        String hrId = EntityHelper.uuid();
        HrEntity hr = new HrEntity();
        hr.setHrId(hrId);
        hr.setUserId(userId);
        hr.setCompanyId(companyId);
        hr.setName(dto.getContactPerson());
        hr.setWorkPhone(dto.getContactPhone());
        hr.setWorkEmail(dto.getAdminAccount());
        hr.setPosition("招聘专员");
        hr.setStatus(1);
        hr.setCreateTime(EntityHelper.now());
        hr.setUpdateTime(EntityHelper.now());
        hrMapper.insert(hr);

        // 9. 删除Redis验证码（防止重复使用）
        stringRedisTemplate.delete(redisKey);

        return Result.success();
    }
}
