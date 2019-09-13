package com.miaoshaproject.dao;

import com.miaoshaproject.dataobject.StockLogDao;
import org.springframework.stereotype.Repository;

@Repository
public interface StockLogDaoMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table stock_log
     *
     * @mbg.generated Wed Sep 04 23:49:21 CST 2019
     */
    int deleteByPrimaryKey(String itemLogId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table stock_log
     *
     * @mbg.generated Wed Sep 04 23:49:21 CST 2019
     */
    int insert(StockLogDao record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table stock_log
     *
     * @mbg.generated Wed Sep 04 23:49:21 CST 2019
     */
    int insertSelective(StockLogDao record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table stock_log
     *
     * @mbg.generated Wed Sep 04 23:49:21 CST 2019
     */
    StockLogDao selectByPrimaryKey(String itemLogId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table stock_log
     *
     * @mbg.generated Wed Sep 04 23:49:21 CST 2019
     */
    int updateByPrimaryKeySelective(StockLogDao record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table stock_log
     *
     * @mbg.generated Wed Sep 04 23:49:21 CST 2019
     */
    int updateByPrimaryKey(StockLogDao record);
}