package com.miaoshaproject.dao;

import com.miaoshaproject.dataobject.UserDao;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDaoMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_info
     *
     * @mbg.generated Fri Jul 12 00:27:56 CST 2019
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_info
     *
     * @mbg.generated Fri Jul 12 00:27:56 CST 2019
     */
    int insert(UserDao record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_info
     *
     * @mbg.generated Fri Jul 12 00:27:56 CST 2019
     */
    int insertSelective(UserDao record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_info
     *
     * @mbg.generated Fri Jul 12 00:27:56 CST 2019
     */
    UserDao selectByPrimaryKey(Integer id);
    UserDao selectByTelphone(String telphone);
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_info
     *
     * @mbg.generated Fri Jul 12 00:27:56 CST 2019
     */
    int updateByPrimaryKeySelective(UserDao record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_info
     *
     * @mbg.generated Fri Jul 12 00:27:56 CST 2019
     */
    int updateByPrimaryKey(UserDao record);
}