package com.miaoshaproject.dataobject;

public class OrderDao {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order_info.id
     *
     * @mbg.generated Fri Jul 19 14:47:30 CST 2019
     */
    private String id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order_info.user_id
     *
     * @mbg.generated Fri Jul 19 14:47:30 CST 2019
     */
    private Integer userId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order_info.item_id
     *
     * @mbg.generated Fri Jul 19 14:47:30 CST 2019
     */
    private Integer itemId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order_info.item_price
     *
     * @mbg.generated Fri Jul 19 14:47:30 CST 2019
     */
    private Double itemPrice;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order_info.amount
     *
     * @mbg.generated Fri Jul 19 14:47:30 CST 2019
     */
    private Integer amount;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order_info.order_price
     *
     * @mbg.generated Fri Jul 19 14:47:30 CST 2019
     */
    private Double orderPrice;

    private Integer promoId;

    public Integer getPromoId() {
        return promoId;
    }

    public void setPromoId(Integer promoId) {
        this.promoId = promoId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order_info.id
     *
     * @return the value of order_info.id
     *
     * @mbg.generated Fri Jul 19 14:47:30 CST 2019
     */
    public String getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order_info.id
     *
     * @param id the value for order_info.id
     *
     * @mbg.generated Fri Jul 19 14:47:30 CST 2019
     */
    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order_info.user_id
     *
     * @return the value of order_info.user_id
     *
     * @mbg.generated Fri Jul 19 14:47:30 CST 2019
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order_info.user_id
     *
     * @param userId the value for order_info.user_id
     *
     * @mbg.generated Fri Jul 19 14:47:30 CST 2019
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order_info.item_id
     *
     * @return the value of order_info.item_id
     *
     * @mbg.generated Fri Jul 19 14:47:30 CST 2019
     */
    public Integer getItemId() {
        return itemId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order_info.item_id
     *
     * @param itemId the value for order_info.item_id
     *
     * @mbg.generated Fri Jul 19 14:47:30 CST 2019
     */
    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order_info.item_price
     *
     * @return the value of order_info.item_price
     *
     * @mbg.generated Fri Jul 19 14:47:30 CST 2019
     */
    public Double getItemPrice() {
        return itemPrice;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order_info.item_price
     *
     * @param itemPrice the value for order_info.item_price
     *
     * @mbg.generated Fri Jul 19 14:47:30 CST 2019
     */
    public void setItemPrice(Double itemPrice) {
        this.itemPrice = itemPrice;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order_info.amount
     *
     * @return the value of order_info.amount
     *
     * @mbg.generated Fri Jul 19 14:47:30 CST 2019
     */
    public Integer getAmount() {
        return amount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order_info.amount
     *
     * @param amount the value for order_info.amount
     *
     * @mbg.generated Fri Jul 19 14:47:30 CST 2019
     */
    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order_info.order_price
     *
     * @return the value of order_info.order_price
     *
     * @mbg.generated Fri Jul 19 14:47:30 CST 2019
     */
    public Double getOrderPrice() {
        return orderPrice;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order_info.order_price
     *
     * @param orderPrice the value for order_info.order_price
     *
     * @mbg.generated Fri Jul 19 14:47:30 CST 2019
     */
    public void setOrderPrice(Double orderPrice) {
        this.orderPrice = orderPrice;
    }
}