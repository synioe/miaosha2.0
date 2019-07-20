package com.miaoshaproject.dao;

import com.miaoshaproject.dataobject.PromoDao;
import org.springframework.stereotype.Repository;

@Repository
public interface PromoDaoMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table promo
     *
     * @mbg.generated Fri Jul 19 23:18:37 CST 2019
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table promo
     *
     * @mbg.generated Fri Jul 19 23:18:37 CST 2019
     */
    int insert(PromoDao record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table promo
     *
     * @mbg.generated Fri Jul 19 23:18:37 CST 2019
     */
    int insertSelective(PromoDao record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table promo
     *
     * @mbg.generated Fri Jul 19 23:18:37 CST 2019
     */
    PromoDao selectByPrimaryKey(Integer id);

    PromoDao selectByItemId(Integer itemId);
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table promo
     *
     * @mbg.generated Fri Jul 19 23:18:37 CST 2019
     */
    int updateByPrimaryKeySelective(PromoDao record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table promo
     *
     * @mbg.generated Fri Jul 19 23:18:37 CST 2019
     */
    int updateByPrimaryKey(PromoDao record);
}