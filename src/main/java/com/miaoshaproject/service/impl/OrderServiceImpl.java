package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.OrderDaoMapper;
import com.miaoshaproject.dao.SequenceDaoMapper;
import com.miaoshaproject.dataobject.OrderDao;
import com.miaoshaproject.dataobject.SequenceDao;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EnumBussinessError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.OrderModel;
import com.miaoshaproject.service.model.UserModel;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDaoMapper orderDaoMapper;

    @Autowired
    private SequenceDaoMapper sequenceDaoMapper;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BusinessException {
        // 1.校验下单状态：下单的商品是否存在， 用户是否合法， 购买数量是否正确
        ItemModel itemModel = itemService.getItemById(itemId);
        if(itemId == null){
            throw new BusinessException(EnumBussinessError.PARAMETER_VALIDATION_ERROR,"商品信息不存在");
        }
        UserModel userModel = userService.getUserById(userId);
        if(userModel ==null){
            throw new BusinessException(EnumBussinessError.PARAMETER_VALIDATION_ERROR,"用户信息不存在");
        }
        if(amount <=0 || amount >100){
            throw new BusinessException(EnumBussinessError.PARAMETER_VALIDATION_ERROR,"购买数量不存在");
        }

        // 还需要校验活动信息
        if (promoId !=null){
            // (1)校验对应活动是否存在对应商品
            if(promoId.intValue() != itemModel.getPromoModel().getId()){
                throw new BusinessException(EnumBussinessError.PARAMETER_VALIDATION_ERROR, "活动信息不正确");
            }else if (itemModel.getPromoModel().getStatus() != 2){
                throw new BusinessException(EnumBussinessError.PARAMETER_VALIDATION_ERROR, "活动还未开始");
            }

        }

        // 2.落单减库存，支付减库存，这里采用的是落单减库存
        boolean result = itemService.decreaseStock(itemId,amount);
        if(!result){
            // 没能成功减库存
            throw new BusinessException(EnumBussinessError.STOCK_NOT_ENOUGH);
        }
        // 3.订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setAmount(amount);
        orderModel.setItemId(itemId);
        if (promoId !=null){
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice()); // 活动价格
        }else{
            orderModel.setItemPrice(itemModel.getPrice()); // 平价价格
        }
        orderModel.setPromoId(promoId); // 秒杀活动校验完成后传递活动ID

        // orderModel.getItemPrice()已包含在活动中或不在活动的分支逻辑
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));

        orderModel.setId(generateOrderNo());
        OrderDao orderDao = convertFromItemModel(orderModel);
        orderDaoMapper.insertSelective(orderDao); // 订单数据库插入

        // 加上商品的销量
        itemService.increaseSales(itemId, amount);

        // 4.返回前端
        return orderModel;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW) // sequence表必须提交
    String generateOrderNo(){
        //订单号有16位
        StringBuilder stringBuilder = new StringBuilder();
        //前8位是日期信息，年月日
        LocalDateTime now = LocalDateTime.now();
        String nowData = now.format(DateTimeFormatter.ISO_DATE).replace("-","");
        stringBuilder.append(nowData);

        //中间6位位自增序列 唯一
        int sequence;
        SequenceDao sequenceDao = sequenceDaoMapper.getSequenceByName("order_info");

        sequence = sequenceDao.getCurrentValue();
        sequenceDao.setCurrentValue(sequenceDao.getCurrentValue()+sequenceDao.getStep());
        sequenceDaoMapper.updateByPrimaryKeySelective(sequenceDao);

        String sequenceStr = String.valueOf(sequence);
        for (int i = 0; i < 6-sequenceStr.length(); i++) {
            stringBuilder.append(0);
        }
        stringBuilder.append(sequenceStr);
        //最后两位为分库分表位
        stringBuilder.append("00");

        return stringBuilder.toString();
    }

    private OrderDao convertFromItemModel(OrderModel orderModel){
        if(orderModel ==null){
            return  null;
        }
        OrderDao orderDao = new OrderDao();
        BeanUtils.copyProperties(orderModel,orderDao);
        orderDao.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDao.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return orderDao;
    }
}
