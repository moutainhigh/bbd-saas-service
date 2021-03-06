package com.bbd.saas.controllers;

import com.bbd.drivers.api.mongo.OrderTrackService;
import com.bbd.drivers.enums.TransStatus;
import com.bbd.drivers.mongoModels.OrderTrack;
import com.bbd.saas.Services.AdminService;
import com.bbd.saas.api.mongo.*;
import com.bbd.saas.api.mysql.IncomeService;
import com.bbd.saas.api.mysql.PushService;
import com.bbd.saas.constants.Constants;
import com.bbd.saas.constants.UserSession;
import com.bbd.saas.controllers.service.CommonService;
import com.bbd.saas.enums.*;
import com.bbd.saas.mongoModels.*;
import com.bbd.saas.utils.*;
import com.bbd.saas.vo.*;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 揽件入库
 */
@Controller
@RequestMapping("/holdToStore")
public class HoldToStoreController {

    @Autowired
    OrderService orderService;
    @Autowired
    UserService userService;
    @Autowired
    AdminService adminService;

    @Autowired
    PushService pushService;
    @Autowired
    TradeService tradeService;
    @Autowired
    SiteService siteService;
    @Autowired
    ExpressExchangeService expressExchangeService;
    @Autowired
    OrderParcelService orderParcelService;
    @Autowired
    OrderTrackService orderTrackService;
    @Autowired
    CommonService commonService;
    @Autowired
    ConstantService constantService;
    @Autowired
    IncomeService incomeService;

    public static final Logger logger = LoggerFactory.getLogger(HoldToStoreController.class);

    /**
     * 跳转到揽件入库页面
     *
     * @param pageIndex //开始页
     * @param status    //状态
     * @param request//
     * @param model//
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public String index(Integer pageIndex, @RequestParam(value = "status", defaultValue = "-1") String status,  final HttpServletRequest request, Model model) {
        //当前登录的用户信息
        User user = adminService.get(UserSession.get(request));
        if (user != null) {
            //获取站长下的所有揽件员
            List<User> userList = userService.findUsersBySite(user.getSite(), null, null);
            //查询 今日成功揽件数量，今日入库，未入库，历史未入库数量
            OrderHoldToStoreNumVO orderHoldToStoreNum = orderService.getOrderHoldToStoreNum(user);
            long historyToStoreNum = orderHoldToStoreNum.getHistoryToStoreNum();
            long todayNoToStoreNum = orderHoldToStoreNum.getTodayNoToStoreNum();
            long successOrderNum = orderHoldToStoreNum.getSuccessOrderNum();
            long todayToStoreNum = orderHoldToStoreNum.getTodayToStoreNum();
            //放在model 中传到页面
            model.addAttribute("historyToStoreNum", historyToStoreNum);
            model.addAttribute("todayNoToStoreNum", todayNoToStoreNum);
            model.addAttribute("successOrderNum", successOrderNum);
            model.addAttribute("todayToStoreNum", todayToStoreNum);
            model.addAttribute("user", user);
            try {
                //查询数据
                PageModel<OrderHoldToStoreVo> orderHoldPageModel = getList(pageIndex, status, null,null, request, model);
                logger.info("=====揽件入库页面====" + orderHoldPageModel);
                model.addAttribute("orderHoldPageModel", orderHoldPageModel);
                model.addAttribute("userList", userList);
            } catch (Exception e) {
                logger.error("===揽件入库页面===出错:" + e.getMessage());
            }
        }
        return "page/holdToStore";
    }


    /**
     * 根据状态，揽件员id 查询
     * @param pageIndex  当前页
     * @param orderSetStatus 状态
     * @param embraceId 揽件员
     * @param type 查询类型。0：今日成功接单数；1：历史未入库；2：今日已入库；3：今日未入库
     * @param request 请求
     * @param model  模板
     * @return 订单分页列表
     */
    @ResponseBody
    @RequestMapping(value = "/getList", method = RequestMethod.GET)
    public PageModel<OrderHoldToStoreVo> getList(Integer pageIndex, String orderSetStatus, String embraceId, String type,final HttpServletRequest request, Model model) {
        //设置查询条件
        OrderQueryVO orderQueryVO = new OrderQueryVO();
        orderQueryVO.embraceId = embraceId;
        orderQueryVO.user = adminService.get(UserSession.get(request));
        orderQueryVO.type=type;
        orderQueryVO.orderSetStatus=orderSetStatus;

        if ("0".equals(type)) {//今日成功接单数
            Date start =  Dates.getBeginOfDay(new Date());
            Date end =  Dates.getEndOfDay(new Date());
            orderQueryVO.between = new DateBetween(start,end).toString();

        }
        if ("1".equals(type)||"3".equals(type)) {//历史未入库
            if ("3".equals(type)) {//今日未入库
                Date start =  Dates.getBeginOfDay(new Date());
                Date end =  Dates.getEndOfDay(new Date());
                orderQueryVO.between = new DateBetween(start,end).toString();
            }
        }
        if ("2".equals(type)) {//今日已入库
            Date start =  Dates.getBeginOfDay(new Date());
            Date end =  Dates.getEndOfDay(new Date());
            orderQueryVO.between = new DateBetween(start,end).toString();
        }
        //首页进行默认值设置
        pageIndex = Numbers.defaultIfNull(pageIndex, 0);
        //根据embraceId查询tradeNo ，每一个tradeNo对应一个Order  封装到tradeNoList 中
        PageModel<OrderHoldToStoreVo> orderHoldPageModel = orderService.findPageOrdersForHoldToStore(pageIndex,  orderQueryVO);
        return orderHoldPageModel;
    }

    /**
     * 根据运单号检查是否存在此订单
     * 根据相关条件做，到站，入库操作
     *
     * @param request
     * @param mailNum
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/checkHoldToStoreByMailNum", method = RequestMethod.GET)
    public Map<String, Object> checkHoldToStoreByMailNum(HttpServletRequest request, @RequestParam(value = "mailNum", required = true) String mailNum) {
        User user = adminService.get(UserSession.get(request));//当前登录的用户信息
        Map<String, Object> result = new HashMap<String, Object>();//封装返回参数
        String msg = "";
        Order order = orderService.findOneByMailNum(mailNum);//根据运单号查询

        boolean status = true;
        if (order == null || order.getOrderSetStatus() == OrderSetStatus.NOEMBRACE) {//运单是否存在 未取件的也为不存在
            status = false;
            msg = "【异常扫描】不存在此运单号";
        } else if (order.getOrderSetStatus() != OrderSetStatus.WAITTOIN && order.getOrderSetStatus() != OrderSetStatus.DRIVERGETED ) {//是否重复扫描
            status = false;
            msg = "重复扫描，此运单已经扫描过啦";
        } else if (user.getSite().getAreaCode().equals(order.getAreaCode())) {//运单号存在且属于此站
            order.setOrderSetStatus(OrderSetStatus.ARRIVED);
            List<SiteTime> siteTimes = order.getSiteTimes();
            if(siteTimes==null){
                siteTimes = Lists.newArrayList();
            }
            SiteTime siteTime = new SiteTime();
            siteTime.setDateAdd(new Date());
            siteTime.setOrderSetStatus(OrderSetStatus.ARRIVED);
            siteTime.setSiteId(user.getSite().getId().toHexString());
            siteTimes.add(siteTime);
            order.setSiteTimes(siteTimes);
            //入库
            doToStore(request, order);
            //到站 == 到站和入库顺序不可以颠倒，若颠倒order需要重新查一遍。
            orderToSite(order, user);
            status = true;
            msg = "扫描成功,完成入库。此订单属于您的站点,可直接进行【运单分派】操作";
        } else if ("1".equals(user.getSite().getType())) {//分拨站点
            order.setOrderSetStatus(OrderSetStatus.ARRIVEDISPATCH);
            List<SiteTime> siteTimes = order.getSiteTimes();
            if(siteTimes==null){
                siteTimes = Lists.newArrayList();
            }
            SiteTime siteTime = new SiteTime();
            siteTime.setDateAdd(new Date());
            siteTime.setOrderSetStatus(OrderSetStatus.ARRIVEDISPATCH);
            siteTime.setSiteId(user.getSite().getId().toHexString());
            siteTimes.add(siteTime);
            order.setSiteTimes(siteTimes);
            //入库
            doToStore(request, order);
            status = true;
            msg = "扫描成功，完成入库。请到App中进行【分拣】操作";
        } else if (StringUtils.isBlank(user.getSite().getType())||"0".equals(user.getSite().getType())) {//运单号存在且属于此站且不是分拨站点
            //入库
            order.setOrderSetStatus(OrderSetStatus.WAITSET);
            List<SiteTime> siteTimes = order.getSiteTimes();
            if(siteTimes==null){
                siteTimes = Lists.newArrayList();
            }
            SiteTime siteTime = new SiteTime();
            siteTime.setDateAdd(new Date());
            siteTime.setOrderSetStatus(OrderSetStatus.WAITSET);
            siteTime.setSiteId(user.getSite().getId().toHexString());
            siteTimes.add(siteTime);
            order.setSiteTimes(siteTimes);
            doToStore(request, order);
            status = true;
            msg = "扫描成功，完成入库。请到App中进行【揽件集包】操作";
        }
        result.put("msg", msg);
        result.put("status", status);
        return result;
    }


    /**
     * 单个订单到站方法
     * @param order
     * @param user
     */
    public Order orderToSite(Order order, User user) {
        if (order.getOrderStatus() == null || order.getOrderStatus() == OrderStatus.NOTARR) {
            order.setOrderStatus(OrderStatus.NOTDISPATCH);
            order.setDateArrived(new Date());
            order.setOrderSetStatus(OrderSetStatus.ARRIVED);
        }
        order.setDateUpd(new Date());
        orderService.updateOrderOrderStatu(order.getMailNum(), OrderStatus.NOTARR, OrderStatus.NOTDISPATCH);//修改该订单所处包裹里的订单状态
        //增加物流信息
        OrderCommon.addOrderExpress(ExpressStatus.ArriveStation, order, user, "订单已送达【" + user.getSite().getName() + "】，正在分派配送员");
        orderService.save(order);
        if (order != null) {
            if(Srcs.DANGDANG.equals(order.getSrc())||Srcs.PINHAOHUO.equals(order.getSrc())||Srcs.DDKY.equals(order.getSrc())||Srcs.QIANGXIANSH.equals(order.getSrc())){
                commonService.doSaveExpressExChange(order, user.getRealName(), user.getLoginName());
            }
            orderParcleStatusChange(order.getId().toHexString(), "0");//检查是否需要更新包裹状态 包裹类型 0：配件包裹（默认） 1：集包
        }
        return order;
    }

    /**
     * 做入库操作
     * @param request
     * @param order
     */
    private void doToStore(HttpServletRequest request, Order order) {
        if (order != null) {
            //入库
            order.setDateUpd(new Date());

            orderService.save(order);

            //修改司机包裹的状态
            orderParcleStatusChange(order.getId().toHexString(),"1");//parceType 包裹类型 0：配件包裹（默认） 1：集包

            Trade trade = tradeService.findOneByTradeNo(order.getTradeNo());
            User embrace = userService.findOne(trade.getEmbraceId().toHexString());//揽件员
            if(embrace!=null){
                User user = adminService.get(UserSession.get(request));//当前登录的用户信息
                if(user!=null && user.getSite()!=null && embrace.getSite()!=null && user.getSite().getId().toHexString().equals(embrace.getSite().getId().toHexString()) ){
                    pushService.courierAdd(embrace.getPostmanuserId(),order.getMailNum());//揽件员收益
                }
            }

            //修改交易订单的状态
            long totalCount = orderService.findCountByTradeNo(order.getTradeNo(), 0);//此商户订单号下的所有运单(非移除的)
            long arrCount = orderService.findArrCountByTradeNo(order.getTradeNo());//此商户订单号下的所有已入库的运单
            if(totalCount==arrCount){//全部入库完成,修改trade的状态

                trade.setTradeStatus(TradeStatus.ARRIVED);
                trade.setDateUpd(new Date());
                String REWARD_SEND = constantService.findValueByName(Constants.REWARD_SEND);
                BigDecimal amtReaForSm = new BigDecimal(REWARD_SEND).multiply(new BigDecimal(totalCount));
                trade.setAmtReaForSm(amtReaForSm.intValue());
                trade.setOrdercnt((int)totalCount);
                tradeService.save(trade);

                if(embrace!=null){
                    User user = adminService.get(UserSession.get(request));//当前登录的用户信息
                    if(user!=null && user.getSite()!=null && embrace.getSite()!=null && user.getSite().getId().toHexString().equals(embrace.getSite().getId().toHexString()) ){
                        pushService.tradePush(embrace.getPostmanuserId(),"2",trade.getTradeNo());//推送消息给揽件员
                    }
                }
            }

            List<OrderParcel> parcelCursor =orderParcelService.findOrderParcelsByAreaCodeAndStatusAndSrc(order.getAreaCode(),Srcs.BBT.toString(), ParcelStatus.Suspense);
            if(parcelCursor.size()<=0){
                Site sitearea = siteService.findSiteByAreaCode(order.getAreaCode());
                if (sitearea != null) {
                    OrderParcel orderParcel = new OrderParcel();
                    orderParcel.setProvince(sitearea.getProvince());
                    orderParcel.setCity(sitearea.getCity());
                    orderParcel.setArea(sitearea.getArea());
                    orderParcel.setAreaName(sitearea.getName());
                    orderParcel.setAreaCode(sitearea.getAreaCode());
                    orderParcel.setAreaRemark(sitearea.getName());
                    orderParcel.setParcelCode("");
                    orderParcel.setSort_uid("");
                    orderParcel.setStation_address(sitearea.getAddress());
                    orderParcel.setDateAdd(new Date());
                    orderParcel.setDateUpd(new Date());
                    orderParcel.setStatus(ParcelStatus.Suspense);
                    orderParcel.setParceltyp("0");
                    orderParcel.setSrc(Srcs.BBT.toString());
                    orderParcelService.saveOrderParcel(orderParcel);
                }
            }
        }
    }

    /**
     * 重新计算揽件的数据统计
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/updateOrderHoldToStoreNumVO", method = RequestMethod.GET)
    public OrderHoldToStoreNumVO updateOrderHoldToStoreNumVO(HttpServletRequest request) {
        return orderService.getOrderHoldToStoreNum(adminService.get(UserSession.get(request)));
    }
    /**
     * 检查是否需要更新包裹状态
     * @param orderId
     * @param parceType 包裹类型 0：配件包裹（默认） 1：集包
     */
    private void orderParcleStatusChange(String orderId,String parceType){
        OrderParcel orderParcel = orderParcelService.findOrderParcelByOrderIdAndParcelType(orderId,parceType);
        if (orderParcel != null) {
            Boolean flag = true;//是否可以更新包裹的状态
            for (Order orderTemp : orderParcel.getOrderList()) {
                Order orderReal = orderService.findOneByMailNum(orderTemp.getAreaCode(),orderTemp.getMailNum());
                if (orderReal==null || orderReal.getOrderSetStatus() == null || orderReal.getOrderSetStatus() == OrderSetStatus.WAITTOIN) {
                    flag = false;
                }
            }
            if (flag) {//更新包裹状态，做包裹到站操作
                orderParcel.setStatus(ParcelStatus.ArriveStation);//包裹到站
                orderParcel.setDateUpd(new Date());
                orderParcelService.saveOrderParcel(orderParcel);
                /**修改orderTrack里的状态*/
                try {
                    String trackNo = orderParcel.getTrackNo();
                    if (StringUtils.isNotBlank(trackNo)) {
                        OrderTrack orderTrack = orderTrackService.findOneByTrackNo(trackNo);
                        if (orderTrack != null) {
                            List<OrderParcel> orderParcelList = orderParcelService.findOrderParcelListByTrackCode(trackNo);
                            Boolean flagForUpdateTrackNo = true;//是否可以更新orderTrack下的状态
                            for (OrderParcel orderParcel1 : orderParcelList) {
                                if (orderParcel1.getStatus() != ParcelStatus.ArriveStation) {
                                    flagForUpdateTrackNo = false;//不可更新
                                }
                            }
                            if (flagForUpdateTrackNo) {//可以更新orderTrack下的状态
                                orderTrack.dateUpd = new Date();
                                orderTrack.sendStatus = OrderTrack.SendStatus.ArriveStation;
                                orderTrack.transStatus = TransStatus.YWC;
                                orderTrack.preSchedule="已送达";
                                orderTrackService.updateOrderTrack(trackNo, orderTrack);
                                incomeService.driverIncome(Numbers.parseInt(orderTrack.driverId, 0), orderTrack.actOrderPrice, orderTrack.trackNo);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
