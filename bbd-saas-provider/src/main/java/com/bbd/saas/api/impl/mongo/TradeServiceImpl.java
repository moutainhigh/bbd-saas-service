package com.bbd.saas.api.impl.mongo;

import com.bbd.saas.api.mongo.RelationService;
import com.bbd.saas.api.mongo.TradeService;
import com.bbd.saas.dao.mongo.*;
import com.bbd.saas.dao.mysql.PostmanUserDao;
import com.bbd.saas.enums.TradeStatus;
import com.bbd.saas.models.PostmanUser;
import com.bbd.saas.mongoModels.*;
import com.bbd.saas.utils.Dates;
import com.bbd.saas.utils.Numbers;
import com.bbd.saas.utils.PageModel;
import com.bbd.saas.utils.PropertiesLoader;
import com.bbd.saas.vo.OrderQueryVO;
import com.bbd.saas.vo.TradeQueryVO;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.*;

/**
 * Created by liyanlei on 2016/6/13.
 * trade接口(包裹)
 */
public class TradeServiceImpl implements TradeService {
    public static final Logger logger = LoggerFactory.getLogger(TradeServiceImpl.class);

    private TradeDao tradeDao;
    private TradePushDao tradePushDao;
    private OrderDao orderDao;
    private UserDao userDao;
    private OrderNumDao orderNumDao;
    private PostmanUserDao postmanUserDao;
    @Autowired
    private RelationService relationService;

    PropertiesLoader propertiesLoader = new PropertiesLoader("config.properties");
    private int bbdTradePushCount = Numbers.parseInt(propertiesLoader.getProperty("bbd.trade.push.count"),2);
    private int bbdTradePushRangeInit = Numbers.parseInt(propertiesLoader.getProperty("bbd.trade.push.range.init"),2);
    private int bbdTradePushRangeStep = Numbers.parseInt(propertiesLoader.getProperty("bbd.trade.push.range.step"),1);
    private int bbdTradePushRangeThreshold = Numbers.parseInt(propertiesLoader.getProperty("bbd.trade.push.range.threshold"),5);
    private int bbdTradePushPerNum = Numbers.parseInt(propertiesLoader.getProperty("bbd.trade.push.bbdTradePushPerNum"),20);

    public OrderNumDao getOrderNumDao() {
        return orderNumDao;
    }

    public void setOrderNumDao(OrderNumDao orderNumDao) {
        this.orderNumDao = orderNumDao;
    }

    public TradeDao getTradeDao() {
        return tradeDao;
    }

    public void setTradeDao(TradeDao tradeDao) {
        this.tradeDao = tradeDao;
    }

    public OrderDao getOrderDao() {
        return orderDao;
    }

    public void setOrderDao(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public TradePushDao getTradePushDao() {
        return tradePushDao;
    }

    public void setTradePushDao(TradePushDao tradePushDao) {
        this.tradePushDao = tradePushDao;
    }

    public PostmanUserDao getPostmanUserDao() {
        return postmanUserDao;
    }

    public void setPostmanUserDao(PostmanUserDao postmanUserDao) {
        this.postmanUserDao = postmanUserDao;
    }

    /**
     * 根据id查询
     * @param tradeId  商户订单id
     * @return  商户订单
     */
    @Override
    public Trade findOne(String tradeId) {
        if (tradeId == null || "".equals(tradeId)) {
            return null;
        }
        return tradeDao.findOne("_id", new ObjectId(tradeId));

    }

    @Override
    public Trade findOneByTradeNo(String tradeNo) {
        //订单信息
        Trade trade = tradeDao.findOne("tradeNo", tradeNo);
        return trade;
    }
    /**
     * 根据查询条件和订单状态获取商户订单列表信息
     * @param pageIndex 当前页
     * @param tradeQueryVO 查询条件
     * @return 分页对象（分页信息和当前页的数据）
     */
    @Override
    public PageModel<Trade> findTradePage(Integer pageIndex, TradeQueryVO tradeQueryVO){
        //查询订单号或者运单号包含tradeQueryVO.tradeNoLike && 运单的收件人手机号、姓名、地址中包含rcvKeyword的订单
        if(tradeQueryVO.tradeStatus != null && tradeQueryVO.tradeStatus != TradeStatus.WAITPAY.getStatus()){
            //针对已经打单成功的数据,此时并未更新trade表里的orderSnaps里的运单号
            //先去order表里检索mailNum为nolike的order,将tradeNo值取出来
            if(StringUtils.isNotBlank(tradeQueryVO.noLike)){//订单或者运单的搜索
                OrderQueryVO orderQueryVO = new OrderQueryVO();
                orderQueryVO.tradeOrMailNoLike = tradeQueryVO.noLike;
                List<Order> orders = orderDao.findTradeNoList(orderQueryVO);
                tradeQueryVO.tradeNoList = Lists.newArrayList();
                for(Order order : orders){
                    String tradeNo = order.getTradeNo();
                    if(StringUtils.isNotBlank(tradeNo)){
                        tradeQueryVO.tradeNoList.add(tradeNo);
                    }
                }
            }
        }

        PageModel<Trade> tradePageModel = tradeDao.findTradePage(pageIndex, tradeQueryVO);
        //设置快件数量和揽件人、状态
        List<Trade> tradeList = tradePageModel.getDatas();
        if (tradeList != null && tradeList.size() > 0){
            for (Trade trade : tradeList){
                //揽件人
                if(trade.getEmbraceId() != null){
                    trade.setEmbrace(userDao.findOne("_id", trade.getEmbraceId()));
                }
                //状态
                trade.setStatusMsg(trade.getTradeStatus().getMessage());
            }
        }
        return tradePageModel;
    }
    /**
     * 根据指定条件查询商户订单列表
     * @param tradeQueryVO 指定查询条件
     * @return  商户订单列表
     */
    @Override
    public List<Trade> findTradeListByQuerys(TradeQueryVO tradeQueryVO) {
        return tradeDao.findTradeListByQuerys(tradeQueryVO);
    }
    /**
     * 根据指定条件查询商户订单号集合
     * @param tradeQueryVO 指定查询条件
     * @return  商户订单号集合
     */
    @Override
    public Set<String> findTradeNoSetByQuerys(TradeQueryVO tradeQueryVO) {
        List<Trade> tradeList = tradeDao.findTradeListByQuerys(tradeQueryVO);
        Set<String> tradeNoSet = new HashSet<String>();
        if(tradeList != null && tradeList.size() > 0){
            for (Trade trade : tradeList){
                if(trade != null && trade.getTradeNo() != null){
                    tradeNoSet.add(trade.getTradeNo());
                }
            }
        }
        return  tradeNoSet;
    }

    /**
     * 保存商户订单对象
     * @param trade 商户订单
     * @return Key<Trade>保存结果
     */
    @Override
    public Key<Trade> save(Trade trade) {
        return tradeDao.save(trade);
    }

    /**
     * 根据商户订单名id去更新此商户订单的状态
     * @param tradeId 商户订单名id
     * @param tradeStatus 订单状态
     */
    @Override
    public void updateTradeStatusByTradeId(String tradeId, TradeStatus tradeStatus) {
        tradeDao.updateTradeStatusByTradeId(tradeId, tradeStatus);
    }

    /**
     * 根据商户订单id删除
     * @param tradeId 商户订单id
     */
    @Override
    public void delTradesBySiteId(String tradeId) {
        tradeDao.delTradesBySiteId(tradeId);
    }

    /**
     * 根据trade对象删除商户订单
     * @param trade
     */
    @Override
    public void delTrade(Trade trade) {
        tradeDao.delete(trade);
    }

    /**
     * 根据用户Id获得该用户不同状态的订单的数量
     * @return {WAITPAY:待支付；WAITCATCH：待接单；WAITGET:待取件； GETED:已取件；CANCELED:已取消}
     */
    @Override
    public Map<String, Long> findDiffStatusCountByUid(ObjectId uId) {
        Map<String, Long> map = new HashMap<String, Long>();
        //WAITPAY:待支付
        map.put("WAITPAY", tradeDao.selectCountByUidAndStatus(uId, TradeStatus.WAITPAY));
        //WAITCATCH：待接单
        map.put("WAITCATCH", tradeDao.selectCountByUidAndStatus(uId, TradeStatus.WAITCATCH));
        //WAITGET:待取件
        map.put("WAITGET", tradeDao.selectCountByUidAndStatus(uId, TradeStatus.WAITGET));
        //GETED:已取件
        map.put("GETED", tradeDao.selectCountByUidAndStatus(uId, TradeStatus.GETED));
        //CANCELED:已取消
        map.put("CANCELED", tradeDao.selectCountByUidAndStatus(uId, TradeStatus.CANCELED));
        return map;
    }
    /**
     * 根据用户Id、订单状态获取订单的数量
     * @return 订单数量
     */
    @Override
    public long findCountByUidAndStatus(ObjectId uId, TradeStatus tradeStatus) {
        return tradeDao.selectCountByUidAndStatus(uId, tradeStatus);
    }
    /**
     * 获取递增的9位数字，用于记录支付订单号
     *
     * @return
     */
    @Override
    public String reduceTradeNo() {
        OrderNum one = orderNumDao.findOrderNum();
        long tradeNum = Long.parseLong(one.tradeNum);
        one.tradeNum = (tradeNum + 1) + "";
        orderNumDao.updateOrderNum("tradeNum",one.tradeNum);
        return one.tradeNum;
    }

    /**
     * 获取需要推送给快递员的运单集合
     * @return
     */
    @Override
    public List<Trade> findTradeListByPushJob() {
        return tradeDao.findTradeListByPushJob(bbdTradePushCount);
    }
    @Override
    public List<Trade> findTradesByEmbraceId(ObjectId embraceId,String type){
        return tradeDao.findTradesByEmbraceId(embraceId,type);
    }

    @Override
    public void doJobWithAllPushTrade() {
        try {
            //获取到所有需要推送的Trade信息
            List<Trade> tradeList = findTradeListByPushJob();
            if(tradeList!=null&&tradeList.size()>0){
                for (Trade trade : tradeList) {
                    //设置推送范围
                    if(trade.getPushRange()==null||trade.getPushRange()<=0){
                        //设置默认推送次数
                        if(trade.getPushCount()==null||trade.getPushCount()<=0){
                            trade.setPushCount(0);
                        }
                        trade.setPushRange(bbdTradePushRangeInit);
                        trade.setDateUpd(new Date());
                        tradeDao.save(trade);
                    }
                    this.doJobWithPushTrade(trade);
                    //this.pushTradeToPostmanUsers(trade, relation.getEmbraceIdList());//给绑定的所有揽件员推单
                }
                logger.info("一波订单推送揽件员完成");
            }else{
                logger.info("暂无订单需要推送给揽件员");
            }
        } catch (Exception e) {
            logger.error("把订单物流状态同步到mysql库出错：" + e.getMessage());
        }
    }
    /**
     * 订单推送逻辑 -- 订单推送给商家绑定的揽件员
     * @param trade
     */
    public void pushTradeToPostmanUsers(Trade trade, List<Integer> pmUserIds) {
        if(trade.getPushCount()> bbdTradePushCount){//推送了bbdTradePushCount+1次
            //若pushCount >= 2,则对trade进行兜底处理，变更tradeStatus，不再进行推送
            trade.setTradeStatus(TradeStatus.LASTOPER);
        }else{//开始新的一轮推送
            //循环遍历揽件员
            for (Integer userId : pmUserIds) {
                //判断揽件员是否已推送，如没有则直接进行推送操作，已推送则跳过
                TradePush tradePush = tradePushDao.findTradePushWithPostmanUserId(trade.getTradeNo(),userId);
                this.doPushToPostMan(userId, trade.getTradeNo(), tradePush);
            }
            trade.setPushCount(trade.getPushCount()+1);//推送次数+1
        }
        trade.setDateUpd(new Date());
        tradeDao.save(trade);
    }
    /**
     * 订单推送逻辑 -- 抢单模式
     * @param trade
     */
    public void doJobWithPushTrade(Trade trade) {
        //获取该运单范围内的所有的揽件员信息,已根据距离排好序
        List<PostmanUser> postmanUserList = getPostmanUserList(trade);
        int flag = 1;//已推送
        int i=0;
        //循环遍历揽件员
        for (PostmanUser postmanUser: postmanUserList) {
            //判断揽件员是否已推送，如没有则直接进行推送操作，已推送则跳过
            TradePush tradePush = tradePushDao.findTradePushWithPostmanUserId(trade.getTradeNo(),postmanUser.getId());
            if(tradePush==null||(tradePush!=null&&tradePush.getFlag()==0)){
                //进行推送操作
                //调用存储过程，记录push指令
                //支付后客户端推送存储  call  `sp_postman_bbdpush_trade`(p_uid INT, p_typ VARCHAR (1),p_trade VARCHAR(32))
                Map<String, Object> map = new HashMap<String,Object>();
                map.put("id",postmanUser.getId());
                map.put("typ","1");
                map.put("trade",trade.getTradeNo());
                postmanUserDao.pushBbdTrade(map);
                //将此揽件员信息记录到tradePush中
                if(tradePush==null) {
                    tradePush = new TradePush();
                    tradePush.setTradeNo(trade.getTradeNo());
                    tradePush.setPostmanId(postmanUser.getId());
                    tradePush.setTime(0);
                    tradePush.setDateAdd(new Date());
                }else{
                    tradePush.setTime(tradePush.getTime()+1);
                }
                tradePush.setFlag(1);
                tradePush.setDateUpd(new Date());
                logger.info("运单："+trade.getTradeNo()+"推送给小件员："+postmanUser.getId()+"成功");
                tradePushDao.save(tradePush);
                //如果推送成功，flag = 2;推送不成功，则flag一直为1
                flag = 2;
                i++;
                if(i>=bbdTradePushPerNum){
                    break;
                }

            }
        }
        //如果flag 为1，表明未对任何一个快递员进行推送操作 或者没有存在可以推送的快递员
        //相当于推送一轮完成，此时将pushCount+1，推送范围增加
        if(flag == 1){
            //进行推送操作之后，变更推送的次数为+1
            if(trade.getPushRange()<=bbdTradePushRangeThreshold){
                trade.setPushRange(trade.getPushRange()+bbdTradePushRangeStep);
            }else{//开始新的一轮推送（pushRange : bbdTradePushRangeInit---bbdTradePushRangeStep--->bbdTradePushRangeThreshold）
                trade.setPushCount(trade.getPushCount()+1);
                //若pushCount == 1,则更tradePush下 tradeNo的所有flag 为0
                if(trade.getPushCount()<bbdTradePushCount){
                    trade.setPushRange(bbdTradePushRangeInit);
                    //更改tradePush下的所有tradeNo相关的flag为0
                    List<TradePush> tradePushList = tradePushDao.getAllTradePushWithTradeNo(trade.getTradeNo());
                    for (TradePush tradePush: tradePushList) {
                        tradePush.setFlag(0);
                        tradePush.setDateUpd(new Date());
                        tradePushDao.save(tradePush);
                    }
                }else{
                    //若pushCount >= 2,则对trade进行兜底处理，变更tradeStatus，不再进行推送
                    trade.setTradeStatus(TradeStatus.LASTOPER);
                }
            }
            logger.info("运单："+trade.getTradeNo()+"推送完成，推送范围："+trade.getPushRange()+",推送次数："+trade.getPushCount());
            trade.setDateUpd(new Date());
            tradeDao.save(trade);
        }
    }

    /**
     * 获取订单配送范围内的所有快递员 -- 抢单模式
     * @param trade
     * @return
     */
    private List<PostmanUser> getPostmanUserList(Trade trade) {
        String poststatus = "1";    //开启接单的用户
        long meter = trade.getPushRange()*1000;
        double x = Numbers.parseDouble(trade.getSender().getLat(),0.0);
        double y = Numbers.parseDouble(trade.getSender().getLon(),0.0);
        MathContext mc = new MathContext(2, RoundingMode.HALF_DOWN);
        double i = new BigDecimal(meter).divide(new BigDecimal(111000), mc).doubleValue();
        double minlat=x-i;
        double maxlat=x+i;
        double minlong=y-i;
        double maxlong=y+i;
        //postrole 0 代表小件员 4 代表站长 poststatus 1 代表司机处于接单状态
        StringBuffer sqlSB = new StringBuffer("SELECT u.*  FROM postmanuser u WHERE ");
        //若商家配置揽件员，则只给附近绑定的揽件员推单
        Relation relation = this.relationService.findByMechId(trade.getuId().toString());
        if(relation != null && relation.getEmbraceIdList() != null && !relation.getEmbraceIdList().isEmpty()){//商家配置揽件员，只给给附近绑定的揽件员推单
            List<Integer> idList = relation.getEmbraceIdList();
            sqlSB.append(" u.id in (");
            for(int n=0; n < idList.size(); n++){
                if(n != 0){
                    sqlSB.append(",");
                }
                sqlSB.append(idList.get(n));
            }
            sqlSB.append(") and ");
        }
        sqlSB.append(" (u.lat BETWEEN "+minlat+" AND "+maxlat+") AND (u.lon BETWEEN "+minlong+" AND "+maxlong+") AND u.sta='1' AND token<>'' AND (u.postrole = '0' or u.postrole='99') AND u.poststatus="+poststatus);
        sqlSB.append("  AND SQRT(POWER("+x+" - u.lat, 2) + POWER("+y+" - u.lon, 2)) < "+i);
        sqlSB.append(" order by SQRT(POWER("+x+" - u.lat, 2) + POWER("+y+" - u.lon, 2)) asc LIMIT 0,20");
        List<PostmanUser> pulist = new ArrayList<PostmanUser>();
        pulist = postmanUserDao.findPostmanUsers(sqlSB.toString());
        return pulist;
    }

    @Override
    public void doJobWithAllPushTradeOneByOne() {
        try {
            //获取到所有需要推送的Trade信息
            List<Trade> tradeList = findTradeListByPushJob();
            if(tradeList!=null&&tradeList.size()>0){
                for (Trade trade : tradeList) {
                    this.pushOneTradeOneByOne(trade);
                }
                logger.info("一波订单推送揽件员完成");
            }else{
                logger.info("暂无订单需要推送给揽件员");
            }
        } catch (Exception e) {
            logger.error("订单推送给揽件员出错：" + e.getMessage());
        }
    }

    /**
     * 单个订单逐个推送给最近的快递员 -- 非抢单模式
     * @param trade
     */
    public void pushOneTradeOneByOne(Trade trade) throws ParseException {
        if(Dates.secondsBetween(trade.getDatePay(), new Date()) > 7200){//从下单到当前在2小时内,推单
            TradePush latestPush = tradePushDao.selectLatestOneByTradeNo(trade.getTradeNo());
            if(Dates.secondsBetween(latestPush.getDateAdd(), new Date()) > 60){//超过60秒未接单，继续推单
                boolean isPushed = false;//是否已推送
                int pushRange = bbdTradePushRangeInit;
                while (!isPushed && pushRange < bbdTradePushRangeThreshold){
                    //获取该运单发送者pushRange公里内最近的bbdTradePushPerNum（20）个揽件员列表,根据距离由近及远排序
                    List<PostmanUser> postmanUserList = this.getNearPostmanUserList(pushRange, trade.getSender().getLat(), trade.getSender().getLon());
                    //循环遍历揽件员
                    for (PostmanUser postmanUser: postmanUserList) {
                        //判断揽件员是否有未处理的订单
                        long waitTradeNum = tradePushDao.selectCountByPMIdAndFlag(postmanUser.getId());
                        if(waitTradeNum == 0){ //揽件员没有待处理订单（空闲），可以推单
                            //判断揽件员是否已推送，如没有则直接进行推送操作，已推送则跳过
                            TradePush tradePush = tradePushDao.findTradePushWithPostmanUserId(trade.getTradeNo(),postmanUser.getId());
                            if(tradePush==null||(tradePush!=null&&tradePush.getFlag()==0)){
                                this.doPushToPostMan(postmanUser.getId(), trade.getTradeNo(), tradePush);
                                //如果推送成功，isPushed = 1;推送不成功，则isPushed一直为0
                                isPushed = true;
                                break;
                            }
                        }/*else{//揽件员有待处理订单(忙)，不可以推单
                            continue;
                        }*/
                    }
                    logger.info("运单："+trade.getTradeNo()+"推送完成，推送范围："+pushRange+",推送次数："+trade.getPushCount());
                    pushRange = pushRange + bbdTradePushRangeStep;
                }
                //如果isPushed 为false，表明未对任何一个快递员进行推送操作 或者没有存在可以推送的快递员 或者 没有一个揽件员接单
                //相当于推送一轮完成，此时将pushCount+1，进行新一轮推送
                if(!isPushed){
                    //开始新的一轮推送（pushRange : bbdTradePushRangeInit---bbdTradePushRangeStep--->bbdTradePushRangeThreshold）
                    trade.setPushCount(trade.getPushCount()+1);//可以不要，两个小时条件取代
                    //更改tradePush下的所有tradeNo相关的flag为0
                    tradePushDao.updateFlagByTradeNo(trade.getTradeNo());
                    trade.setDateUpd(new Date());
                    tradeDao.save(trade);
                }
            }
        }else{//下单超过2个小时，变为已取消，不再进行推送
            trade.setTradeStatus(TradeStatus.LASTOPER);//CANCELED ？
        }
    }

    /**
     * 进行推送操作，
     * @param postmanUserId 揽件员Id
     * @param tradeNo 订单号
     * @param tradePush 订单推送
     */
    private void doPushToPostMan(int postmanUserId, String tradeNo, TradePush tradePush){
        //调用存储过程，记录push指令
        //支付后客户端推送存储  call  `sp_postman_bbdpush_trade`(p_uid INT, p_typ VARCHAR (1),p_trade VARCHAR(32))
        Map<String, Object> map = new HashMap<String,Object>();
        map.put("id",postmanUserId);
        map.put("typ","1");
        map.put("trade",tradeNo);
        postmanUserDao.pushBbdTrade(map);
        //将此揽件员信息记录到tradePush中
        if(tradePush==null) {
            tradePush = new TradePush();
            tradePush.setTradeNo(tradeNo);
            tradePush.setPostmanId(postmanUserId);
            tradePush.setTime(0);
            tradePush.setDateAdd(new Date());
        }else{
            tradePush.setTime(tradePush.getTime()+1);
        }
        tradePush.setFlag(1);
        tradePush.setDateUpd(new Date());
        logger.info("运单："+tradeNo+"推送给小件员："+postmanUserId+"成功");
        tradePushDao.save(tradePush);
    }
    /**
     * 获取离发送者较近的快递员，按照距离排序
     * @param range
     * @param lat
     * @param lng
     * @return
     */
    private List<PostmanUser> getNearPostmanUserList(int range, String lat, String lng) {
        String poststatus = "1";    //开启接单的用户
        long meter = range * 1000;
        double x = Numbers.parseDouble(lat, 0.0);
        double y = Numbers.parseDouble(lng, 0.0);
        MathContext mc = new MathContext(2, RoundingMode.HALF_DOWN);
        double i = new BigDecimal(meter).divide(new BigDecimal(111000), mc).doubleValue();
        double minlat = x - i;
        double maxlat = x + i;
        double minlong = y - i;
        double maxlong = y + i;
        //postrole 0 代表小件员 4 代表站长 poststatus 1 代表司机处于接单状态
        String sql = "SELECT u.*  FROM postmanuser"
                + " u WHERE (u.lat BETWEEN " + minlat + " AND " + maxlat + ") AND (u.lon BETWEEN " + minlong + " AND " + maxlong + ") AND u.sta='1' AND token<>'' AND (u.postrole = '0' or u.postrole='99') AND u.poststatus=" + poststatus + ""
                + "  AND SQRT(POWER(" + x + " - u.lat, 2) + POWER(" + y + " - u.lon, 2)) < " + i
                + " order by SQRT(POWER(" + x + " - u.lat, 2) + POWER(" + y + " - u.lon, 2)) asc LIMIT 0,"+bbdTradePushPerNum;
        List<PostmanUser> pulist = new ArrayList<PostmanUser>();
        pulist = postmanUserDao.findPostmanUsers(sql);
        return pulist;
    }
    /**
     * 分站点根据城市与状态
     * @param city
     * @param type
     * @return
     */
    @Override
    public List<Trade> findTradesBySenderCity(String city, String type) {
        return tradeDao.findTradesBySenderCity(city,type);
    }
    /**
     * 根据运单号和订单号查询
     * @param orderNo 订单号
     * @param mailNum 运单号
     * @return 订单集合
     */
    public List<Trade> findByOrderSnapNo(String orderNo, String mailNum){
        return tradeDao.selectByOrderSnapNo(orderNo, mailNum);
    }
}
