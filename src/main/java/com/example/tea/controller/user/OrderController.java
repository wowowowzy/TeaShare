package com.example.tea.controller.user;

import com.example.tea.entity.dto.Order.OrderPayDTO;
import com.example.tea.entity.pojo.Result;
import com.example.tea.entity.vo.Order.OrderListVO;
import com.example.tea.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/user/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 支付接口
     * @param list
     * @param couponId
     * @return
     */
    @PostMapping("/pay")
    public Result pay(@RequestBody List<OrderPayDTO> list,
                      @RequestParam(required = false) Long couponId) {
        if (list.get(0).getQuantity()==null||list.get(0).getGoodsId()==null){
            return Result.error("必须参数未填写");
        }
        try {
            orderService.pay(list, couponId, null);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询全部订单
     * @return
     */
    @GetMapping("/getOrders")
    public Result getOrders(){
         List<OrderListVO> list = orderService.getOrders();
        return Result.success(list);
    }

    /**
     * 查询指定订单
     * @param orderId
     * @return
     */
    @GetMapping("/getOrderById")
    public Result getOrderById(Long orderId){
         OrderListVO vo = orderService.getOrderById(orderId);
        return Result.success(vo);
    }

}
