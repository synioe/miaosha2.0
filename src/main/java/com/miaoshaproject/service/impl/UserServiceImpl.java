package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.UserDaoMapper;
import com.miaoshaproject.dao.UserPasswordDaoMapper;
import com.miaoshaproject.dataobject.UserDao;
import com.miaoshaproject.dataobject.UserPasswordDao;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EnumBussinessError;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDaoMapper userDaoMapper;
    @Autowired
    private UserPasswordDaoMapper userPasswordDaoMapper;

    @Autowired
    private ValidatorImpl validator;

    @Override
    public UserModel getUserById(Integer id) {
        //调用Userdaomapper获取对应用户的dataobject
        //userdao不能直接透传给前端
        UserDao userDAO = userDaoMapper.selectByPrimaryKey(id);

//        userPasswordDaoMapper.selectByPrimaryKey()
        if (userDAO == null) {
            return null;
        }
        //通过用户id获取对应的用户加密密码信息
        UserPasswordDao userPasswordDao = userPasswordDaoMapper.selectByUserId(userDAO.getId());
        return convertFromDataObject(userDAO, userPasswordDao);
    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if (userModel == null) {
            throw new BusinessException(EnumBussinessError.PARAMETER_VALIDATION_ERROR);
        }

//        if (StringUtils.isEmpty(userModel.getName())
//                || userModel.getAge() == null
//                || userModel.getGender() == null
//                || StringUtils.isEmpty(userModel.getTelphone())) {
//            throw new BusinessException(EnumBussinessError.PARAMETER_VALIDATION_ERROR);
//        }
        ValidationResult result = validator.validate(userModel);

        if(result.isHasErrors()){
            throw new BusinessException(EnumBussinessError.PARAMETER_VALIDATION_ERROR,result.getErrMsg());
        }
        //实现model->dataobject=方法
        UserDao userDao = convertFromModel(userModel);
        //userDaoMapper.insertSelective(userDao); // error

        try {
            userDaoMapper.insertSelective(userDao);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(EnumBussinessError.PARAMETER_VALIDATION_ERROR, "手机号已存在");
        }

        userModel.setId(userDao.getId()); // 少了这一句 原因是数据库表中的id没有设置成AI
        // 这里遇到的坑是将userDaoMapper.xml中的insertSelect后的参数弄掉了，导致数据库无法注入
        UserPasswordDao userPasswordDao = convertPasswordFromModel(userModel);
        userPasswordDaoMapper.insertSelective(userPasswordDao);
    }

    @Override
    public UserModel validateLogin(String telphone, String encrptPassword) throws BusinessException {
        //通过用户手机获取用户信息userModel
        UserDao userDao = userDaoMapper.selectByTelphone(telphone);
        if (userDao == null) {
            throw new BusinessException(EnumBussinessError.USER_LOGIN_FAIL);
        }
        UserPasswordDao userPasswordDao = userPasswordDaoMapper.selectByUserId(userDao.getId());
        UserModel userModel = convertFromDataObject(userDao, userPasswordDao);

        //对比用户信息内加密的密码是否和传输进来的密码相匹配
        if (!StringUtils.equals(encrptPassword, userModel.getEncrptPassword())){
            throw new BusinessException(EnumBussinessError.USER_LOGIN_FAIL);
        }
        return userModel;

    }

    private UserPasswordDao convertPasswordFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserPasswordDao userPasswordDao = new UserPasswordDao();
        userPasswordDao.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDao.setUserId(userModel.getId());

        return userPasswordDao;
    }

    private UserDao convertFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserDao userDao = new UserDao();
        BeanUtils.copyProperties(userModel, userDao);

        return userDao;
    }

    private UserModel convertFromDataObject(UserDao userDao, UserPasswordDao userPasswordDao) {
        if (userDao == null) {
            return null;
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDao, userModel);
        if (userPasswordDao != null) {
            userModel.setEncrptPassword(userPasswordDao.getEncrptPassword());
        }

        return userModel;
    }
}
